/*
 * Copyright (C) 2018 Information Management Services, Inc.
 */
package com.imsweb.staging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.imsweb.decisionengine.ColumnDefinition.ColumnType;
import com.imsweb.staging.StagingData.Result;
import com.imsweb.staging.entities.StagingColumnDefinition;
import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingSchemaInput;
import com.imsweb.staging.entities.StagingTable;

import static org.junit.Assert.assertEquals;

public class BasicStagingTest {

    @Test
    public void testBlankInputs() {
        InMemoryDataProvider provider = new InMemoryDataProvider("test", "1.0");

        StagingTable table = new StagingTable();
        table.setId("table_input1");
        StagingColumnDefinition def1 = new StagingColumnDefinition();
        def1.setKey("input1");
        def1.setName("Input 1");
        def1.setType(ColumnType.INPUT);
        StagingColumnDefinition def2 = new StagingColumnDefinition();
        def2.setKey("result1");
        def2.setName("Result1");
        def2.setType(ColumnType.DESCRIPTION);
        table.setColumnDefinitions(Arrays.asList(def1, def2));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Arrays.asList("1", "ONE"));
        table.getRawRows().add(Arrays.asList("2", "TWO"));
        provider.addTable(table);

        table = new StagingTable();
        table.setId("table_input2");
        def1 = new StagingColumnDefinition();
        def1.setKey("input2");
        def1.setName("Input 2");
        def1.setType(ColumnType.INPUT);
        def2 = new StagingColumnDefinition();
        def2.setKey("result2");
        def2.setName("Result2");
        def2.setType(ColumnType.DESCRIPTION);
        table.setColumnDefinitions(Arrays.asList(def1, def2));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Arrays.asList("", "Blank"));
        table.getRawRows().add(Arrays.asList("A", "Letter A"));
        table.getRawRows().add(Arrays.asList("B", "Letter B"));
        provider.addTable(table);

        table = new StagingTable();
        table.setId("table_selection");
        def1 = new StagingColumnDefinition();
        def1.setKey("input1");
        def1.setName("Input 1");
        def1.setType(ColumnType.INPUT);
        table.setColumnDefinitions(Collections.singletonList(def1));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Collections.singletonList("*"));
        provider.addTable(table);

        table = new StagingTable();
        table.setId("primary_site");
        def1 = new StagingColumnDefinition();
        def1.setKey("site");
        def1.setName("Site");
        def1.setType(ColumnType.INPUT);
        table.setColumnDefinitions(Collections.singletonList(def1));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Collections.singletonList("C509"));
        provider.addTable(table);

        table = new StagingTable();
        table.setId("histology");
        def1 = new StagingColumnDefinition();
        def1.setKey("hist");
        def1.setName("Histology");
        def1.setType(ColumnType.INPUT);
        table.setColumnDefinitions(Collections.singletonList(def1));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Collections.singletonList("8000"));
        provider.addTable(table);

        table = new StagingTable();
        table.setId("table_year_dx");
        def1 = new StagingColumnDefinition();
        def1.setKey("year_dx");
        def1.setName("Year DX");
        def1.setType(ColumnType.INPUT);
        table.setColumnDefinitions(Collections.singletonList(def1));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Collections.singletonList("1900-2100"));
        provider.addTable(table);

        StagingSchema schema = new StagingSchema();
        schema.setId("schema_test");
        schema.setSchemaSelectionTable("table_selection");
        List<StagingSchemaInput> inputs = new ArrayList<>();
        inputs.add(new StagingSchemaInput("site", "Primary Site", "primary_site"));
        inputs.add(new StagingSchemaInput("hist", "Hist", "histology"));
        inputs.add(new StagingSchemaInput("year_dx", "Year DX", "table_year_dx"));
        inputs.add(new StagingSchemaInput("input1", "Input 1", "table_input1"));
        inputs.add(new StagingSchemaInput("input2", "Input 2", "table_input2"));
        schema.setInputs(inputs);

        provider.addSchema(schema);

        Staging staging = Staging.getInstance(provider);

        assertEquals("schema_test", staging.getSchema("schema_test").getId());

        // check case where required input field not supplied (i.e. no default); since there are is no workflow defined, this should
        // not cause an error

        StagingData data = new StagingData("C509", "8000");
        data.setInput("year_dx", "2018");
        data.setInput("input1", "1");

        staging.stage(data);
        assertEquals(Result.STAGED, data.getResult());

        // pass in blank for "input2"

        data = new StagingData("C509", "8000");
        data.setInput("year_dx", "2018");
        data.setInput("input1", "1");
        data.setInput("input2", "");

        staging.stage(data);
        assertEquals(Result.STAGED, data.getResult());

        // pass in null for "input2"

        data = new StagingData("C509", "8000");
        data.setInput("year_dx", "2018");
        data.setInput("input1", "1");
        data.setInput("input2", null);

        staging.stage(data);
        assertEquals(Result.STAGED, data.getResult());
    }

    @Test
    public void testNumericRangeTableMatch() {
        InMemoryDataProvider provider = new InMemoryDataProvider("test", "1.0");

        StagingTable table = new StagingTable();
        table.setId("psa");
        StagingColumnDefinition def1 = new StagingColumnDefinition();
        def1.setKey("psa");
        def1.setName("PSA Value");
        def1.setType(ColumnType.INPUT);
        StagingColumnDefinition def2 = new StagingColumnDefinition();
        def2.setKey("description");
        def2.setName("PSA Description");
        def2.setType(ColumnType.DESCRIPTION);
        table.setColumnDefinitions(Arrays.asList(def1, def2));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Arrays.asList("0.1", "0.1 or less nanograms/milliliter (ng/ml)"));
        table.getRawRows().add(Arrays.asList("0.2-999.9", "0.2 – 999.9 ng/ml"));
        provider.addTable(table);

        Staging staging = Staging.getInstance(provider);

        assertEquals(Integer.valueOf(0), staging.findMatchingTableRow("psa", "psa", "0.1"));
        assertEquals(Integer.valueOf(1), staging.findMatchingTableRow("psa", "psa", "0.2"));
        assertEquals(Integer.valueOf(1), staging.findMatchingTableRow("psa", "psa", "500"));
        assertEquals(Integer.valueOf(1), staging.findMatchingTableRow("psa", "psa", "500.99"));
        assertEquals(Integer.valueOf(1), staging.findMatchingTableRow("psa", "psa", "500.0001"));
        assertEquals(Integer.valueOf(1), staging.findMatchingTableRow("psa", "psa", "999.9"));
    }
}
