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

import org.ahocorasick.trie.Trie;
import org.ahocorasick.trie.Trie.TrieBuilder;

import com.imsweb.staging.entities.Endpoint;
import com.imsweb.staging.entities.GlossaryDefinition;
import com.imsweb.staging.entities.Range;
import com.imsweb.staging.entities.Schema;
import com.imsweb.staging.entities.Table;
import com.imsweb.staging.entities.TableRow;
import com.imsweb.staging.entities.impl.StagingEndpoint;
import com.imsweb.staging.entities.impl.StagingRange;
import com.imsweb.staging.entities.impl.StagingSchema;
import com.imsweb.staging.entities.impl.StagingTable;
import com.imsweb.staging.entities.impl.StagingTableRow;

import static com.imsweb.staging.entities.Endpoint.EndpointType;

/**
 * Implementation of DataProvider which loads from internal directories and holds all data in memory
 */
public class StagingFileDataProvider extends StagingDataProvider {

    private final String _algorithm;
    private final String _version;

    private final Map<String, StagingTable> _tables = new HashMap<>();
    private final Map<String, StagingSchema> _schemas = new HashMap<>();

    private final Map<String, String> _glossaryTerms = new HashMap<>();

    private static final String _ALGORITHM_BASE_DIR = "algorithms/";
    private static final String _JSON_EXT = ".json";

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
            String directory = _ALGORITHM_BASE_DIR + algorithm.toLowerCase() + "/" + version + "/tables";
            for (String file : readLines(directory + "/ids.txt")) {
                if (!file.isEmpty()) {
                    StagingTable table = getMapper().reader().readValue(getMapper().getFactory().createParser(getStagingInputStream(directory + "/" + file + _JSON_EXT)), StagingTable.class);

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
            String directory = _ALGORITHM_BASE_DIR + algorithm.toLowerCase() + "/" + version + "/schemas";
            for (String file : readLines(directory + "/ids.txt")) {
                if (!file.isEmpty()) {
                    StagingSchema schema = getMapper().reader().readValue(getMapper().getFactory().createParser(getStagingInputStream(directory + "/" + file + _JSON_EXT)), StagingSchema.class);

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
            String keywords = _ALGORITHM_BASE_DIR + algorithm.toLowerCase() + "/" + version + "/glossary/terms.txt";

            // if the file is not found, that just means that there are no glossary terms
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(keywords);
            if (is != null) {
                TrieBuilder builder = Trie.builder().onlyWholeWords().ignoreCase();

                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    for (String line : buffer.lines().toList()) {
                        if (!line.isEmpty()) {
                            String[] parts = line.split("~");
                            if (parts.length != 2)
                                throw new IllegalStateException("Error parsing glossary terms.  Should only be two parts of each line in terms.txt");

                            _glossaryTerms.put(parts[0], parts[1]);
                            builder.addKeyword(parts[0]);
                        }
                    }
                }

                _trie = builder.build();
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
    public Table getTable(String id) {
        return _tables.get(id);
    }

    @Override
    public Endpoint getEndpoint(EndpointType type, String value) {
        return new StagingEndpoint(type, value);
    }

    @Override
    public TableRow getTableRow() {
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

    @Override
    public Set<String> getSchemaIds() {
        return _schemas.keySet();
    }

    @Override
    public Set<String> getTableIds() {
        return _tables.keySet();
    }

    @Override
    public Schema getSchema(String id) {
        return _schemas.get(id);
    }

    @Override
    public Set<String> getGlossaryTerms() {
        return _glossaryTerms.keySet();
    }

    @SuppressWarnings("java:S1075")
    @Override
    public GlossaryDefinition getGlossaryDefinition(String term) {
        String id = _glossaryTerms.get(term);
        if (id == null)
            return null;

        String filename = _ALGORITHM_BASE_DIR + getAlgorithm() + "/" + getVersion() + "/glossary/" + id + _JSON_EXT;
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

}
