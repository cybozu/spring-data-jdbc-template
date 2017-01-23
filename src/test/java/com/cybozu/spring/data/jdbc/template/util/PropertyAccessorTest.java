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

        @Column
        public Long getField2() {
            return 42L;
        }

        public void setField2(Long l) {
        }

        public boolean getField3() {
            return true;
        }

        @Column
        public void setField3(boolean b) {
        }

        @Column(name = "get")
        public int getField4() {
            return 1;
        }

        @Column(name = "set")
        public void setField4(int i) {
        }

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
        assertThat(propertyAccessor("field2").getAnnotation(Column.class)).isNotNull().isInstanceOf(Column.class);
        assertThat(propertyAccessor("field3").getAnnotation(Column.class)).isNotNull().isInstanceOf(Column.class);
        assertThat(propertyAccessor("field4").getAnnotation(Column.class).name()).isEqualTo("get");
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
