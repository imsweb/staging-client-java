/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.imsweb.decisionengine.ColumnDefinition;
import com.imsweb.decisionengine.ColumnDefinition.ColumnType;
import com.imsweb.decisionengine.DecisionEngine;
import com.imsweb.decisionengine.Error;
import com.imsweb.decisionengine.Input;
import com.imsweb.decisionengine.Mapping;
import com.imsweb.decisionengine.Output;
import com.imsweb.decisionengine.Result;
import com.imsweb.decisionengine.Result.Type;
import com.imsweb.decisionengine.Schema;
import com.imsweb.decisionengine.Table;
import com.imsweb.decisionengine.TablePath;
import com.imsweb.staging.entities.GlossaryDefinition;
import com.imsweb.staging.entities.GlossaryHit;

import static com.imsweb.decisionengine.ColumnDefinition.ColumnType.DESCRIPTION;

public final class Staging {

    // context keys definitions
    public static final String CTX_ALGORITHM_VERSION = "ctx_alg_version";
    public static final String CTX_YEAR_CURRENT = "ctx_year_current";

    // list of all context keys
    public static final List<String> CONTEXT_KEYS = Collections.unmodifiableList(Arrays.asList(CTX_ALGORITHM_VERSION, CTX_YEAR_CURRENT));

    private final DecisionEngine _engine;
    private final StagingDataProvider _provider;

    /**
     * Private constructor
     * @param provider data provider
     */
    private Staging(StagingDataProvider provider) {
        _provider = provider;
        _engine = new DecisionEngine(provider);
    }

    /**
     * Create an instance of the Staging object with a pre-constructed provider
     * @param provider StagingDataProvider
     * @return a Staging instance
     */
    public static Staging getInstance(StagingDataProvider provider) {
        return new Staging(provider);
    }

    /**
     * Return the algorithm name
     * @return an Algorithm
     */
    public String getAlgorithm() {
        return _provider.getAlgorithm();
    }

    /**
     * Return the data version
     * @return a version string
     */
    public String getVersion() {
        return _provider.getVersion();
    }

    /**
     * Return the list a schema ids
     * @return a Set of Strings of schema ids
     */
    public Set<String> getSchemaIds() {
        return _provider.getSchemaIds();
    }

    /**
     * Return a schema based on schema id
     * @param id Schema identifier
     * @return an Algorithm object
     */
    public Schema getSchema(String id) {
        return _provider.getSchema(id);
    }

    /**
     * Return a list of glossary matches for the specific schema
     * @param id Schema identifier
     * @return a set of glossary terms
     */
    public Set<String> getSchemaGlossary(String id) {
        Set<String> hits = new HashSet<>();

        Schema schema = getSchema(id);
        if (schema != null) {
            addGlossaryMatches(hits, schema.getName());
            addGlossaryMatches(hits, schema.getTitle());
            addGlossaryMatches(hits, schema.getDescription());
            addGlossaryMatches(hits, schema.getSubtitle());
            addGlossaryMatches(hits, schema.getNotes());
        }

        return hits;
    }

    /**
     * Return true if the site is valid
     * @param site primary site
     * @return true if the side is valid
     */
    public boolean isValidSite(String site) {
        return _provider.isValidSite(site);
    }

    /**
     * Return true if the histology is valid
     * @param histology histology
     * @return true if the histology is valid
     */
    public boolean isValidHistology(String histology) {
        return _provider.isValidHistology(histology);
    }

    /**
     * Look up a schema based on site, histology and an optional discriminator.
     * @param lookup schema lookup input
     * @return a list of StagingSchema objects
     */
    public List<Schema> lookupSchema(SchemaLookup lookup) {
        return _provider.lookupSchema(lookup);
    }

    /**
     * Return a complete list of mapping table names
     * @return Set of String objects containing table names
     */
    public Set<String> getTableIds() {
        return _provider.getTableIds();
    }

    /**
     * Return a mapping table based on table name
     * @param id table identifier
     * @return Table object
     */
    public Table getTable(String id) {
        return _provider.getTable(id);
    }

    /**
     * Return a list of glossary matches for the specific table
     * @param id Table identifier
     * @return a set of glossary terms
     */
    public Set<String> getTableGlossary(String id) {
        Set<String> hits = new HashSet<>();

        Table table = getTable(id);
        if (table != null) {
            // add all the String fields
            addGlossaryMatches(hits, table.getName());
            addGlossaryMatches(hits, table.getTitle());
            addGlossaryMatches(hits, table.getDescription());
            addGlossaryMatches(hits, table.getSubtitle());
            addGlossaryMatches(hits, table.getNotes());
            addGlossaryMatches(hits, table.getFootnotes());

            // add any DESCRIPTION columns glossary matches
            if (table.getColumnDefinitions() != null && table.getRawRows() != null) {
                Set<Integer> descriptionCols = IntStream.range(0, table.getColumnDefinitions().size())
                        .filter(i -> DESCRIPTION.equals(table.getColumnDefinitions().get(i).getType()))
                        .boxed()
                        .collect(Collectors.toSet());
                for (List<String> row : table.getRawRows())
                    for (Integer col : descriptionCols)
                        addGlossaryMatches(hits, row.get(col));
            }
        }

        return hits;
    }

    /**
     * Check the code validity of a single field in a schema.  If the schema or field do no exist, false will be returned.
     * @param schemaId schema identifier
     * @param key input key
     * @param value value to check validity
     * @return a boolean indicating whether the code exists for the the passed schema field
     */
    public boolean isCodeValid(String schemaId, String key, String value) {
        Map<String, String> context = new HashMap<>();

        // add the value to the context
        context.put(key, value);

        // add the context keys
        addContextKeys(context);

        return isContextValid(schemaId, key, context);
    }

    /**
     * Check the validity of a single field of a schema based on the supplied context.  The value of this key should be in the context as well
     * as any other properties needed to evaluation validity.  If the schema or field do no exist, false will be returned.
     * @param schemaId schema identifier
     * @param key input key
     * @param context Map of keys/values to validate against
     * @return a boolean indicating whether the code exists for the the passed schema field
     */
    public boolean isContextValid(String schemaId, String key, Map<String, String> context) {
        // first get the algorithm
        Schema schema = getSchema(schemaId);
        if (schema == null)
            return false;

        // get the table id from the schema
        Input input = schema.getInputMap().get(key);
        if (input == null)
            return false;

        // missing context will always return false
        if (context == null || context.isEmpty())
            return false;

        // all context input needs to be trimmed
        Map<String, String> testContext = new HashMap<>();
        for (Entry<String, String> entry : context.entrySet())
            testContext.put(entry.getKey(), entry.getValue() != null ? entry.getValue().trim() : "");

        // if the input specifies a table for validation, test against it
        if (input.getTable() != null) {
            Table table = getTable(input.getTable());

            return table != null && (DecisionEngine.matchTable(table, testContext) != null);
        }

        return true;
    }

    /**
     * For a given table, return the index of the row that matches the key/value combination.  If you need to match on more than
     * one input, see the other findMatchingTableRow.
     * @param tableId table identifier
     * @param key input key
     * @param value value
     * @return the index of the matching table row or null if no match was found
     */
    public Integer findMatchingTableRow(String tableId, String key, String value) {
        Map<String, String> context = new HashMap<>();

        context.put(key, value);

        return findMatchingTableRow(tableId, context);
    }

    /**
     * For a given table, return the index of the row that matches supplied context.
     * @param tableId table identifier
     * @param context context of input key/values to use to match
     * @return the index of the matching table row or null if no match was found
     */
    public Integer findMatchingTableRow(String tableId, Map<String, String> context) {
        Integer rowIndex = null;

        // add the context keys
        addContextKeys(context);

        Table table = getTable(tableId);
        if (table != null)
            rowIndex = DecisionEngine.findMatchingTableRow(table, context);

        return rowIndex;
    }

    /**
     * Return a list of tables identifiers involved in the specified schema
     * @param schemaId schema identifier
     * @return a Set of table identifiers; if the schema is not found the set will be empty
     */
    public Set<String> getInvolvedTables(String schemaId) {
        Set<String> tables = new HashSet<>();

        Schema schema = getSchema(schemaId);
        if (schema != null && schema.getInvolvedTables() != null)
            tables = schema.getInvolvedTables();

        return tables;
    }

    /**
     * Return a list of schema identifiers which the specified table is used in
     * @param tableId table identifier
     * @return a Set of schema identifiers
     */
    public Set<String> getInvolvedSchemas(String tableId) {
        // loop over all schemas and find the ones that have the passed table identifier in the involved table set
        return getSchemaIds().stream().filter(schemaId -> getInvolvedTables(schemaId).contains(tableId)).collect(Collectors.toSet());
    }

    /**
     * Looks at all tables involved in the table path and returns a list of input keys that could be used.  This also includes the input keys
     * used in jumps tables.
     * @param path a StagingTablePath
     * @return a Set of unique input keys
     */
    public Set<String> getInputs(TablePath path) {
        return getInputs(path, new HashSet<>());
    }

    /**
     * Looks at all tables involved in the table path and returns a list of input keys that could be used.  This also includes the input keys
     * used in jumps tables.
     * @param path a StagingTablePath
     * @param excludedInputs a list of input keys to not consider as inputs
     * @return a Set of unique input keys
     */
    public Set<String> getInputs(TablePath path, Set<String> excludedInputs) {
        Set<String> inputs = new HashSet<>();

        if (path != null)
            inputs.addAll(_engine.getInputs(path, excludedInputs));

        // always remove all context variables since they are never needed to be supplied
        CONTEXT_KEYS.forEach(inputs::remove);

        return inputs;
    }

    /**
     * Looks at all tables involved in the mapping and returns a list of input keys that could be used.  This also includes the input keys
     * used in the inclusion and exclusion tables if any.
     * @param mapping a StagingMapping
     * @return a Set of unique input keys
     */
    public Set<String> getInputs(Mapping mapping) {
        return getInputs(mapping, null, new HashSet<>());
    }

    /**
     * Looks at all tables involved in the mapping and returns a list of input keys that could be used.  This always includes the input keys
     * used in the inclusion and exclusion tables if any.  The inputs from each table path will only be included if it passes the inclusion/exclusion
     * criteria based on the context.
     * @param mapping a StagingMapping
     * @param context a context of values used to to check mapping inclusion/exclusion
     * @param excludedInputs a list of input keys to not consider as inputs
     * @return a Set of unique input keys
     */
    public Set<String> getInputs(Mapping mapping, Map<String, String> context, Set<String> excludedInputs) {
        Set<String> inputs = new HashSet<>();

        // the inclusion tables are always evaluated so any inputs there should be added always
        if (mapping.getInclusionTables() != null)
            for (TablePath path : mapping.getInclusionTables())
                inputs.addAll(getInputs(path, excludedInputs));

        // the exclusion tables are always evaluated so any inputs there should be added always
        if (mapping.getExclusionTables() != null)
            for (TablePath path : mapping.getExclusionTables())
                inputs.addAll(getInputs(path, excludedInputs));

        // if there are tables paths and if the mapping is involved, add the inputs
        if (mapping.getTablePaths() != null)
            if (context == null || _engine.isMappingInvolved(mapping, context))
                inputs.addAll(_engine.getInputs(mapping, excludedInputs));

        // always remove all context variables since they are never needed to be supplied
        CONTEXT_KEYS.forEach(inputs::remove);

        return inputs;
    }

    /**
     * Looks at all tables involved in all the mappings in the definition and returns a list of input keys that could be used.  It will also deal with mapped
     * inputs.  Note that if an input to a table was not a supplied input (i.e. it was created as an output of a previous table) it will not be included
     * in the list of inputs.  The inputs will also include any used in schema selection.  All inputs returned from this method should be in the schema input
     * list otherwise there is a problem with the schema.
     * @param schema a StagingSchema
     * @return a Set of unique input keys
     */
    public Set<String> getInputs(Schema schema) {
        return getInputs(schema, null);
    }

    /**
     * Looks at all tables involved in all the mappings in the definition and returns a list of input keys that could be used.  It will also deal with mapped
     * inputs.  The inputs from each mapping will only be included if it passes the inclusion/exclusion criteria based on the context.  Note that if an input
     * to a table was not a supplied input (i.e. it was created as an output of a previous table) it will not be included in the list of inputs.  The inputs will
     * also include any used in schema selection.  All inputs returned from this method should be in the schema input list otherwise there is a problem with the
     * schema.
     * @param schema a StagingSchema
     * @param context a context of values used to to check mapping inclusion/exclusion
     * @return a Set of unique input keys
     */
    public Set<String> getInputs(Schema schema, Map<String, String> context) {
        Set<String> inputs = new HashSet<>();

        // add schema selection fields
        if (schema.getSchemaSelectionTable() != null) {
            Table table = getTable(schema.getSchemaSelectionTable());
            if (table != null) {
                for (ColumnDefinition def : table.getColumnDefinitions())
                    if (ColumnType.INPUT.equals(def.getType()))
                        inputs.add(def.getKey());
            }
        }

        // process all mappings
        if (schema.getMappings() != null) {
            Set<String> excludedInputs = new HashSet<>();

            for (Mapping mapping : schema.getMappings())
                inputs.addAll(getInputs(mapping, context, excludedInputs));
        }

        // always remove all context variables since they are never needed to be supplied
        CONTEXT_KEYS.forEach(inputs::remove);

        return inputs;
    }

    /**
     * Looks at a table path (and all jump tables within in) and returns a list of output keys that could be created.
     * @param path a StagingTablePath
     * @return a Set of unique output keys
     */
    public Set<String> getOutputs(TablePath path) {
        return _engine.getOutputs(path);
    }

    /**
     * Looks at all tables involved in the mapping and returns a list of output keys that could be created.  This also includes the output keys
     * used in the inclusion and exclusion tables if any.
     * @param mapping a StagingMapping
     * @return a Set of unique output keys
     */
    public Set<String> getOutputs(Mapping mapping) {
        return getOutputs(mapping, null);
    }

    /**
     * Looks at all tables involved in the mapping and returns a list of output keys that could be created.  This also includes the output keys
     * used in the inclusion and exclusion tables if any.  The outputs from each mapping will only be included if it passes the inclusion/exclusion
     * criteria based on the context.
     * @param mapping a StagingMapping
     * @param context a context of values used to to check mapping inclusion/exclusion
     * @return a Set of unique output keys
     */
    public Set<String> getOutputs(Mapping mapping, Map<String, String> context) {
        Set<String> outputs = new HashSet<>();

        if (mapping.getTablePaths() != null) {
            if (context == null || _engine.isMappingInvolved(mapping, context))
                outputs.addAll(_engine.getOutputs(mapping));
        }

        return outputs;
    }

    /**
     * Looks at all tables involved in all the mappings in the definition and returns a list of output keys that could be created.  It will also deal with mapped
     * outputs.
     * @param schema a StagingSchema
     * @return a Set of unique output keys
     */
    public Set<String> getOutputs(Schema schema) {
        return getOutputs(schema, null);
    }

    /**
     * Looks at all tables involved in all the mappings in the definition and returns a list of output keys that will be created.  It will also deal with mapped
     * outputs.  The outputs from each mapping will only be included if it passes the inclusion/exclusion criteria based on the context.  If the schema has StagingOutputs
     * defined, then the calulated output list is exactly the same as the schema output list.
     * @param schema a StagingSchema
     * @param context a context of values used to to check mapping inclusion/exclusion
     * @return a Set of unique output keys
     */
    public Set<String> getOutputs(Schema schema, Map<String, String> context) {
        Set<String> outputs = new HashSet<>();

        // if outputs are defined in the schema, then there is no reason to look any further into the mappings; the output defines exactly what keys will
        // be returned and it doesn't matter what context is passed in that case
        if (schema.getOutputMap() != null) {
            for (Entry<String, ? extends Output> entry : schema.getOutputMap().entrySet())
                outputs.add(entry.getKey());

            return outputs;
        }

        // if outputs were not defined, then the tables involved in the mappings will be used to determine the possible outputs

        if (schema.getMappings() != null)
            for (Mapping mapping : schema.getMappings())
                outputs.addAll(getOutputs(mapping, context));

        // if valid outputs are defined on the schema level, only return outputs that defined; this removed "temporary" outputs that may be defined during the
        // staging process
        if (schema.getOutputMap() != null)
            outputs.removeIf(entry -> !schema.getOutputMap().containsKey(entry));

        return outputs;
    }

    /**
     * Stage the passed case.
     * @param data all input values are passed through this database
     * @return the same StagingData with output values filled in
     */
    public StagingData stage(StagingData data) {
        // first clear out schema/output/errors/path
        data.setSchemaId(null);
        data.setOutput(new HashMap<>());
        data.setErrors(new ArrayList<>());
        data.setPath(new ArrayList<>());

        // make sure site and histology are supplied
        if (data.getInput(StagingData.PRIMARY_SITE_KEY) == null || data.getInput(StagingData.HISTOLOGY_KEY) == null) {
            data.setResult(StagingData.Result.FAILED_MISSING_SITE_OR_HISTOLOGY);
            return data;
        }

        // get the schema; if a single schema is not found, return right away with an error
        List<Schema> schemas = lookupSchema(new SchemaLookup(data.getInput()));
        if (schemas.size() != 1) {
            if (schemas.size() == 0)
                data.setResult(StagingData.Result.FAILED_NO_MATCHING_SCHEMA);
            else
                data.setResult(StagingData.Result.FAILED_MULITPLE_MATCHING_SCHEMAS);
            return data;
        }

        Schema schema = schemas.get(0);

        // add schema id to result
        data.setSchemaId(schema.getId());

        // copy the input into a new context
        Map<String, String> context = new HashMap<>(data.getInput());

        // make sure all supplied inputs are defined in the definition
        for (Entry<String, String> entry : context.entrySet())
            if (!schema.getInputMap().containsKey(entry.getKey()))
                data.addError(new Error.ErrorBuilder(Error.Type.UNKNOWN_INPUT).message("Unknown input key supplied: " + entry.getKey()).key(entry.getKey()).build());

        if (data.getErrors().size() > 0) {
            data.setResult(StagingData.Result.FAILED_INVALID_INPUT);
            return data;
        }

        // add context variables
        addContextKeys(context);

        // check that year of DX is valid
        if (!isContextValid(schema.getId(), StagingData.YEAR_DX_KEY, context)) {
            data.setResult(StagingData.Result.FAILED_INVALID_YEAR_DX);
            return data;
        }

        // perform the staging
        Result result = _engine.process(schema.getId(), context);

        // remove the context variables
        removeContextKeys(context);

        // set the staging data result based on the Result returned from the DecisionEngine
        if (Type.FAILED_INPUT.equals(result.getType()))
            data.setResult(StagingData.Result.FAILED_INVALID_INPUT);
        else
            data.setResult(StagingData.Result.STAGED);

        // remove the original input keys from the resulting context;  in addition, we want to remove any input keys
        // from the resulting context that were set with a default value; to accomplish this remove all keys that are
        // defined as input in the selected schema
        Set<String> definedOutputs = new HashSet<>();
        if (schema.getOutputs() != null)
            definedOutputs = schema.getOutputs().stream().map(Output::getKey).collect(Collectors.toSet());
        for (Entry<String, String> entry : data.getInput().entrySet())
            if (!definedOutputs.contains(entry.getKey()))
                context.remove(entry.getKey());
        for (Input input : schema.getInputs())
            if (!definedOutputs.contains(input.getKey()))
                context.remove(input.getKey());

        // add the results to the data card
        data.setOutput(result.getContext());
        data.setErrors(result.getErrors());
        data.setPath(result.getPath());

        return data;
    }

    /**
     * Return a list of all supported glossary terms
     * @return a set of terms
     */
    public Set<String> getGlossaryTerms() {
        return _provider.getGlossaryTerms();
    }

    /**
     * Return the definition of a glossary term
     * @param term glossery term
     * @return full glossary definition
     */
    public GlossaryDefinition getGlossaryDefinition(String term) {
        return _provider.getGlossaryDefinition(term);
    }

    /**
     * Return a list of glossary terms in the passed text
     * @param text text to match against
     * @return a list of glossary terms
     */
    public Collection<GlossaryHit> getGlossaryMatches(String text) {
        return _provider.getGlossaryMatches(text);
    }

    /**
     * Add the context keys which are used in staging and other calls
     * @param context Map of context
     */
    private void addContextKeys(Map<String, String> context) {
        // make the algorithm version available in the context
        context.put(CTX_ALGORITHM_VERSION, getVersion());

        // put the current year in the context
        Calendar now = Calendar.getInstance();
        context.put(CTX_YEAR_CURRENT, String.valueOf(now.get(Calendar.YEAR)));
    }

    /**
     * Remove all context keys
     * @param context Map of context
     */
    private void removeContextKeys(Map<String, String> context) {
        context.remove(CTX_ALGORITHM_VERSION);
        context.remove(CTX_YEAR_CURRENT);
    }

    /**
     * Helper method to collect glossary matches
     */
    private void addGlossaryMatches(Set<String> hits, String text) {
        if (text != null) {
            Collection<GlossaryHit> matches = getGlossaryMatches(text);
            if (matches != null)
                matches.forEach(m -> hits.add(m.getTerm()));
        }
    }

}
