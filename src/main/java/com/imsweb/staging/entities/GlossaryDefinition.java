/*
 * Copyright (C) 2020 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"name", "definition", "alternate_names", "last_modified"})
public class GlossaryDefinition {

    private String _name;
    private String _definition;
    private List<String> _alternateNames;
    private Date _lastModified;

    public GlossaryDefinition() {
    }

    public GlossaryDefinition(String name, String definition, List<String> alternateNames, Date lastModified) {
        _name = name;
        _definition = definition;
        _alternateNames = alternateNames;
        _lastModified = lastModified;
    }

    @JsonProperty("name")
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    @JsonProperty("definition")
    public String getDefinition() {
        return _definition;
    }

    public void setDefinition(String definition) {
        _definition = definition;
    }

    @JsonProperty("alternate_names")
    public List<String> getAlternateNames() {
        return _alternateNames;
    }

    public void setAlternateNames(List<String> alternateNames) {
        _alternateNames = alternateNames;
    }

    @JsonProperty("last_modified")
    public Date getLastModified() {
        return _lastModified;
    }

    public void setLastModified(Date lastModified) {
        _lastModified = lastModified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GlossaryDefinition))
            return false;
        GlossaryDefinition that = (GlossaryDefinition)o;
        return Objects.equals(_name, that._name) &&
                Objects.equals(_definition, that._definition) &&
                Objects.equals(_alternateNames, that._alternateNames) &&
                Objects.equals(_lastModified, that._lastModified);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_name, _definition, _alternateNames, _lastModified);
    }
}
