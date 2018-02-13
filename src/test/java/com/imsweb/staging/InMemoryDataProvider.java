/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingTable;

/**
 * In implementation of DataProvider which holds all data in memory
 */
public class InMemoryDataProvider extends StagingDataProvider {

    // a list of supported algorithms
    public enum Algorithm {
        TEST1
    }

    private String _algorithm;
    private String _version;

    private Map<String, StagingTable> _tables = new HashMap<>();
    private Map<String, StagingSchema> _schemas = new HashMap<>();

    /**
     * Constructor loads all schemas and sets up table cache
     */
    public InMemoryDataProvider(String algorithm, String version) {
        _algorithm = algorithm;
        _version = version;
    }

    @Override
    public String getAlgorithm() {
        return _algorithm;
    }

    @Override
    public String getVersion() {
        return _version;
    }

    @Override
    public StagingTable getTable(String id) {
        return _tables.get(id);
    }

    /**
     * Add a table
     */
    public void addTable(StagingTable table) {
        _tables.put(table.getId(), table);
    }

    /**
     * Return a set of all the table names
     */
    @Override
    public Set<String> getTableIds() {
        return _tables.keySet();
    }

    @Override
    public StagingSchema getDefinition(String id) {
        return _schemas.get(id);
    }

    @Override
    public Set<String> getSchemaIds() {
        return null;
    }

    /**
     * Add a schema
     */
    public void addSchema(StagingSchema schema) {
        _schemas.put(schema.getId(), schema);
    }

}
