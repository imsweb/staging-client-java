/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.eod;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.imsweb.staging.ExternalStagingFileDataProvider;
import com.imsweb.staging.Staging;
import com.imsweb.staging.StagingDataProvider;
import com.imsweb.staging.StagingTest;
import com.imsweb.staging.entities.GlossaryDefinition;
import com.imsweb.staging.entities.Input;
import com.imsweb.staging.entities.Output;
import com.imsweb.staging.entities.Schema;
import com.imsweb.staging.entities.SchemaLookup;
import com.imsweb.staging.entities.StagingData;
import com.imsweb.staging.entities.StagingData.Result;
import com.imsweb.staging.entities.Table;
import com.imsweb.staging.entities.impl.StagingMetadata;
import com.imsweb.staging.eod.EodStagingData.EodInput;
import com.imsweb.staging.eod.EodStagingData.EodOutput;
import com.imsweb.staging.eod.EodStagingData.EodStagingInputBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EodStagingTest extends StagingTest {

    @BeforeAll
    static void init() throws URISyntaxException, IOException {
        _PROVIDER = new ExternalStagingFileDataProvider(getAlgorithmPath("eod_public"));
        _STAGING = Staging.getInstance(_PROVIDER);
    }

    @Override
    public String getAlgorithm() {
        return "eod_public";
    }

    @Override
    public String getVersion() {
        return "3.3";
    }

    @Test
    void testBasicInitialization() {
        assertThat(_STAGING.getSchemaIds()).hasSize(141);
        assertThat(_STAGING.getTableIds()).isNotEmpty();

        assertThat(_STAGING.getSchema("urethra")).isNotNull();
        assertThat(_STAGING.getTable("ss2018_urethra_14363")).isNotNull();
    }

    @Test
    void testDescriminatorKeys() {
        assertThat(_STAGING.getSchema("nasopharynx").getSchemaDiscriminators()).containsOnly("discriminator_1", "year_dx");
        assertThat(_STAGING.getSchema("oropharynx_p16_neg").getSchemaDiscriminators()).containsOnly("discriminator_1", "discriminator_2", "year_dx");
    }

    @Test
    void testSchemaSelection() {
        // test bad values
        List<Schema> lookup = _STAGING.lookupSchema(new SchemaLookup());
        assertThat(lookup).isEmpty();

        lookup = _STAGING.lookupSchema(new EodSchemaLookup("XXX", "YYY"));
        assertThat(lookup).isEmpty();

        // test valid combinations that do not require a discriminator
        EodSchemaLookup schemaLookup = new EodSchemaLookup("C629", "9231");
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), "");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).hasSize(1);
        assertThat(lookup.getFirst().getId()).isEqualTo("soft_tissue_rare");
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), null);
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).hasSize(1);
        assertThat(lookup.getFirst().getId()).isEqualTo("soft_tissue_rare");

        // test valid combination that requires a discriminator but is not supplied one
        lookup = _STAGING.lookupSchema(new EodSchemaLookup("C111", "8200"));
        assertThat(lookup).hasSize(4);
        assertThat(lookup.stream().map(Schema::getId).collect(Collectors.toSet())).isEqualTo(
                new HashSet<>(Arrays.asList(
                        "oropharynx_p16_neg",
                        "nasopharynx",
                        "nasopharynx_v9_2025",
                        "oropharynx_hpv_mediated_p16_pos"
                )));
        assertThat(lookup.stream().flatMap(d -> d.getSchemaDiscriminators().stream()).collect(Collectors.toSet())).isEqualTo(
                new HashSet<>(Arrays.asList("year_dx", "discriminator_1", "discriminator_2")));

        // test valid combination that requires discriminator and a good discriminator is supplied
        schemaLookup = new EodSchemaLookup("C111", "8200");
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), "1");
        schemaLookup.setInput(EodInput.DX_YEAR.toString(), "2022");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).hasSize(1);
        assertThat(lookup.stream().flatMap(d -> d.getSchemaDiscriminators().stream()).collect(Collectors.toSet())).isEqualTo(new HashSet<>(Arrays.asList("discriminator_1", "year_dx")));
        assertThat(lookup.getFirst().getId()).isEqualTo("nasopharynx");

        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), "2");
        schemaLookup.setInput(EodInput.DISCRIMINATOR_2.toString(), "1");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).hasSize(1);
        assertThat(lookup.stream().flatMap(d -> d.getSchemaDiscriminators().stream()).collect(Collectors.toSet())).isEqualTo(
                new HashSet<>(Arrays.asList("year_dx", "discriminator_1", "discriminator_2")));
        assertThat(lookup.getFirst().getId()).isEqualTo("oropharynx_p16_neg");

        // test valid combination that requires a discriminator but is supplied a bad disciminator value
        schemaLookup = new EodSchemaLookup("C111", "8200");
        schemaLookup.setInput(EodInput.YEAR_DX.toString(), "X");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).isEmpty();

        // test searching on only site
        lookup = _STAGING.lookupSchema(new EodSchemaLookup("C401", null));
        assertThat(lookup).hasSize(9);

        // test searching on only hist
        lookup = _STAGING.lookupSchema(new EodSchemaLookup(null, "9702"));
        assertThat(lookup).hasSize(8);

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
        assertThat(lookup.getFirst().getId()).isEqualTo("retroperitoneum");
    }

    @Test
    void testDiscriminatorInputs() {
        Set<String> discriminators = new HashSet<>();
        _STAGING.getSchemaIds().stream()
                .map(schemaId -> _STAGING.getSchema(schemaId))
                .map(Schema::getSchemaDiscriminators)
                .filter(Objects::nonNull)
                .forEach(discriminators::addAll);

        assertThat(discriminators).containsOnly("year_dx", "sex_at_birth", "behavior", "discriminator_1", "discriminator_2");
    }

    @Test
    void testLookupCache() {
        // do the same lookup twice
        List<Schema> lookup = _STAGING.lookupSchema(new EodSchemaLookup("C629", "9231"));
        assertThat(lookup).hasSize(1);
        assertThat(lookup.getFirst().getId()).isEqualTo("soft_tissue_rare");

        lookup = _STAGING.lookupSchema(new EodSchemaLookup("C629", "9231"));
        assertThat(lookup).hasSize(1);
        assertThat(lookup.getFirst().getId()).isEqualTo("soft_tissue_rare");

        // now invalidate the cache
        _PROVIDER.invalidateCache();

        // try the lookup again
        lookup = _STAGING.lookupSchema(new EodSchemaLookup("C629", "9231"));
        assertThat(lookup).hasSize(1);
        assertThat(lookup.getFirst().getId()).isEqualTo("soft_tissue_rare");
    }

    @Test
    void testFindTableRow() {
        assertThat(_STAGING.findMatchingTableRow("tumor_size_clinical_60979", "size_clin", "00X")).isNull();

        // null maps to blank
        assertThat(_STAGING.findMatchingTableRow("tumor_size_clinical_60979", "size_clin", "000")).isEqualTo(Integer.valueOf(0));
        assertThat(_STAGING.findMatchingTableRow("tumor_size_clinical_60979", "size_clin", "002")).isEqualTo(Integer.valueOf(2));
        assertThat(_STAGING.findMatchingTableRow("tumor_size_clinical_60979", "size_clin", "100")).isEqualTo(Integer.valueOf(2));
        assertThat(_STAGING.findMatchingTableRow("tumor_size_clinical_60979", "size_clin", "988")).isEqualTo(Integer.valueOf(2));
        assertThat(_STAGING.findMatchingTableRow("tumor_size_clinical_60979", "size_clin", "999")).isEqualTo(Integer.valueOf(5));
    }

    @Test
    void testFindTableRowDecimal() {
        // only do float comparison of ranges if the low or high vaslues have a decimal
        assertThat(_STAGING.findMatchingTableRow("age_at_diagnosis_validation_65093", "age_dx", "10.5")).isNull();

        assertThat(_STAGING.findMatchingTableRow("age_at_diagnosis_validation_65093", "age_dx", "10")).isNotNull();
    }

    @Test
    void testStagePancreas() {
        EodStagingData data = new EodStagingInputBuilder()
                .withInput(EodInput.DX_YEAR, "2018")
                .withInput(EodInput.PRIMARY_SITE, "C250")
                .withInput(EodInput.HISTOLOGY, "8154")
                .withInput(EodInput.EOD_PRIMARY_TUMOR, "500")
                .withInput(EodInput.EOD_REGIONAL_NODES, "300")
                .withInput(EodInput.EOD_METS, "10")
                .withInput(EodInput.NODES_POS, "03")
                .build();

        // perform the staging
        _STAGING.stage(data);

        assertThat(data.getResult()).isEqualTo(Result.STAGED);
        assertThat(data.getSchemaId()).isEqualTo("pancreas");
        assertThat(data.getErrors()).isEmpty();
        assertThat(data.getPath()).hasSize(5);
        assertThat(data.getOutput()).hasSize(4);

        // check outputs
        assertThat(data.getOutput(EodOutput.DERIVED_VERSION)).isEqualTo(getVersion());
        assertThat(data.getOutput(EodOutput.NAACCR_SCHEMA_ID)).isEqualTo("00280");
        assertThat(data.getOutput(EodOutput.SS_2018_DERIVED)).isEqualTo("7");
        assertThat(data.getOutput(EodOutput.DERIVED_SUMMARY_GRADE)).isEqualTo("9");
    }

    @Test
    void testStageDefaultSsdi() {
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
        // - Lymph Nodes Pos Axillary Level I-II: leave blank, should default to X8
        // - Oncotype DX Recur Score: leave blank, should default to XX9
        data.setInput("er", "1");
        data.setInput("pr", "0");
        data.setInput("her2_summary", "0");

        // perform the staging
        _STAGING.stage(data);

        assertThat(data.getResult()).isEqualTo(Result.STAGED);
        assertThat(data.getSchemaId()).isEqualTo("breast");
        assertThat(data.getErrors()).isEmpty();
        assertThat(data.getPath()).hasSize(5);
        assertThat(data.getOutput()).hasSize(4);

        // check outputs
        assertThat(data.getOutput(EodOutput.DERIVED_VERSION)).isEqualTo(getVersion());
        assertThat(data.getOutput(EodOutput.SS_2018_DERIVED)).isEqualTo("3");
        assertThat(data.getOutput(EodOutput.NAACCR_SCHEMA_ID)).isEqualTo("00480");
        assertThat(data.getOutput(EodOutput.DERIVED_SUMMARY_GRADE)).isEqualTo("1");
    }

    @Test
    void testBadLookupInStage() {
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
    void testInvolvedTables() {
        Set<String> tables = _STAGING.getInvolvedTables("adnexa_uterine_other");

        assertThat(tables).containsOnly("seer_mets_48348", "nodes_dcc", "grade_clinical_standard_non_ajcc_32473", "grade_pathological_standard_non_ajcc_5627",
                "adnexa_uterine_other_97891", "nodes_pos_fpa", "tumor_size_pathological_25597", "tumor_size_clinical_60979", "primary_site", "histology",
                "nodes_exam_76029", "grade_post_therapy_clin_69737", "grade_post_therapy_path_75348", "schema_selection_adnexa_uterine_other",
                "year_dx_validation", "summary_stage_rpa", "tumor_size_summary_63115", "extension_bcn", "combined_grade_56638", "neoadjuvant_therapy_37302",
                "derived_grade_standard_non_ajcc_63932", "neoadj_tx_treatment_effect_18122", "neoadj_tx_clinical_response_31723", "ss2018_adnexa_uterine_other_values_44976",
                "behavior", "type_of_reporting_source_76696");
    }

    @Test
    void testInvolvedSchemas() {
        Set<String> schemas = _STAGING.getInvolvedSchemas("her2_summary_30512");

        assertThat(schemas).isEqualTo(new HashSet<>(Collections.singletonList("breast")));
    }

    @Test
    void testGetInputs() {
        assertThat(_STAGING.getInputs(_STAGING.getSchema("adnexa_uterine_other"))).containsOnly("eod_mets", "site", "hist",
                "eod_primary_tumor", "eod_regional_nodes", "grade_path", "grade_clin");
        assertThat(_STAGING.getInputs(_STAGING.getSchema("testis"))).containsOnly("eod_mets", "site", "hist",
                "eod_primary_tumor", "eod_regional_nodes", "grade_path", "grade_clin");
    }

    @Test
    void testIsCodeValid() {
        // test bad parameters for schema or field
        assertThat(_STAGING.isCodeValid("bad_schema_name", "site", "C509")).isFalse();
        assertThat(_STAGING.isCodeValid("testis", "bad_field_name", "C509")).isFalse();

        // test null values
        assertThat(_STAGING.isCodeValid(null, null, null)).isFalse();
        assertThat(_STAGING.isCodeValid("urethra", null, null)).isFalse();
        assertThat(_STAGING.isCodeValid("urethra", "site", null)).isFalse();

        // test fields that have a "value" specified
        assertThat(_STAGING.isCodeValid("urethra", "year_dx", null)).isTrue();
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
    void testIsContextValid() {
        EodStagingData data = new EodStagingData();

        data.setInput(Staging.CTX_YEAR_CURRENT, "2018");

        // test valid year
        data.setInput(EodInput.DX_YEAR, "2018");
        assertThat(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput())).isTrue();

        // test invalid year
        data.setInput(EodInput.DX_YEAR, "2016");
        assertThat(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput())).isFalse();
    }

    @Test
    void testGetSchemaIds() {
        Set<String> algorithms = _STAGING.getSchemaIds();

        assertThat(algorithms).isNotEmpty().contains("testis");
    }

    @Test
    void testGetTableIds() {
        Set<String> tables = _STAGING.getTableIds();

        assertThat(tables).isNotEmpty().contains("urethra_prostatic_urethra_30106");
    }

    @Test
    void testGetSchema() {
        assertThat(_STAGING.getSchema("bad_schema_name")).isNull();
        assertThat(_STAGING.getSchema("brain")).isNotNull();
        assertThat(_STAGING.getSchema("brain").getName()).isEqualTo("Brain [8th: 2018-2022]");
    }

    @Test
    void testLookupOutputs() {
        EodSchemaLookup lookup = new EodSchemaLookup("C680", "8590");
        List<Schema> lookups = _STAGING.lookupSchema(lookup);
        assertThat(lookups).hasSize(2);

        Schema schema = _STAGING.getSchema(lookups.getFirst().getId());
        assertThat(schema.getId()).isEqualTo("urethra");

        // build list of output keys
        Set<String> definedOutputs = schema.getOutputs().stream().map(Output::getKey).collect(Collectors.toSet());

        // test without context
        assertThat(_STAGING.getOutputs(schema)).isEqualTo(definedOutputs);

        // test with context
        assertThat(_STAGING.getOutputs(schema, lookup.getInputs())).isEqualTo(definedOutputs);
    }

    @Test
    void testEncoding() {
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
    void testContentReturnedForInvalidInput() {
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
        assertThat(data.getOutput()).hasSize(4);
        assertThat(data.getOutput()).containsEntry(EodOutput.DERIVED_VERSION.toString(), "3.3");
    }

    @Test
    void testContentNotReturnedForInvalidYear() {
        EodStagingData data = new EodStagingInputBuilder()
                .withInput(EodInput.PRIMARY_SITE, "C670")
                .withInput(EodInput.HISTOLOGY, "8000")
                .withInput(EodInput.BEHAVIOR, "3")
                .withInput(EodInput.DX_YEAR, "2010")
                .withInput(EodInput.EOD_PRIMARY_TUMOR, "200")
                .withInput(EodInput.EOD_REGIONAL_NODES, "300")
                .withInput(EodInput.EOD_METS, "00").build();

        // perform the staging
        _STAGING.stage(data);

        assertThat(data.getResult()).isEqualTo(Result.FAILED_INVALID_YEAR_DX);
        assertThat(data.getSchemaId()).isEqualTo("bladder");
        assertThat(data.getErrors()).isEmpty();
        assertThat(data.getPath()).isEmpty();
        assertThat(data.getOutput()).isEmpty();
    }

    @Test
    void testGlossary() {
        assertEquals(1, _STAGING.getGlossaryTerms().size());
        GlossaryDefinition entry = _STAGING.getGlossaryDefinition("Level V lymph nodes");
        assertNotNull(entry);
        assertEquals("Level V lymph nodes", entry.getName());
        assertTrue(entry.getDefinition().startsWith("The two groups dorsal cervical nodes along the spinal"));
        assertEquals(Arrays.asList("Level VA", "Level VB"), entry.getAlternateNames());
        assertNotNull(entry.getLastModified());
    }

    @Test
    void testMetadata() {
        Schema urethra = _STAGING.getSchema("urethra");
        assertNotNull(urethra);

        Input gradeClin = urethra.getInputMap().get("grade_clin");
        assertNotNull(gradeClin);

        assertEquals(5, gradeClin.getMetadata().size());
        assertTrue(gradeClin.getMetadata().contains(new StagingMetadata("COC_REQUIRED")));
        assertTrue(gradeClin.getMetadata().contains(new StagingMetadata("CCCR_REQUIRED")));
        assertTrue(gradeClin.getMetadata().contains(new StagingMetadata("SEER_REQUIRED")));
        assertTrue(gradeClin.getMetadata().contains(new StagingMetadata("NPCR_REQUIRED")));
        assertTrue(gradeClin.getMetadata().contains(new StagingMetadata("SSDI")));
    }

    @Test
    void testCachedSiteAndHistology() {
        StagingDataProvider provider = _PROVIDER;
        assertThat(provider.getValidSites()).isNotEmpty();
        assertThat(provider.getValidHistologies()).isNotEmpty();

        // site tests
        List<String> validSites = Arrays.asList("C000", "C809");
        List<String> invalidSites = Arrays.asList("C727", "C810");
        for (String site : validSites)
            assertThat(provider.getValidSites()).contains(site);
        for (String site : invalidSites)
            assertThat(provider.getValidSites()).doesNotContain(site);

        // hist tests
        List<String> validHist = Arrays.asList("8000", "8002", "8005", "8290", "9992", "9993");
        List<String> invalidHist = Arrays.asList("8006", "9990");
        for (String hist : validHist)
            assertThat(provider.getValidHistologies())
                    .withFailMessage("The histology '" + hist + "' is not in the valid histology list")
                    .contains(hist);
        for (String hist : invalidHist)
            assertThat(provider.getValidHistologies())
                    .withFailMessage("The histology '" + hist + "' is not supposed to be in the valid histology list")
                    .doesNotContain(hist);
    }

    @Test
    void testInputsAndOutputs() {
        Set<String> inputs = new HashSet<>();
        Set<String> outputs = new HashSet<>();

        // collect all unique input and output keys
        for (String schemaId : _STAGING.getSchemaIds()) {
            Schema schema = _STAGING.getSchema(schemaId);
            schema.getInputs().stream().map(Input::getKey).forEach(inputs::add);
            schema.getOutputs().stream().map(Output::getKey).forEach(outputs::add);
        }

        // test outputs
        Set<String> enumOutputKeys = Arrays.stream(EodOutput.values())
                .map(EodOutput::toString)
                .collect(Collectors.toSet());

        Set<String> missingInOutputEnum = new HashSet<>(outputs);
        missingInOutputEnum.removeAll(enumOutputKeys);

        Set<String> unusedOutputEnum = new HashSet<>(enumOutputKeys);
        unusedOutputEnum.removeAll(outputs);

        assertThat(missingInOutputEnum).as("Output keys found in staging data but not in EodOutput enum").isEmpty();
        assertThat(unusedOutputEnum).as("EodOutput enum values never appear in staging outputs").isEmpty();

        // test inputs (don't need every input to be in enum)
        Set<String> unusedInputEnum = Arrays.stream(EodInput.values()).map(EodInput::toString).collect(Collectors.toSet());
        unusedInputEnum.removeAll(inputs);

        assertThat(unusedInputEnum).as("EodInput enum values never appear in staging inputs").isEmpty();
    }

}
