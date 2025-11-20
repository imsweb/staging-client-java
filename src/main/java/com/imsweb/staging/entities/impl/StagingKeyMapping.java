/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities.impl;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.imsweb.staging.entities.KeyMapping;

@JsonPropertyOrder({"from", "to"})
public class StagingKeyMapping implements KeyMapping {

    String _from;
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

        return Objects.equals(_from, that._from) && Objects.equals(_to, that._to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_from, _to);
    }
}
