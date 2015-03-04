/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.imsweb.decisionengine.Definition;

public class BasicDefinition implements Definition {

    private String _id;
    private List<BasicInput> _inputs;
    private List<BasicKeyValue> _initialContext;
    private List<BasicMapping> _mappings;

    // parsed fields
    private Map<String, BasicInput> _parsedInputMap = new HashMap<String, BasicInput>();

    /**
     * Default constructor
     */
    public BasicDefinition() {
    }

    /**
     * Construct with an indentifier
     * @param id a definition identifier
     */
    public BasicDefinition(String id) {
        setId(id);
    }

    @Override
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    public List<BasicInput> getInputs() {
        return _inputs;
    }

    public void setInputs(List<BasicInput> inputs) {
        _inputs = inputs;
    }

    public void addInput(String key) {
        if (_inputs == null)
            _inputs = new ArrayList<BasicInput>();

        _inputs.add(new BasicInput(key));
    }

    public void addInput(BasicInput input) {
        if (_inputs == null)
            _inputs = new ArrayList<BasicInput>();

        _inputs.add(input);
    }

    public void addInput(String key, String table) {
        if (_inputs == null)
            _inputs = new ArrayList<BasicInput>();

        _inputs.add(new BasicInput(key, table));
    }

    @Override
    public List<BasicKeyValue> getInitialContext() {
        return _initialContext;
    }

    public void setInitialContext(List<BasicKeyValue> initialContext) {
        _initialContext = initialContext;
    }

    public void addInitialContext(String key, String value) {
        if (_initialContext == null)
            _initialContext = new ArrayList<BasicKeyValue>();

        _initialContext.add(new BasicKeyValue(key, value));
    }

    @Override
    public List<BasicMapping> getMappings() {
        return _mappings;
    }

    public void setMappings(List<BasicMapping> mappings) {
        _mappings = mappings;
    }

    public void addMapping(BasicMapping mapping) {
        if (_mappings == null)
            _mappings = new ArrayList<BasicMapping>();

        _mappings.add(mapping);
    }

    @Override
    public Map<String, BasicInput> getInputMap() {
        return _parsedInputMap;
    }

    public void setInputMap(Map<String, BasicInput> parsedInputMap) {
        _parsedInputMap = parsedInputMap;
    }
}
