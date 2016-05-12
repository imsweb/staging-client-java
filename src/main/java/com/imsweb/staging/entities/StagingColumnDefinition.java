/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.Objects;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.imsweb.decisionengine.ColumnDefinition;

@JsonPropertyOrder({"key", "name", "type", "source"})
@Embedded
public class StagingColumnDefinition implements ColumnDefinition {

    @Property("key")
    private String _key;
    @Property("name")
    private String _name;
    @Property("type")
    private ColumnType _type;
    @Property("source")
    private String _source;

    /**
     * Morphia requires a default constructor
     */
    public StagingColumnDefinition() {
    }

    /**
     * Constructor
     * @param key input key
     * @param name column name
     * @param type column type
     */
    public StagingColumnDefinition(String key, String name, ColumnType type) {
        setKey(key);
        setName(name);
        setType(type);
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

    @Override
    @JsonProperty("type")
    public ColumnType getType() {
        return _type;
    }

    public void setType(ColumnType type) {
        _type = type;
    }

    @JsonProperty("source")
    public String getSource() {
        return _source;
    }

    public void setSource(String source) {
        _source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        StagingColumnDefinition that = (StagingColumnDefinition)o;

        return Objects.equals(_key, that._key) &&
                Objects.equals(_name, that._name) &&
                Objects.equals(_type, that._type) &&
                Objects.equals(_source, that._source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_key, _name, _type, _source);
    }
}
