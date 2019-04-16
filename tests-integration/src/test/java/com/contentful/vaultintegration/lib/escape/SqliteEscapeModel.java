package com.contentful.vaultintegration.lib.escape;

import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;

@ContentType("foo")
public class SqliteEscapeModel {
  @Field
  private final Resource resource;

  @Field
  private final String order;

  public SqliteEscapeModel(Resource resource, String order) {
    this.resource = resource;
    this.order = order;
  }

  public Resource resource() {
    return resource;
  }

  public String order() {
    return order;
  }
}
