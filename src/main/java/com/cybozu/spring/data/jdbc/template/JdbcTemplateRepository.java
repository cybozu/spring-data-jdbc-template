package com.cybozu.spring.data.jdbc.template;

import java.io.Serializable;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import com.cybozu.spring.data.jdbc.template.JdbcTemplateRepository.IdPlaceholder;

@NoRepositoryBean
public interface JdbcTemplateRepository<T> extends Repository<T, IdPlaceholder> {
    void insert(T entity);

    Number insertAndReturnKey(T entity);

    void update(T entity);

    // We don't use CrudRepository. Thus ID type is not required.
    interface IdPlaceholder extends Serializable {
    }
}
