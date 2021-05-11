/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import com.imsweb.staging.entities.Mapping;

@JsonPropertyOrder({"id", "name", "inclusion_tables", "exclusion_tables", "initial_context", "tables"})
public class StagingMapping implements Mapping {

    private String _id;
    private String _name;
    private List<StagingTablePath> _inclusionTables;
    private List<StagingTablePath> _exclusionTables;
    private Set<StagingKeyValue> _initialContext;
    private List<StagingTablePath> _tablePaths;

    /**
     * Default constructor
     */
    public StagingMapping() {
    }

    public StagingMapping(String id) {
        setId(id);
    }

    public StagingMapping(String id, String name) {
        setId(id);
        setName(name);
    }

    public StagingMapping(String id, List<StagingTablePath> tablePaths) {
        _id = id;
        _tablePaths = tablePaths;
    }

    @Override
    @JsonProperty("id")
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    @Override
    @JsonProperty("name")
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    @Override
    @JsonProperty("inclusion_tables")
    public List<StagingTablePath> getInclusionTables() {
        return _inclusionTables;
    }

    public void setInclusionTables(List<StagingTablePath> inclusionTables) {
        _inclusionTables = inclusionTables;
    }

    @Override
    @JsonProperty("exclusion_tables")
    public List<StagingTablePath> getExclusionTables() {
        return _exclusionTables;
    }

    public void setExclusionTables(List<StagingTablePath> exclusionTables) {
        _exclusionTables = exclusionTables;
    }

    @Override
    @JsonProperty("initial_context")
    public Set<StagingKeyValue> getInitialContext() {
        return _initialContext;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    public void setInitialContext(Set<StagingKeyValue> initialContext) {
        _initialContext = initialContext;
    }

    @Override
    @JsonProperty("tables")
    public List<StagingTablePath> getTablePaths() {
        return _tablePaths;
    }

    public void setTablePaths(List<StagingTablePath> tablePaths) {
        _tablePaths = tablePaths;
    }

    public void addTablePath(StagingTablePath path) {
        if (_tablePaths == null)
            _tablePaths = new ArrayList<>();

        _tablePaths.add(path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        StagingMapping mapping = (StagingMapping)o;

        return Objects.equals(_id, mapping._id) &&
                Objects.equals(_name, mapping._name) &&
                Objects.equals(_inclusionTables, mapping._inclusionTables) &&
                Objects.equals(_exclusionTables, mapping._exclusionTables) &&
                Objects.equals(_initialContext, mapping._initialContext) &&
                Objects.equals(_tablePaths, mapping._tablePaths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id, _name, _inclusionTables, _exclusionTables, _initialContext, _tablePaths);
    }
}
