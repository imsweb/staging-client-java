package com.imsweb.staging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

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

    public static void update(String algorithm, String version) throws IOException {
        long start = System.currentTimeMillis();

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
        final String apiKey = settings.getProperty("apikey");

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

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();

                    // add the api key to all requests
                    Request request = original.newBuilder()
                            .header("Accept", "application/json")
                            .header("X-SEERAPI-Key", apiKey)
                            .method(original.method(), original.body())
                            .build();

                    return chain.proceed(request);
                })
                .addInterceptor(new ErrorInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .client(client)
                .build();

        StagingService staging = retrofit.create(StagingService.class);

        // first, get a list of unused tables so they can be ignored later
        System.out.println("Getting list of unused table identifiers");
        Set<String> unusedTableIds = new HashSet<>();
        for (StagingTable table : staging.getTables(algorithm, version, true).execute().body())
            unusedTableIds.add(table.getId());
        System.out.println(unusedTableIds.size() + " unused table identifiers found.");

        System.out.println("Getting list of table identifiers");
        List<String> tableIds = new ArrayList<>();
        for (StagingTable table : staging.getTables(algorithm, version, null).execute().body()) {
            String id = table.getId();

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
        for (StagingSchema schema : staging.getSchemas(algorithm, version).execute().body()) {
            String id = schema.getId();

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
            StagingTable table = staging.getTable(algorithm, version, tableId).execute().body();
            String tableText = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(table);

            Files.write(Paths.get(tableDir + "/" + table.getId() + ".json"), tableText.getBytes(StandardCharsets.UTF_8));
            System.out.println("Saved table: " + table.getId());
        }

        // output the table ids.txt file
        Files.write(Paths.get(tableDir + "/ids.txt"), tableIds, StandardCharsets.UTF_8);

        // import the schemas
        for (String schemaId : schemaIds) {
            StagingSchema schema = staging.getSchema(algorithm, version, schemaId).execute().body();

            String schemaText = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);

            Files.write(Paths.get(schemaDir + "/" + schema.getId() + ".json"), schemaText.getBytes(StandardCharsets.UTF_8));
            System.out.println("Saved schema: " + schema.getId());
        }

        // output the table ids.txt file
        Files.write(Paths.get(schemaDir + "/ids.txt"), schemaIds, StandardCharsets.UTF_8);

        System.out.println("Completed in " + (System.currentTimeMillis() - start) + "ms");
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
