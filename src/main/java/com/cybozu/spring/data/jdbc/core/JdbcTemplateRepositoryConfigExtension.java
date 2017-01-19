package com.cybozu.spring.data.jdbc.core;

import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;

public class JdbcTemplateRepositoryConfigExtension extends RepositoryConfigurationExtensionSupport {
    @Override
    protected String getModulePrefix() {
        return "jdbctemplate";
    }

    @Override
    public String getRepositoryFactoryClassName() {
        return JdbcTemplateRepositoryFactoryBean.class.getName();
    }
}
