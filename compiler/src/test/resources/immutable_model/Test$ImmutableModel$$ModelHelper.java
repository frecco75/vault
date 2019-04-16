import android.database.Cursor;
import com.contentful.vault.AssetProxy;
import com.contentful.vault.FieldMeta;
import com.contentful.vault.ModelHelper;
import com.contentful.vault.SpaceHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Test$ImmutableModel$$ModelHelper extends ModelHelper<Test.ImmutableModel, Test$ImmutableModel$$ProxyResource> {
  final List<FieldMeta> fields = new ArrayList<FieldMeta>();

  public Test$ImmutableModel$$ModelHelper() {
    fields.add(FieldMeta.builder().setId("textField").setName("textField").setGetter("textField").setSqliteType("TEXT").build());
    fields.add(FieldMeta.builder().setId("entryLink").setName("entryLink").setGetter("entryLink").setLinkType("ENTRY").build());
    fields.add(FieldMeta.builder().setId("arrayOfModels").setName("arrayOfModels").setGetter("arrayOfModels").setArrayType("Test.ImmutableModel").build());
    fields.add(FieldMeta.builder().setId("mapField").setName("mapField").setGetter("mapField").setSqliteType("BLOB").build());
    fields.add(FieldMeta.builder().setId("mapOfModels").setName("mapOfModels").setGetter("mapOfModels").setSqliteType("BLOB").build());
    fields.add(FieldMeta.builder().setId("asset").setName("asset").setGetter("asset").setLinkType("ASSET").build());
    fields.add(FieldMeta.builder().setId("arrayOfAssets").setName("arrayOfAssets").setGetter("arrayOfAssets").setArrayType("com.contentful.vault.Asset").build());
    fields.add(FieldMeta.builder().setId("arrayOfSymbols").setName("arrayOfSymbols").setGetter("arrayOfSymbols").setSqliteType("BLOB").setArrayType("java.lang.String").build());
  }

  @Override
  public List<FieldMeta> getFields() {
    return fields;
  }

  @Override
  public String getTableName() {
    return "entry_y2lk";
  }

  @Override
  public List<String> getCreateStatements(SpaceHelper spaceHelper) {
    List<String> list = new ArrayList<String>();
    for (String code : spaceHelper.getLocales()) {
      list.add("CREATE TABLE `entry_y2lk$" + code + "` (`remote_id` STRING NOT NULL UNIQUE, `created_at` STRING NOT NULL, `updated_at` STRING, `textField` TEXT, `mapField` BLOB, `mapOfModels` BLOB, `arrayOfSymbols` BLOB);");
    }
    return list;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Test$ImmutableModel$$ProxyResource fromCursor(Cursor cursor) {
    Test$ImmutableModel$$ProxyResource result = new Test$ImmutableModel$$ProxyResource();
    result.setContentType("cid");
    result.textField(cursor.getString(3));
    result.arrayOfSymbols(fieldFromBlob(ArrayList.class, cursor, 6));
    return result;
  }

  @Override
  public Test$ImmutableModel$$ProxyResource fromResource(Test.ImmutableModel resource) {
    return Test$ImmutableModel$$ProxyResource.of(resource);
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean setField(Test$ImmutableModel$$ProxyResource resource, String name, Object value) {
    if ("textField".equals(name)) {
      resource.textField((String) value);
    }
    else if ("entryLink".equals(name)) {
      resource.entryLink((Test$ImmutableModel$$ProxyResource) value);
    }
    else if ("arrayOfModels".equals(name)) {
      resource.arrayOfModels((List<Test$ImmutableModel$$ProxyResource>) value);
    }
    else if ("mapField".equals(name)) {
      resource.mapField((Map<String, Object>) value);
    }
    else if ("mapOfModels".equals(name)) {
      resource.mapOfModels((Map<Long, Test$ImmutableModel$$ProxyResource>) value);
    }
    else if ("asset".equals(name)) {
      resource.asset((AssetProxy) value);
    }
    else if ("arrayOfAssets".equals(name)) {
      resource.arrayOfAssets((List<AssetProxy>) value);
    }
    else if ("arrayOfSymbols".equals(name)) {
      resource.arrayOfSymbols((List<String>) value);
    }
    else {
      return false;
    }
    return true;
  }
}