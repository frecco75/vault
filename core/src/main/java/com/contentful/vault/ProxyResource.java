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

import org.apache.commons.lang3.StringUtils;

public abstract class ProxyResource<T> implements ResourceInterface {

  protected String remoteId;

  protected String createdAt;

  protected String updatedAt;

  protected String contentType;

  @Override
  public String remoteId() {
    return remoteId;
  }

  @Override
  public String createdAt() {
    return createdAt;
  }

  @Override
  public String updatedAt() {
    return updatedAt;
  }

  @Override
  public String contentType() {
    return contentType;
  }

  public void setRemoteId(String remoteId) {
    this.remoteId = remoteId;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  protected String getIdPrefix() {
    return null;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProxyResource)) return false;

    ProxyResource resource = (ProxyResource) o;
    String prefix = StringUtils.defaultString(getIdPrefix(), "");
    if (!prefix.equals(StringUtils.defaultString(resource.getIdPrefix(), ""))) return false;
    return (prefix + remoteId()).equals(prefix + resource.remoteId());
  }

  @Override public int hashCode() {
    return (StringUtils.defaultString(getIdPrefix(), "") + remoteId).hashCode();
  }


  protected Resource asResource() {
    return new Resource(remoteId, createdAt, updatedAt, contentType);
  }

  public abstract T toOriginalType();

}
