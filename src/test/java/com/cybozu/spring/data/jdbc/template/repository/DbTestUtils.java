package com.cybozu.spring.data.jdbc.template.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;

public final class DbTestUtils {
    private DbTestUtils() {
        throw new AssertionError();
    }

    public static void initDb(DataSource dataSource, Operation createTable, Operation dropTable,
            Operation... operations) {
        List<Operation> ops = new ArrayList<>();
        ops.add(dropTable);
        ops.add(createTable);
        ops.addAll(Arrays.asList(operations));
        DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), Operations.sequenceOf(ops));

        dbSetup.launch();
    }
}
