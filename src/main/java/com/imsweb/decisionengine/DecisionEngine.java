/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.imsweb.decisionengine.ColumnDefinition.ColumnType;
import com.imsweb.decisionengine.Endpoint.EndpointType;
import com.imsweb.decisionengine.Error.ErrorBuilder;
import com.imsweb.decisionengine.Error.Type;

/**
 * An engine for processing declarative algorithms.
 */
public class DecisionEngine {

    // string to use for blank or null in error strings
    public static final String _BLANK_OUTPUT = "<blank>";
    private static Pattern _TEMPLATE_REFERENCE = Pattern.compile("\\{\\{(.*?)\\}\\}");
    private DataProvider _provider;

    /**
     * Construct the decision engine with the passed data provider
     * @param provider a DataProvider
     */
    public DecisionEngine(DataProvider provider) {
        setProvider(provider);
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
            throw new IllegalStateException("Context must not be missing");

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
     * if will return the value unchanged.  If the context key does not exist in the context, blank will be returned
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
                    inputs.add((value == null || value.trim().isEmpty()) ? _BLANK_OUTPUT : value.trim());
                }

        return inputs.stream().collect(Collectors.joining(","));
    }

    /**
     * Returns the internal data provider
     * @return a DataProvider
     */
    public DataProvider getProvider() {
        return _provider;
    }

    /**
     * Sets the provider and initiaizes all definitions and tables
     * @param provider a DataProvider
     */
    public void setProvider(DataProvider provider) {
        _provider = provider;
    }

    /**
     * Given a mapping and a context, check the inclusion/exclusion tables to see if mapping should be processed
     * @param mapping a Mapping
     * @param context a Map containing the context
     * @return true if the mapping is involved
     */
    public boolean isMappingInvolved(Mapping mapping, Map<String, String> context) {
        if (context == null)
            throw new IllegalStateException("Context must not be missing");

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
     * Given a definition and context, return a list of mappings that match inclusion and exclusion criteria
     * @param definition a Definition
     * @param context a Map containing the context
     * @return a List of involved Mapping entities
     */
    public List<Mapping> getInvolvedMappings(Definition definition, Map<String, String> context) {
        List<Mapping> mappings = new ArrayList<>();

        if (context == null)
            throw new IllegalStateException("Context must not be missing");

        if (definition.getMappings() != null) {
            for (Mapping mapping : definition.getMappings())
                if (isMappingInvolved(mapping, context))
                    mappings.add(mapping);
        }

        return mappings;
    }

    /**
     * Return a list of tables involved in a definition
     * @param definitionId an Definition identifier
     * @return a set of table identifiers
     */
    public Set<String> getInvolvedTables(String definitionId) {
        Definition definition = getProvider().getDefinition(definitionId);

        if (definition == null)
            throw new IllegalStateException("Unknown starting table: '" + definitionId + "'");

        return getInvolvedTables(definition);
    }

    /**
     * Return a list of tables involved in an definition.  This includes not only the tables paths, but also tables references in the input section.
     * @param definition a Definition
     * @return a set of table identifiers
     */
    public Set<String> getInvolvedTables(Definition definition) {
        Set<String> tables = new LinkedHashSet<>();

        // first, evaluate inputs and outputs
        for (String key : definition.getInputMap().keySet()) {
            Input input = definition.getInputMap().get(key);
            if (input.getTable() != null)
                getInvolvedTables(getProvider().getTable(input.getTable()), tables);
        }
        for (String key : definition.getOutputMap().keySet()) {
            Output output = definition.getOutputMap().get(key);
            if (output.getTable() != null)
                getInvolvedTables(getProvider().getTable(output.getTable()), tables);
        }

        // next loop over mappings and paths
        if (definition.getMappings() != null) {
            for (Mapping mapping : definition.getMappings()) {
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
     * re-mapped, then it is still considered an input, otherwise if should be excluded.
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
     * re-mapped, then it is still considered an input, otherwise if should be excluded.
     * @param path a TablePath
     * @param excludedInputs a list of keys that should not be included in the inputs
     * @return a Set of unique inputs
     */
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
     * Looks at all tables involved in all the mappings in the definition and returns a list of inputs that are used.  It will also deal with mapped inputs.
     * @param definition a Definition
     * @return a Set of Strings contianing the unique Definition input keys
     */
    public Set<String> getInputs(Definition definition) {
        Set<String> inputs = new LinkedHashSet<>();
        Set<String> excludedInputs = new HashSet<>();

        if (definition.getMappings() != null)
            for (Mapping mapping : definition.getMappings())
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
     * Looks at all tables involved in all the mappings in the definition and returns a list of outputs produced.  It will also handle mapped outputs.
     * @param definition a Definition
     * @return a Set of Strings containing the unique Mapping output keys
     */
    public Set<String> getOutputs(Definition definition) {
        Set<String> outputs = new LinkedHashSet<>();

        if (definition.getMappings() != null)
            for (Mapping mapping : definition.getMappings())
                outputs.addAll(getOutputs(mapping));

        return outputs;
    }

    /**
     * Using the supplied context, process an definition.  The results will be added to the context.
     * @param definitionId an Definition identifier
     * @param context a Map containing the context
     * @return a Result
     */
    public Result process(String definitionId, Map<String, String> context) {
        Definition start = getProvider().getDefinition(definitionId);

        if (start == null)
            throw new IllegalStateException("Unknown definition: '" + definitionId + "'");

        return process(start, context);
    }

    /**
     * Using the supplied context, process a definition.  The results will be added to the context.
     * @param definition a Definition
     * @param context a Map containing the context
     * @return a Result
     */
    public Result process(Definition definition, Map<String, String> context) {
        Result result = new Result(context);

        // trim all context Strings; " " will match ""
        for (Entry<String, String> entry : context.entrySet())
            if (entry.getValue() != null)
                context.put(entry.getKey(), entry.getValue().trim());

        // validate inputs
        boolean stopForBadInput = false;
        for (String key : definition.getInputMap().keySet()) {
            Input input = definition.getInputMap().get(key);

            String value = context.get(input.getKey());

            // if value not supplied, use the default and set it back into the context; if not supplied and no default, set the input the blank
            if (value == null) {
                value = (input.getDefault() != null ? translateValue(input.getDefault(), context) : "");
                context.put(input.getKey(), value);
            }

            // validate value against associated table, if supplied; if a value is not supplied, or blank, there is no need to validate it against the table
            if (!value.isEmpty() && input.getTable() != null) {
                Table lookup = getProvider().getTable(input.getTable());

                if (lookup == null) {
                    result.addError(new ErrorBuilder(Type.UNKNOWN_TABLE).message("Input table does not exist: " + input.getTable()).key(input.getKey()).build());
                    continue;
                }

                List<? extends Endpoint> endpoints = matchTable(lookup, context);
                if (endpoints == null) {
                    result.addError(new ErrorBuilder(Boolean.TRUE.equals(input.getUsedForStaging()) ? Type.INVALID_REQUIRED_INPUT : Type.INVALID_NON_REQUIRED_INPUT).message(
                            "Invalid '" + input.getKey() + "' value (" + (value.isEmpty() ? _BLANK_OUTPUT : value) + ")").key(input.getKey()).table(input.getTable()).build());

                    // if the schema error handling is set to FAIL or if the input is required for staging and the error handling is set to FAIL_WHEN_REQUIRED_FOR_STAGING,
                    // then stop processing and return a failure result
                    if (Definition.StagingInputErrorHandler.FAIL.equals(definition.getOnInvalidInput()) || (Boolean.TRUE.equals(input.getUsedForStaging())
                            && Definition.StagingInputErrorHandler.FAIL_WHEN_USED_FOR_STAGING.equals(definition.getOnInvalidInput())))
                        stopForBadInput = true;
                }
            }

        }

        // add all output keys to the context; if no default is supplied, use an empty string
        for (Entry<String, ? extends Output> entry : definition.getOutputMap().entrySet())
            context.put(entry.getValue().getKey(), entry.getValue().getDefault() != null ? translateValue(entry.getValue().getDefault(), context) : "");

        // add the initial context
        if (definition.getInitialContext() != null)
            for (KeyValue keyValue : definition.getInitialContext())
                context.put(keyValue.getKey(), translateValue(keyValue.getValue(), context));

        // if an invalid input was flagged to stop processing, set result and exit
        if (stopForBadInput) {
            result.setType(Result.Type.FAILED_INPUT);
            return result;
        }

        // process each mapping if it is "involved", which is checked using the current context against inclusion/exclusion criteria
        if (definition.getMappings() != null) {
            for (Mapping mapping : definition.getMappings()) {
                // make sure mapping passes inclusion/exclusion tables if present
                if (isMappingInvolved(mapping, context)) {
                    // if there are any inclusion/exclusion tables, add them to path
                    if (mapping.getInclusionTables() != null)
                        for (TablePath path : mapping.getInclusionTables())
                            result.addPath(mapping.getId(), path.getId());
                    if (mapping.getExclusionTables() != null)
                        for (TablePath path : mapping.getExclusionTables())
                            result.addPath(mapping.getId(), path.getId());

                    // set the mapping-specific initial context if any
                    if (mapping.getInitialContext() != null)
                        for (KeyValue keyValue : mapping.getInitialContext())
                            context.put(keyValue.getKey(), keyValue.getValue());

                    // loop over all table paths in the mapping
                    if (mapping.getTablePaths() != null) {
                        for (TablePath path : mapping.getTablePaths()) {
                            String tableId = path.getId();

                            // if there is input mapping defined, add the new mapping to the context
                            if (path.getInputMapping() != null) {
                                for (KeyMapping key : path.getInputMapping()) {
                                    String mapFromKey = key.getFrom();

                                    if (!context.containsKey(mapFromKey)) {
                                        result.addError(new ErrorBuilder(Type.UNKNOWN_INPUT_MAPPING).message("Input mapping '" + mapFromKey + "' does not exist for table '" + tableId + "'").key(
                                                mapFromKey).table(tableId).build());
                                        continue;
                                    }

                                    context.put(key.getTo(), context.get(mapFromKey));
                                }
                            }

                            // create a stack to keep track of table calls and ensure there is no infinite recursion
                            Deque<String> stack = new ArrayDeque<>();

                            // recursively process the mapping; if false is returned, stop all processing
                            boolean continueProcessing = process(mapping.getId(), tableId, path, result, stack);

                            // remove the temporary input mappings
                            if (path.getInputMapping() != null) {
                                for (KeyMapping key : path.getInputMapping())
                                    context.remove(key.getTo());
                            }

                            if (!continueProcessing)
                                break;
                        }
                    }
                }

            }
        }

        // if outputs were specified, remove any extra keys and validate the others if a table was specified
        if (definition.getOutputMap() != null && !definition.getOutputMap().isEmpty()) {
            Iterator<Entry<String, String>> iter = context.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                Output output = definition.getOutputMap().get(entry.getKey());

                // if the key is not defined in the output, remove it
                if (output == null)
                    iter.remove();
                else if (output.getTable() != null) {
                    Table lookup = getProvider().getTable(output.getTable());

                    if (lookup == null) {
                        result.addError(new ErrorBuilder(Type.UNKNOWN_TABLE).message("Output table does not exist: " + output.getTable()).key(output.getKey()).build());
                        continue;
                    }

                    // verify the value of the output key is contained in the associated table
                    List<? extends Endpoint> endpoints = matchTable(lookup, context);
                    if (endpoints == null) {
                        String value = context.get(output.getKey());
                        result.addError(new ErrorBuilder(Type.INVALID_OUTPUT).message("Invalid '" + output.getKey() + "' value (" + (value.isEmpty() ? _BLANK_OUTPUT : value) + ")").key(
                                output.getKey()).table(output.getTable()).build());
                    }
                }
            }
        }

        return result;
    }

    /**
     * Internal method to recursively process a table
     * @param mappingId a Mapping identifier
     * @param tableId a Table identifier
     * @param path a TablePath
     * @param result a Result
     * @param stack a stack which tracks the path and makes sure the path doesn't enter an infinite recusive state
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
        if (endpoints == null)
            result.addError(new ErrorBuilder(Type.MATCH_NOT_FOUND).message("Match not found in table '" + tableId + "' (" + getTableInputsAsString(table, result.getContext()) + ")").table(tableId)
                    .build());
        else {
            for (Endpoint endpoint : endpoints) {
                if (EndpointType.STOP.equals(endpoint.getType()))
                    continueProcessing = false;
                else if (EndpointType.JUMP.equals(endpoint.getType()))
                    continueProcessing = process(mappingId, endpoint.getValue(), path, result, stack);
                else if (EndpointType.ERROR.equals(endpoint.getType())) {
                    String message = endpoint.getValue();
                    if (message == null || message.isEmpty())
                        message = "Matching resulted in an error in table '" + tableId + "' (" + getTableInputsAsString(table, result.getContext()) + ")";

                    result.addError(new ErrorBuilder(Type.STAGING_ERROR).message(message).table(tableId).build());
                }
                else if (EndpointType.VALUE.equals(endpoint.getType())) {
                    // if output mapping(s) were provided, check whether the key was mapped
                    List<String> mappedKeys = new ArrayList<>();
                    if (path.getOutputMapping() != null) {
                        for (KeyMapping key : path.getOutputMapping()) {
                            if (key.getFrom().equals(endpoint.getResultKey()))
                                mappedKeys.add(key.getTo());
                        }
                    }

                    // if the value if null, that is indicating that the key should be removed from the context; otherwise set the value into the context
                    if (mappedKeys.isEmpty())
                        mappedKeys = Collections.singletonList(endpoint.getResultKey());

                    // iterate over all the mappings for this endpoint key
                    for (String key : mappedKeys) {
                        if (endpoint.getValue() == null)
                            result.getContext().remove(key);
                        else
                            result.getContext().put(key, translateValue(endpoint.getValue(), result.getContext()));
                    }
                }
            }
        }

        // processing of this table is complete and it can be removed from the recursion stack
        stack.pop();

        return continueProcessing;
    }

}
