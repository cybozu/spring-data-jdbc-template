package com.cybozu.spring.data.jdbc.template;

import java.io.Serializable;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import com.cybozu.spring.data.jdbc.template.JdbcTemplateRepository.IdPlaceHolder;

@NoRepositoryBean
public interface JdbcTemplateRepository<T> extends Repository<T, IdPlaceHolder> {
    void insert(T entity);

    void update(T entity);

    interface IdPlaceHolder extends Serializable {
    }
}
