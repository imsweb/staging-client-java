/*

 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import com.google.common.collect.Sets;

import com.imsweb.decisionengine.ColumnDefinition.ColumnType;
import com.imsweb.decisionengine.Endpoint.EndpointType;
import com.imsweb.decisionengine.Result.Type;
import com.imsweb.decisionengine.basic.BasicDataProvider;
import com.imsweb.decisionengine.basic.BasicDefinition;
import com.imsweb.decisionengine.basic.BasicEndpoint;
import com.imsweb.decisionengine.basic.BasicInput;
import com.imsweb.decisionengine.basic.BasicMapping;
import com.imsweb.decisionengine.basic.BasicStringRange;
import com.imsweb.decisionengine.basic.BasicTable;
import com.imsweb.decisionengine.basic.BasicTablePath;

/**
 * Test class for DecisionEngine
 */
public class DecisionEngineTest {

    private static DecisionEngine _ENGINE;

    @BeforeClass
    public static void init() throws IOException {
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

        BasicDefinition def = new BasicDefinition("starting_sample");
        def.setOnInvalidInput(Definition.StagingInputErrorHandler.FAIL);
        def.addInput("a");
        BasicInput input = new BasicInput("b", "table_lookup_sample");
        def.addInput(input);
        def.addInput("c");
        def.addInitialContext("d", "HARD-CODE");
        BasicMapping mapping = new BasicMapping("m1");
        mapping.setTablePaths(Arrays.asList(new BasicTablePath("table_sample_first"), new BasicTablePath("table_sample_second")));
        def.addMapping(mapping);
        provider.addDefinition(def);

        def = new BasicDefinition("starting_min");
        def.addInitialContext("foo", "bar");
        provider.addDefinition(def);

        def = new BasicDefinition("starting_multiple_endpoints");
        def.addInput("a");
        def.addInput("b");
        def.addInput("c");
        def.addMapping(new BasicMapping("m1", Collections.singletonList(new BasicTablePath("table_multiple_inputs"))));
        provider.addDefinition(def);

        def = new BasicDefinition("starting_recursion");
        def.addInput("a");
        def.addMapping(new BasicMapping("m1", Collections.singletonList(new BasicTablePath("table_recursion"))));
        provider.addDefinition(def);

        def = new BasicDefinition("starting_inclusions");
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

        def = new BasicDefinition("starting_intermediate_values");
        def.addInput("main_input");
        mapping = new BasicMapping("m1");
        mapping.addTablePath(new BasicTablePath("table_create_intermediate"));
        mapping.addTablePath(new BasicTablePath("table_use_intermediate"));
        def.addMapping(mapping);
        provider.addDefinition(def);

        def = new BasicDefinition("starting_inclusions_extra_inputs");
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

        def = new BasicDefinition("starting_blank");
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

        def = new BasicDefinition("starting_double_input");
        def.addInput("x");
        mapping = new BasicMapping("m1");
        path = new BasicTablePath("table_multiple_input");
        path.addInputMapping("x", "a");
        path.addInputMapping("x", "b");
        mapping.addTablePath(path);
        def.addMapping(mapping);
        provider.addDefinition(def);

        def = new BasicDefinition("starting_double_output");
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
        List<StringRange> range = new ArrayList<StringRange>();
        range.add(new BasicStringRange("1", "1"));
        range.add(new BasicStringRange("4", "4"));
        range.add(new BasicStringRange("9", "9"));
        Assert.assertTrue(DecisionEngine.testMatch(range, "9", new HashMap<String, String>()));
        Assert.assertFalse(DecisionEngine.testMatch(range, "7", new HashMap<String, String>()));

        range = new ArrayList<StringRange>();
        range.add(new BasicStringRange("11", "54"));
        range.add(new BasicStringRange("99", "99"));
        Assert.assertTrue(DecisionEngine.testMatch(range, "23", new HashMap<String, String>()));
    }

    @Test
    public void testEmptyTable() throws IOException {
        BasicDataProvider provider = new BasicDataProvider();
        BasicTable table = new BasicTable("basic_test_table");
        table.addColumnDefinition("size", ColumnType.INPUT);
        table.addColumnDefinition("description", ColumnType.DESCRIPTION);
        table.addColumnDefinition("size_result", ColumnType.ENDPOINT);
        provider.addTable(table);

        Assert.assertTrue(provider.getTable("basic_test_table").getRawRows().isEmpty());
        Assert.assertTrue(provider.getTable("basic_test_table").getTableRows().isEmpty());
    }

    @Test
    public void testMatchTable() throws IOException {
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
        Assert.assertNotNull(matchTable);

        // create context of input fields
        Map<String, String> input = new HashMap<String, String>();

        // first try it with missing input
        Assert.assertNull(DecisionEngine.matchTable(matchTable, input));

        input.put("size", "003");
        ReflectionAssert.assertReflectionEquals(Collections.singletonList(new BasicEndpoint(EndpointType.JUMP, "some_crazy_table")), DecisionEngine.matchTable(matchTable, input));

        input.put("size", "014");
        List<? extends Endpoint> results = DecisionEngine.matchTable(matchTable, input);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(EndpointType.VALUE, results.get(0).getType());
        Assert.assertEquals("medium_stuff", results.get(0).getValue());
        Assert.assertEquals("size_result", results.get(0).getResultKey());

        input.put("size", "086");
        ReflectionAssert.assertReflectionEquals(Collections.singletonList(new BasicEndpoint(EndpointType.ERROR, "Get that huge stuff out of here!")), DecisionEngine.matchTable(matchTable, input));

        // try with a value not in the table
        input.put("size", "021");
        Assert.assertNull(DecisionEngine.matchTable(matchTable, input));
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
        Assert.assertNotNull(matchTable);

        // create context of input fields
        Map<String, String> input = new HashMap<String, String>();

        // first try it with missing input
        Assert.assertNull(DecisionEngine.matchTable(matchTable, input));

        // if searching all keys and only supplying key1, not match will be found
        input.put("key1", "050");
        Assert.assertNull(DecisionEngine.matchTable(matchTable, input));

        // specify to only match on key1, there should be a match to the first line
        ReflectionAssert.assertReflectionEquals(Collections.singletonList(new BasicEndpoint(EndpointType.MATCH, "LINE1")), DecisionEngine.matchTable(matchTable, input, Sets.newHashSet("key1")));

        // add key2 to the input map and there should be a match
        input.put("key2", "7");
        ReflectionAssert.assertReflectionEquals(Collections.singletonList(new BasicEndpoint(EndpointType.MATCH, "LINE2")), DecisionEngine.matchTable(matchTable, input));

        // if searching on key1 only, even though key2 was supplied should still match to first line
        ReflectionAssert.assertReflectionEquals(Collections.singletonList(new BasicEndpoint(EndpointType.MATCH, "LINE1")), DecisionEngine.matchTable(matchTable, input, Sets.newHashSet("key1")));

        // supply an empty set of keys (the same meaning as not passing any keys
        ReflectionAssert.assertReflectionEquals(Collections.singletonList(new BasicEndpoint(EndpointType.MATCH, "LINE1")), DecisionEngine.matchTable(matchTable, input, new HashSet<String>()));

        // supply an invalid key.  I think this should find nothing, but for the moment finds a match to the first row since none of the cells were compared to.  It
        // is the same as matching to a table with no INPUTS which would currently find a match to the first row.
        ReflectionAssert.assertReflectionEquals(Collections.singletonList(new BasicEndpoint(EndpointType.MATCH, "LINE1")), DecisionEngine.matchTable(matchTable, input, Sets.newHashSet("bad_key")));
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
        Map<String, String> input = new HashMap<String, String>();
        input.put("a", "");
        endpoints = DecisionEngine.matchTable(tableMissing, input);
        Assert.assertEquals(1, endpoints.size());
        Assert.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        Assert.assertEquals("missing", endpoints.get(0).getValue());

        //        input.put("a", " ");
        //        endpoints = DecisionEngine.matchTable(tableMissing, input);
        //        Assert.assertEquals(1, endpoints.size());
        //        Assert.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        //        Assert.assertEquals("space", endpoints.get(0).getValue());

        input.put("a", "1");
        endpoints = DecisionEngine.matchTable(tableMissing, input);
        Assert.assertEquals(1, endpoints.size());
        Assert.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        Assert.assertEquals("all", endpoints.get(0).getValue());
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
        BasicDefinition def = new BasicDefinition("alg_key_references");
        def.addInput("a");
        def.addInput("b");
        def.addMapping(new BasicMapping("m1", Collections.singletonList(new BasicTablePath("table_key_references"))));
        provider.addDefinition(def);
        DecisionEngine engine = new DecisionEngine(provider);

        Map<String, String> input = new HashMap<String, String>();
        input.put("a", "1");
        input.put("b", "B VALUE");
        Assert.assertFalse(engine.process("alg_key_references", input).hasErrors());
        Assert.assertEquals("b", input.get("result"));

        input.put("a", "2");
        Assert.assertFalse(engine.process("alg_key_references", input).hasErrors());
        Assert.assertEquals("{b}", input.get("result"));

        input.put("a", "3");
        Assert.assertFalse(engine.process("alg_key_references", input).hasErrors());
        Assert.assertEquals("{{b", input.get("result"));

        input.put("a", "4");
        Assert.assertFalse(engine.process("alg_key_references", input).hasErrors());
        Assert.assertEquals("b}}", input.get("result"));

        input.put("a", "10");
        Assert.assertFalse(engine.process("alg_key_references", input).hasErrors());
        Assert.assertEquals("B VALUE", input.get("result"));

        input.put("a", "11");
        Assert.assertFalse(engine.process("alg_key_references", input).hasErrors());
        Assert.assertEquals("", input.get("result"));
    }

    @Test
    public void testMatchTableMissingCell() throws IOException {
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
        Assert.assertNotNull(tableSample);

        // first test with no "a"
        Map<String, String> input = new HashMap<String, String>();
        input.put("b", "55");
        List<? extends Endpoint> endpoints = DecisionEngine.matchTable(tableSample, input);
        Assert.assertEquals(1, endpoints.size());
        Assert.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        Assert.assertEquals("LINE5", endpoints.get(0).getValue());

        // then test with a random "a"
        input.put("a", "982");
        endpoints = DecisionEngine.matchTable(tableSample, input);
        Assert.assertEquals(1, endpoints.size());
        Assert.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        Assert.assertEquals("LINE5", endpoints.get(0).getValue());
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

        Map<String, String> input = new HashMap<String, String>();
        input.put("a", "X");
        input.put("b", "");
        List<? extends Endpoint> endpoints = DecisionEngine.matchTable(table, input);
        Assert.assertEquals(2, endpoints.size());
        Assert.assertEquals("LINE1", endpoints.get(0).getValue());
        Assert.assertEquals("LINE1", endpoints.get(1).getValue());

        input.clear();
        input.put("a", "NA");
        input.put("b", "99");
        endpoints = DecisionEngine.matchTable(table, input);
        Assert.assertEquals(2, endpoints.size());
        Assert.assertEquals("LINE2", endpoints.get(0).getValue());
        Assert.assertEquals("LINE2", endpoints.get(1).getValue());
    }

    @Test
    public void testLookupTable() throws IOException {
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
        Assert.assertNotNull(siteTable);

        Map<String, String> input = new HashMap<String, String>();
        input.put("site", "C809");

        // a lookup table in this case has no ENDPOINT column.  In those cases, an endpoint type of MATCH should be returned
        List<? extends Endpoint> endpoints = DecisionEngine.matchTable(siteTable, input);
        Assert.assertEquals(0, endpoints.size());
    }

    @Test
    public void testAllValuesMatching() throws IOException {
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
        Assert.assertNotNull(table);

        Map<String, String> input = new HashMap<String, String>();
        input.put("a", "1");
        input.put("b", "3");
        List<? extends Endpoint> endpoints = DecisionEngine.matchTable(allValuesTable, input);
        Assert.assertEquals(1, endpoints.size());
        Assert.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        Assert.assertEquals("RESULT2", endpoints.get(0).getValue());

        input = new HashMap<String, String>();
        input.put("a", "3");
        endpoints = DecisionEngine.matchTable(allValuesTable, input);
        Assert.assertEquals(1, endpoints.size());
        Assert.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        Assert.assertEquals("3A,ANY B", endpoints.get(0).getValue());

        input = new HashMap<String, String>();
        input.put("a", "3");
        input.put("b", "9");
        endpoints = DecisionEngine.matchTable(allValuesTable, input);
        Assert.assertEquals(1, endpoints.size());
        Assert.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        Assert.assertEquals("3A,ANY B", endpoints.get(0).getValue());

        input = new HashMap<String, String>();
        input.put("a", "6");
        input.put("b", "4");
        endpoints = DecisionEngine.matchTable(allValuesTable, input);
        Assert.assertEquals(1, endpoints.size());
        Assert.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        Assert.assertEquals("ANY A,4B", endpoints.get(0).getValue());

        input = new HashMap<String, String>();
        Assert.assertEquals(1, endpoints.size());
        endpoints = DecisionEngine.matchTable(allValuesTable, input);
        Assert.assertEquals(EndpointType.VALUE, endpoints.get(0).getType());
        Assert.assertEquals("CATCHALL", endpoints.get(0).getValue());
    }

    @Test
    public void testMinimumAlgorithm() {
        Definition minDefinition = _ENGINE.getProvider().getDefinition("starting_min");
        Assert.assertNotNull(minDefinition);
        Assert.assertEquals("starting_min", minDefinition.getId());
        Assert.assertNotNull(minDefinition.getInitialContext());

        Map<String, String> input = new HashMap<String, String>();
        Result result = _ENGINE.process(minDefinition, input);

        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals("bar", input.get("foo"));
    }

    @Test
    public void testAlgorithm() {
        Definition starting = _ENGINE.getProvider().getDefinition("starting_sample");
        Assert.assertNotNull(starting);
        Assert.assertEquals("starting_sample", starting.getId());
        Assert.assertNotNull(starting.getInitialContext());
    }

    @Test
    public void testMissingParameters() {
        Map<String, String> input = new HashMap<String, String>();
        Result result = _ENGINE.process("starting_sample", input);

        Assert.assertTrue(result.hasErrors());

        // even though there were no inputs, both tables were still processed
        Assert.assertEquals(2, result.getPath().size());
        Assert.assertEquals("m1.table_sample_first", result.getPath().get(0));
        Assert.assertEquals("m1.table_sample_second", result.getPath().get(1));
    }

    @Test
    public void testParameterLookupValidation() {
        Map<String, String> input = new HashMap<String, String>();
        input.put("a", "3");
        input.put("b", "31");  // value is not in lookup table

        Result result = _ENGINE.process("starting_sample", input);

        Assert.assertEquals(Type.FAILED_INPUT, result.getType());

        // since input "b" is fail_on_invalud, table processing should not continue
        Assert.assertEquals(0, result.getPath().size());

        // one error for input, and one error each of the two tables because of no match
        Assert.assertEquals(1, result.getErrors().size());

        // make "b" a valid value
        input.put("b", "30");

        result = _ENGINE.process("starting_sample", input);

        Assert.assertEquals(Type.STAGED, result.getType());

        // one error for input, and one error each of the two tables because of no match
        Assert.assertEquals(2, result.getErrors().size());
    }

    @Test
    public void testSingleTableProcess() {
        Map<String, String> input = new HashMap<String, String>();
        input.put("a", "7");
        input.put("b", "03");  // should map to "hemeretic" without using second table
        input.put("e", "X");
        Result result = _ENGINE.process("starting_sample", input);

        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals(2, result.getPath().size());

        Assert.assertEquals("LINE1", input.get("result"));
        Assert.assertEquals("HARD-CODE", input.get("d"));
    }

    @Test
    public void testOneJumpProcess() {
        Map<String, String> input = new HashMap<String, String>();
        input.put("a", "5");
        input.put("b", "20");
        input.put("c", "A");
        input.put("e", "X");
        Result result = _ENGINE.process("starting_sample", input);

        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals(3, result.getPath().size());

        Assert.assertEquals("A", input.get("result"));

        // now test an error line in the jump table
        input = new HashMap<String, String>();
        input.put("a", "5");
        input.put("b", "20");
        input.put("c", "C");
        input.put("e", "X");

        result = _ENGINE.process("starting_sample", input);

        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals(3, result.getPath().size());
        Assert.assertEquals(1, result.getErrors().size());
        Assert.assertEquals("Bad C value", result.getErrors().get(0).getMessage());
        Assert.assertNull(result.getErrors().get(0).getKey());
        Assert.assertEquals("table_jump_sample", result.getErrors().get(0).getTable());

        // finally test that no match is found in the jump table
        input = new HashMap<String, String>();
        input.put("a", "5");
        input.put("b", "20");
        input.put("c", "D");
        input.put("e", "X");

        result = _ENGINE.process("starting_sample", input);

        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals(3, result.getPath().size());
        Assert.assertEquals(1, result.getErrors().size());
        Assert.assertTrue(result.getErrors().get(0).getMessage().startsWith("Match not found"));
        Assert.assertNull(result.getErrors().get(0).getKey());
        Assert.assertEquals("table_jump_sample", result.getErrors().get(0).getTable());
    }

    @Test
    public void testProcessError() {
        Map<String, String> input = new HashMap<String, String>();
        input.put("a", "9");
        input.put("b", "99");
        input.put("e", "X");
        Result result = _ENGINE.process("starting_sample", input);

        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals(1, result.getErrors().size());
        Assert.assertEquals(2, result.getPath().size());
        Assert.assertEquals("999", result.getErrors().get(0).getMessage());
        Assert.assertNull(result.getErrors().get(0).getKey());
        Assert.assertEquals("table_sample_first", result.getErrors().get(0).getTable());
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

        BasicDefinition def = new BasicDefinition("starting_null_values");
        BasicInput inputKey = new BasicInput("a");
        inputKey.setDefault("0");
        def.addInput(inputKey);
        def.addInitialContext("result", "0");
        def.addMapping(new BasicMapping("m1", Collections.singletonList(new BasicTablePath("table_null_values"))));
        provider.addDefinition(def);
        DecisionEngine engine = new DecisionEngine(provider);

        Map<String, String> input = new HashMap<String, String>();
        Result result = engine.process("starting_null_values", input);

        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals("0", input.get("result"));

        input.clear();
        input.put("a", "1");
        result = engine.process("starting_null_values", input);
        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals("FOUND1", input.get("result"));

        input.clear();
        input.put("a", "2");
        result = engine.process("starting_null_values", input);
        Assert.assertFalse(result.hasErrors());
        Assert.assertFalse(input.containsKey("result"));

        input.clear();
        input.put("a", "3");
        result = engine.process("starting_null_values", input);
        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals("", input.get("result"));
    }

    @Test
    public void testProcessWithStop() {
        // first test that we get a result from the second table
        Map<String, String> input = new HashMap<String, String>();
        input.put("a", "5");
        input.put("b", "20");
        input.put("c", "A");
        input.put("e", "X");
        Result result = _ENGINE.process("starting_sample", input);

        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals(3, result.getPath().size());

        Assert.assertEquals("A", input.get("result"));
        Assert.assertEquals("LINE1", input.get("shared_result"));

        // next text then when STOP is encountered, the second table is not processed
        input.clear();
        input.put("a", "5");
        input.put("b", "20");
        input.put("c", "Z");
        input.put("e", "X");
        result = _ENGINE.process("starting_sample", input);

        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals(2, result.getPath().size());

        Assert.assertFalse(input.containsKey("result"));
        Assert.assertFalse(input.containsKey("shared_result"));
    }

    @Test
    public void testProcessWithBlanks() {
        Map<String, String> input = new HashMap<String, String>();
        input.put("x", "1");
        input.put("y", "");
        Result result = _ENGINE.process("starting_blank", input);

        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals(1, result.getPath().size());

        Assert.assertEquals("LINE1", input.get("y"));
        Assert.assertEquals("LINE1", input.get("z"));

        // verify that context blanks are trimmed
        input.put("x", "1");
        input.put("y", "  ");
        result = _ENGINE.process("starting_blank", input);

        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals(1, result.getPath().size());

        Assert.assertEquals("LINE1", input.get("y"));
        Assert.assertEquals("LINE1", input.get("z"));
    }

    @Test
    public void testProcessWithDoubleInputMapping() {
        Map<String, String> input = new HashMap<String, String>();
        input.put("x", "1");
        Result result = _ENGINE.process("starting_double_input", input);

        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals(1, result.getPath().size());

        Assert.assertEquals("LINE3", input.get("result"));
    }

    @Test
    public void testProcessWithDoubleOutputMapping() {
        Map<String, String> input = new HashMap<String, String>();
        input.put("a", "1");
        input.put("b", "00");
        Result result = _ENGINE.process("starting_double_output", input);

        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals(1, result.getPath().size());

        Assert.assertNull(input.get("result"));
        Assert.assertEquals("LINE1", input.get("output1"));
        Assert.assertEquals("LINE1", input.get("output2"));
    }

    @Test
    public void testInvolvedAlgorithms() {
        List<Mapping> mappings;
        Map<String, String> context = new HashMap<String, String>();

        Definition definition = _ENGINE.getProvider().getDefinition("starting_min");
        mappings = _ENGINE.getInvolvedMappings(definition, context);
        Assert.assertEquals(0, mappings.size());

        definition = _ENGINE.getProvider().getDefinition("starting_inclusions");

        mappings = _ENGINE.getInvolvedMappings(definition, context);
        Assert.assertEquals(1, mappings.size());

        context.put("a", "1");
        mappings = _ENGINE.getInvolvedMappings(definition, context);
        Assert.assertEquals(1, mappings.size());

        context.put("a", "2");
        mappings = _ENGINE.getInvolvedMappings(definition, context);
        Assert.assertEquals(2, mappings.size());
    }

    @Test
    public void testInvolvedTables() {
        Set<String> tables;

        // test a case with no involved tables
        tables = _ENGINE.getInvolvedTables("starting_min");
        Assert.assertEquals(0, tables.size());

        // test a case with a single table with one jump
        tables = _ENGINE.getInvolvedTables("starting_sample");
        Assert.assertEquals(4, tables.size());
        Assert.assertTrue(tables.contains("table_lookup_sample"));
        Assert.assertTrue(tables.contains("table_sample_first"));
        Assert.assertTrue(tables.contains("table_sample_second"));
        Assert.assertTrue(tables.contains("table_jump_sample"));

        // test a case with inclusion/exclusion tables
        tables = _ENGINE.getInvolvedTables("starting_inclusions");
        Assert.assertEquals(5, tables.size());
        Assert.assertTrue(tables.contains("table_part1"));
        Assert.assertTrue(tables.contains("table_part2"));
        Assert.assertTrue(tables.contains("table_inclusion1"));
        Assert.assertTrue(tables.contains("table_inclusion2"));
        Assert.assertTrue(tables.contains("table_exclusion1"));
    }

    @Test
    public void testInvolvedTableRecursion() {
        Set<String> tables = _ENGINE.getInvolvedTables("starting_recursion");

        Assert.assertEquals(1, tables.size());
        Assert.assertTrue(tables.contains("table_recursion"));
    }

    @Test
    public void testProcessWithRecursion() {
        Map<String, String> input = new HashMap<String, String>();
        input.put("a", "4");
        Result result = _ENGINE.process("starting_recursion", input);

        Assert.assertEquals(Type.STAGED, result.getType());
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals(1, result.getErrors().size());
        Assert.assertEquals(1, result.getPath().size());
        Assert.assertEquals("table_recursion", result.getErrors().get(0).getTable());
    }

    @Test
    public void testProcessWithMultipleEndpoints() {
        // first test all 3 results get set
        Map<String, String> input = new HashMap<String, String>();
        input.put("a", "2");
        input.put("b", "12");
        Result result = _ENGINE.process("starting_multiple_endpoints", input);

        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals(Type.STAGED, result.getType());
        Assert.assertEquals(1, result.getPath().size());

        Assert.assertEquals("1_LINE1", input.get("r1"));
        Assert.assertEquals("2_LINE1", input.get("r2"));
        Assert.assertEquals("3_LINE1", input.get("r3"));

        // test 2 VALUEs and an ERROR
        input.clear();
        input.put("a", "0");
        input.put("b", "25");
        result = _ENGINE.process("starting_multiple_endpoints", input);
        Assert.assertEquals(Type.STAGED, result.getType());
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals(1, result.getErrors().size());
        Assert.assertEquals("2_LINE2", result.getErrors().get(0).getMessage());
        Assert.assertEquals(1, result.getPath().size());

        Assert.assertEquals("1_LINE2", input.get("r1"));
        Assert.assertFalse(input.containsKey("r2"));
        Assert.assertEquals("3_LINE2", input.get("r3"));

        // test 2 JUMPs and one VALUE and a missing jump table value
        input.clear();
        input.put("a", "5");
        input.put("b", "20");
        result = _ENGINE.process("starting_multiple_endpoints", input);

        Assert.assertEquals(Type.STAGED, result.getType());
        Assert.assertTrue(result.hasErrors());
        Assert.assertTrue(result.getErrors().get(0).getMessage().startsWith("Match not found"));
        Assert.assertNull(result.getErrors().get(0).getKey());
        Assert.assertEquals("table_jump_sample", result.getErrors().get(0).getTable());

        // test 1 JUMP and 2 VALUEs
        input.clear();
        input.put("a", "5");
        input.put("b", "20");
        input.put("c", "A");
        result = _ENGINE.process("starting_multiple_endpoints", input);

        Assert.assertEquals(Type.STAGED, result.getType());
        Assert.assertFalse(result.hasErrors());
        Assert.assertFalse(input.containsKey("r1"));
        Assert.assertEquals("A", input.get("result"));
        Assert.assertEquals("2_LINE3", input.get("r2"));
        Assert.assertEquals("3_LINE3", input.get("r3"));

        // test 3 ERRORs
        input.clear();
        input.put("a", "9");
        input.put("b", "99");
        result = _ENGINE.process("starting_multiple_endpoints", input);

        Assert.assertEquals(Type.STAGED, result.getType());
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals(3, result.getErrors().size());
        Assert.assertEquals("1_LINE4", result.getErrors().get(0).getMessage());
        Assert.assertNull(result.getErrors().get(0).getKey());
        Assert.assertEquals("2_LINE4", result.getErrors().get(1).getMessage());
        Assert.assertNull(result.getErrors().get(1).getKey());
        Assert.assertEquals("3_LINE4", result.getErrors().get(2).getMessage());
        Assert.assertNull(result.getErrors().get(2).getKey());
        Assert.assertFalse(input.containsKey("r1"));
        Assert.assertFalse(input.containsKey("r2"));
        Assert.assertFalse(input.containsKey("r3"));
    }

    @Test
    public void testMultipleEndpointWithoutRecursion() {
        // test 2 JUMPs to same table and one value; this is not infinite recursion but since same table called twice it gets confused
        Map<String, String> input = new HashMap<String, String>();
        input.put("a", "4");
        input.put("b", "44");
        input.put("c", "A");
        Result result = _ENGINE.process("starting_multiple_endpoints", input);

        Assert.assertEquals(Type.STAGED, result.getType());
        Assert.assertFalse(result.hasErrors());
        Assert.assertFalse(input.containsKey("r1"));
        Assert.assertFalse(input.containsKey("r3"));
        Assert.assertEquals("A", input.get("result"));
        Assert.assertEquals("2_LINE5", input.get("r2"));
    }

    @Test
    public void testInclusionsAndExclusions() {
        Map<String, String> input = new HashMap<String, String>();
        input.put("a", "1");
        input.put("b", "2");
        input.put("c", "3");
        Result result = _ENGINE.process("starting_inclusions", input);

        Assert.assertEquals(Type.STAGED, result.getType());
        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals("2", input.get("result"));
        Assert.assertFalse(input.containsKey("special"));

        input.clear();
        input.put("a", "8");
        input.put("b", "2");
        input.put("c", "3");
        result = _ENGINE.process("starting_inclusions", input);

        Assert.assertEquals(Type.STAGED, result.getType());
        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals("3", input.get("result"));
        Assert.assertEquals("SUCCESS", input.get("special"));

        input.clear();
        input.put("a", "9");
        input.put("b", "2");
        input.put("c", "3");
        result = _ENGINE.process("starting_inclusions", input);

        Assert.assertEquals(Type.STAGED, result.getType());
        Assert.assertFalse(result.hasErrors());
        Assert.assertFalse(input.containsKey("result"));
        Assert.assertEquals("SUCCESS", input.get("special"));
    }

    @Test(expected = IllegalStateException.class)
    public void testDuplicateAlgorithms() throws IOException {
        BasicDataProvider provider = new BasicDataProvider();
        BasicDefinition def = new BasicDefinition();
        def.setId("TEST1");
        provider.addDefinition(def);
        provider.addDefinition(def);
    }

    /**
     * Shortcut for constructing a Set using a List
     * @param values list of values
     * @return Set of values
     */
    private Set<String> asSet(String... values) {
        return new HashSet<String>(Arrays.asList(values));
    }

    @Test
    public void testAlgorithmInputs() {
        DataProvider provider = _ENGINE.getProvider();

        Assert.assertEquals(asSet(), _ENGINE.getInputs(provider.getDefinition("starting_min")));
        Assert.assertEquals(asSet("a", "b", "c", "e"), _ENGINE.getInputs(provider.getDefinition("starting_sample")));
        Assert.assertEquals(asSet("a", "b", "c"), _ENGINE.getInputs(provider.getDefinition("starting_inclusions")));
        Assert.assertEquals(asSet("a"), _ENGINE.getInputs(provider.getDefinition("starting_recursion")));
        Assert.assertEquals(asSet("a", "b", "c"), _ENGINE.getInputs(provider.getDefinition("starting_multiple_endpoints")));
        Assert.assertEquals(asSet("b", "not_in_input_list"), _ENGINE.getInputs(provider.getDefinition("starting_inclusions_extra_inputs")));
        Assert.assertEquals(asSet("main_input"), _ENGINE.getInputs(provider.getDefinition("starting_intermediate_values")));
    }

    @Test
    public void testGetAlgorithmOutputs() {
        DataProvider provider = _ENGINE.getProvider();

        Assert.assertEquals(asSet(), _ENGINE.getOutputs(provider.getDefinition("starting_min")));
        Assert.assertEquals(asSet("result", "shared_result"), _ENGINE.getOutputs(provider.getDefinition("starting_sample")));
        Assert.assertEquals(asSet("result", "special"), _ENGINE.getOutputs(provider.getDefinition("starting_inclusions")));
        Assert.assertEquals(asSet("result"), _ENGINE.getOutputs(provider.getDefinition("starting_recursion")));
        Assert.assertEquals(asSet("result", "r1", "r2", "r3"), _ENGINE.getOutputs(provider.getDefinition("starting_multiple_endpoints")));
        Assert.assertEquals(asSet("mapped_result"), _ENGINE.getOutputs(provider.getDefinition("starting_inclusions_extra_inputs")));
        Assert.assertEquals(asSet("intermediate_output", "final_output"), _ENGINE.getOutputs(provider.getDefinition("starting_intermediate_values")));
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

        Map<String, String> context = new HashMap<String, String>();

        Assert.assertEquals(DecisionEngine._BLANK_OUTPUT + "," + DecisionEngine._BLANK_OUTPUT, DecisionEngine.getTableInputsAsString(table, context));

        context.put("b", "25");
        Assert.assertEquals(DecisionEngine._BLANK_OUTPUT + ",25", DecisionEngine.getTableInputsAsString(table, context));

        context.put("a", "7");
        Assert.assertEquals("7,25", DecisionEngine.getTableInputsAsString(table, context));
        context.put("a", "    7");
        Assert.assertEquals("7,25", DecisionEngine.getTableInputsAsString(table, context));
        context.put("a", "7    ");
        Assert.assertEquals("7,25", DecisionEngine.getTableInputsAsString(table, context));

        table = new BasicTable("table_empty");
        context = new HashMap<String, String>();
        Assert.assertEquals("", DecisionEngine.getTableInputsAsString(table, context));
    }

}
