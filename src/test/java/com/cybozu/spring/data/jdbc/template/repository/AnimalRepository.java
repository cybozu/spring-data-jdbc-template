package com.cybozu.spring.data.jdbc.template.repository;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cybozu.spring.data.jdbc.template.JdbcTemplateRepository;
import com.cybozu.spring.data.jdbc.template.annotation.Modifying;
import com.cybozu.spring.data.jdbc.template.annotation.Query;
import com.cybozu.spring.data.jdbc.template.annotation.SingleColumn;

@Repository
public interface AnimalRepository extends JdbcTemplateRepository<Animal> {
    @Query("SELECT * FROM " + Animal.TABLE_NAME + " WHERE name = :name")
    Animal getOneByName(@Param("name") String name);

    @Query("SELECT * FROM " + Animal.TABLE_NAME + " WHERE name IN (:names)")
    List<Animal> getByNames(@Param("names") List<String> names);

    @Query("SELECT count(*) FROM " + Animal.TABLE_NAME)
    @SingleColumn
    int countAll();

    @Modifying
    @Query("UPDATE " + Animal.TABLE_NAME + " SET scientific_name = :scientificName WHERE name = :name")
    int updateScientificNameByName(@Param("scientificName") String scientificName, @Param("name") String name);

    Animal getOneByNameWithoutQuery(@Param("name") String name);

}
