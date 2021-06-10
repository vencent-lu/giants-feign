package com.giants.feign.querymap;

import feign.Param;
import feign.QueryMapEncoder;
import feign.codec.EncodeException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BeanQueryMapNestEncoder TODO
 * date time: 2021/6/4 14:13
 * Copyright 2021 github.com/vencent-lu/giants-boot Inc. All rights reserved.
 * @author vencent-lu
 * @since 1.0
 */
public class BeanQueryMapNestEncoder implements QueryMapEncoder {
    private final Map<Class<?>, ObjectParamMetadata> classToMetadata =
            new HashMap<Class<?>, ObjectParamMetadata>();

    @Override
    public Map<String, Object> encode(Object object) {
        try {
            Map<String, Object> propertyNameToValue = new HashMap<String, Object>();
            this.putPropertiesToMap(null, object, propertyNameToValue);
            return propertyNameToValue;
        } catch (IllegalAccessException | IntrospectionException | InvocationTargetException e) {
            throw new EncodeException("Failure encoding object into query map", e);
        }
    }

    private void putPropertiesToMap(String parentProperty, Object object, Map<String, Object> propertyNameToValue)
            throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        ObjectParamMetadata metadata = getMetadata(object.getClass());
        for (PropertyDescriptor pd : metadata.objectProperties) {
            Method method = pd.getReadMethod();
            Object value = method.invoke(object);
            StringBuffer nameBuffer = new StringBuffer();
            if (StringUtils.isNotEmpty(parentProperty)) {
                nameBuffer.append(parentProperty).append('.');
            }
            if (value != null && value != object) {
                Param alias = method.getAnnotation(Param.class);
                nameBuffer.append(alias != null ? alias.value() : pd.getName());
                if (BeanUtils.isSimpleProperty(value.getClass())) {
                    propertyNameToValue.put(nameBuffer.toString(), value);
                } else if (value instanceof Collection){
                    StringBuffer arrValueBuffer = new StringBuffer();
                    for(Object arrObj : (Collection)value) {
                        if (BeanUtils.isSimpleProperty(arrObj.getClass())) {
                            if (arrValueBuffer.length() != 0) {
                                arrValueBuffer.append(',');
                            }
                            arrValueBuffer.append(arrObj);
                        } else {
                            break;
                        }
                    }
                    if (arrValueBuffer.length() > 0) {
                        propertyNameToValue.put(nameBuffer.toString(), arrValueBuffer.toString());
                    }
                } else {
                    this.putPropertiesToMap(nameBuffer.toString(), value, propertyNameToValue);
                }
            }
        }
    }

    private ObjectParamMetadata getMetadata(Class<?> objectType) throws IntrospectionException {
        ObjectParamMetadata metadata = classToMetadata.get(objectType);
        if (metadata == null) {
            metadata = ObjectParamMetadata.parseObjectType(objectType);
            classToMetadata.put(objectType, metadata);
        }
        return metadata;
    }

    private static class ObjectParamMetadata {

        private final List<PropertyDescriptor> objectProperties;

        private ObjectParamMetadata(List<PropertyDescriptor> objectProperties) {
            this.objectProperties = Collections.unmodifiableList(objectProperties);
        }

        private static ObjectParamMetadata parseObjectType(Class<?> type)
                throws IntrospectionException {
            List<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();

            for (PropertyDescriptor pd : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
                boolean isGetterMethod = pd.getReadMethod() != null && !"class".equals(pd.getName());
                if (isGetterMethod) {
                    properties.add(pd);
                }
            }

            return new ObjectParamMetadata(properties);
        }
    }
}
