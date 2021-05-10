/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine.basic;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.imsweb.decisionengine.Input;
import com.imsweb.decisionengine.Output;
import com.imsweb.decisionengine.Schema;

public class BasicSchema implements Schema {

    private String _id;
    private List<BasicInput> _inputs;
    private List<BasicOutput> _outputs;
    private Set<BasicKeyValue> _initialContext;
    private List<BasicMapping> _mappings;
    private StagingInputErrorHandler _onInvalidInput;

    // parsed fields
    private Map<String, BasicInput> _parsedInputMap = new HashMap<>();
    private Map<String, BasicOutput> _parsedOutputMap = new HashMap<>();

    /**
     * Default constructor
     */
    public BasicSchema() {
    }

    /**
     * Construct with an indentifier
     * @param id a definition identifier
     */
    public BasicSchema(String id) {
        setId(id);
    }

    @Override
    public String getId() {
        return _id;
    }

    @Override
    public String getAlgorithm() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getSubtitle() {
        return null;
    }

    @Override
    public String getNotes() {
        return null;
    }

    @Override
    public Date getLastModified() {
        return null;
    }

    @Override
    public Integer getSchemaNum() {
        return null;
    }

    @Override
    public String getSchemaSelectionTable() {
        return null;
    }

    @Override
    public Set<String> getSchemaDiscriminators() {
        return null;
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
            _inputs = new ArrayList<>();

        _inputs.add(new BasicInput(key));
    }

    public void addInput(BasicInput input) {
        if (_inputs == null)
            _inputs = new ArrayList<>();

        _inputs.add(input);
    }

    public void addInput(String key, String table) {
        if (_inputs == null)
            _inputs = new ArrayList<>();

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
            _outputs = new ArrayList<>();

        _outputs.add(new BasicOutput(key));
    }

    public void addOutput(BasicOutput output) {
        if (_outputs == null)
            _outputs = new ArrayList<>();

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
            _initialContext = new HashSet<>();

        _initialContext.add(new BasicKeyValue(key, value));
    }

    @Override
    public List<BasicMapping> getMappings() {
        return _mappings;
    }

    @Override
    public Set<String> getInvolvedTables() {
        return null;
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
            _mappings = new ArrayList<>();

        _mappings.add(mapping);
    }

    @Override
    public Map<String, BasicInput> getInputMap() {
        return _parsedInputMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setInputMap(Map<String, ? extends Input> parsedInputMap) {
        _parsedInputMap = (Map<String, BasicInput>)parsedInputMap;
    }

    @Override
    public Map<String, BasicOutput> getOutputMap() {
        return _parsedOutputMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setOutputMap(Map<String, ? extends Output> parsedOutputMap) {
        _parsedOutputMap = (Map<String, BasicOutput>)parsedOutputMap;
    }
}
