package com.cybozu.spring.data.jdbc.template.repository;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cybozu.spring.data.jdbc.template.JdbcTemplateRepository;
import com.cybozu.spring.data.jdbc.template.annotation.Query;

@Repository
public interface AnimalRepository extends JdbcTemplateRepository<Animal> {
    @Query("SELECT * FROM " + Animal.TABLE_NAME + " WHERE name = :name")
    Animal getOneByName(@Param("name") String name);

    @Query("SELECT * FROM " + Animal.TABLE_NAME + " WHERE name IN (:names)")
    List<Animal> getByNames(@Param("names") List<String> names);
}
