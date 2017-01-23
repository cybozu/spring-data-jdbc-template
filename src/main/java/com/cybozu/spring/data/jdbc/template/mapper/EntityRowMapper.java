package com.cybozu.spring.data.jdbc.template.mapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

public class EntityRowMapper<T> implements RowMapper<T> {
    private final EntityMapper<T> entityMapper;

    public EntityRowMapper(EntityMapper<T> entityMapper) {
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
