/*
 * Copyright (C) 2018 Information Management Services, Inc.
 */
package com.imsweb.staging.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import com.imsweb.staging.InMemoryDataProvider;
import com.imsweb.staging.Staging;
import com.imsweb.staging.entities.ColumnDefinition.ColumnType;
import com.imsweb.staging.entities.StagingData;
import com.imsweb.staging.entities.StagingData.Result;
import com.imsweb.staging.entities.impl.StagingColumnDefinition;
import com.imsweb.staging.entities.impl.StagingKeyMapping;
import com.imsweb.staging.entities.impl.StagingKeyValue;
import com.imsweb.staging.entities.impl.StagingMapping;
import com.imsweb.staging.entities.impl.StagingSchema;
import com.imsweb.staging.entities.impl.StagingSchemaInput;
import com.imsweb.staging.entities.impl.StagingSchemaOutput;
import com.imsweb.staging.entities.impl.StagingTable;
import com.imsweb.staging.entities.impl.StagingTablePath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchRuntimeException;

public class BasicStagingTest {

    @Test
    public void testBlankInputs() {
        InMemoryDataProvider provider = new InMemoryDataProvider("test", "1.0");

        StagingTable table = new StagingTable();
        table.setId("table_input1");
        table.setColumnDefinitions(Arrays.asList(new StagingColumnDefinition("table_input1", "INPUT1", ColumnType.INPUT), new StagingColumnDefinition("result1", "Result1", ColumnType.DESCRIPTION)));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Arrays.asList("1", "ONE"));
        table.getRawRows().add(Arrays.asList("2", "TWO"));
        provider.addTable(table);

        table = new StagingTable();
        table.setId("table_input2");
        table.setColumnDefinitions(Arrays.asList(new StagingColumnDefinition("input2", "INPUT2", ColumnType.INPUT), new StagingColumnDefinition("result2", "RESULT2", ColumnType.DESCRIPTION)));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Arrays.asList("", "Blank"));
        table.getRawRows().add(Arrays.asList("A", "Letter A"));
        table.getRawRows().add(Arrays.asList("B", "Letter B"));
        provider.addTable(table);

        table = new StagingTable();
        table.setId("table_selection");
        table.setColumnDefinitions(Collections.singletonList(new StagingColumnDefinition("input1", "INPUT1", ColumnType.INPUT)));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Collections.singletonList("*"));
        provider.addTable(table);

        table = new StagingTable();
        table.setId("primary_site");
        table.setColumnDefinitions(Collections.singletonList(new StagingColumnDefinition("site", "Site", ColumnType.INPUT)));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Collections.singletonList("C509"));
        provider.addTable(table);

        table = new StagingTable();
        table.setId("histology");
        table.setColumnDefinitions(Collections.singletonList(new StagingColumnDefinition("hist", "Histology", ColumnType.INPUT)));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Collections.singletonList("8000"));
        provider.addTable(table);

        table = new StagingTable();
        table.setId("table_year_dx");
        table.setColumnDefinitions(Collections.singletonList(new StagingColumnDefinition("year_dx", "Year DX", ColumnType.INPUT)));
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

        assertThat(staging.getSchema("schema_test").getId()).isEqualTo("schema_test");
        assertThat(staging.getTableIds()).hasSameElementsAs(Arrays.asList("table_input1", "table_input2", "table_selection", "primary_site", "histology", "table_year_dx"));

        // check case where required input field not supplied (i.e. no default); since there are is no workflow defined, this should
        // not cause an error

        StagingData data = new StagingData("C509", "8000");
        data.setInput("year_dx", "2018");
        data.setInput("input1", "1");

        staging.stage(data);
        assertThat(data.getResult()).isEqualTo(Result.STAGED);

        // pass in blank for "input2"

        data = new StagingData("C509", "8000");
        data.setInput("year_dx", "2018");
        data.setInput("input1", "1");
        data.setInput("input2", "");

        staging.stage(data);
        assertThat(data.getResult()).isEqualTo(Result.STAGED);

        // pass in null for "input2"

        data = new StagingData("C509", "8000");
        data.setInput("year_dx", "2018");
        data.setInput("input1", "1");
        data.setInput("input2", null);

        staging.stage(data);
        assertThat(data.getResult()).isEqualTo(Result.STAGED);
    }

    @Test
    public void testNumericRangeTableMatch() {
        InMemoryDataProvider provider = new InMemoryDataProvider("test", "1.0");

        StagingTable table = new StagingTable();
        table.setId("psa");
        table.setColumnDefinitions(Arrays.asList(new StagingColumnDefinition("psa", "PSA Value", ColumnType.INPUT), new StagingColumnDefinition("description", "Description", ColumnType.DESCRIPTION)));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Arrays.asList("0.1", "0.1 or less nanograms/milliliter (ng/ml)"));
        table.getRawRows().add(Arrays.asList("0.2-999.9", "0.2 – 999.9 ng/ml"));
        provider.addTable(table);

        Staging staging = Staging.getInstance(provider);

        assertThat(staging.findMatchingTableRow("psa", "psa", "0.1")).isZero();
        assertThat(staging.findMatchingTableRow("psa", "psa", "0.2")).isEqualTo(1);
        assertThat(staging.findMatchingTableRow("psa", "psa", "500")).isEqualTo(1);
        assertThat(staging.findMatchingTableRow("psa", "psa", "500.99")).isEqualTo(1);
        assertThat(staging.findMatchingTableRow("psa", "psa", "500.0001")).isEqualTo(1);
        assertThat(staging.findMatchingTableRow("psa", "psa", "999.9")).isEqualTo(1);
    }

    @Test
    public void testGetInputsWithContext() {
        InMemoryDataProvider provider = new InMemoryDataProvider("test", "1.0");

        StagingTable table = new StagingTable();
        table.setId("table_input1");
        table.setColumnDefinitions(Collections.singletonList(new StagingColumnDefinition("input1", "Input 1", ColumnType.INPUT)));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Collections.singletonList("1"));
        table.getRawRows().add(Collections.singletonList("2"));
        provider.addTable(table);

        table = new StagingTable();
        table.setId("table_input2");
        table.setColumnDefinitions(Collections.singletonList(new StagingColumnDefinition("input1", "Input 2", ColumnType.INPUT)));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Collections.singletonList(""));
        table.getRawRows().add(Collections.singletonList("A"));
        table.getRawRows().add(Collections.singletonList("B"));
        provider.addTable(table);

        table = new StagingTable();
        table.setId("table_selection");
        table.setColumnDefinitions(Collections.singletonList(new StagingColumnDefinition("input1", "Input 1", ColumnType.INPUT)));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Collections.singletonList("*"));
        provider.addTable(table);

        table = new StagingTable();
        table.setId("table_mapping");
        table.setColumnDefinitions(Arrays.asList(
                new StagingColumnDefinition("input1", "Input 1", ColumnType.INPUT),
                new StagingColumnDefinition("input2", "Input 2", ColumnType.INPUT),
                new StagingColumnDefinition("mapped_field", "Temp value", ColumnType.INPUT),
                new StagingColumnDefinition("final_output", "Output", ColumnType.ENDPOINT)));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Arrays.asList("*", "*", "*", "VALUE:ABC"));
        provider.addTable(table);

        table = new StagingTable();
        table.setId("primary_site");
        table.setColumnDefinitions(Collections.singletonList(new StagingColumnDefinition("site", "Site", ColumnType.INPUT)));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Collections.singletonList("C509"));
        provider.addTable(table);

        table = new StagingTable();
        table.setId("histology");
        table.setColumnDefinitions(Collections.singletonList(new StagingColumnDefinition("hist", "Histology", ColumnType.INPUT)));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Collections.singletonList("8000"));
        provider.addTable(table);

        table = new StagingTable();
        table.setId("table_year_dx");
        table.setColumnDefinitions(Collections.singletonList(new StagingColumnDefinition("year_dx", "Year DX", ColumnType.INPUT)));
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
        inputs.add(new StagingSchemaInput("final_output", "Final Output"));  // field is input and output
        schema.setInputs(inputs);
        schema.setOutputs(Collections.singletonList(new StagingSchemaOutput("final_output", "Final Output")));

        StagingMapping mapping = new StagingMapping();
        mapping.setId("m1");
        mapping.setInitialContext(new HashSet<>(Collections.singletonList(new StagingKeyValue("tmp_field", null))));
        StagingTablePath path = new StagingTablePath();
        path.setId("table_mapping");
        path.setInputMapping(new HashSet<>(Collections.singletonList(new StagingKeyMapping("tmp_field", "mapped_field"))));
        path.setInputs(new HashSet<>(Arrays.asList("input1", "input2", "tmp_field")));
        path.setOutputs(new HashSet<>(Collections.singletonList("final_output")));
        mapping.setTablePaths(Collections.singletonList(path));
        schema.setMappings(Collections.singletonList(mapping));

        provider.addSchema(schema);

        Staging staging = Staging.getInstance(provider);

        // should only return the "real" inputs and not the temp field set in initial context
        assertThat(staging.getInputs(staging.getSchema("schema_test"))).isEqualTo(new HashSet<>(Arrays.asList("input1", "input2")));

        StagingData data = new StagingData("C509", "8000");
        data.setInput("year_dx", "2018");
        data.setInput("input1", "1");
        data.setInput("input2", "B");

        staging.stage(data);
        assertThat(data.getResult()).isEqualTo(Result.STAGED);
    }

    @Test
    public void testInvalidContext() {
        InMemoryDataProvider provider = new InMemoryDataProvider("test", "1.0");

        StagingTable table = new StagingTable();
        table.setId("table_input1");
        table.setColumnDefinitions(Collections.singletonList(new StagingColumnDefinition("input1", "Input 1", ColumnType.INPUT)));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Collections.singletonList("1"));
        table.getRawRows().add(Collections.singletonList("2"));
        provider.addTable(table);

        table = new StagingTable();
        table.setId("table_selection");
        table.setColumnDefinitions(Collections.singletonList(new StagingColumnDefinition("input1", "Input 1", ColumnType.INPUT)));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Collections.singletonList("*"));
        provider.addTable(table);

        table = new StagingTable();
        table.setId("table_mapping");
        table.setColumnDefinitions(Arrays.asList(new StagingColumnDefinition("input1", "Input 1", ColumnType.INPUT),
                new StagingColumnDefinition("final_output", "Output", ColumnType.ENDPOINT)));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Arrays.asList("*", "VALUE:ABC"));
        provider.addTable(table);

        StagingSchema schema = new StagingSchema();
        schema.setId("schema_test");
        schema.setSchemaSelectionTable("table_selection");
        List<StagingSchemaInput> inputs = new ArrayList<>();
        inputs.add(new StagingSchemaInput("input1", "Input 1", "table_input1"));
        schema.setInputs(inputs);
        schema.setOutputs(Collections.singletonList(new StagingSchemaOutput("final_output", "Final Output")));

        StagingMapping mapping = new StagingMapping();
        mapping.setId("m1");
        mapping.setInitialContext(new HashSet<>(Collections.singletonList(new StagingKeyValue("input1", "XXX"))));
        StagingTablePath path = new StagingTablePath();
        path.setId("table_mapping");
        path.setInputs(new HashSet<>(Collections.singletonList("input1")));
        path.setOutputs(new HashSet<>(Collections.singletonList("final_output")));
        mapping.setTablePaths(Collections.singletonList(path));
        schema.setMappings(Collections.singletonList(mapping));

        // initializing should throw an exception since the initial context maps a real input variable
        Throwable thrown = catchRuntimeException(() -> provider.addSchema(schema));

        assertThat(thrown).isInstanceOf(IllegalStateException.class).hasNoCause().hasMessageContaining("not allowed since it is also defined as an input");
    }
}
