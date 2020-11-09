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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie.Hit;

import com.imsweb.staging.entities.GlossaryDefinition;
import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingTable;

/**
 * Implementation of DataProvider which loads from internal directories and holds all data in memory
 */
public class StagingFileDataProvider extends StagingDataProvider {

    private final String _algorithm;
    private final String _version;

    private final Map<String, StagingTable> _tables = new HashMap<>();
    private final Map<String, StagingSchema> _schemas = new HashMap<>();

    private final Map<String, String> _glossaryTerms = new HashMap<>();
    private final AhoCorasickDoubleArrayTrie<String> _trie = new AhoCorasickDoubleArrayTrie<>();

    /**
     * Constructor loads all schemas and sets up table cache
     * @param algorithm algorithm
     * @param version version
     */
    protected StagingFileDataProvider(String algorithm, String version) {
        super();

        _algorithm = algorithm;
        _version = version;

        // loop over all tables and load them into Map
        try {
            String directory = "algorithms/" + algorithm.toLowerCase() + "/" + version + "/tables";
            for (String file : readLines(directory + "/ids.txt")) {
                if (!file.isEmpty()) {
                    StagingTable table = getMapper().reader().readValue(getMapper().getFactory().createParser(getStagingInputStream(directory + "/" + file + ".json")), StagingTable.class);

                    initTable(table);

                    _tables.put(table.getId(), table);
                }
            }
        }
        catch (IOException e) {
            throw new IllegalStateException("IOException reading schemas: " + e.getMessage());
        }

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

        // load the glossary terms
        try {
            String keywords = "algorithms/" + algorithm.toLowerCase() + "/" + version + "/glossary/terms.txt";

            // if the file is not found, that just means that there are no glossary terms
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(keywords);
            if (is != null) {
                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    for (String line : buffer.lines().collect(Collectors.toList())) {
                        if (line.length() > 0) {
                            String[] parts = line.split("~");
                            if (parts.length != 2)
                                throw new IllegalStateException("Error parsing glossary terms.  Should only be two parts of each line in terms.txt");

                            _glossaryTerms.put(parts[0], parts[1]);
                        }
                    }
                }

                _trie.build(_glossaryTerms.keySet().stream().collect(Collectors.toMap(String::toLowerCase, String::toLowerCase)));
            }
        }
        catch (IOException e) {
            throw new IllegalStateException("IOException reading glossary terms: " + e.getMessage());
        }

        // finally, initialize any caches now that everything else has been set up
        invalidateCache();
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
        String id = _glossaryTerms.get(term);
        if (id == null)
            return null;

        String filename = "algorithms/" + getAlgorithm() + "/" + getVersion() + "/glossary/" + id + ".json";
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
        if (input == null)
            return null;

        try {
            return getMapper().reader().readValue(getMapper().getFactory().createParser(input), GlossaryDefinition.class);
        }
        catch (IOException e) {
            throw new IllegalStateException("Error reading glossary term: " + e.getMessage());
        }
    }

    @Override
    public List<Hit<String>> getGlossaryMatches(String text) {
        return _trie.parseText(text);
    }

}
