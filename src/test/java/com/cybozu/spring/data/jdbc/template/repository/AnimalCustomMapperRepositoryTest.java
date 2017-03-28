package com.cybozu.spring.data.jdbc.template.repository;

import java.util.List;

import javax.sql.DataSource;

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
public class AnimalCustomMapperRepositoryTest {
    private static final Operation DROP_TABLE = Operations.sql("DROP TABLE IF EXISTS " + Animal.TABLE_NAME);
    private static final Operation CREATE_TABLE = Operations
            .sql("CREATE TABLE "
                    + Animal.TABLE_NAME
                    + " ("
                    + "id bigint(20) NOT NULL AUTO_INCREMENT, name varchar(256) NOT NULL, scientific_name text, status varchar(256) NOT NULL)");
    @Autowired
    private DataSource dataSource;
    @Autowired
    private AnimalCustomMapperRepository sut;

    @After
    public void tearDown() {
        DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), Operations.sequenceOf(DROP_TABLE));
        dbSetup.launch();
    }

    private void initDb(Operation... operations) {
        DbTestUtils.initDb(dataSource, CREATE_TABLE, DROP_TABLE, operations);
    }

    private Insert.Builder insertBuilder() {
        return Operations.insertInto(Animal.TABLE_NAME).columns("name", "scientific_name", "status");
    }

    @Test
    public void testCustomMapper() {
        initDb(insertBuilder().values("serval", "Leptailurus serval", "LC")
                .values("crested ibis", "Nipponia nippon", "EN").build());
        List<Animal> animals = sut.getByStatus("LC");
        assertThat(animals).hasSize(1);

        Animal animal = animals.get(0);
        assertThat(animal.getId()).isEqualTo(1L);
        assertThat(animal.getName()).isEqualTo("serval");
        assertThat(animal.getScientificName()).isEqualTo("Leptailurus serval");
        assertThat(animal.getStatus()).isEqualTo(Status.LC);
    }
}
