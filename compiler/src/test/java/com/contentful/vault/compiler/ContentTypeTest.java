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

import com.google.common.base.Joiner;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.contentful.vault.compiler.lib.TestUtils.processors;
import static com.contentful.vault.compiler.lib.TestUtils.readTestResource;
import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaFileObjects.forSourceString;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class ContentTypeTest {
  @Test public void testInjection() throws Exception {
    JavaFileObject source = forSourceString("Test", Joiner.on('\n').join(
        "import com.contentful.vault.Asset;",
        "import com.contentful.vault.ContentType;",
        "import com.contentful.vault.Field;",
        "import com.contentful.vault.Resource;",
        "import java.util.List;",
        "import java.util.Map;",
        "class Test {",
        "  @ContentType(\"cid\")",
        "  static class AwesomeModel {",
        "    @Field Resource resource;",
        "    @Field String textField;",
        "    @Field Boolean booleanField;",
        "    @Field Integer integerField;",
        "    @Field Double doubleField;",
        "    @Field Map mapField;",
        "    @Field Asset assetLink;",
        "    @Field AwesomeModel entryLink;",
        "    @Field List<Asset> arrayOfAssets;",
        "    @Field List<AwesomeModel> arrayOfModels;",
        "    @Field List<String> arrayOfSymbols;",
        "    @Field private String privateField;",
        "    @Field private String privateFluentField;",
        "    public void setPrivateField(String privateField) { this.privateField = privateField; }",
        "    public void privateFluentField(String privateFluentField) { this.privateFluentField = privateFluentField; }",
        "    public String getPrivateField() { return privateField; }",
        "    public String privateFluentField() { return privateFluentField; }",
        "  }",
        "}"
    ));

    JavaFileObject expectedFields = forSourceString(
            "Test$AwesomeModel$Fields",
            readTestResource("awesome_model/Test$AwesomeModel$Fields.java"));

    JavaFileObject expectedProxy = forSourceString(
            "Test$AwesomeModel$$ProxyResource",
            readTestResource("awesome_model/Test$AwesomeModel$$ProxyResource.java"));

    JavaFileObject expectedModelHelper = forSourceString(
        "Test$AwesomeModel$$ModelHelper",
        readTestResource("awesome_model/Test$AwesomeModel$$ModelHelper.java"));

    assert_().about(javaSource()).that(source)
        .processedWith(processors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedFields, expectedProxy, expectedModelHelper);
  }

  @Test public void testInjectionWithImmuableModelAndNoResourceField() throws Exception {
    JavaFileObject source = forSourceString("Test", Joiner.on('\n').join(
            "import com.contentful.vault.ContentType;",
            "import com.contentful.vault.Field;",
            "class Test {",
            "  @ContentType(\"cid\")",
            "  static class ImmutableModel {",
            "    @Field private final String textField;",
            "    @Field private final ImmutableModel entryLink;",
            "    public ImmutableModel(",
            "             String textField,",
            "             ImmutableModel entryLink) {",
            "        this.textField = textField;",
            "        this.entryLink = entryLink;",
            "    }",
            "    public String textField() { return textField; }",
            "    public ImmutableModel entryLink() { return entryLink; }",
            "  }",
            "}"
    ));

    JavaFileObject expectedFields = forSourceString(
            "Test$ImmutableModel$Fields",
            readTestResource("immutable_model/Test$ImmutableModelWithoutResource$Fields.java"));

    JavaFileObject expectedProxy = forSourceString(
            "Test$ImmutableModel$$ProxyResource",
            readTestResource("immutable_model/Test$ImmutableModelWithoutResource$$ProxyResource.java"));

    JavaFileObject expectedModelHelper = forSourceString(
            "Test$ImmutableModel$$ModelHelper",
            readTestResource("immutable_model/Test$ImmutableModelWithoutResource$$ModelHelper.java"));

    assert_().about(javaSource()).that(source)
            .processedWith(processors())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFields, expectedProxy, expectedModelHelper);
  }

  @Test public void testInjectionWitImmutableModel() throws Exception {
    JavaFileObject source = forSourceString("Test", Joiner.on('\n').join(
            "import com.contentful.vault.Asset;",
            "import com.contentful.vault.ContentType;",
            "import com.contentful.vault.Field;",
            "import com.contentful.vault.Resource;",
            "import java.util.List;",
            "import java.util.Map;",
            "class Test {",
            "  @ContentType(\"cid\")",
            "  static class ImmutableModel {",
            "    @Field private final Resource resource;",
            "    @Field private final String textField;",
            "    @Field private final ImmutableModel entryLink;",
            "    @Field private final List<ImmutableModel> arrayOfModels;",
            "    @Field private final Map<String, Object> mapField;",
            "    @Field private final Map<Long, ImmutableModel> mapOfModels;",
            "    @Field private final Asset asset;",
            "    @Field private final List<Asset> arrayOfAssets;",
            "    @Field private final List<String> arrayOfSymbols;",

            "    public ImmutableModel(",
            "             Resource resource,",
            "             String textField,",
            "             ImmutableModel entryLink,",
            "             List<ImmutableModel> arrayOfModels,",
            "             Map<String, Object> mapField,",
            "             Map<Long, ImmutableModel> mapOfModels,",
            "             Asset asset,",
            "             List<Asset> arrayOfAssets,",
            "             List<String> arrayOfSymbols) {",
            "        this.resource = resource;",
            "        this.textField = textField;",
            "        this.entryLink = entryLink;",
            "        this.arrayOfModels = arrayOfModels;",
            "        this.mapField = mapField;",
            "        this.mapOfModels = mapOfModels;",
            "        this.asset = asset;",
            "        this.arrayOfAssets = arrayOfAssets;",
            "        this.arrayOfSymbols = arrayOfSymbols;",
            "    }",
            "    public Resource resource() { return resource; }",
            "    public String textField() { return textField; }",
            "    public ImmutableModel entryLink() { return entryLink; }",
            "    public List<ImmutableModel> arrayOfModels() { return arrayOfModels; }",
            "    public Map<String, Object> mapField() { return mapField; }",
            "    public Map<Long, ImmutableModel> mapOfModels() { return mapOfModels; }",
            "    public Asset asset() { return asset; }",
            "    public List<Asset> arrayOfAssets() { return arrayOfAssets; }",
            "    public List<String> arrayOfSymbols() { return arrayOfSymbols; }",
            "  }",
            "}"
    ));

    JavaFileObject expectedFields = forSourceString(
            "Test$ImmutableModel$Fields",
            readTestResource("immutable_model/Test$ImmutableModel$Fields.java"));

    JavaFileObject expectedProxy = forSourceString(
            "Test$ImmutableModel$$ProxyResource",
            readTestResource("immutable_model/Test$ImmutableModel$$ProxyResource.java"));

    JavaFileObject expectedModelHelper = forSourceString(
            "Test$ImmutableModel$$ModelHelper",
            readTestResource("immutable_model/Test$ImmutableModel$$ModelHelper.java"));

    assert_().about(javaSource()).that(source)
            .processedWith(processors())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFields, expectedProxy, expectedModelHelper);
  }

  @Test public void failsEmptyId() throws Exception {
    JavaFileObject source = forSourceString("Test",
        Joiner.on('\n').join(
            "import com.contentful.vault.ContentType;",
            "@ContentType(\"\")",
            "class Test {",
            "}"
        ));

    assert_().about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining("@ContentType id may not be empty. (Test)");
  }

  @Test public void failsNoFields() throws Exception {
    JavaFileObject source = forSourceString("Test", Joiner.on('\n').join(
        "import com.contentful.vault.ContentType;",
        "@ContentType(\"foo\")",
        "class Test {",
        "}"));

    assert_().about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining("Model must contain at least one @Field element. (Test)");
  }
}
