package com.contentful.vaultintegration.lib.allthethings;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;

import java.util.List;
import java.util.Map;

@ContentType("2585ClSxW8OeuMEoIiKW4i")
public class AllTheThingsResource {

  @Field
  private final Resource resource;

  @Field
  private final String text;
  
  @Field
  private final Integer number;
  
  @Field
  private final Double decimal;

  @Field
  private final Boolean yesno;

  @Field
  private final String dateTime;

  @Field
  private final Map location;

  @Field
  private final AllTheThingsResource entry;

  @Field
  private final Asset asset;

  @Field
  private final Map object;

  @Field
  private final List<AllTheThingsResource> entries;

  @Field
  private final List<Asset> assets;

  @Field
  private final List<String> symbols;

  public AllTheThingsResource(Resource resource, String text, Integer number, Double decimal, Boolean yesno, String dateTime, Map location, AllTheThingsResource entry, Asset asset, Map object, List<AllTheThingsResource> entries, List<Asset> assets, List<String> symbols) {
    this.resource = resource;
    this.text = text;
    this.number = number;
    this.decimal = decimal;
    this.yesno = yesno;
    this.dateTime = dateTime;
    this.location = location;
    this.entry = entry;
    this.asset = asset;
    this.object = object;
    this.entries = entries;
    this.assets = assets;
    this.symbols = symbols;
  }

  public Resource resource() {
    return resource;
  }

  public String text() {
    return text;
  }

  public Integer number() {
    return number;
  }

  public Double decimal() {
    return decimal;
  }

  public Boolean yesno() {
    return yesno;
  }

  public String dateTime() {
    return dateTime;
  }

  public Map location() {
    return location;
  }

  public AllTheThingsResource entry() {
    return entry;
  }

  public Asset asset() {
    return asset;
  }

  public Map object() {
    return object;
  }

  public List<AllTheThingsResource> entries() {
    return entries;
  }

  public List<Asset> assets() {
    return assets;
  }

  public List<String> symbols() {
    return symbols;
  }
}