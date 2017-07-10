package com.cybozu.spring.data.jdbc.template.repository;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import static org.mockito.Mockito.*;

public abstract class NamedParameterJdbcOperationsMock implements NamedParameterJdbcOperations {
    public static NamedParameterJdbcOperationsMock create() {
        return mock(NamedParameterJdbcOperationsMock.class);
    }
}
