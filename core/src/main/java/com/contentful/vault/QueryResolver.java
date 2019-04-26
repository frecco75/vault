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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.text.TextUtils.join;
import static com.contentful.vault.Sql.*;

final class QueryResolver<T> {
  private final AbsQuery<T, ?> query;

  private final Vault vault;

  private final Map<String, ProxyResource<?>> assets = new HashMap<>();

  private final Map<String, ProxyResource<?>> entries = new HashMap<>();

  QueryResolver(AbsQuery<T, ?> query) {
    this.query = query;
    this.vault = query.vault();
  }

  List<T> all(boolean resolveLinks, String locale) {
    Cursor cursor = cursorFromQuery(query, locale);
    List<ProxyResource<T>> result = new ArrayList<>();
    try {
      if (cursor.moveToFirst()) {
        Map<String, ProxyResource<?>> cache = cacheForType(query.type());
        do {
          ProxyResource<T> item = vault.getSqliteHelper().fromCursor(query.type(), cursor);
          if (item == null) {
            continue;
          }
          result.add(item);
          cache.put(item.remoteId(), item);
        } while (cursor.moveToNext());
      }
    } finally {
      cursor.close();
    }

    if (resolveLinks && query.type() != Asset.class && !result.isEmpty()) {
      resolveLinks(result, locale);
    }

    //Conversion to original type
    List<T> resources = new ArrayList<>();
    for(ProxyResource<T> proxyResource : result) {
      resources.add(proxyResource.toOriginalType());
    }

    return resources;
  }

  private Map<String, ProxyResource<?>> cacheForType(Class<T> type) {
    if (type == Asset.class) {
      return assets;
    }
    return entries;
  }

  private void resolveLinks(List<ProxyResource<T>> resources, String locale) {
    LinkResolver resolver = new LinkResolver(query, assets, entries);
    for (ProxyResource<T> resource : resources) {
      resolver.resolveLinks(resource, helperForEntry(resource).getFields(), locale);
    }
  }

  private ModelHelper<?,?> helperForEntry(ProxyResource<T> resource) {
    SpaceHelper spaceHelper = vault.getSqliteHelper().getSpaceHelper();
    Class<?> modelType = spaceHelper.getTypes().get(resource.contentType());
    return spaceHelper.getModels().get(modelType);
  }

  private Cursor cursorFromQuery(AbsQuery<T, ?> query, String locale) {
    String[] orderArray = query.params().order();
    String order = null;
    if (orderArray != null && orderArray.length > 0) {
      order = join(", ", orderArray);
    }
    String tableName;
    if (query.type() == Asset.class) {
      tableName = TABLE_ASSETS;
    } else {
      tableName = query.vault()
          .getSqliteHelper()
          .getSpaceHelper()
          .getModels()
          .get(query.type())
          .getTableName();
    }
    return query.vault().getReadableDatabase().query(
        escape(localizeName(tableName, locale)),
        null,                           // columns
        query.params().selection(),     // selection
        query.params().selectionArgs(), // selectionArgs
        null,                           // groupBy
        null,                           // having
        order,                          // order
        query.params().limit());        // limit
  }
}
