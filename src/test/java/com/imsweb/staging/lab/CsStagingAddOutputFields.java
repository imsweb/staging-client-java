package com.imsweb.staging.lab;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import us.monoid.json.JSONException;

import com.imsweb.staging.Staging;
import com.imsweb.staging.cs.CsDataProvider;
import com.imsweb.staging.cs.CsDataProvider.CsVersion;
import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingSchemaInput;
import com.imsweb.staging.entities.StagingSchemaOutput;

public class CsStagingAddOutputFields {

    private static final String _ALGORITHM = "cs";
    private static final String _VERSION = "02.05.50";
    private static final String _BASE_DIRECTORY = "C:/Prj/staging-client-java/src/main/resources/algorithms";

    public static void main(String[] args) throws IOException, JSONException, URISyntaxException {
        // create an object mapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        mapper.setDateFormat(format);

        Map<String, Integer> codes = new HashMap<>();
        codes.put("csver_derived", 2936);
        codes.put("stor_ajcc6_t", 2940);
        codes.put("stor_ajcc6_tdescriptor", 2950);
        codes.put("stor_ajcc6_n", 2960);
        codes.put("stor_ajcc6_ndescriptor", 2970);
        codes.put("stor_ajcc6_m", 2980);
        codes.put("stor_ajcc6_mdescriptor", 2990);
        codes.put("stor_ajcc6_stage", 3000);
        codes.put("stor_ajcc7_t", 3400);
        codes.put("stor_ajcc7_tdescriptor", 3402);
        codes.put("stor_ajcc7_n", 3410);
        codes.put("stor_ajcc7_ndescriptor", 3412);
        codes.put("stor_ajcc7_m", 3420);
        codes.put("stor_ajcc7_mdescriptor", 3422);
        codes.put("stor_ajcc7_stage", 3430);
        codes.put("stor_ss77", 3010);
        codes.put("stor_ss2000", 3020);

        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("site", "Code for the primary site of the tumor being reported using either ICD-O-2 or ICD-O-3.");
        descriptions.put("hist", "Codes for the histologic type of the tumor being reported using ICD-O-3.");
        descriptions.put("year_dx", "Date of initial diagnosis by a recognized medical practitioner for the tumor being reported whether clinically or microscopically confirmed.");
        descriptions.put("cs_input_version_original", "Collaborative Staging (CS) version number initially used to code CS fields.");
        descriptions.put("behavior", "Code for the behavior of the tumor being reported using ICD-O-3.");
        descriptions.put("grade", "Code for the grade or degree of differentiation of the reportable tumor.");
        descriptions.put("age_dx", "Age of the patient at diagnosis in complete years.");
        descriptions.put("lvi", "Indicates whether lymphatic duct or blood vessel (LVI) is identified in the pathology report.");
        descriptions.put("size", "Records the largest dimension or diameter of the primary tumor in millimeters.");
        descriptions.put("extension",
                "Identifies contiguous growth (extension) of the primary tumor within the organ of origin or its direct extension into neighboring organs. For certain sites such as ovary, discontinuous metastasis is coded in CS Extension.");
        descriptions.put("extension_eval", "Records how the codes for the two items CS Tumor Size and CS Extension were determined, based on the diagnostic methods employed.");
        descriptions.put("nodes", "Identifies the regional lymph nodes involved with cancer at the time of diagnosis.");
        descriptions.put("nodes_eval", "Records how the code for CS Lymph Nodes was determined, based on the diagnosticmethods employed.");
        descriptions.put("nodes_pos", "Records the exact number of regional nodes examined by the pathologist and found to contain metastases. ");
        descriptions.put("nodes_exam", "Records the total number of regional lymph nodes that were removed and examined by the pathologist. ");
        descriptions.put("mets", "Identifies the distant site(s) of metastatic involvement at time of diagnosis.");
        descriptions.put("mets_eval", "Records how the code for CS Mets at Dx was determined based on the diagnostic methods employed.");
        descriptions.put("schema_number", "CS assigned schema number.");
        descriptions.put("csver_derived", "Collaborative Staging (CS) version used to derive the CS output fields.");
        descriptions.put("ajcc6_t", "");
        descriptions.put("ajcc6_tdescriptor", "");
        descriptions.put("ajcc6_n", "");
        descriptions.put("ajcc6_ndescriptor", "");
        descriptions.put("ajcc6_m", "");
        descriptions.put("ajcc6_mdescriptor", "");
        descriptions.put("ajcc7_stage", "");
        descriptions.put("ajcc7_t", "");
        descriptions.put("ajcc7_tdescriptor", "");
        descriptions.put("ajcc7_n", "");
        descriptions.put("ajcc7_ndescriptor", "");
        descriptions.put("ajcc7_m", "");
        descriptions.put("ajcc7_mdescriptor", "");
        descriptions.put("ajcc7_stage", "");
        descriptions.put("t77", "");
        descriptions.put("n77", "");
        descriptions.put("m77", "");
        descriptions.put("t2000", "");
        descriptions.put("n2000", "");
        descriptions.put("m2000", "");
        descriptions.put("ss2000", "");
        descriptions.put("stor_ajcc6_t", "");
        descriptions.put("stor_ajcc6_tdescriptor", "");
        descriptions.put("stor_ajcc6_n", "");
        descriptions.put("stor_ajcc6_ndescriptor", "");
        descriptions.put("stor_ajcc6_m", "");
        descriptions.put("stor_ajcc6_mdescriptor", "");
        descriptions.put("stor_ajcc6_stage", "");
        descriptions.put("stor_ajcc7_t", "");
        descriptions.put("stor_ajcc7_tdescriptor", "");
        descriptions.put("stor_ajcc7_n", "");
        descriptions.put("stor_ajcc7_ndescriptor", "");
        descriptions.put("stor_ajcc7_m", "");
        descriptions.put("stor_ajcc7_mdescriptor", "");
        descriptions.put("stor_ajcc7_stage", "");
        descriptions.put("stor_ss77", "");
        descriptions.put("stor_ss2000", "");

        Staging staging = Staging.getInstance(CsDataProvider.getInstance(CsVersion.v020550));

        // hold all schema in a map
        Map<String, StagingSchema> schemaMap = new HashMap<>();
        for (String schemaId : staging.getSchemaIds()) {
            StagingSchema schema = staging.getSchema(schemaId);

            // update input descriptions
            for (StagingSchemaInput input : schema.getInputs())
                if (descriptions.containsKey(input.getKey())) {
                    String desc = descriptions.get(input.getKey());
                    if (desc != null && !desc.isEmpty())
                        input.setDescription(desc);
                }

            // update output descriptions and NAACCR item numbers
            for (StagingSchemaOutput output : schema.getOutputs()) {
                if (codes.containsKey(output.getKey()))
                    output.setNaaccrItem(codes.get(output.getKey()));
                if (descriptions.containsKey(output.getKey())) {
                    String desc = descriptions.get(output.getKey());
                    if (desc != null && !desc.isEmpty())
                        output.setDescription(desc);
                }
            }

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
            Files.write(schemaText, schemaFile, Charsets.UTF_8);
            System.out.println("Saved: " + schema.getId());
        }

        System.out.println("Complete.");
    }
}