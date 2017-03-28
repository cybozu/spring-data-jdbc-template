package com.cybozu.spring.data.jdbc.template.repository;

import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cybozu.spring.data.jdbc.template.JdbcTemplateRepository;
import com.cybozu.spring.data.jdbc.template.annotation.Mapper;
import com.cybozu.spring.data.jdbc.template.annotation.Query;
import com.cybozu.spring.data.jdbc.template.mapper.AbstractEntityMapper;

@Repository
public interface AnimalCustomMapperRepository extends JdbcTemplateRepository<Animal> {
    class CustomMapper extends AbstractEntityMapper<Animal> {
        @Override
        public void setValue(Animal animal, @Nonnull String name, Object value) {
            switch (name) {
            case "id":
                animal.setId(((Number) value).longValue());
                break;
            case "name":
                animal.setName(((String) value));
                break;
            case "scientific_name":
                animal.setScientificName(((String) value));
                break;
            case "status":
                animal.setStatus(Animal.Status.valueOf(((String) value)));
                break;
            default:
                // do nothing
            }
        }
    }

    @Query("SELECT * FROM " + Animal.TABLE_NAME + " WHERE status = :status")
    @Mapper(CustomMapper.class)
    List<Animal> getByStatus(@Param("status") String status);
}
