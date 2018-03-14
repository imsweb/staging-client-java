/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.eod;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import com.imsweb.staging.SchemaLookup;
import com.imsweb.staging.Staging;
import com.imsweb.staging.StagingData;
import com.imsweb.staging.StagingFileDataProvider;
import com.imsweb.staging.StagingTest;
import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingSchemaOutput;
import com.imsweb.staging.entities.StagingTable;
import com.imsweb.staging.eod.EodStagingData.EodInput;
import com.imsweb.staging.eod.EodStagingData.EodOutput;
import com.imsweb.staging.eod.EodStagingData.EodStagingInputBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class EodStagingTest extends StagingTest {

    @BeforeClass
    public static void init() {
        _STAGING = Staging.getInstance(EodDataProvider.getInstance(EodDataProvider.EodVersion.LATEST));
    }

    @Override
    public String getAlgorithm() {
        return "eod_public";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public StagingFileDataProvider getProvider() {
        return EodDataProvider.getInstance(EodDataProvider.EodVersion.LATEST);
    }

    @Test
    public void testBasicInitialization() {
        assertEquals(118, _STAGING.getSchemaIds().size());
        assertTrue(_STAGING.getTableIds().size() > 0);

        assertNotNull(_STAGING.getSchema("urethra"));
        assertNotNull(_STAGING.getTable("ss2018_urethra_14363"));
    }

    @Test
    public void testVersionInitiaizationTypes() {
        Staging staging10 = Staging.getInstance(EodDataProvider.getInstance(EodDataProvider.EodVersion.v1_0));
        assertEquals(EodDataProvider.EodVersion.LATEST.getVersion(), staging10.getVersion());

        Staging stagingLatest = Staging.getInstance(EodDataProvider.getInstance());
        assertEquals(EodDataProvider.EodVersion.LATEST.getVersion(), stagingLatest.getVersion());
    }

    @Test
    public void testDescriminatorKeys() {
        assertEquals(new HashSet<>(Collections.singletonList("discriminator_1")), _STAGING.getSchema("nasopharynx").getSchemaDiscriminators());
        assertEquals(new HashSet<>(Arrays.asList("discriminator_1", "discriminator_2")), _STAGING.getSchema("oropharynx_p16_neg").getSchemaDiscriminators());
    }

    @Test
    public void testSchemaSelection() {
        // test bad values
        List<StagingSchema> lookup = _STAGING.lookupSchema(new SchemaLookup());
        assertEquals(0, lookup.size());

        lookup = _STAGING.lookupSchema(new EodSchemaLookup("XXX", "YYY"));
        assertEquals(0, lookup.size());

        // test valid combinations that do not require a discriminator
        EodSchemaLookup schemaLookup = new EodSchemaLookup("C629", "9231");
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), "");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertEquals(1, lookup.size());
        assertEquals("soft_tissue_other", lookup.get(0).getId());
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), null);
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertEquals(1, lookup.size());
        assertEquals("soft_tissue_other", lookup.get(0).getId());

        // test valid combination that requires a discriminator but is not supplied one
        lookup = _STAGING.lookupSchema(new EodSchemaLookup("C111", "8200"));
        assertEquals(3, lookup.size());
        for (StagingSchema schema : lookup)
            assertTrue(new HashSet<>(Arrays.asList("discriminator_1", "discriminator_2")).containsAll(schema.getSchemaDiscriminators()));

        // test valid combination that requires discriminator and a good discriminator is supplied
        schemaLookup = new EodSchemaLookup("C111", "8200");
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), "1");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertEquals(1, lookup.size());
        for (StagingSchema schema : lookup)
            assertTrue(new HashSet<>(Arrays.asList("discriminator_1", "discriminator_2")).containsAll(schema.getSchemaDiscriminators()));
        assertEquals("nasopharynx", lookup.get(0).getId());
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), "2");
        schemaLookup.setInput(EodInput.DISCRIMINATOR_2.toString(), "1");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertEquals(1, lookup.size());
        for (StagingSchema schema : lookup)
            assertTrue(new HashSet<>(Arrays.asList("discriminator_1", "discriminator_2")).containsAll(schema.getSchemaDiscriminators()));
        assertEquals("oropharynx_p16_neg", lookup.get(0).getId());

        // test valid combination that requires a discriminator but is supplied a bad disciminator value
        schemaLookup = new EodSchemaLookup("C111", "8200");
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), "X");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertEquals(0, lookup.size());

        // test searching on only site
        lookup = _STAGING.lookupSchema(new EodSchemaLookup("C401", null));
        assertEquals(8, lookup.size());

        // test searching on only hist
        lookup = _STAGING.lookupSchema(new EodSchemaLookup(null, "9702"));
        assertEquals(5, lookup.size());

        // test that searching on only discriminator_1 returns no results
        schemaLookup = new EodSchemaLookup(null, null);
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), "1");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertEquals(0, lookup.size());
        schemaLookup = new EodSchemaLookup("", null);
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), "1");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertEquals(0, lookup.size());
        schemaLookup = new EodSchemaLookup(null, "");
        schemaLookup.setInput(EodInput.DISCRIMINATOR_1.toString(), "1");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertEquals(0, lookup.size());

        // test lookups based on sex
        schemaLookup = new EodSchemaLookup("C481", "8720");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertEquals(2, lookup.size());
        schemaLookup.setInput(EodInput.SEX.toString(), "1");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertEquals(1, lookup.size());
        assertEquals("retroperitoneum", lookup.get(0).getId());
    }

    @Test
    public void testDiscriminatorInputs() {
        Set<String> discriminators = new HashSet<>();
        _STAGING.getSchemaIds().stream()
                .map(schemaId -> _STAGING.getSchema(schemaId))
                .filter(schema -> schema.getSchemaDiscriminators() != null)
                .map(StagingSchema::getSchemaDiscriminators)
                .forEach(discriminators::addAll);

        assertEquals(new HashSet<>(Arrays.asList("sex", "discriminator_1", "discriminator_2")), discriminators);
    }

    @Test
    public void testLookupCache() {
        // do the same lookup twice
        List<StagingSchema> lookup = _STAGING.lookupSchema(new EodSchemaLookup("C629", "9231"));
        assertEquals(1, lookup.size());
        assertEquals("soft_tissue_other", lookup.get(0).getId());

        lookup = _STAGING.lookupSchema(new EodSchemaLookup("C629", "9231"));
        assertEquals(1, lookup.size());
        assertEquals("soft_tissue_other", lookup.get(0).getId());

        // now invalidate the cache
        EodDataProvider.getInstance(EodDataProvider.EodVersion.v1_0).invalidateCache();

        // try the lookup again
        lookup = _STAGING.lookupSchema(new EodSchemaLookup("C629", "9231"));
        assertEquals(1, lookup.size());
        assertEquals("soft_tissue_other", lookup.get(0).getId());
    }

    @Test
    public void testFindTableRow() {
        assertNull(_STAGING.findMatchingTableRow("tumor_size_clinical_60979", "size_clin", "00X"));

        // null maps to blank
        assertEquals(Integer.valueOf(0), _STAGING.findMatchingTableRow("tumor_size_clinical_60979", "size_clin", "000"));
        assertEquals(Integer.valueOf(2), _STAGING.findMatchingTableRow("tumor_size_clinical_60979", "size_clin", "002"));
        assertEquals(Integer.valueOf(2), _STAGING.findMatchingTableRow("tumor_size_clinical_60979", "size_clin", "100"));
        assertEquals(Integer.valueOf(2), _STAGING.findMatchingTableRow("tumor_size_clinical_60979", "size_clin", "988"));
        assertEquals(Integer.valueOf(6), _STAGING.findMatchingTableRow("tumor_size_clinical_60979", "size_clin", "999"));
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

        assertEquals(StagingData.Result.STAGED, data.getResult());
        assertEquals("pancreas", data.getSchemaId());
        assertEquals(0, data.getErrors().size());
        assertEquals(12, data.getPath().size());
        assertEquals(8, data.getOutput().size());

        // check outputs
        assertEquals(EodDataProvider.EodVersion.LATEST.getVersion(), data.getOutput(EodOutput.DERIVED_VERSION));
        assertEquals("7", data.getOutput(EodOutput.SS_2018_DERIVED));
        assertEquals("00280", data.getOutput(EodOutput.NAACCR_SCHEMA_ID));
        assertEquals("4", data.getOutput(EodOutput.EOD_2018_STAGE_GROUP));
        assertEquals("T1", data.getOutput(EodOutput.EOD_2018_T));
        assertEquals("N1", data.getOutput(EodOutput.EOD_2018_N));
        assertEquals("M1", data.getOutput(EodOutput.EOD_2018_M));
        assertEquals("28", data.getOutput(EodOutput.AJCC_ID));
    }

    @Test
    public void testBadLookupInStage() {
        EodStagingData data = new EodStagingData();

        // if site/hist are not supplied, no lookup
        _STAGING.stage(data);
        assertEquals(StagingData.Result.FAILED_MISSING_SITE_OR_HISTOLOGY, data.getResult());

        // add hist only and it should fail with same result
        data.setInput(EodStagingData.EodInput.PRIMARY_SITE, "C489");
        _STAGING.stage(data);
        assertEquals(StagingData.Result.FAILED_MISSING_SITE_OR_HISTOLOGY, data.getResult());

        // put a site/hist combo that doesn't match a schema
        data.setInput(EodStagingData.EodInput.HISTOLOGY, "9898");
        _STAGING.stage(data);
        assertEquals(StagingData.Result.FAILED_NO_MATCHING_SCHEMA, data.getResult());

        // now a site/hist that returns multiple results
        data.setInput(EodStagingData.EodInput.PRIMARY_SITE, "C111");
        data.setInput(EodStagingData.EodInput.HISTOLOGY, "8200");
        _STAGING.stage(data);
        assertEquals(StagingData.Result.FAILED_MULITPLE_MATCHING_SCHEMAS, data.getResult());
    }

    @Test
    public void testInvolvedTables() {
        Set<String> tables = _STAGING.getInvolvedTables("adnexa_uterine_other");

        assertEquals(
                new HashSet<>(Arrays.asList("seer_mets_48348", "nodes_dcc", "grade_clinical_standard_non_ajcc_32473", "grade_pathological_standard_non_ajcc_5627", "adnexa_uterine_other_97891",
                        "nodes_pos_fpa", "tumor_size_pathological_25597", "tumor_size_clinical_60979", "primary_site", "histology", "nodes_exam_76029", "grade_post_therapy_standard_non_ajcc_43091",
                        "schema_selection_adnexa_uterine_other", "year_dx_validation", "summary_stage_rpa", "tumor_size_summary_63115", "extension_bcn")), tables);
    }

    @Test
    public void testInvolvedSchemas() {
        Set<String> schemas = _STAGING.getInvolvedSchemas("her2_summary_30512");

        assertEquals(new HashSet<>(Collections.singletonList("breast")), schemas);
    }

    @Test
    public void testGetInputs() {
        assertEquals(new HashSet<>(Arrays.asList("eod_mets", "site", "hist", "eod_primary_tumor", "eod_regional_nodes")),
                _STAGING.getInputs(_STAGING.getSchema("adnexa_uterine_other")));

        assertEquals(
                new HashSet<>(Arrays.asList("eod_mets", "site", "hist", "nodes_pos", "s_category_path", "eod_primary_tumor", "s_category_clin", "eod_regional_nodes")),
                _STAGING.getInputs(_STAGING.getSchema("testis")));
    }

    @Test
    public void testIsCodeValid() {
        // test bad parameters for schema or field
        assertFalse(_STAGING.isCodeValid("bad_schema_name", "site", "C509"));
        assertFalse(_STAGING.isCodeValid("testis", "bad_field_name", "C509"));

        // test null values
        assertFalse(_STAGING.isCodeValid(null, null, null));
        assertFalse(_STAGING.isCodeValid("urethra", null, null));
        assertFalse(_STAGING.isCodeValid("urethra", "site", null));

        // test fields that have a "value" specified
        assertFalse(_STAGING.isCodeValid("urethra", "year_dx", null));
        assertFalse(_STAGING.isCodeValid("urethra", "year_dx", "200"));
        assertFalse(_STAGING.isCodeValid("urethra", "year_dx", "2003"));
        assertFalse(_STAGING.isCodeValid("urethra", "year_dx", "2145"));
        assertTrue(_STAGING.isCodeValid("urethra", "year_dx", "2018"));

        // test valid and invalid fields
        assertTrue(_STAGING.isCodeValid("urethra", "eod_primary_tumor", "000"));
        assertFalse(_STAGING.isCodeValid("urethra", "eod_primary_tumor", "001"));
        assertTrue(_STAGING.isCodeValid("urethra", "discriminator_1", "1"));
        assertFalse(_STAGING.isCodeValid("urethra", "discriminator_1", "9"));
    }

    @Test
    public void testIsContextValid() {
        EodStagingData data = new EodStagingData();

        data.setInput(Staging.CTX_YEAR_CURRENT, "2018");

        // test valid year
        data.setInput(EodStagingData.EodInput.DX_YEAR, "2018");
        assertTrue(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput()));

        // test invalid year
        data.setInput(EodStagingData.EodInput.DX_YEAR, "2016");
        assertFalse(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput()));
    }

    @Test
    public void testGetSchemaIds() {
        Set<String> algorithms = _STAGING.getSchemaIds();

        assertTrue(algorithms.size() > 0);
        assertTrue(algorithms.contains("testis"));
    }

    @Test
    public void testGetTableIds() {
        Set<String> tables = _STAGING.getTableIds();

        assertTrue(tables.size() > 0);
        assertTrue(tables.contains("urethra_prostatic_urethra_30106"));
    }

    @Test
    public void testGetSchema() {
        assertNull(_STAGING.getSchema("bad_schema_name"));
        assertNotNull(_STAGING.getSchema("brain"));
        assertEquals("Brain", _STAGING.getSchema("brain").getName());
    }

    @Test
    public void testLookupOutputs() {
        EodSchemaLookup lookup = new EodSchemaLookup("C680", "8590");
        List<StagingSchema> lookups = _STAGING.lookupSchema(lookup);
        assertEquals(2, lookups.size());

        StagingSchema schema = _STAGING.getSchema(lookups.get(0).getId());
        assertEquals("urethra", schema.getId());

        // build list of output keys
        Set<String> definedOutputs = schema.getOutputs().stream().map(StagingSchemaOutput::getKey).collect(Collectors.toSet());

        // test without context
        assertEquals(definedOutputs, _STAGING.getOutputs(schema));

        // test with context
        assertEquals(definedOutputs, _STAGING.getOutputs(schema, lookup.getInputs()));
    }

    @Test
    public void testEncoding() {
        StagingTable table = _STAGING.getTable("psa_46258");

        assertNotNull(table);

        // the notes of this table contain UTF-8 characters, specifically the single quote character in this phrase: "You may use a physician’s statement"

        // converting to UTF-8 should change nothing
        assertEquals(table.getNotes(), new String(table.getNotes().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        // converting to other encoding should change the text
        assertNotEquals(table.getNotes(), new String(table.getNotes().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1));
        assertNotEquals(table.getNotes(), new String(table.getNotes().getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII));
    }

}
