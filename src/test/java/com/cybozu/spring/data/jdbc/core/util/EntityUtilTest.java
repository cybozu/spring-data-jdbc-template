package com.cybozu.spring.data.jdbc.core.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class EntityUtilTest {

    @Table(name = "test_table")
    private static class TestEntity {
        @Getter
        @Setter
        @Column(name = "field_1")
        private String field1;

        @Id
        @GeneratedValue
        @Column(name = "field_2")
        public int field2;

        @Id
        @Getter
        @Setter
        private Long field3;

        @Column
        public boolean field4;
    }

    private static class TestEntity2 {

    }

    private Accessor propertyAccessor(String name) throws IntrospectionException {
        return Accessors.ofProperty(TestEntity.class, new PropertyDescriptor(name, TestEntity.class));
    }

    @Test
    public void testColumnNameForPropertyWithColumn() throws Exception {
        assertThat(EntityUtil.columnName(propertyAccessor("field1"))).isEqualTo("field_1");
    }

    @Test
    public void testColumnNameForFieldWithColumn() throws Exception {
        assertThat(EntityUtil.columnName(Accessors.ofField(TestEntity.class.getField("field2")))).isEqualTo("field_2");
    }

    @Test
    public void testColumnNameForPropertyWithoutColumn() throws Exception {
        assertThat(EntityUtil.columnName(propertyAccessor("field3"))).isEqualTo("field3");
    }

    @Test
    public void testColumnNameForFieldWithoutColumnNameAttr() throws Exception {
        assertThat(EntityUtil.columnName(Accessors.ofField(TestEntity.class.getField("field4")))).isEqualTo("field4");
    }

    @Test
    public void testTableNameWithTableAnnotation() throws Exception {
        assertThat(EntityUtil.tableName(TestEntity.class)).isEqualTo("test_table");
    }

    @Test
    public void testTableNameWithoutTableAnnotation() throws Exception {
        assertThat(EntityUtil.tableName(TestEntity2.class)).isEqualTo("TestEntity2");
    }

    @Test
    public void testGetAccessors() throws Exception {
        List<Accessor> accessors = EntityUtil.getAccessors(TestEntity.class);
        assertThat(accessors).extracting(a -> a.getName()).containsExactlyInAnyOrder("field1", "field2", "field3",
                "field4");
    }

    @Test
    public void testKeyNames() {
        List<String> keyNames = EntityUtil.keyNames(TestEntity.class);
        assertThat(keyNames).containsExactlyInAnyOrder("field_2", "field3");
    }

    @Test
    public void testColumnNames() {
        assertThat(EntityUtil.columnNames(TestEntity.class)).containsExactlyInAnyOrder("field_1", "field_2", "field3",
                "field4");
    }

    @Test
    public void testColumnNamesExceptKeys() {
        assertThat(EntityUtil.columnNamesExceptKeys(TestEntity.class)).containsExactlyInAnyOrder("field_1", "field4");
    }

    @Test
    public void testValuesIncludingGeneratedValues() {
        TestEntity entity = new TestEntity();
        entity.setField1("FIELD_1");
        entity.field2 = 2;
        entity.setField3(3L);
        entity.field4 = true;

        Map<String, Object> values = EntityUtil.values(entity, TestEntity.class, true);

        assertThat(values).containsOnly(entry("field_1", "FIELD_1"), entry("field_2", 2), entry("field3", 3L),
                entry("field4", true));
    }

    @Test
    public void testValuesNotIncludingGeneratedValues() {
        TestEntity entity = new TestEntity();
        entity.setField1("FIELD_1");
        entity.field2 = 2;
        entity.setField3(3L);
        entity.field4 = true;

        Map<String, Object> values = EntityUtil.values(entity, TestEntity.class, false);
        assertThat(values).containsOnly(entry("field_1", "FIELD_1"), entry("field3", 3L), entry("field4", true));
    }
}
