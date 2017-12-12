package com.imsweb.staging.lab;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.imsweb.staging.Staging;
import com.imsweb.staging.cs.CsDataProvider;
import com.imsweb.staging.cs.CsDataProvider.CsVersion;
import com.imsweb.staging.entities.StagingKeyValue;
import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingSchemaOutput;

public class CsStagingAddOutput {

    private static final String _ALGORITHM = "cs";
    private static final String _VERSION = "02.05.50";
    private static final String _BASE_DIRECTORY = "C:/Prj/staging-client-java/src/main/resources/algorithms";

    public static void main(String[] args) throws IOException, URISyntaxException {
        // create an object mapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        mapper.setDateFormat(format);

        Staging staging = Staging.getInstance(CsDataProvider.getInstance(CsVersion.v020550));

        // hold all schema in a map
        Map<String, StagingSchema> schemaMap = new HashMap<>();
        for (String schemaId : staging.getSchemaIds()) {
            StagingSchema schema = staging.getSchema(schemaId);

            List<StagingSchemaOutput> outputs = new ArrayList<>();

            StagingSchemaOutput output = new StagingSchemaOutput("schema_number", "Schema Number");
            for (StagingKeyValue value : schema.getInitialContext())
                if ("schema_number".equals(value.getKey()))
                    output.setDefault(value.getValue());
            if (output.getDefault() == null)
                throw new IllegalStateException("Unable to find schema number in context");
            outputs.add(output);

            output = new StagingSchemaOutput("csver_derived", "CS Version Derived");
            output.setDefault("020550");
            outputs.add(output);

            outputs.add(new StagingSchemaOutput("ajcc6_t", "AJCC6 T"));
            outputs.add(new StagingSchemaOutput("ajcc6_tdescriptor", "AJCC6 T Descriptor"));
            outputs.add(new StagingSchemaOutput("ajcc6_n", "AJCC6 N"));
            outputs.add(new StagingSchemaOutput("ajcc6_ndescriptor", "AJCC6 N Descriptor"));
            outputs.add(new StagingSchemaOutput("ajcc6_m", "AJCC6 M"));
            outputs.add(new StagingSchemaOutput("ajcc6_mdescriptor", "AJCC6 M Descriptor"));
            outputs.add(new StagingSchemaOutput("ajcc6_stage", "AJCC6 Stage Group"));
            outputs.add(new StagingSchemaOutput("ajcc7_t", "AJCC7 T"));
            outputs.add(new StagingSchemaOutput("ajcc7_tdescriptor", "AJCC7 T Descriptor"));
            outputs.add(new StagingSchemaOutput("ajcc7_n", "AJCC7 N"));
            outputs.add(new StagingSchemaOutput("ajcc7_ndescriptor", "AJCC7 N Descriptor"));
            outputs.add(new StagingSchemaOutput("ajcc7_m", "AJCC7 M"));
            outputs.add(new StagingSchemaOutput("ajcc7_mdescriptor", "AJCC7 M Descriptor"));
            outputs.add(new StagingSchemaOutput("ajcc7_stage", "AJCC7 Stage Group"));
            outputs.add(new StagingSchemaOutput("t77", "Summary Stage T 1977"));
            outputs.add(new StagingSchemaOutput("n77", "Summary Stage N 1977"));
            outputs.add(new StagingSchemaOutput("m77", "Summary Stage M 1977"));
            outputs.add(new StagingSchemaOutput("ss77", "Summary Stage Group 1977"));
            outputs.add(new StagingSchemaOutput("t2000", "Summary Stage T 2000"));
            outputs.add(new StagingSchemaOutput("n2000", "Summary Stage N 2000"));
            outputs.add(new StagingSchemaOutput("m2000", "Summary Stage M 2000"));
            outputs.add(new StagingSchemaOutput("ss2000", "Summary Stage Group 2000"));
            outputs.add(new StagingSchemaOutput("stor_ajcc6_t", "AJCC6 T (Storage)"));
            outputs.add(new StagingSchemaOutput("stor_ajcc6_tdescriptor", "AJCC6 T Descriptor (Storage)"));
            outputs.add(new StagingSchemaOutput("stor_ajcc6_n", "AJCC6 N (Storage)"));
            outputs.add(new StagingSchemaOutput("stor_ajcc6_ndescriptor", "AJCC6 N Descriptor (Storage)"));
            outputs.add(new StagingSchemaOutput("stor_ajcc6_m", "AJCC6 M (Storage)"));
            outputs.add(new StagingSchemaOutput("stor_ajcc6_mdescriptor", "AJCC6 M Descriptor (Storage)"));
            outputs.add(new StagingSchemaOutput("stor_ajcc6_stage", "AJCC6 Stage Group (Storage)"));
            outputs.add(new StagingSchemaOutput("stor_ajcc7_t", "AJCC7 T (Storage)"));
            outputs.add(new StagingSchemaOutput("stor_ajcc7_tdescriptor", "AJCC7 T Descriptor (Storage)"));
            outputs.add(new StagingSchemaOutput("stor_ajcc7_n", "AJCC7 N (Storage)"));
            outputs.add(new StagingSchemaOutput("stor_ajcc7_ndescriptor", "AJCC7 N Descriptor (Storage)"));
            outputs.add(new StagingSchemaOutput("stor_ajcc7_m", "AJCC7 M (Storage)"));
            outputs.add(new StagingSchemaOutput("stor_ajcc7_mdescriptor", "AJCC7 M Descriptor (Storage)"));
            outputs.add(new StagingSchemaOutput("stor_ajcc7_stage", "AJCC7 Stage Group (Storage)"));
            outputs.add(new StagingSchemaOutput("stor_ss77", "Summary Stage Group 1977 (Storage)"));
            outputs.add(new StagingSchemaOutput("stor_ss2000", "Summary Stage Group 2000 (Storage)"));

            schema.setOutputs(outputs);

            // defaults are now in the "outputs"; initial context is not needed anymore
            schema.setInitialContext(null);

            schemaMap.put(schemaId, schema);
        }

        // loop over all schemas
        String schemaDir = _BASE_DIRECTORY + "/" + _ALGORITHM + "/" + _VERSION + "/schemas";
        for (Entry<String, StagingSchema> entry : schemaMap.entrySet()) {
            StagingSchema schema = entry.getValue();

            // save the schema back out
            String schemaText = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);

            File schemaFile = new File(schemaDir + "/" + schema.getId() + ".json");
            if (!schemaFile.delete())
                throw new IllegalStateException("Unable to delete file " + schemaFile.getName());
            Files.write(Paths.get(schemaFile.toURI()), Collections.singleton(schemaText), StandardCharsets.UTF_8);

            System.out.println("Saved: " + schema.getId());
        }

        System.out.println("Complete.");
    }
}