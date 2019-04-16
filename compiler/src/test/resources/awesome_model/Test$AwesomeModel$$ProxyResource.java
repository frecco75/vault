import com.contentful.vault.Asset;
import com.contentful.vault.AssetProxy;
import com.contentful.vault.ProxyResource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Test$AwesomeModel$$ProxyResource extends ProxyResource<Test.AwesomeModel> {
  Map<String, Test.AwesomeModel> resolvedObjects = new HashMap<String, Test.AwesomeModel>();

  private String textField;

  private Boolean booleanField;

  private Integer integerField;

  private Double doubleField;

  private Map mapField;

  private AssetProxy assetLink;

  private Test$AwesomeModel$$ProxyResource entryLink;

  private List<AssetProxy> arrayOfAssets;

  private List<Test$AwesomeModel$$ProxyResource> arrayOfModels;

  private List<String> arrayOfSymbols;

  private String privateField;

  private String privateFluentField;

  public String textField() {
    return textField;
  }

  public Boolean booleanField() {
    return booleanField;
  }

  public Integer integerField() {
    return integerField;
  }

  public Double doubleField() {
    return doubleField;
  }

  public Map mapField() {
    return mapField;
  }

  public AssetProxy assetLink() {
    return assetLink;
  }

  public Test$AwesomeModel$$ProxyResource entryLink() {
    return entryLink;
  }

  public List<AssetProxy> arrayOfAssets() {
    return arrayOfAssets;
  }

  public List<Test$AwesomeModel$$ProxyResource> arrayOfModels() {
    return arrayOfModels;
  }

  public List<String> arrayOfSymbols() {
    return arrayOfSymbols;
  }

  public String privateField() {
    return privateField;
  }

  public String privateFluentField() {
    return privateFluentField;
  }

  public void textField(String textField) {
    this.textField = textField;
  }

  public void booleanField(Boolean booleanField) {
    this.booleanField = booleanField;
  }

  public void integerField(Integer integerField) {
    this.integerField = integerField;
  }

  public void doubleField(Double doubleField) {
    this.doubleField = doubleField;
  }

  public void mapField(Map mapField) {
    this.mapField = mapField;
  }

  public void assetLink(AssetProxy assetLink) {
    this.assetLink = assetLink;
  }

  public void entryLink(Test$AwesomeModel$$ProxyResource entryLink) {
    this.entryLink = entryLink;
  }

  public void arrayOfAssets(List<AssetProxy> arrayOfAssets) {
    this.arrayOfAssets = arrayOfAssets;
  }

  public void arrayOfModels(List<Test$AwesomeModel$$ProxyResource> arrayOfModels) {
    this.arrayOfModels = arrayOfModels;
  }

  public void arrayOfSymbols(List<String> arrayOfSymbols) {
    this.arrayOfSymbols = arrayOfSymbols;
  }

  public void privateField(String privateField) {
    this.privateField = privateField;
  }

  public void privateFluentField(String privateFluentField) {
    this.privateFluentField = privateFluentField;
  }

  @Override
  public Test.AwesomeModel toOriginalType() {
    if(resolvedObjects.containsKey(remoteId())) {
      return resolvedObjects.get(remoteId());
    }
    Test.AwesomeModel result = new Test.AwesomeModel();
    resolvedObjects.put(remoteId(), result);
    Asset newAssetLink = null;
    if(assetLink != null) {
      newAssetLink = assetLink.toOriginalType();
    }
    Test.AwesomeModel newEntryLink = null;
    if(entryLink != null) {
      newEntryLink = entryLink.toOriginalType();
    }
    List<Asset> newArrayOfAssets = null;
    if(arrayOfAssets != null) {
      newArrayOfAssets = new ArrayList<>();
      for(AssetProxy tmp : arrayOfAssets) {
        newArrayOfAssets.add(tmp.toOriginalType());
      }
    }
    List<Test.AwesomeModel> newArrayOfModels = null;
    if(arrayOfModels != null) {
      newArrayOfModels = new ArrayList<>();
      for(Test$AwesomeModel$$ProxyResource tmp : arrayOfModels) {
        newArrayOfModels.add(tmp.toOriginalType());
      }
    }
    result.resource = asResource();
    result.textField = textField;
    result.booleanField = booleanField;
    result.integerField = integerField;
    result.doubleField = doubleField;
    result.mapField = mapField;
    result.assetLink = newAssetLink;
    result.entryLink = newEntryLink;
    result.arrayOfAssets = newArrayOfAssets;
    result.arrayOfModels = newArrayOfModels;
    result.arrayOfSymbols = arrayOfSymbols;
    result.setPrivateField(privateField);
    result.privateFluentField(privateFluentField);
    return result;
  }

  public static Test$AwesomeModel$$ProxyResource of(Test.AwesomeModel resource) {
    Test$AwesomeModel$$ProxyResource proxy = new Test$AwesomeModel$$ProxyResource();
    if(resource != null) {
      if(resource.resource != null) {
        proxy.setRemoteId(resource.resource.remoteId());
        proxy.setCreatedAt(resource.resource.createdAt());
        proxy.setUpdatedAt(resource.resource.updatedAt());
        proxy.setContentType(resource.resource.contentType());
      }
      proxy.textField = resource.textField;
      proxy.booleanField = resource.booleanField;
      proxy.integerField = resource.integerField;
      proxy.doubleField = resource.doubleField;
      proxy.mapField = resource.mapField;
      proxy.assetLink = AssetProxy.of(resource.assetLink);
      proxy.entryLink = Test$AwesomeModel$$ProxyResource.of(resource.entryLink);
      if(resource.arrayOfAssets != null) {
        List<com.contentful.vault.AssetProxy> newArrayOfAssets = new ArrayList<>();
        for(Asset tmp : resource.arrayOfAssets) {
          newArrayOfAssets.add(AssetProxy.of(tmp));
        }
        proxy.arrayOfAssets = newArrayOfAssets;
      }
      if(resource.arrayOfModels != null) {
        List<Test$AwesomeModel$$ProxyResource> newArrayOfModels = new ArrayList<>();
        for(Test.AwesomeModel tmp : resource.arrayOfModels) {
          newArrayOfModels.add(Test$AwesomeModel$$ProxyResource.of(tmp));
        }
        proxy.arrayOfModels = newArrayOfModels;
      }
      proxy.arrayOfSymbols = resource.arrayOfSymbols;
      proxy.privateField = resource.getPrivateField();
      proxy.privateFluentField = resource.privateFluentField();
    }
    return proxy;
  }
}