/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.Sets;

import com.imsweb.decisionengine.Error;

@JsonPropertyOrder({"result", "schema_id", "input", "output", "errors", "path"})
public class StagingData {

    // key definitions
    public static final String PRIMARY_SITE_KEY = "site";
    public static final String HISTOLOGY_KEY = "hist";
    public static final String YEAR_DX_KEY = "year_dx";

    // set of keys that are standard for all schema lookups; any other keys are considered a discriminator
    public static final Set<String> STANDARD_LOOKUP_KEYS = Sets.newHashSet(PRIMARY_SITE_KEY, HISTOLOGY_KEY);

    private Result _result;
    private String _schemaId;
    private Map<String, String> _input = new HashMap<String, String>();
    private Map<String, String> _output = new HashMap<String, String>();
    private List<Error> _errors = new ArrayList<Error>();
    private List<String> _path = new ArrayList<String>();

    public enum Result {
        // staging was performed
        STAGED,

        // both primary site and histology must be supplied
        FAILED_MISSING_SITE_OR_HISTOLOGY,

        // no matching schema was found
        FAILED_NO_MATCHING_SCHEMA,

        // multiple matching schemas were found; a discriminator is probably needed
        FAILED_MULITPLE_MATCHING_SCHEMAS,

        // year of DX out of valid range
        FAILED_INVALID_YEAR_DX,

        // a field that was flagged as "fail_on_invalid" has an invalid value
        FAILED_INVALID_INPUT
    }

    /**
     * Default constructor
     */
    public StagingData() {
    }

    /**
     * Construct with input map
     * @param input input map
     */
    public StagingData(Map<String, String> input) {
        _input = input;
    }

    /**
     * Construct with site/histology
     * @param site primary site
     * @param hist histology
     */
    public StagingData(String site, String hist) {
        setInput(PRIMARY_SITE_KEY, site);
        setInput(HISTOLOGY_KEY, hist);
    }

    @JsonProperty("result")
    public Result getResult() {
        return _result;
    }

    public void setResult(Result result) {
        _result = result;
    }

    @JsonProperty("schema_id")
    public String getSchemaId() {
        return _schemaId;
    }

    public void setSchemaId(String schemaId) {
        _schemaId = schemaId;
    }

    @JsonProperty("input")
    public Map<String, String> getInput() {
        return _input;
    }

    @JsonIgnore
    public String getInput(String key) {
        return _input.get(key);
    }

    public void setInput(String key, String value) {
        _input.put(key, value);
    }

    // output getters

    @JsonProperty("output")
    public Map<String, String> getOutput() {
        return _output;
    }

    @JsonIgnore
    public String getOutput(String key) {
        return _output.get(key);
    }

    public void setOutput(Map<String, String> output) {
        _output = output;
    }

    // errors

    @JsonProperty("errors")
    public List<Error> getErrors() {
        return _errors;
    }

    public void setErrors(List<Error> errors) {
        _errors = errors;
    }

    // path

    @JsonProperty("path")
    public List<String> getPath() {
        return _path;
    }

    public void setPath(List<String> path) {
        _path = path;
    }

}
