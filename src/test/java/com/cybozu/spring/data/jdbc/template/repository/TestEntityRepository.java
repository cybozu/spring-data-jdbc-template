package com.cybozu.spring.data.jdbc.template.repository;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cybozu.spring.data.jdbc.template.JdbcTemplateRepository;
import com.cybozu.spring.data.jdbc.template.annotation.Query;

@Repository
public interface TestEntityRepository extends JdbcTemplateRepository<TestEntity> {
    @Query("SELECT * FROM test WHERE name = :name")
    TestEntity getOneByName(@Param("name") String name);

    @Query("SELECT * FROM test WHERE name = :name")
    List<TestEntity> getByName(@Param("name") String name);
}
