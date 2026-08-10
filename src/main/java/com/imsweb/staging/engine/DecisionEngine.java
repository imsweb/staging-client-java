/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.imsweb.staging.entities.ColumnDefinition;
import com.imsweb.staging.entities.ColumnDefinition.ColumnType;
import com.imsweb.staging.entities.DataProvider;
import com.imsweb.staging.entities.Endpoint;
import com.imsweb.staging.entities.Endpoint.EndpointType;
import com.imsweb.staging.entities.Error.ErrorBuilder;
import com.imsweb.staging.entities.Error.Type;
import com.imsweb.staging.entities.Input;
import com.imsweb.staging.entities.KeyMapping;
import com.imsweb.staging.entities.KeyValue;
import com.imsweb.staging.entities.Mapping;
import com.imsweb.staging.entities.Output;
import com.imsweb.staging.entities.Range;
import com.imsweb.staging.entities.Result;
import com.imsweb.staging.entities.Schema;
import com.imsweb.staging.entities.Table;
import com.imsweb.staging.entities.TablePath;
import com.imsweb.staging.entities.TableRow;

/**
 * An engine for processing declarative algorithms.
 */
public class DecisionEngine {

    // string to use for blank or null in error strings
    public static final String BLANK_OUTPUT = "<blank>";

    private static final Pattern _TEMPLATE_REFERENCE = Pattern.compile("\\{\\{(.*?)}}");
    private static final String _CONTEXT_MISSING_MESSAGE = "Context must not be missing";

    private final DataProvider _provider;

    /**
     * Construct the decision engine with the passed data provider
     * @param provider a DataProvider
     * @throws NullPointerException if provider is null
     */
    public DecisionEngine(DataProvider provider) {
        _provider = Objects.requireNonNull(provider, "Provider must not be null");
    }

    /**
     * Checked whether the value is a reference to another variable or context
     * @param value String value
     * @return true if the value is a reference to another variable or context
     */
    public static boolean isReferenceVariable(String value) {
        return value != null && value.startsWith("{{") && value.endsWith("}}");
    }

    /**
     * Takes a key reference, like {{key}} and returns just the key ("key" in this example)
     * @param value a key refrerence
     * @return the inner key
     */
    public static String trimBraces(String value) {
        if (value.length() > 3)
            return value.substring(2, value.length() - 2);
        else
            return value;
    }

    /**
     * Return the list of endpoints for the matching row in the table; returns null if there is no match
     * @param table a Table
     * @param context a Map containing the context
     * @return returns a List of Endpoint entities from the matching row or null if no match
     */
    public static List<? extends Endpoint> matchTable(Table table, Map<String, String> context) {
        return matchTable(table, context, null);
    }

    /**
     * Return the list of endpoints for the matching row in the table; returns null if there is no match
     * @param table a Table
     * @param context a Map containing the context
     * @param keysToMatch if not null, only keys in this set will be matched against
     * @return returns a List of Endpoint entities from the matching row or null if no match
     */
    public static List<? extends Endpoint> matchTable(Table table, Map<String, String> context, Set<String> keysToMatch) {
        List<? extends Endpoint> endpoints = null;

        Integer index = findMatchingTableRow(table, context, keysToMatch);
        if (index != null)
            endpoints = table.getTableRows().get(index).getEndpoints();

        return endpoints;
    }

    /**
     * Return the matching table row index based on the passed context
     * @param table a Table
     * @param context a Map containing the context
     * @return the index of the matching table row or null if no match was found
     */
    public static Integer findMatchingTableRow(Table table, Map<String, String> context) {
        return findMatchingTableRow(table, context, null);
    }

    /**
     * Return the matching table row index based on the passed context
     * @param table a Table
     * @param context a Map containing the context
     * @param keysToMatch if not null, only keys in this set will be matched against
     * @return the index of the matching table row or null if no match was found
     */
    public static Integer findMatchingTableRow(Table table, Map<String, String> context, Set<String> keysToMatch) {
        Integer rowIndex = null;

        if (context == null)
            throw new IllegalStateException(_CONTEXT_MISSING_MESSAGE);

        if (table.getTableRows() != null) {
            for (int i = 0; i < table.getTableRows().size(); i++) {
                boolean matchAll = true;
                for (ColumnDefinition col : table.getColumnDefinitions()) {
                    if (ColumnType.INPUT.equals(col.getType()) && (keysToMatch == null || keysToMatch.contains(col.getKey())))
                        matchAll = testMatch(table.getTableRows().get(i).getColumnInput(col.getKey()), context.get(col.getKey()), context);

                    if (!matchAll)
                        break;
                }

                // if all inputs match, we are done
                if (matchAll) {
                    rowIndex = i;
                    break;
                }
            }
        }

        return rowIndex;
    }

    /**
     * Tests that a value is contained in a list of ranges; if the list of ranges is missing or empty, then all values will match to it
     * @param values a List of Range objects
     * @param value a value to look for
     * @param context the context will be used to do key lookups when values are in the format of {{var}}
     * @return return true if the value is contained in the List of Range objects
     */
    public static boolean testMatch(List<? extends Range> values, String value, Map<String, String> context) {
        boolean match = (values == null || values.isEmpty());

        if (!match) {
            for (Range range : values) {
                match = range.contains(value, context);
                if (match)
                    break;
            }
        }

        return match;
    }

    /**
     * Translates a value.  If it is a reference to a context, like {{var}} it will return the context value; otherwise
     * it will return the value unchanged.  If the context key does not exist in the context, blank will be returned
     * @param value String value
     * @param context Context for handling variable references
     * @return the context value if a reference, otherwise the original value is returned
     */
    public static String translateValue(String value, Map<String, String> context) {
        if (value != null && value.startsWith("{{")) {
            Matcher m = _TEMPLATE_REFERENCE.matcher(value);
            if (m.matches()) {
                String referencedKey = m.group(1);
                value = context.getOrDefault(referencedKey, "");
            }
        }

        return value;
    }

    /**
     * Return a comma-separated list of input values the table needs taken from the passed context.  Used for error message.
     * @param table a Table
     * @param context a Map of context
     * @return a String representing the input for the table
     */
    static String getTableInputsAsString(Table table, Map<String, String> context) {
        List<String> inputs = new ArrayList<>();

        if (table.getColumnDefinitions() != null)
            for (ColumnDefinition def : table.getColumnDefinitions())
                if (ColumnType.INPUT.equals(def.getType())) {
                    String value = context.get(def.getKey());
                    inputs.add((value == null || value.trim().isEmpty()) ? BLANK_OUTPUT : value.trim());
                }

        return String.join(",", inputs);
    }

    /**
     * Returns the internal data provider
     * @return a DataProvider
     */
    public DataProvider getProvider() {
        return _provider;
    }

    /**
     * Given a mapping and a context, check the inclusion/exclusion tables to see if mapping should be processed
     * @param mapping a Mapping
     * @param context a Map containing the context
     * @return true if the mapping is involved
     */
    public boolean isMappingInvolved(Mapping mapping, Map<String, String> context) {
        if (context == null)
            throw new IllegalStateException(_CONTEXT_MISSING_MESSAGE);

        boolean matches = true;

        // process inclusion table if it exists
        if (mapping.getInclusionTables() != null) {
            for (TablePath path : mapping.getInclusionTables()) {
                // make a copy of the context so mapping changes are only included for a single table path
                Map<String, String> pathContext = new HashMap<>(context);

                Table table = getProvider().getTable(path.getId());
                if (table == null)
                    throw new IllegalStateException("Inclusion table '" + path.getId() + "' does not exist");
                else {
                    // if there is input mapping defined, add the new mapping to the context
                    if (path.getInputMapping() != null) {
                        for (KeyMapping key : path.getInputMapping()) {
                            if (pathContext.containsKey(key.getFrom()))
                                pathContext.put(key.getTo(), pathContext.get(key.getFrom()));
                        }
                    }

                    matches = (matchTable(table, pathContext) != null);
                }

                // stop processing if any inclusion not met
                if (!matches)
                    break;
            }
        }

        // process exclusion table if it exists
        if (matches && mapping.getExclusionTables() != null) {
            for (TablePath path : mapping.getExclusionTables()) {
                // make a copy of the context so mapping changes are only included for a single table path
                Map<String, String> pathContext = new HashMap<>(context);

                Table table = getProvider().getTable(path.getId());
                if (table == null)
                    throw new IllegalStateException("Exclusion table '" + path.getId() + "' does not exist");
                else {
                    // if there is input mapping defined, add the new mapping to the context
                    if (path.getInputMapping() != null) {
                        for (KeyMapping key : path.getInputMapping()) {
                            if (pathContext.containsKey(key.getFrom()))
                                pathContext.put(key.getTo(), pathContext.get(key.getFrom()));
                        }
                    }

                    matches = (matchTable(table, pathContext) == null);
                }

                // stop processing if any exclusion met
                if (!matches)
                    break;
            }
        }

        return matches;
    }

    /**
     * Given a schema and context, return a list of mappings that match inclusion and exclusion criteria
     * Given a schema and context, return a list of mappings that match inclusion and exclusion criteria
     * @param schema a Schema
     * @param context a Map containing the context
     * @return a List of involved Mapping entities
     */
    public List<Mapping> getInvolvedMappings(Schema schema, Map<String, String> context) {
        List<Mapping> mappings = new ArrayList<>();

        if (context == null)
            throw new IllegalStateException(_CONTEXT_MISSING_MESSAGE);

        if (schema.getMappings() != null) {
            for (Mapping mapping : schema.getMappings())
                if (isMappingInvolved(mapping, context))
                    mappings.add(mapping);
        }

        return mappings;
    }

    /**
     * Return a list of tables involved in a schema
     * @param schemaId an schema identifier
     * @return a set of table identifiers
     */
    public Set<String> getInvolvedTables(String schemaId) {
        Schema schema = getProvider().getSchema(schemaId);

        if (schema == null)
            throw new IllegalStateException("Unknown starting table: '" + schemaId + "'");

        return getInvolvedTables(schema);
    }

    /**
     * Return a list of tables involved in a schema.  This includes not only the tables paths, but also tables references in the input section.
     * @param schema a schema
     * @return a set of table identifiers
     */
    public Set<String> getInvolvedTables(Schema schema) {
        Set<String> tables = new LinkedHashSet<>();

        // first, evaluate inputs and outputs
        for (String key : schema.getInputMap().keySet()) {
            Input input = schema.getInputMap().get(key);
            if (input.getTable() != null)
                getInvolvedTables(getProvider().getTable(input.getTable()), tables);
            if (input.getDefaultTable() != null)
                getInvolvedTables(getProvider().getTable(input.getDefaultTable()), tables);
        }
        for (String key : schema.getOutputMap().keySet()) {
            Output output = schema.getOutputMap().get(key);
            if (output.getTable() != null)
                getInvolvedTables(getProvider().getTable(output.getTable()), tables);
        }

        // next loop over mappings and paths
        if (schema.getMappings() != null) {
            for (Mapping mapping : schema.getMappings()) {
                // handle inclusion tables
                if (mapping.getInclusionTables() != null)
                    for (TablePath path : mapping.getInclusionTables())
                        getInvolvedTables(getProvider().getTable(path.getId()), tables);

                // handle exclusion tables
                if (mapping.getExclusionTables() != null)
                    for (TablePath path : mapping.getExclusionTables())
                        getInvolvedTables(getProvider().getTable(path.getId()), tables);

                // handle table paths
                if (mapping.getTablePaths() != null)
                    for (TablePath path : mapping.getTablePaths())
                        getInvolvedTables(getProvider().getTable(path.getId()), tables);
            }
        }

        return tables;
    }

    /**
     * Internal recursive helper function to find the tables that could be called from within a table, stepping through all JUMPs
     * @param table a Table
     * @param tables a Set of Strings representing the involved table identifiers
     * @return the same Set that was passed in, with possibly extra table identifiers added
     */
    private Set<String> getInvolvedTables(Table table, Set<String> tables) {
        if (table == null)
            return tables;

        tables.add(table.getId());

        if (table.getTableRows() != null)
            for (TableRow tableRow : table.getTableRows()) {
                for (Endpoint endpoint : tableRow.getEndpoints()) {
                    if (endpoint != null && EndpointType.JUMP.equals(endpoint.getType())) {
                        // if table has already been visited, don't call getInvolvedTables again; otherwise we could have infinite recursion
                        if (!tables.contains(endpoint.getValue()))
                            getInvolvedTables(getProvider().getTable(endpoint.getValue()), tables);
                    }
                }
            }

        return tables;
    }

    /**
     * Returns a list of inputs that are required for the specified TablePath.  This method will deal with mapped inputs.
     * Note that if an output key is added during the mapping and used as an input in one of the later tables, we do not want
     * to include it in the final list of inputs.  Order matters here since if the key was already used as an input before being
     * re-mapped, then it is still considered an input, otherwise it should be excluded.
     * @param path a TablePath
     * @return a Set of unique inputs
     */
    public Set<String> getInputs(TablePath path) {
        return getInputs(path, new HashSet<>());
    }

    /**
     * Returns a list of inputs that are required for the specified TablePath.  This method will deal with mapped inputs.
     * Note that if an output key is added during the mapping and used as an input in one of the later tables, we do not want
     * to include it in the final list of inputs.  Order matters here since if the key was already used as an input before being
     * re-mapped, then it is still considered an input, otherwise it should be excluded.
     * @param path a TablePath
     * @param excludedInputs a list of keys that should not be included in the inputs
     * @return a Set of unique inputs
     */
    @SuppressWarnings("java:S125")
    public Set<String> getInputs(TablePath path, Set<String> excludedInputs) {
        Set<String> inputs = new LinkedHashSet<>();

        if (path != null) {
            Map<String, String> inputMappings = new HashMap<>();

            if (path.getInputMapping() != null)
                for (KeyMapping keymapping : path.getInputMapping())
                    inputMappings.put(keymapping.getTo(), keymapping.getFrom());
            Map<String, String> outputMappings = new HashMap<>();
            if (path.getOutputMapping() != null)
                for (KeyMapping keymapping : path.getOutputMapping())
                    outputMappings.put(keymapping.getFrom(), keymapping.getTo());

            // process the table (and any "JUMP" tables) for the mapping
            for (String tableId : getInvolvedTables(getProvider().getTable(path.getId()), new LinkedHashSet<>())) {
                Table table = getProvider().getTable(tableId);
                if (table != null) {
                    // first process the inputs from the column definitions
                    if (table.getColumnDefinitions() != null) {
                        for (ColumnDefinition def : table.getColumnDefinitions()) {
                            if (ColumnType.INPUT.equals(def.getType())) {
                                String inputKey = inputMappings.containsKey(def.getKey()) ? inputMappings.get(def.getKey()) : def.getKey();
                                if (!excludedInputs.contains(inputKey))
                                    inputs.add(inputKey);
                            }
                            else if (ColumnType.ENDPOINT.equals(def.getType())) {
                                String outputKey = outputMappings.containsKey(def.getKey()) ? outputMappings.get(def.getKey()) : def.getKey();
                                if (!inputs.contains(outputKey))
                                    excludedInputs.add(outputKey);
                            }
                        }
                    }

                    // next add any inputs that are referenced in the table rows, i.e. format of {{key}}
                    if (table.getExtraInput() != null) {
                        for (String inputKey : table.getExtraInput()) {
                            // variable references need to use input mappings as well
                            if (inputMappings.containsKey(inputKey))
                                inputKey = inputMappings.get(inputKey);

                            if (!excludedInputs.contains(inputKey))
                                inputs.add(inputKey);
                        }
                    }
                }
            }
        }

        return inputs;
    }

    /**
     * Looks at all tables involved in the mapping and returns a list of inputs that are used.  This also includes the inputs
     * used in the inclusion and exclusion tables if any.
     * @param mapping a Mapping
     * @param excludedInputs a list of keys that should not be included in the inputs
     * @return a Set of unique inputs
     */
    public Set<String> getInputs(Mapping mapping, Set<String> excludedInputs) {
        Set<String> inputs = new LinkedHashSet<>();

        // if any fields are added in the initial context, they should not be considered inputs since their value is set
        if (mapping.getInitialContext() != null) {
            for (KeyValue kv : mapping.getInitialContext())
                excludedInputs.add(kv.getKey());
        }

        // handle inclusion tables if any
        if (mapping.getInclusionTables() != null)
            for (TablePath path : mapping.getInclusionTables())
                inputs.addAll(getInputs(path, excludedInputs));

        // handle exclusion tables if any
        if (mapping.getExclusionTables() != null)
            for (TablePath path : mapping.getExclusionTables())
                inputs.addAll(getInputs(path, excludedInputs));

        // handle table paths if any
        if (mapping.getTablePaths() != null)
            for (TablePath path : mapping.getTablePaths())
                inputs.addAll(getInputs(path, excludedInputs));

        return inputs;
    }

    /**
     * Looks at all tables involved in all the mappings in the schema and returns a list of inputs that are used.  It will also deal with mapped inputs.
     * @param schema a schema
     * @return a Set of Strings containing the unique schema input keys
     */
    public Set<String> getInputs(Schema schema) {
        Set<String> inputs = new LinkedHashSet<>();
        Set<String> excludedInputs = new HashSet<>();

        if (schema.getMappings() != null)
            for (Mapping mapping : schema.getMappings())
                inputs.addAll(getInputs(mapping, excludedInputs));

        return inputs;
    }

    /**
     * Return a list of outputs that are produced form the specified TablePath.  It will also handle mapped outputs.
     * @param path a TablePath
     * @return a Set of Strings containing the unique Mapping output keys
     */
    public Set<String> getOutputs(TablePath path) {
        Set<String> outputs = new LinkedHashSet<>();

        if (path != null) {
            // build map of from key -> to key
            Map<String, String> mappings = new HashMap<>();
            if (path.getOutputMapping() != null)
                for (KeyMapping keymapping : path.getOutputMapping())
                    mappings.put(keymapping.getFrom(), keymapping.getTo());

            for (String tableId : getInvolvedTables(getProvider().getTable(path.getId()), new LinkedHashSet<>())) {
                Table table = getProvider().getTable(tableId);
                if (table != null && table.getColumnDefinitions() != null) {
                    for (ColumnDefinition def : table.getColumnDefinitions()) {
                        if (ColumnType.ENDPOINT.equals(def.getType()) && def.getKey() != null)
                            outputs.add(mappings.containsKey(def.getKey()) ? mappings.get(def.getKey()) : def.getKey());
                    }
                }
            }
        }

        return outputs;
    }

    /**
     * Looks at all tables involved in the mapping and returns a list of outputs that are produced.  It will also handle mapped outputs.  Since
     * inclusion/exclusion tables should not map any new values, they are not included in the calculation.
     * @param mapping a Mapping
     * @return a Set of Strings containing the unique Mapping output keys
     */
    public Set<String> getOutputs(Mapping mapping) {
        Set<String> outputs = new LinkedHashSet<>();

        if (mapping.getTablePaths() != null)
            for (TablePath path : mapping.getTablePaths())
                outputs.addAll(getOutputs(path));

        return outputs;
    }

    /**
     * Looks at all tables involved in all the mappings in the schema and returns a list of outputs produced.  It will also handle mapped outputs.
     * @param schema a schema
     * @return a Set of Strings containing the unique Mapping output keys
     */
    public Set<String> getOutputs(Schema schema) {
        Set<String> outputs = new LinkedHashSet<>();

        if (schema.getMappings() != null)
            for (Mapping mapping : schema.getMappings())
                outputs.addAll(getOutputs(mapping));

        return outputs;
    }

    /**
     * Calculates the default value for an Input using supplied context
     * @param input Input definition
     * @param context a Map containing the context
     * @param result a Result object to store errors
     * @return the default value for the input or blank if there is none
     */
    public String getDefault(Input input, Map<String, String> context, Result result) {
        String value = "";

        if (input.getDefault() != null)
            value = translateValue(input.getDefault(), context);
        else if (input.getDefaultTable() != null) {
            Table defaultTable = getProvider().getTable(input.getDefaultTable());
            if (defaultTable == null) {
                result.addError(new ErrorBuilder(Type.UNKNOWN_TABLE).message("Default table does not exist: " + input.getDefaultTable()).key(input.getKey()).build());
                return value;
            }

            // look up default value from table
            List<? extends Endpoint> endpoints = matchTable(defaultTable, context);
            if (endpoints != null) {
                value = endpoints.stream()
                        .filter(endpoint -> EndpointType.VALUE.equals(endpoint.getType()))
                        .filter(endpoint -> endpoint.getResultKey().equals(input.getKey()))
                        .map(endpoint -> translateValue(endpoint.getValue(), context))
                        .findFirst()
                        .orElse(null);
            }

            // if no match found, report the error
            if (endpoints == null || value == null) {
                result.addError(new ErrorBuilder(Type.MATCH_NOT_FOUND)
                        .message("Default table " + input.getDefaultTable() + " did not find a match")
                        .key(input.getKey())
                        .build());
                return "";
            }
        }

        return value;
    }

    /**
     * Using the supplied context, process a schema.  The results will be added to the context.
      * <p>
      * **The context Map is mutated in place.** Input values are trimmed, defaults are resolved,
      * and the map's contents are replaced with only the staging outputs by the time this method returns.
      * Do not reuse the same Map instance across multiple calls unless you deep-copy it first.
     * @param schemaId an schema identifier
     * @param context a Map containing the context
     * @return a Result
     */
    public Result process(String schemaId, Map<String, String> context) {
        Schema start = getProvider().getSchema(schemaId);

        if (start == null)
            throw new IllegalStateException("Unknown schema: '" + schemaId + "'");

        return process(start, context);
    }

    /**
     * Using the supplied context, process a schema.  The results will be added to the context.
      * <p>
      * **The context Map is mutated in place.** Input values are trimmed, defaults are resolved,
      * and the map's contents are replaced with only the staging outputs by the time this method returns.
      * Do not reuse the same Map instance across multiple calls unless you deep-copy it first.
     * <p>
     * Input-mapping destination keys on a table path are temporary aliases scoped to that path. They are added before the path is processed and removed afterward. An input-mapping
     * destination must therefore not be used to preserve a pre-existing context value; any previous value with the same key is overwritten and is not restored.
     * </p>
     * @param schema a schema
     * @param context a Map containing the context
     * @return a Result
     */
    public Result process(Schema schema, Map<String, String> context) {
        Result result = new Result(context);

        trimContext(context);

        if (!validateInputs(schema, context, result)) {
            result.setType(Result.Type.FAILED_INPUT);
            return result;
        }

        initializeSchemaContext(schema, context);

        executeMappings(schema, context, result);

        validateOutputs(schema, context, result);

        return result;
    }

    /**
     * Trims every non-null value in the supplied context.
     * @param context the context to normalize
     */
    private void trimContext(Map<String, String> context) {
        // Trim all context strings so whitespace-only values are treated as blank.
        for (Entry<String, String> entry : context.entrySet()) {
            if (entry.getValue() != null) {
                entry.setValue(entry.getValue().trim());
            }
        }
    }

    /**
     * Resolves missing input defaults and validates non-blank inputs against their configured tables.
     * @param schema the schema being processed
     * @param context the current processing context
     * @param result the result to receive validation errors
     * @return {@code true} when processing should continue; {@code false} when the schema's invalid-input policy requires failure
     */
    private boolean validateInputs(Schema schema, Map<String, String> context, Result result) {
        boolean stopForBadInput = false;
        for (String key : schema.getInputMap().keySet()) {
            Input input = schema.getInputMap().get(key);

            String value = context.get(input.getKey());

            // If no value was supplied, resolve its default and add it to the context.
            if (value == null) {
                value = getDefault(input, context, result);
                context.put(input.getKey(), value);
            }

            // Blank inputs do not need validation against their associated table.
            if (value != null && !value.isEmpty() && input.getTable() != null) {
                Table lookup = getProvider().getTable(input.getTable());

                if (lookup == null) {
                    result.addError(new ErrorBuilder(Type.UNKNOWN_TABLE).message("Input table does not exist: " + input.getTable()).key(input.getKey()).build());
                    continue;
                }

                List<? extends Endpoint> endpoints = matchTable(lookup, context);
                if (endpoints == null) {
                    result.addError(new ErrorBuilder(Boolean.TRUE.equals(input.getUsedForStaging()) ? Type.INVALID_REQUIRED_INPUT : Type.INVALID_NON_REQUIRED_INPUT).message(
                            "Invalid '" + input.getKey() + "' value (" + value + ")").key(input.getKey()).table(input.getTable()).build());

                    // The schema controls whether this invalid input should stop processing.
                    if (Schema.StagingInputErrorHandler.FAIL.equals(schema.getOnInvalidInput()) || (Boolean.TRUE.equals(input.getUsedForStaging())
                            && Schema.StagingInputErrorHandler.FAIL_WHEN_USED_FOR_STAGING.equals(schema.getOnInvalidInput())))
                        stopForBadInput = true;
                }
            }
        }
        return !stopForBadInput;
    }

    /**
     * Initializes output defaults followed by schema-level initial-context values.
     * @param schema the schema being processed
     * @param context the context to initialize
     */
    private void initializeSchemaContext(Schema schema, Map<String, String> context) {
        // Output defaults must be available to the schema's initial-context expressions.
        for (Entry<String, ? extends Output> entry : schema.getOutputMap().entrySet())
            context.put(entry.getValue().getKey(), entry.getValue().getDefault() != null ? translateValue(entry.getValue().getDefault(), context) : "");

        if (schema.getInitialContext() != null)
            for (KeyValue keyValue : schema.getInitialContext())
                context.put(keyValue.getKey(), translateValue(keyValue.getValue(), context));
    }

    /**
     * Executes each mapping whose inclusion and exclusion criteria match the current context.
     * @param schema the schema containing the mappings
     * @param context the current processing context
     * @param result the result to update
     */
    private void executeMappings(Schema schema, Map<String, String> context, Result result) {
        if (schema.getMappings() == null)
            return;

        for (Mapping mapping : schema.getMappings()) {
            // Only mappings that pass their inclusion and exclusion criteria are processed.
            if (!isMappingInvolved(mapping, context))
                continue;

            recordInvolvementPaths(mapping, result);
            initializeMappingContext(mapping, context);
            executeTablePaths(mapping, context, result);
        }
    }

    /**
     * Records the mapping's inclusion and exclusion tables in the result path.
     * @param mapping the involved mapping
     * @param result the result to update
     */
    private void recordInvolvementPaths(Mapping mapping, Result result) {
        // Inclusion and exclusion tables participate in processing and belong in the result path.
        recordPaths(mapping.getId(), mapping.getInclusionTables(), result);
        recordPaths(mapping.getId(), mapping.getExclusionTables(), result);
    }

    /**
     * Records a collection of table paths for a mapping.
     * @param mappingId the mapping identifier
     * @param paths the table paths to record, or {@code null}
     * @param result the result to update
     */
    private void recordPaths(String mappingId, List<? extends TablePath> paths, Result result) {
        if (paths != null)
            for (TablePath path : paths)
                result.addPath(mappingId, path.getId());
    }

    /**
     * Adds mapping-level initial-context values to the processing context.
     * @param mapping the mapping being processed
     * @param context the context to initialize
     */
    private void initializeMappingContext(Mapping mapping, Map<String, String> context) {
        // Mapping-specific values are available to every table path in this mapping.
        if (mapping.getInitialContext() != null)
            for (KeyValue keyValue : mapping.getInitialContext())
                context.put(keyValue.getKey(), keyValue.getValue());
    }

    /**
     * Executes the mapping's table paths in order until all paths complete or a STOP endpoint is reached.
     * @param mapping the mapping being processed
     * @param context the current processing context
     * @param result the result to update
     */
    private void executeTablePaths(Mapping mapping, Map<String, String> context, Result result) {
        if (mapping.getTablePaths() == null)
            return;

        // A STOP endpoint ends the remaining table paths for this mapping.
        for (TablePath path : mapping.getTablePaths()) {
            if (!executeTablePath(mapping.getId(), path, context, result))
                break;
        }
    }

    /**
     * Applies temporary input mappings and executes one table path, including any JUMP tables.
     * @param mappingId the mapping identifier
     * @param path the table path to execute
     * @param context the current processing context
     * @param result the result to update
     * @return {@code true} when processing should continue; {@code false} when a STOP endpoint was reached
     */
    private boolean executeTablePath(String mappingId, TablePath path, Map<String, String> context, Result result) {
        // Input mappings create aliases used while processing this path and any JUMP tables it reaches.
        applyInputMappings(path, context, result);
        try {
            return process(mappingId, path.getId(), path, result, new ArrayDeque<>());
        }
        finally {
            // Input-mapping destinations are temporary aliases scoped to this table path.
            removeInputMappings(path, context);
        }
    }

    /**
     * Adds the table path's temporary input aliases to the context.
     * @param path the table path defining the aliases
     * @param context the context to update
     * @param result the result to receive unknown-source errors
     */
    private void applyInputMappings(TablePath path, Map<String, String> context, Result result) {
        if (path.getInputMapping() == null)
            return;

        for (KeyMapping key : path.getInputMapping()) {
            String sourceKey = key.getFrom();
            if (!context.containsKey(sourceKey)) {
                result.addError(new ErrorBuilder(Type.UNKNOWN_INPUT_MAPPING)
                        .message("Input mapping '" + sourceKey + "' does not exist for table '" + path.getId() + "'")
                        .key(sourceKey)
                        .table(path.getId())
                        .build());
                continue;
            }

            context.put(key.getTo(), context.get(sourceKey));
        }
    }

    /**
     * Removes the table path's temporary input aliases from the context.
     * @param path the table path defining the aliases
     * @param context the context to update
     */
    private void removeInputMappings(TablePath path, Map<String, String> context) {
        if (path.getInputMapping() != null)
            for (KeyMapping key : path.getInputMapping())
                context.remove(key.getTo());
    }

    /**
     * Removes non-output values and validates configured outputs against their associated tables.
     * @param schema the schema defining the outputs
     * @param context the final processing context
     * @param result the result to receive validation errors
     */
    private void validateOutputs(Schema schema, Map<String, String> context, Result result) {
        if (schema.getOutputMap() != null && !schema.getOutputMap().isEmpty()) {
            Iterator<Entry<String, String>> iter = context.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                Output output = schema.getOutputMap().get(entry.getKey());

                // Once outputs are defined, internal and input values are removed from the returned context.
                if (output == null)
                    iter.remove();
                else if (output.getTable() != null) {
                    Table lookup = getProvider().getTable(output.getTable());

                    if (lookup == null) {
                        result.addError(new ErrorBuilder(Type.UNKNOWN_TABLE).message("Output table does not exist: " + output.getTable()).key(output.getKey()).build());
                        continue;
                    }

                    // Validate the final output value when the output declares a validation table.
                    List<? extends Endpoint> endpoints = matchTable(lookup, context);
                    if (endpoints == null) {
                        String value = context.get(output.getKey());
                        result.addError(new ErrorBuilder(Type.INVALID_OUTPUT).message("Invalid '" + output.getKey() + "' value (" + (value.isEmpty() ? BLANK_OUTPUT : value) + ")").key(
                                output.getKey()).table(output.getTable()).build());
                    }
                }
            }
        }
    }

    /**
     * Internal method to recursively process a table
     * @param mappingId a Mapping identifier
     * @param tableId a Table identifier
     * @param path a TablePath
     * @param result a Result
     * @param stack a stack which tracks the path and makes sure the path doesn't enter an infinite recursive state
     * @return a boolean indicating whether processing should continue
     */
    protected boolean process(String mappingId, String tableId, TablePath path, Result result, Deque<String> stack) {
        boolean continueProcessing = true;

        Table table = getProvider().getTable(tableId);
        if (table == null) {
            result.addError(new ErrorBuilder(Type.UNKNOWN_TABLE).message("The processing of '" + path.getId() + "' contains a reference to an unknown table: '" + tableId + "'").table(tableId)
                    .build());
            return true;
        }

        // track the path history to make sure no table is reached twice
        if (stack.contains(tableId)) {
            result.addError(new ErrorBuilder(Type.INFINITE_LOOP).message(
                    "The processing of '" + path.getId() + "' has entered an infinite recursive state.  Table '" + tableId + "' was accessed multiple times.").table(tableId).build());
            return true;
        }

        // keep track of every table that was visited for the entire process
        result.addPath(mappingId, tableId);

        // add the table to the recursion stack
        stack.push(tableId);

        // look for the match in the mapping table; if no match is found, used the table-specific no_match value
        List<? extends Endpoint> endpoints = matchTable(table, result.getContext());
        if (endpoints == null) {
            // if a match is not found, include all the endpoints as columns in the error
            result.addError(new ErrorBuilder(Type.MATCH_NOT_FOUND)
                    .message("Match not found in table '" + tableId + "' (" + getTableInputsAsString(table, result.getContext()) + ")")
                    .table(tableId)
                    .columns(table.getColumnDefinitions().stream().filter(c -> c.getType().equals(ColumnType.ENDPOINT)).map(ColumnDefinition::getKey).toList())
                    .build());
        }
        else {
            for (Endpoint endpoint : endpoints) {
                if (EndpointType.STOP.equals(endpoint.getType()))
                    continueProcessing = false;
                else if (EndpointType.JUMP.equals(endpoint.getType()) && continueProcessing)
                    continueProcessing = process(mappingId, endpoint.getValue(), path, result, stack);
                else if (EndpointType.ERROR.equals(endpoint.getType())) {
                    String message = endpoint.getValue();
                    if (message == null || message.isEmpty())
                        message = "Matching resulted in an error in table '" + tableId + "' for column '" + endpoint.getResultKey() + "' (" + getTableInputsAsString(table, result.getContext()) + ")";

                    result.addError(new ErrorBuilder(Type.STAGING_ERROR).message(message).table(tableId).columns(Collections.singletonList(endpoint.getResultKey())).build());
                }
                else if (EndpointType.VALUE.equals(endpoint.getType()))
                    applyEndpointValue(endpoint, path, result.getContext());
            }
        }

        // processing of this table is complete, and it can be removed from the recursion stack
        stack.pop();

        return continueProcessing;
    }

    /**
     * Applies a value endpoint to its mapped output keys, resolving templates against the current context.
     * @param endpoint the value endpoint to apply
     * @param path the table path defining output mappings
     * @param context the context to update
     */
    private void applyEndpointValue(Endpoint endpoint, TablePath path, Map<String, String> context) {
        // A null endpoint value removes its destination; otherwise templates resolve against the current context.
        for (String key : getMappedOutputKeys(endpoint.getResultKey(), path)) {
            if (endpoint.getValue() == null)
                context.remove(key);
            else
                context.put(key, translateValue(endpoint.getValue(), context));
        }
    }

    /**
     * Resolves the destination keys for an endpoint result key.
     * @param resultKey the endpoint result key
     * @param path the table path defining output mappings
     * @return the mapped destination keys, or the original result key when no mapping applies
     */
    private List<String> getMappedOutputKeys(String resultKey, TablePath path) {
        if (path.getOutputMapping() == null)
            return Collections.singletonList(resultKey);

        // One endpoint can populate multiple destination keys through output mappings.
        List<String> mappedKeys = path.getOutputMapping().stream()
                .filter(key -> key.getFrom().equals(resultKey))
                .map(KeyMapping::getTo)
                .toList();
        return mappedKeys.isEmpty() ? Collections.singletonList(resultKey) : mappedKeys;
    }

}
