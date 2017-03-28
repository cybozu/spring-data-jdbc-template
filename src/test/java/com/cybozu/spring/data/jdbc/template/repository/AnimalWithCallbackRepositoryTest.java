package com.cybozu.spring.data.jdbc.template.repository;

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

import static org.mockito.Mockito.*;

@RepositoryTest
@RunWith(SpringJUnit4ClassRunner.class)
public class AnimalWithCallbackRepositoryTest {
    private static final Operation DROP_TABLE = Operations.sql("DROP TABLE IF EXISTS " + AnimalWithCallback.TABLE_NAME);
    private static final Operation CREATE_TABLE = Operations
            .sql("CREATE TABLE "
                    + AnimalWithCallback.TABLE_NAME
                    + " ("
                    + "id bigint(20) NOT NULL AUTO_INCREMENT, name varchar(256) NOT NULL, scientific_name text, status varchar(256) NOT NULL)");
    @Autowired
    private DataSource dataSource;
    @Autowired
    private AnimalWithCallbackRepository sut;

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

    private Table table() {
        return new Table(dataSource, AnimalWithCallback.TABLE_NAME);
    }

    @Test
    public void testInsert() {
        initDb();

        AnimalWithCallback entity = spy(new AnimalWithCallback());
        entity.setName("aurochs");
        entity.setScientificName("Bos primigenius");
        entity.setStatus(Status.EX);

        sut.insert(entity);

        verify(entity).beforeInsert();
        verify(entity).afterInsert();
        verify(entity, never()).beforeUpdate();
        verify(entity, never()).afterUpdate();
    }

    @Test
    public void testInsertAndReturnKey() {
        initDb(insertBuilder().values("serval", "Leptailurus serval", "LC").build());

        AnimalWithCallback entity = spy(new AnimalWithCallback());
        entity.setName("lion");
        entity.setScientificName("Panthera leo");
        entity.setStatus(Status.VU);

        sut.insertAndReturnKey(entity);

        verify(entity).beforeInsert();
        verify(entity).afterInsert();
        verify(entity, never()).beforeUpdate();
        verify(entity, never()).afterUpdate();
    }

    @Test
    public void testUpdate() {
        initDb(insertBuilder().values("humboldt penguin", "Spheniscus humboldti", "LC").build());

        AnimalWithCallback entity = spy(sut.getOneByName("humboldt penguin"));
        entity.setStatus(Status.VU);

        sut.update(entity);

        verify(entity, never()).beforeInsert();
        verify(entity, never()).afterInsert();
        verify(entity).beforeUpdate();
        verify(entity).afterUpdate();
    }

    @Test
    public void insertCanCauseSideEffect() {
        initDb();

        AnimalWithCallback entity = spy(new AnimalWithCallback());
        entity.setName("western lowland gorilla");
        entity.setScientificName("Gorilla gorilla");
        entity.setStatus(Status.CR);

        doAnswer(invocation -> {
            AnimalWithCallback gorilla = (AnimalWithCallback) invocation.getMock();
            entity.setScientificName(gorilla.getScientificName() + " gorilla");
            return null;
        }).when(entity).beforeInsert();

        sut.insert(entity);

        Assertions.assertThat(table()).row(0).hasValues(1L, "western lowland gorilla", "Gorilla gorilla gorilla", "CR");
    }

    @Test
    public void updateCanCauseSideEffect() {
        initDb(insertBuilder().values("western lowland gorilla", "Gorilla gorilla", "LC").build());

        AnimalWithCallback entity = spy(sut.getOneByName("western lowland gorilla"));
        entity.setStatus(Status.CR);

        doAnswer(invocation -> {
            AnimalWithCallback gorilla = (AnimalWithCallback) invocation.getMock();
            entity.setScientificName(gorilla.getScientificName() + " gorilla");
            return null;
        }).when(entity).beforeUpdate();

        sut.update(entity);

        Assertions.assertThat(table()).row(0).hasValues(1L, "western lowland gorilla", "Gorilla gorilla gorilla", "CR");
    }
}
