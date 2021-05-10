/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities.basic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.imsweb.staging.entities.Mapping;

public class BasicMapping implements Mapping {

    private String _id;
    private String _name;
    private List<BasicTablePath> _inclusionTables;
    private List<BasicTablePath> _exclusionTables;
    private Set<BasicKeyValue> _initialContext;
    private List<BasicTablePath> _tablePaths;

    /**
     * Constructor
     * @param id mapping indentifier
     */
    public BasicMapping(String id) {
        _id = id;
    }

    /**
     * Contruct a BasicAlgorithm with an intial set of table paths
     * @param id String identifier
     * @param tablePaths a List of BasicTablePath objects
     */
    public BasicMapping(String id, List<BasicTablePath> tablePaths) {
        _id = id;
        _tablePaths = tablePaths;
    }

    @Override
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    @Override
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    @Override
    public List<BasicTablePath> getInclusionTables() {
        return _inclusionTables;
    }

    public void setInclusionTables(List<BasicTablePath> inclusionTables) {
        _inclusionTables = inclusionTables;
    }

    @Override
    public List<BasicTablePath> getExclusionTables() {
        return _exclusionTables;
    }

    public void setExclusionTables(List<BasicTablePath> exclusionTables) {
        _exclusionTables = exclusionTables;
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
    public List<BasicTablePath> getTablePaths() {
        return _tablePaths;
    }

    /**
     * Set the table paths
     * @param tablePaths a List of BasicTablePath objects
     */
    public void setTablePaths(List<BasicTablePath> tablePaths) {
        _tablePaths = tablePaths;
    }

    /**
     * Add a new table path
     * @param path a BasicTablePath
     */
    public void addTablePath(BasicTablePath path) {
        if (_tablePaths == null)
            _tablePaths = new ArrayList<>();

        _tablePaths.add(path);
    }
}
