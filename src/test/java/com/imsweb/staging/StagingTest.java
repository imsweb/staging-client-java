/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.imsweb.staging.entities.ColumnDefinition;
import com.imsweb.staging.entities.Input;
import com.imsweb.staging.entities.Mapping;
import com.imsweb.staging.entities.Range;
import com.imsweb.staging.entities.Schema;
import com.imsweb.staging.entities.Table;
import com.imsweb.staging.entities.TableRow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Base class for all algorithm-specific testing
 */
public abstract class StagingTest {

    private static final Logger _LOG = LoggerFactory.getLogger(StagingTest.class);

    protected static Staging _STAGING;
    protected static StagingDataProvider _PROVIDER;

    /**
     * Return the algorithm name
     */
    public abstract String getAlgorithm();

    /**
     * Return the algorithm version
     */
    public abstract String getVersion();

    /**
     * Return the full path of specified algorithm
     */
    public static Path getAlgorithmPath(String algorithm) throws URISyntaxException, IOException {
        Path algorithmsDir = Paths.get(Thread.currentThread()
                .getContextClassLoader()
                .getResource("algorithms")
                .toURI());

        try (Stream<Path> files = Files.list(algorithmsDir)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(algorithm + "-"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No " + algorithm + "  file found in algorithms directory"));
        }
    }

    @Test
    public void testInitialization() {
        assertEquals(getAlgorithm(), _STAGING.getAlgorithm());
        assertEquals(getVersion(), _STAGING.getVersion());
    }

    @Test
    public void testInitAllTables() {
        for (String id : _STAGING.getTableIds()) {
            Table table = _STAGING.getTable(id);

            assertNotNull(table);
            assertNotNull(table.getAlgorithm());
            assertNotNull(table.getVersion());
            assertNotNull(table.getName());
        }
    }

    @Test
    public void testValidCode() {
        Map<String, String> context = new HashMap<>();
        context.put("hist", "7000");
        assertFalse(_STAGING.isContextValid("prostate", "hist", context));
        context.put("hist", "8000");
        assertTrue(_STAGING.isContextValid("prostate", "hist", context));
        context.put("hist", "8542");
        assertTrue(_STAGING.isContextValid("prostate", "hist", context));

        // make sure null is handled
        context.put("hist", null);
        assertFalse(_STAGING.isContextValid("prostate", "hist", context));

        // make sure blank is handled
        context.put("hist", "");
        assertFalse(_STAGING.isContextValid("prostate", "hist", context));
    }

    @Test
    public void testBasicInputs() {
        // all inputs for all schemas will have null unit and decimal places
        for (String id : _STAGING.getSchemaIds()) {
            Schema schema = _STAGING.getSchema(id);
            for (Input input : schema.getInputs()) {
                assertNull(input.getUnit(), "No schemas should have units");
                assertNull(input.getDecimalPlaces(), "No schemas should have decimal places");
            }
        }
    }

    @Test
    public void testInputDefault() {
        Map<String, String> context = new HashMap<>();

        // error conditions
        String schemaId = _STAGING.getSchemaIds().stream().findFirst().orElse(null);
        assertNotNull(schemaId);
        assertEquals("", _STAGING.getInputDefault(_STAGING.getSchema(schemaId), "i_do_not_exist", context));

        for (String id : _STAGING.getSchemaIds()) {
            Schema schema = _STAGING.getSchema(id);
            for (Input input : schema.getInputs()) {
                if (input.getDefault() != null && input.getDefaultTable() != null)
                    fail("In " + getAlgorithm() + ", schema " + schema.getId() + " and input " + input.getKey() + " there is a default and default_table. That is not allowed.");

                if (input.getDefault() != null)
                    assertEquals(input.getDefault(), _STAGING.getInputDefault(schema, input.getKey(), context));
                else if (input.getDefaultTable() != null)
                    assertFalse(_STAGING.getInputDefault(schema, input.getKey(), context).isEmpty());
                else
                    assertTrue(_STAGING.getInputDefault(schema, input.getKey(), context).isEmpty());
            }
        }
    }

    @Test
    public void testValidSite() {
        assertFalse(_STAGING.isValidSite(null));
        assertFalse(_STAGING.isValidSite(""));
        assertFalse(_STAGING.isValidSite("C21"));
        assertFalse(_STAGING.isValidSite("C115"));

        assertTrue(_STAGING.isValidSite("C509"));
    }

    @Test
    public void testValidHistology() {
        assertFalse(_STAGING.isValidHistology(null));
        assertFalse(_STAGING.isValidHistology(""));
        assertFalse(_STAGING.isValidHistology("810"));
        assertFalse(_STAGING.isValidHistology("8176"));

        assertTrue(_STAGING.isValidHistology("8000"));
        assertTrue(_STAGING.isValidHistology("8201"));
    }

    @Test
    public void testGetTable() {
        assertNull(_STAGING.getTable("bad_table_name"));
    }

    @Test
    public void testForUnusedTables() {
        Set<String> usedTables = new HashSet<>();
        for (String id : _STAGING.getSchemaIds())
            usedTables.addAll(_STAGING.getSchema(id).getInvolvedTables());

        Set<String> unusedTables = _STAGING.getTableIds().stream().filter(id -> !usedTables.contains(id) && !id.startsWith("conversion_")).collect(Collectors.toSet());

        if (!unusedTables.isEmpty())
            fail("There are " + unusedTables.size() + " tables that are not used in any schema: " + unusedTables);
    }

    @Test
    public void testInputTables() {
        Set<String> errors = new HashSet<>();

        for (String schemaId : _STAGING.getSchemaIds()) {
            Schema schema = _STAGING.getSchema(schemaId);

            // build a list of input tables that should be excluded
            for (Input input : schema.getInputs()) {
                if (input.getTable() != null) {
                    Set<String> inputKeys = new HashSet<>();
                    Table table = _STAGING.getTable(input.getTable());
                    for (ColumnDefinition def : table.getColumnDefinitions())
                        if (ColumnDefinition.ColumnType.INPUT.equals(def.getType()))
                            inputKeys.add(def.getKey());

                    // make sure the input key matches an input column
                    if (!inputKeys.contains(input.getKey()))
                        errors.add("Input key " + schemaId + ":" + input.getKey() + " does not match validation table " + table.getId() + ": " + inputKeys);
                }
            }
        }

        assertNoErrors(errors, "input values and their assocated validation tables");
    }

    @Test
    public void verifyInputs() {
        List<String> errors = new ArrayList<>();

        for (String id : _STAGING.getSchemaIds()) {
            Schema schema = _STAGING.getSchema(id);

            // loop over all the inputs returned by processing the schema and make sure they are all part of the main list of inputs on the schema
            for (String input : _STAGING.getInputs(schema))
                if (!schema.getInputMap().containsKey(input))
                    errors.add("Error processing schema " + schema.getId() + ": Table input '" + input + "' not in master list of inputs");
        }

        assertNoErrors(errors, "input values");
    }

    @Test
    public void testMappingIdUniqueness() {
        Set<String> errors = new HashSet<>();

        for (String schemaId : _STAGING.getSchemaIds()) {
            Schema schema = _STAGING.getSchema(schemaId);

            // build a list of input tables that should be excluded
            Set<String> ids = new HashSet<>();

            if (schema.getMappings() != null)
                for (Mapping mapping : schema.getMappings()) {
                    if (ids.contains(mapping.getId()))
                        errors.add("The mapping id " + schemaId + ":" + mapping.getId() + " is duplicated.  This should never happen");
                    ids.add(mapping.getId());
                }
        }

        assertNoErrors(errors, "input values and their assocated validation tables");
    }

    /**
     * Helper method to assert failures when tracked errors exist
     */
    public void assertNoErrors(Collection<String> errors, String description) {
        if (!errors.isEmpty()) {
            _LOG.error("There were {} issues with {}.", errors.size(), description);
            errors.forEach(_LOG::error);
            fail();
        }
    }

    /**
     * Return the input length from a specified table
     * @param tableId table indentifier
     * @param key input key
     * @return null if no length couild be determined, or the length
     */
    protected Integer getInputLength(String tableId, String key) {
        Table table = _STAGING.getTable(tableId);
        Integer length = null;

        // loop over each row
        for (TableRow row : table.getTableRows()) {
            for (Range range : row.getColumnInput(key)) {
                String low = range.getLow();
                String high = range.getHigh();

                if (range.matchesAll() || low.isEmpty())
                    continue;

                if (low.startsWith("{{") && low.contains(Staging.CTX_YEAR_CURRENT))
                    low = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
                if (high.startsWith("{{") && high.contains(Staging.CTX_YEAR_CURRENT))
                    high = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

                if (length != null && (low.length() != length || high.length() != length))
                    throw new IllegalStateException("Inconsistent lengths in table " + tableId + " for key " + key);

                length = low.length();
            }
        }

        return length;
    }
}