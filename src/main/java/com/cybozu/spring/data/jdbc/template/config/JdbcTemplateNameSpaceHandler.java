package com.cybozu.spring.data.jdbc.template.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.data.repository.config.RepositoryBeanDefinitionParser;

import com.cybozu.spring.data.jdbc.template.JdbcTemplateRepositoryConfigExtension;

public class JdbcTemplateNameSpaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        JdbcTemplateRepositoryConfigExtension extension = new JdbcTemplateRepositoryConfigExtension();
        RepositoryBeanDefinitionParser repositoryBeanDefinitionParser = new RepositoryBeanDefinitionParser(extension);
        registerBeanDefinitionParser("repositories", repositoryBeanDefinitionParser);
    }
}
