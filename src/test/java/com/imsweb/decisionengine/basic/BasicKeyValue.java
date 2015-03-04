/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine.basic;

import com.imsweb.decisionengine.KeyValue;

public class BasicKeyValue implements KeyValue {

    private String _key;
    private String _value;

    public BasicKeyValue() {
    }

    public BasicKeyValue(String key, String value) {
        _key = key;
        _value = value;
    }

    @Override
    public String getKey() {
        return _key;
    }

    public void setKey(String key) {
        _key = key;
    }

    @Override
    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }
}
