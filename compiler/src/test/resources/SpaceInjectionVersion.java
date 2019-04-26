import com.contentful.vault.ModelHelper;
import com.contentful.vault.SpaceHelper;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Test$AwesomeSpace$$SpaceHelper extends SpaceHelper {
  final Map<Class<?>, ModelHelper<?, ?>> models = new LinkedHashMap<Class<?>, ModelHelper<?, ?>>();

  final Map<String, Class<?>> types = new LinkedHashMap<String, Class<?>>();

  final List<String> locales = Arrays.asList("foo");

  public Test$AwesomeSpace$$SpaceHelper() {
    models.put(Test.Model.class, new Test$Model$$ModelHelper());
    types.put("cid", Test.Model.class);
  }

  @Override
  public String getDatabaseName() {
    return "space_c2lk";
  }

  @Override
  public int getDatabaseVersion() {
    return 9;
  }

  @Override
  public Map<Class<?>, ModelHelper<?, ?>> getModels() {
    return models;
  }

  @Override
  public Map<String, Class<?>> getTypes() {
    return types;
  }

  @Override
  public String getCopyPath() {
    return null;
  }

  @Override
  public String getSpaceId() {
    return "sid";
  }

  @Override
  public List<String> getLocales() {
    return locales;
  }
}