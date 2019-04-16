package com.contentful.vaultintegration.lib.localizedlinks;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;

import java.util.List;

@ContentType("2Ux73cONSU2w2sCe42Gusy")
public final class Container {
  @Field
  private final Resource resource;

  @Field
  private final List<Asset> assets;

  @Field
  private final Asset one;

  public Container(Resource resource, List<Asset> assets, Asset one) {
    this.resource = resource;
    this.assets = assets;
    this.one = one;
  }

  public Resource resource() {
    return resource;
  }

  public List<Asset> assets() {
    return assets;
  }

  public Asset one() {
    return one;
  }
}
