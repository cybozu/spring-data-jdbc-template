package com.cybozu.spring.data.jdbc.template.repository;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cybozu.spring.data.jdbc.template.JdbcTemplateRepository;
import com.cybozu.spring.data.jdbc.template.annotation.Query;

@Repository
public interface AnimalWithCallbackRepository extends JdbcTemplateRepository<AnimalWithCallback> {
    @Query("SELECT * FROM " + AnimalWithCallback.TABLE_NAME + " WHERE name = :name")
    AnimalWithCallback getOneByName(@Param("name") String name);
}
