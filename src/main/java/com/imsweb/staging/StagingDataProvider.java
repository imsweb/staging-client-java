/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

import com.imsweb.decisionengine.ColumnDefinition.ColumnType;
import com.imsweb.decisionengine.DataProvider;
import com.imsweb.decisionengine.DecisionEngine;
import com.imsweb.decisionengine.Endpoint.EndpointType;
import com.imsweb.staging.entities.StagingColumnDefinition;
import com.imsweb.staging.entities.StagingEndpoint;
import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingSchemaInput;
import com.imsweb.staging.entities.StagingStringRange;
import com.imsweb.staging.entities.StagingTable;
import com.imsweb.staging.entities.StagingTableRow;

/**
 * An abstract implementation of DataProvider customized for handling staging schemas/tables
 */
public abstract class StagingDataProvider implements DataProvider {

    // tables that all algorithm versions must have
    public static final String PRIMARY_SITE_TABLE = "primary_site";
    public static final String HISTOLOGY_TABLE = "histology";

    // output all dates in ISO-8061 format and UTC time
    private static final DateFormat _DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private static final ObjectMapper _MAPPER = new ObjectMapper();

    private static StagingStringRange _MATCH_ALL_ENDPOINT = new StagingStringRange();

    // lookup cache
    private LoadingCache<SchemaLookup, List<StagingSchema>> _lookupCache;

    // site/hist cache
    private LoadingCache<String, Set<String>> _validValuesCache;

    static {
        _DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));

        // do not write null values
        _MAPPER.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        _MAPPER.setSerializationInclusion(Include.NON_NULL);

        // set Date objects to output in readable customized format
        _MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        _MAPPER.setDateFormat(_DATE_FORMAT);

        _MAPPER.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        _MAPPER.setVisibility(PropertyAccessor.GETTER, Visibility.ANY);
    }

    /**
     * Constructor loads all schemas and sets up cache
     */
    protected StagingDataProvider() {
        // cache schema lookups
        _lookupCache = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(new CacheLoader<SchemaLookup, List<StagingSchema>>() {
                    @Override
                    public List<StagingSchema> load(SchemaLookup lookup) throws Exception {
                        return getSchemas(lookup);
                    }
                });

        // cache the valid values for certain tables including site and histology
        _validValuesCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, Set<String>>() {
                    @Override
                    public Set<String> load(String tableId) throws Exception {
                        return getAllInputValues(tableId);
                    }
                });

    }

    /**
     * Clear the caches
     */
    public void invalidateCache() {
        _lookupCache.invalidateAll();
        _validValuesCache.invalidateAll();
    }

    /**
     * Initialize a schema.
     * @param schema schema entity
     * @return initialized schema entity
     */
    public static StagingSchema initSchema(StagingSchema schema) {
        // parse the schema selection ranges
        if (schema.getSchemaSelectionTable() == null)
            throw new IllegalStateException("Schemas must have a schema selection table.");

        // parse the values into something that can searched more efficiently
        if (schema.getInputs() != null) {
            Map<String, StagingSchemaInput> parsedInputMap = new HashMap<String, StagingSchemaInput>();
            for (StagingSchemaInput input : schema.getInputs()) {
                // verify that all inputs contain a key
                if (input.getKey() == null)
                    throw new IllegalStateException("All input definitions must have a 'key' value defined.");

                // make a copy of the StagingSchemaInput; the parsed input map provides an easy way to look up by key
                StagingSchemaInput inputCopy = new StagingSchemaInput(input);
                parsedInputMap.put(inputCopy.getKey(), inputCopy);
                schema.setInputMap(parsedInputMap);
            }
        }

        return schema;
    }

    /**
     * Initialize a table.
     * @param table table entity
     * @return initialized table entity
     */
    public static StagingTable initTable(StagingTable table) {
        Set<String> extraInputs = new HashSet<String>();

        if (table.getRawRows() != null) {
            for (List<String> row : table.getRawRows()) {
                StagingTableRow tableRowEntity = new StagingTableRow();

                // make sure the number of cells in the row matches the number of columns defined
                if (table.getColumnDefinitions().size() != row.size())
                    throw new IllegalStateException("Table '" + table.getId() + "' has a row with " + row.size() + " values but should have " + table.getColumnDefinitions().size() + ": " + row);

                // loop over the column definitions in order since the data needs to retrieved by array position
                for (int i = 0; i < table.getColumnDefinitions().size(); i++) {
                    StagingColumnDefinition col = table.getColumnDefinitions().get(i);
                    String cellValue = row.get(i);

                    switch (col.getType()) {
                        case INPUT:
                            // if there are no ranges in the list, that means the cell was "blank" and is not needed in the table row
                            List<StagingStringRange> ranges = splitValues(cellValue);
                            if (!ranges.isEmpty()) {
                                tableRowEntity.addInput(col.getKey(), ranges);

                                // if there are key references used (values that reference other inputs) like {{key}}, then add them to the extra inputs list
                                for (StagingStringRange range : ranges) {
                                    if (DecisionEngine.isReferenceVariable(range.getLow()))
                                        extraInputs.add(DecisionEngine.trimBraces(range.getLow()));
                                    if (DecisionEngine.isReferenceVariable(range.getHigh()))
                                        extraInputs.add(DecisionEngine.trimBraces(range.getHigh()));
                                }
                            }
                            break;
                        case ENDPOINT:
                            StagingEndpoint endpoint = parseEndpoint(cellValue);
                            if (EndpointType.VALUE.equals(endpoint.getType()))
                                endpoint.setResultKey(col.getKey());
                            tableRowEntity.addEndpoint(endpoint);

                            // if there are key references used (values that reference other inputs) like {{key}}, then add them to the extra inputs list
                            if (EndpointType.VALUE.equals(endpoint.getType()) && DecisionEngine.isReferenceVariable(endpoint.getValue()))
                                extraInputs.add(DecisionEngine.trimBraces(endpoint.getValue()));
                            break;
                        case DESCRIPTION:
                            // do nothing
                            break;
                        default:
                            throw new IllegalStateException("Table '" + table.getId() + " has an unknown column type: '" + col.getType() + "'");
                    }
                }

                table.getTableRows().add(tableRowEntity);
            }
        }

        // add extra inputs, if any; do not include context variables since they are not user input
        extraInputs.removeAll(Staging.CONTEXT_KEYS);
        table.setExtraInput(extraInputs.isEmpty() ? null : extraInputs);

        return table;
    }

    /**
     * Return true if the site is valid
     * @param site primary site
     * @return true if the side is valid
     */
    public boolean isValidSite(String site) {
        boolean valid = (site != null);

        if (valid) {
            StagingTable table = getTable(PRIMARY_SITE_TABLE);
            if (table == null)
                throw new IllegalStateException("Unable to locate " + PRIMARY_SITE_TABLE + " table");

            valid = getValidSites().contains(site);
        }

        return valid;
    }

    /**
     * Return true if the histology is valid
     * @param histology histology
     * @return true if the histology is valid
     */
    public boolean isValidHistology(String histology) {
        boolean valid = (histology != null);

        if (valid) {
            StagingTable table = getTable(HISTOLOGY_TABLE);
            if (table == null)
                throw new IllegalStateException("Unable to locate " + HISTOLOGY_TABLE + " table");

            valid = getValidHistologies().contains(histology);
        }

        return valid;
    }

    /**
     * Parse the string representation of an endpoint into a Endpoint object
     * @param endpoint endpoint String
     * @return an Endpoint object
     */
    public static StagingEndpoint parseEndpoint(String endpoint) {
        String[] parts = endpoint.split(":", 2);

        EndpointType type = null;

        try {
            type = EndpointType.valueOf(parts[0].trim());
        }
        catch (IllegalArgumentException e) {
            // catch exception; it will be re-thrown below
        }

        // make sure type was valid
        if (type == null)
            throw new IllegalStateException("Invalid endpoint type: '" + endpoint + "'.  Must be either JUMP, VALUE, MATCH, STOP, or ERROR");

        String value = parts.length == 2 ? parts[1].trim() : null;

        // some endpoint types do not require a value but these do
        if ((value == null || value.isEmpty()) && EndpointType.JUMP.equals(type))
            throw new IllegalStateException("JUMP endpoint types must have a value: '" + endpoint + "'");

        return new StagingEndpoint(type, value);
    }

    /**
     * Parses a string in having lists of ranges into a List of StringRange objects
     * @param values String representing sets value ranges
     * @return a parsed list of string Range objects
     */
    public static List<StagingStringRange> splitValues(String values) {
        List<StagingStringRange> convertedRanges = new ArrayList<StagingStringRange>();

        if (values != null) {
            // if the value of the string is "*", then consider it as matching anything
            if (values.equals("*"))
                convertedRanges.add(_MATCH_ALL_ENDPOINT);
            else {
                String[] ranges = values.split(",");

                for (String range : ranges) {
                    // Not sure if this is the best way to handle this, but I ran into a problem when I converted the CS data.  One of the tables had
                    // a value of "N0(mol-)".  This fails since it considers it a range and we require our ranges to have the same size on both sides.
                    // The only thing I can think of is for cases like this, assume it is not a range and use the whole string as a non-range value.
                    // The problem is that if something is entered incorrectly and was supposed to be a range, we will not process it correctly.  We
                    // may need to revisit this issue later.
                    String[] parts = range.split("-");
                    if (parts.length == 1)
                        convertedRanges.add(new StagingStringRange(parts[0].trim(), parts[0].trim()));
                    else if (parts.length == 2) {
                        // don't worry about length differences if one of the parts is a context variable
                        if (parts[0].trim().length() != parts[1].trim().length() && !DecisionEngine.isReferenceVariable(parts[0].trim()) && !DecisionEngine.isReferenceVariable(parts[1].trim()))
                            convertedRanges.add(new StagingStringRange(range.trim(), range.trim()));
                        else
                            convertedRanges.add(new StagingStringRange(parts[0].trim(), parts[1].trim()));
                    }
                    else
                        convertedRanges.add(new StagingStringRange(range.trim(), range.trim()));
                }
            }
        }

        return convertedRanges;
    }

    /**
     * Return the ObjectMapper instance
     * @return ObjectMapper instance
     */
    public ObjectMapper getMapper() {
        return _MAPPER;
    }

    /**
     * Return the algorithm associated with the provider
     * @return algorithm id
     */
    public abstract String getAlgorithm();

    /**
     * Return the version associated with the provider
     * @return version number
     */
    public abstract String getVersion();

    @Override
    public abstract StagingTable getTable(String id);

    @Override
    public abstract StagingSchema getDefinition(String id);

    /**
     * Return a set of all schema identifiers
     * @return a Set of schema identifiers
     */
    public abstract Set<String> getSchemaIds();

    /**
     * Return a set of all the table names
     * @return a List of table identifier
     */
    public abstract Set<String> getTableIds();

    /**
     * Return all the legal site values
     * @return a set of valid sites
     */
    public Set<String> getValidSites() {
        try {
            return _validValuesCache.get(PRIMARY_SITE_TABLE);
        }
        catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause());
        }
        catch (UncheckedExecutionException e) {
            throw new IllegalStateException(e.getCause());
        }
    }

    /**
     * Return all the legal histology values
     * @return a set of valid histologies
     */
    public Set<String> getValidHistologies() {
        try {
            return _validValuesCache.get(HISTOLOGY_TABLE);
        }
        catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause());
        }
        catch (UncheckedExecutionException e) {
            throw new IllegalStateException(e.getCause());
        }
    }

    /**
     * Look up a schema based on site, histology and an optional discriminator.
     * @param lookup schema lookup input
     * @return a list of StagingSchemaInfo objects
     */
    public List<StagingSchema> lookupSchema(SchemaLookup lookup) {
        // If doing a more broad lookup without giving both site and histology, do not use the cache.  I don't want to cache
        // since the results could include all the data
        if (lookup.getSite() == null || lookup.getHistology() == null)
            return getSchemas(lookup);

        try {
            return _lookupCache.get(lookup);
        }
        catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause());
        }
        catch (UncheckedExecutionException e) {
            throw new IllegalStateException(e.getCause());
        }
    }

    /**
     * Look up a schema based on site, histology and an optional discriminator.
     * @param lookup schema lookup input
     * @return a list of StagingSchemaInfo objects
     */
    private List<StagingSchema> getSchemas(SchemaLookup lookup) {
        List<StagingSchema> matchedSchemas = new ArrayList<StagingSchema>();

        String site = lookup.getInput(StagingData.PRIMARY_SITE_KEY);
        String histology = lookup.getInput(StagingData.HISTOLOGY_KEY);
        boolean hasDiscriminator = lookup.hasDiscriminator();

        // site or histology must be supplied and they must be valid; I am assuming that all algorithms must have tables that validate
        // both site and histology
        if ((site != null && !isValidSite(site)) || (histology != null && !isValidHistology(histology)))
            return matchedSchemas;

        // searching on a discriminator is only supported if also searching on site and histology; if ssf25 supplied without either
        // of those fields, return no results
        if (hasDiscriminator && (site == null || site.isEmpty() || histology == null || histology.isEmpty()))
            return matchedSchemas;

        // site or histology must be supplied
        if (site != null || histology != null) {
            Set<String> keysToMatch = new HashSet<String>();

            if (site != null)
                keysToMatch.add(StagingData.PRIMARY_SITE_KEY);
            if (histology != null)
                keysToMatch.add(StagingData.HISTOLOGY_KEY);

            // sometimes discriminator is a default value (like 988), so first search for site/hist only match even if discriminator was supplied
            for (String schemaId : getSchemaIds()) {
                StagingSchema schema = getDefinition(schemaId);

                if (schema.getSchemaSelectionTable() != null) {
                    StagingTable table = getTable(schema.getSchemaSelectionTable());
                    if (table != null && DecisionEngine.matchTable(table, lookup.getInputs(), keysToMatch) != null)
                        matchedSchemas.add(schema);
                }
            }

            // if multiple matches were found on site/hist and a discriminator was supplied, trim down the list
            if (hasDiscriminator && matchedSchemas.size() > 1) {
                List<StagingSchema> trimmedMatches = new ArrayList<StagingSchema>();

                for (StagingSchema schema : matchedSchemas) {
                    if (schema.getSchemaSelectionTable() != null) {
                        StagingTable table = getTable(schema.getSchemaSelectionTable());
                        if (table != null && DecisionEngine.matchTable(table, lookup.getInputs()) != null)
                            trimmedMatches.add(schema);
                    }
                }

                matchedSchemas = trimmedMatches;
            }
        }

        return matchedSchemas;
    }

    /**
     * Given a table, return a Set of all the distinct input values.  This is for tables that have a single INPUT column.
     * @param tableId table identifier
     * @return A set of unique inputs
     */
    private Set<String> getAllInputValues(String tableId) {
        Set<String> values = new HashSet<String>();

        // if the table is not found, return right away with an empty list
        StagingTable table = getTable(tableId);
        if (table == null)
            return values;

        // find the input key
        Set<String> inputKeys = new HashSet<String>();
        for (StagingColumnDefinition def : table.getColumnDefinitions())
            if (ColumnType.INPUT.equals(def.getType()))
                inputKeys.add(def.getKey());

        if (inputKeys.size() != 1)
            throw new IllegalStateException("Table '" + table.getId() + "' must have one and only one INPUT column.");

        String inputKey = inputKeys.iterator().next();
        for (StagingTableRow row : table.getTableRows()) {
            for (StagingStringRange range : row.getColumnInput(inputKey)) {
                if (range.getLow() != null) {
                    if (range.getLow().equals(range.getHigh()))
                        values.add(range.getLow());
                    else {
                        Integer low = Integer.parseInt(range.getLow());
                        Integer high = Integer.parseInt(range.getHigh());

                        // add all values in range
                        for (Integer i = low; i <= high; i++)
                            values.add(Strings.padStart(String.valueOf(i), range.getLow().length(), '0'));
                    }
                }
            }

        }

        return values;
    }

}
