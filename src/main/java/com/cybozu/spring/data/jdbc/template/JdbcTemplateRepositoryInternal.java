package com.cybozu.spring.data.jdbc.template;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.cybozu.spring.data.jdbc.template.entity.EntityCallback;
import com.cybozu.spring.data.jdbc.template.util.BeanFactoryUtils;
import com.cybozu.spring.data.jdbc.template.util.EntityUtils;

class JdbcTemplateRepositoryInternal<T> implements JdbcTemplateRepository<T> {
    private final Class<T> domainClass;
    private final BeanFactory beanFactory;
    private final JdbcTemplateRepositoryConfiguration configuration;

    JdbcTemplateRepositoryInternal(BeanFactory beanFactory, JdbcTemplateRepositoryConfiguration configuration,
            Class<T> domainClass) {
        this.beanFactory = beanFactory;
        this.configuration = configuration;
        this.domainClass = domainClass;
    }

    private NamedParameterJdbcOperations operations() {
        return BeanFactoryUtils.getBeanByNameOrType(beanFactory, configuration.getOperationsBeanName(),
                NamedParameterJdbcOperations.class);
    }

    private SimpleJdbcInsert createJdbcInsert() {
        JdbcTemplate jdbcTemplate = (JdbcTemplate) operations().getJdbcOperations();
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);

        jdbcInsert.withTableName(EntityUtils.tableName(domainClass));
        Set<String> usingColumns = EntityUtils.columnNamesExceptGeneratedValues(domainClass);
        jdbcInsert.usingColumns(usingColumns.toArray(new String[usingColumns.size()]));

        return jdbcInsert;
    }

    private void callBeforeInsert(T entity) {
        if (entity instanceof EntityCallback) {
            ((EntityCallback) entity).beforeInsert();
        }
    }

    private void callAfterInsert(T entity) {
        if (entity instanceof EntityCallback) {
            ((EntityCallback) entity).afterInsert();
        }
    }

    private void callBeforeUpdate(T entity) {
        if (entity instanceof EntityCallback) {
            ((EntityCallback) entity).beforeUpdate();
        }
    }

    private void callAfterUpdate(T entity) {
        if (entity instanceof EntityCallback) {
            ((EntityCallback) entity).afterUpdate();
        }
    }

    @Override
    public void insert(T entity) {
        SimpleJdbcInsert jdbcInsert = createJdbcInsert();
        Map<String, Object> values = EntityUtils.values(entity, domainClass, false);

        callBeforeInsert(entity);
        jdbcInsert.execute(values);
        callAfterInsert(entity);
    }

    @Override
    public Number insertAndReturnKey(T entity) {
        SimpleJdbcInsert jdbcInsert = createJdbcInsert();

        Set<String> generatedKeyColumns = EntityUtils.generateValueColumnNames(domainClass);
        jdbcInsert.usingGeneratedKeyColumns(generatedKeyColumns.toArray(new String[generatedKeyColumns.size()]));

        Map<String, Object> values = EntityUtils.values(entity, domainClass, false);

        callBeforeInsert(entity);
        Number key = jdbcInsert.executeAndReturnKey(values);
        callAfterInsert(entity);

        return key;
    }

    static <U> String generateUpdateQuery(Class<U> domainClass) {
        String tableName = EntityUtils.tableName(domainClass);
        String setClause = EntityUtils.columnNamesExceptKeys(domainClass).stream().map(c -> c + " = :" + c)
                .collect(Collectors.joining(" , "));
        String keyClause = EntityUtils.keyNames(domainClass).stream().map(k -> k + " = :" + k)
                .collect(Collectors.joining(" AND "));
        return "UPDATE " + tableName + " SET " + setClause + " WHERE " + keyClause;
    }

    @Override
    public void update(T entity) {
        String query = generateUpdateQuery(domainClass);
        Map<String, Object> values = EntityUtils.values(entity, domainClass, true);

        callBeforeUpdate(entity);
        operations().update(query, values);
        callAfterUpdate(entity);
    }
}
