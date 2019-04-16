/*
 * Copyright (C) 2018 Contentful GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.contentful.vault.compiler;

import com.contentful.vault.FieldMeta;
import com.contentful.vault.ModelHelper;
import com.contentful.vault.SpaceHelper;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.*;

import static com.contentful.vault.Sql.RESOURCE_COLUMNS;

final class ModelInjection extends Injection {
  final String sqlTableName;

  final Set<FieldMeta> fields;

  private final Set<FieldMeta> directFields;

  private final Map<FieldMeta, TypeName> proxies;

  private FieldSpec specFields;

  private final ClassName modelBuilderClass;

  public ModelInjection(String remoteId, ClassName className, ClassName modelBuilderClass, TypeElement originatingElement,
                        String sqlTableName, Set<FieldMeta> fields, Map<FieldMeta, TypeName> proxies) {
    super(remoteId, className, originatingElement);
    this.modelBuilderClass = modelBuilderClass;
    this.sqlTableName = sqlTableName;
    this.fields = fields;
    directFields = extractDirectFields(fields);
    this.proxies = proxies;
  }

  @Override TypeSpec.Builder getTypeSpecBuilder() {
    ParameterizedTypeName modelHelperType = ParameterizedTypeName.get(ClassName.get(ModelHelper.class), ClassName.get(originatingElement), modelBuilderClass);

    TypeSpec.Builder builder = TypeSpec.classBuilder(className.simpleName())
            .superclass(modelHelperType)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

    appendFields(builder);
    appendTableName(builder);
    appendCreateStatements(builder);
    appendFromCursor(builder);
    appendFromResource(builder);
    appendSetField(builder);
    appendConstructor(builder);

    return builder;
  }

  @SuppressWarnings("unchecked")
  private void appendConstructor(TypeSpec.Builder builder) {
    MethodSpec.Builder ctor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

    for (FieldMeta f : fields) {
      CodeBlock.Builder block = CodeBlock.builder();

      block.add("$N.add($T.builder()", specFields, ClassName.get(FieldMeta.class))
              .add(".setId($S)", f.id())
              .add(".setName($S)", f.name());

      if (f.setter() != null) {
        block.add(".setSetter($S)", f.setter());
      }

      if (f.getter() != null) {
        block.add(".setGetter($S)", f.getter());
      }

      if (f.sqliteType() != null) {
        block.add(".setSqliteType($S)", f.sqliteType());
      }

      if (f.isLink()) {
        block.add(".setLinkType($S)", f.linkType());
      }

      if (f.isArray()) {
        block.add(".setArrayType($S)", f.arrayType());
      }

      block.add(".build());\n");

      ctor.addCode(block.build());
    }

    builder.addMethod(ctor.build());
  }

  private void appendSetField(TypeSpec.Builder builder) {
    MethodSpec.Builder method = MethodSpec.methodBuilder("setField")
            .addAnnotation(Override.class)
            .addAnnotation(
                    AnnotationSpec.builder(SuppressWarnings.class)
                            .addMember("value", "$S", "unchecked")
                            .build())
            .addModifiers(Modifier.PUBLIC)
            .returns(boolean.class)
            .addParameter(ParameterSpec.builder(modelBuilderClass, "resource").build())
            .addParameter(ParameterSpec.builder(ClassName.get(String.class), "name").build())
            .addParameter(ParameterSpec.builder(ClassName.get(Object.class), "value").build());

    FieldMeta[] array = fields.toArray(new FieldMeta[fields.size()]);
    boolean firstParam = true;
    for (int i = 0; i < array.length; i++) {
      FieldMeta field = array[i];
      if(field.isResource()) {
        continue;
      }

      if (firstParam) {
        method.beginControlFlow("if ($S.equals(name))", field.name());
        firstParam = false;
      } else {
        method.endControlFlow().beginControlFlow("else if ($S.equals(name))", field.name());
      }
      TypeName proxyType = proxies.get(field);
      method.addStatement("resource.$L(($T) value)", field.name(), proxyType != null ? proxyType : field.type());
    }
    method.endControlFlow()
            .beginControlFlow("else")
            .addStatement("return false")
            .endControlFlow()
            .addStatement("return true");

    builder.addMethod(method.build());
  }

  private void appendFromCursor(TypeSpec.Builder builder) {
    MethodSpec.Builder method = MethodSpec.methodBuilder("fromCursor")
            .returns(modelBuilderClass)
            .addAnnotation(Override.class)
            .addAnnotation(
                    AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unchecked").build())
            .addModifiers(Modifier.PUBLIC)
            .addParameter(
                    ParameterSpec.builder(ClassName.get("android.database", "Cursor"), "cursor").build());

    String result = "result";
    method.addStatement("$T $N = new $T()", modelBuilderClass, result, modelBuilderClass)
            .addStatement("$N.setContentType($S)", result, remoteId);

    int columnIndex = RESOURCE_COLUMNS.length;
    for (FieldMeta field : directFields) {
      String fqClassName = field.type().toString();

      if (String.class.getName().equals(fqClassName)) {
        method.addStatement("$N.$L(cursor.getString($L))", result, field.name(), columnIndex);
      } else if (Boolean.class.getName().equals(fqClassName)) {
        method.addStatement("$N.$L(Integer.valueOf(1).equals(cursor.getInt($L)))", result, field.name(), columnIndex);
      } else if (Integer.class.getName().equals(fqClassName)) {
        method.addStatement("$N.$L(cursor.getInt($L))", result, field.name(), columnIndex);
      } else if (Double.class.getName().equals(fqClassName)) {
        method.addStatement("$N.$L(cursor.getDouble($L))", result, field.name(), columnIndex);
      } else if (Map.class.getName().equals(fqClassName)) {
        method.addStatement("$N.$L(fieldFromBlob($T.class, cursor, $L))", result, field.name(), ClassName.get(HashMap.class), columnIndex);
      } else if (field.isArrayOfSymbols()) {
        method.addStatement("$N.$L(fieldFromBlob($T.class, cursor, $L))", result, field.name(), ClassName.get(ArrayList.class), columnIndex);
      }
      columnIndex++;
    }

    method.addStatement("return $N", result);
    builder.addMethod(method.build());
  }

  private void appendFromResource(TypeSpec.Builder builder) {
    String resource = "resource";
    MethodSpec.Builder methodSpec = MethodSpec.methodBuilder("fromResource")
            .returns(modelBuilderClass)
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ClassName.get(originatingElement), resource);

    methodSpec.addStatement("return $T.of($N)", modelBuilderClass, resource);
    builder.addMethod(methodSpec.build());
  }

  private void appendCreateStatements(TypeSpec.Builder builder) {
    MethodSpec.Builder method = MethodSpec.methodBuilder("getCreateStatements")
            .returns(ParameterizedTypeName.get(List.class, String.class))
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(SpaceHelper.class, "spaceHelper");

    method.addStatement("$T list = new $T()",
            ParameterizedTypeName.get(List.class, String.class),
            ParameterizedTypeName.get(ArrayList.class, String.class));


    method.beginControlFlow("for (String code : spaceHelper.getLocales())");
    for (String sql : getModelCreateStatements()) {
      method.addStatement("list.add($L)", sql);
    }
    method.endControlFlow();

    method.addStatement("return list");
    builder.addMethod(method.build());
  }

  private void appendTableName(TypeSpec.Builder builder) {
    builder.addMethod(MethodSpec.methodBuilder("getTableName")
            .returns(ClassName.get(String.class))
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return $S", sqlTableName)
            .build());
  }

  private void appendFields(TypeSpec.Builder builder) {
    // Field
    specFields = createListWithInitializer("fields", ArrayList.class,
            ClassName.get(FieldMeta.class)).addModifiers(Modifier.FINAL).build();

    builder.addField(specFields);

    // Getter
    builder.addMethod(createGetterImpl(specFields, "getFields").build());
  }

  List<String> getModelCreateStatements() {
    List<String> statements = new ArrayList<>();
    StringBuilder builder = new StringBuilder();
    builder.append("\"CREATE TABLE `")
            .append(sqlTableName)
            .append("$\" + code + \"")
            .append("` (");

    for (int i = 0; i < RESOURCE_COLUMNS.length; i++) {
      builder.append(RESOURCE_COLUMNS[i]);
      if (i < RESOURCE_COLUMNS.length - 1) {
        builder.append(", ");
      }
    }


    for (FieldMeta f : directFields) {
      builder.append(", `")
              .append(f.name())
              .append("` ")
              .append(f.sqliteType());
    }
    builder.append(");\"");
    statements.add(builder.toString());
    return statements;
  }

  private Set<FieldMeta> extractDirectFields(final Set<FieldMeta> fields) {
    Set<FieldMeta> result = new LinkedHashSet<>();
    for (FieldMeta f : fields) {
      // Skip links / arrays of links / resources
      if (f.isLink() || f.isArrayOfLinks() || f.isResource()) {
        continue;
      }
      result.add(f);
    }
    return result;
  }
}
