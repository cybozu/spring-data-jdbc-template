package com.cybozu.spring.data.jdbc.template.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.assertj.db.api.Assertions;
import org.assertj.db.type.Table;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cybozu.spring.data.jdbc.template.repository.Animal.Status;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Insert;
import com.ninja_squad.dbsetup.operation.Operation;

import static org.assertj.core.api.Assertions.*;

@RepositoryTest
@RunWith(SpringJUnit4ClassRunner.class)
public class AnimalRepositoryTest {
    private static final Operation DROP_TABLE = Operations.sql("DROP TABLE IF EXISTS " + Animal.TABLE_NAME);
    private static final Operation CREATE_TABLE = Operations
            .sql("CREATE TABLE "
                    + Animal.TABLE_NAME
                    + " ("
                    + "id bigint(20) NOT NULL AUTO_INCREMENT, name varchar(256) NOT NULL, scientific_name text, status varchar(256) NOT NULL)");
    @Autowired
    private DataSource dataSource;
    @Autowired
    private AnimalRepository sut;

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

    private Insert.Builder insertBuilder() {
        return Operations.insertInto(Animal.TABLE_NAME).columns("name", "scientific_name", "status");
    }

    private Table table() {
        return new Table(dataSource, Animal.TABLE_NAME);
    }

    @Test
    public void testGetOneByName() {
        initDb(insertBuilder().values("serval", "Leptailurus serval", "LC").build());

        Animal actual = sut.getOneByName("serval");
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getName()).isEqualTo("serval");
        assertThat(actual.getScientificName()).isEqualTo("Leptailurus serval");
        assertThat(actual.getStatus()).isEqualTo(Status.LC);
    }

    @Test
    public void testGetByName() {
        initDb(insertBuilder().values("serval", "Leptailurus serval", "LC")
                .values("crested ibis", "Nipponia nippon", "EN").build());

        List<Animal> actual = sut.getByNames(Arrays.asList("serval", "crested ibis"));
        assertThat(actual).extracting(e -> e.getId()).allMatch(Objects::nonNull);
        assertThat(actual).extracting(e -> e.getName()).containsExactlyInAnyOrder("serval", "crested ibis");
        assertThat(actual).extracting(e -> e.getScientificName()).containsExactlyInAnyOrder("Leptailurus serval",
                "Nipponia nippon");
        assertThat(actual).extracting(e -> e.getStatus()).containsExactlyInAnyOrder(Status.LC, Status.EN);
    }

    @Test
    public void testInsert() {
        initDb();

        Animal entity = new Animal();
        entity.setName("aurochs");
        entity.setScientificName("Bos primigenius");
        entity.setStatus(Status.EX);

        sut.insert(entity);

        Assertions.assertThat(table()).hasNumberOfRows(1).row(0).hasValues(1L, "aurochs", "Bos primigenius", "EX");
    }

    @Test
    public void testInsertAndReturnKey() {
        initDb(insertBuilder().values("serval", "Leptailurus serval", "LC").build());

        Animal entity = new Animal();
        entity.setName("lion");
        entity.setScientificName("Panthera leo");
        entity.setStatus(Status.VU);

        Number id = sut.insertAndReturnKey(entity);
        assertThat(id).isEqualTo(2L);

        Assertions.assertThat(table()).hasNumberOfRows(2).row(1).hasValues(2L, "lion", "Panthera leo", "VU");
    }

    @Test
    public void testUpdate() {
        initDb(insertBuilder().values("humboldt penguin", "Spheniscus humboldti", "LC").build());

        Animal humboldt = sut.getOneByName("humboldt penguin");
        humboldt.setStatus(Status.VU);

        sut.update(humboldt);

        Assertions.assertThat(table()).hasNumberOfRows(1).row(0)
                .hasValues(1L, "humboldt penguin", "Spheniscus humboldti", "VU");
    }
}
