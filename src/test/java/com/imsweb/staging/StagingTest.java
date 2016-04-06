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

import org.junit.Assert;
import org.junit.Test;

import com.imsweb.decisionengine.ColumnDefinition;
import com.imsweb.staging.entities.StagingColumnDefinition;
import com.imsweb.staging.entities.StagingMapping;
import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingSchemaInput;
import com.imsweb.staging.entities.StagingStringRange;
import com.imsweb.staging.entities.StagingTable;
import com.imsweb.staging.entities.StagingTableRow;
import com.imsweb.staging.tnm.TnmDataProvider;

/**
 * Base class for all algorithm-specific testing
 */
public abstract class StagingTest {

    protected static Staging _STAGING;

    /**
     * Return the algorithm name
     * @return
     */
    public abstract String getAlgorithm();

    /**
     * Return the algorithm version
     * @return
     */
    public abstract String getVersion();

    /**
     * Return the staging data provider
     * @return
     */
    public abstract StagingFileDataProvider getProvider();

    @Test
    public void testInitialization() {
        Assert.assertEquals(getAlgorithm(), _STAGING.getAlgorithm());
        Assert.assertEquals(getVersion(), _STAGING.getVersion());
    }

    @Test
    public void testInitAllTables() {
        for (String id : _STAGING.getTableIds()) {
            StagingTable table = _STAGING.getTable(id);

            Assert.assertNotNull(table);
            Assert.assertNotNull(table.getAlgorithm());
            Assert.assertNotNull(table.getVersion());
            Assert.assertNotNull(table.getName());
        }
    }

    @Test
    public void testValidCode() {
        Map<String, String> context = new HashMap<>();
        context.put("hist", "8000");
        Assert.assertTrue(_STAGING.isContextValid("prostate", "hist", context));
        context.put("hist", "8542");
        Assert.assertTrue(_STAGING.isContextValid("prostate", "hist", context));
    }

    @Test
    public void testBasicInputs() {
        // all inputs for all schemas will have null unit and decimal places
        for (String id : _STAGING.getSchemaIds()) {
            StagingSchema schema = _STAGING.getSchema(id);
            for (StagingSchemaInput input : schema.getInputs()) {
                Assert.assertNull("No schemas should have units", input.getUnit());
                Assert.assertNull("No schemas should have decimal places", input.getDecimalPlaces());
            }
        }
    }

    @Test
    public void testValidSite() {
        Assert.assertFalse(_STAGING.isValidSite(null));
        Assert.assertFalse(_STAGING.isValidSite(""));
        Assert.assertFalse(_STAGING.isValidSite("C21"));
        Assert.assertFalse(_STAGING.isValidSite("C115"));

        Assert.assertTrue(_STAGING.isValidSite("C509"));
    }

    @Test
    public void testValidHistology() {
        Assert.assertFalse(_STAGING.isValidHistology(null));
        Assert.assertFalse(_STAGING.isValidHistology(""));
        Assert.assertFalse(_STAGING.isValidHistology("810"));
        Assert.assertFalse(_STAGING.isValidHistology("8176"));

        Assert.assertTrue(_STAGING.isValidHistology("8000"));
        Assert.assertTrue(_STAGING.isValidHistology("8201"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetTable() {
        _STAGING.getTable("bad_table_name");
    }

    @Test
    public void testCachedSiteAndHistology() {
        TnmDataProvider provider = TnmDataProvider.getInstance(TnmDataProvider.TnmVersion.v1_1);
        Assert.assertTrue(provider.getValidSites().size() > 0);
        Assert.assertTrue(provider.getValidHistologies().size() > 0);

        // site tests
        List<String> validSites = Arrays.asList("C000", "C809");
        List<String> invalidSites = Arrays.asList("C727", "C810");
        for (String site : validSites)
            Assert.assertTrue(provider.getValidSites().contains(site));
        for (String site : invalidSites)
            Assert.assertFalse(provider.getValidSites().contains(site));

        // hist tests
        List<String> validHist = Arrays.asList("8000", "8002", "8005", "8290", "9992");
        List<String> invalidHist = Arrays.asList("8006", "9993");
        for (String hist : validHist)
            Assert.assertTrue(provider.getValidHistologies().contains(hist));
        for (String hist : invalidHist)
            Assert.assertFalse(provider.getValidHistologies().contains(hist));
    }

    @Test
    public void testForUnusedTables() {
        Set<String> usedTables = new HashSet<>();
        for (String id : _STAGING.getSchemaIds())
            usedTables.addAll(_STAGING.getSchema(id).getInvolvedTables());

        Set<String> unusedTables = new HashSet<>();
        for (String id : _STAGING.getTableIds())
            if (!usedTables.contains(id))
                unusedTables.add(id);

        if (!unusedTables.isEmpty())
            Assert.fail("There are " + unusedTables.size() + " tables that are not used in any schema: " + unusedTables);
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
            for (StagingMapping mapping : schema.getMappings()) {
                if (ids.contains(mapping.getId()))
                    errors.add("The mapping id " + schemaId + ":" + mapping.getId() + " is duplicated.  This should never happen");
                ids.add(mapping.getId());
            }
        }

        assertNoErrors(errors, "input values and their assocated validation tables");
    }

    /**
     * Helper method to assert failures when tracked errors exist
     * @param errors
     * @param description
     */
    public void assertNoErrors(Collection<String> errors, String description) {
        if (!errors.isEmpty()) {
            System.out.println("There were " + errors.size() + " issues with " + description + ".");
            for (String error : errors)
                System.out.println(error);
            Assert.fail();
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
            List<StagingStringRange> ranges = row.getInputs().get(key);

            for (StagingStringRange range : ranges) {
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
