package com.cybozu.spring.data.jdbc.template.util;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class SimpleJdbcUpdateTest {
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

    @Test
    public void testGenerateUpdateQuery() {
        String query = SimpleJdbcUpdate.generateUpdateQuery(TestEntity.class, "test_table");
        assertThat(query)
                .isEqualTo(
                        "UPDATE test_table SET field_1 = :field_1 , field4 = :field4 WHERE field_2 = :field_2 AND field3 = :field3");
    }
}
