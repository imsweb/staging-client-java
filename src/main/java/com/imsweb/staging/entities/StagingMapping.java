/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

import com.imsweb.decisionengine.Mapping;

@JsonPropertyOrder({"id", "name", "inclusion_tables", "exclusion_tables", "initial_context", "tables"})
@Embedded
public class StagingMapping implements Mapping {

    @Property("id")
    private String _id;
    @Property("name")
    private String _name;
    @Embedded("inclusion_tables")
    private List<StagingTablePath> _inclusionTables;
    @Embedded("exclusion_tables")
    private List<StagingTablePath> _exclusionTables;
    @Embedded("initial_context")
    private Set<StagingKeyValue> _initialContext;
    @Embedded("tables")
    private List<StagingTablePath> _tablePaths;

    /**
     * Default constructor
     */
    public StagingMapping() {
    }

    /**
     * Constructs with a name and title
     * @param id identifier
     * @param name name
     */
    public StagingMapping(String id, String name) {
        setId(id);
        setName(name);
    }

    @Override
    @JsonProperty("id")
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

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
