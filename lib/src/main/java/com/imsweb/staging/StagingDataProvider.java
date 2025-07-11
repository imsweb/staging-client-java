/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.ahocorasick.trie.Trie;
import org.apache.commons.lang3.math.NumberUtils;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.imsweb.staging.engine.DecisionEngine;
import com.imsweb.staging.entities.ColumnDefinition;
import com.imsweb.staging.entities.ColumnDefinition.ColumnType;
import com.imsweb.staging.entities.DataProvider;
import com.imsweb.staging.entities.Endpoint;
import com.imsweb.staging.entities.Endpoint.EndpointType;
import com.imsweb.staging.entities.GlossaryDefinition;
import com.imsweb.staging.entities.GlossaryHit;
import com.imsweb.staging.entities.Input;
import com.imsweb.staging.entities.KeyValue;
import com.imsweb.staging.entities.Mapping;
import com.imsweb.staging.entities.Output;
import com.imsweb.staging.entities.Range;
import com.imsweb.staging.entities.Schema;
import com.imsweb.staging.entities.SchemaLookup;
import com.imsweb.staging.entities.StagingData;
import com.imsweb.staging.entities.Table;
import com.imsweb.staging.entities.TableRow;

import static com.imsweb.staging.Staging.CONTEXT_KEYS;

/**
 * An abstract implementation of DataProvider customized for handling staging schemas/tables
 */
@SuppressWarnings("deprecation")
public abstract class StagingDataProvider implements DataProvider {

    // tables that all algorithm versions must have
    public static final String PRIMARY_SITE_TABLE = "primary_site";
    public static final String HISTOLOGY_TABLE = "histology";

    private final ObjectMapper _mapper = new ObjectMapper();
    private final Range _matchAllEndpoint = getMatchAllRange();

    protected Trie _trie;

    // lookup cache
    private final Cache<SchemaLookup, List<Schema>> _lookupCache;

    // site/hist cache
    private final Cache<String, Set<String>> _validValuesCache;

    /**
     * Constructor loads all schemas and sets up cache
     */
    protected StagingDataProvider() {
        // do not write null values
        _mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        _mapper.setSerializationInclusion(Include.NON_NULL);

        // set Date objects to output in readable customized format
        _mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // output all dates in ISO-8061 format and UTC time
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        _mapper.setDateFormat(dateFormat);

        _mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        _mapper.setVisibility(PropertyAccessor.GETTER, Visibility.ANY);

        // cache schema lookups
        _lookupCache = new Cache2kBuilder<SchemaLookup, List<Schema>>() {}
                .entryCapacity(500)
                .eternal(true)
                .loader(this::getSchemas)
                .build();

        // cache the valid values for certain tables including site and histology
        _validValuesCache = new Cache2kBuilder<String, Set<String>>() {}
                .eternal(true)
                .loader(this::getAllInputValues)
                .build();
    }

    /**
     * Initialize a schema.
     * @param schema schema entity
     * @return initialized schema entity
     */
    @SuppressWarnings("UnusedReturnValue")
    public Schema initSchema(Schema schema) {
        // parse the schema selection ranges
        if (schema.getSchemaSelectionTable() == null)
            throw new IllegalStateException("Schemas must have a schema selection table.");

        // store the inputs in a Map that can be searched more efficiently
        if (schema.getInputs() != null) {
            Map<String, Input> parsedInputMap = new HashMap<>();

            for (Input input : schema.getInputs()) {
                // verify that all inputs contain a key
                if (input.getKey() == null)
                    throw new IllegalStateException("All input definitions must have a 'key' defined.");

                parsedInputMap.put(input.getKey(), input);
            }

            schema.setInputMap(parsedInputMap);
        }

        // store the outputs in a Map that can be searched more efficiently
        if (schema.getOutputs() != null) {
            Map<String, Output> parsedOutputMap = new HashMap<>();

            for (Output output : schema.getOutputs()) {
                // verify that all inputs contain a key
                if (output.getKey() == null)
                    throw new IllegalStateException("All output definitions must have a 'key' defined.");

                parsedOutputMap.put(output.getKey(), output);
            }

            schema.setOutputMap(parsedOutputMap);
        }

        // make sure that the mapping initial context does not set a value for an input field
        if (schema.getMappings() != null)
            for (Mapping mapping : schema.getMappings())
                if (mapping.getInitialContext() != null)
                    for (KeyValue kv : mapping.getInitialContext())
                        if (schema.getInputMap().containsKey(kv.getKey()))
                            throw new IllegalStateException("The key '" + kv.getKey() + "' is defined in an initial context, but that is not allowed since it is also defined as an input.");

        return schema;
    }

    /**
     * Initialize a table.
     * @param table table entity
     * @return initialized table entity
     */
    @SuppressWarnings({"UnusedReturnValue", "java:S3776"})
    public Table initTable(Table table) {
        Set<String> extraInputs = new HashSet<>();

        // empty out the parsed rows
        table.clearTableRows();

        if (table.getRawRows() != null) {
            for (List<String> row : table.getRawRows()) {
                TableRow tableRowEntity = getTableRow();

                // make sure the number of cells in the row matches the number of columns defined
                if (table.getColumnDefinitions().size() != row.size())
                    throw new IllegalStateException("Table '" + table.getId() + "' has a row with " + row.size() + " values but should have " + table.getColumnDefinitions().size() + ": " + row);

                // loop over the column definitions in order since the data needs to retrieved by array position
                for (int i = 0; i < table.getColumnDefinitions().size(); i++) {
                    ColumnDefinition col = table.getColumnDefinitions().get(i);
                    String cellValue = row.get(i);

                    switch (col.getType()) {
                        case INPUT:
                            // if there are no ranges in the list, that means the cell was "blank" and is not needed in the table row
                            List<? extends Range> ranges = splitValues(cellValue);
                            if (!ranges.isEmpty()) {
                                tableRowEntity.addInput(col.getKey(), ranges);

                                // if there are key references used (values that reference other inputs) like {{key}}, then add them to the extra inputs list
                                for (Range range : ranges) {
                                    if (DecisionEngine.isReferenceVariable(range.getLow()))
                                        extraInputs.add(DecisionEngine.trimBraces(range.getLow()));
                                    if (DecisionEngine.isReferenceVariable(range.getHigh()))
                                        extraInputs.add(DecisionEngine.trimBraces(range.getHigh()));
                                }
                            }
                            break;
                        case ENDPOINT:
                            Endpoint endpoint = parseEndpoint(cellValue);
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

                table.addTableRow(tableRowEntity);
            }
        }

        // add extra inputs, if any; do not include context variables since they are not user input
        CONTEXT_KEYS.forEach(extraInputs::remove);
        table.setExtraInput(extraInputs.isEmpty() ? null : extraInputs);

        return table;
    }

    /**
     * Parse the string representation of an endpoint into an Endpoint object
     * @param endpoint endpoint String
     * @return an Endpoint object
     */
    public Endpoint parseEndpoint(String endpoint) {
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

        return getEndpoint(type, value);
    }

    /**
     * Parses a string in having lists of ranges into a List of Range objects
     * @param values String representing sets value ranges
     * @return a parsed list of string Range objects
     */
    public List<? extends Range> splitValues(String values) {
        List<Range> convertedRanges = new ArrayList<>();

        if (values != null) {
            // if the value of the string is "*", then consider it as matching anything
            if (values.equals("*"))
                convertedRanges.add(_matchAllEndpoint);
            else {
                // split the string; the "-1" makes sure to not discard empty strings
                String[] ranges = values.split(",", -1);

                for (String range : ranges) {
                    // Not sure if this is the best way to handle this, but I ran into a problem when I converted the CS data.  One of the tables had
                    // a value of "N0(mol-)".  This fails since it considers it a range, and we require our ranges to have the same size on both sides.
                    // The only thing I can think of is for cases like this, assume it is not a range and use the whole string as a non-range value.
                    // The problem is that if something is entered incorrectly and was supposed to be a range, we will not process it correctly.  We
                    // may need to revisit this issue later.
                    String[] parts = range.split("-");
                    if (parts.length == 2) {
                        String low = parts[0].trim();
                        String high = parts[1].trim();

                        // check if both sides of the range are numeric values; if so the length does not have to match
                        boolean isNumericRange = isNumeric(low) && isNumeric(high);

                        // if same length, a numeric range, or one of the parts is a context variable, use the low and high as range; otherwise consider
                        // a single value (i.e. low = high)
                        if (low.length() == high.length() || isNumericRange || DecisionEngine.isReferenceVariable(low) || DecisionEngine.isReferenceVariable(high))
                            convertedRanges.add(getRange(low, high));
                        else
                            convertedRanges.add(getRange(range.trim(), range.trim()));
                    }
                    else
                        convertedRanges.add(getRange(range.trim(), range.trim()));
                }
            }
        }

        return convertedRanges;
    }

    public static boolean isNumeric(String value) {
        return NumberUtils.isParsable(value);
    }

    /**
     * Returns a string, of length at least {@code minLength}, consisting of {@code string} prepended
     * with as many copies of {@code padChar} as are necessary to reach that length.
     */
    @SuppressWarnings("SameParameterValue")
    static String padStart(String string, int minLength, char padChar) {
        if (string == null || string.length() >= minLength)
            return string;

        return String.valueOf(padChar).repeat(minLength - string.length()) + string;
    }

    /**
     * Clear the caches
     */
    public void invalidateCache() {
        _lookupCache.removeAll();
        _validValuesCache.removeAll();
    }

    /**
     * Return true if the site is valid
     * @param site primary site
     * @return true if the side is valid
     */
    public boolean isValidSite(String site) {
        boolean valid = (site != null);

        if (valid) {
            Table table = getTable(PRIMARY_SITE_TABLE);
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
            Table table = getTable(HISTOLOGY_TABLE);
            if (table == null)
                throw new IllegalStateException("Unable to locate " + HISTOLOGY_TABLE + " table");

            valid = getValidHistologies().contains(histology);
        }

        return valid;
    }

    /**
     * Return the ObjectMapper instance
     * @return ObjectMapper instance
     */
    public ObjectMapper getMapper() {
        return _mapper;
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

    /**
     * Return a new endpoint
     * @param type type of endpoint
     * @param value value of endpoint
     * @return Endpoint entity
     */
    public abstract Endpoint getEndpoint(EndpointType type, String value);

    /**
     * Return a new table row
     * @return TableRow entity
     */
    public abstract TableRow getTableRow();

    /**
     * Return a range representing "match all"
     * @return a Range entity
     */
    public abstract Range getMatchAllRange();

    /**
     * Return a newly created range
     * @param low low value
     * @param high high value
     * @return Range entity
     */
    public abstract Range getRange(String low, String high);

    /**
     * Return a set of all schema identifiers
     * @return a Set of schema identifiers
     */
    public abstract Set<String> getSchemaIds();

    /**
     * Return a set of all the table names
     * @return a Set of table identifier
     */
    public abstract Set<String> getTableIds();

    /**
     * Return a set of supported glossary terms
     * @return a Set of terms
     */
    public abstract Set<String> getGlossaryTerms();

    /**
     * Return a defitition of a glossary term
     * @param term glossary term
     * @return a glossary definiiion
     */
    public abstract GlossaryDefinition getGlossaryDefinition(String term);

    /**
     * Return a list of all glossary matches in the supplied text
     * @param text text to match against
     * @return a List of glossary hits
     */
    public Collection<GlossaryHit> getGlossaryMatches(String text) {
        return _trie.parseText(text).stream().map(hit -> new GlossaryHit(hit.getKeyword(), hit.getStart(), hit.getEnd())).collect(Collectors.toSet());
    }

    /**
     * Return all the legal site values
     * @return a set of valid sites
     */
    public Set<String> getValidSites() {
        return _validValuesCache.get(PRIMARY_SITE_TABLE);
    }

    /**
     * Return all the legal histology values
     * @return a set of valid histologies
     */
    public Set<String> getValidHistologies() {
        return _validValuesCache.get(HISTOLOGY_TABLE);
    }

    /**
     * Look up a schema based on site, histology and an optional discriminator.
     * @param lookup schema lookup input
     * @return a list of StagingSchemaInfo objects
     */
    public List<Schema> lookupSchema(SchemaLookup lookup) {
        // If doing a more broad lookup without giving both site and histology, do not use the cache.  I don't want to cache
        // since the results could include all the data
        if (lookup.getSite() == null || lookup.getHistology() == null)
            return getSchemas(lookup);

        return _lookupCache.get(lookup);
    }

    /**
     * Look up a schema based on site, histology and an optional discriminator.
     * @param lookup schema lookup input
     * @return a list of StagingSchemaInfo objects
     */
    private List<Schema> getSchemas(SchemaLookup lookup) {
        List<Schema> matchedSchemas = new ArrayList<>();

        String site = lookup.getInput(StagingData.PRIMARY_SITE_KEY);
        String histology = lookup.getInput(StagingData.HISTOLOGY_KEY);
        boolean hasDiscriminator = lookup.hasDiscriminator();

        // site or histology must be supplied, and they must be valid; I am assuming that all algorithms must have tables that validate
        // both site and histology
        if ((site != null && !isValidSite(site)) || (histology != null && !isValidHistology(histology)))
            return matchedSchemas;

        // searching on a discriminator is only supported if also searching on site and histology; if ssf25 supplied without either
        // of those fields, return no results
        if (hasDiscriminator && (site == null || site.isEmpty() || histology == null || histology.isEmpty()))
            return matchedSchemas;

        // site or histology must be supplied
        if (site != null || histology != null) {
            // loop over selection table and match using only the supplied keys
            for (String schemaId : getSchemaIds()) {
                Schema schema = getSchema(schemaId);

                if (schema.getSchemaSelectionTable() != null) {
                    Table table = getTable(schema.getSchemaSelectionTable());
                    if (table != null && DecisionEngine.matchTable(table, lookup.getInputs(), lookup.getKeys()) != null)
                        matchedSchemas.add(schema);
                }
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
        Set<String> values = new HashSet<>();

        // if the table is not found, return right away with an empty list
        Table table = getTable(tableId);
        if (table == null)
            return values;

        // find the input key
        Set<String> inputKeys = table.getColumnDefinitions().stream()
                .filter(def -> ColumnType.INPUT.equals(def.getType()))
                .map(ColumnDefinition::getKey)
                .collect(Collectors.toSet());

        if (inputKeys.size() != 1)
            throw new IllegalStateException("Table '" + table.getId() + "' must have one and only one INPUT column.");

        String inputKey = inputKeys.iterator().next();
        for (TableRow row : table.getTableRows()) {
            for (Range range : row.getColumnInput(inputKey)) {
                if (range.getLow() != null) {
                    if (range.getLow().equals(range.getHigh()))
                        values.add(range.getLow());
                    else {
                        int low = Integer.parseInt(range.getLow());
                        int high = Integer.parseInt(range.getHigh());

                        // add all values in range
                        for (int i = low; i <= high; i++)
                            values.add(padStart(String.valueOf(i), range.getLow().length(), '0'));
                    }
                }
            }

        }

        return values;
    }

}
