/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.imsweb.decisionengine.Definition;

public class BasicDefinition implements Definition {

    private String _id;
    private List<BasicInput> _inputs;
    private List<BasicOutput> _outputs;
    private Set<BasicKeyValue> _initialContext;
    private List<BasicMapping> _mappings;
    private StagingInputErrorHandler _onInvalidInput;

    // parsed fields
    private Map<String, BasicInput> _parsedInputMap = new HashMap<String, BasicInput>();
    private Map<String, BasicOutput> _parsedOutputMap = new HashMap<String, BasicOutput>();

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

    public List<BasicOutput> getOutputs() {
        return _outputs;
    }

    public void setOutputs(List<BasicOutput> outputs) {
        _outputs = outputs;
    }

    public void addOutput(String key) {
        if (_outputs == null)
            _outputs = new ArrayList<BasicOutput>();

        _outputs.add(new BasicOutput(key));
    }

    public void addOutput(BasicOutput output) {
        if (_outputs == null)
            _outputs = new ArrayList<BasicOutput>();

        _outputs.add(output);
    }

    @Override
    public Set<BasicKeyValue> getInitialContext() {
        return _initialContext;
    }

    public void setInitialContext(Set<BasicKeyValue> initialContext) {
        _initialContext = initialContext;
    }

    public void addInitialContext(String key, String value) {
        if (_initialContext == null)
            _initialContext = new HashSet<BasicKeyValue>();

        _initialContext.add(new BasicKeyValue(key, value));
    }

    @Override
    public List<BasicMapping> getMappings() {
        return _mappings;
    }

    @Override
    public StagingInputErrorHandler getOnInvalidInput() {
        return _onInvalidInput;
    }

    public void setOnInvalidInput(StagingInputErrorHandler onInvalidInput) {
        _onInvalidInput = onInvalidInput;
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

    @Override
    public Map<String, BasicOutput> getOutputMap() {
        return _parsedOutputMap;
    }

    public void setOutputMap(Map<String, BasicOutput> parsedOutputMap) {
        _parsedOutputMap = parsedOutputMap;
    }
}
