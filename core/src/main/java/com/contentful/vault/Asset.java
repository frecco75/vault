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

import android.os.Parcel;
import android.os.Parcelable;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

import static com.contentful.java.cda.CDAType.ASSET;

public final class Asset implements Parcelable, ResourceInterface {

  private final Resource resource;

  private final String url;

  private final String mimeType;

  private final String title;

  private final String description;

  private final HashMap<String, Object> file;

  Asset(Builder builder) {
    this.resource = builder.resource;
    this.url = builder.url;
    this.mimeType = builder.mimeType;
    this.title = builder.title;
    this.description = builder.description;
    this.file = builder.file;
  }

  public String url() {
    return url;
  }

  public String mimeType() {
    return mimeType;
  }

  public String title() {
    return title;
  }

  public String description() {
    return description;
  }

  public HashMap<String, Object> file() {
    return file;
  }


  static Builder builder() {
    return new Builder();
  }

  @Override
  public String remoteId() {
    return resource != null ? resource.remoteId() : null;
  }

  @Override
  public String createdAt() {
    return resource != null ? resource.createdAt() : null;
  }

  @Override
  public String updatedAt() {
    return resource != null ? resource.updatedAt() : null;
  }

  @Override
  public String contentType() {
    return resource != null ? resource.contentType() : null;
  }

  private String getIdPrefix() {
    return ASSET.toString();
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Asset)) return false;

    Asset resource = (Asset) o;
    String prefix = StringUtils.defaultString(getIdPrefix(), "");
    if (!prefix.equals(StringUtils.defaultString(resource.getIdPrefix(), ""))) return false;
    return (prefix + remoteId()).equals(prefix + resource.remoteId());
  }

  static class Builder {
    Resource resource;
    String url;
    String mimeType;
    String title;
    String description;
    HashMap<String, Object> file;

    public Builder setResource(Resource resource) {
      this.resource = resource;
      return this;
    }

    public Builder setUrl(String url) {
      this.url = url;
      return this;
    }

    public Builder setMimeType(String mimeType) {
      this.mimeType = mimeType;
      return this;
    }

    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    public Builder setFile(HashMap<String, Object> file) {
      this.file = file;
      return this;
    }

    public Asset build() {
      return new Asset(this);
    }
  }

  // Parcelable
  public int describeContents() {
    return 0;
  }

  public void writeToParcel(Parcel out, int flags) {
    out.writeString(resource.remoteId());

    out.writeString(resource.createdAt());

    writeOptionalString(out, resource.updatedAt());

    out.writeString(url);

    out.writeString(mimeType);

    writeOptionalString(out, title());

    writeOptionalString(out, description());

    if (file == null) {
      out.writeInt(-1);
    } else {
      out.writeInt(1);
      out.writeSerializable(file);
    }
  }

  private void writeOptionalString(Parcel out, String s) {
    if (s == null) {
      out.writeInt(-1);
    } else {
      out.writeInt(1);
      out.writeString(s);
    }
  }

  public static final Parcelable.Creator<Asset> CREATOR
      = new Parcelable.Creator<Asset>() {
    public Asset createFromParcel(Parcel in) {
      return new Asset(in);
    }

    public Asset[] newArray(int size) {
      return new Asset[size];
    }
  };

  @SuppressWarnings("unchecked")
  Asset(Parcel in) {
    resource = new Resource(in.readString(), in.readString(), in.readInt() != -1 ? in.readString() : null, null);

    this.url = in.readString();

    this.mimeType = in.readString();

    if (in.readInt() == -1) {
      this.title = null;
    } else {
      this.title = in.readString();
    }

    if (in.readInt() == -1) {
      this.description = null;
    } else {
      this.description = in.readString();
    }

    if (in.readInt() == -1) {
      this.file = null;
    } else {
      this.file = (HashMap<String, Object>) in.readSerializable();
    }
  }

  public static final class Fields extends BaseFields {
    public static final String URL = "url";

    public static final String MIME_TYPE = "mime_type";

    public static final String TITLE = "title";

    public static final String DESCRIPTION = "description";

    public static final String FILE = "file";
  }
}
