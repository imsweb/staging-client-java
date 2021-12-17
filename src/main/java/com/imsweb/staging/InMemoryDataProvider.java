/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.imsweb.staging.entities.GlossaryDefinition;
import com.imsweb.staging.entities.GlossaryHit;
import com.imsweb.staging.entities.Range;
import com.imsweb.staging.entities.impl.StagingEndpoint;
import com.imsweb.staging.entities.impl.StagingRange;
import com.imsweb.staging.entities.impl.StagingSchema;
import com.imsweb.staging.entities.impl.StagingTable;
import com.imsweb.staging.entities.impl.StagingTableRow;

import static com.imsweb.staging.entities.Endpoint.EndpointType;

/**
 * In implementation of DataProvider which holds all data in memory
 */
public class InMemoryDataProvider extends StagingDataProvider {

    private final String _algorithm;
    private final String _version;

    private final Map<String, StagingTable> _tables = new HashMap<>();
    private final Map<String, StagingSchema> _schemas = new HashMap<>();

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

    @Override
    public Range getMatchAllRange() {
        return new StagingRange();
    }

    @Override
    public Range getRange(String low, String high) {
        return new StagingRange(low, high);
    }

    public void addTable(StagingTable table) {
        initTable(table);
        _tables.put(table.getId(), table);
    }

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

    public void addSchema(StagingSchema schema) {
        initSchema(schema);
        _schemas.put(schema.getId(), schema);
    }

    @Override
    public Set<String> getGlossaryTerms() {
        throw new IllegalStateException("Glossary not supported in this provider");
    }

    @Override
    public GlossaryDefinition getGlossaryDefinition(String term) {
        throw new IllegalStateException("Glossary not supported in this provider");
    }

    @Override
    public Collection<GlossaryHit> getGlossaryMatches(String text) {
        throw new IllegalStateException("Glossary not supported in this provider");
    }
}
