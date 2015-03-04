/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.imsweb.decisionengine.Result;

/**
 * A wrapper class for the decision engine Result class.  It exists to add the JSON mappings.
 */
@JsonPropertyOrder({"input", "output", "table_path", "errors"})
public class StagingOutput {

    Result _result;
    Map<String, String> _input;

    /**
     * Constructor
     * @param result Result of staging call
     */
    public StagingOutput(Result result) {
        _result = result;
    }

    @JsonProperty("output")
    public Map<String, String> getOutput() {
        return _result.getContext();
    }

    @JsonProperty("table_path")
    public List<String> getPath() {
        return _result.getPath();
    }

    @JsonProperty("errors")
    public List<com.imsweb.decisionengine.Error> getErrors() {
        return _result.getErrors();
    }

    @JsonProperty("input")
    public Map<String, String> getInput() {
        return _input;
    }

    public void setInput(Map<String, String> input) {
        _input = input;
    }
}
