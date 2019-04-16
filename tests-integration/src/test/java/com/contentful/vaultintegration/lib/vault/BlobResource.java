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

import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;

import java.util.Map;

@ContentType("1HRG7uai2g8YMswwqoAaC8")
public class BlobResource {
  @Field
  private final Resource resource;

  @Field
  private final Map object;

  public BlobResource(Resource resource, Map object) {
    this.resource = resource;
    this.object = object;
  }

  public Resource resource() {
    return resource;
  }

  public Map object() {
    return object;
  }
}