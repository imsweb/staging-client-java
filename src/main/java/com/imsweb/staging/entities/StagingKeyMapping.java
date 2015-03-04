/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.Objects;

import com.imsweb.decisionengine.KeyMapping;

@JsonPropertyOrder({"from", "to"})
@Embedded
public class StagingKeyMapping implements KeyMapping {

    @Property("from")
    String _from;
    @Property("to")
    String _to;

    public StagingKeyMapping() {
    }

    public StagingKeyMapping(String from, String to) {
        _from = from;
        _to = to;
    }

    @Override
    @JsonProperty("from")
    public String getFrom() {
        return _from;
    }

    public void setFrom(String from) {
        _from = from;
    }

    @Override
    @JsonProperty("to")
    public String getTo() {
        return _to;
    }

    public void setTo(String to) {
        _to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        StagingKeyMapping that = (StagingKeyMapping)o;

        return Objects.equal(_from, that._from) && Objects.equal(_to, that._to);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(_from, _to);
    }
}
