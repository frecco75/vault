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

import java.util.HashMap;
import java.util.Map;

import static com.contentful.java.cda.CDAType.ASSET;

public final class AssetProxy extends ProxyResource<Asset> {

  private final Map<String, Asset> resolvedAssets = new HashMap<>();

  private final Asset asset;

  private AssetProxy(Asset asset) {
    this.asset = asset;
    if(asset != null) {
      this.remoteId = asset.remoteId();
      this.createdAt = asset.createdAt();
      this.updatedAt = asset.updatedAt();
      this.contentType = asset.contentType();
    }
  }

  @Override
  protected String getIdPrefix() {
    return ASSET.toString();
  }

  @Override
  public Asset toOriginalType() {
    if(resolvedAssets.containsKey(remoteId)) {
      return resolvedAssets.get(remoteId);
    }
    
    Asset tmp = this.asset != null ? this.asset : Asset.builder().build();
    Asset resolvedAsset = new Asset.Builder()
            .setDescription(tmp.description())
            .setFile(tmp.file())
            .setMimeType(tmp.mimeType())
            .setTitle(tmp.title())
            .setUrl(tmp.url())
            .setResource(asResource())
            .build();

    resolvedAssets.put(remoteId, resolvedAsset);
    return resolvedAsset;
  }

  public static AssetProxy of(Asset asset) {
    return new AssetProxy(asset);
  }
}
