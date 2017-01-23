package com.cybozu.spring.data.jdbc.template;

import java.io.Serializable;
import java.lang.reflect.Method;

import javax.persistence.Id;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.ReflectionEntityInformation;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;

@Slf4j
public class JdbcTemplateRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable> extends
        RepositoryFactoryBeanSupport<T, S, ID> {

    @Setter
    private Configuration configuration;

    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        return new JdbcTemplateRepositoryFactory(configuration);
    }

    private static class JdbcTemplateRepositoryFactory extends RepositoryFactorySupport {
        private BeanFactory beanFactory;
        private final Configuration configuration;

        private JdbcTemplateRepositoryFactory(Configuration configuration) {
            this.configuration = configuration;
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            super.setBeanFactory(beanFactory);
            this.beanFactory = beanFactory;
        }

        @Override
        public <T, ID extends Serializable> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
            return new ReflectionEntityInformation<>(domainClass, Id.class);
        }

        @Override
        protected Object getTargetRepository(RepositoryInformation metadata) {
            return new JdbcTemplateRepositoryInternal<>(beanFactory, configuration, metadata.getDomainType());
        }

        @Override
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            return JdbcTemplateRepository.class;
        }

        @Override
        protected QueryLookupStrategy getQueryLookupStrategy(QueryLookupStrategy.Key key,
                EvaluationContextProvider evaluationContextProvider) {
            return new JdbcTemplateQueryLookupStrategy(beanFactory, configuration);
        }
    }

    private static class JdbcTemplateQueryLookupStrategy implements QueryLookupStrategy {
        private final BeanFactory beanFactory;
        private final Configuration configuration;

        JdbcTemplateQueryLookupStrategy(BeanFactory beanFactory, Configuration configuration) {
            this.beanFactory = beanFactory;
            this.configuration = configuration;
        }

        @Override
        public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
                NamedQueries namedQueries) {
            return JdbcTemplateRepositoryQuery.create(beanFactory, configuration, method, metadata, factory);
        }
    }

}
