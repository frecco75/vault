package com.contentful.vaultintegration.lib.arraylinks;

import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;

import java.util.List;

@ContentType("product")
public class Product {
  @Field
  private Resource resource;

  @Field
  private String name;

  @Field
  private List<Shop> shops;

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

  public List<Shop> shops() {
    return shops;
  }

  public void shops(List<Shop> shops) {
    this.shops = shops;
  }
}
