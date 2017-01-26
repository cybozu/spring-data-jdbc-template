package com.cybozu.spring.data.jdbc.template.mapper;

import java.util.Map;

import javax.persistence.Column;

import lombok.Getter;
import lombok.Setter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class BeanEntityMapperTest {
    static class TestEntity {
        @Getter
        @Setter
        @Column(name = "field_1")
        private String field1;

        public Long field2;
    }

    @Test
    public void testCreateInstance() {
        BeanEntityMapper<TestEntity> mapper = BeanEntityMapper.create(TestEntity.class);

        TestEntity entity = mapper.createInstance();
        assertThat(entity).isExactlyInstanceOf(TestEntity.class);
    }

    @Test
    public void testTypes() {
        BeanEntityMapper<TestEntity> mapper = BeanEntityMapper.create(TestEntity.class);

        Map<String, Class<?>> actual = mapper.types();

        assertThat(actual).containsExactly(entry("field_1", String.class), entry("field2", Long.class));
    }

    @Test
    public void testSetValue() {
        BeanEntityMapper<TestEntity> mapper = BeanEntityMapper.create(TestEntity.class);

        TestEntity entity = new TestEntity();

        mapper.startMapping(entity);
        mapper.setValue(entity, "field_1", "value1");
        mapper.setValue(entity, "field2", 42L);
        mapper.finishMapping(entity);

        assertThat(entity.getField1()).isEqualTo("value1");
        assertThat(entity.field2).isEqualTo(42L);
    }
}
