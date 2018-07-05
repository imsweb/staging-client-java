/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * An error object
 */
@JsonPropertyOrder({"type", "table", "column", "key", "message"})
public class Error {

    private Type _type;
    private String _table;
    private String _column;
    private String _key;
    private String _message;

    public enum Type {
        // an input key was supplied that is not defined in the input definition
        UNKNOWN_INPUT,

        // a required input value was not contained in the input definition table
        INVALID_REQUIRED_INPUT,

        // a non-required input value was not contained in the input definition table
        INVALID_NON_REQUIRED_INPUT,

        // an input mapping from value did not exist
        UNKNOWN_INPUT_MAPPING,

        // an ERROR endpoint was hit during staging processing
        STAGING_ERROR,

        // a table was processed during staging and no match was found
        MATCH_NOT_FOUND,

        // a specified table does not exist
        UNKNOWN_TABLE,

        // processing a table ended up in an infinite loop due to JUMPs
        INFINITE_LOOP,

        // an output value was produced which was not contained in the output definition table
        INVALID_OUTPUT
    }

    /**
     * Default constructor
     */
    public Error() {
    }

    /**
     * Constructor
     * @param type type of error
     */
    public Error(Type type) {
        setType(type);
    }

    @JsonProperty("type")
    public Type getType() {
        return _type;
    }

    public void setType(Type type) {
        _type = type;
    }

    @JsonProperty("table")
    public String getTable() {
        return _table;
    }

    public void setTable(String table) {
        _table = table;
    }

    @JsonProperty("column")
    public String getColumn() {
        return _column;
    }

    public void setColumn(String column) {
        _column = column;
    }

    @JsonProperty("key")
    public String getKey() {
        return _key;
    }

    public void setKey(String key) {
        _key = key;
    }

    @JsonProperty("message")
    public String getMessage() {
        return _message;
    }

    public void setMessage(String message) {
        _message = message;
    }

    /**
     * Build class for Error
     */
    public static class ErrorBuilder {

        private Error _error;

        public ErrorBuilder(Type type) {
            _error = new Error(type);
        }

        public ErrorBuilder type(Type type) {
            _error.setType(type);
            return this;
        }

        public ErrorBuilder table(String table) {
            _error.setTable(table);
            return this;
        }

        public ErrorBuilder column(String column) {
            _error.setColumn(column);
            return this;
        }

        public ErrorBuilder key(String key) {
            _error.setKey(key);
            return this;
        }

        public ErrorBuilder message(String message) {
            _error.setMessage(message);
            return this;
        }

        public com.imsweb.decisionengine.Error build() {
            return _error;
        }
    }
}
