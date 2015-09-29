/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.UncheckedExecutionException;

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
        _tableCache = CacheBuilder.newBuilder()
                .maximumSize(2500)
                .build(new CacheLoader<String, StagingTable>() {
                    @Override
                    public StagingTable load(String id) throws Exception {
                        StagingTable table = getMapper().reader().readValue(getMapper().getFactory().createParser(createReader(_tableDirectory + "/" + id + ".json")),
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
                    StagingSchema schema = getMapper().reader().readValue(getMapper().getFactory().createParser(createReader(directory + "/" + file + ".json")), StagingSchema.class);

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
        try {
            if (id == null)
                return null;

            return _tableCache.get(id);
        }
        catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause());
        }
        catch (UncheckedExecutionException e) {
            throw new IllegalStateException(e.getCause());
        }
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
        return CharStreams.readLines(createReader(location));
    }

    /**
     * @param location relative file location within the classpath
     * @return The {@link java.io.Reader} resource
     */
    private static Reader createReader(String location) {
        return new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(location), Charsets.UTF_8);
    }

}
