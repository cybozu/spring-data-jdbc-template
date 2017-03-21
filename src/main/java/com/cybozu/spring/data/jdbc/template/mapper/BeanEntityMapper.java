package com.cybozu.spring.data.jdbc.template.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.cybozu.spring.data.jdbc.template.util.Accessor;
import com.cybozu.spring.data.jdbc.template.util.EntityUtils;

public class BeanEntityMapper<T> extends AbstractEntityMapper<T> {
    private Map<String, Accessor> columnToAccessor;

    public static <U> BeanEntityMapper<U> create(Class<U> mappedClass) {
        BeanEntityMapper<U> result = new BeanEntityMapper<>();
        result.initialize(mappedClass);
        return result;
    }

    @Override
    public void initialize(Class<T> mappedClass) {
        super.initialize(mappedClass);
        Map<String, Accessor> properties = new HashMap<>();

        for (Accessor accessor : getAccessors()) {
            properties.put(EntityUtils.columnName(accessor), accessor);
        }
        columnToAccessor = Collections.unmodifiableMap(properties);
    }

    @Override
    public void setValue(T object, @Nonnull String name, Object value) {
        Accessor accessor = columnToAccessor.get(name);
        if (accessor != null) {
            columnToAccessor.get(name).setValue(object, convertValue(value, accessor.getValueType()));
        }
    }

    protected Object convertValue(Object value, Class<?> requiredType) {
        if (requiredType.isEnum() && value instanceof String) {
            return EntityUtils.stringToEnum((String) value, requiredType);
        }
        return value;
    }

    public static class DefaultEntityMapper extends BeanEntityMapper<Object> {
    }
}
