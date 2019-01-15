

package com.abhidev.agri.util;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.beans.BeanMap;



public final class POJOConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(POJOConverter.class);
    private static final String POJO_ID_KEY = "id";
    private static final String MONGODB_ID_KEY = "_id";


    private POJOConverter() {
        // No instantiations
    }


    public static List<Object> getPOJOListFromDocument(final Document doc, final Method method) {
        final Type returnType = method.getGenericReturnType();
        final List<Object> listObject = new ArrayList<>();
        if (returnType instanceof ParameterizedType) {
            final Type paramType = ((ParameterizedType)returnType).getActualTypeArguments()[0];
            doc.entrySet().forEach(element -> {
                final Object value = element.getValue();
                final Object subObject = getPOJOFromDocument((Document)value, (Class<?>)paramType);
                listObject.add(subObject);
            });
        }

        return listObject;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getPOJOFromDocument(final Document doc, final Class<T> pojoType) {
        try {
            if (pojoType.isInterface()) {
                return null;
            }
            final T pojo = pojoType.newInstance();
            final Map<String, POJOProperty> props = POJOProperty.getClassProperties(pojoType);
            props.entrySet().forEach(entry -> {
                final String propName = entry.getKey();
                final POJOProperty prop = entry.getValue();
                final Method setter = prop.getSetter();
                try {
                    if (propName.equals(POJO_ID_KEY)) {
                        setter.invoke(pojo, String.valueOf(doc.get(MONGODB_ID_KEY)));
                        /*
                         * if (doc.get(MONGODB_ID_KEY) instanceof Long) { setter.invoke(pojo,
                         * String.valueOf(doc.get(MONGODB_ID_KEY))); } else { setter.invoke(pojo,
                         * doc.get(MONGODB_ID_KEY)); }
                         */
                    } else {
                        final Object value = doc.get(propName);
                        final Class<?> returnTypeClass = prop.getGetter().getReturnType();
                        if (value != null) {
                            if (value instanceof Document) {
                                if (prop.getGetter().getReturnType().isAssignableFrom(List.class)) {
                                    final Object subObject = getPOJOListFromDocument((Document)value, prop.getGetter());
                                    setter.invoke(pojo, subObject);
                                } else {
                                    final Object subObject = getPOJOFromDocument((Document)value, returnTypeClass);
                                    setter.invoke(pojo, subObject);
                                }
                            } else if (value instanceof Date) {
                                setter.invoke(pojo, ((Date)value));
                            } else if (value instanceof Integer) {
                                if (prop.getGetter().getReturnType().isAssignableFrom(Long.class)) {
                                    setter.invoke(pojo, ((Integer)value).longValue());
                                } else {
                                    setter.invoke(pojo, value);
                                }
                            } else if (value instanceof Double) {
                                if (prop.getGetter().getReturnType().isAssignableFrom(Double.class)) {
                                    setter.invoke(pojo, ((Double)value).longValue());
                                } else {
                                    setter.invoke(pojo, value);
                                }
                            } else if (prop.getGetter().getReturnType().isAssignableFrom(Set.class)) {
                                setter.invoke(pojo, new HashSet<>((Collection<?>)value));
                                /*
                                 * } else if (value instanceof Boolean) { setter.invoke(pojo, ((Boolean)value));
                                 */
                            } else if (value instanceof Collection) {
                                final Collection<T> valueCollection = (Collection<T>)value;
                                List<T> objectList = new ArrayList<>();
                                /*
                                 * valueCollection.forEach(element -> { try { if (element instanceof Document) {
                                 * Document elementDoc = (Document) element; final Type returnType =
                                 * prop.getGetter().getGenericReturnType(); if (returnType instanceof ParameterizedType)
                                 * { final Type paramType = ((ParameterizedType)returnType).getActualTypeArguments()[0];
                                 * T t = getPOJOFromDocument(elementDoc, (Class<T>)paramType); objectList.add(t); } }
                                 * else { setter.invoke(pojo, value); break; } } catch (Exception e) { throw new
                                 * IllegalArgumentException("Unable to convert object to document", e); } });
                                 */
                                for (final Object element : valueCollection) {
                                    try {
                                        if (element instanceof Document) {
                                            final Document elementDoc = (Document)element;
                                            final Type returnType = prop.getGetter().getGenericReturnType();
                                            if (returnType instanceof ParameterizedType) {
                                                final Type paramType = ((ParameterizedType)returnType).getActualTypeArguments()[0];
                                                final T t = getPOJOFromDocument(elementDoc, (Class<T>)paramType);
                                                objectList.add(t);
                                            }
                                        } else {
                                            objectList = (List<T>)valueCollection;
                                            /*
                                             * setter.invoke(pojo, value); checkFlag=false;
                                             */
                                            break;
                                        }
                                    } catch (final Exception e) {
                                        throw new IllegalArgumentException("Unable to convert object to document", e);
                                    }
                                }
                                setter.invoke(pojo, objectList);
                            } else {
                                try {
                                    setter.invoke(pojo, value);
                                } catch (final IllegalArgumentException e) {
                                    LOGGER.debug("pojoType: " + pojoType + ", valueType: '" + value.getClass().getName() + ", prop: '" + prop + "'");
                                    throw e;
                                }
                            }
                        }
                    }
                } catch (final InvocationTargetException | IllegalAccessException e) {
                    throw new IllegalArgumentException("Unable to set property " + propName + " on POJO type " + pojoType, e);
                }
            });

            return pojo;
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Unable to instantiate POJO type " + pojoType, e);
        }
    }

    /*
     * public static Document getModifiedFields(final Object oldObject, final Object newObject, Set<String>
     * excludeProperties) { final Document lastModification = new Document();
     * lastModification.put(CompensationConstants.OLD_VALUES_KEY, POJOConverter.compareObjects(oldObject, newObject,
     * excludeProperties)); lastModification.put(CompensationConstants.LAST_MODIFIED_TIME, new Date().getTime()); return
     * lastModification; }
     */

    /**
     * Populates the values in the passed object into a Document and passed back.
     *
     * @param obj
     * @return
     */
    public static Document populateDocumentFromPOJO(final Object obj) {

        try {
            final Map<String, Object> map = convertObjectToMap(obj);
            return new Document(map);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalArgumentException("Unable to set property", e);
        }

    }

    public static Map<String, Object> convertObjectToMap(final Object obj) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        final Method[] methods = obj.getClass().getMethods();

        final Map<String, Object> map = new HashMap<>();
        for (final Method m : methods) {
            if (m.getName().matches("^(get|is).+") && !m.getName().startsWith("getClass")) {
                final Object value = m.invoke(obj);
                if (value != null) {
                    String propertyName = m.getName().replaceAll("^(get|is)", "");
                    if (propertyName.length() > 1) {
                        propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
                    }
                    if (propertyName.equals(POJO_ID_KEY)) {
                        map.put(MONGODB_ID_KEY, value);
                    } else if (String.class.isAssignableFrom(value.getClass()) || Long.class.isAssignableFrom(value.getClass())
                                || Boolean.class.isAssignableFrom(value.getClass()) || Double.class.isAssignableFrom(value.getClass())) {
                        if ((propertyName.endsWith("Date") || propertyName.endsWith("Time") || propertyName.endsWith("time")) && (value instanceof Long)
                                    && ((long)value != 0)) {
                            map.put(propertyName, new Date((long)value));
                        } else {
                            map.put(propertyName, value);
                        }
                    } else if (Date.class.isAssignableFrom(value.getClass())) {
                        map.put(propertyName, value);
                    } else if (Number.class.isAssignableFrom(value.getClass())) {
                        map.put(propertyName, value);
                    } else {
                        if (List.class.isAssignableFrom(value.getClass()) || Set.class.isAssignableFrom(value.getClass())) {
                            final Collection<?> valueCollection = (Collection<?>)value;
                            final List<Object> documentList = new ArrayList<>();
                            valueCollection.forEach(element -> {
                                try {
                                    if ((element instanceof String) || (element instanceof Number)) {
                                        documentList.add(element);
                                    } else {
                                        final Document listDocument = new Document(convertObjectToMap(element));
                                        documentList.add(listDocument);
                                    }
                                } catch (final Exception e) {
                                    throw new IllegalArgumentException("Unable to convert object to document", e);
                                }
                            });
                            map.put(propertyName, documentList);
                        } else {
                            map.put(propertyName, new Document(convertObjectToMap(value)));
                        }
                    }
                }
            }
        }
        return map;
    }

}

