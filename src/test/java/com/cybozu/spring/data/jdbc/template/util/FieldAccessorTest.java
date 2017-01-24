package com.cybozu.spring.data.jdbc.template.util;

import javax.persistence.Column;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class FieldAccessorTest {
    private static class TestEntity {
        @Column
        public String field1;

        public transient String transientField;
    }

    private Accessor fieldAccessor(String name) throws NoSuchFieldException {
        return Accessors.ofField(TestEntity.class.getField(name));
    }

    @Test
    public void testGetName() throws Exception {
        assertThat(fieldAccessor("field1").getName()).isEqualTo("field1");
    }

    @Test
    public void testGetValueType() throws Exception {
        assertThat(fieldAccessor("field1").getValueType()).isEqualTo(String.class);
    }

    @Test
    public void testGetAnnotation() throws Exception {
        assertThat(fieldAccessor("field1").getAnnotation(Column.class)).isNotNull().isInstanceOf(Column.class);
    }

    @Test
    public void testGetValue() throws Exception {
        TestEntity entity = new TestEntity();
        entity.field1 = "test";
        assertThat(fieldAccessor("field1").getValue(entity)).isEqualTo("test");
    }

    @Test
    public void testSetValue() throws Exception {
        TestEntity entity = new TestEntity();
        fieldAccessor("field1").setValue(entity, "test");
        assertThat(entity.field1).isEqualTo("test");
    }

    @Test
    public void testIsTransient() throws Exception {
        assertThat(fieldAccessor("field1").isTransient()).isFalse();
        assertThat(fieldAccessor("transientField").isTransient()).isTrue();
    }

}
