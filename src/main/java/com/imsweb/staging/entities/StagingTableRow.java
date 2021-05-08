/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.imsweb.decisionengine.TableRow;

public class StagingTableRow implements TableRow {

    private Map<String, List<StagingRange>> _inputs = new HashMap<>();
    private List<StagingEndpoint> _endpoints = new ArrayList<>();

    @Override
    @JsonIgnore
    public List<StagingRange> getColumnInput(String key) {
        return _inputs.get(key);
    }

    @JsonProperty("inputs")
    public Map<String, List<StagingRange>> getInputs() {
        return _inputs;
    }

    public void setInputs(Map<String, List<StagingRange>> inputs) {
        _inputs = inputs;
    }

    /**
     * Add a single columns input list
     *
     * @param key   key
     * @param range range
     */
    public void addInput(String key, List<StagingRange> range) {
        _inputs.put(key, range);
    }

    @Override
    @JsonProperty("endpoint")
    public List<StagingEndpoint> getEndpoints() {
        return _endpoints;
    }

    public void setEndpoints(List<StagingEndpoint> endpoints) {
        _endpoints = endpoints;
    }

    public void addEndpoint(StagingEndpoint endpoint) {
        _endpoints.add(endpoint);
    }
}
