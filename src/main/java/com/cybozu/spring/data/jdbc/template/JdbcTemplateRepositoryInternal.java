package com.cybozu.spring.data.jdbc.template;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.cybozu.spring.data.jdbc.template.entity.EntityCallback;
import com.cybozu.spring.data.jdbc.template.util.BeanFactoryUtils;
import com.cybozu.spring.data.jdbc.template.util.EntityUtils;
import com.cybozu.spring.data.jdbc.template.util.SimpleJdbcUpdate;

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
        callBeforeInsert(entity);

        SimpleJdbcInsert jdbcInsert = createJdbcInsert();
        Map<String, Object> values = EntityUtils.values(entity, domainClass, false);

        jdbcInsert.execute(values);
        callAfterInsert(entity);
    }

    @Override
    public Number insertAndReturnKey(T entity) {
        callBeforeInsert(entity);

        SimpleJdbcInsert jdbcInsert = createJdbcInsert();

        Set<String> generatedKeyColumns = EntityUtils.generatedValueColumnNames(domainClass);
        jdbcInsert.usingGeneratedKeyColumns(generatedKeyColumns.toArray(new String[generatedKeyColumns.size()]));

        Map<String, Object> values = EntityUtils.values(entity, domainClass, false);

        Number key = jdbcInsert.executeAndReturnKey(values);
        callAfterInsert(entity);

        return key;
    }

    private Map<String, Object> filterMap(Map<String, Object> values,
            @Nonnull BiPredicate<String, Object> includeColumnPredicate) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (includeColumnPredicate.test(entry.getKey(), entry.getValue())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    @Override
    public void update(T entity, BiPredicate<String, Object> includeColumnPredicate) {
        callBeforeUpdate(entity);

        Map<String, Object> values = EntityUtils.values(entity, domainClass, true);
        if (includeColumnPredicate != null) {
            values = filterMap(values, includeColumnPredicate);
        }

        SimpleJdbcUpdate jdbcUpdate = SimpleJdbcUpdate.create(operations(), domainClass);

        jdbcUpdate.update(values);
        callAfterUpdate(entity);
    }
}
