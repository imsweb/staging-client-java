package com.imsweb.staging.updater;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.imsweb.seerapi.client.SeerApi;
import com.imsweb.seerapi.client.glossary.Glossary;
import com.imsweb.seerapi.client.glossary.Glossary.Category;
import com.imsweb.seerapi.client.glossary.GlossaryService;
import com.imsweb.seerapi.client.shared.KeywordMatch;
import com.imsweb.seerapi.client.staging.StagingSchema;
import com.imsweb.seerapi.client.staging.StagingSchemaInfo;
import com.imsweb.seerapi.client.staging.StagingService;
import com.imsweb.seerapi.client.staging.StagingTable;
import com.imsweb.staging.entities.GlossaryDefinition;
import com.imsweb.staging.util.Stopwatch;

/**
 * Updates the staging library data from the API.
 * 1. Gets a list of schema IDs
 * 2. Gets a list of table IDs
 * 3. Deletes all the existing schemas and tables
 * 4. Gets the tables from the API, initializes them, and saves them
 * 5. Saves the table identifiers into the ids.txt in the tables directory
 * 6. Gets the schemas from the API, initializes them (including the involved tables), and saves them
 * 7. Saves the schema identifiers into the ids.txt in the schemas directory
 */
@SuppressWarnings({"java:S1192", "ConstantConditions"})
public final class UpdaterUtils {

    private static final Logger _LOG = LoggerFactory.getLogger(UpdaterUtils.class);

    private static final Pattern _ID_CHARACTERS = Pattern.compile("[a-z0-9_]+");

    /**
     * Private constructor
     */
    private UpdaterUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void update(String algorithm, String version, Set<String> glossaryCategories) throws IOException {
        update(algorithm, version, glossaryCategories, null);
    }

    public static void update(String algorithm, String version, Set<String> glossaryCategories, Path outputDir) throws IOException {
        Stopwatch stopwatch = Stopwatch.create();

        // convert set of strings to set of Enums; we can't expose the optional dependency Category class in the method signature.
        Set<Category> categories = glossaryCategories.stream().map(Category::valueOf).collect(Collectors.toSet());

        _LOG.info("Updating {} version {} from SEER*API", algorithm, version);

        // create an object mapper used to output the entities
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(Include.ALWAYS, Include.NON_NULL));
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        mapper.setDateFormat(format);

        StagingService staging = new SeerApi.Builder().connect().staging();

        // first, get a list of unused tables so they can be ignored later
        _LOG.info("Getting list of unused table identifiers");
        Set<String> unusedTableIds = new HashSet<>();
        for (StagingTable table : staging.tables(algorithm, version, null, true).execute().body())
            unusedTableIds.add(table.getId());
        _LOG.info("{} unused table identifiers found.", unusedTableIds.size());

        _LOG.info("Getting list of table identifiers");
        List<String> tableIds = new ArrayList<>();
        for (StagingTable table : staging.tables(algorithm, version, null).execute().body()) {
            String id = table.getId();

            // if there are invalid table identifiers, just skip them
            if (!_ID_CHARACTERS.matcher(id).matches())
                _LOG.info(" **** skipping bad table identifier: '{}' ****", id);
            else if (unusedTableIds.contains(id))
                _LOG.info(" **** skipping unused table identifier: '{}' ****", id);
            else
                tableIds.add(id);
        }
        _LOG.info("{} valid table identifiers found.", tableIds.size());

        _LOG.info("Getting list of schema identifiers...");
        List<String> schemaIds = new ArrayList<>();
        for (StagingSchemaInfo schema : staging.schemas(algorithm, version).execute().body()) {
            String id = schema.getId();

            // if there are invalid schema identifiers, just skip them
            if (!_ID_CHARACTERS.matcher(id).matches())
                _LOG.info(" **** skipping bad schema identifier: '{}' ****", id);
            else
                schemaIds.add(id);
        }
        _LOG.info("{} valid schema idenfiers found.", schemaIds.size());

        // get root algorithms directory in src/main/resources or use the passed in Path
        Path resourceDirectory = Paths.get("src", "main", "resources");
        String baseDirectory = (outputDir != null) ? outputDir.toFile().getAbsolutePath() : resourceDirectory.toFile().getAbsolutePath();

        // add the algorithm and version to the path
        baseDirectory = baseDirectory + "/algorithms/" + algorithm + "/" + version;

        String schemaDir = baseDirectory + "/schemas";
        _LOG.info("Deleting all files from {}...", schemaDir);
        int count = purgeDirectory(new File(schemaDir));
        _LOG.info("Removed {} files", count);

        String tableDir = baseDirectory + "/tables";
        _LOG.info("Deleting all files from {}...", tableDir);
        count = purgeDirectory(new File(tableDir));
        _LOG.info("Removed {} files", count);

        // create map to hold all the relevant glossary entries
        Map<String, String> glossaryEntries = new HashMap<>();

        // import the tables
        for (String tableId : tableIds) {
            StagingTable table = staging.tableById(algorithm, version, tableId).execute().body();
            String tableText = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(table);

            Files.write(Paths.get(tableDir + "/" + table.getId() + ".json"), tableText.getBytes(StandardCharsets.UTF_8));
            _LOG.info("Saved table: {}", table.getId());

            // collect the glossary hits
            Set<KeywordMatch> matches = staging.tableGlossary(algorithm, version, tableId, categories, true).execute().body();
            matches.forEach(match -> glossaryEntries.put(match.getKeyword(), match.getId()));
        }

        // output the table ids.txt file
        tableIds.sort(String::compareTo);
        Files.write(Paths.get(tableDir + "/ids.txt"), tableIds, StandardCharsets.UTF_8);
        _LOG.info("Saved table IDs to {}/ids.txt", tableDir);

        // output the schemas
        for (String schemaId : schemaIds) {
            StagingSchema schema = staging.schemaById(algorithm, version, schemaId).execute().body();

            String schemaText = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);

            Files.write(Paths.get(schemaDir + "/" + schema.getId() + ".json"), schemaText.getBytes(StandardCharsets.UTF_8));
            _LOG.info("Saved schema: {}", schema.getId());

            // collect the glossary hits
            Set<KeywordMatch> matches = staging.schemaGlossary(algorithm, version, schemaId, categories, true).execute().body();
            matches.forEach(match -> glossaryEntries.put(match.getKeyword(), match.getId()));
        }

        // output the schema ids.txt file
        schemaIds.sort(String::compareTo);
        Files.write(Paths.get(schemaDir + "/ids.txt"), schemaIds, StandardCharsets.UTF_8);
        _LOG.info("Saved schema IDs to {}/ids.txt", schemaDir);

        // build glossary
        String glossaryDir = baseDirectory + "/glossary";
        _LOG.info("Deleting all files from {}...", glossaryDir);
        count = purgeDirectory(new File(glossaryDir));
        _LOG.info("removed {} files", count);

        // write the individual glossary definitions
        GlossaryService glossary = new SeerApi.Builder().connect().glossary();
        for (String glossaryId : glossaryEntries.values()) {
            Glossary entry = glossary.getById("latest", glossaryId).execute().body();

            String glossaryText = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                    new GlossaryDefinition(entry.getName(), entry.getDefinition(), entry.getAlternateName(), entry.getLastModified()));

            Files.write(Paths.get(glossaryDir + "/" + entry.getId() + ".json"), glossaryText.getBytes(StandardCharsets.UTF_8));
            _LOG.info("Saved glossary term: {}", entry.getName());
        }

        // write the keywords.txt file
        List<String> keywords = glossaryEntries.entrySet().stream()
                .sorted(Entry.comparingByKey())
                .map(e -> e.getKey() + "~" + e.getValue())
                .collect(Collectors.toList());
        Files.write(Paths.get(glossaryDir + "/terms.txt"), keywords, StandardCharsets.UTF_8);
        _LOG.info("Saved glossary terms to {}/terms.txt", schemaDir);

        stopwatch.stop();
        _LOG.info("Completed in {}", stopwatch);
    }

    @SuppressWarnings("java:S4042")
    private static int purgeDirectory(File dir) {
        int count = 0;

        if (!dir.exists()) {
            if (!dir.mkdirs())
                throw new IllegalStateException("Unable to create directory: " + dir);
        }
        else {
            File[] files = dir.listFiles();

            if (files != null)
                for (File file : files) {
                    if (!file.isDirectory()) {
                        if (!file.delete())
                            throw new IllegalStateException("Unable to delete " + file.getName());
                        count += 1;
                    }
                }
        }

        return count;
    }

}
