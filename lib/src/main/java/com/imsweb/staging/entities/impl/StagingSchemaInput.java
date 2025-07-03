/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.imsweb.staging.entities.Input;

@JsonPropertyOrder({"key", "name", "description", "naaccr_item", "naaccr_xml_id", "values", "default", "table", "used_for_staging",
        "fail_on_invalid", "unit", "decimal_places", "metadata"})
public class StagingSchemaInput implements Input {

    private String _key;
    private String _name;
    private String _description;
    private Integer _naaccrItem;
    private String _naaccrXmlId;
    private String _default;
    private String _defaultTable;
    private String _table;
    private Boolean _usedForStaging;
    private String _unit;
    private Integer _decimalPlaces;
    private List<StagingMetadata> _metadata;

    /**
     * Morphia requires a default constructor
     */
    public StagingSchemaInput() {
    }

    public StagingSchemaInput(String key) {
        setKey(key);
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
        setDescription(other.getDescription());
        setNaaccrItem(other.getNaaccrItem());
        setNaaccrXmlId(other.getNaaccrXmlId());
        setDefault(other.getDefault());
        setDefaultTable(other.getDefaultTable());
        setTable(other.getTable());
        if (other.getMetadata() != null)
            setMetadata(new ArrayList<>(other.getMetadata()));
        setUsedForStaging(other.getUsedForStaging());
        setUnit(other.getUnit());
        setDecimalPlaces(other.getDecimalPlaces());
    }

    @Override
    @JsonProperty("key")
    public String getKey() {
        return _key;
    }

    public void setKey(String key) {
        _key = key;
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
    @JsonProperty("description")
    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    @Override
    @JsonProperty("naaccr_item")
    public Integer getNaaccrItem() {
        return _naaccrItem;
    }

    public void setNaaccrItem(Integer naaccrItem) {
        _naaccrItem = naaccrItem;
    }

    @Override
    @JsonProperty("naaccr_xml_id")
    public String getNaaccrXmlId() {
        return _naaccrXmlId;
    }

    public void setNaaccrXmlId(String naaccrXmlId) {
        _naaccrXmlId = naaccrXmlId;
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
    @JsonProperty("default_table")
    public String getDefaultTable() {
        return _defaultTable;
    }

    public void setDefaultTable(String defaultTable) {
        _defaultTable = defaultTable;
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
    @JsonProperty("decimal_places")
    public Integer getDecimalPlaces() {
        return _decimalPlaces;
    }

    public void setDecimalPlaces(Integer decimalPlaces) {
        _decimalPlaces = decimalPlaces;
    }

    @Override
    @JsonProperty("unit")
    public String getUnit() {
        return _unit;
    }

    public void setUnit(String unit) {
        _unit = unit;
    }

    @Override
    @JsonProperty("metadata")
    public List<StagingMetadata> getMetadata() {
        return _metadata;
    }

    public void setMetadata(List<StagingMetadata> metadata) {
        _metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        StagingSchemaInput that = (StagingSchemaInput)o;

        // do not include _parsedValues
        return Objects.equals(_key, that._key) &&
                Objects.equals(_name, that._name) &&
                Objects.equals(_description, that._description) &&
                Objects.equals(_naaccrItem, that._naaccrItem) &&
                Objects.equals(_naaccrXmlId, that._naaccrXmlId) &&
                Objects.equals(_default, that._default) &&
                Objects.equals(_defaultTable, that._defaultTable) &&
                Objects.equals(_table, that._table) &&
                Objects.equals(_usedForStaging, that._usedForStaging) &&
                Objects.equals(_unit, that._unit) &&
                Objects.equals(_decimalPlaces, that._decimalPlaces) &&
                Objects.equals(_metadata, that._metadata);
    }

    @Override
    public int hashCode() {
        // do not include _parsedValues
        return Objects.hash(_key, _name, _description, _naaccrItem, _naaccrXmlId, _default, _defaultTable, _table, _usedForStaging, _unit, _decimalPlaces, _metadata);
    }
}
