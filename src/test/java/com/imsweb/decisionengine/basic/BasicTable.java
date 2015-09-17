/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.imsweb.decisionengine.ColumnDefinition.ColumnType;
import com.imsweb.decisionengine.Table;

public class BasicTable implements Table {

    private String _id;
    private List<BasicColumnDefinition> _definitions;
    private Set<String> _extraInput;
    private List<List<String>> _rows = new ArrayList<>();

    // parsed fields
    private List<BasicTableRow> _parsedTableRows = new ArrayList<>();

    /**
     * Default constructor
     */
    public BasicTable() {
    }

    /**
     * Construct with a table name
     * @param name a table name
     */
    public BasicTable(String name) {
        setId(name);
    }

    @Override
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    @Override
    public List<BasicColumnDefinition> getColumnDefinitions() {
        return _definitions;
    }

    @Override
    public Set<String> getExtraInput() {
        return _extraInput;
    }

    public void setExtraInput(Set<String> extraInput) {
        _extraInput = extraInput;
    }

    public void setColumnDefinitions(List<BasicColumnDefinition> definitions) {
        _definitions = definitions;
    }

    public void addColumnDefinition(String key, ColumnType type) {
        if (_definitions == null)
            _definitions = new ArrayList<>();

        _definitions.add(new BasicColumnDefinition(key, type));
    }

    public List<List<String>> getRawRows() {
        return _rows;
    }

    public void setRawRows(List<List<String>> rows) {
        _rows = rows;
    }

    public void addRawRow(String... row) {
        if (_rows == null)
            _rows = new ArrayList<>();

        _rows.add(Arrays.asList(row));
    }

    @Override
    public List<BasicTableRow> getTableRows() {
        return _parsedTableRows;
    }

    public void setTableRows(List<BasicTableRow> parsedTableRows) {
        _parsedTableRows = parsedTableRows;
    }

}
