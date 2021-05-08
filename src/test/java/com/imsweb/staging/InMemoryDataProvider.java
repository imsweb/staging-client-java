/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging;

import com.imsweb.staging.entities.GlossaryDefinition;
import com.imsweb.staging.entities.GlossaryHit;
import com.imsweb.staging.entities.StagingEndpoint;
import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingTable;
import com.imsweb.staging.entities.StagingTableRow;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.imsweb.decisionengine.Endpoint.EndpointType;

/**
 * In implementation of DataProvider which holds all data in memory
 */
public class InMemoryDataProvider extends StagingDataProvider {

    private final String _algorithm;
    private final String _version;

    private final Map<String, StagingTable> _tables = new HashMap<>();
    private final Map<String, StagingSchema> _schemas = new HashMap<>();

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

    @Override
    public StagingEndpoint getEndpoint(EndpointType type, String value) {
        return new StagingEndpoint(type, value);
    }

    @Override
    public StagingTableRow getTableRow() {
        return new StagingTableRow();
    }

    /**
     * Add a table
     */
    public void addTable(StagingTable table) {
        initTable(table);
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
    public StagingSchema getSchema(String id) {
        return _schemas.get(id);
    }

    @Override
    public Set<String> getSchemaIds() {
        return _schemas.keySet();
    }

    /**
     * Add a schema
     */
    public void addSchema(StagingSchema schema) {
        initSchema(schema);
        _schemas.put(schema.getId(), schema);
    }

    @Override
    public Set<String> getGlossaryTerms() {
        throw new RuntimeException("Glossary not supported in this provider");
    }

    @Override
    public GlossaryDefinition getGlossaryDefinition(String term) {
        throw new RuntimeException("Glossary not supported in this provider");
    }

    @Override
    public Collection<GlossaryHit> getGlossaryMatches(String text) {
        throw new RuntimeException("Glossary not supported in this provider");
    }
}
