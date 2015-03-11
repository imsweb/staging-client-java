/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The result of processing an Algorithm
 */
public class Result {

    public enum Type {
        // staging was executed
        STAGED,

        // a field which was required to be valid failed validation; staging was not executed
        FAILED_INPUT
    }

    private Type _type;
    private Map<String, String> _context;
    private List<Error> _errors = new ArrayList<Error>();
    private List<String> _path = new ArrayList<String>();

    /**
     * Default constructor
     */
    public Result() {
        setType(Type.STAGED);
    }

    /**
     * Constructor
     * @param context a Map representing the context
     */
    public Result(Map<String, String> context) {
        setType(Type.STAGED);
        setContext(context);
    }

    /**
     * Return the type of result
     * @return a result type
     */
    public Type getType() {
        return _type;
    }

    /**
     * Set the type of result
     * @param type a result type
     */
    public void setType(Type type) {
        _type = type;
    }

    /**
     * Return the result context
     * @return a Map of field key to value representing the results
     */
    public Map<String, String> getContext() {
        return _context;
    }

    /**
     * Set the result context
     * @param context a Map representing the context
     */
    public void setContext(Map<String, String> context) {
        _context = context;
    }

    /**
     * Does the result contain any errors
     * @return true if there are one or more errors
     */
    public boolean hasErrors() {
        return !_errors.isEmpty();
    }

    /**
     * Get the full list of errors
     * @return a list of Error objects
     */
    public List<Error> getErrors() {
        return _errors;
    }

    /**
     * Add an error to the result
     * @param error an error object
     */
    public void addError(Error error) {
        _errors.add(error);
    }

    /**
     * Get a list of all the tables that were used in the calculation
     * @return a List of table identifiers
     */
    public List<String> getPath() {
        return _path;
    }

    /**
     * Add a mapping/table to the path history
     * @param mappingId a mapping identifier
     * @param tableId a table identifier
     */
    public void addPath(String mappingId, String tableId) {
        if (mappingId != null && !mappingId.isEmpty())
            _path.add(mappingId + "." + tableId);
        else
            _path.add(tableId);
    }

}
