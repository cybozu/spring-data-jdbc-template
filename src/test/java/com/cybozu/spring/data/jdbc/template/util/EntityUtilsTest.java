package com.cybozu.spring.data.jdbc.template.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class EntityUtilsTest {

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

        @Column(name = "field_5")
        public double getField5() {
            return 0.0;
        }

        public void setField5(double d) {

        }

        public byte[] getField6() {
            return new byte[] { 1, 2, 3 };
        }

        @Column(name = "field_6")
        public void setField6(byte[] ary) {

        }

        @Getter
        @Setter
        @Transient
        private String transient1;

        @Getter
        @Setter
        private transient String transient2;

        public transient String transient3;

        @Transient
        public String getTransient4() {
            return "";
        }

        public void setTransient4(String s) {

        }

        public String getTransient5() {
            return "";
        }

        @Transient
        public void setTransient5(String s) {

        }
    }

    private static class TestEntity2 {

    }

    static class Parent {
        @Getter
        @Setter
        @Column(name = "field_parent")
        @Id
        private Long fieldParent;
    }

    static class Child extends Parent {
        @Getter
        @Setter
        @Column(name = "field_child")
        private String fieldChild;
    }

    private Accessor propertyAccessor(String name) throws IntrospectionException {
        return Accessors.ofProperty(TestEntity.class, new PropertyDescriptor(name, TestEntity.class));
    }

    @Test
    public void testColumnNameForPropertyWithColumn() throws Exception {
        assertThat(EntityUtils.columnName(propertyAccessor("field1"))).isEqualTo("field_1");
    }

    @Test
    public void testColumnNameForFieldWithColumn() throws Exception {
        assertThat(EntityUtils.columnName(Accessors.ofField(TestEntity.class.getField("field2")))).isEqualTo("field_2");
    }

    @Test
    public void testColumnNameForPropertyWithoutColumn() throws Exception {
        assertThat(EntityUtils.columnName(propertyAccessor("field3"))).isEqualTo("field3");
    }

    @Test
    public void testColumnNameForFieldWithoutColumnNameAttr() throws Exception {
        assertThat(EntityUtils.columnName(Accessors.ofField(TestEntity.class.getField("field4")))).isEqualTo("field4");
    }

    @Test
    public void testTableNameWithTableAnnotation() throws Exception {
        assertThat(EntityUtils.tableName(TestEntity.class)).isEqualTo("test_table");
    }

    @Test
    public void testTableNameWithoutTableAnnotation() throws Exception {
        assertThat(EntityUtils.tableName(TestEntity2.class)).isEqualTo("TestEntity2");
    }

    @Test
    public void testGetAccessors() throws Exception {
        List<Accessor> accessors = EntityUtils.getAccessors(TestEntity.class);
        assertThat(accessors).extracting(a -> a.getName()).containsExactlyInAnyOrder("field1", "field2", "field3",
                "field4", "field5", "field6");
    }

    @Test
    public void testGetAccessorsWithParentClass() throws Exception {
        List<Accessor> accessors = EntityUtils.getAccessors(Child.class);
        assertThat(accessors).extracting(a -> a.getName()).containsExactlyInAnyOrder("fieldParent", "fieldChild");
    }

    @Test
    public void testKeyNames() {
        Set<String> keyNames = EntityUtils.keyNames(TestEntity.class);
        assertThat(keyNames).containsExactlyInAnyOrder("field_2", "field3");
    }

    @Test
    public void testColumnNames() {
        assertThat(EntityUtils.columnNames(TestEntity.class)).containsExactlyInAnyOrder("field_1", "field_2", "field3",
                "field4", "field_5", "field_6");
    }

    @Test
    public void testColumnNamesWithParentClass() {
        assertThat(EntityUtils.columnNames(Child.class)).containsExactlyInAnyOrder("field_parent", "field_child");
    }

    @Test
    public void testColumnNamesExceptKeys() {
        assertThat(EntityUtils.columnNamesExceptKeys(TestEntity.class)).containsExactlyInAnyOrder("field_1", "field4",
                "field_5", "field_6");
    }

    @Test
    public void testColumnNamesExceptKeysWithParentClass() {
        assertThat(EntityUtils.columnNamesExceptKeys(Child.class)).containsExactlyInAnyOrder("field_child");
    }

    @Test
    public void testValuesIncludingGeneratedValues() {
        TestEntity entity = new TestEntity();
        entity.setField1("FIELD_1");
        entity.field2 = 2;
        entity.setField3(3L);
        entity.field4 = true;

        Map<String, Object> values = EntityUtils.values(entity, TestEntity.class, true);

        assertThat(values).containsOnly(entry("field_1", "FIELD_1"), entry("field_2", 2), entry("field3", 3L),
                entry("field4", true), entry("field_5", 0.0), entry("field_6", new byte[] { 1, 2, 3 }));
    }

    @Test
    public void testValuesNotIncludingGeneratedValues() {
        TestEntity entity = new TestEntity();
        entity.setField1("FIELD_1");
        entity.field2 = 2;
        entity.setField3(3L);
        entity.field4 = true;

        Map<String, Object> values = EntityUtils.values(entity, TestEntity.class, false);
        assertThat(values).containsOnly(entry("field_1", "FIELD_1"), entry("field3", 3L), entry("field4", true),
                entry("field_5", 0.0), entry("field_6", new byte[] { 1, 2, 3 }));
    }

    @Test
    public void testGeneratedValueColumnNames() {
        assertThat(EntityUtils.generateValueColumnNames(TestEntity.class)).containsExactlyInAnyOrder("field_2");
    }

    @Test
    public void testColumnNamesExceptGeneratedValues() {
        assertThat(EntityUtils.columnNamesExceptGeneratedValues(TestEntity.class)).containsExactlyInAnyOrder("field_1",
                "field3", "field4", "field_5", "field_6");
    }

    private enum TestEnum {
        A
    }

    @Test
    public void testStringToEnum() {
        assertThat(EntityUtils.stringToEnum("A", TestEnum.class)).isEqualTo(TestEnum.A);
        assertThat(EntityUtils.stringToEnum(" A ", TestEnum.class)).isEqualTo(TestEnum.A);
        assertThatThrownBy(() -> EntityUtils.stringToEnum("a", TestEnum.class)).isInstanceOf(
                IllegalArgumentException.class);
        assertThatThrownBy(() -> EntityUtils.stringToEnum("X", TestEnum.class)).isInstanceOf(
                IllegalArgumentException.class);
    }
}
