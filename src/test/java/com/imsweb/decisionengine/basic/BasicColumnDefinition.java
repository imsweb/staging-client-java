/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine.basic;

import com.imsweb.decisionengine.ColumnDefinition;

public class BasicColumnDefinition implements ColumnDefinition {

    private String _key;
    private ColumnType _type;

    /**
     * Default constructor
     */
    public BasicColumnDefinition() {
    }

    /**
     * Construct with a key and type
     * @param key a column key
     * @param type a column type
     */
    public BasicColumnDefinition(String key, ColumnType type) {
        setKey(key);
        setType(type);
    }

    @Override
    public String getKey() {
        return _key;
    }

    @Override
    public String getName() {
        return null;
    }

    /**
     * Set the column key
     *
     * @param key a column key
     */
    public void setKey(String key) {
        _key = key;
    }

    @Override
    public ColumnType getType() {
        return _type;
    }

    @Override
    public String getSource() {
        return null;
    }

    /**
     * Set the column type
     *
     * @param type a column type
     */
    public void setType(ColumnType type) {
        _type = type;
    }
}
