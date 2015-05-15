/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine.basic;

import com.imsweb.decisionengine.Output;

public class BasicOutput implements Output {

    private String _key;
    private String _table;

    /**
     * Default constrctor
     */
    public BasicOutput() {
    }

    /**
     * Construct with an input key
     * @param key input key
     */
    public BasicOutput(String key) {
        setKey(key);
    }

    /**
     * Construct with an input key and table
     * @param key input key
     * @param table table
     */
    public BasicOutput(String key, String table) {
        setKey(key);
        setTable(table);
    }

    @Override
    public String getKey() {
        return _key;
    }

    public void setKey(String key) {
        _key = key;
    }

    @Override
    public String getTable() {
        return _table;
    }

    public void setTable(String table) {
        _table = table;
    }
}
