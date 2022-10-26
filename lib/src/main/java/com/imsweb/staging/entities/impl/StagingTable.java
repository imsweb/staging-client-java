/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.imsweb.staging.entities.ColumnDefinition.ColumnType;
import com.imsweb.staging.entities.Table;
import com.imsweb.staging.entities.TableRow;

@JsonPropertyOrder({"id", "algorithm", "version", "name", "title", "subtitle", "description", "notes", "footnotes", "last_modified", "definition", "extra_input", "rows"})
public class StagingTable implements Table {

    private String _displayId;
    private String _algorithm;
    private String _version;
    private String _name;
    private String _title;
    private String _description;
    private String _subtitle;
    private String _notes;
    private String _footnotes;
    private Date _lastModified;
    private List<StagingColumnDefinition> _definition;
    private Set<String> _extraInput;
    private List<List<String>> _rows = new ArrayList<>();

    // parsed fields
    private List<StagingTableRow> _parsedTableRows = new ArrayList<>();

    /**
     * Morphia requires a default constructor
     */
    public StagingTable() {
    }

    public StagingTable(String id) {
        setId(id);
    }

    @Override
    @JsonProperty("id")
    public String getId() {
        return _displayId;
    }

    public void setId(String id) {
        _displayId = id;
    }

    @Override
    @JsonProperty("algorithm")
    public String getAlgorithm() {
        return _algorithm;
    }

    public void setAlgorithm(String algorithm) {
        _algorithm = algorithm;
    }

    @Override
    @JsonProperty("version")
    public String getVersion() {
        return _version;
    }

    public void setVersion(String version) {
        _version = version;
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
    @JsonProperty("title")
    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        _title = title;
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
    @JsonProperty("subtitle")
    public String getSubtitle() {
        return _subtitle;
    }

    public void setSubtitle(String subtitle) {
        _subtitle = subtitle;
    }

    @Override
    @JsonProperty("notes")
    public String getNotes() {
        return _notes;
    }

    public void setNotes(String notes) {
        _notes = notes;
    }

    @Override
    @JsonProperty("footnotes")
    public String getFootnotes() {
        return _footnotes;
    }

    public void setFootnotes(String footnotes) {
        _footnotes = footnotes;
    }

    @Override
    @JsonProperty("last_modified")
    public Date getLastModified() {
        return _lastModified;
    }

    public void setLastModified(Date lastModified) {
        _lastModified = lastModified;
    }

    @Override
    @JsonProperty("definition")
    public List<StagingColumnDefinition> getColumnDefinitions() {
        return _definition;
    }

    public void setColumnDefinitions(List<StagingColumnDefinition> definition) {
        _definition = definition;
    }

    public void addColumnDefinition(String key, ColumnType type) {
        if (_definition == null)
            _definition = new ArrayList<>();

        StagingColumnDefinition def = new StagingColumnDefinition();
        def.setKey(key);
        def.setType(type);

        _definition.add(def);
    }

    @Override
    @JsonProperty("extra_input")
    public Set<String> getExtraInput() {
        return _extraInput;
    }

    @Override
    public void setExtraInput(Set<String> extraInput) {
        _extraInput = extraInput;
    }

    @Override
    @JsonProperty("rows")
    public List<List<String>> getRawRows() {
        return _rows;
    }

    public void setRawRows(List<List<String>> rows) {
        _rows = rows;
    }

    public void addRawRow(String... row) {
        if (_rows == null)
            _rows = new ArrayList<>();

        _rows.add(Arrays.asList(row));
    }

    @Override
    @JsonIgnore
    public List<StagingTableRow> getTableRows() {
        return _parsedTableRows;
    }

    public void setTableRows(List<StagingTableRow> parsedTableRows) {
        _parsedTableRows = parsedTableRows;
    }

    @Override
    public void addTableRow(TableRow row) {
        getTableRows().add((StagingTableRow)row);
    }

    @Override
    public void clearTableRows() {
        _parsedTableRows.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        StagingTable table = (StagingTable)o;

        // intentionally does not include _id, _lastModified, _parsedTableRows
        return Objects.equals(_displayId, table._displayId) &&
                Objects.equals(_algorithm, table._algorithm) &&
                Objects.equals(_version, table._version) &&
                Objects.equals(_name, table._name) &&
                Objects.equals(_title, table._title) &&
                Objects.equals(_description, table._description) &&
                Objects.equals(_subtitle, table._subtitle) &&
                Objects.equals(_notes, table._notes) &&
                Objects.equals(_footnotes, table._footnotes) &&
                Objects.equals(_definition, table._definition) &&
                Objects.equals(_extraInput, table._extraInput) &&
                Objects.equals(_rows, table._rows);
    }

    @Override
    public int hashCode() {
        // intentionally does not include _id, _lastModified, _parsedTableRows
        return Objects.hash(_displayId, _algorithm, _version, _name, _title, _description, _subtitle, _notes, _footnotes, _definition, _extraInput, _rows);
    }
}
