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

import com.contentful.vault.*;
import com.google.common.base.Joiner;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Type;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.*;

import static com.contentful.java.cda.CDAType.ASSET;
import static com.contentful.java.cda.CDAType.ENTRY;
import static com.contentful.vault.Constants.*;
import static javax.tools.Diagnostic.Kind.ERROR;

public class Processor extends AbstractProcessor {
    private Elements elementUtils;

    private Types typeUtils;

    private Filer filer;

    private ProxyUtils proxyUtils;

  private static final String FQ_ASSET = Asset.class.getName();

  @Override public Set<String> getSupportedAnnotationTypes() {
    Set<String> types = new LinkedHashSet<>();
    types.add(ContentType.class.getCanonicalName());
    types.add(Space.class.getCanonicalName());
    return types;
  }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        filer = processingEnv.getFiler();
        proxyUtils = new ProxyUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Injection injection : findAndParseTargets(roundEnv)) {
            try {
                injection.brewJava().writeTo(filer);
            } catch (Exception e) {
                TypeElement element = injection.originatingElement;
                error(element, "Failed writing injection for \"%s\", message: %s",
                        element.getQualifiedName(), e.getMessage());
            }
        }
        return true;
    }

    private Set<Injection> findAndParseTargets(RoundEnvironment env) {
        Set<TypeMirror> contentTypes = new HashSet<>();

        Map<TypeElement, ModelInjection> models = new LinkedHashMap<>();
        Map<TypeElement, FieldsInjection> fields = new LinkedHashMap<>();
        Map<TypeElement, SpaceInjection> spaces = new LinkedHashMap<>();
        Map<TypeElement, ProxyResourceInjection> proxyResources = new LinkedHashMap<>();

        // List all needed proxies
        for (Element element : env.getElementsAnnotatedWith(ContentType.class)) {
            TypeElement currElement = (TypeElement) element;
            contentTypes.add(currElement.asType());
            ClassName injectionClassName = getInjectionClassName(currElement, SUFFIX_PROXY);
            proxyUtils.put(ClassName.get(element.asType()), injectionClassName);
        }

        // Parse ContentType bindings
        for (Element element : env.getElementsAnnotatedWith(ContentType.class)) {
            try {
                parseContentType((TypeElement) element, contentTypes, proxyResources);
            } catch (Exception e) {
                parsingError(element, ContentType.class, e);
            }
        }

        // Prepare models
        for (ProxyResourceInjection proxyResourceInjection : proxyResources.values()) {
            models.put(proxyResourceInjection.originatingElement, createModelInjection(proxyResourceInjection));
        }

        // Parse Space bindings
        for (Element element : env.getElementsAnnotatedWith(Space.class)) {
            try {
                parseSpace((TypeElement) element, spaces, models);
            } catch (Exception e) {
                parsingError(element, Space.class, e);
            }
        }

        // Prepare FieldsInjection targets
        for (ModelInjection modelInjection : models.values()) {
            fields.put(modelInjection.originatingElement, createFieldsInjection(modelInjection));
        }

        Set<Injection> result = new LinkedHashSet<>();
        result.addAll(proxyResources.values());
        result.addAll(models.values());
        result.addAll(fields.values());
        result.addAll(spaces.values());
        return result;
    }

    private FieldsInjection createFieldsInjection(ModelInjection injection) {
        ClassName name = getInjectionClassName(injection.originatingElement, SUFFIX_FIELDS);
        return new FieldsInjection(injection.remoteId, name, injection.originatingElement,
                injection.fields);
    }

    private ModelInjection createModelInjection(ProxyResourceInjection injection) {
        String id = injection.originatingElement.getAnnotation(ContentType.class).value();
        String tableName = "entry_" + SqliteUtils.hashForId(id);
        ClassName name = getInjectionClassName(injection.originatingElement, SUFFIX_MODEL);
        return new ModelInjection(injection.remoteId, name, injection.className, injection.originatingElement, tableName, injection.extractNonResourceFields(), injection.proxies);
    }

    private void parseSpace(TypeElement element, Map<TypeElement, SpaceInjection> spaces,
                            Map<TypeElement, ModelInjection> models) {
        Space annotation = element.getAnnotation(Space.class);
        String id = annotation.value();
        if (id.isEmpty()) {
            error(element, "@%s id may not be empty. (%s)",
                    Space.class.getSimpleName(),
                    element.getQualifiedName());
            return;
        }

        TypeMirror spaceMirror = elementUtils.getTypeElement(Space.class.getName()).asType();
        List<ModelInjection> includedModels = new ArrayList<>();
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (typeUtils.isSameType(mirror.getAnnotationType(), spaceMirror)) {
                Set<? extends Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> items =
                        mirror.getElementValues().entrySet();

                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : items) {
                    if ("models".equals(entry.getKey().getSimpleName().toString())) {
                        List l = (List) entry.getValue().getValue();
                        if (l.size() == 0) {
                            error(element, "@%s models must not be empty. (%s)",
                                    Space.class.getSimpleName(),
                                    element.getQualifiedName());
                            return;
                        }

                        Set<String> modelIds = new LinkedHashSet<>();
                        for (Object model : l) {
                            TypeElement e = (TypeElement) ((Type) ((Attribute) model).getValue()).asElement();
                            ModelInjection modelInjection = models.get(e);
                            if (modelInjection == null) {
                                return;
                            } else {
                                String rid = modelInjection.remoteId;
                                if (!modelIds.add(rid)) {
                                    error(element, "@%s includes multiple models with the same id \"%s\". (%s)",
                                            Space.class.getSimpleName(), rid, element.getQualifiedName());
                                    return;
                                }
                                includedModels.add(modelInjection);
                            }
                        }
                    }
                }
            }
        }

        List<String> locales = Arrays.asList(annotation.locales());
        Set<String> checked = new HashSet<>();
        for (int i = locales.size() - 1; i >= 0; i--) {
            String code = locales.get(i);
            if (!checked.add(code)) {
                error(element, "@%s contains duplicate locale code '%s'. (%s)",
                        Space.class.getSimpleName(), code, element.getQualifiedName());
                return;
            } else if (code.contains(" ") || code.isEmpty()) {
                error(element, "Invalid locale code '%s', must not be empty and may not contain spaces. (%s)",
                        code, element.getQualifiedName());
                return;
            }
        }
        if (checked.size() == 0) {
            error(element, "@%s at least one locale must be configured. (%s)",
                    Space.class.getSimpleName(), element.getQualifiedName());
            return;
        }

        ClassName injectionClassName = getInjectionClassName(element, SUFFIX_SPACE);
        String dbName = "space_" + SqliteUtils.hashForId(id);
        String copyPath = StringUtils.defaultIfBlank(annotation.copyPath(), null);
        spaces.put(element, new SpaceInjection(id, injectionClassName, element, includedModels, dbName,
                annotation.dbVersion(), copyPath, locales));
    }

    private void parseContentType(TypeElement element, Set<TypeMirror> contentTypes, Map<TypeElement, ProxyResourceInjection> models) {
        String id = element.getAnnotation(ContentType.class).value();
        if (id.isEmpty()) {
            error(element, "@%s id may not be empty. (%s)",
                    ContentType.class.getSimpleName(),
                    element.getQualifiedName());
            return;
        }

        boolean immutable = isImmutable(element);

        Set<FieldMeta> fields = new LinkedHashSet<>();
        Set<String> memberIds = new LinkedHashSet<>();
        for (Element enclosedElement : element.getEnclosedElements()) {
            Field field = enclosedElement.getAnnotation(Field.class);
            if (field == null) {
                continue;
            }

            String fieldId = field.value();
            if (fieldId.isEmpty()) {
                fieldId = enclosedElement.getSimpleName().toString();
            }

            FieldMeta.Builder fieldBuilder = FieldMeta.builder();
            Set<Modifier> modifiers = enclosedElement.getModifiers();
            if (modifiers.contains(Modifier.STATIC)) {
                error(element, "@%s elements must not be static. (%s.%s)", Field.class.getSimpleName(),
                        element.getQualifiedName(), enclosedElement.getSimpleName());
                return;
            }

            if(! immutable) {
                String setter = getSetter(element, enclosedElement);
                if (setter != null) {
                    fieldBuilder.setSetter(setter);
                } else if ((modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.PROTECTED))) {
                    error(element, "@%s private elements must have public setter methods. (%s.%s)", Field.class.getSimpleName(),
                            element.getQualifiedName(), enclosedElement.getSimpleName());
                    return;
                }
            }

            String getter = getGetter(element, enclosedElement);
            if(getter != null) {
                fieldBuilder.setGetter(getter);
            }

            if (!memberIds.add(fieldId)) {
                error(element,
                        "@%s for the same id (\"%s\") was used multiple times in the same class. (%s)",
                        Field.class.getSimpleName(), fieldId, element.getQualifiedName());
                return;
            }

            if (isList(enclosedElement)) {
                DeclaredType declaredType = (DeclaredType) enclosedElement.asType();
                List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                if (typeArguments.size() == 0) {
                    error(element,
                            "Array fields must have a type parameter specified. (%s.%s)",
                            element.getQualifiedName(),
                            enclosedElement.getSimpleName());
                    return;
                }

                TypeMirror arrayType = typeArguments.get(0);
                if (!isValidListType(contentTypes, arrayType)) {
                    error(element, "Invalid list type \"%s\" specified. (%s.%s)",
                            arrayType.toString(),
                            element.getQualifiedName(),
                            enclosedElement.getSimpleName());
                    return;
                }

                String sqliteType = null;
                if (String.class.getName().equals(arrayType.toString())) {
                    sqliteType = SqliteUtils.typeForClass(List.class.getName());
                }

                fieldBuilder.setSqliteType(sqliteType).setArrayType(arrayType.toString());
            } else {
                TypeMirror enclosedType = enclosedElement.asType();
                boolean isResource = isSubtypeOfType(enclosedType, Resource.class.getName());
                String linkType = getLinkType(contentTypes, enclosedType);
                String sqliteType = null;
                if (linkType == null) {
                    String className = enclosedType.toString();
                    TypeName typeName = ClassName.get(enclosedType);
                    if(typeName instanceof ParameterizedTypeName) {
                        className = ((ParameterizedTypeName) typeName).rawType.toString();
                    }
                    sqliteType = SqliteUtils.typeForClass(className);
                    if (sqliteType == null && ! isResource) {
                        error(element,
                                "@%s specified for unsupported type (\"%s\"). (%s.%s)",
                                Field.class.getSimpleName(),
                                enclosedType.toString(),
                                element.getQualifiedName(),
                                enclosedElement.getSimpleName());
                        return;
                    }
                }

                fieldBuilder
                        .setResource(isResource)
                        .setSqliteType(sqliteType)
                        .setLinkType(linkType);
            }

            fields.add(fieldBuilder.setId(fieldId)
                    .setName(enclosedElement.getSimpleName().toString())
                    .setType(enclosedElement.asType())
                    .build());
        }

        if (fields.size() == 0) {
            error(element, "Model must contain at least one @%s element. (%s)",
                    Field.class.getSimpleName(),
                    element.getQualifiedName());
            return;
        }

        ClassName injectionClassName = getInjectionClassName(element, SUFFIX_PROXY);
        models.put(element, new ProxyResourceInjection(proxyUtils, id, injectionClassName, element, fields, immutable));
    }

    private boolean isImmutable(TypeElement element) {
        List<VariableElement> fieldsIn = ElementFilter.fieldsIn(elementUtils.getAllMembers(element));
        List<ExecutableElement> constructorsIn = ElementFilter.constructorsIn(elementUtils.getAllMembers(element));
        for (ExecutableElement constructor : constructorsIn) {
            List<? extends VariableElement> parameters = constructor.getParameters();
            if (fieldsIn.size() <= parameters.size()) {
                int parentsElements = parameters.size() - fieldsIn.size();
                for (int i = 0; i < fieldsIn.size(); ++i) {
                    VariableElement field = fieldsIn.get(i);
                    Set<Modifier> modifiers = field.getModifiers();
                    if (!typeUtils.isSameType(field.asType(), parameters.get(parentsElements + i).asType())
                            || getSetter(element, field) != null
                            || !modifiers.contains(Modifier.PRIVATE) && !modifiers.contains(Modifier.FINAL)) { //TODO pas top peut mieux faire !!
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private String getSetter(TypeElement element, Element enclosedElement) {
        List<ExecutableElement> methodsIn = ElementFilter.methodsIn(elementUtils.getAllMembers(element));
        for (ExecutableElement method : methodsIn) {
            Name methodName = method.getSimpleName();
            String fluentSetterName = enclosedElement.getSimpleName().toString();
            String setterName = "set" + StringUtils.capitalize(fluentSetterName);
            if ((methodName.contentEquals(setterName) || methodName.contentEquals(fluentSetterName))
                    && method.getParameters().size() == 1
                    && method.getModifiers().contains(Modifier.PUBLIC)
                    && typeUtils.isSameType(method.getParameters().get(0).asType(), enclosedElement.asType())) {
                return methodName.toString();
            }
        }
        return null;
    }

    private String getGetter(TypeElement element, Element enclosedElement) {
        List<ExecutableElement> methodsIn = ElementFilter.methodsIn(elementUtils.getAllMembers(element));
        for (ExecutableElement method : methodsIn) {
            Name methodName = method.getSimpleName();
            String fluentGetterName = enclosedElement.getSimpleName().toString();
            String getterName = "get" + StringUtils.capitalize(fluentGetterName);
            if ((methodName.contentEquals(getterName) || methodName.contentEquals(fluentGetterName))
                    && method.getParameters().isEmpty()
                    && method.getModifiers().contains(Modifier.PUBLIC)
                    && typeUtils.isSameType(method.getReturnType(), enclosedElement.asType())) {
                return methodName.toString();
            }
        }
        return null;
    }

    private boolean isValidListType(Set<TypeMirror> contentTypes, TypeMirror typeMirror) {
        return isSubtypeOfType(typeMirror, String.class.getName())
                || isSubtypeOfType(typeMirror, FQ_ASSET)
                || contentTypes.contains(typeMirror);
    }

    private boolean isList(Element element) {
        TypeMirror typeMirror = element.asType();
        if (List.class.getName().equals(typeMirror.toString())) {
            return true;
        }
        return typeMirror instanceof DeclaredType && List.class.getName().equals(
                ((DeclaredType) typeMirror).asElement().toString());
    }

    private ClassName getInjectionClassName(TypeElement typeElement, String suffix) {
        ClassName specClassName = ClassName.get(typeElement);
        return ClassName.get(specClassName.packageName(),
                Joiner.on('$').join(specClassName.simpleNames()) + suffix);
    }

    private String getLinkType(Set<TypeMirror> contentTypes, TypeMirror typeMirror) {
      if(isSubtypeOfType(typeMirror, FQ_ASSET)) {
          return ASSET.toString();
      }

      return contentTypes.contains(typeMirror) ?  ENTRY.toString() : null;
    }

    private void parsingError(Element element, Class<? extends Annotation> annotation, Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        error(element, "Unable to parse @%s injection.\n\n%s", annotation.getSimpleName(), stackTrace);
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(ERROR, message, element);
    }

    private boolean isSubtypeOfType(TypeMirror typeMirror, String otherType) {
        if (otherType.equals(typeMirror.toString())) {
            return true;
        }
        if (!(typeMirror instanceof DeclaredType)) {
            return false;
        }
        DeclaredType declaredType = (DeclaredType) typeMirror;
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments.size() > 0) {
            StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
            typeString.append('<');
            for (int i = 0; i < typeArguments.size(); i++) {
                if (i > 0) {
                    typeString.append(',');
                }
                typeString.append('?');
            }
            typeString.append('>');
            if (typeString.toString().equals(otherType)) {
                return true;
            }
        }
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) {
            return false;
        }
        TypeElement typeElement = (TypeElement) element;
        TypeMirror superType = typeElement.getSuperclass();
        if (isSubtypeOfType(superType, otherType)) {
            return true;
        }
        for (TypeMirror interfaceType : typeElement.getInterfaces()) {
            if (isSubtypeOfType(interfaceType, otherType)) {
                return true;
            }
        }
        return false;
    }
}
