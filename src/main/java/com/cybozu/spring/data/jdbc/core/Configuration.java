package com.cybozu.spring.data.jdbc.core;

import lombok.Getter;

@Getter
class Configuration {
    private final String operationsBeanName;

    static Configuration create(String operationsBeanName) {
        return new Configuration(operationsBeanName);
    }

    private Configuration(String operationsBeanName) {
        this.operationsBeanName = operationsBeanName;
    }
}
