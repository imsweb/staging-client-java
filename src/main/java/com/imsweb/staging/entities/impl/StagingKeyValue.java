/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities.impl;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.imsweb.staging.entities.KeyValue;

@JsonPropertyOrder({"key", "value"})
public class StagingKeyValue implements KeyValue {

    private String _key;
    private String _value;

    public StagingKeyValue() {
    }

    public StagingKeyValue(String key, String value) {
        _key = key;
        _value = value;
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
    @JsonProperty("value")
    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        StagingKeyValue that = (StagingKeyValue)o;

        return Objects.equals(_key, that._key) && Objects.equals(_value, that._value);

    }

    @Override
    public int hashCode() {
        return Objects.hash(_key, _value);
    }
}
