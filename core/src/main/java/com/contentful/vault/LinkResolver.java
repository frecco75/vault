/*
 * Copyright (C) 2018 Contentful GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.contentful.vault;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.contentful.java.cda.CDAType.ASSET;
import static com.contentful.vault.BaseFields.REMOTE_ID;
import static com.contentful.vault.Sql.*;

final class LinkResolver {
  private static final String LINKS_WHERE_CLAUSE =
      "l.parent = ? AND l.is_asset = ? AND l.field = ?";

  private static final String QUERY_ENTRY_TYPE = String.format(
      "SELECT `type_id` FROM %s WHERE %s = ?", TABLE_ENTRY_TYPES, REMOTE_ID);

  private final AbsQuery<?, ?> query;

  private final Map<String, ProxyResource<?>> assets;

  private final Map<String, ProxyResource<?>> entries;

  LinkResolver(AbsQuery<?, ?> query, Map<String, ProxyResource<?>> assets, Map<String, ProxyResource<?>> entries) {
    this.query = query;
    this.assets = assets;
    this.entries = entries;
  }

  void resolveLinks(ProxyResource<?> resource, List<FieldMeta> links, String locale) {
    for (FieldMeta field : links) {
      if (field.isLink() || field.isArrayOfLinks()) {
        resolveLinksForField(resource, field, locale);
      }
    }
  }

  private <T> void resolveLinksForField(ProxyResource<T> resource, FieldMeta field, String locale) {
    List<Link> links = fetchLinks(resource.remoteId(), field, locale);
    List<ProxyResource> targets = null;
    if (links.size() > 0) {
      targets = new ArrayList<>();
      for (Link link : links) {
        ProxyResource<?> child = getCachedResourceOrFetch(link, locale);
        if (child != null) {
          targets.add(child);
        }
      }
    }

    Object result = null;
    if (field.isArray()) {
      if (targets == null) {
        result = Collections.emptyList();
      } else {
        result = Collections.unmodifiableList(targets);
      }
    } else if (targets != null && targets.size() > 0) {
      result = targets.get(0);
    }
    if (result != null) {
      ModelHelper<T, ProxyResource<T>> modelHelper = getHelperForEntry(resource);
      modelHelper.setField(resource, field.name(), result);
    }
  }

  private ProxyResource<?> getCachedResourceOrFetch(Link link, String locale) {
    boolean linksToAsset = link.isAsset();
    Map<String, ProxyResource<?>> cache = linksToAsset ? assets : entries;
    ProxyResource<?> child = cache.get(link.child());
    if (child == null) {
      // Link target not found in cache, fetch from DB
      child = resourceForLink(link, locale);
      if (child != null) {
        // Put into cache
        cache.put(child.remoteId(), child);

        if (!linksToAsset) {
          // Resolve links for linked target
          resolveLinks(child, getHelperForEntry(child).getFields(), locale);
        }
      }
    }
    return child;
  }

  @SuppressWarnings("unchecked")
  private <T> ModelHelper<T, ProxyResource<T>> getHelperForEntry(ProxyResource<T> resource) {
    SpaceHelper spaceHelper = query.vault().getSqliteHelper().getSpaceHelper();
    Class<?> modelType = spaceHelper.getTypes().get(resource.contentType());
    return (ModelHelper<T, ProxyResource<T>>) spaceHelper.getModels().get(modelType);
  }

  @SuppressWarnings("unchecked")
  private <T> ModelHelper<T, ProxyResource<T>> getHelperForEntry(T object) {
    SpaceHelper spaceHelper = query.vault().getSqliteHelper().getSpaceHelper();
    return (ModelHelper<T, ProxyResource<T>>) spaceHelper.getModels().get(object.getClass());
  }

  private List<Link> fetchLinks(String parentId, FieldMeta field, String locale) {
    List<Link> result;
    boolean linksToAssets = isLinkForAssets(field);
    String sql = linksToAssets ? queryAssetLinks(locale) : queryEntryLinks(locale);

    String[] args = new String[] {
        parentId,
        linksToAssets ? "1" : "0",
        field.id()
    };

    Cursor cursor = query.vault().getReadableDatabase().rawQuery(sql, args);
    try {
      result = new ArrayList<>();
      if (cursor.moveToFirst()) {
        do {
          String childId = cursor.getString(0);
          String childContentType = cursor.getString(1);
          result.add(new Link(parentId, childId, field.id(), childContentType));
        } while (cursor.moveToNext());
      }
    } finally {
      cursor.close();
    }

    return result;
  }

  private String queryAssetLinks(String locale) {
    return String.format("SELECT l.child, null FROM %s l WHERE %s ORDER BY l.position",
        escape(localizeName(TABLE_LINKS, locale)),
        LINKS_WHERE_CLAUSE);
  }

  private String queryEntryLinks(String locale) {
    return String.format(
        "SELECT l.child, t.type_id FROM %s l INNER JOIN %s t ON l.child = t.%s WHERE %s ORDER BY l.position",
        escape(localizeName(TABLE_LINKS, locale)),
        TABLE_ENTRY_TYPES,
        REMOTE_ID,
        LINKS_WHERE_CLAUSE);
  }

  private boolean isLinkForAssets(FieldMeta field) {
    String linkType = field.linkType();
    if (linkType != null) {
      linkType = linkType.toUpperCase(Vault.LOCALE);
    }
    return ASSET.toString().equals(linkType) || Asset.class.getName().equals(field.arrayType());
  }

  public static String fetchEntryType(SQLiteDatabase db, String remoteId) {
    String args[] = new String[]{ remoteId };
    String result = null;
    Cursor cursor = db.rawQuery(QUERY_ENTRY_TYPE, args);
    try {
      if (cursor.moveToFirst()) {
        result = cursor.getString(0);
      }
    } finally {
      cursor.close();
    }
    return result;
  }

  private ProxyResource<?> resourceForLink(Link link, String locale) {
    Object resource = null;
    Class<?> clazz;
    if (link.isAsset()) {
      clazz = Asset.class;
    } else {
      clazz = query.vault()
          .getSqliteHelper()
          .getSpaceHelper()
          .getTypes()
          .get(link.childContentType());
    }
    if (clazz != null) {
      resource = query.vault().fetch(clazz)
          .where(REMOTE_ID + " = ?", link.child())
          .resolveFirst(false, locale);
    }
    return proxy(resource);
  }

  private ProxyResource<?> proxy(Object object) {
    if(object == null) {
      return null;
    }

    if(object instanceof Asset) {
      return AssetProxy.of((Asset) object);
    }

    return getHelperForEntry(object).fromResource(object);

  }

}
