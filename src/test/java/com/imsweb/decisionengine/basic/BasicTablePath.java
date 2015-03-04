/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine.basic;

import java.util.ArrayList;
import java.util.List;

import com.imsweb.decisionengine.TablePath;

public class BasicTablePath implements TablePath {

    private String _id;
    private List<BasicKeyMapping> _input;
    private List<BasicKeyMapping> _output;

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
    public List<BasicKeyMapping> getInputMapping() {
        return _input;
    }

    public void setInputMapping(List<BasicKeyMapping> input) {
        _input = input;
    }

    public void addInputMapping(String from, String to) {
        if (_input == null)
            _input = new ArrayList<BasicKeyMapping>();

        _input.add(new BasicKeyMapping(from, to));
    }

    @Override
    public List<BasicKeyMapping> getOutputMapping() {
        return _output;
    }

    public void setOutputMapping(List<BasicKeyMapping> output) {
        _output = output;
    }

    public void addOutputMapping(String from, String to) {
        if (_output == null)
            _output = new ArrayList<BasicKeyMapping>();

        _output.add(new BasicKeyMapping(from, to));
    }
}
