package com.cybozu.spring.data.jdbc.template;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

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

    @Override
    public void insert(T entity) {
        JdbcTemplate jdbcTemplate = (JdbcTemplate) operations().getJdbcOperations();

        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName(EntityUtils.tableName(domainClass));
        Map<String, Object> values = EntityUtils.values(entity, domainClass, false);
        jdbcInsert.execute(values);
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
        operations().update(query, values);
    }
}
