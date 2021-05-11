/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.imsweb.staging.entities.Endpoint;
import com.imsweb.staging.entities.Range;
import com.imsweb.staging.entities.TableRow;

public class StagingTableRow implements TableRow {

    private Map<String, List<StagingRange>> _inputs = new HashMap<>();
    private List<StagingEndpoint> _endpoints = new ArrayList<>();

    @Override
    public Set<String> getColumns() {
        return _inputs.keySet();
    }

    @Override
    @JsonIgnore
    public List<? extends Range> getColumnInput(String key) {
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
     * @param key key
     * @param range range
     */
    @SuppressWarnings("unchecked")
    @Override
    public void addInput(String key, List<? extends Range> range) {
        _inputs.put(key, (List<StagingRange>)range);
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
