/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.imsweb.decisionengine.Endpoint;
import com.imsweb.decisionengine.Range;
import com.imsweb.decisionengine.TableRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StagingTableRow implements TableRow {

    private Map<String, List<Range>> _inputs = new HashMap<>();
    private List<StagingEndpoint> _endpoints = new ArrayList<>();

    @Override
    @JsonIgnore
    public List<Range> getColumnInput(String key) {
        return _inputs.get(key);
    }

    @JsonProperty("inputs")
    public Map<String, List<Range>> getInputs() {
        return _inputs;
    }

    public void setInputs(Map<String, List<Range>> inputs) {
        _inputs = inputs;
    }

    /**
     * Add a single columns input list
     *
     * @param key   key
     * @param range range
     */
    @Override
    public void addInput(String key, List<Range> range) {
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

    @Override
    public void addEndpoint(Endpoint endpoint) {
        _endpoints.add((StagingEndpoint) endpoint);
    }
}
