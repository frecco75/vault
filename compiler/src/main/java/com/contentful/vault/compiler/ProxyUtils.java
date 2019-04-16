package com.contentful.vault.compiler;

import com.contentful.vault.Asset;
import com.contentful.vault.AssetProxy;
import com.contentful.vault.FieldMeta;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ProxyUtils {

    final static Map<ClassName, ClassName> DEFAULT_IMPLEMENTATION = new HashMap<>();

    private final Map<TypeName, ClassName> proxyByType = new HashMap<>();

    static {
        Map<Class<?>, Class<?>> map = new HashMap<>();
        map.put(List.class, ArrayList.class);
        map.put(Map.class, HashMap.class);

        for(Map.Entry<Class<?>, Class<?>> entry : map.entrySet()) {
            DEFAULT_IMPLEMENTATION.put(ClassName.get(entry.getKey()), ClassName.get(entry.getValue()));
        }
    }

    public ProxyUtils() {
        proxyByType.put(ClassName.get(Asset.class), ClassName.get(AssetProxy.class));
    }

    public void put(TypeName typeName, ClassName className) {
        proxyByType.put(typeName, className);
    }

    public boolean isProxyModel(FieldMeta field) {
        return proxyByType.containsKey(ClassName.get(field.type()));
    }

    public TypeName fetchTypeName(TypeName typeName) {
        if(typeName instanceof ClassName) {
            return fetchClassName((ClassName) typeName);
        }
        if(typeName instanceof ParameterizedTypeName) {
            ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName) typeName;
            ClassName rawType = fetchClassName(parameterizedTypeName.rawType);

            List<TypeName> typeArguments = new ArrayList<>();
            for(TypeName typeArgument : parameterizedTypeName.typeArguments) {
                typeArguments.add(fetchTypeName(typeArgument));
            }

            return parameterizedTypeName(rawType, typeArguments);
        }

        return null;
    }

    public ParameterizedTypeName parameterizedTypeNameForInstanciation(ParameterizedTypeName parameterizedTypeName) {
        ClassName defaultImpl = DEFAULT_IMPLEMENTATION.get(parameterizedTypeName.rawType);
        return defaultImpl != null ?
                parameterizedTypeName(defaultImpl, parameterizedTypeName.typeArguments) :
                parameterizedTypeName;
    }

    @SuppressWarnings("all")
    private ParameterizedTypeName parameterizedTypeName(ClassName className, List<TypeName> typeArguments) {
        return ParameterizedTypeName.get(className, typeArguments.toArray(new TypeName[typeArguments.size()]));
    }

    private ClassName fetchClassName(ClassName className) {
        ClassName proxy = proxyByType.get(className);
        return proxy != null ? proxy : className;
    }

}
