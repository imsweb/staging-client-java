/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import com.imsweb.decisionengine.ColumnDefinition;
import com.imsweb.staging.entities.StagingColumnDefinition;
import com.imsweb.staging.entities.StagingMapping;
import com.imsweb.staging.entities.StagingRange;
import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingSchemaInput;
import com.imsweb.staging.entities.StagingTable;
import com.imsweb.staging.entities.StagingTableRow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Base class for all algorithm-specific testing
 */
public abstract class StagingTest {

    protected static Staging _STAGING;

    /**
     * Return the algorithm name
     */
    public abstract String getAlgorithm();

    /**
     * Return the algorithm version
     */
    public abstract String getVersion();

    /**
     * Return the staging data provider
     */
    public abstract StagingFileDataProvider getProvider();

    @Test
    public void testInitialization() {
        assertEquals(getAlgorithm(), _STAGING.getAlgorithm());
        assertEquals(getVersion(), _STAGING.getVersion());
    }

    @Test
    public void testInitAllTables() {
        for (String id : _STAGING.getTableIds()) {
            StagingTable table = _STAGING.getTable(id);

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
            StagingSchema schema = _STAGING.getSchema(id);
            for (StagingSchemaInput input : schema.getInputs()) {
                assertNull("No schemas should have units", input.getUnit());
                assertNull("No schemas should have decimal places", input.getDecimalPlaces());
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
    public void testCachedSiteAndHistology() {
        StagingDataProvider provider = getProvider();
        assertTrue(provider.getValidSites().size() > 0);
        assertTrue(provider.getValidHistologies().size() > 0);

        // site tests
        List<String> validSites = Arrays.asList("C000", "C809");
        List<String> invalidSites = Arrays.asList("C727", "C810");
        for (String site : validSites)
            assertTrue(provider.getValidSites().contains(site));
        for (String site : invalidSites)
            assertFalse(provider.getValidSites().contains(site));

        // hist tests
        List<String> validHist = Arrays.asList("8000", "8002", "8005", "8290", "9992");
        List<String> invalidHist = Arrays.asList("8006", "9993");
        for (String hist : validHist)
            assertTrue(provider.getValidHistologies().contains(hist));
        for (String hist : invalidHist)
            assertFalse(provider.getValidHistologies().contains(hist));
    }

    @Test
    public void testForUnusedTables() {
        Set<String> usedTables = new HashSet<>();
        for (String id : _STAGING.getSchemaIds())
            usedTables.addAll(_STAGING.getSchema(id).getInvolvedTables());

        Set<String> unusedTables = _STAGING.getTableIds().stream().filter(id -> !usedTables.contains(id)).collect(Collectors.toSet());

        if (!unusedTables.isEmpty())
            fail("There are " + unusedTables.size() + " tables that are not used in any schema: " + unusedTables);
    }

    @Test
    public void testInputTables() {
        Set<String> errors = new HashSet<>();

        for (String schemaId : _STAGING.getSchemaIds()) {
            StagingSchema schema = _STAGING.getSchema(schemaId);

            // build a list of input tables that should be excluded
            for (StagingSchemaInput input : schema.getInputs()) {
                if (input.getTable() != null) {
                    Set<String> inputKeys = new HashSet<>();
                    StagingTable table = _STAGING.getTable(input.getTable());
                    for (StagingColumnDefinition def : table.getColumnDefinitions())
                        if (ColumnDefinition.ColumnType.INPUT.equals(def.getType()))
                            inputKeys.add(def.getKey());

                    // make sure the input key matches the an input column
                    if (!inputKeys.contains(input.getKey()))
                        errors.add("Input key " + schemaId + ":" + input.getKey() + " does not match validation table " + table.getId() + ": " + inputKeys.toString());
                }
            }
        }

        assertNoErrors(errors, "input values and their assocated validation tables");
    }

    @Test
    public void verifyInputs() {
        List<String> errors = new ArrayList<>();

        for (String id : _STAGING.getSchemaIds()) {
            StagingSchema schema = _STAGING.getSchema(id);

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
            StagingSchema schema = _STAGING.getSchema(schemaId);

            // build a list of input tables that should be excluded
            Set<String> ids = new HashSet<>();

            List<StagingMapping> mappings = schema.getMappings();
            if (mappings != null)
                for (StagingMapping mapping : mappings) {
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
            System.out.println("There were " + errors.size() + " issues with " + description + ".");
            errors.forEach(System.out::println);
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
        StagingTable table = _STAGING.getTable(tableId);
        Integer length = null;

        // loop over each row
        for (StagingTableRow row : table.getTableRows()) {
            List<StagingRange> ranges = row.getInputs().get(key);

            for (StagingRange range : ranges) {
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
