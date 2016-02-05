package com.imsweb.staging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.io.Files;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.web.JSONResource;
import us.monoid.web.Resty;

import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingTable;

/**
 * Updates the staging library data from the API.
 * <p/>
 * 1. Gets a list of schema IDs
 * 2. Gets a list of table IDs
 * 3. Deletes all the existing schemas and tables
 * 4. Gets the tables from the API, initializes them, and saves them
 * 5. Saves the table identifiers into the ids.txt in the tables directory
 * 6. Gets the schemas from the API, initializes them (including the involved tables), and saves them
 * 7. Saves the schema identifiers into the ids.txt in the schemas directory
 */
public final class UpdaterUtils {

    private static final String _BASE_DIRECTORY = "C:/Prj/staging-client-java/src/main/resources/algorithms";

    private static Pattern _ID_CHARACTERS = Pattern.compile("[a-z0-9_]+");

    public static void update(String algorithm, String version) throws IOException, JSONException {
        Stopwatch stopwatch = Stopwatch.createStarted();

        System.out.println("Updating " + algorithm + " version " + version + " from SEER*API");

        // if the config file location was passed in, use that; otherwise load out of the user home directory
        String configuration = System.getProperty("config.file");
        if (configuration == null)
            configuration = System.getProperty("user.home") + "/.seerapi";

        File configFile = new File(configuration);
        if (!configFile.exists())
            throw new IllegalStateException("Configuration file " + configuration + " does not exist.  This file must exist for the system to work properly.");

        FileInputStream in = new FileInputStream(configFile);
        Properties settings = new Properties();
        settings.load(in);
        in.close();

        String url = settings.getProperty("url");
        String apiKey = settings.getProperty("apikey");

        if (url == null || apiKey == null)
            throw new IllegalStateException("URL and API Key must be in the .seerapi file");

        System.out.println("Pulling data from " + url);

        // create an object mapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        mapper.setDateFormat(format);

        Resty r = new Resty();
        r.withHeader("X-SEERAPI-Key", apiKey);
        r.withHeader("Accept", "application/json");

        // first, get a list of unused tables so they can be ignored later
        System.out.println("Getting list of unused table identifiers");
        Set<String> unusedTableIds = new HashSet<>();
        JSONResource tables = r.json(url + "staging/" + algorithm + "/" + version + "/tables?unused=true");
        JSONArray tableArray = tables.array();
        for (int i = 0; i < tableArray.length(); i++)
            unusedTableIds.add(tableArray.getJSONObject(i).get("id").toString());
        System.out.println(unusedTableIds.size() + " unused table identifiers found.");

        System.out.println("Getting list of table identifiers");
        List<String> tableIds = new ArrayList<>();
        tables = r.json(url + "staging/" + algorithm + "/" + version + "/tables");
        tableArray = tables.array();
        for (int i = 0; i < tableArray.length(); i++) {
            String id = tableArray.getJSONObject(i).get("id").toString();

            // if there are invalid table identifiers, just skip them
            if (!_ID_CHARACTERS.matcher(id).matches())
                System.out.println(" **** skipping bad table identifier: '" + id + "' ****");
            else if (unusedTableIds.contains(id))
                System.out.println(" **** skipping unused table identifier: '" + id + "' ****");
            else
                tableIds.add(id);
        }
        System.out.println(tableIds.size() + " valid table identifiers found.");

        System.out.println("Getting list of schema identifiers...");
        List<String> schemaIds = new ArrayList<>();
        JSONResource schemas = r.json(url + "staging/" + algorithm + "/" + version + "/schemas");
        JSONArray schemaArray = schemas.array();
        for (int i = 0; i < schemaArray.length(); i++) {
            String id = schemaArray.getJSONObject(i).get("id").toString();

            // if there are invalid schema identifiers, just skip them
            if (!_ID_CHARACTERS.matcher(id).matches())
                System.out.println(" **** skipping bad schema identifier: '" + id + "' ****");
            else
                schemaIds.add(id);
        }
        System.out.println(schemaIds.size() + " valid schema idenfiers found.");

        String schemaDir = _BASE_DIRECTORY + "/" + algorithm + "/" + version + "/schemas";
        System.out.print("Deleting all files from " + schemaDir + "...");
        int count = purgeDirectory(new File(schemaDir));
        System.out.println("removed " + count + " files");

        String tableDir = _BASE_DIRECTORY + "/" + algorithm + "/" + version + "/tables";
        System.out.print("Deleting all files from " + tableDir + "...");
        count = purgeDirectory(new File(tableDir));
        System.out.println("removed " + count + " files");

        // import the tables
        for (String tableId : tableIds) {
            String tableText = r.text(url + "staging/" + algorithm + "/" + version + "/table/" + tableId).toString();
            StagingTable table = mapper.readValue(tableText, StagingTable.class);

            tableText = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(table);

            Files.write(tableText, new File(tableDir + "/" + table.getId() + ".json"), Charsets.UTF_8);
            System.out.println("Saved table: " + table.getId());
        }

        // output the table ids.txt file
        Files.write(Joiner.on("\n").join(tableIds), new File(tableDir + "/ids.txt"), Charsets.UTF_8);

        // import the schemas
        for (String schemaId : schemaIds) {
            String schemaText = r.text(url + "staging/" + algorithm + "/" + version + "/schema/" + schemaId).toString();
            StagingSchema schema = mapper.readValue(schemaText, StagingSchema.class);

            schemaText = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);

            Files.write(schemaText, new File(schemaDir + "/" + schema.getId() + ".json"), Charsets.UTF_8);
            System.out.println("Saved schema: " + schema.getId());
        }

        // output the table ids.txt file
        Files.write(Joiner.on("\n").join(schemaIds), new File(schemaDir + "/ids.txt"), Charsets.UTF_8);

        stopwatch.stop();
        System.out.println("Completed in " + stopwatch);
    }

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
