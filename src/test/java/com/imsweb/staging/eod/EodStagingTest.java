/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.eod;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import com.imsweb.staging.ExternalStagingFileDataProvider;
import com.imsweb.staging.Staging;
import com.imsweb.staging.StagingDataProvider;
import com.imsweb.staging.entities.ColumnDefinition;
import com.imsweb.staging.entities.GlossaryDefinition;
import com.imsweb.staging.entities.Input;
import com.imsweb.staging.entities.Mapping;
import com.imsweb.staging.entities.Output;
import com.imsweb.staging.entities.Schema;
import com.imsweb.staging.entities.SchemaLookup;
import com.imsweb.staging.entities.Table;

import static com.imsweb.staging.entities.StagingData.HISTOLOGY_KEY;
import static com.imsweb.staging.entities.StagingData.PRIMARY_SITE_KEY;
import static com.imsweb.staging.eod.EodStagingData.EodInput;
import static com.imsweb.staging.eod.EodStagingData.EodOutput;
import static com.imsweb.staging.eod.EodStagingData.EodStagingInputBuilder;
import static com.imsweb.staging.eod.EodStagingData.Result;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Full test of a production algorithm loaded externally from a zip file
 */
public class EodStagingTest {

    protected static StagingDataProvider _PROVIDER;
    protected static Staging _STAGING;

    @BeforeClass
    public static void init() throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("eod_2.0.zip");

        _PROVIDER = new ExternalStagingFileDataProvider(is);
        _STAGING = Staging.getInstance(_PROVIDER);
    }

    /**
     * Helper method to assert failures when tracked errors exist
     */
    private void assertNoErrors(Collection<String> errors, String description) {
        if (!errors.isEmpty()) {
            System.out.println("There were " + errors.size() + " issues with " + description + ".");
            errors.forEach(System.out::println);
            fail("There were " + errors.size() + " issues with " + description + ".");
        }
    }

    @Test
    public void testInitialization() {
        assertThat(_STAGING.getAlgorithm()).isEqualTo("eod_public");
        assertThat(_STAGING.getVersion()).isEqualTo("2.0");
    }

    @Test
    public void testInitAllTables() {
        for (String id : _STAGING.getTableIds()) {
            Table table = _STAGING.getTable(id);

            assertThat(table).isNotNull();
            assertThat(table.getAlgorithm()).isNotNull();
            assertThat(table.getVersion()).isNotNull();
            assertThat(table.getName()).isNotNull();
        }
    }

    @Test
    public void testValidCode() {
        Map<String, String> context = new HashMap<>();
        context.put("hist", "7000");
        assertThat(_STAGING.isContextValid("prostate", "hist", context)).isFalse();
        context.put("hist", "8000");
        assertThat(_STAGING.isContextValid("prostate", "hist", context)).isTrue();
        context.put("hist", "8542");
        assertThat(_STAGING.isContextValid("prostate", "hist", context)).isTrue();

        // make sure null is handled
        context.put("hist", null);
        assertThat(_STAGING.isContextValid("prostate", "hist", context)).isFalse();

        // make sure blank is handled
        context.put("hist", "");
        assertThat(_STAGING.isContextValid("prostate", "hist", context)).isFalse();
    }

    @Test
    public void testBasicInputs() {
        // all inputs for all schemas will have null unit and decimal places
        for (String id : _STAGING.getSchemaIds()) {
            Schema schema = _STAGING.getSchema(id);
            for (Input input : schema.getInputs()) {
                assertThat(input.getUnit()).as("No schemas should have units").isNull();
                assertThat(input.getDecimalPlaces()).as("No schemas should have decimal places").isNull();
            }
        }
    }

    @Test
    public void testValidSite() {
        assertThat(_STAGING.isValidSite(null)).isFalse();
        assertThat(_STAGING.isValidSite("")).isFalse();
        assertThat(_STAGING.isValidSite("C21")).isFalse();
        assertThat(_STAGING.isValidSite("C115")).isFalse();

        assertThat(_STAGING.isValidSite("C509")).isTrue();
    }

    @Test
    public void testValidHistology() {
        assertThat(_STAGING.isValidHistology(null)).isFalse();
        assertThat(_STAGING.isValidHistology("")).isFalse();
        assertThat(_STAGING.isValidHistology("810")).isFalse();
        assertThat(_STAGING.isValidHistology("8176")).isFalse();

        assertThat(_STAGING.isValidHistology("8000")).isTrue();
        assertThat(_STAGING.isValidHistology("8201")).isTrue();
    }

    @Test
    public void testAllowedFields() {
        Set<String> descriminators = new HashSet<>();
        descriminators.add(PRIMARY_SITE_KEY);
        descriminators.add(HISTOLOGY_KEY);

        for (String id : _STAGING.getSchemaIds()) {
            Schema schema = _STAGING.getSchema(id);
            if (schema.getSchemaDiscriminators() != null)
                descriminators.addAll(schema.getSchemaDiscriminators());
        }

        assertThat(descriminators).containsExactlyInAnyOrder(
                PRIMARY_SITE_KEY,
                HISTOLOGY_KEY,
                "sex",
                "behavior",
                "year_dx",
                "discriminator_1",
                "discriminator_2");
    }

    @Test
    public void testGetTable() {
        assertThat(_STAGING.getTable("bad_table_name")).isNull();
    }

    @Test
    public void testCachedSiteAndHistology() {
        assertThat(_PROVIDER.getValidSites().size() > 0).isTrue();
        assertThat(_PROVIDER.getValidHistologies().size() > 0).isTrue();

        // site tests
        List<String> validSites = Arrays.asList("C000", "C809");
        List<String> invalidSites = Arrays.asList("C727", "C810");
        for (String site : validSites)
            assertThat(_PROVIDER.getValidSites()).contains(site);
        for (String site : invalidSites)
            assertThat(_PROVIDER.getValidSites()).doesNotContain(site);

        // hist tests
        List<String> validHist = Arrays.asList("8000", "8002", "8005", "8290", "9992", "9993");
        List<String> invalidHist = Arrays.asList("8006", "8444");
        for (String hist : validHist)
            assertThat(_PROVIDER.getValidHistologies())
                    .withFailMessage("The histology '" + hist + "' is not in the valid histology list")
                    .contains(hist);
        for (String hist : invalidHist)
            assertThat(_PROVIDER.getValidHistologies())
                    .withFailMessage("The histology '" + hist + "' is not supposed to be in the valid histology list")
                    .doesNotContain(hist);
    }

    @Test
    public void testForUnusedTables() {
        Set<String> usedTables = new HashSet<>();
        for (String id : _STAGING.getSchemaIds())
            usedTables.addAll(_STAGING.getSchema(id).getInvolvedTables());

        Set<String> unusedTables = _STAGING.getTableIds().stream().filter(id -> !usedTables.contains(id)).collect(Collectors.toSet());

        if (!unusedTables.isEmpty())
            fail("There are " + unusedTables.size() + " tables that are not used in any schema: " + unusedTables);
    }

    @Test
    public void testInputTables() {
        Set<String> errors = new HashSet<>();

        for (String schemaId : _STAGING.getSchemaIds()) {
            Schema schema = _STAGING.getSchema(schemaId);

            // build a list of input tables that should be excluded
            for (Input input : schema.getInputs()) {
                if (input.getTable() != null) {
                    Set<String> inputKeys = new HashSet<>();
                    Table table = _STAGING.getTable(input.getTable());
                    for (ColumnDefinition def : table.getColumnDefinitions())
                        if (ColumnDefinition.ColumnType.INPUT.equals(def.getType()))
                            inputKeys.add(def.getKey());

                    // make sure the input key matches the an input column
                    if (!inputKeys.contains(input.getKey()))
                        errors.add("Input key " + schemaId + ":" + input.getKey() + " does not match validation table " + table.getId() + ": " + inputKeys);
                }
            }
        }

        assertNoErrors(errors, "input values and their assocated validation tables");
    }

    @Test
    public void verifyInputs() {
        List<String> errors = new ArrayList<>();

        for (String id : _STAGING.getSchemaIds()) {
            Schema schema = _STAGING.getSchema(id);

            // loop over all the inputs returned by processing the schema and make sure they are all part of the main list of inputs on the schema
            for (String input : _STAGING.getInputs(schema))
                if (!schema.getInputMap().containsKey(input))
                    errors.add("Error processing schema " + schema.getId() + ": Table input '" + input + "' not in master list of inputs");
        }

        assertNoErrors(errors, "input values");
    }

    @Test
    public void testMappingIdUniqueness() {
        Set<String> errors = new HashSet<>();

        for (String schemaId : _STAGING.getSchemaIds()) {
            Schema schema = _STAGING.getSchema(schemaId);

            // build a list of input tables that should be excluded
            Set<String> ids = new HashSet<>();

            if (schema.getMappings() != null)
                for (Mapping mapping : schema.getMappings()) {
                    if (ids.contains(mapping.getId()))
                        errors.add("The mapping id " + schemaId + ":" + mapping.getId() + " is duplicated.  This should never happen");
                    ids.add(mapping.getId());
                }
        }

        assertNoErrors(errors, "input values and their assocated validation tables");
    }

    @Test
    public void testBehaviorDescriminator() {
        // test valid combination that requires discriminator and a good discriminator is supplied
        SchemaLookup lookup = new SchemaLookup("C717", "9591");
        List<Schema> lookups = _STAGING.lookupSchema(lookup);
        assertThat(lookups).hasSize(3);
        lookup.setInput("discriminator_1", "1");
        lookup.setInput("behavior", "3");
        lookups = _STAGING.lookupSchema(lookup);
        assertThat(lookups).hasSize(1);
        assertThat(lookups.get(0).getId()).isEqualTo("hemeretic");
    }

    @Test
    public void testStagingEnums() {
        Set<String> enumInput = Arrays.stream(EodInput.values()).map(EodInput::toString).collect(Collectors.toSet());
        Set<String> enumOutput = Arrays.stream(EodOutput.values()).map(EodOutput::toString).collect(Collectors.toSet());

        // collect all input and output fields from all schemas
        Set<String> schemaInput = new HashSet<>();
        Set<String> schemaOutput = new HashSet<>();
        for (String schemaId : _STAGING.getSchemaIds()) {
            Schema schema = _STAGING.getSchema(schemaId);

            schemaInput.addAll(_STAGING.getInputs(schema));
            schemaOutput.addAll(_STAGING.getOutputs(schema));
        }

        assertThat(schemaInput).hasSameElementsAs(enumInput);
        assertThat(schemaOutput).hasSameElementsAs(enumOutput);
    }

    @Test
    public void testNaaccrXmlIds() {
        List<String> errors = new ArrayList<>();

        Map<String, Set<String>> inputMappings = new HashMap<>();
        Map<String, Set<String>> outputMappings = new HashMap<>();
        for (String schemaId : _STAGING.getSchemaIds()) {
            Schema schema = _STAGING.getSchema(schemaId);

            for (Input input : schema.getInputs()) {
                if (input.getNaaccrItem() != null && input.getNaaccrXmlId() == null)
                    errors.add("Schema input " + schema.getId() + "." + input.getKey() + " has a NAACCR number but is missing NAACCR XML ID");

                if (input.getNaaccrXmlId() != null) {
                    if (inputMappings.containsKey(input.getNaaccrXmlId()))
                        inputMappings.get(input.getNaaccrXmlId()).add(input.getKey());
                    else
                        inputMappings.put(input.getNaaccrXmlId(), new HashSet<>(Collections.singletonList(input.getKey())));
                }
            }

            for (Output output : schema.getOutputs()) {
                if (output.getNaaccrItem() != null && output.getNaaccrXmlId() == null)
                    errors.add("Schema output " + schema.getId() + "." + output.getKey() + " has a NAACCR number but is missing NAACCR XML ID");

                if (output.getNaaccrXmlId() != null) {
                    if (outputMappings.containsKey(output.getNaaccrXmlId()))
                        outputMappings.get(output.getNaaccrXmlId()).add(output.getKey());
                    else
                        outputMappings.put(output.getNaaccrXmlId(), new HashSet<>(Collections.singletonList(output.getKey())));
                }
            }
        }

        // verify that if a field has a given NAACCR XML ID, then all fields with that same XML ID have the same key.
        inputMappings.forEach((k, v) -> {
            if (v.size() > 1)
                errors.add("NAACCR XML Id " + k + " is listed for multiple inputs: " + v);
        });
        outputMappings.forEach((k, v) -> {
            if (v.size() > 1)
                errors.add("NAACCR XML Id " + k + " is listed for multiple outputs: " + v);
        });

        assertThat(errors).overridingErrorMessage(() -> "\n" + String.join("\n", errors)).isEmpty();
    }

    @Test
    public void testMisspelledProperty() {
        EodStagingData data = new EodStagingData();
        data.setInput(EodInput.DX_YEAR, "2020");
        data.setInput(EodInput.PRIMARY_SITE, "C180");
        data.setInput(EodInput.HISTOLOGY, "8000");
        data.setInput(EodInput.NODES_POS, "90");
        data.setInput(EodInput.EOD_PRIMARY_TUMOR, "700");
        data.setInput(EodInput.EOD_REGIONAL_NODES, "300");
        data.setInput(EodInput.EOD_METS, "10");

        // perform the staging
        _STAGING.stage(data);

        assertThat(data.getResult()).isEqualTo(Result.STAGED);
        assertThat(data.getSchemaId()).isEqualTo("colon_rectum");
        assertThat(data.getErrors()).isEmpty();
        assertThat(data.getPath()).hasSize(11);

        // before the bug fix, AJCC_VERSION_NUMBER was returning an empty string
        assertThat(data.getOutput(EodOutput.AJCC_VERSION_NUMBER)).isEqualTo("08");

        // check other output
        assertThat(data.getOutput(EodOutput.NAACCR_SCHEMA_ID)).isEqualTo("00200");
        assertThat(data.getOutput(EodOutput.EOD_2018_STAGE_GROUP)).isEqualTo("4A");
        assertThat(data.getOutput(EodOutput.DERIVED_VERSION)).isEqualTo("2.0");
        assertThat(data.getOutput(EodOutput.SS_2018_DERIVED)).isEqualTo("7");
        assertThat(data.getOutput(EodOutput.EOD_2018_T)).isEqualTo("T4b");
        assertThat(data.getOutput(EodOutput.EOD_2018_N)).isEqualTo("N2b");
        assertThat(data.getOutput(EodOutput.EOD_2018_M)).isEqualTo("M1a");
        assertThat(data.getOutput(EodOutput.AJCC_ID)).isEqualTo("20");
    }

    @Test
    public void testBasicInitialization() {
        assertThat(_STAGING.getSchemaIds()).hasSize(119);
        assertThat(_STAGING.getTableIds()).isNotEmpty();

        assertThat(_STAGING.getSchema("urethra")).isNotNull();
        assertThat(_STAGING.getTable("ss2018_urethra_14363")).isNotNull();
    }

    @Test
    public void testDescriminatorKeys() {
        assertThat(_STAGING.getSchema("nasopharynx").getSchemaDiscriminators()).containsOnly("discriminator_1");
        assertThat(_STAGING.getSchema("oropharynx_p16_neg").getSchemaDiscriminators()).containsOnly("discriminator_1", "discriminator_2");
    }

    @Test
    public void testSchemaSelection() {
        // test bad values
        List<Schema> lookup = _STAGING.lookupSchema(new SchemaLookup());
        assertThat(lookup).isEmpty();

        lookup = _STAGING.lookupSchema(new SchemaLookup("XXX", "YYY"));
        assertThat(lookup).isEmpty();

        // test valid combinations that do not require a discriminator
        SchemaLookup schemaLookup = new SchemaLookup("C629", "9231");
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), "");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).hasSize(1);
        assertThat(lookup.get(0).getId()).isEqualTo("soft_tissue_other");
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), null);
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).hasSize(1);
        assertThat(lookup.get(0).getId()).isEqualTo("soft_tissue_other");

        // test valid combination that requires a discriminator but is not supplied one
        lookup = _STAGING.lookupSchema(new EodSchemaLookup("C111", "8200"));
        assertThat(lookup).hasSize(3);
        assertThat(lookup.stream().map(Schema::getId).collect(Collectors.toSet())).isEqualTo(
                new HashSet<>(Arrays.asList("oropharynx_hpv_mediated_p16_pos", "nasopharynx", "oropharynx_p16_neg")));
        assertThat(lookup.stream().flatMap(d -> d.getSchemaDiscriminators().stream()).collect(Collectors.toSet())).isEqualTo(new HashSet<>(Arrays.asList("discriminator_1", "discriminator_2")));

        // test valid combination that requires discriminator and a good discriminator is supplied
        schemaLookup = new EodSchemaLookup("C111", "8200");
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), "1");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).hasSize(1);
        assertThat(lookup.stream().flatMap(d -> d.getSchemaDiscriminators().stream()).collect(Collectors.toSet())).isEqualTo(new HashSet<>(Collections.singletonList("discriminator_1")));
        assertThat(lookup.get(0).getId()).isEqualTo("nasopharynx");

        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), "2");
        schemaLookup.setInput(EodInput.DISCRIMINATOR_2.toString(), "1");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).hasSize(1);
        assertThat(lookup.stream().flatMap(d -> d.getSchemaDiscriminators().stream()).collect(Collectors.toSet())).isEqualTo(new HashSet<>(Arrays.asList("discriminator_1", "discriminator_2")));
        assertThat(lookup.get(0).getId()).isEqualTo("oropharynx_p16_neg");

        // test valid combination that requires a discriminator but is supplied a bad disciminator value
        schemaLookup = new EodSchemaLookup("C111", "8200");
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), "X");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).isEmpty();

        // test searching on only site
        lookup = _STAGING.lookupSchema(new EodSchemaLookup("C401", null));
        assertThat(lookup).hasSize(8);

        // test searching on only hist
        lookup = _STAGING.lookupSchema(new EodSchemaLookup(null, "9702"));
        assertThat(lookup).hasSize(5);

        // test that searching on only discriminator_1 returns no results
        schemaLookup = new EodSchemaLookup(null, null);
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), "1");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).isEmpty();
        schemaLookup = new EodSchemaLookup("", null);
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), "1");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).isEmpty();
        schemaLookup = new EodSchemaLookup(null, "");
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), "1");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).isEmpty();

        // test lookups based on sex
        schemaLookup = new EodSchemaLookup("C481", "8720");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).hasSize(2);
        schemaLookup.setInput(EodInput.SEX.toString(), "1");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).hasSize(1);
        assertThat(lookup.get(0).getId()).isEqualTo("retroperitoneum");
    }

    @Test
    public void testDiscriminatorInputs() {
        Set<String> discriminators = new HashSet<>();
        _STAGING.getSchemaIds().stream()
                .map(schemaId -> _STAGING.getSchema(schemaId))
                .map(Schema::getSchemaDiscriminators)
                .filter(Objects::nonNull)
                .forEach(discriminators::addAll);

        assertThat(discriminators).containsOnly("year_dx", "sex", "behavior", "discriminator_1", "discriminator_2");
    }

    @Test
    public void testLookupCache() {
        // do the same lookup twice
        List<Schema> lookup = _STAGING.lookupSchema(new EodSchemaLookup("C629", "9231"));
        assertThat(lookup).hasSize(1);
        assertThat(lookup.get(0).getId()).isEqualTo("soft_tissue_other");

        lookup = _STAGING.lookupSchema(new EodSchemaLookup("C629", "9231"));
        assertThat(lookup).hasSize(1);
        assertThat(lookup.get(0).getId()).isEqualTo("soft_tissue_other");

        // now invalidate the cache
        _PROVIDER.invalidateCache();

        // try the lookup again
        lookup = _STAGING.lookupSchema(new EodSchemaLookup("C629", "9231"));
        assertThat(lookup).hasSize(1);
        assertThat(lookup.get(0).getId()).isEqualTo("soft_tissue_other");
    }

    @Test
    public void testFindTableRow() {
        assertThat(_STAGING.findMatchingTableRow("tumor_size_clinical_60979", "size_clin", "00X")).isNull();

        // null maps to blank
        assertThat(_STAGING.findMatchingTableRow("tumor_size_clinical_60979", "size_clin", "000")).isEqualTo(Integer.valueOf(0));
        assertThat(_STAGING.findMatchingTableRow("tumor_size_clinical_60979", "size_clin", "002")).isEqualTo(Integer.valueOf(2));
        assertThat(_STAGING.findMatchingTableRow("tumor_size_clinical_60979", "size_clin", "100")).isEqualTo(Integer.valueOf(2));
        assertThat(_STAGING.findMatchingTableRow("tumor_size_clinical_60979", "size_clin", "988")).isEqualTo(Integer.valueOf(2));
        assertThat(_STAGING.findMatchingTableRow("tumor_size_clinical_60979", "size_clin", "999")).isEqualTo(Integer.valueOf(5));
    }

    @Test
    public void testStageUrethra() {
        EodStagingData data = new EodStagingInputBuilder()
                .withInput(EodInput.PRIMARY_SITE, "C250")
                .withInput(EodInput.HISTOLOGY, "8154")
                .withInput(EodInput.DX_YEAR, "2018")
                .withInput(EodInput.TUMOR_SIZE_SUMMARY, "004")
                .withInput(EodInput.NODES_POS, "03")
                .withInput(EodInput.EOD_PRIMARY_TUMOR, "500")
                .withInput(EodInput.EOD_REGIONAL_NODES, "300")
                .withInput(EodInput.EOD_METS, "10").build();

        // perform the staging
        _STAGING.stage(data);

        assertThat(data.getResult()).isEqualTo(Result.STAGED);
        assertThat(data.getSchemaId()).isEqualTo("pancreas");
        assertThat(data.getErrors()).isEmpty();
        assertThat(data.getPath()).hasSize(12);
        assertThat(data.getOutput()).hasSize(9);

        // check outputs
        assertThat(data.getOutput(EodOutput.DERIVED_VERSION)).isEqualTo("2.0");
        assertThat(data.getOutput(EodOutput.SS_2018_DERIVED)).isEqualTo("7");
        assertThat(data.getOutput(EodOutput.NAACCR_SCHEMA_ID)).isEqualTo("00280");
        assertThat(data.getOutput(EodOutput.EOD_2018_STAGE_GROUP)).isEqualTo("4");
        assertThat(data.getOutput(EodOutput.EOD_2018_T)).isEqualTo("T1");
        assertThat(data.getOutput(EodOutput.EOD_2018_N)).isEqualTo("N1");
        assertThat(data.getOutput(EodOutput.EOD_2018_M)).isEqualTo("M1");
        assertThat(data.getOutput(EodOutput.AJCC_ID)).isEqualTo("28");
    }

    @Test
    public void testStageDefaultSsdi() {
        EodStagingData data = new EodStagingInputBuilder()
                .withInput(EodInput.PRIMARY_SITE, "C502")
                .withInput(EodInput.HISTOLOGY, "8500")
                .withInput(EodInput.BEHAVIOR, "3")
                .withInput(EodInput.DX_YEAR, "2020")
                .withInput(EodInput.TUMOR_SIZE_SUMMARY, "025")
                .withInput(EodInput.EOD_PRIMARY_TUMOR, "100")
                .withInput(EodInput.EOD_REGIONAL_NODES, "200")
                .withInput(EodInput.EOD_METS, "00")
                .withInput(EodInput.GRADE_CLIN, "1")
                .withInput(EodInput.GRADE_PATH, "1")
                .build();

        // add SSDIs
        // - Lymph Nodes Pos Axillary Level I-II: leave blank, should default to X8)
        // - Oncotype DX Recur Score: leave blank, should default to XX9
        data.setInput("er", "1");
        data.setInput("pr", "0");
        data.setInput("her2_summary", "0");

        // perform the staging
        _STAGING.stage(data);

        assertThat(data.getResult()).isEqualTo(Result.STAGED);
        assertThat(data.getSchemaId()).isEqualTo("breast");
        assertThat(data.getErrors()).isEmpty();
        assertThat(data.getPath()).hasSize(16);
        assertThat(data.getOutput()).hasSize(9);

        // check outputs
        assertThat(data.getOutput(EodOutput.DERIVED_VERSION)).isEqualTo("2.0");
        assertThat(data.getOutput(EodOutput.SS_2018_DERIVED)).isEqualTo("3");
        assertThat(data.getOutput(EodOutput.NAACCR_SCHEMA_ID)).isEqualTo("00480");
        assertThat(data.getOutput(EodOutput.EOD_2018_STAGE_GROUP)).isEqualTo("2B");
        assertThat(data.getOutput(EodOutput.EOD_2018_T)).isEqualTo("T2");
        assertThat(data.getOutput(EodOutput.EOD_2018_N)).isEqualTo("N1");
        assertThat(data.getOutput(EodOutput.EOD_2018_M)).isEqualTo("M0");
        assertThat(data.getOutput(EodOutput.AJCC_ID)).isEqualTo("48.2");
    }

    @Test
    public void testBadLookupInStage() {
        EodStagingData data = new EodStagingData();

        // if site/hist are not supplied, no lookup
        _STAGING.stage(data);
        assertThat(data.getResult()).isEqualTo(Result.FAILED_MISSING_SITE_OR_HISTOLOGY);

        // add hist only and it should fail with same result
        data.setInput(EodInput.PRIMARY_SITE, "C489");
        _STAGING.stage(data);
        assertThat(data.getResult()).isEqualTo(Result.FAILED_MISSING_SITE_OR_HISTOLOGY);

        // put a site/hist combo that doesn't match a schema
        data.setInput(EodInput.HISTOLOGY, "9898");
        _STAGING.stage(data);
        assertThat(data.getResult()).isEqualTo(Result.FAILED_NO_MATCHING_SCHEMA);

        // now a site/hist that returns multiple results
        data.setInput(EodInput.PRIMARY_SITE, "C111");
        data.setInput(EodInput.HISTOLOGY, "8200");
        _STAGING.stage(data);
        assertThat(data.getResult()).isEqualTo(Result.FAILED_MULITPLE_MATCHING_SCHEMAS);
    }

    @Test
    public void testInvolvedTables() {
        Set<String> tables = _STAGING.getInvolvedTables("adnexa_uterine_other");

        assertThat(tables).containsOnly("seer_mets_48348", "nodes_dcc", "grade_clinical_standard_non_ajcc_32473", "grade_pathological_standard_non_ajcc_5627",
                "adnexa_uterine_other_97891", "nodes_pos_fpa", "tumor_size_pathological_25597", "tumor_size_clinical_60979", "primary_site", "histology",
                "nodes_exam_76029", "grade_post_therapy_clin_69737", "grade_post_therapy_path_75348", "schema_selection_adnexa_uterine_other",
                "year_dx_validation", "summary_stage_rpa", "lvi_dna_56663", "tumor_size_summary_63115", "extension_bcn");
    }

    @Test
    public void testInvolvedSchemas() {
        Set<String> schemas = _STAGING.getInvolvedSchemas("her2_summary_30512");

        assertThat(schemas).isEqualTo(new HashSet<>(Collections.singletonList("breast")));
    }

    @Test
    public void testGetInputs() {
        assertThat(_STAGING.getInputs(_STAGING.getSchema("adnexa_uterine_other"))).containsOnly("eod_mets", "site", "hist", "eod_primary_tumor", "eod_regional_nodes");
        assertThat(_STAGING.getInputs(_STAGING.getSchema("testis"))).containsOnly("eod_mets", "site", "hist", "nodes_pos", "s_category_path",
                "eod_primary_tumor", "s_category_clin", "eod_regional_nodes");
    }

    @Test
    public void testIsCodeValid() {
        // test bad parameters for schema or field
        assertThat(_STAGING.isCodeValid("bad_schema_name", "site", "C509")).isFalse();
        assertThat(_STAGING.isCodeValid("testis", "bad_field_name", "C509")).isFalse();

        // test null values
        assertThat(_STAGING.isCodeValid(null, null, null)).isFalse();
        assertThat(_STAGING.isCodeValid("urethra", null, null)).isFalse();
        assertThat(_STAGING.isCodeValid("urethra", "site", null)).isFalse();

        // test fields that have a "value" specified
        assertThat(_STAGING.isCodeValid("urethra", "year_dx", null)).isFalse();
        assertThat(_STAGING.isCodeValid("urethra", "year_dx", "200")).isFalse();
        assertThat(_STAGING.isCodeValid("urethra", "year_dx", "2003")).isFalse();
        assertThat(_STAGING.isCodeValid("urethra", "year_dx", "2145")).isFalse();
        assertThat(_STAGING.isCodeValid("urethra", "year_dx", "2018")).isTrue();

        // test valid and invalid fields
        assertThat(_STAGING.isCodeValid("urethra", "eod_primary_tumor", "000")).isTrue();
        assertThat(_STAGING.isCodeValid("urethra", "eod_primary_tumor", "001")).isFalse();
        assertThat(_STAGING.isCodeValid("urethra", "discriminator_1", "1")).isTrue();
        assertThat(_STAGING.isCodeValid("urethra", "discriminator_1", "9")).isFalse();
    }

    @Test
    public void testIsContextValid() {
        EodStagingData data = new EodStagingData();

        data.setInput(Staging.CTX_YEAR_CURRENT, "2018");

        // test valid year
        data.setInput(EodInput.DX_YEAR, "2018");
        assertThat(_STAGING.isContextValid("urethra", EodStagingData.YEAR_DX_KEY, data.getInput())).isTrue();

        // test invalid year
        data.setInput(EodInput.DX_YEAR, "2016");
        assertThat(_STAGING.isContextValid("urethra", EodStagingData.YEAR_DX_KEY, data.getInput())).isFalse();
    }

    @Test
    public void testGetSchemaIds() {
        Set<String> algorithms = _STAGING.getSchemaIds();

        assertThat(algorithms).isNotEmpty().contains("testis");
    }

    @Test
    public void testGetTableIds() {
        Set<String> tables = _STAGING.getTableIds();

        assertThat(tables).isNotEmpty().contains("urethra_prostatic_urethra_30106");
    }

    @Test
    public void testGetSchema() {
        assertThat(_STAGING.getSchema("bad_schema_name")).isNull();
        assertThat(_STAGING.getSchema("brain")).isNotNull();
        assertThat(_STAGING.getSchema("brain").getName()).isEqualTo("Brain");
    }

    @Test
    public void testLookupOutputs() {
        EodSchemaLookup lookup = new EodSchemaLookup("C680", "8590");
        List<Schema> lookups = _STAGING.lookupSchema(lookup);
        assertThat(lookups).hasSize(2);

        Schema schema = _STAGING.getSchema(lookups.get(0).getId());
        assertThat(schema.getId()).isEqualTo("urethra");

        // build list of output keys
        Set<String> definedOutputs = schema.getOutputs().stream().map(Output::getKey).collect(Collectors.toSet());

        // test without context
        assertThat(_STAGING.getOutputs(schema)).isEqualTo(definedOutputs);

        // test with context
        assertThat(_STAGING.getOutputs(schema, lookup.getInputs())).isEqualTo(definedOutputs);
    }

    @Test
    public void testEncoding() {
        Table table = _STAGING.getTable("serum_alb_pretx_level_58159");

        assertThat(table).isNotNull();

        // the notes of this table contain UTF-8 characters, specifically the symbol: ≥

        // converting to UTF-8 should change nothing
        assertThat(new String(table.getNotes().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8)).isEqualTo(table.getNotes());

        // converting to other encoding should change the text
        assertThat(new String(table.getNotes().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1)).isNotEqualTo(table.getNotes());
        assertThat(new String(table.getNotes().getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII)).isNotEqualTo(table.getNotes());
    }

    @Test
    public void testContentReturnedForInvalidInput() {
        EodStagingData data = new EodStagingInputBuilder()
                .withInput(EodInput.PRIMARY_SITE, "C713")
                .withInput(EodInput.HISTOLOGY, "8020")
                .withInput(EodInput.BEHAVIOR, "3")
                .withInput(EodInput.DX_YEAR, "2018")
                .withInput(EodInput.EOD_PRIMARY_TUMOR, "200")
                .withInput(EodInput.EOD_REGIONAL_NODES, "300")
                .withInput(EodInput.EOD_METS, "00").build();

        // perform the staging
        _STAGING.stage(data);

        assertThat(data.getResult()).isEqualTo(Result.STAGED);
        assertThat(data.getSchemaId()).isEqualTo("brain");
        assertThat(data.getErrors()).hasSize(5);
        assertThat(data.getPath()).hasSize(5);
        assertThat(data.getOutput()).hasSize(9);
        assertThat(data.getOutput()).containsEntry(EodOutput.DERIVED_VERSION.toString(), "2.0");
    }

    @Test
    public void testContentNotReturnedForInvalidYear() {
        EodStagingData data = new EodStagingInputBuilder()
                .withInput(EodInput.PRIMARY_SITE, "C713")
                .withInput(EodInput.HISTOLOGY, "8020")
                .withInput(EodInput.BEHAVIOR, "3")
                .withInput(EodInput.DX_YEAR, "2010")
                .withInput(EodInput.EOD_PRIMARY_TUMOR, "200")
                .withInput(EodInput.EOD_REGIONAL_NODES, "300")
                .withInput(EodInput.EOD_METS, "00").build();

        // perform the staging
        _STAGING.stage(data);

        assertThat(data.getResult()).isEqualTo(Result.FAILED_INVALID_YEAR_DX);
        assertThat(data.getSchemaId()).isEqualTo("brain");
        assertThat(data.getErrors()).isEmpty();
        assertThat(data.getPath()).isEmpty();
        assertThat(data.getOutput()).isEmpty();
    }

    @Test
    public void testGlossary() {
        assertThat(_STAGING.getGlossaryTerms()).hasSize(15);
        GlossaryDefinition entry = _STAGING.getGlossaryDefinition("Medulla");
        assertThat(entry).isNotNull();
        assertThat(entry.getName()).isEqualTo("Medulla");
        assertThat(entry.getDefinition()).startsWith("The central portion of an organ, in contrast to the outer layer");
        assertThat(entry.getAlternateNames()).isEqualTo(Collections.singletonList("Medullary"));
        assertThat(entry.getLastModified()).isNotNull();

        Set<String> hits = _STAGING.getSchemaGlossary("urethra");
        assertThat(hits).hasSize(1);
        hits = _STAGING.getTableGlossary("extension_baj");
        assertThat(hits).hasSize(3);
    }

    @Test
    public void testMetadata() {
        Schema schema = _STAGING.getSchema("lymphoma_cll_sll");
        assertThat(schema).isNotNull();

        Input input = schema.getInputMap().get("grade_path");
        assertThat(input).isNotNull();
        assertThat(input.getMetadata()).extracting("name").containsExactlyInAnyOrder("COC_REQUIRED", "NPCR_REQUIRED", "SSDI", "CCCR_REQUIRED", "SEER_REQUIRED");
    }

}
