/*

 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

import com.imsweb.decisionengine.ColumnDefinition.ColumnType;
import com.imsweb.decisionengine.Endpoint.EndpointType;
import com.imsweb.decisionengine.Result.Type;
import com.imsweb.decisionengine.basic.BasicDataProvider;
import com.imsweb.decisionengine.basic.BasicInput;
import com.imsweb.decisionengine.basic.BasicMapping;
import com.imsweb.decisionengine.basic.BasicOutput;
import com.imsweb.decisionengine.basic.BasicSchema;
import com.imsweb.decisionengine.basic.BasicTable;
import com.imsweb.decisionengine.basic.BasicTablePath;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for DecisionEngine
 */
public class DecisionEngineTest {

    private static DecisionEngine _ENGINE;

    @BeforeClass
    public static void init() {
        BasicDataProvider provider = new BasicDataProvider();

        BasicTable table = new BasicTable("table_lookup_sample");
        table.addColumnDefinition("b", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addRawRow("00-09", "First 10");
        table.addRawRow("10-19", "Second 10");
        table.addRawRow("20-29", "Third 10");
        table.addRawRow("30", "Thirty");
        table.addRawRow("99", "Ninety-nine");
        table.addRawRow("", "blank");
        provider.addTable(table);

        table = new BasicTable("table_jump_sample");
        table.addColumnDefinition("c", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("A", "Line1", "VALUE:A");
        table.addRawRow("B", "Line2", "VALUE:B");
        table.addRawRow("C", "Line3", "ERROR:Bad C value");
        table.addRawRow("Z", "Line4", "STOP");
        provider.addTable(table);

        table = new BasicTable("table_sample_first");
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

        table = new BasicTable("table_multiple_inputs");
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

        table = new BasicTable("table_sample_second");
        table.addColumnDefinition("e", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("shared_result", ColumnType.ENDPOINT);
        table.addRawRow("X", "Line1", "VALUE:LINE1");
        table.addRawRow("Y", "Line2", "VALUE:LINE1");
        table.addRawRow("Z", "Line3", "VALUE:LINE3");
        provider.addTable(table);

        table = new BasicTable("table_recursion");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("1,2,5-9", "Line1", "VALUE:LINE1");
        table.addRawRow("3,4", "Line2", "JUMP:table_recursion");
        provider.addTable(table);

        table = new BasicTable("table_inclusion1");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addRawRow("0-3,5");
        provider.addTable(table);

        table = new BasicTable("table_inclusion2");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addRawRow("4,6-8");
        provider.addTable(table);

        table = new BasicTable("table_inclusion3");
        table.addColumnDefinition("not_in_input_list", ColumnType.INPUT);
        table.addRawRow("4,6-8");
        provider.addTable(table);

        table = new BasicTable("table_exclusion1");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addRawRow("1");
        provider.addTable(table);

        table = new BasicTable("table_part1");
        table.addColumnDefinition("val", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("1", "1", "VALUE:1");
        table.addRawRow("2", "2", "VALUE:2");
        table.addRawRow("3", "3", "VALUE:3");
        provider.addTable(table);

        table = new BasicTable("table_part2");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("special", ColumnType.ENDPOINT);
        table.addRawRow("*", "", "VALUE:SUCCESS");
        provider.addTable(table);

        table = new BasicTable("table_create_intermediate");
        table.addColumnDefinition("main_input", ColumnType.INPUT);
        table.addColumnDefinition("intermediate_output", ColumnType.ENDPOINT);
        table.addRawRow("1", "VALUE:1");
        provider.addTable(table);

        table = new BasicTable("table_use_intermediate");
        table.addColumnDefinition("intermediate_output", ColumnType.INPUT);
        table.addColumnDefinition("final_output", ColumnType.ENDPOINT);
        table.addRawRow("1", "VALUE:1");
        provider.addTable(table);

        table = new BasicTable("table_blank_matching");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("b", ColumnType.INPUT);
        table.addColumnDefinition("b", ColumnType.ENDPOINT);
        table.addColumnDefinition("c", ColumnType.ENDPOINT);
        table.addRawRow("*", "", "VALUE:LINE1", "VALUE:LINE1");
        table.addRawRow("NA", "*", "VALUE:LINE2", "VALUE:LINE2");
        table.addRawRow("", "*", "VALUE:LINE3", "VALUE:LINE3");
        table.addRawRow("*", "*", "MATCH", "MATCH");
        provider.addTable(table);

        table = new BasicTable("table_multiple_input");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("b", ColumnType.INPUT);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("1", "0", "VALUE:LINE1");
        table.addRawRow("0", "1", "VALUE:LINE2");
        table.addRawRow("1", "1", "VALUE:LINE3");
        provider.addTable(table);

        BasicSchema def = new BasicSchema("starting_sample");
        def.setOnInvalidInput(Schema.StagingInputErrorHandler.FAIL);
        def.addInput("a");
        BasicInput input = new BasicInput("b", "table_lookup_sample");
        def.addInput(input);
        def.addInput("c");
        def.addInitialContext("d", "HARD-CODE");
        BasicMapping mapping = new BasicMapping("m1");
        mapping.setTablePaths(Arrays.asList(new BasicTablePath("table_sample_first"), new BasicTablePath("table_sample_second")));
        def.addMapping(mapping);
        provider.addDefinition(def);

        def = new BasicSchema("starting_min");
        def.addInitialContext("foo", "bar");
        provider.addDefinition(def);

        def = new BasicSchema("starting_multiple_endpoints");
        def.addInput("a");
        def.addInput("b");
        def.addInput("c");
        def.addMapping(new BasicMapping("m1", Collections.singletonList(new BasicTablePath("table_multiple_inputs"))));
        provider.addDefinition(def);

        def = new BasicSchema("starting_recursion");
        def.addInput("a");
        def.addMapping(new BasicMapping("m1", Collections.singletonList(new BasicTablePath("table_recursion"))));
        provider.addDefinition(def);

        def = new BasicSchema("starting_inclusions");
        def.addInput("a");
        def.addInput("b");
        def.addInput("c");
        mapping = new BasicMapping("m1");
        mapping.setInclusionTables(Collections.singletonList(new BasicTablePath("table_inclusion1")));
        BasicTablePath path = new BasicTablePath("table_part1");
        path.addInputMapping("b", "val");
        mapping.addTablePath(path);
        def.addMapping(mapping);
        mapping = new BasicMapping("m2");
        mapping.setInclusionTables(Collections.singletonList(new BasicTablePath("table_inclusion2")));
        path = new BasicTablePath("table_part1");
        path.addInputMapping("c", "val");
        mapping.addTablePath(path);
        def.addMapping(mapping);
        mapping = new BasicMapping("m3");
        mapping.setExclusionTables(Collections.singletonList(new BasicTablePath("table_exclusion1")));
        path = new BasicTablePath("table_part2");
        mapping.addTablePath(path);
        def.addMapping(mapping);
        provider.addDefinition(def);

        def = new BasicSchema("starting_intermediate_values");
        def.addInput("main_input");
        mapping = new BasicMapping("m1");
        mapping.addTablePath(new BasicTablePath("table_create_intermediate"));
        mapping.addTablePath(new BasicTablePath("table_use_intermediate"));
        def.addMapping(mapping);
        provider.addDefinition(def);

        def = new BasicSchema("starting_inclusions_extra_inputs");
        def.addInput("a");
        def.addInput("b");
        def.addInput("c");
        mapping = new BasicMapping("m1");
        mapping.setInclusionTables(Collections.singletonList(new BasicTablePath("table_inclusion3")));
        path = new BasicTablePath("table_part1");
        path.addInputMapping("b", "val");
        path.addOutputMapping("result", "mapped_result");
        mapping.addTablePath(path);
        def.addMapping(mapping);
        provider.addDefinition(def);

        def = new BasicSchema("starting_blank");
        def.addInput("a");
        def.addInput("b");
        mapping = new BasicMapping("m1");
        path = new BasicTablePath("table_blank_matching");
        path.addInputMapping("x", "a");
        path.addInputMapping("y", "b");
        path.addOutputMapping("b", "y");
        path.addOutputMapping("c", "z");
        mapping.addTablePath(path);
        def.addMapping(mapping);
        provider.addDefinition(def);

        def = new BasicSchema("starting_double_input");
        def.addInput("x");
        mapping = new BasicMapping("m1");
        path = new BasicTablePath("table_multiple_input");
        path.addInputMapping("x", "a");
        path.addInputMapping("x", "b");
        mapping.addTablePath(path);
        def.addMapping(mapping);
        provider.addDefinition(def);

        def = new BasicSchema("starting_double_output");
        def.addInput("a");
        def.addInput("b");
        mapping = new BasicMapping("m1");
        path = new BasicTablePath("table_sample_first");
        path.addOutputMapping("result", "output1");
        path.addOutputMapping("result", "output2");
        mapping.addTablePath(path);
        def.addMapping(mapping);
        provider.addDefinition(def);

        _ENGINE = new DecisionEngine(provider);
    }

    @Test
    public void testMatch() {
        List<Range> range = new ArrayList<>();
        range.add(new Range("1", "1"));
        range.add(new Range("4", "4"));
        range.add(new Range("9", "9"));
        assertTrue(DecisionEngine.testMatch(range, "9", new HashMap<>()));
        assertFalse(DecisionEngine.testMatch(range, "7", new HashMap<>()));

        range = new ArrayList<>();
        range.add(new Range("11", "54"));
        range.add(new Range("99", "99"));
        assertTrue(DecisionEngine.testMatch(range, "23", new HashMap<>()));
    }

    @Test
    public void testEmptyTable() {
        BasicDataProvider provider = new BasicDataProvider();
        BasicTable table = new BasicTable("basic_test_table");
        table.addColumnDefinition("size", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("size_result", ColumnType.ENDPOINT);
        provider.addTable(table);

        assertTrue(provider.getTable("basic_test_table").getRawRows().isEmpty());
        assertTrue(provider.getTable("basic_test_table").getTableRows().isEmpty());
    }

    @Test
    public void testMatchTable() {
        BasicDataProvider provider = new BasicDataProvider();
        BasicTable table = new BasicTable("basic_test_table");
        table.addColumnDefinition("size", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("size_result", ColumnType.ENDPOINT);
        table.addRawRow("001-003,004,006", "Little", "JUMP:some_crazy_table");
        table.addRawRow("007-019,022", "Medium", "VALUE:medium_stuff");
        table.addRawRow("076-099,999", "HUGE!", "ERROR:Get that huge stuff out of here!");
        table.addRawRow("030", "Match endpoint", "MATCH");
        provider.addTable(table);

        Table matchTable = provider.getTable("basic_test_table");
        assertNotNull(matchTable);

        // create context of input fields
        Map<String, String> input = new HashMap<>();

        // first try it with missing input
        assertNull(DecisionEngine.matchTable(matchTable, input));

        input.put("size", "003");
        assertThat(DecisionEngine.matchTable(matchTable, input))
                .extracting(Endpoint::getType, Endpoint::getValue, Endpoint::getResultKey)
                .containsExactly(tuple(EndpointType.JUMP, "some_crazy_table", "size_result"));

        input.put("size", "014");
        List<? extends Endpoint> results = DecisionEngine.matchTable(matchTable, input);
        assertEquals(1, results.size());
        assertEquals(EndpointType.VALUE, results.get(0).getType());
        assertEquals("medium_stuff", results.get(0).getValue());
        assertEquals("size_result", results.get(0).getResultKey());

        input.put("size", "086");
        assertThat(DecisionEngine.matchTable(matchTable, input))
                .extracting(Endpoint::getType, Endpoint::getValue, Endpoint::getResultKey)
                .containsExactly(tuple(EndpointType.ERROR, "Get that huge stuff out of here!", "size_result"));

        // try with a value not in the table
        input.put("size", "021");
        assertNull(DecisionEngine.matchTable(matchTable, input));
    }

    @Test
    public void testMatchTableWithBlankOrMissingInput() {
        BasicDataProvider provider = new BasicDataProvider();
        BasicTable table = new BasicTable("basic_test_table");
        table.addColumnDefinition("size", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("", "BLANK", "MATCH");
        table.addRawRow("1", "ONE", "MATCH");
        table.addRawRow("2", "TWO", "MATCH");
        provider.addTable(table);

        Table matchTable = provider.getTable("basic_test_table");
        assertNotNull(matchTable);

        // create context of input fields
        Map<String, String> input = new HashMap<>();

        // first try it with missing input (null should match just like blank))
        assertNotNull(DecisionEngine.matchTable(matchTable, input));

        // now add blank input
        input.put("size", "");
        assertNotNull(DecisionEngine.matchTable(matchTable, input));

        // test matching on multiple mising values
        table = new BasicTable("basic_test_table_multi");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("b", ColumnType.INPUT);
        table.addColumnDefinition("c", ColumnType.INPUT);
        table.addColumnDefinition("d", ColumnType.INPUT);
        table.addColumnDefinition("e", ColumnType.INPUT);
        table.addRawRow("1", "", "", "", "");
        table.addRawRow("2", "", "", "", "");
        provider.addTable(table);

        matchTable = provider.getTable("basic_test_table_multi");
        assertNotNull(matchTable);

        // first try it with missing input (null should match just like blank))
        assertNull(DecisionEngine.matchTable(matchTable, new HashMap<>()));

        input.put("a", "2");
        assertNotNull(DecisionEngine.matchTable(matchTable, input));
    }

    @Test
    public void testMatchOnSpecificKeys() {
        BasicDataProvider provider = new BasicDataProvider();
        BasicTable table = new BasicTable("basic_test_table_keytest");
        table.addColumnDefinition("key1", ColumnType.INPUT);
        table.addColumnDefinition("key2", ColumnType.INPUT);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("001-099", "1-3", "MATCH:LINE1");
        table.addRawRow("001-099", "5-9", "MATCH:LINE2");
        table.addRawRow("100,106", "1", "MATCH:LINE3");
        provider.addTable(table);

        Table matchTable = provider.getTable("basic_test_table_keytest");
        assertNotNull(matchTable);

        // create context of input fields
        Map<String, String> input = new HashMap<>();

        // first try it with missing input
        assertNull(DecisionEngine.matchTable(matchTable, input));

        // if searching all keys and only supplying key1, not match will be found
        input.put("key1", "050");
        assertNull(DecisionEngine.matchTable(matchTable, input));

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
    public void testMatchMissingVsAll() {
        BasicDataProvider provider = new BasicDataProvider();
        BasicTable table = new BasicTable("testMissingAndAll");
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
        assertEquals(1, endpoints.size());
        assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        assertEquals("missing", endpoints.get(0).getValue());

        input.put("a", "1");
        endpoints = DecisionEngine.matchTable(tableMissing, input);
        assertEquals(1, endpoints.size());
        assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        assertEquals("all", endpoints.get(0).getValue());
    }

    @Test
    public void testValueKeyReferences() {
        BasicDataProvider provider = new BasicDataProvider();
        BasicTable table = new BasicTable("table_key_references");
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
        BasicSchema def = new BasicSchema("alg_key_references");
        def.addInput("a");
        def.addInput("b");
        def.addMapping(new BasicMapping("m1", Collections.singletonList(new BasicTablePath("table_key_references"))));
        provider.addDefinition(def);
        DecisionEngine engine = new DecisionEngine(provider);

        Map<String, String> input = new HashMap<>();
        input.put("a", "1");
        input.put("b", "B VALUE");
        assertFalse(engine.process("alg_key_references", input).hasErrors());
        assertEquals("b", input.get("result"));

        input.put("a", "2");
        assertFalse(engine.process("alg_key_references", input).hasErrors());
        assertEquals("{b}", input.get("result"));

        input.put("a", "3");
        assertFalse(engine.process("alg_key_references", input).hasErrors());
        assertEquals("{{b", input.get("result"));

        input.put("a", "4");
        assertFalse(engine.process("alg_key_references", input).hasErrors());
        assertEquals("b}}", input.get("result"));

        input.put("a", "10");
        assertFalse(engine.process("alg_key_references", input).hasErrors());
        assertEquals("B VALUE", input.get("result"));

        input.put("a", "11");
        assertFalse(engine.process("alg_key_references", input).hasErrors());
        assertEquals("", input.get("result"));
    }

    @Test
    public void testMatchTableMissingCell() {
        BasicDataProvider provider = new BasicDataProvider();
        BasicTable table = new BasicTable("table_sample_first");
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
        assertNotNull(tableSample);

        // first test with no "a"
        Map<String, String> input = new HashMap<>();
        input.put("b", "55");
        List<? extends Endpoint> endpoints = DecisionEngine.matchTable(tableSample, input);
        assertEquals(1, endpoints.size());
        assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        assertEquals("LINE5", endpoints.get(0).getValue());

        // then test with a random "a"
        input.put("a", "982");
        endpoints = DecisionEngine.matchTable(tableSample, input);
        assertEquals(1, endpoints.size());
        assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        assertEquals("LINE5", endpoints.get(0).getValue());
    }

    @Test
    public void testBlankMatching() {
        BasicDataProvider provider = new BasicDataProvider();
        BasicTable table = new BasicTable("table_blank_matching");
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
        assertEquals(2, endpoints.size());
        assertEquals("LINE1", endpoints.get(0).getValue());
        assertEquals("LINE1", endpoints.get(1).getValue());

        input.clear();
        input.put("a", "NA");
        input.put("b", "99");
        endpoints = DecisionEngine.matchTable(table, input);
        assertEquals(2, endpoints.size());
        assertEquals("LINE2", endpoints.get(0).getValue());
        assertEquals("LINE2", endpoints.get(1).getValue());
    }

    @Test
    public void testLookupTable() {
        BasicDataProvider provider = new BasicDataProvider();
        BasicTable table = new BasicTable("site_table");
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
        assertNotNull(siteTable);

        Map<String, String> input = new HashMap<>();
        input.put("site", "C809");

        // a lookup table in this case has no ENDPOINT column.  In those cases, an endpoint type of MATCH should be returned
        List<? extends Endpoint> endpoints = DecisionEngine.matchTable(siteTable, input);
        assertEquals(0, endpoints.size());
    }

    @Test
    public void testAllValuesMatching() {
        BasicDataProvider provider = new BasicDataProvider();
        BasicTable table = new BasicTable("all_values_test");
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
        assertNotNull(table);

        Map<String, String> input = new HashMap<>();
        input.put("a", "1");
        input.put("b", "3");
        List<? extends Endpoint> endpoints = DecisionEngine.matchTable(allValuesTable, input);
        assertEquals(1, endpoints.size());
        assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        assertEquals("RESULT2", endpoints.get(0).getValue());

        input = new HashMap<>();
        input.put("a", "3");
        endpoints = DecisionEngine.matchTable(allValuesTable, input);
        assertEquals(1, endpoints.size());
        assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        assertEquals("3A,ANY B", endpoints.get(0).getValue());

        input = new HashMap<>();
        input.put("a", "3");
        input.put("b", "9");
        endpoints = DecisionEngine.matchTable(allValuesTable, input);
        assertEquals(1, endpoints.size());
        assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        assertEquals("3A,ANY B", endpoints.get(0).getValue());

        input = new HashMap<>();
        input.put("a", "6");
        input.put("b", "4");
        endpoints = DecisionEngine.matchTable(allValuesTable, input);
        assertEquals(1, endpoints.size());
        assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        assertEquals("ANY A,4B", endpoints.get(0).getValue());

        input = new HashMap<>();
        assertEquals(1, endpoints.size());
        endpoints = DecisionEngine.matchTable(allValuesTable, input);
        assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        assertEquals("CATCHALL", endpoints.get(0).getValue());
    }

    @Test
    public void testMinimumAlgorithm() {
        Schema minSchema = _ENGINE.getProvider().getSchema("starting_min");
        assertNotNull(minSchema);
        assertEquals("starting_min", minSchema.getId());
        assertNotNull(minSchema.getInitialContext());

        Map<String, String> input = new HashMap<>();
        Result result = _ENGINE.process(minSchema, input);

        assertFalse(result.hasErrors());
        assertEquals("bar", input.get("foo"));
    }

    @Test
    public void testAlgorithm() {
        Schema starting = _ENGINE.getProvider().getSchema("starting_sample");
        assertNotNull(starting);
        assertEquals("starting_sample", starting.getId());
        assertNotNull(starting.getInitialContext());
    }

    @Test
    public void testMissingParameters() {
        Map<String, String> input = new HashMap<>();
        Result result = _ENGINE.process("starting_sample", input);

        assertTrue(result.hasErrors());

        // even though there were no inputs, both tables were still processed
        assertEquals(2, result.getPath().size());
        assertEquals("m1.table_sample_first", result.getPath().get(0));
        assertEquals("m1.table_sample_second", result.getPath().get(1));
    }

    @Test
    public void testParameterLookupValidation() {
        Map<String, String> input = new HashMap<>();
        input.put("a", "3");
        input.put("b", "31");  // value is not in lookup table

        Result result = _ENGINE.process("starting_sample", input);

        assertEquals(Type.FAILED_INPUT, result.getType());

        // since input "b" is fail_on_invalud, table processing should not continue
        assertEquals(0, result.getPath().size());

        // one error for input, and one error each of the two tables because of no match
        assertEquals(1, result.getErrors().size());

        // make "b" a valid value
        input.put("b", "30");

        result = _ENGINE.process("starting_sample", input);

        assertEquals(Type.STAGED, result.getType());

        // one error for input, and one error each of the two tables because of no match
        assertEquals(2, result.getErrors().size());
    }

    @Test
    public void testSingleTableProcess() {
        Map<String, String> input = new HashMap<>();
        input.put("a", "7");
        input.put("b", "03");  // should map to "hemeretic" without using second table
        input.put("e", "X");
        Result result = _ENGINE.process("starting_sample", input);

        assertFalse(result.hasErrors());
        assertEquals(2, result.getPath().size());

        assertEquals("LINE1", input.get("result"));
        assertEquals("HARD-CODE", input.get("d"));
    }

    @Test
    public void testOneJumpProcess() {
        Map<String, String> input = new HashMap<>();
        input.put("a", "5");
        input.put("b", "20");
        input.put("c", "A");
        input.put("e", "X");
        Result result = _ENGINE.process("starting_sample", input);

        assertFalse(result.hasErrors());
        assertEquals(3, result.getPath().size());

        assertEquals("A", input.get("result"));

        // now test an error line in the jump table
        input = new HashMap<>();
        input.put("a", "5");
        input.put("b", "20");
        input.put("c", "C");
        input.put("e", "X");

        result = _ENGINE.process("starting_sample", input);

        assertTrue(result.hasErrors());
        assertEquals(3, result.getPath().size());
        assertEquals(1, result.getErrors().size());
        assertEquals("Bad C value", result.getErrors().get(0).getMessage());
        assertNull(result.getErrors().get(0).getKey());
        assertEquals("table_jump_sample", result.getErrors().get(0).getTable());

        // finally test that no match is found in the jump table
        input = new HashMap<>();
        input.put("a", "5");
        input.put("b", "20");
        input.put("c", "D");
        input.put("e", "X");

        result = _ENGINE.process("starting_sample", input);

        assertTrue(result.hasErrors());
        assertEquals(3, result.getPath().size());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).getMessage().startsWith("Match not found"));
        assertNull(result.getErrors().get(0).getKey());
        assertEquals("table_jump_sample", result.getErrors().get(0).getTable());
    }

    @Test
    public void testProcessError() {
        Map<String, String> input = new HashMap<>();
        input.put("a", "9");
        input.put("b", "99");
        input.put("e", "X");
        Result result = _ENGINE.process("starting_sample", input);

        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals(2, result.getPath().size());
        assertEquals("999", result.getErrors().get(0).getMessage());
        assertNull(result.getErrors().get(0).getKey());
        assertEquals("table_sample_first", result.getErrors().get(0).getTable());
        assertEquals(Collections.singletonList("result"), result.getErrors().get(0).getColumns());

        // test case with generated error message (i.e. the column is "ERROR:" without a message
        input.put("a", "8");
        input.put("b", "99");
        input.put("e", "X");
        result = _ENGINE.process("starting_sample", input);
        assertEquals(1, result.getErrors().size());
        assertEquals(2, result.getPath().size());
        assertEquals("Matching resulted in an error in table 'table_sample_first' for column 'result' (8,99)", result.getErrors().get(0).getMessage());
        assertNull(result.getErrors().get(0).getKey());
        assertEquals("table_sample_first", result.getErrors().get(0).getTable());
        assertEquals(Collections.singletonList("result"), result.getErrors().get(0).getColumns());
    }

    @Test
    public void testProcessWithNullValues() {
        BasicDataProvider provider = new BasicDataProvider();

        BasicTable table = new BasicTable("table_null_values");
        table.addColumnDefinition("a", ColumnType.INPUT);
        table.addColumnDefinition("result", ColumnType.ENDPOINT);
        table.addRawRow("1", "VALUE:FOUND1");
        table.addRawRow("2", "VALUE");
        table.addRawRow("3", "VALUE:");
        table.addRawRow("*", "MATCH");
        provider.addTable(table);

        BasicSchema def = new BasicSchema("starting_null_values");
        BasicInput inputKey = new BasicInput("a");
        inputKey.setDefault("0");
        def.addInput(inputKey);
        def.addInitialContext("result", "0");
        def.addMapping(new BasicMapping("m1", Collections.singletonList(new BasicTablePath("table_null_values"))));
        provider.addDefinition(def);
        DecisionEngine engine = new DecisionEngine(provider);

        Map<String, String> input = new HashMap<>();
        Result result = engine.process("starting_null_values", input);

        assertFalse(result.hasErrors());
        assertEquals("0", input.get("result"));

        input.clear();
        input.put("a", "1");
        result = engine.process("starting_null_values", input);
        assertFalse(result.hasErrors());
        assertEquals("FOUND1", input.get("result"));

        input.clear();
        input.put("a", "2");
        result = engine.process("starting_null_values", input);
        assertFalse(result.hasErrors());
        assertFalse(input.containsKey("result"));

        input.clear();
        input.put("a", "3");
        result = engine.process("starting_null_values", input);
        assertFalse(result.hasErrors());
        assertEquals("", input.get("result"));
    }

    @Test
    public void testProcessWithStop() {
        // first test that we get a result from the second table
        Map<String, String> input = new HashMap<>();
        input.put("a", "5");
        input.put("b", "20");
        input.put("c", "A");
        input.put("e", "X");
        Result result = _ENGINE.process("starting_sample", input);

        assertFalse(result.hasErrors());
        assertEquals(3, result.getPath().size());

        assertEquals("A", input.get("result"));
        assertEquals("LINE1", input.get("shared_result"));

        // next text then when STOP is encountered, the second table is not processed
        input.clear();
        input.put("a", "5");
        input.put("b", "20");
        input.put("c", "Z");
        input.put("e", "X");
        result = _ENGINE.process("starting_sample", input);

        assertFalse(result.hasErrors());
        assertEquals(2, result.getPath().size());

        assertFalse(input.containsKey("result"));
        assertFalse(input.containsKey("shared_result"));
    }

    @Test
    public void testProcessWithBlanks() {
        Map<String, String> input = new HashMap<>();
        input.put("x", "1");
        input.put("y", "");
        Result result = _ENGINE.process("starting_blank", input);

        assertFalse(result.hasErrors());
        assertEquals(1, result.getPath().size());

        assertEquals("LINE1", input.get("y"));
        assertEquals("LINE1", input.get("z"));

        // verify that context blanks are trimmed
        input.put("x", "1");
        input.put("y", "  ");
        result = _ENGINE.process("starting_blank", input);

        assertFalse(result.hasErrors());
        assertEquals(1, result.getPath().size());

        assertEquals("LINE1", input.get("y"));
        assertEquals("LINE1", input.get("z"));
    }

    @Test
    public void testProcessWithDoubleInputMapping() {
        Map<String, String> input = new HashMap<>();
        input.put("x", "1");
        Result result = _ENGINE.process("starting_double_input", input);

        assertFalse(result.hasErrors());
        assertEquals(1, result.getPath().size());

        assertEquals("LINE3", input.get("result"));
    }

    @Test
    public void testProcessWithDoubleOutputMapping() {
        Map<String, String> input = new HashMap<>();
        input.put("a", "1");
        input.put("b", "00");
        Result result = _ENGINE.process("starting_double_output", input);

        assertFalse(result.hasErrors());
        assertEquals(1, result.getPath().size());

        assertNull(input.get("result"));
        assertEquals("LINE1", input.get("output1"));
        assertEquals("LINE1", input.get("output2"));
    }

    @Test
    public void testInvolvedAlgorithms() {
        List<Mapping> mappings;
        Map<String, String> context = new HashMap<>();

        Schema schema = _ENGINE.getProvider().getSchema("starting_min");
        mappings = _ENGINE.getInvolvedMappings(schema, context);
        assertEquals(0, mappings.size());

        schema = _ENGINE.getProvider().getSchema("starting_inclusions");

        mappings = _ENGINE.getInvolvedMappings(schema, context);
        assertEquals(1, mappings.size());

        context.put("a", "1");
        mappings = _ENGINE.getInvolvedMappings(schema, context);
        assertEquals(1, mappings.size());

        context.put("a", "2");
        mappings = _ENGINE.getInvolvedMappings(schema, context);
        assertEquals(2, mappings.size());
    }

    @Test
    public void testInvolvedTables() {
        Set<String> tables;

        // test a case with no involved tables
        tables = _ENGINE.getInvolvedTables("starting_min");
        assertEquals(0, tables.size());

        // test a case with a single table with one jump
        tables = _ENGINE.getInvolvedTables("starting_sample");
        assertEquals(4, tables.size());
        assertTrue(tables.contains("table_lookup_sample"));
        assertTrue(tables.contains("table_sample_first"));
        assertTrue(tables.contains("table_sample_second"));
        assertTrue(tables.contains("table_jump_sample"));

        // test a case with inclusion/exclusion tables
        tables = _ENGINE.getInvolvedTables("starting_inclusions");
        assertEquals(5, tables.size());
        assertTrue(tables.contains("table_part1"));
        assertTrue(tables.contains("table_part2"));
        assertTrue(tables.contains("table_inclusion1"));
        assertTrue(tables.contains("table_inclusion2"));
        assertTrue(tables.contains("table_exclusion1"));
    }

    @Test
    public void testInvolvedTablesWhenEmpty() {
        BasicDataProvider provider = new BasicDataProvider();

        // add empty table
        BasicTable table = new BasicTable("table1");
        table.addColumnDefinition("a", ColumnType.INPUT);
        provider.addTable(table);

        // add another empty table
        table = new BasicTable("table2");
        table.addColumnDefinition("b", ColumnType.INPUT);
        provider.addTable(table);

        BasicSchema def = new BasicSchema("def1");
        def.setOnInvalidInput(Schema.StagingInputErrorHandler.FAIL);
        def.addInput("a");
        def.addInput("b");
        BasicMapping mapping = new BasicMapping("m1");
        mapping.setTablePaths(Arrays.asList(new BasicTablePath("table1"), new BasicTablePath("table2")));
        def.addMapping(mapping);
        provider.addDefinition(def);

        DecisionEngine engine = new DecisionEngine(provider);

        Set<String> tables = engine.getInvolvedTables("def1");
        assertEquals(2, tables.size());
        assertTrue(tables.contains("table1"));
        assertTrue(tables.contains("table2"));
    }

    @Test
    public void testInvolvedTableRecursion() {
        Set<String> tables = _ENGINE.getInvolvedTables("starting_recursion");

        assertEquals(1, tables.size());
        assertTrue(tables.contains("table_recursion"));
    }

    @Test
    public void testProcessWithRecursion() {
        Map<String, String> input = new HashMap<>();
        input.put("a", "4");
        Result result = _ENGINE.process("starting_recursion", input);

        assertEquals(Type.STAGED, result.getType());
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals(1, result.getPath().size());
        assertEquals("table_recursion", result.getErrors().get(0).getTable());
        assertNull(result.getErrors().get(0).getColumns());
    }

    @Test
    public void testProcessWithMultipleEndpoints() {
        // first test all 3 results get set
        Map<String, String> input = new HashMap<>();
        input.put("a", "2");
        input.put("b", "12");
        Result result = _ENGINE.process("starting_multiple_endpoints", input);

        assertFalse(result.hasErrors());
        assertEquals(Type.STAGED, result.getType());
        assertEquals(1, result.getPath().size());

        assertEquals("1_LINE1", input.get("r1"));
        assertEquals("2_LINE1", input.get("r2"));
        assertEquals("3_LINE1", input.get("r3"));

        // test 2 VALUEs and an ERROR
        input.clear();
        input.put("a", "0");
        input.put("b", "25");
        result = _ENGINE.process("starting_multiple_endpoints", input);
        assertEquals(Type.STAGED, result.getType());
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals("table_multiple_inputs", result.getErrors().get(0).getTable());
        assertEquals(Collections.singletonList("r2"), result.getErrors().get(0).getColumns());
        assertEquals("2_LINE2", result.getErrors().get(0).getMessage());
        assertEquals(1, result.getPath().size());

        assertEquals("1_LINE2", input.get("r1"));
        assertFalse(input.containsKey("r2"));
        assertEquals("3_LINE2", input.get("r3"));

        // test 2 JUMPs and one VALUE and a missing jump table value
        input.clear();
        input.put("a", "5");
        input.put("b", "20");
        result = _ENGINE.process("starting_multiple_endpoints", input);

        assertEquals(Type.STAGED, result.getType());
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().get(0).getMessage().startsWith("Match not found"));
        assertNull(result.getErrors().get(0).getKey());
        assertEquals("table_jump_sample", result.getErrors().get(0).getTable());
        assertEquals(Collections.singletonList("result"), result.getErrors().get(0).getColumns());

        // test 1 JUMP and 2 VALUEs
        input.clear();
        input.put("a", "5");
        input.put("b", "20");
        input.put("c", "A");
        result = _ENGINE.process("starting_multiple_endpoints", input);

        assertEquals(Type.STAGED, result.getType());
        assertFalse(result.hasErrors());
        assertFalse(input.containsKey("r1"));
        assertEquals("A", input.get("result"));
        assertEquals("2_LINE3", input.get("r2"));
        assertEquals("3_LINE3", input.get("r3"));

        // test 3 ERRORs
        input.clear();
        input.put("a", "9");
        input.put("b", "99");
        result = _ENGINE.process("starting_multiple_endpoints", input);

        assertEquals(Type.STAGED, result.getType());
        assertTrue(result.hasErrors());
        assertEquals(3, result.getErrors().size());
        assertEquals("1_LINE4", result.getErrors().get(0).getMessage());
        assertNull(result.getErrors().get(0).getKey());
        assertEquals("2_LINE4", result.getErrors().get(1).getMessage());
        assertNull(result.getErrors().get(1).getKey());
        assertEquals("3_LINE4", result.getErrors().get(2).getMessage());
        assertNull(result.getErrors().get(2).getKey());
        assertFalse(input.containsKey("r1"));
        assertFalse(input.containsKey("r2"));
        assertFalse(input.containsKey("r3"));
    }

    @Test
    public void testRowNotFoundWithMultipleOutputs() {
        BasicDataProvider provider = new BasicDataProvider();

        // test a situation where a a row with mulitple inputs is not found
        BasicTable table = new BasicTable("table_input");
        table.addColumnDefinition("input1", ColumnType.INPUT);
        table.addColumnDefinition("output1", ColumnType.ENDPOINT);
        table.addColumnDefinition("output2", ColumnType.ENDPOINT);
        table.addRawRow("000", "VALUE:000", "VALUE:000");
        table.addRawRow("001", "VALUE:001", "VALUE:001");
        provider.addTable(table);

        BasicSchema def = new BasicSchema("sample_outputs");
        def.setOnInvalidInput(Schema.StagingInputErrorHandler.FAIL);
        def.addInput(new BasicInput("input1", "table_input"));

        BasicMapping mapping = new BasicMapping("mapping1");
        BasicTablePath path = new BasicTablePath("table_input");
        mapping.addTablePath(path);
        def.addMapping(mapping);
        provider.addDefinition(def);

        DecisionEngine engine = new DecisionEngine(provider);

        // test match not found
        Map<String, String> input = new HashMap<>();
        input.put("a", "4");
        input.put("b", "55");
        Result result = engine.process("sample_outputs", input);
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals("table_input", result.getErrors().get(0).getTable());
        assertEquals(Arrays.asList("output1", "output2"), result.getErrors().get(0).getColumns());
    }

    @Test
    public void testMultipleEndpointWithoutRecursion() {
        // test 2 JUMPs to same table and one value; this is not infinite recursion but since same table called twice it gets confused
        Map<String, String> input = new HashMap<>();
        input.put("a", "4");
        input.put("b", "44");
        input.put("c", "A");
        Result result = _ENGINE.process("starting_multiple_endpoints", input);

        assertEquals(Type.STAGED, result.getType());
        assertFalse(result.hasErrors());
        assertFalse(input.containsKey("r1"));
        assertFalse(input.containsKey("r3"));
        assertEquals("A", input.get("result"));
        assertEquals("2_LINE5", input.get("r2"));
    }

    @Test
    public void testInclusionsAndExclusions() {
        Map<String, String> input = new HashMap<>();
        input.put("a", "1");
        input.put("b", "2");
        input.put("c", "3");
        Result result = _ENGINE.process("starting_inclusions", input);

        assertEquals(Type.STAGED, result.getType());
        assertFalse(result.hasErrors());
        assertEquals("2", input.get("result"));
        assertFalse(input.containsKey("special"));

        input.clear();
        input.put("a", "8");
        input.put("b", "2");
        input.put("c", "3");
        result = _ENGINE.process("starting_inclusions", input);

        assertEquals(Type.STAGED, result.getType());
        assertFalse(result.hasErrors());
        assertEquals("3", input.get("result"));
        assertEquals("SUCCESS", input.get("special"));

        input.clear();
        input.put("a", "9");
        input.put("b", "2");
        input.put("c", "3");
        result = _ENGINE.process("starting_inclusions", input);

        assertEquals(Type.STAGED, result.getType());
        assertFalse(result.hasErrors());
        assertFalse(input.containsKey("result"));
        assertEquals("SUCCESS", input.get("special"));
    }

    @Test(expected = IllegalStateException.class)
    public void testDuplicateAlgorithms() {
        BasicDataProvider provider = new BasicDataProvider();
        BasicSchema def = new BasicSchema();
        def.setId("TEST1");
        provider.addDefinition(def);
        provider.addDefinition(def);
    }

    /**
     * Shortcut for constructing a Set using a List
     *
     * @param values list of values
     * @return Set of values
     */
    private Set<String> asSet(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    @Test
    public void testSchemaInputs() {
        DataProvider provider = _ENGINE.getProvider();

        assertEquals(asSet(), _ENGINE.getInputs(provider.getSchema("starting_min")));
        assertEquals(asSet("a", "b", "c", "e"), _ENGINE.getInputs(provider.getSchema("starting_sample")));
        assertEquals(asSet("a", "b", "c"), _ENGINE.getInputs(provider.getSchema("starting_inclusions")));
        assertEquals(asSet("a"), _ENGINE.getInputs(provider.getSchema("starting_recursion")));
        assertEquals(asSet("a", "b", "c"), _ENGINE.getInputs(provider.getSchema("starting_multiple_endpoints")));
        assertEquals(asSet("b", "not_in_input_list"), _ENGINE.getInputs(provider.getSchema("starting_inclusions_extra_inputs")));
        assertEquals(asSet("main_input"), _ENGINE.getInputs(provider.getSchema("starting_intermediate_values")));
    }

    @Test
    public void testGetSchemaOutputs() {
        DataProvider provider = _ENGINE.getProvider();

        assertEquals(asSet(), _ENGINE.getOutputs(provider.getSchema("starting_min")));
        assertEquals(asSet("result", "shared_result"), _ENGINE.getOutputs(provider.getSchema("starting_sample")));
        assertEquals(asSet("result", "special"), _ENGINE.getOutputs(provider.getSchema("starting_inclusions")));
        assertEquals(asSet("result"), _ENGINE.getOutputs(provider.getSchema("starting_recursion")));
        assertEquals(asSet("result", "r1", "r2", "r3"), _ENGINE.getOutputs(provider.getSchema("starting_multiple_endpoints")));
        assertEquals(asSet("mapped_result"), _ENGINE.getOutputs(provider.getSchema("starting_inclusions_extra_inputs")));
        assertEquals(asSet("intermediate_output", "final_output"), _ENGINE.getOutputs(provider.getSchema("starting_intermediate_values")));
    }

    @Test
    public void testGetTableInputsAsString() {
        BasicTable table = new BasicTable("table_inputs");
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

        assertEquals(DecisionEngine._BLANK_OUTPUT + "," + DecisionEngine._BLANK_OUTPUT, DecisionEngine.getTableInputsAsString(table, context));

        context.put("b", "25");
        assertEquals(DecisionEngine._BLANK_OUTPUT + ",25", DecisionEngine.getTableInputsAsString(table, context));

        context.put("a", "7");
        assertEquals("7,25", DecisionEngine.getTableInputsAsString(table, context));
        context.put("a", "    7");
        assertEquals("7,25", DecisionEngine.getTableInputsAsString(table, context));
        context.put("a", "7    ");
        assertEquals("7,25", DecisionEngine.getTableInputsAsString(table, context));

        table = new BasicTable("table_empty");
        context = new HashMap<>();
        assertEquals("", DecisionEngine.getTableInputsAsString(table, context));
    }

    @Test
    public void testOutputsAndDefaults() {
        BasicDataProvider provider = new BasicDataProvider();

        BasicTable table = new BasicTable("table_input");
        table.addColumnDefinition("input1", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addRawRow("000", "Alpha");
        table.addRawRow("001", "Beta");
        table.addRawRow("002", "Gamma");
        provider.addTable(table);

        table = new BasicTable("table_output");
        table.addColumnDefinition("output1", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addRawRow("A", "Alpha");
        table.addRawRow("B", "Beta");
        table.addRawRow("C", "Gamma");
        provider.addTable(table);

        BasicSchema def = new BasicSchema("sample_outputs");
        def.setOnInvalidInput(Schema.StagingInputErrorHandler.FAIL);
        def.addInput(new BasicInput("input1", "table_input"));

        BasicOutput output = new BasicOutput("output1", "table_output");
        output.setDefault("A");
        def.addOutput(output);

        output = new BasicOutput("output2");
        def.addOutput(output);

        BasicMapping mapping = new BasicMapping("mapping1");
        def.addMapping(mapping);
        provider.addDefinition(def);

        DecisionEngine engine = new DecisionEngine(provider);

        Map<String, String> input = new HashMap<>();
        input.put("input1", "000");
        Result result = engine.process("sample_outputs", input);

        assertEquals(Type.STAGED, result.getType());

        // default value should be set
        assertEquals("A", input.get("output1"));
        // no default value so it should be blank
        assertEquals("", input.get("output2"));

        assertFalse(result.hasErrors());

        assertEquals(new HashSet<>(Arrays.asList("table_input", "table_output")), engine.getInvolvedTables(def));

        // modify the definition to create a bad default value for output1
        def.getOutputs().get(0).setDefault("BAD");
        provider.initDefinition(def);
        engine = new DecisionEngine(provider);

        input = new HashMap<>();
        input.put("input1", "000");
        result = engine.process("sample_outputs", input);

        assertEquals(Type.STAGED, result.getType());
        assertTrue(result.hasErrors());
        assertEquals(Error.Type.INVALID_OUTPUT, result.getErrors().get(0).getType());
        assertEquals("table_output", result.getErrors().get(0).getTable());
        assertEquals("output1", result.getErrors().get(0).getKey());

        // default value should be set
        assertEquals("BAD", input.get("output1"));
        // no default value so it should be blank
        assertEquals("", input.get("output2"));
    }

    @Test
    public void testInitialContextReferences() {
        BasicSchema def = new BasicSchema("test_initial_context");
        def.setOnInvalidInput(Schema.StagingInputErrorHandler.FAIL);

        def.addInitialContext("a", "foo1");
        def.addInitialContext("b", "{{foo1}}");
        def.addInitialContext("c", "foo2");
        def.addInitialContext("d", "{{foo2}}");
        def.addInitialContext("e", "{{bad_key}}");

        BasicDataProvider provider = new BasicDataProvider();
        provider.addDefinition(def);
        DecisionEngine engine = new DecisionEngine(provider);

        Map<String, String> context = new HashMap<>();
        context.put("foo1", "FIRST");
        context.put("foo2", "SECOND");
        Result result = engine.process("test_initial_context", context);

        assertEquals(Type.STAGED, result.getType());

        assertEquals(context.get("a"), "foo1");
        assertEquals(context.get("b"), "FIRST");
        assertEquals(context.get("c"), "foo2");
        assertEquals(context.get("d"), "SECOND");
        assertEquals(context.get("e"), "");
    }

    @Test
    public void testInputsOutputsDefaultContextReferences() {
        BasicSchema def = new BasicSchema("test_context");
        def.setOnInvalidInput(Schema.StagingInputErrorHandler.FAIL);

        // add inputs
        BasicInput input = new BasicInput("input1");
        input.setDefault("foo1");
        def.addInput(input);

        input = new BasicInput("input2");
        input.setDefault("{{foo1}}");
        def.addInput(input);

        // add outputs
        BasicOutput output = new BasicOutput("output1");
        output.setDefault("foo2");
        def.addOutput(output);

        output = new BasicOutput("output2");
        output.setDefault("{{foo2}}");
        def.addOutput(output);

        output = new BasicOutput("output3");
        output.setDefault("{{bad_key}}");
        def.addOutput(output);

        output = new BasicOutput("output4");
        output.setDefault("{{input1}}");
        def.addOutput(output);

        output = new BasicOutput("output5");
        output.setDefault("{{input2}}");
        def.addOutput(output);

        BasicDataProvider provider = new BasicDataProvider();
        provider.addDefinition(def);
        DecisionEngine engine = new DecisionEngine(provider);

        Map<String, String> context = new HashMap<>();
        context.put("foo1", "FIRST");
        context.put("foo2", "SECOND");
        Result result = engine.process("test_context", context);

        assertEquals(Type.STAGED, result.getType());

        assertEquals(context.get("output1"), "foo2");
        assertEquals(context.get("output2"), "SECOND");
        assertEquals(context.get("output3"), "");
        assertEquals(context.get("output4"), "foo1");
        assertEquals(context.get("output5"), "FIRST");
    }

    @Test
    public void testMappingInputsWithReferenceInTable() {
        BasicDataProvider provider = new BasicDataProvider();

        // test a situation where an input is also used as a reference in an endpoint; when the definition remaps that input it should not show up
        // as both values
        BasicTable table = new BasicTable("table_input");
        table.addColumnDefinition("input1", ColumnType.INPUT);
        table.addColumnDefinition("output1", ColumnType.ENDPOINT);
        table.addRawRow("000", "VALUE:001");
        table.addRawRow("001", "VALUE:000");
        table.addRawRow("002", "VALUE:{{input1}}");
        provider.addTable(table);

        BasicSchema def = new BasicSchema("sample_outputs");
        def.setOnInvalidInput(Schema.StagingInputErrorHandler.FAIL);
        def.addInput(new BasicInput("input1", "table_input"));

        BasicMapping mapping = new BasicMapping("mapping1");
        BasicTablePath path = new BasicTablePath("table_input");
        path.addInputMapping("remapped1", "input1");
        mapping.addTablePath(path);
        def.addMapping(mapping);
        provider.addDefinition(def);

        DecisionEngine engine = new DecisionEngine(provider);

        assertEquals(new HashSet<>(Collections.singletonList("remapped1")), engine.getInputs(def.getMappings().get(0).getTablePaths().get(0)));
    }

}
