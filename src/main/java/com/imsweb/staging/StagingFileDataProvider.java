/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingTable;

/**
 * In implementation of DataProvider which holds all data in memory
 */
public class StagingFileDataProvider extends StagingDataProvider {

    private String _algorithm;
    private String _version;
    private String _tableDirectory;
    private Set<String> _tableIds;
    private LoadingCache<String, StagingTable> _tableCache;
    private Map<String, StagingSchema> _schemas = new HashMap<>();

    /**
     * Constructor loads all schemas and sets up table cache
     * @param algorithm algorithm
     * @param version version
     */
    protected StagingFileDataProvider(String algorithm, String version) {
        super();

        _algorithm = algorithm;
        _version = version;

        _tableDirectory = "algorithms/" + algorithm.toLowerCase() + "/" + version + "/tables";

        // first get a list of all tables ids
        try {
            _tableIds = new HashSet<>();
            for (String tableId : readLines(_tableDirectory + "/ids.txt"))
                _tableIds.add(tableId);
        }
        catch (IOException e) {
            throw new IllegalStateException("IOException reading ids: " + e.getMessage());
        }

        // set up table cache; it is too slow to load all the tables at startup
        _tableCache = Caffeine.newBuilder()
                .maximumSize(2500)
                .build(new CacheLoader<String, StagingTable>() {
                    @Override
                    public StagingTable load(String id) throws Exception {
                        StagingTable table = getMapper().reader().readValue(getMapper().getFactory().createParser(getStagingInputStream(_tableDirectory + "/" + id + ".json")),
                                StagingTable.class);

                        if (!id.equals(table.getId()))
                            throw new IllegalStateException("The table " + id + " has an identifier that doesn't match the name (" + table.getId() + ")");

                        initTable(table);

                        return table;
                    }
                });

        // loop over all schemas and load them into Map
        try {
            String directory = "algorithms/" + algorithm.toLowerCase() + "/" + version + "/schemas";
            for (String file : readLines(directory + "/ids.txt")) {
                if (!file.isEmpty()) {
                    StagingSchema schema = getMapper().reader().readValue(getMapper().getFactory().createParser(getStagingInputStream(directory + "/" + file + ".json")), StagingSchema.class);

                    initSchema(schema);

                    _schemas.put(schema.getId(), schema);
                }
            }
        }
        catch (IOException e) {
            throw new IllegalStateException("IOException reading schemas: " + e.getMessage());
        }

        // finally, initialize any caches now that everything else has been set up
        invalidateCache();
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
     * @param location relative file location within the classpath
     * @return a {@link String} {@link java.util.List} of all lines in the file
     * @throws IOException error reading file
     */
    private static List<String> readLines(String location) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(getStagingInputStream(location), StandardCharsets.UTF_8))) {
            return buffer.lines().collect(Collectors.toList());
        }
    }

    /**
     * @param location relative file location within the classpath
     * @return The {@link java.io.Reader} resource
     */
    private static InputStream getStagingInputStream(String location) {
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(location);

        if (input == null)
            throw new IllegalStateException("Internal error reading file; File could not be found: " + location);

        return input;
    }

}
