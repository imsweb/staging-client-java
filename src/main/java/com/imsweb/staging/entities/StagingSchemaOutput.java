/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

import com.imsweb.decisionengine.Output;

@JsonPropertyOrder({"key", "name", "description", "naaccr_item", "table"})
@Embedded
public class StagingSchemaOutput implements Output {

    @Property("key")
    private String _key;
    @Property("name")
    private String _name;
    @Property("description")
    private String _description;
    @Property("naaccr_item")
    private Integer _naaccrItem;
    @Property("table")
    private String _table;
    @Property("default")
    private String _default;

    /**
     * Morphia requires a default constructor
     */
    public StagingSchemaOutput() {
    }

    public StagingSchemaOutput(String key, String name) {
        setKey(key);
        setName(name);
    }

    public StagingSchemaOutput(String key, String name, String table) {
        setKey(key);
        setName(name);
        setTable(table);
    }

    /**
     * Copy constructor
     * @param other other StagingSchemaInput
     */
    public StagingSchemaOutput(StagingSchemaOutput other) {
        setKey(other.getKey());
        setName(other.getName());
        setDescription(other.getDescription());
        setNaaccrItem(other.getNaaccrItem());
        setTable(other.getTable());
        setDefault(other.getDefault());
    }

    @Override
    @JsonProperty("key")
    public String getKey() {
        return _key;
    }

    public void setKey(String key) {
        _key = key;
    }

    @JsonProperty("name")
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    @JsonProperty("description")
    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    @JsonProperty("naaccr_item")
    public Integer getNaaccrItem() {
        return _naaccrItem;
    }

    public void setNaaccrItem(Integer naaccrItem) {
        _naaccrItem = naaccrItem;
    }

    @Override
    @JsonProperty("table")
    public String getTable() {
        return _table;
    }

    public void setTable(String table) {
        _table = table;
    }

    @Override
    @JsonProperty("default")
    public String getDefault() {
        return _default;
    }

    public void setDefault(String aDefault) {
        _default = aDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        StagingSchemaOutput that = (StagingSchemaOutput)o;

        // do not include _parsedValues
        return Objects.equals(_key, that._key) &&
                Objects.equals(_name, that._name) &&
                Objects.equals(_description, that._description) &&
                Objects.equals(_naaccrItem, that._naaccrItem) &&
                Objects.equals(_table, that._table) &&
                Objects.equals(_default, that._default);
    }

    @Override
    public int hashCode() {
        // do not include _parsedValues
        return Objects.hash(_key, _name, _description, _naaccrItem, _table, _default);
    }
}
