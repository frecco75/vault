import com.contentful.vault.Asset;
import com.contentful.vault.AssetProxy;
import com.contentful.vault.ProxyResource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Test$ImmutableModel$$ProxyResource extends ProxyResource<Test.ImmutableModel> {
  Map<String, Test.ImmutableModel> resolvedObjects = new HashMap<String, Test.ImmutableModel>();

  private String textField;

  private Test$ImmutableModel$$ProxyResource entryLink;

  private List<Test$ImmutableModel$$ProxyResource> arrayOfModels;

  private Map<String, Object> mapField;

  private Map<Long, Test$ImmutableModel$$ProxyResource> mapOfModels;

  private AssetProxy asset;

  private List<AssetProxy> arrayOfAssets;

  private List<String> arrayOfSymbols;

  public String textField() {
    return textField;
  }

  public Test$ImmutableModel$$ProxyResource entryLink() {
    return entryLink;
  }

  public List<Test$ImmutableModel$$ProxyResource> arrayOfModels() {
    return arrayOfModels;
  }

  public Map<String, Object> mapField() {
    return mapField;
  }

  public Map<Long, Test$ImmutableModel$$ProxyResource> mapOfModels() {
    return mapOfModels;
  }

  public AssetProxy asset() {
    return asset;
  }

  public List<AssetProxy> arrayOfAssets() {
    return arrayOfAssets;
  }

  public List<String> arrayOfSymbols() {
    return arrayOfSymbols;
  }

  public void textField(String textField) {
    this.textField = textField;
  }

  public void entryLink(Test$ImmutableModel$$ProxyResource entryLink) {
    this.entryLink = entryLink;
  }

  public void arrayOfModels(List<Test$ImmutableModel$$ProxyResource> arrayOfModels) {
    this.arrayOfModels = arrayOfModels;
  }

  public void mapField(Map<String, Object> mapField) {
    this.mapField = mapField;
  }

  public void mapOfModels(Map<Long, Test$ImmutableModel$$ProxyResource> mapOfModels) {
    this.mapOfModels = mapOfModels;
  }

  public void asset(AssetProxy asset) {
    this.asset = asset;
  }

  public void arrayOfAssets(List<AssetProxy> arrayOfAssets) {
    this.arrayOfAssets = arrayOfAssets;
  }

  public void arrayOfSymbols(List<String> arrayOfSymbols) {
    this.arrayOfSymbols = arrayOfSymbols;
  }

  @Override
  public Test.ImmutableModel toOriginalType() {
    if(resolvedObjects.containsKey(remoteId())) {
      return resolvedObjects.get(remoteId());
    }
    Test.ImmutableModel newEntryLink = null;
    if(entryLink != null) {
      newEntryLink = entryLink.toOriginalType();
    }
    List<Test.ImmutableModel> newArrayOfModels = null;
    if(arrayOfModels != null) {
      newArrayOfModels = new ArrayList<>();
      for(Test$ImmutableModel$$ProxyResource tmp : arrayOfModels) {
        newArrayOfModels.add(tmp.toOriginalType());
      }
    }
    Map<Long, Test.ImmutableModel> newMapOfModels = null;
    if(mapOfModels != null) {
      newMapOfModels = new HashMap<>();
      for(Map.Entry<Long, Test$ImmutableModel$$ProxyResource> entry : mapOfModels.entrySet()) {
        newMapOfModels.put(entry.getKey(), entry.getValue().toOriginalType());
      }
    }
    Asset newAsset = null;
    if(asset != null) {
      newAsset = asset.toOriginalType();
    }
    List<Asset> newArrayOfAssets = null;
    if(arrayOfAssets != null) {
      newArrayOfAssets = new ArrayList<>();
      for(AssetProxy tmp : arrayOfAssets) {
        newArrayOfAssets.add(tmp.toOriginalType());
      }
    }
    Test.ImmutableModel result = new Test.ImmutableModel(asResource(), textField, newEntryLink, newArrayOfModels, mapField, newMapOfModels, newAsset, newArrayOfAssets, arrayOfSymbols);
    resolvedObjects.put(remoteId(), result);
    return result;
  }

  public static Test$ImmutableModel$$ProxyResource of(Test.ImmutableModel resource) {
    Test$ImmutableModel$$ProxyResource proxy = new Test$ImmutableModel$$ProxyResource();
    if(resource != null) {
      if(resource.resource() != null) {
        proxy.setRemoteId(resource.resource().remoteId());
        proxy.setCreatedAt(resource.resource().createdAt());
        proxy.setUpdatedAt(resource.resource().updatedAt());
        proxy.setContentType(resource.resource().contentType());
      }
      proxy.textField = resource.textField();
      proxy.entryLink = Test$ImmutableModel$$ProxyResource.of(resource.entryLink());
      if(resource.arrayOfModels() != null) {
        List<Test$ImmutableModel$$ProxyResource> newArrayOfModels = new ArrayList<>();
        for(Test.ImmutableModel tmp : resource.arrayOfModels()) {
          newArrayOfModels.add(Test$ImmutableModel$$ProxyResource.of(tmp));
        }
        proxy.arrayOfModels = newArrayOfModels;
      }
      proxy.mapField = resource.mapField();
      if(resource.mapOfModels() != null) {
        Map<java.lang.Long,Test$ImmutableModel$$ProxyResource> newMapOfModels = new HashMap<>();
        for(Map.Entry<Long, Test.ImmutableModel> entry : resource.mapOfModels().entrySet()) {
          newMapOfModels.put(entry.getKey(), Test$ImmutableModel$$ProxyResource.of(entry.getValue()));
        }
        proxy.mapOfModels = newMapOfModels;
      }
      proxy.asset = AssetProxy.of(resource.asset());
      if(resource.arrayOfAssets() != null) {
        List<com.contentful.vault.AssetProxy> newArrayOfAssets = new ArrayList<>();
        for(Asset tmp : resource.arrayOfAssets()) {
          newArrayOfAssets.add(AssetProxy.of(tmp));
        }
        proxy.arrayOfAssets = newArrayOfAssets;
      }
      proxy.arrayOfSymbols = resource.arrayOfSymbols();
    }
    return proxy;
  }
}