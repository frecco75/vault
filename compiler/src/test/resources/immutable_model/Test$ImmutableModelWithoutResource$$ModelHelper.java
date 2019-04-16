import android.database.Cursor;
import com.contentful.vault.FieldMeta;
import com.contentful.vault.ModelHelper;
import com.contentful.vault.SpaceHelper;
import java.util.ArrayList;
import java.util.List;

public final class Test$ImmutableModel$$ModelHelper extends ModelHelper<Test.ImmutableModel, Test$ImmutableModel$$ProxyResource> {
  final List<FieldMeta> fields = new ArrayList<FieldMeta>();

  public Test$ImmutableModel$$ModelHelper() {
    fields.add(FieldMeta.builder().setId("textField").setName("textField").setGetter("textField").setSqliteType("TEXT").build());
    fields.add(FieldMeta.builder().setId("entryLink").setName("entryLink").setGetter("entryLink").setLinkType("ENTRY").build());
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
      list.add("CREATE TABLE `entry_y2lk$" + code + "` (`remote_id` STRING NOT NULL UNIQUE, `created_at` STRING NOT NULL, `updated_at` STRING, `textField` TEXT);");
    }
    return list;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Test$ImmutableModel$$ProxyResource fromCursor(Cursor cursor) {
    Test$ImmutableModel$$ProxyResource result = new Test$ImmutableModel$$ProxyResource();
    result.setContentType("cid");
    result.textField(cursor.getString(3));
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
    else {
      return false;
    }
    return true;
  }
}