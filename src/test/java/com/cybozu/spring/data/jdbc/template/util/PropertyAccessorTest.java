package com.cybozu.spring.data.jdbc.template.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import javax.persistence.Column;

import lombok.Getter;
import lombok.Setter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class PropertyAccessorTest {
    private static class TestEntity {
        @Getter
        @Setter
        @Column
        private String field1;

        @Getter
        @Setter
        private transient String transientField;
    }

    private Accessor propertyAccessor(String name) throws IntrospectionException {
        return Accessors.ofProperty(TestEntity.class, new PropertyDescriptor(name, TestEntity.class));
    }

    @Test
    public void testGetName() throws Exception {
        assertThat(propertyAccessor("field1").getName()).isEqualTo("field1");
    }

    @Test
    public void testGetValueType() throws Exception {
        assertThat(propertyAccessor("field1").getValueType()).isEqualTo(String.class);
    }

    @Test
    public void testGetAnnotation() throws Exception {
        assertThat(propertyAccessor("field1").getAnnotation(Column.class)).isNotNull().isInstanceOf(Column.class);
    }

    @Test
    public void testGetValue() throws Exception {
        TestEntity entity = new TestEntity();
        entity.setField1("test");
        assertThat(propertyAccessor("field1").getValue(entity)).isEqualTo("test");
    }

    @Test
    public void testSetValue() throws Exception {
        TestEntity entity = new TestEntity();
        propertyAccessor("field1").setValue(entity, "test");
        assertThat(entity.getField1()).isEqualTo("test");
    }

    @Test
    public void testIsTransient() throws Exception {
        assertThat(propertyAccessor("field1").isTransient()).isFalse();
        assertThat(propertyAccessor("transientField").isTransient()).isTrue();
    }
}
