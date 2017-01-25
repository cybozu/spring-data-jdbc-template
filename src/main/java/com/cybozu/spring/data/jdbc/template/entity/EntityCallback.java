package com.cybozu.spring.data.jdbc.template.entity;

/**
 * This interface provides callbacks for entities. If an entity class implements this interface,
 * {@link com.cybozu.spring.data.jdbc.template.JdbcTemplateRepository}'s methods call the callback methods.
 */
public interface EntityCallback {
    /**
     * This method is called before inserting the entity.
     */
    default void beforeInsert() {
    }

    /**
     * This method is called after inserting the entity.
     */
    default void afterInsert() {
    }

    /**
     * This method is called before updating the entity.
     */
    default void beforeUpdate() {
    }

    /**
     * This method is called after updating the entity.
     */
    default void afterUpdate() {
    }
}
