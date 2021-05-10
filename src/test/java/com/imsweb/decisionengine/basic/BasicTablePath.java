/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine.basic;

import java.util.HashSet;
import java.util.Set;

import com.imsweb.decisionengine.TablePath;

public class BasicTablePath implements TablePath {

    private String _id;
    private Set<BasicKeyMapping> _input;
    private Set<BasicKeyMapping> _output;

    /**
     * Default constructor
     */
    public BasicTablePath() {
    }

    /**
     * Construct with a table name
     * @param id a table identifier
     */
    public BasicTablePath(String id) {
        _id = id;
    }

    @Override
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    @Override
    public Set<BasicKeyMapping> getInputMapping() {
        return _input;
    }

    public void setInputMapping(Set<BasicKeyMapping> input) {
        _input = input;
    }

    public void addInputMapping(String from, String to) {
        if (_input == null)
            _input = new HashSet<>();

        _input.add(new BasicKeyMapping(from, to));
    }

    @Override
    public Set<BasicKeyMapping> getOutputMapping() {
        return _output;
    }

    public void setOutputMapping(Set<BasicKeyMapping> output) {
        _output = output;
    }

    public void addOutputMapping(String from, String to) {
        if (_output == null)
            _output = new HashSet<>();

        _output.add(new BasicKeyMapping(from, to));
    }

    @Override
    public Set<String> getInputs() {
        return null;
    }

    @Override
    public Set<String> getOutputs() {
        return null;
    }

}
