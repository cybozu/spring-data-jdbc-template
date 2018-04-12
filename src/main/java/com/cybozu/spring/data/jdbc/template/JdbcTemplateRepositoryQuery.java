package com.cybozu.spring.data.jdbc.template;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.cybozu.spring.data.jdbc.template.annotation.Mapper;
import com.cybozu.spring.data.jdbc.template.annotation.Modifying;
import com.cybozu.spring.data.jdbc.template.annotation.Query;
import com.cybozu.spring.data.jdbc.template.annotation.SingleColumn;
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

    private Object convertValue(Object value, Class<?> type) {
        if (type.isEnum() && value != null) {
            // SimpleJdbcInsert calls toString() for enum.
            // This class also calls it for consistency.
            return value.toString();
        }
        return value;
    }

    @Override
    public Object execute(Object[] parameters) {
        NamedParameterJdbcOperations jdbcTemplate = BeanFactoryUtils.getBeanByNameOrType(beanFactory,
                configuration.getOperationsBeanName(), NamedParameterJdbcOperations.class);

        Map<String, Object> paramMap = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = queryMethod.getParameters().getParameter(i);
            paramMap.put(param.getName(), convertValue(parameters[i], param.getType()));
        }

        Class<?> resultType = queryMethod.getReturnedObjectType();

        if (queryMethod.isModifyingQuery()) {
            return jdbcTemplate.update(getQuery(), paramMap);
        } else {
            List<?> result = jdbcTemplate.query(getQuery(), paramMap, getRowMapper(this.queryMethod, resultType));
            return getFirst(result, queryMethod);
        }
    }

    private Object getFirst(List<?> source, QueryMethod queryMethod) {
        if (source == null) {
            return null;
        }

        if (queryMethod.isCollectionQuery()) {
            return source;
        } else {
            if (source.isEmpty()) {
                return null;
            } else {
                return source.get(0);
            }
        }
    }

    private <S> RowMapper<S> getRowMapper(JdbcTemplateQueryMethod queryMethod, Class<S> resultType) {
        if (queryMethod.isSingleColumn()) {
            return SingleColumnRowMapper.newInstance(resultType);
        } else {
            return getEntityRowMapper(queryMethod, resultType);
        }
    }

    @SuppressWarnings("unchecked")
    private <S> RowMapper<S> getEntityRowMapper(JdbcTemplateQueryMethod queryMethod, Class<S> resultType) {
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

        @Getter
        private final boolean singleColumn;

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

            this.singleColumn = method.getAnnotation(SingleColumn.class) != null;

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
