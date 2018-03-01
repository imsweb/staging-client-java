/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.imsweb.decisionengine.TableRow;

public class BasicTableRow implements TableRow {

    private Map<String, List<BasicRange>> _inputs = new HashMap<>();
    private String _description;
    private List<BasicEndpoint> _endpoints = new ArrayList<>();

    @Override
    public List<BasicRange> getColumnInput(String key) {
        return _inputs.get(key);
    }

    public Map<String, List<BasicRange>> getInputs() {
        return _inputs;
    }

    public void setInputs(Map<String, List<BasicRange>> inputs) {
        _inputs = inputs;
    }

    /**
     * Add a single columns input list
     * @param key an input key
     * @param range a List of BasicRange objects
     */
    public void addInput(String key, List<BasicRange> range) {
        _inputs.put(key, range);
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    @Override
    public List<BasicEndpoint> getEndpoints() {
        return _endpoints;
    }

    public void setEndpoints(List<BasicEndpoint> endpoints) {
        _endpoints = endpoints;
    }

    public void addEndpoint(BasicEndpoint endpoint) {
        _endpoints.add(endpoint);
    }
}
