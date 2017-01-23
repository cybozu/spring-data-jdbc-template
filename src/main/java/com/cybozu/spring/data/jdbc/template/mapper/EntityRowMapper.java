package com.cybozu.spring.data.jdbc.template.mapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import javax.annotation.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

public class EntityRowMapper<T> implements RowMapper<T> {
    private final EntityMapper<T> entityMapper;

    public static <U> EntityRowMapper<U> create(Class<U> entityType,
            @Nullable Class<? extends EntityMapper<?>> mapperClass) {
        EntityMapper<U> entityMapper = instantiateEntityMapper(mapperClass);
        return new EntityRowMapper<>(entityType, entityMapper);
    }

    @SuppressWarnings("unchecked")
    private static <U> EntityMapper<U> instantiateEntityMapper(@Nullable Class<? extends EntityMapper<?>> mapperClass) {
        if (mapperClass == null) {
            return new BeanEntityMapper<>();
        } else {
            return (EntityMapper<U>) BeanUtils.instantiate(mapperClass);
        }
    }

    private EntityRowMapper(Class<T> entityType, EntityMapper<T> entityMapper) {
        entityMapper.initialize(entityType);
        this.entityMapper = entityMapper;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        T mappedObject = this.entityMapper.createInstance();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        Map<String, Class<?>> types = this.entityMapper.types();

        this.entityMapper.startMapping(mappedObject);
        for (int index = 1; index <= columnCount; index++) {
            String column = JdbcUtils.lookupColumnName(rsmd, index);
            Object value = JdbcUtils.getResultSetValue(rs, index, types.get(column));

            this.entityMapper.setValue(mappedObject, column, value);
        }
        this.entityMapper.finishMapping(mappedObject);
        return mappedObject;
    }
}
