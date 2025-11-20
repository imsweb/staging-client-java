/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.staging;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
 * Implementation of DataProvider which loads from an external ZIP file and holds all data in memory
 */
public class ExternalStagingFileDataProvider extends StagingDataProvider {

    private String _algorithm;
    private String _version;
    private final Map<String, StagingTable> _tables = new HashMap<>();
    private final Map<String, StagingSchema> _schemas = new HashMap<>();
    private final Map<String, GlossaryDefinition> _glossaryTerms = new HashMap<>();

    private static final int THRESHOLD_ENTRIES = 10000;
    private static final long THRESHOLD_ENTRY_SIZE = 10L * 1024 * 1024;
    private static final long THRESHOLD_SIZE = 100_000_000; // 100 MB
    private static final double THRESHOLD_RATIO = 50;

    /**
     * Constructor loads all schemas and sets up table cache
     * @param zipFilePath full path to algorithm zip file
     * @throws IOException exception for file operations
     */
    public ExternalStagingFileDataProvider(Path zipFilePath) throws IOException {
        super();

        try (InputStream is = Files.newInputStream(zipFilePath)) {
            init(is);
        }
    }

    /**
     * Constructor loads all schemas and sets up table cache
     * @param zipFileName full path to algorithm zip file
     * @throws IOException exception for file operations
     */
    public ExternalStagingFileDataProvider(String zipFileName) throws IOException {
        this(Paths.get(zipFileName));
    }

    /**
     * Constructor loads all schemas and sets up table cache
     * @param is InputStream pointing the zip file
     * @throws IOException exception for file operations
     */
    public ExternalStagingFileDataProvider(InputStream is) throws IOException {
        super();

        init(is);
    }

    private record EntryData(String json, long uncompressedSize) {}

    /**
     * Read a zip entry from an inputstream and return as a String
     */
    private static EntryData readEntrySafely(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        long total = 0;
        int read;

        while ((read = is.read(buffer)) != -1) {
            total += read;

            if (total > THRESHOLD_ENTRY_SIZE)
                throw new IllegalStateException("Zip entry is too large; maximum permitted per entry is " + THRESHOLD_ENTRY_SIZE + " bytes");

            baos.write(buffer, 0, read);
        }

        String json = baos.toString(StandardCharsets.UTF_8);
        return new EntryData(json, total);
    }

    /**
     * Initialize data provider
     */
    private void init(InputStream is) throws IOException {
        Set<String> algorithms = new HashSet<>();
        Set<String> versions = new HashSet<>();
        long totalSizeArchive = 0L;
        int totalEntries = 0;

        TrieBuilder builder = Trie.builder().onlyWholeWords().ignoreCase();

        try (ZipInputStream stream = new ZipInputStream(new BufferedInputStream(is))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                if (entry.isDirectory() || !entry.getName().endsWith(".json"))
                    continue;

                totalEntries++;

                if (totalEntries > THRESHOLD_ENTRIES)
                    throw new IllegalStateException("Algorithm zip file has too many entries; maximum permitted is " + THRESHOLD_ENTRIES);

                EntryData data = readEntrySafely(stream);

                totalSizeArchive += data.uncompressedSize;
                if (totalSizeArchive > THRESHOLD_SIZE)
                    throw new IllegalStateException("Algorithm zip file uncompressed size is too large; maximum permitted is " + THRESHOLD_SIZE + " bytes");

                long compressedSize = entry.getCompressedSize(); // may be -1 if unknown
                if (compressedSize > 0) {
                    double ratio = (double)data.uncompressedSize / (double)compressedSize;
                    if (ratio > THRESHOLD_RATIO)
                        throw new IllegalStateException("Zip entry compression ratio too high (" + ratio + "); potential zip bomb");
                }

                if (entry.getName().startsWith("tables")) {
                    StagingTable table = getMapper().reader().readValue(getMapper().getFactory().createParser(data.json()), StagingTable.class);

                    initTable(table);

                    algorithms.add(table.getAlgorithm());
                    versions.add(table.getVersion());

                    _tables.put(table.getId(), table);
                }
                else if (entry.getName().startsWith("schemas")) {
                    StagingSchema schema = getMapper().reader().readValue(getMapper().getFactory().createParser(data.json()), StagingSchema.class);

                    initSchema(schema);

                    algorithms.add(schema.getAlgorithm());
                    versions.add(schema.getVersion());

                    _schemas.put(schema.getId(), schema);
                }
                else if (entry.getName().startsWith("glossary")) {
                    GlossaryDefinition glossary = getMapper().reader().readValue(getMapper().getFactory().createParser(data.json()), GlossaryDefinition.class);
                    _glossaryTerms.put(glossary.getName(), glossary);
                    builder.addKeyword(glossary.getName());
                }
            }
        }

        _trie = builder.build();

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
    public Table getTable(String id) {
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
    public Schema getSchema(String id) {
        return _schemas.get(id);
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
    public Set<String> getGlossaryTerms() {
        return _glossaryTerms.keySet();
    }

    @Override
    public GlossaryDefinition getGlossaryDefinition(String term) {
        return _glossaryTerms.get(term);
    }

}
