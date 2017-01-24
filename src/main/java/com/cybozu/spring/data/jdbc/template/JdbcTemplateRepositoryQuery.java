package com.cybozu.spring.data.jdbc.template;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.cybozu.spring.data.jdbc.template.annotation.Mapper;
import com.cybozu.spring.data.jdbc.template.annotation.Modifying;
import com.cybozu.spring.data.jdbc.template.annotation.Query;
import com.cybozu.spring.data.jdbc.template.mapper.EntityMapper;
import com.cybozu.spring.data.jdbc.template.mapper.EntityRowMapper;
import com.cybozu.spring.data.jdbc.template.util.BeanFactoryUtils;

class JdbcTemplateRepositoryQuery implements RepositoryQuery {
    private final BeanFactory beanFactory;
    private final JdbcTemplateQueryMethod queryMethod;
    private final JdbcTemplateRepositoryConfiguration configuration;

    static JdbcTemplateRepositoryQuery create(BeanFactory beanFactory,
            JdbcTemplateRepositoryConfiguration configuration, Method method, RepositoryMetadata metadata,
            ProjectionFactory factory) {
        JdbcTemplateQueryMethod queryMethod = new JdbcTemplateQueryMethod(method, metadata, factory);
        return new JdbcTemplateRepositoryQuery(beanFactory, configuration, queryMethod);
    }

    private JdbcTemplateRepositoryQuery(BeanFactory beanFactory, JdbcTemplateRepositoryConfiguration configuration,
            JdbcTemplateQueryMethod queryMethod) {
        this.beanFactory = beanFactory;
        this.configuration = configuration;
        this.queryMethod = queryMethod;
    }

    private String getQuery() {
        String query = this.queryMethod.getQuery();
        if (query == null) {
            throw new RuntimeException("query is not specified.");
        }
        return query;
    }

    @Override
    public Object execute(Object[] parameters) {
        NamedParameterJdbcOperations jdbcTemplate = BeanFactoryUtils.getBeanByNameOrType(beanFactory,
                configuration.getOperationsBeanName(), NamedParameterJdbcOperations.class);

        Map<String, Object> paramMap = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            String paramName = queryMethod.getParameters().getParameter(i).getName();
            paramMap.put(paramName, parameters[i]);
        }

        Class<?> resultType = queryMethod.getReturnedObjectType();

        if (queryMethod.isModifyingQuery()) {
            return jdbcTemplate.update(getQuery(), paramMap);
        } else {
            return jdbcTemplate.query(getQuery(), paramMap, getRowMapper(this.queryMethod, resultType));
        }
    }

    @SuppressWarnings("unchecked")
    private <S> RowMapper<S> getRowMapper(JdbcTemplateQueryMethod queryMethod, Class<S> resultType) {
        Class<? extends EntityMapper<?>> mapperClass = null;

        if (queryMethod.mapperClass != null) {
            mapperClass = queryMethod.mapperClass;
        } else if (resultType.getAnnotation(Mapper.class) != null) {
            Mapper mapping = resultType.getAnnotation(Mapper.class);
            mapperClass = mapping.value();
        }
        return EntityRowMapper.create(resultType, mapperClass);
    }

    @Override
    public QueryMethod getQueryMethod() {
        return queryMethod;
    }

    private static class JdbcTemplateQueryMethod extends QueryMethod {
        @Getter
        private final String query;

        private final Method method;

        private final Class<? extends EntityMapper<?>> mapperClass;

        JdbcTemplateQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
            super(method, metadata, factory);
            Query query = method.getAnnotation(Query.class);
            if (query == null) {
                this.query = null;
            } else {
                this.query = query.value();
            }

            Mapper mapperOnMethod = method.getAnnotation(Mapper.class);
            Mapper mapperOnInterface = metadata.getRepositoryInterface().getAnnotation(Mapper.class);
            if (mapperOnMethod != null) {
                this.mapperClass = mapperOnMethod.value();
            } else if (mapperOnInterface != null) {
                this.mapperClass = mapperOnInterface.value();
            } else {
                this.mapperClass = null;
            }

            this.method = method;

        }

        public boolean isModifyingQuery() {
            return this.method.getAnnotation(Modifying.class) != null;
        }
    }
}