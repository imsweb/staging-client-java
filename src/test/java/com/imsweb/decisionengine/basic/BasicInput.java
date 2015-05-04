/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine.basic;

import com.imsweb.decisionengine.Input;

public class BasicInput implements Input {

    private String _key;
    private String _default;
    private String _table;
    private Boolean _usedForStaging;

    /**
     * Default constrctor
     */
    public BasicInput() {
    }

    /**
     * Construct with an input key
     * @param key input key
     */
    public BasicInput(String key) {
        setKey(key);
    }

    /**
     * Construct with an input key and table
     * @param key input key
     * @param table table
     */
    public BasicInput(String key, String table) {
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
    public String getDefault() {
        return _default;
    }

    public void setDefault(String aDefault) {
        _default = aDefault;
    }

    @Override
    public String getTable() {
        return _table;
    }

    public void setTable(String table) {
        _table = table;
    }

    @Override
    public Boolean getUsedForStaging() {
        return _usedForStaging;
    }

    public void setUsedForStaging(Boolean usedForStaging) {
        _usedForStaging = usedForStaging;
    }
}
