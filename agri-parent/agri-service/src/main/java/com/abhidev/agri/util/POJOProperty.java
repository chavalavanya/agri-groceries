

package com.abhidev.agri.util;


/**
 * Copyright 2015 Apple, Inc
 * Apple Internal Use Only
 **/

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


public final class POJOProperty {
    private static final Map<Class<?>, Map<String, POJOProperty>> POJO_PROPS = new HashMap<>();

    private static final String GET_PREFIX = "get";
    private static final String SET_PREFIX = "set";
    private static final String IS_PREFIX = "is";


    private final String name;
    private Method getter;
    private Method setter;


    public static Map<String, POJOProperty> getClassProperties(final Class<?> pojoType) {
        if (POJO_PROPS.containsKey(pojoType)) {
            return POJO_PROPS.get(pojoType);
        }

        final Map<String, POJOProperty> props = new HashMap<>();

        final Method[] methods = pojoType.getMethods();
        for (final Method method : methods) {
            final String methodName = method.getName();
            if (methodName.startsWith(GET_PREFIX) && !methodName.equals("getClass")) {
                final POJOProperty prop = getProperty(methodName, GET_PREFIX, props);

                prop.setGetter(method);
            } else if (methodName.startsWith(IS_PREFIX)) {
                final POJOProperty prop = getProperty(methodName, IS_PREFIX, props);

                prop.setGetter(method);
            } else if (methodName.startsWith(SET_PREFIX)) {
                final POJOProperty prop = getProperty(methodName, SET_PREFIX, props);

                prop.setSetter(method);
            }
        }

        POJO_PROPS.put(pojoType, props);
        return props;
    }


    private static POJOProperty getProperty(final String methodName, final String prefix, final Map<String, POJOProperty> props) {
        final int prefixLength = prefix.length();
        final int nextIndex = prefixLength + 1;
        final String propName = methodName.substring(prefixLength, nextIndex).toLowerCase() + methodName.substring(nextIndex);
        final POJOProperty prop;
        if (props.containsKey(propName)) {
            prop = props.get(propName);
        } else {
            prop = new POJOProperty(propName);
            props.put(propName, prop);
        }

        return prop;
    }


    private POJOProperty(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Method getGetter() {
        return getter;
    }

    public void setGetter(final Method getter) {
        this.getter = getter;
    }

    public Method getSetter() {
        return setter;
    }

    public void setSetter(final Method setter) {
        this.setter = setter;
    }

    @Override
    public String toString() {
        return String.format("Property name=%s, getter=%s, setter=%s", name, getter.getName(), setter.getName());
    }
}

