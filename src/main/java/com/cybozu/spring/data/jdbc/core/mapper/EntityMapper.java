package com.cybozu.spring.data.jdbc.core.mapper;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Classes which implements this class must have no-arg constructor.
 *
 * @param <T> a type which this class maps
 */
public interface EntityMapper<T> {
    default void initialize(Class<T> mappedClass) {
    }

    T createInstance();

    default void startMapping(T object) {
    }

    void setValue(T object, @Nonnull String name, Object value);

    default void finishMapping(T object) {
    }

    /**
     * 
     * @return a Map whose key is a column name and values is a type of the property.
     */
    Map<String, Class<?>> types();
}
