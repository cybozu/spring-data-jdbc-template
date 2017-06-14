package com.cybozu.spring.data.jdbc.template.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.metadata.TableMetaDataContext;
import org.springframework.jdbc.core.metadata.TableMetaDataProvider;
import org.springframework.jdbc.core.metadata.TableMetaDataProviderFactory;
import org.springframework.jdbc.core.metadata.TableParameterMetaData;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class SimpleJdbcUpdate {
    private final TableMetaDataContext tableMetaDataContext = new TableMetaDataContext();
    private final NamedParameterJdbcOperations namedParameterJdbcOperations;
    private final JdbcTemplate jdbcTemplate;

    private final Class<?> entityClass;
    private final String tableName;
    private final List<String> columnNames;
    private final List<String> generatedKeyNames;

    private SimpleJdbcUpdate(NamedParameterJdbcOperations namedParameterJdbcOperations, Class<?> entityClass,
            String tableName, Collection<String> columnNames, Collection<String> generatedKeyNames) {
        this.namedParameterJdbcOperations = namedParameterJdbcOperations;
        this.jdbcTemplate = (JdbcTemplate) namedParameterJdbcOperations.getJdbcOperations();
        this.tableMetaDataContext.setNativeJdbcExtractor(jdbcTemplate.getNativeJdbcExtractor());
        this.entityClass = entityClass;
        this.tableName = tableName;
        this.columnNames = Collections.unmodifiableList(new ArrayList<>(columnNames));
        this.generatedKeyNames = Collections.unmodifiableList(new ArrayList<>(generatedKeyNames));

        initMetaData();
    }

    public static SimpleJdbcUpdate create(NamedParameterJdbcOperations namedParameterJdbcOperations,
            Class<?> entityClass) {
        String tableName = EntityUtils.tableName(entityClass);
        return new SimpleJdbcUpdate(namedParameterJdbcOperations, entityClass, tableName,
                EntityUtils.columnNames(entityClass), EntityUtils.generatedValueColumnNames(entityClass));
    }

    static <U> String generateUpdateQuery(Class<U> domainClass, String tableName, Predicate<String> includeColumn) {
        String setClause = EntityUtils.columnNamesExceptKeys(domainClass).stream().filter(includeColumn)
                .map(c -> c + " = :" + c).collect(Collectors.joining(" , "));
        String keyClause = EntityUtils.keyNames(domainClass).stream().map(k -> k + " = :" + k)
                .collect(Collectors.joining(" AND "));
        return "UPDATE " + tableName + " SET " + setClause + " WHERE " + keyClause;
    }

    private String[] getGeneratedKeyNames() {
        return generatedKeyNames.toArray(new String[generatedKeyNames.size()]);
    }

    private void initMetaData() {
        tableMetaDataContext.setTableName(tableName);
        tableMetaDataContext.processMetaData(jdbcTemplate.getDataSource(), columnNames, getGeneratedKeyNames());
    }

    private Map<String, Integer> sqlTypes() {
        TableMetaDataProvider metaDataProvider = TableMetaDataProviderFactory.createMetaDataProvider(
                jdbcTemplate.getDataSource(), tableMetaDataContext, jdbcTemplate.getNativeJdbcExtractor());

        Map<String, Integer> result = new HashMap<>();
        for (TableParameterMetaData parameterMetaData : metaDataProvider.getTableParameterMetaData()) {
            result.put(parameterMetaData.getParameterName(), parameterMetaData.getSqlType());
        }
        return result;
    }

    private SqlParameterSource parameterSource(Map<String, Object> values) {
        Map<String, Integer> sqlTypes = sqlTypes();

        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String paramName = entry.getKey();
            int sqlType = sqlTypes.get(paramName) != null ? sqlTypes.get(paramName) : SqlTypeValue.TYPE_UNKNOWN;
            sqlParameterSource.addValue(paramName, entry.getValue(), sqlType);
        }
        return sqlParameterSource;
    }

    public void update(Map<String, Object> values) {
        String query = generateUpdateQuery(entityClass, tableName, values::containsKey);
        namedParameterJdbcOperations.update(query, parameterSource(values));
    }
}
