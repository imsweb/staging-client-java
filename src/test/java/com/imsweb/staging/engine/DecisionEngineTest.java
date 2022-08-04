/*

 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.imsweb.staging.InMemoryDataProvider;
import com.imsweb.staging.entities.ColumnDefinition.ColumnType;
import com.imsweb.staging.entities.DataProvider;
import com.imsweb.staging.entities.Endpoint;
import com.imsweb.staging.entities.Endpoint.EndpointType;
import com.imsweb.staging.entities.Error;
import com.imsweb.staging.entities.Mapping;
import com.imsweb.staging.entities.Range;
import com.imsweb.staging.entities.Result;
import com.imsweb.staging.entities.Result.Type;
import com.imsweb.staging.entities.Schema;
import com.imsweb.staging.entities.Table;
import com.imsweb.staging.entities.impl.StagingColumnDefinition;
import com.imsweb.staging.entities.impl.StagingMapping;
import com.imsweb.staging.entities.impl.StagingRange;
import com.imsweb.staging.entities.impl.StagingSchema;
import com.imsweb.staging.entities.impl.StagingSchemaInput;
import com.imsweb.staging.entities.impl.StagingSchemaOutput;
import com.imsweb.staging.entities.impl.StagingTable;
import com.imsweb.staging.entities.impl.StagingTablePath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Test class for DecisionEngine
 */
public class DecisionEngineTest {

    private static DecisionEngine _ENGINE;

    @BeforeAll
    public static void init() {
        InMemoryDataProvider provider = new InMemoryDataProvider("test", "1.0");

        StagingTable table = new StagingTable("table_lookup_sample");
        table.addColumnDefinition("b", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addRawRow("00-09", "First 10");
        table.addRawRow("10-19", "Second 10");
        table.addRawRow("20-29", "Third 10");
        table.addRawRow("30", "Thirty");
        table.addRawRow("99", "Ninety-nine");
        table.addRawRow("", "blank");
        provider.addTable(table);

        table = new StagingTable("table_jump_sample");
        table.addColumnDefinition("c", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("A", "Line1", "VALUE:A");
        table.addRawRow("B", "Line2", "VALUE:B");
        table.addRawRow("C", "Line3", "ERROR:Bad C value");
        table.addRawRow("Z", "Line4", "STOP");
        provider.addTable(table);

        table = new StagingTable("table_sample_first");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("b", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("1,2,5-9", "00-12", "Line1", "VALUE:LINE1");
        table.addRawRow("3,4", "17,19,22", "Line2", "JUMP:table_jump_sample");
        table.addRawRow("0", "23-30", "Line3", "VALUE:LINE3");
        table.addRawRow("5", "20", "Line4", "JUMP:table_jump_sample");
        table.addRawRow("*", "55", "Line5", "VALUE:LINE5");
        table.addRawRow("8", "99", "Line6", "ERROR:");
        table.addRawRow("9", "99", "Line6", "ERROR:999");
        provider.addTable(table);

        table = new StagingTable("table_multiple_inputs");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("b", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("r1", ColumnType.ENDPOINT);
        table.addColumnDefinition("r2", ColumnType.ENDPOINT);
        table.addColumnDefinition("r3", ColumnType.ENDPOINT);
        table.addRawRow("1,2,5-9", "00-12", "Line1", "VALUE:1_LINE1", "VALUE:2_LINE1", "VALUE:3_LINE1");
        table.addRawRow("0", "23-30", "Line2", "VALUE:1_LINE2", "ERROR:2_LINE2", "VALUE:3_LINE2");
        table.addRawRow("5", "20", "Line3", "JUMP:table_jump_sample", "VALUE:2_LINE3", "VALUE:3_LINE3");
        table.addRawRow("4", "44", "Line5", "JUMP:table_jump_sample", "VALUE:2_LINE5", "JUMP:table_jump_sample");
        table.addRawRow("9", "99", "Line4", "ERROR:1_LINE4", "ERROR:2_LINE4", "ERROR:3_LINE4");
        provider.addTable(table);

        table = new StagingTable("table_sample_second");
        table.addColumnDefinition("e", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("shared_result", ColumnType.ENDPOINT);
        table.addRawRow("X", "Line1", "VALUE:LINE1");
        table.addRawRow("Y", "Line2", "VALUE:LINE1");
        table.addRawRow("Z", "Line3", "VALUE:LINE3");
        provider.addTable(table);

        table = new StagingTable("table_recursion");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("1,2,5-9", "Line1", "VALUE:LINE1");
        table.addRawRow("3,4", "Line2", "JUMP:table_recursion");
        provider.addTable(table);

        table = new StagingTable("table_inclusion1");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addRawRow("0-3,5");
        provider.addTable(table);

        table = new StagingTable("table_inclusion2");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addRawRow("4,6-8");
        provider.addTable(table);

        table = new StagingTable("table_inclusion3");
        table.addColumnDefinition("not_in_input_list", ColumnType.INPUT);
        table.addRawRow("4,6-8");
        provider.addTable(table);

        table = new StagingTable("table_exclusion1");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addRawRow("1");
        provider.addTable(table);

        table = new StagingTable("table_part1");
        table.addColumnDefinition("val", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("1", "1", "VALUE:1");
        table.addRawRow("2", "2", "VALUE:2");
        table.addRawRow("3", "3", "VALUE:3");
        provider.addTable(table);

        table = new StagingTable("table_part2");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("special", ColumnType.ENDPOINT);
        table.addRawRow("*", "", "VALUE:SUCCESS");
        provider.addTable(table);

        table = new StagingTable("table_create_intermediate");
        table.addColumnDefinition("main_input", ColumnType.INPUT);
        table.addColumnDefinition("intermediate_output", ColumnType.ENDPOINT);
        table.addRawRow("1", "VALUE:1");
        provider.addTable(table);

        table = new StagingTable("table_use_intermediate");
        table.addColumnDefinition("intermediate_output", ColumnType.INPUT);
        table.addColumnDefinition("final_output", ColumnType.ENDPOINT);
        table.addRawRow("1", "VALUE:1");
        provider.addTable(table);

        table = new StagingTable("table_blank_matching");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("b", ColumnType.INPUT);
        table.addColumnDefinition("b", ColumnType.ENDPOINT);
        table.addColumnDefinition("c", ColumnType.ENDPOINT);
        table.addRawRow("*", "", "VALUE:LINE1", "VALUE:LINE1");
        table.addRawRow("NA", "*", "VALUE:LINE2", "VALUE:LINE2");
        table.addRawRow("", "*", "VALUE:LINE3", "VALUE:LINE3");
        table.addRawRow("*", "*", "MATCH", "MATCH");
        provider.addTable(table);

        table = new StagingTable("table_multiple_input");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("b", ColumnType.INPUT);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("1", "0", "VALUE:LINE1");
        table.addRawRow("0", "1", "VALUE:LINE2");
        table.addRawRow("1", "1", "VALUE:LINE3");
        provider.addTable(table);

        table = new StagingTable();
        table.setId("table_selection");
        table.setColumnDefinitions(Collections.singletonList(new StagingColumnDefinition("a", "a", ColumnType.INPUT)));
        table.setRawRows(new ArrayList<>());
        table.getRawRows().add(Collections.singletonList("*"));
        provider.addTable(table);

        StagingSchema schema = new StagingSchema("starting_sample");
        schema.setSchemaSelectionTable("table_selection");
        schema.setOnInvalidInput(Schema.StagingInputErrorHandler.FAIL);
        schema.addInput("a");
        StagingSchemaInput input = new StagingSchemaInput("b", "b", "table_lookup_sample");
        schema.addInput(input);
        schema.addInput("c");
        schema.addInitialContext("d", "HARD-CODE");
        StagingMapping mapping = new StagingMapping("m1");
        mapping.setTablePaths(Arrays.asList(new StagingTablePath("table_sample_first"), new StagingTablePath("table_sample_second")));
        schema.addMapping(mapping);
        provider.addSchema(schema);

        schema = new StagingSchema("starting_min");
        schema.setSchemaSelectionTable("table_selection");
        schema.addInitialContext("foo", "bar");
        provider.addSchema(schema);

        schema = new StagingSchema("starting_multiple_endpoints");
        schema.setSchemaSelectionTable("table_selection");
        schema.addInput("a");
        schema.addInput("b");
        schema.addInput("c");
        schema.addMapping(new StagingMapping("m1", Collections.singletonList(new StagingTablePath("table_multiple_inputs"))));
        provider.addSchema(schema);

        schema = new StagingSchema("starting_recursion");
        schema.setSchemaSelectionTable("table_selection");
        schema.addInput("a");
        schema.addMapping(new StagingMapping("m1", Collections.singletonList(new StagingTablePath("table_recursion"))));
        provider.addSchema(schema);

        schema = new StagingSchema("starting_inclusions");
        schema.setSchemaSelectionTable("table_selection");
        schema.addInput("a");
        schema.addInput("b");
        schema.addInput("c");
        mapping = new StagingMapping("m1");
        mapping.setInclusionTables(Collections.singletonList(new StagingTablePath("table_inclusion1")));
        StagingTablePath path = new StagingTablePath("table_part1");
        path.addInputMapping("b", "val");
        mapping.addTablePath(path);
        schema.addMapping(mapping);
        mapping = new StagingMapping("m2");
        mapping.setInclusionTables(Collections.singletonList(new StagingTablePath("table_inclusion2")));
        path = new StagingTablePath("table_part1");
        path.addInputMapping("c", "val");
        mapping.addTablePath(path);
        schema.addMapping(mapping);
        mapping = new StagingMapping("m3");
        mapping.setExclusionTables(Collections.singletonList(new StagingTablePath("table_exclusion1")));
        path = new StagingTablePath("table_part2");
        mapping.addTablePath(path);
        schema.addMapping(mapping);
        provider.addSchema(schema);

        schema = new StagingSchema("starting_intermediate_values");
        schema.setSchemaSelectionTable("table_selection");
        schema.addInput("main_input");
        mapping = new StagingMapping("m1");
        mapping.addTablePath(new StagingTablePath("table_create_intermediate"));
        mapping.addTablePath(new StagingTablePath("table_use_intermediate"));
        schema.addMapping(mapping);
        provider.addSchema(schema);

        schema = new StagingSchema("starting_inclusions_extra_inputs");
        schema.setSchemaSelectionTable("table_selection");
        schema.addInput("a");
        schema.addInput("b");
        schema.addInput("c");
        mapping = new StagingMapping("m1");
        mapping.setInclusionTables(Collections.singletonList(new StagingTablePath("table_inclusion3")));
        path = new StagingTablePath("table_part1");
        path.addInputMapping("b", "val");
        path.addOutputMapping("result", "mapped_result");
        mapping.addTablePath(path);
        schema.addMapping(mapping);
        provider.addSchema(schema);

        schema = new StagingSchema("starting_blank");
        schema.setSchemaSelectionTable("table_selection");
        schema.addInput("a");
        schema.addInput("b");
        mapping = new StagingMapping("m1");
        path = new StagingTablePath("table_blank_matching");
        path.addInputMapping("x", "a");
        path.addInputMapping("y", "b");
        path.addOutputMapping("b", "y");
        path.addOutputMapping("c", "z");
        mapping.addTablePath(path);
        schema.addMapping(mapping);
        provider.addSchema(schema);

        schema = new StagingSchema("starting_double_input");
        schema.setSchemaSelectionTable("table_selection");
        schema.addInput("x");
        mapping = new StagingMapping("m1");
        path = new StagingTablePath("table_multiple_input");
        path.addInputMapping("x", "a");
        path.addInputMapping("x", "b");
        mapping.addTablePath(path);
        schema.addMapping(mapping);
        provider.addSchema(schema);

        schema = new StagingSchema("starting_double_output");
        schema.setSchemaSelectionTable("table_selection");
        schema.addInput("a");
        schema.addInput("b");
        mapping = new StagingMapping("m1");
        path = new StagingTablePath("table_sample_first");
        path.addOutputMapping("result", "output1");
        path.addOutputMapping("result", "output2");
        mapping.addTablePath(path);
        schema.addMapping(mapping);
        provider.addSchema(schema);

        _ENGINE = new DecisionEngine(provider);
    }

    @Test
    void testMatch() {
        List<Range> range = new ArrayList<>();
        range.add(new StagingRange("1", "1"));
        range.add(new StagingRange("4", "4"));
        range.add(new StagingRange("9", "9"));
        Assertions.assertTrue(DecisionEngine.testMatch(range, "9", new HashMap<>()));
        Assertions.assertFalse(DecisionEngine.testMatch(range, "7", new HashMap<>()));

        range = new ArrayList<>();
        range.add(new StagingRange("11", "54"));
        range.add(new StagingRange("99", "99"));
        Assertions.assertTrue(DecisionEngine.testMatch(range, "23", new HashMap<>()));
    }

    @Test
    void testEmptyTable() {
        InMemoryDataProvider provider = new InMemoryDataProvider("Test", "1.0");
        StagingTable table = new StagingTable("basic_test_table");
        table.addColumnDefinition("size", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("size_result", ColumnType.ENDPOINT);
        provider.addTable(table);

        Assertions.assertTrue(provider.getTable("basic_test_table").getRawRows().isEmpty());
        Assertions.assertTrue(provider.getTable("basic_test_table").getTableRows().isEmpty());
    }

    @Test
    void testMatchTable() {
        InMemoryDataProvider provider = new InMemoryDataProvider("Test", "1.0");
        StagingTable table = new StagingTable("basic_test_table");
        table.addColumnDefinition("size", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("size_result", ColumnType.ENDPOINT);
        table.addRawRow("001-003,004,006", "Little", "JUMP:some_crazy_table");
        table.addRawRow("007-019,022", "Medium", "VALUE:medium_stuff");
        table.addRawRow("076-099,999", "HUGE!", "ERROR:Get that huge stuff out of here!");
        table.addRawRow("030", "Match endpoint", "MATCH");
        provider.addTable(table);

        Table matchTable = provider.getTable("basic_test_table");
        Assertions.assertNotNull(matchTable);

        // create context of input fields
        Map<String, String> input = new HashMap<>();

        // first try it with missing input
        Assertions.assertNull(DecisionEngine.matchTable(matchTable, input));

        input.put("size", "003");
        assertThat(DecisionEngine.matchTable(matchTable, input))
                .extracting(Endpoint::getType, Endpoint::getValue, Endpoint::getResultKey)
                .containsExactly(tuple(EndpointType.JUMP, "some_crazy_table", "size_result"));

        input.put("size", "014");
        List<? extends Endpoint> results = DecisionEngine.matchTable(matchTable, input);
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(EndpointType.VALUE, results.get(0).getType());
        Assertions.assertEquals("medium_stuff", results.get(0).getValue());
        Assertions.assertEquals("size_result", results.get(0).getResultKey());

        input.put("size", "086");
        assertThat(DecisionEngine.matchTable(matchTable, input))
                .extracting(Endpoint::getType, Endpoint::getValue, Endpoint::getResultKey)
                .containsExactly(tuple(EndpointType.ERROR, "Get that huge stuff out of here!", "size_result"));

        // try with a value not in the table
        input.put("size", "021");
        Assertions.assertNull(DecisionEngine.matchTable(matchTable, input));
    }

    @Test
    void testMatchTableWithBlankOrMissingInput() {
        InMemoryDataProvider provider = new InMemoryDataProvider("Test", "1.0");
        StagingTable table = new StagingTable("basic_test_table");
        table.addColumnDefinition("size", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("", "BLANK", "MATCH");
        table.addRawRow("1", "ONE", "MATCH");
        table.addRawRow("2", "TWO", "MATCH");
        provider.addTable(table);

        Table matchTable = provider.getTable("basic_test_table");
        Assertions.assertNotNull(matchTable);

        // create context of input fields
        Map<String, String> input = new HashMap<>();

        // first try it with missing input (null should match just like blank)
        Assertions.assertNotNull(DecisionEngine.matchTable(matchTable, input));

        // now add blank input
        input.put("size", "");
        Assertions.assertNotNull(DecisionEngine.matchTable(matchTable, input));

        // test matching on multiple mising values
        table = new StagingTable("basic_test_table_multi");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("b", ColumnType.INPUT);
        table.addColumnDefinition("c", ColumnType.INPUT);
        table.addColumnDefinition("d", ColumnType.INPUT);
        table.addColumnDefinition("e", ColumnType.INPUT);
        table.addRawRow("1", "", "", "", "");
        table.addRawRow("2", "", "", "", "");
        provider.addTable(table);

        matchTable = provider.getTable("basic_test_table_multi");
        Assertions.assertNotNull(matchTable);

        // first try it with missing input (null should match just like blank)
        Assertions.assertNull(DecisionEngine.matchTable(matchTable, new HashMap<>()));

        input.put("a", "2");
        Assertions.assertNotNull(DecisionEngine.matchTable(matchTable, input));
    }

    @Test
    void testMatchOnSpecificKeys() {
        InMemoryDataProvider provider = new InMemoryDataProvider("Test", "1.0");
        StagingTable table = new StagingTable("basic_test_table_keytest");
        table.addColumnDefinition("key1", ColumnType.INPUT);
        table.addColumnDefinition("key2", ColumnType.INPUT);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("001-099", "1-3", "MATCH:LINE1");
        table.addRawRow("001-099", "5-9", "MATCH:LINE2");
        table.addRawRow("100,106", "1", "MATCH:LINE3");
        provider.addTable(table);

        Table matchTable = provider.getTable("basic_test_table_keytest");
        Assertions.assertNotNull(matchTable);

        // create context of input fields
        Map<String, String> input = new HashMap<>();

        // first try it with missing input
        Assertions.assertNull(DecisionEngine.matchTable(matchTable, input));

        // if searching all keys and only supplying key1, not match will be found
        input.put("key1", "050");
        Assertions.assertNull(DecisionEngine.matchTable(matchTable, input));

        // specify to only match on key1, there should be a match to the first line
        assertThat(DecisionEngine.matchTable(matchTable, input, new HashSet<>(Collections.singletonList("key1"))))
                .extracting(Endpoint::getType, Endpoint::getValue, Endpoint::getResultKey)
                .containsExactly(tuple(EndpointType.MATCH, "LINE1", "result"));

        // add key2 to the input map and there should be a match
        input.put("key2", "7");
        assertThat(DecisionEngine.matchTable(matchTable, input))
                .extracting(Endpoint::getType, Endpoint::getValue, Endpoint::getResultKey)
                .containsExactly(tuple(EndpointType.MATCH, "LINE2", "result"));

        // if searching on key1 only, even though key2 was supplied should still match to first line
        assertThat(DecisionEngine.matchTable(matchTable, input, new HashSet<>(Collections.singletonList("key1"))))
                .extracting(Endpoint::getType, Endpoint::getValue, Endpoint::getResultKey)
                .containsExactly(tuple(EndpointType.MATCH, "LINE1", "result"));

        // supply an empty set of keys (the same meaning as not passing any keys
        assertThat(DecisionEngine.matchTable(matchTable, input, new HashSet<>()))
                .extracting(Endpoint::getType, Endpoint::getValue, Endpoint::getResultKey)
                .containsExactly(tuple(EndpointType.MATCH, "LINE1", "result"));

        // supply an invalid key.  I think this should find nothing, but for the moment finds a match to the first row since none of the cells were
        // compared to.  It is the same as matching to a table with no INPUTS which would currently find a match to the first row.
        assertThat(DecisionEngine.matchTable(matchTable, input, new HashSet<>(Collections.singletonList("bad_key"))))
                .extracting(Endpoint::getType, Endpoint::getValue, Endpoint::getResultKey)
                .containsExactly(tuple(EndpointType.MATCH, "LINE1", "result"));
    }

    @Test
    void testMatchMissingVsAll() {
        InMemoryDataProvider provider = new InMemoryDataProvider("Test", "1.0");
        StagingTable table = new StagingTable("testMissingAndAll");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("", "VALUE:missing");
        table.addRawRow("*", "VALUE:all");
        provider.addTable(table);

        Table tableMissing = provider.getTable("testMissingAndAll");

        List<? extends Endpoint> endpoints;
        Map<String, String> input = new HashMap<>();
        input.put("a", "");
        endpoints = DecisionEngine.matchTable(tableMissing, input);
        Assertions.assertEquals(1, endpoints.size());
        Assertions.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        Assertions.assertEquals("missing", endpoints.get(0).getValue());

        input.put("a", "1");
        endpoints = DecisionEngine.matchTable(tableMissing, input);
        Assertions.assertEquals(1, endpoints.size());
        Assertions.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        Assertions.assertEquals("all", endpoints.get(0).getValue());
    }

    @Test
    void testValueKeyReferences() {
        InMemoryDataProvider provider = new InMemoryDataProvider("Test", "1.0");
        StagingTable table = new StagingTable("table_key_references");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("b", ColumnType.INPUT);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("1", "*", "VALUE:b");
        table.addRawRow("2", "*", "VALUE:{b}");
        table.addRawRow("3", "*", "VALUE:{{b");
        table.addRawRow("4", "*", "VALUE:b}}");
        table.addRawRow("10", "*", "VALUE:{{b}}");
        table.addRawRow("11", "*", "VALUE:{{bad_key}}");
        provider.addTable(table);
        StagingSchema schema = new StagingSchema("alg_key_references");
        schema.setSchemaSelectionTable("table_selection");
        schema.addInput("a");
        schema.addInput("b");
        schema.addMapping(new StagingMapping("m1", Collections.singletonList(new StagingTablePath("table_key_references"))));
        provider.addSchema(schema);
        DecisionEngine engine = new DecisionEngine(provider);

        Map<String, String> input = new HashMap<>();
        input.put("a", "1");
        input.put("b", "B VALUE");
        Assertions.assertFalse(engine.process("alg_key_references", input).hasErrors());
        Assertions.assertEquals("b", input.get("result"));

        input.put("a", "2");
        Assertions.assertFalse(engine.process("alg_key_references", input).hasErrors());
        Assertions.assertEquals("{b}", input.get("result"));

        input.put("a", "3");
        Assertions.assertFalse(engine.process("alg_key_references", input).hasErrors());
        Assertions.assertEquals("{{b", input.get("result"));

        input.put("a", "4");
        Assertions.assertFalse(engine.process("alg_key_references", input).hasErrors());
        Assertions.assertEquals("b}}", input.get("result"));

        input.put("a", "10");
        Assertions.assertFalse(engine.process("alg_key_references", input).hasErrors());
        Assertions.assertEquals("B VALUE", input.get("result"));

        input.put("a", "11");
        Assertions.assertFalse(engine.process("alg_key_references", input).hasErrors());
        Assertions.assertEquals("", input.get("result"));
    }

    @Test
    void testMatchTableMissingCell() {
        InMemoryDataProvider provider = new InMemoryDataProvider("Test", "1.0");
        StagingTable table = new StagingTable("table_sample_first");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("b", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("1,2,5-9", "00-12", "Line1", "VALUE:LINE1");
        table.addRawRow("3,4", "17,19,22", "Line2", "JUMP:table_jump_sample");
        table.addRawRow("0", "23-30", "Line3", "VALUE:LINE3");
        table.addRawRow("5", "20", "Line4", "JUMP:table_jump_sample");
        table.addRawRow("*", "55", "Line5", "VALUE:LINE5");
        table.addRawRow("9", "99", "Line6", "ERROR:999");
        provider.addTable(table);

        Table tableSample = provider.getTable("table_sample_first");
        Assertions.assertNotNull(tableSample);

        // first test with no "a"
        Map<String, String> input = new HashMap<>();
        input.put("b", "55");
        List<? extends Endpoint> endpoints = DecisionEngine.matchTable(tableSample, input);
        Assertions.assertEquals(1, endpoints.size());
        Assertions.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        Assertions.assertEquals("LINE5", endpoints.get(0).getValue());

        // then test with a random "a"
        input.put("a", "982");
        endpoints = DecisionEngine.matchTable(tableSample, input);
        Assertions.assertEquals(1, endpoints.size());
        Assertions.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        Assertions.assertEquals("LINE5", endpoints.get(0).getValue());
    }

    @Test
    void testBlankMatching() {
        InMemoryDataProvider provider = new InMemoryDataProvider("Test", "1.0");
        StagingTable table = new StagingTable("table_blank_matching");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("b", ColumnType.INPUT);
        table.addColumnDefinition("b", ColumnType.ENDPOINT);
        table.addColumnDefinition("c", ColumnType.ENDPOINT);
        table.addRawRow("*", "", "VALUE:LINE1", "VALUE:LINE1");
        table.addRawRow("NA", "*", "VALUE:LINE2", "VALUE:LINE2");
        table.addRawRow("", "*", "VALUE:LINE3", "VALUE:LINE3");
        table.addRawRow("*", "*", "MATCH", "MATCH");
        provider.addTable(table);

        Map<String, String> input = new HashMap<>();
        input.put("a", "X");
        input.put("b", "");
        List<? extends Endpoint> endpoints = DecisionEngine.matchTable(table, input);
        Assertions.assertEquals(2, endpoints.size());
        Assertions.assertEquals("LINE1", endpoints.get(0).getValue());
        Assertions.assertEquals("LINE1", endpoints.get(1).getValue());

        input.clear();
        input.put("a", "NA");
        input.put("b", "99");
        endpoints = DecisionEngine.matchTable(table, input);
        Assertions.assertEquals(2, endpoints.size());
        Assertions.assertEquals("LINE2", endpoints.get(0).getValue());
        Assertions.assertEquals("LINE2", endpoints.get(1).getValue());
    }

    @Test
    void testLookupTable() {
        InMemoryDataProvider provider = new InMemoryDataProvider("Test", "1.0");
        StagingTable table = new StagingTable("site_table");
        table.addColumnDefinition("site", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addRawRow("C000", "External upper lip");
        table.addRawRow("C001", "External lower lip");
        table.addRawRow("C002", "External lip, NOS");
        table.addRawRow("C003", "Inner aspect, upper lip");
        table.addRawRow("C004", "Inner aspect, lower lip");
        table.addRawRow("C005", "Inner aspect of lip, NOS");
        table.addRawRow("C006", "Commissure of lip");
        table.addRawRow("C008", "Overlapping lesion of lip");
        table.addRawRow("C009", "Lip, NOS");
        table.addRawRow("C809", "Unknown primary site");
        provider.addTable(table);

        Table siteTable = provider.getTable("site_table");
        Assertions.assertNotNull(siteTable);

        Map<String, String> input = new HashMap<>();
        input.put("site", "C809");

        // a lookup table in this case has no ENDPOINT column.  In those cases, an endpoint type of MATCH should be returned
        List<? extends Endpoint> endpoints = DecisionEngine.matchTable(siteTable, input);
        Assertions.assertEquals(0, endpoints.size());
    }

    @Test
    void testAllValuesMatching() {
        InMemoryDataProvider provider = new InMemoryDataProvider("Test", "1.0");
        StagingTable table = new StagingTable("all_values_test");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("b", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("1", "1", "", "VALUE:RESULT0");
        table.addRawRow("1", "2", "", "VALUE:RESULT1");
        table.addRawRow("1", "3", "", "VALUE:RESULT2");
        table.addRawRow("2", "1", "", "VALUE:RESULT3");
        table.addRawRow("2", "2", "", "VALUE:RESULT4");
        table.addRawRow("2", "3", "", "VALUE:RESULT5");
        table.addRawRow("3", "*", "", "VALUE:3A,ANY B");
        table.addRawRow("*", "4", "", "VALUE:ANY A,4B");
        table.addRawRow("*", "*", "", "VALUE:CATCHALL");
        provider.addTable(table);

        Table allValuesTable = provider.getTable("all_values_test");
        Assertions.assertNotNull(table);

        Map<String, String> input = new HashMap<>();
        input.put("a", "1");
        input.put("b", "3");
        List<? extends Endpoint> endpoints = DecisionEngine.matchTable(allValuesTable, input);
        Assertions.assertEquals(1, endpoints.size());
        Assertions.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        Assertions.assertEquals("RESULT2", endpoints.get(0).getValue());

        input = new HashMap<>();
        input.put("a", "3");
        endpoints = DecisionEngine.matchTable(allValuesTable, input);
        Assertions.assertEquals(1, endpoints.size());
        Assertions.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        Assertions.assertEquals("3A,ANY B", endpoints.get(0).getValue());

        input = new HashMap<>();
        input.put("a", "3");
        input.put("b", "9");
        endpoints = DecisionEngine.matchTable(allValuesTable, input);
        Assertions.assertEquals(1, endpoints.size());
        Assertions.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        Assertions.assertEquals("3A,ANY B", endpoints.get(0).getValue());

        input = new HashMap<>();
        input.put("a", "6");
        input.put("b", "4");
        endpoints = DecisionEngine.matchTable(allValuesTable, input);
        Assertions.assertEquals(1, endpoints.size());
        Assertions.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        Assertions.assertEquals("ANY A,4B", endpoints.get(0).getValue());

        input = new HashMap<>();
        Assertions.assertEquals(1, endpoints.size());
        endpoints = DecisionEngine.matchTable(allValuesTable, input);
        Assertions.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        Assertions.assertEquals("CATCHALL", endpoints.get(0).getValue());
    }

    @Test
    void testMinimumAlgorithm() {
        Schema minSchema = _ENGINE.getProvider().getSchema("starting_min");
        Assertions.assertNotNull(minSchema);
        Assertions.assertEquals("starting_min", minSchema.getId());
        Assertions.assertNotNull(minSchema.getInitialContext());

        Map<String, String> input = new HashMap<>();
        Result result = _ENGINE.process(minSchema, input);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("bar", input.get("foo"));
    }

    @Test
    void testAlgorithm() {
        Schema starting = _ENGINE.getProvider().getSchema("starting_sample");
        Assertions.assertNotNull(starting);
        Assertions.assertEquals("starting_sample", starting.getId());
        Assertions.assertNotNull(starting.getInitialContext());
    }

    @Test
    void testMissingParameters() {
        Map<String, String> input = new HashMap<>();
        Result result = _ENGINE.process("starting_sample", input);

        Assertions.assertTrue(result.hasErrors());

        // even though there were no inputs, both tables were still processed
        Assertions.assertEquals(2, result.getPath().size());
        Assertions.assertEquals("m1.table_sample_first", result.getPath().get(0));
        Assertions.assertEquals("m1.table_sample_second", result.getPath().get(1));
    }

    @Test
    void testParameterLookupValidation() {
        Map<String, String> input = new HashMap<>();
        input.put("a", "3");
        input.put("b", "31");  // value is not in lookup table

        Result result = _ENGINE.process("starting_sample", input);

        Assertions.assertEquals(Type.FAILED_INPUT, result.getType());

        // since input "b" is fail_on_invalid, table processing should not continue
        Assertions.assertEquals(0, result.getPath().size());

        // one error for input, and one error each of the two tables because of no match
        Assertions.assertEquals(1, result.getErrors().size());

        // make "b" a valid value
        input.put("b", "30");

        result = _ENGINE.process("starting_sample", input);

        Assertions.assertEquals(Type.STAGED, result.getType());

        // one error for input, and one error each of the two tables because of no match
        Assertions.assertEquals(2, result.getErrors().size());
    }

    @Test
    void testSingleTableProcess() {
        Map<String, String> input = new HashMap<>();
        input.put("a", "7");
        input.put("b", "03");  // should map to "hemeretic" without using second table
        input.put("e", "X");
        Result result = _ENGINE.process("starting_sample", input);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(2, result.getPath().size());

        Assertions.assertEquals("LINE1", input.get("result"));
        Assertions.assertEquals("HARD-CODE", input.get("d"));
    }

    @Test
    void testOneJumpProcess() {
        Map<String, String> input = new HashMap<>();
        input.put("a", "5");
        input.put("b", "20");
        input.put("c", "A");
        input.put("e", "X");
        Result result = _ENGINE.process("starting_sample", input);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(3, result.getPath().size());

        Assertions.assertEquals("A", input.get("result"));

        // now test an error line in the jump table
        input = new HashMap<>();
        input.put("a", "5");
        input.put("b", "20");
        input.put("c", "C");
        input.put("e", "X");

        result = _ENGINE.process("starting_sample", input);

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertEquals(3, result.getPath().size());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals("Bad C value", result.getErrors().get(0).getMessage());
        Assertions.assertNull(result.getErrors().get(0).getKey());
        Assertions.assertEquals("table_jump_sample", result.getErrors().get(0).getTable());

        // finally test that no match is found in the jump table
        input = new HashMap<>();
        input.put("a", "5");
        input.put("b", "20");
        input.put("c", "D");
        input.put("e", "X");

        result = _ENGINE.process("starting_sample", input);

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertEquals(3, result.getPath().size());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertTrue(result.getErrors().get(0).getMessage().startsWith("Match not found"));
        Assertions.assertNull(result.getErrors().get(0).getKey());
        Assertions.assertEquals("table_jump_sample", result.getErrors().get(0).getTable());
    }

    @Test
    void testProcessError() {
        Map<String, String> input = new HashMap<>();
        input.put("a", "9");
        input.put("b", "99");
        input.put("e", "X");
        Result result = _ENGINE.process("starting_sample", input);

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(2, result.getPath().size());
        Assertions.assertEquals("999", result.getErrors().get(0).getMessage());
        Assertions.assertNull(result.getErrors().get(0).getKey());
        Assertions.assertEquals("table_sample_first", result.getErrors().get(0).getTable());
        Assertions.assertEquals(Collections.singletonList("result"), result.getErrors().get(0).getColumns());

        // test case with generated error message (i.e. the column is "ERROR:" without a message
        input.put("a", "8");
        input.put("b", "99");
        input.put("e", "X");
        result = _ENGINE.process("starting_sample", input);
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(2, result.getPath().size());
        Assertions.assertEquals("Matching resulted in an error in table 'table_sample_first' for column 'result' (8,99)", result.getErrors().get(0).getMessage());
        Assertions.assertNull(result.getErrors().get(0).getKey());
        Assertions.assertEquals("table_sample_first", result.getErrors().get(0).getTable());
        Assertions.assertEquals(Collections.singletonList("result"), result.getErrors().get(0).getColumns());
    }

    @Test
    void testProcessWithNullValues() {
        InMemoryDataProvider provider = new InMemoryDataProvider("Test", "1.0");

        StagingTable table = new StagingTable("table_null_values");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("1", "VALUE:FOUND1");
        table.addRawRow("2", "VALUE");
        table.addRawRow("3", "VALUE:");
        table.addRawRow("*", "MATCH");
        provider.addTable(table);

        StagingSchema schema = new StagingSchema("starting_null_values");
        schema.setSchemaSelectionTable("table_selection");
        StagingSchemaInput inputKey = new StagingSchemaInput("a");
        inputKey.setDefault("0");
        schema.addInput(inputKey);
        schema.addInitialContext("result", "0");
        schema.addMapping(new StagingMapping("m1", Collections.singletonList(new StagingTablePath("table_null_values"))));
        provider.addSchema(schema);
        DecisionEngine engine = new DecisionEngine(provider);

        Map<String, String> input = new HashMap<>();
        Result result = engine.process("starting_null_values", input);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("0", input.get("result"));

        input.clear();
        input.put("a", "1");
        result = engine.process("starting_null_values", input);
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("FOUND1", input.get("result"));

        input.clear();
        input.put("a", "2");
        result = engine.process("starting_null_values", input);
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertFalse(input.containsKey("result"));

        input.clear();
        input.put("a", "3");
        result = engine.process("starting_null_values", input);
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("", input.get("result"));
    }

    @Test
    void testProcessWithStop() {
        // first test that we get a result from the second table
        Map<String, String> input = new HashMap<>();
        input.put("a", "5");
        input.put("b", "20");
        input.put("c", "A");
        input.put("e", "X");
        Result result = _ENGINE.process("starting_sample", input);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(3, result.getPath().size());

        Assertions.assertEquals("A", input.get("result"));
        Assertions.assertEquals("LINE1", input.get("shared_result"));

        // next text then when STOP is encountered, the second table is not processed
        input.clear();
        input.put("a", "5");
        input.put("b", "20");
        input.put("c", "Z");
        input.put("e", "X");
        result = _ENGINE.process("starting_sample", input);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(2, result.getPath().size());

        Assertions.assertFalse(input.containsKey("result"));
        Assertions.assertFalse(input.containsKey("shared_result"));
    }

    @Test
    void testProcessWithBlanks() {
        Map<String, String> input = new HashMap<>();
        input.put("x", "1");
        input.put("y", "");
        Result result = _ENGINE.process("starting_blank", input);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(1, result.getPath().size());

        Assertions.assertEquals("LINE1", input.get("y"));
        Assertions.assertEquals("LINE1", input.get("z"));

        // verify that context blanks are trimmed
        input.put("x", "1");
        input.put("y", "  ");
        result = _ENGINE.process("starting_blank", input);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(1, result.getPath().size());

        Assertions.assertEquals("LINE1", input.get("y"));
        Assertions.assertEquals("LINE1", input.get("z"));
    }

    @Test
    void testProcessWithDoubleInputMapping() {
        Map<String, String> input = new HashMap<>();
        input.put("x", "1");
        Result result = _ENGINE.process("starting_double_input", input);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(1, result.getPath().size());

        Assertions.assertEquals("LINE3", input.get("result"));
    }

    @Test
    void testProcessWithDoubleOutputMapping() {
        Map<String, String> input = new HashMap<>();
        input.put("a", "1");
        input.put("b", "00");
        Result result = _ENGINE.process("starting_double_output", input);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(1, result.getPath().size());

        Assertions.assertNull(input.get("result"));
        Assertions.assertEquals("LINE1", input.get("output1"));
        Assertions.assertEquals("LINE1", input.get("output2"));
    }

    @Test
    void testInvolvedAlgorithms() {
        List<Mapping> mappings;
        Map<String, String> context = new HashMap<>();

        Schema schema = _ENGINE.getProvider().getSchema("starting_min");
        mappings = _ENGINE.getInvolvedMappings(schema, context);
        Assertions.assertEquals(0, mappings.size());

        schema = _ENGINE.getProvider().getSchema("starting_inclusions");

        mappings = _ENGINE.getInvolvedMappings(schema, context);
        Assertions.assertEquals(1, mappings.size());

        context.put("a", "1");
        mappings = _ENGINE.getInvolvedMappings(schema, context);
        Assertions.assertEquals(1, mappings.size());

        context.put("a", "2");
        mappings = _ENGINE.getInvolvedMappings(schema, context);
        Assertions.assertEquals(2, mappings.size());
    }

    @Test
    void testInvolvedTables() {
        Set<String> tables;

        // test a case with no involved tables
        tables = _ENGINE.getInvolvedTables("starting_min");
        Assertions.assertEquals(0, tables.size());

        // test a case with a single table with one jump
        tables = _ENGINE.getInvolvedTables("starting_sample");
        Assertions.assertEquals(4, tables.size());
        Assertions.assertTrue(tables.contains("table_lookup_sample"));
        Assertions.assertTrue(tables.contains("table_sample_first"));
        Assertions.assertTrue(tables.contains("table_sample_second"));
        Assertions.assertTrue(tables.contains("table_jump_sample"));

        // test a case with inclusion/exclusion tables
        tables = _ENGINE.getInvolvedTables("starting_inclusions");
        Assertions.assertEquals(5, tables.size());
        Assertions.assertTrue(tables.contains("table_part1"));
        Assertions.assertTrue(tables.contains("table_part2"));
        Assertions.assertTrue(tables.contains("table_inclusion1"));
        Assertions.assertTrue(tables.contains("table_inclusion2"));
        Assertions.assertTrue(tables.contains("table_exclusion1"));
    }

    @Test
    void testInvolvedTablesWhenEmpty() {
        InMemoryDataProvider provider = new InMemoryDataProvider("Test", "1.0");

        // add empty table
        StagingTable table = new StagingTable("table1");
        table.addColumnDefinition("a", ColumnType.INPUT);
        provider.addTable(table);

        // add another empty table
        table = new StagingTable("table2");
        table.addColumnDefinition("b", ColumnType.INPUT);
        provider.addTable(table);

        StagingSchema schema = new StagingSchema("def1");
        schema.setSchemaSelectionTable("table_selection");
        schema.setOnInvalidInput(Schema.StagingInputErrorHandler.FAIL);
        schema.addInput("a");
        schema.addInput("b");
        StagingMapping mapping = new StagingMapping("m1");
        mapping.setTablePaths(Arrays.asList(new StagingTablePath("table1"), new StagingTablePath("table2")));
        schema.addMapping(mapping);
        provider.addSchema(schema);

        DecisionEngine engine = new DecisionEngine(provider);

        Set<String> tables = engine.getInvolvedTables("def1");
        Assertions.assertEquals(2, tables.size());
        Assertions.assertTrue(tables.contains("table1"));
        Assertions.assertTrue(tables.contains("table2"));
    }

    @Test
    void testInvolvedTableRecursion() {
        Set<String> tables = _ENGINE.getInvolvedTables("starting_recursion");

        Assertions.assertEquals(1, tables.size());
        Assertions.assertTrue(tables.contains("table_recursion"));
    }

    @Test
    void testProcessWithRecursion() {
        Map<String, String> input = new HashMap<>();
        input.put("a", "4");
        Result result = _ENGINE.process("starting_recursion", input);

        Assertions.assertEquals(Type.STAGED, result.getType());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(1, result.getPath().size());
        Assertions.assertEquals("table_recursion", result.getErrors().get(0).getTable());
        Assertions.assertNull(result.getErrors().get(0).getColumns());
    }

    @Test
    void testProcessWithMultipleEndpoints() {
        // first test all 3 results get set
        Map<String, String> input = new HashMap<>();
        input.put("a", "2");
        input.put("b", "12");
        Result result = _ENGINE.process("starting_multiple_endpoints", input);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(Type.STAGED, result.getType());
        Assertions.assertEquals(1, result.getPath().size());

        Assertions.assertEquals("1_LINE1", input.get("r1"));
        Assertions.assertEquals("2_LINE1", input.get("r2"));
        Assertions.assertEquals("3_LINE1", input.get("r3"));

        // test 2 VALUEs and an ERROR
        input.clear();
        input.put("a", "0");
        input.put("b", "25");
        result = _ENGINE.process("starting_multiple_endpoints", input);
        Assertions.assertEquals(Type.STAGED, result.getType());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals("table_multiple_inputs", result.getErrors().get(0).getTable());
        Assertions.assertEquals(Collections.singletonList("r2"), result.getErrors().get(0).getColumns());
        Assertions.assertEquals("2_LINE2", result.getErrors().get(0).getMessage());
        Assertions.assertEquals(1, result.getPath().size());

        Assertions.assertEquals("1_LINE2", input.get("r1"));
        Assertions.assertFalse(input.containsKey("r2"));
        Assertions.assertEquals("3_LINE2", input.get("r3"));

        // test 2 JUMPs and one VALUE and a missing jump table value
        input.clear();
        input.put("a", "5");
        input.put("b", "20");
        result = _ENGINE.process("starting_multiple_endpoints", input);

        Assertions.assertEquals(Type.STAGED, result.getType());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertTrue(result.getErrors().get(0).getMessage().startsWith("Match not found"));
        Assertions.assertNull(result.getErrors().get(0).getKey());
        Assertions.assertEquals("table_jump_sample", result.getErrors().get(0).getTable());
        Assertions.assertEquals(Collections.singletonList("result"), result.getErrors().get(0).getColumns());

        // test 1 JUMP and 2 VALUEs
        input.clear();
        input.put("a", "5");
        input.put("b", "20");
        input.put("c", "A");
        result = _ENGINE.process("starting_multiple_endpoints", input);

        Assertions.assertEquals(Type.STAGED, result.getType());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertFalse(input.containsKey("r1"));
        Assertions.assertEquals("A", input.get("result"));
        Assertions.assertEquals("2_LINE3", input.get("r2"));
        Assertions.assertEquals("3_LINE3", input.get("r3"));

        // test 3 ERRORs
        input.clear();
        input.put("a", "9");
        input.put("b", "99");
        result = _ENGINE.process("starting_multiple_endpoints", input);

        Assertions.assertEquals(Type.STAGED, result.getType());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertEquals(3, result.getErrors().size());
        Assertions.assertEquals("1_LINE4", result.getErrors().get(0).getMessage());
        Assertions.assertNull(result.getErrors().get(0).getKey());
        Assertions.assertEquals("2_LINE4", result.getErrors().get(1).getMessage());
        Assertions.assertNull(result.getErrors().get(1).getKey());
        Assertions.assertEquals("3_LINE4", result.getErrors().get(2).getMessage());
        Assertions.assertNull(result.getErrors().get(2).getKey());
        Assertions.assertFalse(input.containsKey("r1"));
        Assertions.assertFalse(input.containsKey("r2"));
        Assertions.assertFalse(input.containsKey("r3"));
    }

    @Test
    void testRowNotFoundWithMultipleOutputs() {
        InMemoryDataProvider provider = new InMemoryDataProvider("Test", "1.0");

        // test a situation where a row with mulitple inputs is not found
        StagingTable table = new StagingTable("table_input");
        table.addColumnDefinition("input1", ColumnType.INPUT);
        table.addColumnDefinition("output1", ColumnType.ENDPOINT);
        table.addColumnDefinition("output2", ColumnType.ENDPOINT);
        table.addRawRow("000", "VALUE:000", "VALUE:000");
        table.addRawRow("001", "VALUE:001", "VALUE:001");
        provider.addTable(table);

        StagingSchema schema = new StagingSchema("sample_outputs");
        schema.setSchemaSelectionTable("table_selection");
        schema.setOnInvalidInput(Schema.StagingInputErrorHandler.FAIL);
        schema.addInput(new StagingSchemaInput("input1", "input1", "table_input"));

        StagingMapping mapping = new StagingMapping("mapping1");
        StagingTablePath path = new StagingTablePath("table_input");
        mapping.addTablePath(path);
        schema.addMapping(mapping);
        provider.addSchema(schema);

        DecisionEngine engine = new DecisionEngine(provider);

        // test match not found
        Map<String, String> input = new HashMap<>();
        input.put("a", "4");
        input.put("b", "55");
        Result result = engine.process("sample_outputs", input);
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals("table_input", result.getErrors().get(0).getTable());
        Assertions.assertEquals(Arrays.asList("output1", "output2"), result.getErrors().get(0).getColumns());
    }

    @Test
    void testMultipleEndpointWithoutRecursion() {
        // test 2 JUMPs to same table and one value; this is not infinite recursion but since same table called twice it gets confused
        Map<String, String> input = new HashMap<>();
        input.put("a", "4");
        input.put("b", "44");
        input.put("c", "A");
        Result result = _ENGINE.process("starting_multiple_endpoints", input);

        Assertions.assertEquals(Type.STAGED, result.getType());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertFalse(input.containsKey("r1"));
        Assertions.assertFalse(input.containsKey("r3"));
        Assertions.assertEquals("A", input.get("result"));
        Assertions.assertEquals("2_LINE5", input.get("r2"));
    }

    @Test
    void testInclusionsAndExclusions() {
        Map<String, String> input = new HashMap<>();
        input.put("a", "1");
        input.put("b", "2");
        input.put("c", "3");
        Result result = _ENGINE.process("starting_inclusions", input);

        Assertions.assertEquals(Type.STAGED, result.getType());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("2", input.get("result"));
        Assertions.assertFalse(input.containsKey("special"));

        input.clear();
        input.put("a", "8");
        input.put("b", "2");
        input.put("c", "3");
        result = _ENGINE.process("starting_inclusions", input);

        Assertions.assertEquals(Type.STAGED, result.getType());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("3", input.get("result"));
        Assertions.assertEquals("SUCCESS", input.get("special"));

        input.clear();
        input.put("a", "9");
        input.put("b", "2");
        input.put("c", "3");
        result = _ENGINE.process("starting_inclusions", input);

        Assertions.assertEquals(Type.STAGED, result.getType());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertFalse(input.containsKey("result"));
        Assertions.assertEquals("SUCCESS", input.get("special"));
    }

    @Test
    void testDuplicateAlgorithms() {
        InMemoryDataProvider provider = new InMemoryDataProvider("Test", "1.0");
        StagingSchema schema = new StagingSchema();
        schema.setId("TEST1");
        Assertions.assertThrows(IllegalStateException.class, () -> provider.addSchema(schema));
    }

    /**
     * Shortcut for constructing a Set using a List
     * @param values list of values
     * @return Set of values
     */
    private Set<String> asSet(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    @Test
    void testSchemaInputs() {
        DataProvider provider = _ENGINE.getProvider();

        Assertions.assertEquals(asSet(), _ENGINE.getInputs(provider.getSchema("starting_min")));
        Assertions.assertEquals(asSet("a", "b", "c", "e"), _ENGINE.getInputs(provider.getSchema("starting_sample")));
        Assertions.assertEquals(asSet("a", "b", "c"), _ENGINE.getInputs(provider.getSchema("starting_inclusions")));
        Assertions.assertEquals(asSet("a"), _ENGINE.getInputs(provider.getSchema("starting_recursion")));
        Assertions.assertEquals(asSet("a", "b", "c"), _ENGINE.getInputs(provider.getSchema("starting_multiple_endpoints")));
        Assertions.assertEquals(asSet("b", "not_in_input_list"), _ENGINE.getInputs(provider.getSchema("starting_inclusions_extra_inputs")));
        Assertions.assertEquals(asSet("main_input"), _ENGINE.getInputs(provider.getSchema("starting_intermediate_values")));
    }

    @Test
    void testGetSchemaOutputs() {
        DataProvider provider = _ENGINE.getProvider();

        Assertions.assertEquals(asSet(), _ENGINE.getOutputs(provider.getSchema("starting_min")));
        Assertions.assertEquals(asSet("result", "shared_result"), _ENGINE.getOutputs(provider.getSchema("starting_sample")));
        Assertions.assertEquals(asSet("result", "special"), _ENGINE.getOutputs(provider.getSchema("starting_inclusions")));
        Assertions.assertEquals(asSet("result"), _ENGINE.getOutputs(provider.getSchema("starting_recursion")));
        Assertions.assertEquals(asSet("result", "r1", "r2", "r3"), _ENGINE.getOutputs(provider.getSchema("starting_multiple_endpoints")));
        Assertions.assertEquals(asSet("mapped_result"), _ENGINE.getOutputs(provider.getSchema("starting_inclusions_extra_inputs")));
        Assertions.assertEquals(asSet("intermediate_output", "final_output"), _ENGINE.getOutputs(provider.getSchema("starting_intermediate_values")));
    }

    @Test
    void testGetTableInputsAsString() {
        StagingTable table = new StagingTable("table_inputs");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("b", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("1,2,5-9", "00-12", "Line1", "VALUE:LINE1");
        table.addRawRow("3,4", "17,19,22", "Line2", "JUMP:table_jump_sample");
        table.addRawRow("0", "23-30", "Line3", "VALUE:LINE3");
        table.addRawRow("5", "20", "Line4", "JUMP:table_jump_sample");
        table.addRawRow("*", "55", "Line5", "VALUE:LINE5");
        table.addRawRow("9", "99", "Line6", "ERROR:999");

        Map<String, String> context = new HashMap<>();

        Assertions.assertEquals(DecisionEngine._BLANK_OUTPUT + "," + DecisionEngine._BLANK_OUTPUT, DecisionEngine.getTableInputsAsString(table, context));

        context.put("b", "25");
        Assertions.assertEquals(DecisionEngine._BLANK_OUTPUT + ",25", DecisionEngine.getTableInputsAsString(table, context));

        context.put("a", "7");
        Assertions.assertEquals("7,25", DecisionEngine.getTableInputsAsString(table, context));
        context.put("a", "    7");
        Assertions.assertEquals("7,25", DecisionEngine.getTableInputsAsString(table, context));
        context.put("a", "7    ");
        Assertions.assertEquals("7,25", DecisionEngine.getTableInputsAsString(table, context));

        table = new StagingTable("table_empty");
        context = new HashMap<>();
        Assertions.assertEquals("", DecisionEngine.getTableInputsAsString(table, context));
    }

    @Test
    void testOutputsAndDefaults() {
        InMemoryDataProvider provider = new InMemoryDataProvider("Test", "1.0");

        StagingTable table = new StagingTable("table_input");
        table.addColumnDefinition("input1", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addRawRow("000", "Alpha");
        table.addRawRow("001", "Beta");
        table.addRawRow("002", "Gamma");
        provider.addTable(table);

        table = new StagingTable("table_output");
        table.addColumnDefinition("output1", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addRawRow("A", "Alpha");
        table.addRawRow("B", "Beta");
        table.addRawRow("C", "Gamma");
        provider.addTable(table);

        StagingSchema schema = new StagingSchema("sample_outputs");
        schema.setSchemaSelectionTable("table_selection");
        schema.setOnInvalidInput(Schema.StagingInputErrorHandler.FAIL);
        schema.addInput(new StagingSchemaInput("input1", "input1", "table_input"));

        StagingSchemaOutput output = new StagingSchemaOutput("output1", "output1", "table_output");
        output.setDefault("A");
        schema.addOutput(output);

        output = new StagingSchemaOutput("output2");
        schema.addOutput(output);

        StagingMapping mapping = new StagingMapping("mapping1");
        schema.addMapping(mapping);
        provider.addSchema(schema);

        DecisionEngine engine = new DecisionEngine(provider);

        Map<String, String> input = new HashMap<>();
        input.put("input1", "000");
        Result result = engine.process("sample_outputs", input);

        Assertions.assertEquals(Type.STAGED, result.getType());

        // default value should be set
        Assertions.assertEquals("A", input.get("output1"));
        // no default value so it should be blank
        Assertions.assertEquals("", input.get("output2"));

        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(new HashSet<>(Arrays.asList("table_input", "table_output")), engine.getInvolvedTables(schema));

        // modify the definition to create a bad default value for output1
        schema.getOutputs().get(0).setDefault("BAD");
        provider.initSchema(schema);
        engine = new DecisionEngine(provider);

        input = new HashMap<>();
        input.put("input1", "000");
        result = engine.process("sample_outputs", input);

        Assertions.assertEquals(Type.STAGED, result.getType());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertEquals(Error.Type.INVALID_OUTPUT, result.getErrors().get(0).getType());
        Assertions.assertEquals("table_output", result.getErrors().get(0).getTable());
        Assertions.assertEquals("output1", result.getErrors().get(0).getKey());

        // default value should be set
        Assertions.assertEquals("BAD", input.get("output1"));
        // no default value so it should be blank
        Assertions.assertEquals("", input.get("output2"));
    }

    @Test
    void testInitialContextReferences() {
        StagingSchema schema = new StagingSchema("test_initial_context");
        schema.setSchemaSelectionTable("table_selection");
        schema.setOnInvalidInput(Schema.StagingInputErrorHandler.FAIL);

        schema.addInitialContext("a", "foo1");
        schema.addInitialContext("b", "{{foo1}}");
        schema.addInitialContext("c", "foo2");
        schema.addInitialContext("d", "{{foo2}}");
        schema.addInitialContext("e", "{{bad_key}}");

        InMemoryDataProvider provider = new InMemoryDataProvider("Test", "1.0");
        provider.addSchema(schema);
        DecisionEngine engine = new DecisionEngine(provider);

        Map<String, String> context = new HashMap<>();
        context.put("foo1", "FIRST");
        context.put("foo2", "SECOND");
        Result result = engine.process("test_initial_context", context);

        Assertions.assertEquals(Type.STAGED, result.getType());

        Assertions.assertEquals("foo1", context.get("a"));
        Assertions.assertEquals("FIRST", context.get("b"));
        Assertions.assertEquals("foo2", context.get("c"));
        Assertions.assertEquals("SECOND", context.get("d"));
        Assertions.assertEquals("", context.get("e"));
    }

    @Test
    void testInputsOutputsDefaultContextReferences() {
        StagingSchema schema = new StagingSchema("test_context");
        schema.setSchemaSelectionTable("dummy");
        schema.setOnInvalidInput(Schema.StagingInputErrorHandler.FAIL);

        // add inputs
        StagingSchemaInput input = new StagingSchemaInput("input1");
        input.setDefault("foo1");
        schema.addInput(input);

        input = new StagingSchemaInput("input2");
        input.setDefault("{{foo1}}");
        schema.addInput(input);

        // add outputs
        StagingSchemaOutput output = new StagingSchemaOutput("output1");
        output.setDefault("foo2");
        schema.addOutput(output);

        output = new StagingSchemaOutput("output2");
        output.setDefault("{{foo2}}");
        schema.addOutput(output);

        output = new StagingSchemaOutput("output3");
        output.setDefault("{{bad_key}}");
        schema.addOutput(output);

        output = new StagingSchemaOutput("output4");
        output.setDefault("{{input1}}");
        schema.addOutput(output);

        output = new StagingSchemaOutput("output5");
        output.setDefault("{{input2}}");
        schema.addOutput(output);

        InMemoryDataProvider provider = new InMemoryDataProvider("Test", "1.0");
        provider.addSchema(schema);
        DecisionEngine engine = new DecisionEngine(provider);

        Map<String, String> context = new HashMap<>();
        context.put("foo1", "FIRST");
        context.put("foo2", "SECOND");
        Result result = engine.process("test_context", context);

        Assertions.assertEquals(Type.STAGED, result.getType());

        Assertions.assertEquals("foo2", context.get("output1"));
        Assertions.assertEquals("SECOND", context.get("output2"));
        Assertions.assertEquals("", context.get("output3"));
        Assertions.assertEquals("foo1", context.get("output4"));
        Assertions.assertEquals("FIRST", context.get("output5"));
    }

    @Test
    void testMappingInputsWithReferenceInTable() {
        InMemoryDataProvider provider = new InMemoryDataProvider("Test", "1.0");

        // test a situation where an input is also used as a reference in an endpoint; when the definition remaps that input it should not show up
        // as both values
        StagingTable table = new StagingTable("table_input");
        table.addColumnDefinition("input1", ColumnType.INPUT);
        table.addColumnDefinition("output1", ColumnType.ENDPOINT);
        table.addRawRow("000", "VALUE:001");
        table.addRawRow("001", "VALUE:000");
        table.addRawRow("002", "VALUE:{{input1}}");
        provider.addTable(table);

        StagingSchema schema = new StagingSchema("sample_outputs");
        schema.setSchemaSelectionTable("table_selection");
        schema.setOnInvalidInput(Schema.StagingInputErrorHandler.FAIL);
        schema.addInput(new StagingSchemaInput("input1", "input1", "table_input"));

        StagingMapping mapping = new StagingMapping("mapping1");
        StagingTablePath path = new StagingTablePath("table_input");
        path.addInputMapping("remapped1", "input1");
        mapping.addTablePath(path);
        schema.addMapping(mapping);
        provider.addSchema(schema);

        DecisionEngine engine = new DecisionEngine(provider);

        Assertions.assertEquals(new HashSet<>(Collections.singletonList("remapped1")), engine.getInputs(schema.getMappings().get(0).getTablePaths().get(0)));
    }

}
