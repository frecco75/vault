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

package com.contentful.vaultintegration.lib.demo;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;

@ContentType("cat")
public class Cat {
  @Field
  private Resource resource;

  @Field
  private String name;

  @Field
  private Cat bestFriend;

  @Field
  private Asset image;

  public Resource resource() {
    return resource;
  }

  public void resource(Resource resource) {
    this.resource = resource;
  }

  public String name() {
    return name;
  }

  public void name(String name) {
    this.name = name;
  }

  public Cat bestFriend() {
    return bestFriend;
  }

  public void bestFriend(Cat bestFriend) {
    this.bestFriend = bestFriend;
  }

  public Asset image() {
    return image;
  }

  public void image(Asset image) {
    this.image = image;
  }
}
