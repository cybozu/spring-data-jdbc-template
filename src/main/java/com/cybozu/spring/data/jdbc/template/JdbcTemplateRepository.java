package com.cybozu.spring.data.jdbc.template;

import java.io.Serializable;
import java.util.function.BiPredicate;

import javax.annotation.Nullable;

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
    default void update(T entity) {
        update(entity, (column, value) -> true);
    }

    /**
     * Update an entity.
     *
     * @param entity
     *            an updated entity
     * @param includeColumnPredicate
     *            a predicate to determine whether a column should be used for updating. The first argument is the
     *            column name and the second argument is the value.
     *
     */
    void update(T entity, @Nullable BiPredicate<String, Object> includeColumnPredicate);

    // We don't use CrudRepository. Thus ID type is not required.
    interface IdPlaceholder extends Serializable {
    }
}
