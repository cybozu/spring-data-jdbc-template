package com.cybozu.spring.data.jdbc.core;

import java.io.Serializable;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import com.cybozu.spring.data.jdbc.core.JdbcTemplateRepository.IdPlaceHolder;

@NoRepositoryBean
public interface JdbcTemplateRepository<T> extends Repository<T, IdPlaceHolder> {
    void insert(T entity);

    void update(T entity);

    interface IdPlaceHolder extends Serializable {
    }
}
