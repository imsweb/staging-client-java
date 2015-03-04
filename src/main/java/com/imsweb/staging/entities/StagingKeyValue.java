/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.Objects;

import com.imsweb.decisionengine.KeyValue;

@JsonPropertyOrder({"key", "value"})
@Embedded
public class StagingKeyValue implements KeyValue {

    @Property("key")
    private String _key;
    @Property("value")
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

        return Objects.equal(_key, that._key) && Objects.equal(_value, that._value);

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(_key, _value);
    }
}
