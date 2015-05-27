/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.imsweb.decisionengine.DataProvider;
import com.imsweb.decisionengine.DecisionEngine;
import com.imsweb.decisionengine.Endpoint.EndpointType;
import com.imsweb.staging.Staging;

/**
 * In implementation of DataProvider which holds all data in memory
 */
public class BasicDataProvider implements DataProvider {

    private static String _MATCH_ALL_STRING = "*";
    private static BasicStringRange _MATCH_ALL_ENDPOINT = new BasicStringRange();
    private Map<String, BasicTable> _tables = new HashMap<String, BasicTable>();
    private Map<String, BasicDefinition> _definitions = new HashMap<String, BasicDefinition>();

    /**
     * Default constructor
     */
    public BasicDataProvider() {
    }

    /**
     * Initialize a definition.
     * @param definition a BasicDefinition
     */
    public void initDefinition(BasicDefinition definition) {
        // parse the values into something that can searched more efficiently
        if (definition.getInputs() != null) {
            Map<String, BasicInput> parsedInputMap = new HashMap<String, BasicInput>();
            for (BasicInput input : definition.getInputs()) {
                // verify that all inputs contain a key
                if (input.getKey() == null)
                    throw new IllegalStateException("All input defintions must have a 'key' value defined.");

                parsedInputMap.put(input.getKey(), input);

                // the parsed input map provides an easy way to look up by key
                definition.setInputMap(parsedInputMap);
            }
        }

        // store the outputs in a Map that can searched more efficiently
        if (definition.getOutputs() != null) {
            Map<String, BasicOutput> parsedOutputMap = new HashMap<String, BasicOutput>();

            for (BasicOutput output : definition.getOutputs()) {
                // verify that all inputs contain a key
                if (output.getKey() == null)
                    throw new IllegalStateException("All output definitions must have a 'key' defined.");

                parsedOutputMap.put(output.getKey(), output);
            }

            definition.setOutputMap(parsedOutputMap);
        }
    }

    /**
     * Initialize a table.
     * @param table a BasicTable
     */
    public void initTable(BasicTable table) {
        Set<String> extraInputs = new HashSet<String>();

        if (table.getRawRows() != null) {
            for (List<String> row : table.getRawRows()) {
                BasicTableRow tableRowEntity = new BasicTableRow();

                // make sure the number of cells in the row matches the number of columns defined
                if (table.getColumnDefinitions().size() != row.size())
                    throw new IllegalStateException("Table '" + table.getId() + "' has a row with " + row.size() + " values but should have " + table.getColumnDefinitions().size() + ": " + row);

                // loop over the column definitions in order since the data needs to retrieved by array position
                for (int i = 0; i < table.getColumnDefinitions().size(); i++) {
                    BasicColumnDefinition col = table.getColumnDefinitions().get(i);
                    String cellValue = row.get(i);

                    switch (col.getType()) {
                        case INPUT:
                            // if there are no ranges in the list, that means the cell was "blank" and is not needed in the table row
                            List<BasicStringRange> ranges = splitValues(cellValue);
                            if (!ranges.isEmpty()) {
                                tableRowEntity.addInput(col.getKey(), ranges);

                                // if there are key references used (values that reference other inputs) like {{key}}, then add them to the extra inputs list
                                for (BasicStringRange range : ranges) {
                                    if (DecisionEngine.isReferenceVariable(range.getLow()))
                                        extraInputs.add(DecisionEngine.trimBraces(range.getLow()));
                                    if (DecisionEngine.isReferenceVariable(range.getHigh()))
                                        extraInputs.add(DecisionEngine.trimBraces(range.getHigh()));
                                }
                            }
                            break;
                        case ENDPOINT:
                            BasicEndpoint endpoint = parseEndpoint(cellValue);
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
    }

    /**
     * Parse the string representation of an endpoint into a Endpoint object.  There are two supported formats:
     * <p>
     * ENDPOINT_TYPE
     * ENDPOINT_TYPE:PARAMETER
     * </p>
     * @param endpoint an endpoint
     * @return an BasicEndpoint object
     */
    protected BasicEndpoint parseEndpoint(String endpoint) {
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

        return new BasicEndpoint(type, value);
    }

    /**
     * Parses a string of values into a List of StringRange entities.  The values can contain ranges and multiple values.  Some examples might be:
     * <p>
     * 10
     * 10-14
     * 10,15,20
     * 11,13-15,25-29,35
     * </p>
     * Note that all values (both low and high) must be the same length since they are evaluated using String comparison.
     * @param values a string of values
     * @return a List of BasicStringRange objects
     */
    protected List<BasicStringRange> splitValues(String values) {
        List<BasicStringRange> convertedRanges = new ArrayList<BasicStringRange>();

        if (values != null) {
            // if the value of the string is "*", then consider it as matching anything
            if (values.equals(_MATCH_ALL_STRING))
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
                        convertedRanges.add(new BasicStringRange(parts[0].trim(), parts[0].trim()));
                    else if (parts.length == 2) {
                        if (parts[0].trim().length() != parts[1].trim().length())
                            convertedRanges.add(new BasicStringRange(range.trim(), range.trim()));
                        else
                            convertedRanges.add(new BasicStringRange(parts[0].trim(), parts[1].trim()));
                    }
                    else
                        convertedRanges.add(new BasicStringRange(range.trim(), range.trim()));
                }
            }
        }

        return convertedRanges;
    }

    @Override
    public BasicTable getTable(String id) {
        return _tables.get(id);
    }

    @Override
    public BasicDefinition getDefinition(String id) {
        return _definitions.get(id);
    }

    /**
     * Add a table to the list
     * @param table a BasicTable
     */
    public void addTable(BasicTable table) {
        if (_tables.containsKey(table.getId()))
            throw new IllegalStateException("ERROR: A table with identifier '" + table.getId() + "' already exists");

        initTable(table);

        _tables.put(table.getId(), table);
    }

    /**
     * Add a starting point to the list
     * @param definition a BasicDefinition
     */
    public void addDefinition(BasicDefinition definition) {
        if (_definitions.containsKey(definition.getId()))
            throw new IllegalStateException("ERROR: An definition with identifier '" + definition.getId() + "' already exists");

        initDefinition(definition);

        _definitions.put(definition.getId(), definition);
    }

}
