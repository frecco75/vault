package com.contentful.vaultintegration.lib.links;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;

import java.util.List;

@ContentType("5QVPSeE41yo28mA4sU6gIo")
public class AssetsContainer {
  @Field
  private final Resource resource;

  @Field
  private final List<Asset> assets;

  public AssetsContainer(Resource resource, List<Asset> assets) {
    this.resource = resource;
    this.assets = assets;
  }

  public Resource resource() {
    return resource;
  }

  public List<Asset> assets() {
    return assets;
  }
}
