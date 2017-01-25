package com.cybozu.spring.data.jdbc.template;

import java.io.Serializable;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.cybozu.spring.data.jdbc.template.JdbcTemplateRepository.IdPlaceholder;

@NoRepositoryBean
public interface JdbcTemplateRepository<T> extends Repository<T, IdPlaceholder> {

    /**
     * Insert an entity to the table.
     * 
     * <p>
     * Note: In order to use this method, {@link NamedParameterJdbcOperations#getJdbcOperations()} of the registered
     * {@link NamedParameterJdbcOperations} must return an instance of
     * {@link org.springframework.jdbc.core.JdbcTemplate}.
     * </p>
     *
     * @param entity
     *            an inserted entity
     *
     * @throws ClassCastException
     *             if {@link NamedParameterJdbcOperations#getJdbcOperations()} does not return an instance of
     *             {@link org.springframework.jdbc.core.JdbcTemplate}.
     *
     */
    void insert(T entity);

    /**
     * Insert an entity to the table and obtain the auto-generated key.
     * 
     * @param entity
     *            an inserted entity
     *
     * @return an auto-generated key
     * @see JdbcTemplateRepository#insert(Object)
     */
    Number insertAndReturnKey(T entity);

    /**
     * Update an entity.
     *
     * @param entity
     *            an updated entity
     */
    void update(T entity);

    // We don't use CrudRepository. Thus ID type is not required.
    interface IdPlaceholder extends Serializable {
    }
}
