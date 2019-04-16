package com.contentful.vaultintegration.lib.arraylinks;

import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;

import java.util.List;

@ContentType("shop")
public class Shop {
  @Field
  private Resource resource;

  @Field
  private String name;

  @Field
  private List<Product> products;

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

  public List<Product> products() {
    return products;
  }

  public void products(List<Product> products) {
    this.products = products;
  }
}
