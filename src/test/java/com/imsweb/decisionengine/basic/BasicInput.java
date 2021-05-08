/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine.basic;

import com.imsweb.decisionengine.Input;

import java.util.Set;

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
     *
     * @param key input key
     */
    public BasicInput(String key) {
        setKey(key);
    }

    /**
     * Construct with an input key and table
     *
     * @param key   input key
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

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Integer getNaaccrItem() {
        return null;
    }

    @Override
    public String getNaaccrXmlId() {
        return null;
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

    @Override
    public String getUnit() {
        return null;
    }

    @Override
    public Integer getDecimalPlaces() {
        return null;
    }

    @Override
    public Set<String> getMetadata() {
        return null;
    }

    public void setUsedForStaging(Boolean usedForStaging) {
        _usedForStaging = usedForStaging;
    }
}
