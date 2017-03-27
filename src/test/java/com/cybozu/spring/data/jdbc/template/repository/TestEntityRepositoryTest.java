package com.cybozu.spring.data.jdbc.template.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cybozu.spring.data.jdbc.template.repository.TestEntity.Status;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;

import static org.assertj.core.api.Assertions.*;

@RepositoryTest
@RunWith(SpringJUnit4ClassRunner.class)
public class TestEntityRepositoryTest {
    private static final Operation DROP_TABLE = Operations.sql("DROP TABLE IF EXISTS test");
    private static final Operation CREATE_TABLE = Operations
            .sql("CREATE TABLE test ("
                    + "id bigint(20) NOT NULL AUTO_INCREMENT, name varchar(256) NOT NULL, value text, status varchar(256) NOT NULL)");
    @Autowired
    private DataSource dataSource;
    @Autowired
    private TestEntityRepository sut;

    @After
    public void tearDown() {
        DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), Operations.sequenceOf(DROP_TABLE));
        dbSetup.launch();
    }

    private void initDb(Operation... operations) {
        List<Operation> ops = new ArrayList<>();
        ops.add(DROP_TABLE);
        ops.add(CREATE_TABLE);
        ops.addAll(Arrays.asList(operations));
        DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), Operations.sequenceOf(ops));

        dbSetup.launch();
    }

    @Test
    public void testGetOneByName() {
        initDb(Operations.insertInto("test").columns("name", "value", "status").values("a", "this is value!", "GOOD")
                .build());
        TestEntity actual = sut.getOneByName("a");
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getName()).isEqualTo("a");
        assertThat(actual.getValue()).isEqualTo("this is value!");
        assertThat(actual.getStatus()).isEqualTo(Status.GOOD);
    }

    @Test
    public void testGetByName() {
        initDb(Operations.insertInto("test").columns("name", "value", "status").values("a", "this is value!", "GOOD")
                .values("b", null, "BAD").build());
        List<TestEntity> actual = sut.getByNames(Arrays.asList("a", "b"));
        assertThat(actual).extracting(e -> e.getId()).allMatch(Objects::nonNull);
        assertThat(actual).extracting(e -> e.getName()).containsExactlyInAnyOrder("a", "b");
        assertThat(actual).extracting(e -> e.getValue()).containsExactlyInAnyOrder("this is value!", null);
        assertThat(actual).extracting(e -> e.getStatus()).containsExactlyInAnyOrder(Status.GOOD, Status.BAD);
    }
}
