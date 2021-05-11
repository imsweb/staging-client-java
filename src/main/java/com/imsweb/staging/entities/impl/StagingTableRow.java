/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.imsweb.staging.entities.Endpoint;
import com.imsweb.staging.entities.Range;
import com.imsweb.staging.entities.TableRow;

public class StagingTableRow implements TableRow {

    private Map<String, List<? extends Range>> _inputs = new HashMap<>();
    private List<StagingEndpoint> _endpoints = new ArrayList<>();

    @Override
    @JsonIgnore
    public List<? extends Range> getColumnInput(String key) {
        return _inputs.get(key);
    }

    @Override
    @JsonProperty("inputs")
    public Map<String, List<? extends Range>> getInputs() {
        return _inputs;
    }

    public void setInputs(Map<String, List<? extends Range>> inputs) {
        _inputs = inputs;
    }

    /**
     * Add a single columns input list
     * @param key key
     * @param range range
     */
    @Override
    public void addInput(String key, List<? extends Range> range) {
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
        _endpoints.add((StagingEndpoint)endpoint);
    }
}
