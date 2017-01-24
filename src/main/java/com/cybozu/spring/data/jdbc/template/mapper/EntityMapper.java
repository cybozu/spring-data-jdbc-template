package com.cybozu.spring.data.jdbc.template.mapper;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Classes which implements this class must have no-arg constructor.
 *
 * @param <T>
 *            a type which this class maps
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
     * Returns a map. Keys of the map are column names of the entity. Values of the map are Java types of the column.
     * 
     * @return a map.
     */
    Map<String, Class<?>> types();
}
