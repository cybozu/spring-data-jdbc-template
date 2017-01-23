package com.cybozu.spring.data.jdbc.template;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.RepositoryConfigurationSource;

public class JdbcTemplateRepositoryConfigExtension extends RepositoryConfigurationExtensionSupport {
    @Override
    protected String getModulePrefix() {
        return "jdbctemplate";
    }

    @Override
    public String getRepositoryFactoryClassName() {
        return JdbcTemplateRepositoryFactoryBean.class.getName();
    }

    @Override
    public void postProcess(BeanDefinitionBuilder builder, RepositoryConfigurationSource source) {
        super.postProcess(builder, source);

        builder.addPropertyValue("configuration",
                Configuration.create(source.getAttribute("namedParameterJdbcOperationsBeanName")));
    }
}
