/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Objects;

import com.imsweb.decisionengine.Input;

@JsonPropertyOrder({"key", "name", "naaccr_item", "values", "default", "table", "used_for_staging", "fail_on_invalid", "unit", "decimal_places", "metadata"})
@Embedded
public class StagingSchemaInput implements Input {

    @Property("key")
    private String _key;
    @Property("name")
    private String _name;
    @Property("naaccr_item")
    private Integer _naaccrItem;
    @Property("values")
    private String _values;
    @Property("default")
    private String _default;
    @Property("table")
    private String _table;
    @Property("used_for_staging")
    private Boolean _usedForStaging;
    @Property("fail_on_invalid")
    private Boolean _failOnInvalid;
    @Property("unit")
    private String _unit;
    @Property("decimal_places")
    private Integer _decimalPlaces;
    @Property("metadata")
    private Set<String> _metadata;

    // parsed fields
    @Embedded("parsed_values")
    private List<StagingStringRange> _parsedValues = new ArrayList<StagingStringRange>();

    /**
     * Morphia requires a default constructor
     */
    public StagingSchemaInput() {
    }

    public StagingSchemaInput(String key, String name) {
        setKey(key);
        setName(name);
    }

    public StagingSchemaInput(String key, String name, String table) {
        setKey(key);
        setName(name);
        setTable(table);
    }

    /**
     * Copy constructor
     * @param other other StagingSchemaInput
     */
    public StagingSchemaInput(StagingSchemaInput other) {
        setKey(other.getKey());
        setName(other.getName());
        setNaaccrItem(other.getNaaccrItem());
        setRawValues(other.getRawValues());
        setDefault(other.getDefault());
        setTable(other.getTable());
        if (other.getMetadata() != null)
            setMetadata(new HashSet<String>(other.getMetadata()));
        setUsedForStaging(other.getUsedForStaging());
        setUnit(other.getUnit());
        setDecimalPlaces(other.getDecimalPlaces());
        setRawValues(other.getRawValues());
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

    @JsonProperty("naaccr_item")
    public Integer getNaaccrItem() {
        return _naaccrItem;
    }

    public void setNaaccrItem(Integer naaccrItem) {
        _naaccrItem = naaccrItem;
    }

    @JsonProperty("values")
    public String getRawValues() {
        return _values;
    }

    public void setRawValues(String values) {
        _values = values;
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
    @JsonProperty("table")
    public String getTable() {
        return _table;
    }

    public void setTable(String table) {
        _table = table;
    }

    @Override
    @JsonProperty("used_for_staging")
    public Boolean getUsedForStaging() {
        return _usedForStaging;
    }

    public void setUsedForStaging(Boolean usedForStaging) {
        _usedForStaging = usedForStaging;
    }

    @Override
    @JsonProperty("fail_on_invalid")
    public Boolean getFailOnInvalid() {
        return _failOnInvalid;
    }

    public void setFailOnInvalid(Boolean failOnInvalid) {
        _failOnInvalid = failOnInvalid;
    }

    @JsonProperty("decimal_places")
    public Integer getDecimalPlaces() {
        return _decimalPlaces;
    }

    public void setDecimalPlaces(Integer decimalPlaces) {
        _decimalPlaces = decimalPlaces;
    }

    @JsonProperty("unit")
    public String getUnit() {
        return _unit;
    }

    public void setUnit(String unit) {
        _unit = unit;
    }

    @JsonProperty("metadata")
    public Set<String> getMetadata() {
        return _metadata;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    public void setMetadata(Set<String> metadata) {
        _metadata = metadata;
    }

    @Override
    @JsonIgnore
    public List<StagingStringRange> getValues() {
        return _parsedValues;
    }

    public void setValues(List<StagingStringRange> parsedValues) {
        _parsedValues = parsedValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        StagingSchemaInput that = (StagingSchemaInput)o;

        // do not include _parsedValues
        return Objects.equal(_key, that._key) &&
                Objects.equal(_name, that._name) &&
                Objects.equal(_naaccrItem, that._naaccrItem) &&
                Objects.equal(_values, that._values) &&
                Objects.equal(_default, that._default) &&
                Objects.equal(_table, that._table) &&
                Objects.equal(_usedForStaging, that._usedForStaging) &&
                Objects.equal(_unit, that._unit) &&
                Objects.equal(_decimalPlaces, that._decimalPlaces) &&
                Objects.equal(_metadata, that._metadata);
    }

    @Override
    public int hashCode() {
        // do not include _parsedValues
        return Objects.hashCode(_key, _name, _naaccrItem, _values, _default, _table, _usedForStaging, _unit, _decimalPlaces, _metadata);
    }
}
