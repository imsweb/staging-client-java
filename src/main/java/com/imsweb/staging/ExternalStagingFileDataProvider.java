/*
 * Copyright (C) 2017 Information Management Services, Inc.
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie.Hit;

import com.imsweb.staging.entities.GlossaryDefinition;
import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingTable;

/**
 * Implementation of DataProvider which loads from an external ZIP file and holds all data in memory
 */
public class ExternalStagingFileDataProvider extends StagingDataProvider {

    private String _algorithm;
    private String _version;
    private final Map<String, StagingTable> _tables = new HashMap<>();
    private final Map<String, StagingSchema> _schemas = new HashMap<>();
    private final Map<String, GlossaryDefinition> _glossaryTerms = new HashMap<>();
    private final AhoCorasickDoubleArrayTrie<String> _trie = new AhoCorasickDoubleArrayTrie<>();

    /**
     * Constructor loads all schemas and sets up table cache
     * @param is InputStream pointing the the zip file
     * @throws IOException exception for file operations
     */
    public ExternalStagingFileDataProvider(InputStream is) throws IOException {
        super();

        init(is);
    }

    /**
     * Read a zip entry from an inputstream and return as a byte array
     */
    private static String extractEntry(InputStream is) {
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
    }

    /**
     * Initialize data provider
     */
    private void init(InputStream is) throws IOException {
        Set<String> algorithms = new HashSet<>();
        Set<String> versions = new HashSet<>();

        try (ZipInputStream stream = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                if (entry.isDirectory() || !entry.getName().endsWith(".json"))
                    continue;

                if (entry.getName().startsWith("tables")) {
                    StagingTable table = getMapper().reader().readValue(getMapper().getFactory().createParser(extractEntry(stream)), StagingTable.class);

                    initTable(table);

                    algorithms.add(table.getAlgorithm());
                    versions.add(table.getVersion());

                    _tables.put(table.getId(), table);
                }
                else if (entry.getName().startsWith("schemas")) {
                    StagingSchema schema = getMapper().reader().readValue(getMapper().getFactory().createParser(extractEntry(stream)), StagingSchema.class);

                    initSchema(schema);

                    algorithms.add(schema.getAlgorithm());
                    versions.add(schema.getVersion());

                    _schemas.put(schema.getId(), schema);
                }
                else if (entry.getName().startsWith("glossary")) {
                    GlossaryDefinition glossary = getMapper().reader().readValue(getMapper().getFactory().createParser(extractEntry(stream)), GlossaryDefinition.class);
                    _glossaryTerms.put(glossary.getName(), glossary);
                }
            }
        }

        _trie.build(_glossaryTerms.keySet().stream().collect(Collectors.toMap(String::toLowerCase, String::toLowerCase)));

        // verify that all the algorithm names and versions are consistent
        if (algorithms.size() != 1)
            throw new IllegalStateException("Error initializing provider; only a single algorithm should be included in file");
        if (versions.size() != 1)
            throw new IllegalStateException("Error initializing provider; only a single version should be included in file");

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

    @Override
    public Set<String> getGlossaryTerms() {
        return _glossaryTerms.keySet();
    }

    @Override
    public GlossaryDefinition getGlossaryDefinition(String term) {
        return _glossaryTerms.get(term);
    }

    @Override
    public List<Hit<String>> getGlossaryMatches(String text) {
        return _trie.parseText(text);
    }

}
