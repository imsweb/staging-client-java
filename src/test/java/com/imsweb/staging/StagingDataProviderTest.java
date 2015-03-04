package com.imsweb.staging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

import com.imsweb.decisionengine.ColumnDefinition.ColumnType;
import com.imsweb.staging.entities.StagingColumnDefinition;
import com.imsweb.staging.entities.StagingTable;

public class StagingDataProviderTest {

    @Test
    public void testExtraInput() {
        StagingTable table = new StagingTable();
        table.setId("test_table");
        StagingColumnDefinition def1 = new StagingColumnDefinition();
        def1.setKey("input1");
        def1.setName("Input 1");
        def1.setType(ColumnType.INPUT);
        StagingColumnDefinition def2 = new StagingColumnDefinition();
        def2.setKey("result1");
        def2.setName("Result1");
        def2.setType(ColumnType.ENDPOINT);
        table.setColumnDefinitions(Arrays.asList(def1, def2));
        table.setRawRows(new ArrayList<List<String>>());
        table.getRawRows().add(Arrays.asList("1", "MATCH"));
        table.getRawRows().add(Arrays.asList("2", "VALUE:{{extra1}}"));
        table.getRawRows().add(Arrays.asList("{{extra2}}", "MATCH"));
        table.getRawRows().add(Arrays.asList("{{ctx_year_current}}", "MATCH"));
        table.getRawRows().add(Arrays.asList("5", "VALUE:{{ctx_year_current}}"));
        table.getRawRows().add(Arrays.asList("6", "MATCH:{{match_extra}}"));
        table.getRawRows().add(Arrays.asList("7", "ERROR:{{error_extra}}"));

        StagingDataProvider.initTable(table);

        // since context variables are not user-supplied, they should not be included in the extra input
        Assert.assertEquals(Sets.newHashSet("extra1", "extra2"), table.getExtraInput());

        table.setRawRows(new ArrayList<List<String>>());
        table.getRawRows().add(Arrays.asList("{{ctx_year_current}}", "MATCH"));

        StagingDataProvider.initTable(table);

        Assert.assertNull(table.getExtraInput());
    }
}