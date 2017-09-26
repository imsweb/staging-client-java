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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingTable;

/**
 * Implementation of DataProvider which loads from an external ZIP file and holds all data in memory
 */
public class ExternalStagingFileDataProvider extends StagingDataProvider {

    private FileSystem _zipFs;
    private String _algorithm;
    private String _version;
    private Set<String> _tableIds;
    private LoadingCache<String, StagingTable> _tableCache;
    private Map<String, StagingSchema> _schemas = new HashMap<>();

    /**
     * Constructor loads all schemas and sets up table cache
     * @param zipPath path to zip file containing algorithm version
     * @param algorithm algorithm
     * @param version version
     */
    public ExternalStagingFileDataProvider(String zipPath, String algorithm, String version) throws IOException {
        super();

        _zipFs = FileSystems.newFileSystem(URI.create("jar:file:/" + zipPath), Collections.emptyMap());

        _algorithm = algorithm;
        _version = version;

        // first get a list of all tables ids
        try {
            _tableIds = new HashSet<>();
            _tableIds.addAll(readLines("tables/ids.txt"));
        }
        catch (IOException e) {
            throw new IllegalStateException("Exception reading ids: " + e.getMessage());
        }

        // set up table cache; it is too slow to load all the tables at startup
        _tableCache = Caffeine.newBuilder().maximumSize(2500).build(this::load);

        // loop over all schemas and load them into Map
        try {
            for (String file : readLines("schemas/ids.txt")) {
                if (!file.isEmpty()) {
                    try (BufferedReader reader = Files.newBufferedReader(_zipFs.getPath("schemas/" + file + ".json"))) {
                        StagingSchema schema = getMapper().reader().readValue(getMapper().getFactory().createParser(reader), StagingSchema.class);

                        initSchema(schema);

                        _schemas.put(schema.getId(), schema);
                    }
                }
            }
        }
        catch (IOException e) {
            throw new IllegalStateException("Exception reading schemas: " + e.getMessage());
        }

        // finally, initialize any caches now that everything else has been set up
        invalidateCache();
    }

    /**
     * @param location relative file location within the classpath
     * @return a {@link String} {@link List} of all lines in the file
     * @throws IOException error reading file
     */
    private List<String> readLines(String location) throws IOException {
        try (BufferedReader buffer = Files.newBufferedReader(_zipFs.getPath(location))) {
            return buffer.lines().collect(Collectors.toList());
        }
    }

    /**
     * Return the algorithm
     * @return the algorithm
     */
    @Override
    public String getAlgorithm() {
        return _algorithm.toLowerCase();
    }

    /**
     * Return the data version
     * @return a String representing the version
     */
    @Override
    public String getVersion() {
        return _version;
    }

    @Override
    public StagingTable getTable(String id) {
        if (id == null)
            return null;

        return _tableCache.get(id);
    }

    @Override
    public Set<String> getSchemaIds() {
        return _schemas.keySet();
    }

    @Override
    public Set<String> getTableIds() {
        return _tableIds;
    }

    @Override
    public StagingSchema getDefinition(String id) {
        return _schemas.get(id);
    }

    /**
     * Load the StagingTable from a file specified by the passed identifier.
     * @param id table identifier
     * @return StagingTable
     * @throws IllegalStateException if the table has an identifier that does not match the name
     */
    private StagingTable load(String id) throws Exception {
        try (BufferedReader reader = Files.newBufferedReader(_zipFs.getPath("tables/" + id + ".json"))) {
            StagingTable table = getMapper().reader().readValue(getMapper().getFactory().createParser(reader), StagingTable.class);

            if (!id.equals(table.getId()))
                throw new IllegalStateException("The table " + id + " has an identifier that doesn't match the name (" + table.getId() + ")");

            initTable(table);

            return table;
        }
    }

}
