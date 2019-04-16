import com.contentful.vault.ProxyResource;
import java.util.HashMap;
import java.util.Map;

public final class Test$ImmutableModel$$ProxyResource extends ProxyResource<Test.ImmutableModel> {
  Map<String, Test.ImmutableModel> resolvedObjects = new HashMap<String, Test.ImmutableModel>();

  private String textField;

  private Test$ImmutableModel$$ProxyResource entryLink;

  public String textField() {
    return textField;
  }

  public Test$ImmutableModel$$ProxyResource entryLink() {
    return entryLink;
  }

  public void textField(String textField) {
    this.textField = textField;
  }

  public void entryLink(Test$ImmutableModel$$ProxyResource entryLink) {
    this.entryLink = entryLink;
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
    Test.ImmutableModel result = new Test.ImmutableModel(textField, newEntryLink);
    resolvedObjects.put(remoteId(), result);
    return result;
  }

  public static Test$ImmutableModel$$ProxyResource of(Test.ImmutableModel resource) {
    Test$ImmutableModel$$ProxyResource proxy = new Test$ImmutableModel$$ProxyResource();
    if(resource != null) {
      proxy.textField = resource.textField();
      proxy.entryLink = Test$ImmutableModel$$ProxyResource.of(resource.entryLink());
    }
    return proxy;
  }
}