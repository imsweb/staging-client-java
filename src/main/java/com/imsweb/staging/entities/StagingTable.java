/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Property;

import com.imsweb.decisionengine.Table;

@JsonPropertyOrder({"id", "algorithm", "version", "name", "title", "subtitle", "description", "notes", "footnotes", "last_modified", "definition", "extra_input", "rows"})
@Entity(value = "staging_tables")
public class StagingTable implements Table {

    @Id
    private ObjectId _id;
    @Indexed
    @Property("id")
    private String _displayId;
    @Indexed
    @Property("algorithm")
    private String _algorithm;
    @Indexed
    @Property("version")
    private String _version;
    @Property("name")
    private String _name;
    @Property("title")
    private String _title;
    @Property("description")
    private String _description;
    @Property("subtitle")
    private String _subtitle;
    @Property("notes")
    private String _notes;
    @Property("footnotes")
    private String _footnotes;
    @Property("modified")
    private Date _lastModified;
    @Property("definition")
    private List<StagingColumnDefinition> _definition;
    @Property("extra_input")
    private Set<String> _extraInput;
    @Property("rows")
    private List<List<String>> _rows = new ArrayList<>();

    // parsed fields
    @Property("parsed_table_rows")
    private List<StagingTableRow> _parsedTableRows = new ArrayList<>();

    /**
     * Morphia requires a default constructor
     */
    public StagingTable() {
    }

    @JsonIgnore
    public ObjectId getInternalId() {
        return _id;
    }

    public void setInternalId(ObjectId id) {
        _id = id;
    }

    @Override
    @JsonProperty("id")
    public String getId() {
        return _displayId;
    }

    public void setId(String id) {
        _displayId = id;
    }

    @JsonProperty("algorithm")
    public String getAlgorithm() {
        return _algorithm;
    }

    public void setAlgorithm(String algorithm) {
        _algorithm = algorithm;
    }

    @JsonProperty("version")
    public String getVersion() {
        return _version;
    }

    public void setVersion(String version) {
        _version = version;
    }

    @JsonProperty("name")
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    @JsonProperty("title")
    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        _title = title;
    }

    @JsonProperty("description")
    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    @JsonProperty("subtitle")
    public String getSubtitle() {
        return _subtitle;
    }

    public void setSubtitle(String subtitle) {
        _subtitle = subtitle;
    }

    @JsonProperty("notes")
    public String getNotes() {
        return _notes;
    }

    public void setNotes(String notes) {
        _notes = notes;
    }

    @JsonProperty("footnotes")
    public String getFootnotes() {
        return _footnotes;
    }

    public void setFootnotes(String footnotes) {
        _footnotes = footnotes;
    }

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

    @Override
    @JsonProperty("extra_input")
    public Set<String> getExtraInput() {
        return _extraInput;
    }

    public void setExtraInput(Set<String> extraInput) {
        _extraInput = extraInput;
    }

    @JsonProperty("rows")
    public List<List<String>> getRawRows() {
        return _rows;
    }

    public void setRawRows(List<List<String>> rows) {
        _rows = rows;
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
