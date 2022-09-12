/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.eod;

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

import com.imsweb.staging.Staging;
import com.imsweb.staging.StagingDataProvider;
import com.imsweb.staging.StagingFileDataProvider;
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
import com.imsweb.staging.eod.EodDataProvider.EodVersion;
import com.imsweb.staging.eod.EodStagingData.EodInput;
import com.imsweb.staging.eod.EodStagingData.EodOutput;
import com.imsweb.staging.eod.EodStagingData.EodStagingInputBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EodStagingTest extends StagingTest {

    @BeforeAll
    public static void init() {
        _STAGING = Staging.getInstance(EodDataProvider.getInstance(EodDataProvider.EodVersion.LATEST));
    }

    @Override
    public String getAlgorithm() {
        return "eod_public";
    }

    @Override
    public String getVersion() {
        return EodVersion.V3_0.getVersion();
    }

    @Override
    public StagingFileDataProvider getProvider() {
        return EodDataProvider.getInstance(EodDataProvider.EodVersion.LATEST);
    }

    @Test
    void testBasicInitialization() {
        assertThat(_STAGING.getSchemaIds()).hasSize(127);
        assertThat(_STAGING.getTableIds().size() > 0).isTrue();

        assertThat(_STAGING.getSchema("urethra")).isNotNull();
        assertThat(_STAGING.getTable("ss2018_urethra_14363")).isNotNull();
    }

    @Test
    void testVersionInitializationTypes() {
        Staging staging10 = Staging.getInstance(EodDataProvider.getInstance(EodDataProvider.EodVersion.V3_0));
        assertThat(staging10.getVersion()).isEqualTo(EodDataProvider.EodVersion.LATEST.getVersion());

        Staging stagingLatest = Staging.getInstance(EodDataProvider.getInstance());
        assertThat(stagingLatest.getVersion()).isEqualTo(EodDataProvider.EodVersion.LATEST.getVersion());
    }

    @Test
    void testDescriminatorKeys() {
        assertThat(_STAGING.getSchema("nasopharynx").getSchemaDiscriminators()).containsOnly("discriminator_1");
        assertThat(_STAGING.getSchema("oropharynx_p16_neg").getSchemaDiscriminators()).containsOnly("discriminator_1", "discriminator_2");
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
        assertThat(lookup.get(0).getId()).isEqualTo("soft_tissue_rare");
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), null);
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertThat(lookup).hasSize(1);
        assertThat(lookup.get(0).getId()).isEqualTo("soft_tissue_rare");

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
        assertThat(lookup.get(0).getId()).isEqualTo("retroperitoneum");
    }

    @Test
    void testDiscriminatorInputs() {
        Set<String> discriminators = new HashSet<>();
        _STAGING.getSchemaIds().stream()
                .map(schemaId -> _STAGING.getSchema(schemaId))
                .map(Schema::getSchemaDiscriminators)
                .filter(Objects::nonNull)
                .forEach(discriminators::addAll);

        assertThat(discriminators).containsOnly("year_dx", "sex", "behavior", "discriminator_1", "discriminator_2");
    }

    @Test
    void testLookupCache() {
        // do the same lookup twice
        List<Schema> lookup = _STAGING.lookupSchema(new EodSchemaLookup("C629", "9231"));
        assertThat(lookup).hasSize(1);
        assertThat(lookup.get(0).getId()).isEqualTo("soft_tissue_rare");

        lookup = _STAGING.lookupSchema(new EodSchemaLookup("C629", "9231"));
        assertThat(lookup).hasSize(1);
        assertThat(lookup.get(0).getId()).isEqualTo("soft_tissue_rare");

        // now invalidate the cache
        EodDataProvider.getInstance(EodDataProvider.EodVersion.V3_0).invalidateCache();

        // try the lookup again
        lookup = _STAGING.lookupSchema(new EodSchemaLookup("C629", "9231"));
        assertThat(lookup).hasSize(1);
        assertThat(lookup.get(0).getId()).isEqualTo("soft_tissue_rare");
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
    void testStageUrethra() {
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

        assertThat(data.getResult()).isEqualTo(StagingData.Result.STAGED);
        assertThat(data.getSchemaId()).isEqualTo("pancreas");
        assertThat(data.getErrors()).isEmpty();
        assertThat(data.getPath()).hasSize(12);
        assertThat(data.getOutput()).hasSize(9);

        // check outputs
        assertThat(data.getOutput(EodOutput.DERIVED_VERSION)).isEqualTo(EodDataProvider.EodVersion.LATEST.getVersion());
        assertThat(data.getOutput(EodOutput.SS_2018_DERIVED)).isEqualTo("7");
        assertThat(data.getOutput(EodOutput.NAACCR_SCHEMA_ID)).isEqualTo("00280");
        assertThat(data.getOutput(EodOutput.EOD_2018_STAGE_GROUP)).isEqualTo("4");
        assertThat(data.getOutput(EodOutput.EOD_2018_T)).isEqualTo("T1a");
        assertThat(data.getOutput(EodOutput.EOD_2018_N)).isEqualTo("N1");
        assertThat(data.getOutput(EodOutput.EOD_2018_M)).isEqualTo("M1");
        assertThat(data.getOutput(EodOutput.AJCC_ID)).isEqualTo("28");
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

        assertThat(data.getResult()).isEqualTo(StagingData.Result.STAGED);
        assertThat(data.getSchemaId()).isEqualTo("breast");
        assertThat(data.getErrors()).isEmpty();
        assertThat(data.getPath()).hasSize(16);
        assertThat(data.getOutput()).hasSize(9);

        // check outputs
        assertThat(data.getOutput(EodOutput.DERIVED_VERSION)).isEqualTo(EodDataProvider.EodVersion.LATEST.getVersion());
        assertThat(data.getOutput(EodOutput.SS_2018_DERIVED)).isEqualTo("3");
        assertThat(data.getOutput(EodOutput.NAACCR_SCHEMA_ID)).isEqualTo("00480");
        assertThat(data.getOutput(EodOutput.EOD_2018_STAGE_GROUP)).isEqualTo("2B");
        assertThat(data.getOutput(EodOutput.EOD_2018_T)).isEqualTo("T2");
        assertThat(data.getOutput(EodOutput.EOD_2018_N)).isEqualTo("N1");
        assertThat(data.getOutput(EodOutput.EOD_2018_M)).isEqualTo("M0");
        assertThat(data.getOutput(EodOutput.AJCC_ID)).isEqualTo("48.2");
    }

    @Test
    void testBadLookupInStage() {
        EodStagingData data = new EodStagingData();

        // if site/hist are not supplied, no lookup
        _STAGING.stage(data);
        assertThat(data.getResult()).isEqualTo(StagingData.Result.FAILED_MISSING_SITE_OR_HISTOLOGY);

        // add hist only and it should fail with same result
        data.setInput(EodStagingData.EodInput.PRIMARY_SITE, "C489");
        _STAGING.stage(data);
        assertThat(data.getResult()).isEqualTo(StagingData.Result.FAILED_MISSING_SITE_OR_HISTOLOGY);

        // put a site/hist combo that doesn't match a schema
        data.setInput(EodStagingData.EodInput.HISTOLOGY, "9898");
        _STAGING.stage(data);
        assertThat(data.getResult()).isEqualTo(StagingData.Result.FAILED_NO_MATCHING_SCHEMA);

        // now a site/hist that returns multiple results
        data.setInput(EodStagingData.EodInput.PRIMARY_SITE, "C111");
        data.setInput(EodStagingData.EodInput.HISTOLOGY, "8200");
        _STAGING.stage(data);
        assertThat(data.getResult()).isEqualTo(StagingData.Result.FAILED_MULITPLE_MATCHING_SCHEMAS);
    }

    @Test
    void testInvolvedTables() {
        Set<String> tables = _STAGING.getInvolvedTables("adnexa_uterine_other");

        assertThat(tables).containsOnly("seer_mets_48348", "nodes_dcc", "grade_clinical_standard_non_ajcc_32473", "grade_pathological_standard_non_ajcc_5627",
                "adnexa_uterine_other_97891", "nodes_pos_fpa", "tumor_size_pathological_25597", "tumor_size_clinical_60979", "primary_site", "histology",
                "nodes_exam_76029", "grade_post_therapy_clin_69737", "grade_post_therapy_path_75348", "schema_selection_adnexa_uterine_other",
                "year_dx_validation", "summary_stage_rpa", "lvi_full_56663", "tumor_size_summary_63115", "extension_bcn");
    }

    @Test
    void testInvolvedSchemas() {
        Set<String> schemas = _STAGING.getInvolvedSchemas("her2_summary_30512");

        assertThat(schemas).isEqualTo(new HashSet<>(Collections.singletonList("breast")));
    }

    @Test
    void testGetInputs() {
        assertThat(_STAGING.getInputs(_STAGING.getSchema("adnexa_uterine_other"))).containsOnly("eod_mets", "site", "hist", "eod_primary_tumor", "eod_regional_nodes");
        assertThat(_STAGING.getInputs(_STAGING.getSchema("testis"))).containsOnly("eod_mets", "site", "hist", "nodes_pos", "s_category_path",
                "eod_primary_tumor", "s_category_clin", "eod_regional_nodes");
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
    void testIsContextValid() {
        EodStagingData data = new EodStagingData();

        data.setInput(Staging.CTX_YEAR_CURRENT, "2018");

        // test valid year
        data.setInput(EodStagingData.EodInput.DX_YEAR, "2018");
        assertThat(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput())).isTrue();

        // test invalid year
        data.setInput(EodStagingData.EodInput.DX_YEAR, "2016");
        assertThat(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput())).isFalse();
    }

    @Test
    void testGetSchemaIds() {
        Set<String> algorithms = _STAGING.getSchemaIds();

        assertThat(algorithms.size() > 0).isTrue();
        assertThat(algorithms).contains("testis");
    }

    @Test
    void testGetTableIds() {
        Set<String> tables = _STAGING.getTableIds();

        assertThat(tables.size() > 0).isTrue();
        assertThat(tables).contains("urethra_prostatic_urethra_30106");
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
        assertThat(data.getPath()).hasSize(4);
        assertThat(data.getOutput()).hasSize(7);
        assertThat(data.getOutput()).containsEntry(EodOutput.DERIVED_VERSION.toString(), "3.0");
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
        assertEquals(23, _STAGING.getGlossaryTerms().size());
        GlossaryDefinition entry = _STAGING.getGlossaryDefinition("Medulla");
        assertNotNull(entry);
        assertEquals("Medulla", entry.getName());
        assertTrue(entry.getDefinition().startsWith("The central portion of an organ, in contrast to the outer layer"));
        assertEquals(Collections.singletonList("Medullary"), entry.getAlternateNames());
        assertNotNull(entry.getLastModified());

        Set<String> hits = _STAGING.getSchemaGlossary("urethra");
        assertEquals(1, hits.size());
        hits = _STAGING.getTableGlossary("extension_baj");
        assertEquals(3, hits.size());
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
        StagingDataProvider provider = getProvider();
        assertThat(provider.getValidSites().size() > 0).isTrue();
        assertThat(provider.getValidHistologies().size() > 0).isTrue();

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
}
