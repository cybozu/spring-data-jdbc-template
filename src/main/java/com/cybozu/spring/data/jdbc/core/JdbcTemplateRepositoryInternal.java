package com.cybozu.spring.data.jdbc.core;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.cybozu.spring.data.jdbc.core.util.EntityUtil;

class JdbcTemplateRepositoryInternal<T> implements JdbcTemplateRepository<T> {
    private final Class<T> domainClass;
    private final BeanFactory beanFactory;

    JdbcTemplateRepositoryInternal(BeanFactory beanFactory, Class<T> domainClass) {
        this.domainClass = domainClass;
        this.beanFactory = beanFactory;
    }

    private NamedParameterJdbcOperations operations() {
        return beanFactory.getBean(NamedParameterJdbcOperations.class);
    }

    @Override
    public void insert(T entity) {
        JdbcTemplate jdbcTemplate = (JdbcTemplate) operations().getJdbcOperations();

        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName(EntityUtil.tableName(domainClass));
        Map<String, Object> values = EntityUtil.values(entity, domainClass, false);
        jdbcInsert.execute(values);
    }

    @Override
    public void update(T entity) {
        String tableName = EntityUtil.tableName(domainClass);
        String setClause = EntityUtil.columnNamesExceptKeys(domainClass).stream().map(c -> c + " = :" + c)
                .collect(Collectors.joining(" , "));
        String keyClause = EntityUtil.keyNames(domainClass).stream().map(k -> k + " = :" + k)
                .collect(Collectors.joining(" AND "));
        String query = "UPDATE " + tableName + " SET " + setClause + " WHERE " + keyClause;
        Map<String, Object> values = EntityUtil.values(entity, domainClass, true);
        operations().update(query, values);
    }
}
