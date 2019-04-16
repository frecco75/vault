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
import com.contentful.vault.ProxyResource;
import com.squareup.javapoet.*;
import org.apache.commons.lang3.NotImplementedException;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.*;

import static com.contentful.vault.compiler.ProxyUtils.DEFAULT_IMPLEMENTATION;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.join;

final class ProxyResourceInjection extends Injection {

    private final Set<FieldMeta> fields;
    public final Map<FieldMeta, TypeName> proxies;
    private final boolean immutable;
    private final ProxyUtils proxyUtils;

    private final static String PROXY = "proxy";
    private final static String RESOURCE = "resource";
    private final static String TO_ORIGINAL = "toOriginal";
    private final static String TO_ORIGINAL_TYPE = TO_ORIGINAL + "Type";
    private final static String RESOLVED_OBJECTS = "resolvedObjects";

    public ProxyResourceInjection(ProxyUtils proxyUtils, String remoteId, ClassName className, TypeElement originatingElement, Set<FieldMeta> fields, boolean immutable) {
        super(remoteId, className, originatingElement);
        this.fields = fields;
        this.immutable = immutable;
        this.proxyUtils = proxyUtils;

        proxies = new HashMap<>();

        for(FieldMeta field : fields) {
            TypeName typeName = ClassName.get(field.type());
            TypeName proxyType = proxyUtils.fetchTypeName(typeName);
            if(! typeName.equals(proxyType)) {
                proxies.put(field, proxyType);
            }
        }
    }

    @Override
    TypeSpec.Builder getTypeSpecBuilder() {
        ParameterizedTypeName modelHelperType = ParameterizedTypeName.get(ClassName.get(ProxyResource.class),
                ClassName.get(originatingElement));

        TypeSpec.Builder builder = TypeSpec.classBuilder(className.simpleName())
                .superclass(modelHelperType)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        appendFields(builder);
        appendGetters(builder);
        appendSetters(builder);
        appendToOriginalType(builder);
        appendBuild(builder);

        return builder;
    }

    private void appendFields(TypeSpec.Builder builder) {
        FieldSpec.Builder resolvedObjects = createMapWithInitializer(RESOLVED_OBJECTS, HashMap.class, ClassName.get(String.class), ClassName.get(originatingElement.asType()));
        builder.addField(resolvedObjects.build());

        for (FieldMeta field : fields) {
            if(! field.isResource()) {
                FieldSpec.Builder fieldSpec = FieldSpec.builder(resolveType(field), field.name(), Modifier.PRIVATE);
                builder.addField(fieldSpec.build());
            }
        }
    }

    private void appendGetters(TypeSpec.Builder builder) {
        for (FieldMeta field : fields) {
            if(! field.isResource()) {
                MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(field.name())
                        .returns(resolveType(field))
                        .addModifiers(Modifier.PUBLIC);

                methodSpec.addStatement("return $N", field.name());
                builder.addMethod(methodSpec.build());
            }
        }
    }

    private void appendSetters(TypeSpec.Builder builder) {
        for (FieldMeta field : fields) {
            if(! field.isResource()) {
                MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(field.name())
                        .addParameter(resolveType(field), field.name())
                        .addModifiers(Modifier.PUBLIC);

                methodSpec.addStatement("this.$L = $L", field.name(), field.name());
                builder.addMethod(methodSpec.build());
            }
        }
    }

    private void appendToOriginalType(TypeSpec.Builder builder) {
        ClassName modelClassName = ClassName.get(originatingElement);

        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(TO_ORIGINAL_TYPE)
                .returns(modelClassName)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC);

        final String result = "result";

        methodSpec
                .beginControlFlow("if($L.containsKey(remoteId()))", RESOLVED_OBJECTS)
                .addStatement("return $L.get(remoteId())", RESOLVED_OBJECTS)
                .endControlFlow();

        if (!immutable) {
            methodSpec
                    .addStatement("$T $N = new $T()", modelClassName, result, modelClassName)
                    .addStatement("$L.put(remoteId(), $N)", RESOLVED_OBJECTS, result);
        }

        //Compute variables
        List<String> fieldVariables = new ArrayList<>();
        for (FieldMeta field : fields) {
            fieldVariables.add(fieldVariable(methodSpec, field));
        }

        //Instantiate the result
        if (immutable) {
            methodSpec
                    .addStatement("$T $L = new $T($L)", modelClassName, result, modelClassName, join(fieldVariables, ", "))
                    .addStatement("$L.put(remoteId(), $N)", RESOLVED_OBJECTS, result)
                    .addStatement("return $L", result);
        }
        else {
            Iterator<String> iterator = fieldVariables.iterator();
            for (FieldMeta field : fields) {
                if (field.setter() != null) {
                    methodSpec.addStatement("$N.$L($L)", result, field.setter(), iterator.next());
                } else {
                    methodSpec.addStatement("$N.$L = $L", result, field.name(), iterator.next());
                }
            }
            methodSpec.addStatement("return $N", result);
        }

        builder.addMethod(methodSpec.build());
    }

    private void appendBuild(TypeSpec.Builder builder) {
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder("of")
                .returns(className)
                .addParameter(ClassName.get(originatingElement), RESOURCE)
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC);

        methodSpec
                .addStatement("$T $L = new $T()", className, PROXY, className)
                .beginControlFlow("if($L != null)", RESOURCE);
        for (FieldMeta field : fields) {
            appendFieldForBuild(methodSpec, field);
        }
        methodSpec
                .endControlFlow()
                .addStatement("return $L", PROXY);

        builder.addMethod(methodSpec.build());
    }

    private void appendFieldForBuild(MethodSpec.Builder methodSpec, FieldMeta field) {
        final String value = RESOURCE + "." + (field.getter() != null ? field.getter() + "()" : field.name());

        if(field.isResource()) {
            methodSpec
                    .beginControlFlow("if($L != null)", value)
                    .addStatement("$L.setRemoteId($L.remoteId())", PROXY, value)
                    .addStatement("$L.setCreatedAt($L.createdAt())", PROXY, value)
                    .addStatement("$L.setUpdatedAt($L.updatedAt())", PROXY, value)
                    .addStatement("$L.setContentType($L.contentType())", PROXY, value)
                    .endControlFlow();
            return;
        }

        TypeName originalTypeName = ClassName.get(field.type());
        TypeName proxyType = proxies.get(field);

        if (proxyType == null) {
            methodSpec.addStatement("$L.$L = $L", PROXY, field.name(), value);
            return;
        }

        if (proxyType instanceof ParameterizedTypeName) {
            ParameterizedTypeName originalParameterizedTypeName = (ParameterizedTypeName) originalTypeName;
            ParameterizedTypeName proxyParameterizedTypeName = (ParameterizedTypeName) proxyType;
            ParameterizedTypeName parameterizedTypeName = proxyUtils.parameterizedTypeNameForInstanciation(proxyParameterizedTypeName);

            ParameterizedData proxyData = ParameterizedData.of(field, proxies, proxyUtils, true);

            if (parameterizedTypeName.rawType.equals(ClassName.get(ArrayList.class))) {
                methodSpec
                        .beginControlFlow("if($L != null)", value)
                        .addStatement("$T<" + proxyData.variables + "> $L = new $T<>()", proxyData.args)
                        .beginControlFlow("for($T tmp : $L)", originalParameterizedTypeName.typeArguments.get(0), value)
                        .addStatement("$L.add($T.of(tmp))", proxyData.newVariableName, proxyParameterizedTypeName.typeArguments.get(0))
                        .endControlFlow()
                        .addStatement("$L.$L = $L", PROXY, field.name(), proxyData.newVariableName)
                        .endControlFlow();
                return;

            }
            if (parameterizedTypeName.rawType.equals(ClassName.get(HashMap.class))) {
                ParameterizedTypeName entryType = ParameterizedTypeName.get(ClassName.get(Map.Entry.class),
                        originalParameterizedTypeName.typeArguments.get(0),
                        originalParameterizedTypeName.typeArguments.get(1));

                methodSpec
                        .beginControlFlow("if($L != null)", value)
                        .addStatement("$T<" + proxyData.variables + "> $L = new $T<>()", proxyData.args)
                        .beginControlFlow("for($T entry : $L.entrySet())", entryType, value)
                        .addStatement("$L.put(entry.getKey(), $T.of(entry.getValue()))", proxyData.newVariableName, proxyParameterizedTypeName.typeArguments.get(1))
                        .endControlFlow()
                        .addStatement("$L.$L = $L", PROXY, field.name(), proxyData.newVariableName)
                        .endControlFlow();
                return;
            }

            throw new NotImplementedException("Type " + proxyType + " for field `" + field.name() + "` is not implemented");
        }

        methodSpec.addStatement("$L.$L = $T.of($L)", PROXY, field.name(), proxyType, value);
    }

    private String fieldVariable(MethodSpec.Builder methodSpec, FieldMeta field) {
        TypeName originalTypeName = ClassName.get(field.type());
        TypeName proxyTypeName = proxies.get(field);

        if(field.isResource()) {
            return "asResource()";
        }

        if (proxyTypeName == null) {
            return field.name();
        }

        final String result = computeNewVariable(field);

        methodSpec
                .addStatement("$T $L = null", field.type(), result)
                .beginControlFlow("if($L != null)", field.name());

        if (proxyUtils.isProxyModel(field)) {
            methodSpec.addStatement("$L = $L.$L()", result, field.name(), TO_ORIGINAL_TYPE);
        }
        else if (originalTypeName instanceof ParameterizedTypeName) {
            ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName) originalTypeName;
            ParameterizedTypeName proxyParameterizedTypeName = (ParameterizedTypeName) proxyTypeName;
            ClassName className = DEFAULT_IMPLEMENTATION.get(parameterizedTypeName.rawType);

            ParameterizedData proxyData = ParameterizedData.of(field, proxies, proxyUtils, false);

            if (className.equals(ClassName.get(ArrayList.class))) {
                methodSpec
                        .addStatement("$L = new $T<>()", result, ArrayList.class)
                        .beginControlFlow("for($T tmp : $L)", proxyParameterizedTypeName.typeArguments.get(0), field.name())
                        .addStatement("$L.add(tmp.$L())", proxyData.newVariableName, TO_ORIGINAL_TYPE)  //TODO
                        .endControlFlow();
            }

            if (className.equals(ClassName.get(HashMap.class))) {
                ParameterizedTypeName entryType = ParameterizedTypeName.get(ClassName.get(Map.Entry.class),
                        proxyParameterizedTypeName.typeArguments.get(0),
                        proxyParameterizedTypeName.typeArguments.get(1));

                methodSpec
                        .addStatement("$L = new $T<>()", result, HashMap.class)
                        .beginControlFlow("for($T entry : $L.entrySet())", entryType, field.name())
                        .addStatement("$L.put(entry.getKey(), entry.getValue().$L())", proxyData.newVariableName, TO_ORIGINAL_TYPE)
                        .endControlFlow();
            }
        }

        methodSpec.endControlFlow();
        return  result;
    }

    public Set<FieldMeta> extractNonResourceFields() {
        Set<FieldMeta> fields = new LinkedHashSet<>();
        for(FieldMeta field : this.fields) {
            if(! field.isResource()) {
                fields.add(field);
            }
        }
        return fields;
    }

    private TypeName resolveType(FieldMeta field) {
        TypeName fieldType = ClassName.get(field.type());
        TypeName proxyType = proxies.get(field);
        return proxyType != null ? proxyType : fieldType;
    }

    private static class ParameterizedData {
        Object[] args;
        String variables;
        String newVariableName;

        private ParameterizedData(Object[] args, String variables) {
            this.args = args;
            this.variables = variables;
            this.newVariableName = args[args.length - 2].toString();
        }

        static ParameterizedData of(FieldMeta field, Map<FieldMeta, TypeName> proxies, ProxyUtils proxyUtils, boolean fromProxyType) {
            TypeName proxyType = proxies.get(field);

            if (proxyType == null) {
                throw new IllegalArgumentException("field must be proxified");
            }
            if (!(proxyType instanceof ParameterizedTypeName)) {
                throw new IllegalArgumentException("field should be parameterized");
            }

            ParameterizedTypeName proxyParameterizedTypeName = (ParameterizedTypeName) (fromProxyType ? proxyType : ClassName.get(field.type()));
            ParameterizedTypeName parameterizedTypeName = proxyUtils.parameterizedTypeNameForInstanciation(proxyParameterizedTypeName);

            Object[] args = new Object[parameterizedTypeName.typeArguments.size() + 3];
            args[0] = proxyParameterizedTypeName.rawType;
            args[args.length - 2] = computeNewVariable(field);
            args[args.length - 1] = parameterizedTypeName.rawType;

            StringBuilder parametersType = new StringBuilder();
            for (int i = 0; i < parameterizedTypeName.typeArguments.size(); ++i) {
                args[i + 1] = parameterizedTypeName.typeArguments.get(i);
                if (i > 0) {
                    parametersType.append(",");
                }
                parametersType.append("$L");
            }

            return new ParameterizedData(args, parametersType.toString());
        }
    }

    private static String computeNewVariable(FieldMeta field) {
        return "new" + capitalize(field.name());
    }

}
