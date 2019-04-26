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

package com.contentful.vaultintegration.lib.vault;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;

import java.util.List;

@ContentType("4rD4z2Ex0kEOgeWCWG4Se2")
public class ArraysResource {
  @Field
  private final Resource resource;

  @Field
  private final List<Asset> assets;

  @Field
  private final List<String> symbols;

  @Field
  private final List<BlobResource> blobs;

  public ArraysResource(Resource resource, List<Asset> assets, List<String> symbols, List<BlobResource> blobs) {
    this.resource = resource;
    this.assets = assets;
    this.symbols = symbols;
    this.blobs = blobs;
  }

  public Resource resource() {
    return resource;
  }

  public List<Asset> assets() {
    return assets;
  }

  public List<String> symbols() {
    return symbols;
  }

  public List<BlobResource> blobs() {
    return blobs;
  }
}
