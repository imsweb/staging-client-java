/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingTable;

/**
 * Implementation of DataProvider which loads from an external ZIP file and holds all data in memory
 */
public class ExternalStagingFileDataProvider extends StagingDataProvider {

    private String _algorithm;
    private String _version;
    private Map<String, StagingTable> _tables = new HashMap<>();
    private Map<String, StagingSchema> _schemas = new HashMap<>();

    /**
     * Constructor loads all schemas and sets up table cache
     * @param zipPath path to zip file containing algorithm version
     */
    public ExternalStagingFileDataProvider(String zipPath) throws IOException {
        super();

        FileSystem fs = FileSystems.newFileSystem(URI.create("jar:file:/" + zipPath), Collections.emptyMap());

        // verify that all the algorithm names and versions are consistent
        Set<String> algorithms = new HashSet<>();
        Set<String> versions = new HashSet<>();

        // iterate over files in the zip file and process schemas and tables
        fs.getRootDirectories()
                .forEach(root -> {
                    try {
                        Files.walk(root)
                                .filter(path -> Files.isRegularFile(path) && path.toString().contains(".json"))
                                .forEach(path -> {
                                            if (path.startsWith("/tables/")) {
                                                try (BufferedReader reader = Files.newBufferedReader(path)) {
                                                    StagingTable table = getMapper().reader().readValue(getMapper().getFactory().createParser(reader), StagingTable.class);

                                                    initTable(table);

                                                    algorithms.add(table.getAlgorithm());
                                                    versions.add(table.getVersion());

                                                    _tables.put(table.getId(), table);
                                                }
                                                catch (IOException e) {
                                                    throw new IllegalStateException("Exception processing table: " + e.getMessage());
                                                }
                                            }
                                            else if (path.startsWith("/schemas")) {
                                                try (BufferedReader reader = Files.newBufferedReader(path)) {
                                                    StagingSchema schema = getMapper().reader().readValue(getMapper().getFactory().createParser(reader), StagingSchema.class);

                                                    initSchema(schema);

                                                    algorithms.add(schema.getAlgorithm());
                                                    versions.add(schema.getVersion());

                                                    _schemas.put(schema.getId(), schema);
                                                }
                                                catch (IOException e) {
                                                    throw new IllegalStateException("Exception processing schema: " + e.getMessage());
                                                }
                                            }
                                        }
                                );
                    }
                    catch (IOException e) {
                        throw new IllegalStateException("Exception processing external algorithm: " + e.getMessage());
                    }
                });

        if (algorithms.size() != 1)
            throw new IllegalStateException("Error initializing provider; only one algorithm should be included in file");
        if (versions.size() != 1)
            throw new IllegalStateException("Error initializing provider; only one version should be included in file");

        _algorithm = algorithms.iterator().next();
        _version = versions.iterator().next();

        // finally, initialize any caches now that everything else has been set up
        invalidateCache();
    }

    @Override
    public String getAlgorithm() {
        return _algorithm.toLowerCase();
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
    public Set<String> getSchemaIds() {
        return _schemas.keySet();
    }

    @Override
    public Set<String> getTableIds() {
        return _tables.keySet();
    }

    @Override
    public StagingSchema getDefinition(String id) {
        return _schemas.get(id);
    }

}
