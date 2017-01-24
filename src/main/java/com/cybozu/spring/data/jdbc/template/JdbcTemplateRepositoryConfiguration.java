package com.cybozu.spring.data.jdbc.template;

import lombok.Getter;

@Getter
class JdbcTemplateRepositoryConfiguration {
    private final String operationsBeanName;

    static JdbcTemplateRepositoryConfiguration create(String operationsBeanName) {
        return new JdbcTemplateRepositoryConfiguration(operationsBeanName);
    }

    private JdbcTemplateRepositoryConfiguration(String operationsBeanName) {
        this.operationsBeanName = operationsBeanName;
    }
}
