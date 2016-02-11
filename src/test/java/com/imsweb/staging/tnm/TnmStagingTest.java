/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.tnm;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Sets;

import com.imsweb.staging.SchemaLookup;
import com.imsweb.staging.Staging;
import com.imsweb.staging.StagingData;
import com.imsweb.staging.StagingFileDataProvider;
import com.imsweb.staging.StagingTest;
import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingSchemaOutput;
import com.imsweb.staging.entities.StagingTable;

public class TnmStagingTest extends StagingTest {

    @Override
    public String getAlgorithm() {
        return "tnm";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public StagingFileDataProvider getProvider() {
        return TnmDataProvider.getInstance(TnmDataProvider.TnmVersion.v1_0);
    }

    @BeforeClass
    public static void init() throws IOException {
        _STAGING = Staging.getInstance(TnmDataProvider.getInstance(TnmDataProvider.TnmVersion.v1_0));
    }

    @Test
    public void testBasicInitialization() {
        Assert.assertEquals(153, _STAGING.getSchemaIds().size());
        Assert.assertTrue(_STAGING.getTableIds().size() > 0);

        Assert.assertNotNull(_STAGING.getSchema("urethra"));
        Assert.assertNotNull(_STAGING.getTable("extension_bdi"));
    }

    @Test
    public void testVersionInitiaizationTypes() {
        Staging staging10 = Staging.getInstance(TnmDataProvider.getInstance(TnmDataProvider.TnmVersion.v1_0));
        Assert.assertEquals("1.0", staging10.getVersion());

        Staging stagingLatest = Staging.getInstance(TnmDataProvider.getInstance());
        Assert.assertEquals("1.0", stagingLatest.getVersion());
    }

    @Test
    public void testDescriminatorKeys() {
        Assert.assertEquals(Sets.newHashSet("ssf25"), _STAGING.getSchema("nasopharynx").getSchemaDiscriminators());
        Assert.assertEquals(Sets.newHashSet("sex"), _STAGING.getSchema("peritoneum_female_gen").getSchemaDiscriminators());
    }

    @Test
    public void testSchemaSelection() throws IOException, InterruptedException {
        // test bad values
        List<StagingSchema> lookup = _STAGING.lookupSchema(new SchemaLookup());
        Assert.assertEquals(0, lookup.size());

        lookup = _STAGING.lookupSchema(new TnmSchemaLookup("XXX", "YYY"));
        Assert.assertEquals(0, lookup.size());

        // test valid combinations that do not require a discriminator
        TnmSchemaLookup schemaLookup = new TnmSchemaLookup("C629", "9231");
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "");
        lookup = _STAGING.lookupSchema(schemaLookup);
        Assert.assertEquals(1, lookup.size());
        Assert.assertEquals("testis", lookup.get(0).getId());
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, null);
        lookup = _STAGING.lookupSchema(schemaLookup);
        Assert.assertEquals(1, lookup.size());
        Assert.assertEquals("testis", lookup.get(0).getId());

        // now test one that does do AJCC7
        lookup = _STAGING.lookupSchema(new TnmSchemaLookup("C629", "9231"));
        Assert.assertEquals(1, lookup.size());

        // test value combinations that do not require a discriminator and are supplied 988
        schemaLookup = new TnmSchemaLookup("C629", "9231");
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "988");
        lookup = _STAGING.lookupSchema(schemaLookup);
        Assert.assertEquals(1, lookup.size());
        Assert.assertEquals("testis", lookup.get(0).getId());

        // test valid combination that requires a discriminator but is not supplied one
        lookup = _STAGING.lookupSchema(new TnmSchemaLookup("C111", "8200"));
        Assert.assertEquals(2, lookup.size());
        for (StagingSchema schema : lookup)
            Assert.assertEquals(Sets.newHashSet("ssf25"), schema.getSchemaDiscriminators());

        // test valid combination that requires discriminator and a good discriminator is supplied
        schemaLookup = new TnmSchemaLookup("C111", "8200");
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "010");
        lookup = _STAGING.lookupSchema(schemaLookup);
        Assert.assertEquals(1, lookup.size());
        for (StagingSchema schema : lookup)
            Assert.assertEquals(Sets.newHashSet("ssf25"), schema.getSchemaDiscriminators());
        Assert.assertEquals("nasopharynx", lookup.get(0).getId());

        // test valid combination that requires a discriminator but is supplied a bad disciminator value
        schemaLookup = new TnmSchemaLookup("C111", "8200");
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "999");
        lookup = _STAGING.lookupSchema(schemaLookup);
        Assert.assertEquals(0, lookup.size());

        // test searching on only site
        lookup = _STAGING.lookupSchema(new TnmSchemaLookup("C401", null));
        Assert.assertEquals(5, lookup.size());

        // test searching on only hist
        lookup = _STAGING.lookupSchema(new TnmSchemaLookup(null, "9702"));
        Assert.assertEquals(2, lookup.size());

        // test that searching on only ssf25 returns no results
        schemaLookup = new TnmSchemaLookup(null, null);
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "001");
        lookup = _STAGING.lookupSchema(schemaLookup);
        Assert.assertEquals(0, lookup.size());
        schemaLookup = new TnmSchemaLookup("", null);
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "001");
        lookup = _STAGING.lookupSchema(schemaLookup);
        Assert.assertEquals(0, lookup.size());
        schemaLookup = new TnmSchemaLookup(null, "");
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "001");
        lookup = _STAGING.lookupSchema(schemaLookup);
        Assert.assertEquals(0, lookup.size());
    }

    @Test
    public void testLookupCache() {
        // do the same lookup twice
        List<StagingSchema> lookup = _STAGING.lookupSchema(new TnmSchemaLookup("C629", "9231"));
        Assert.assertEquals(1, lookup.size());
        Assert.assertEquals("testis", lookup.get(0).getId());

        lookup = _STAGING.lookupSchema(new TnmSchemaLookup("C629", "9231"));
        Assert.assertEquals(1, lookup.size());
        Assert.assertEquals("testis", lookup.get(0).getId());

        // now invalidate the cache
        TnmDataProvider.getInstance(TnmDataProvider.TnmVersion.v1_0).invalidateCache();

        // try the lookup again
        lookup = _STAGING.lookupSchema(new TnmSchemaLookup("C629", "9231"));
        Assert.assertEquals(1, lookup.size());
        Assert.assertEquals("testis", lookup.get(0).getId());
    }

    @Test
    public void testFindTableRow() {
        Assert.assertNull(_STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "cZ"));
        Assert.assertNull(_STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "c9"));

        // null maps to blank
        Assert.assertEquals(Integer.valueOf(7), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", null));

        Assert.assertEquals(Integer.valueOf(0), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "cX"));
        Assert.assertEquals(Integer.valueOf(1), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "c0"));
        Assert.assertEquals(Integer.valueOf(2), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "c1"));
        Assert.assertEquals(Integer.valueOf(3), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "c2"));
        Assert.assertEquals(Integer.valueOf(4), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "c3"));
        Assert.assertEquals(Integer.valueOf(5), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "c4"));

        Map<String, String> context = new HashMap<>();
        context.put("clin_t", "c4");
        Assert.assertEquals(Integer.valueOf(5), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", context));

        // test a table that has multiple inputs
        context = new HashMap<>();
        context.put("ss2017_t", "L");
        context.put("ss2017_n", "RE");
        context.put("ss2017_m", "D");
        Assert.assertEquals(Integer.valueOf(136), _STAGING.findMatchingTableRow("summary_stage_rpa", context));
    }

    @Test
    public void testInputBuilder() {
        TnmStagingData data1 = new TnmStagingData();
        data1.setInput(TnmStagingData.TnmInput.PRIMARY_SITE, "C680");
        data1.setInput(TnmStagingData.TnmInput.HISTOLOGY, "8000");
        data1.setInput(TnmStagingData.TnmInput.BEHAVIOR, "3");
        data1.setInput(TnmStagingData.TnmInput.GRADE, "9");
        data1.setInput(TnmStagingData.TnmInput.DX_YEAR, "2013");
        data1.setInput(TnmStagingData.TnmInput.SEER_PRIMARY_TUMOR, "100");
        data1.setInput(TnmStagingData.TnmInput.SEER_REGIONAL_NODES, "100");
        data1.setInput(TnmStagingData.TnmInput.REGIONAL_NODES_POSITIVE, "99");
        data1.setInput(TnmStagingData.TnmInput.SEER_METS, "10");
        data1.setInput(TnmStagingData.TnmInput.AGE_AT_DX, "060");
        data1.setInput(TnmStagingData.TnmInput.SEX, "1");
        data1.setInput(TnmStagingData.TnmInput.RX_SUMM_SURGERY, "8");
        data1.setInput(TnmStagingData.TnmInput.RX_SUMM_RADIATION, "9");
        data1.setInput(TnmStagingData.TnmInput.CLIN_T, "1");
        data1.setInput(TnmStagingData.TnmInput.CLIN_N, "2");
        data1.setInput(TnmStagingData.TnmInput.CLIN_M, "3");
        data1.setInput(TnmStagingData.TnmInput.PATH_T, "4");
        data1.setInput(TnmStagingData.TnmInput.PATH_N, "5");
        data1.setInput(TnmStagingData.TnmInput.PATH_M, "6");
        data1.setSsf(1, "020");

        TnmStagingData data2 = new TnmStagingData.TnmStagingInputBuilder().withInput(TnmStagingData.TnmInput.PRIMARY_SITE, "C680").withInput(TnmStagingData.TnmInput.HISTOLOGY, "8000").withInput(
                TnmStagingData.TnmInput.BEHAVIOR, "3").withInput(TnmStagingData.TnmInput.GRADE, "9").withInput(TnmStagingData.TnmInput.DX_YEAR, "2013").withInput(
                TnmStagingData.TnmInput.SEER_PRIMARY_TUMOR, "100").withInput(TnmStagingData.TnmInput.SEER_REGIONAL_NODES, "100").withInput(TnmStagingData.TnmInput.REGIONAL_NODES_POSITIVE, "99")
                .withInput(TnmStagingData.TnmInput.SEER_METS, "10").withInput(TnmStagingData.TnmInput.AGE_AT_DX, "060").withInput(TnmStagingData.TnmInput.SEX, "1").withInput(
                        TnmStagingData.TnmInput.RX_SUMM_SURGERY, "8").withInput(TnmStagingData.TnmInput.RX_SUMM_RADIATION, "9").withInput(TnmStagingData.TnmInput.CLIN_T, "1").withInput(
                        TnmStagingData.TnmInput.CLIN_N, "2").withInput(TnmStagingData.TnmInput.CLIN_M, "3").withInput(TnmStagingData.TnmInput.PATH_T, "4").withInput(TnmStagingData.TnmInput.PATH_N,
                        "5").withInput(TnmStagingData.TnmInput.PATH_M, "6").withSsf(1, "020").build();

        Assert.assertEquals(data1.getInput(), data2.getInput());
    }

    @Test
    public void testStageUrethra() {
        TnmStagingData data = new TnmStagingData();
        data.setInput(TnmStagingData.TnmInput.PRIMARY_SITE, "C680");
        data.setInput(TnmStagingData.TnmInput.HISTOLOGY, "8000");
        data.setInput(TnmStagingData.TnmInput.BEHAVIOR, "3");
        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "2016");
        data.setInput(TnmStagingData.TnmInput.RX_SUMM_SURGERY, "2");
        data.setInput(TnmStagingData.TnmInput.RX_SUMM_RADIATION, "4");
        data.setInput(TnmStagingData.TnmInput.REGIONAL_NODES_POSITIVE, "02");
        data.setInput(TnmStagingData.TnmInput.CLIN_T, "c0");
        data.setInput(TnmStagingData.TnmInput.CLIN_N, "c1");
        data.setInput(TnmStagingData.TnmInput.CLIN_M, "c0");
        data.setInput(TnmStagingData.TnmInput.PATH_T, "p0");
        data.setInput(TnmStagingData.TnmInput.PATH_N, "p1");
        data.setInput(TnmStagingData.TnmInput.PATH_M, "p1");

        // perform the staging
        _STAGING.stage(data);

        Assert.assertEquals(StagingData.Result.STAGED, data.getResult());
        Assert.assertEquals("urethra", data.getSchemaId());
        Assert.assertEquals(0, data.getErrors().size());
        Assert.assertEquals(29, data.getPath().size());
        Assert.assertEquals(10, data.getOutput().size());

        // check outputs
        Assert.assertEquals("1.0", data.getOutput(TnmStagingData.TnmOutput.DERIVED_VERSION));
        Assert.assertEquals("3", data.getOutput(TnmStagingData.TnmOutput.CLIN_STAGE_GROUP));
        Assert.assertEquals("4", data.getOutput(TnmStagingData.TnmOutput.PATH_STAGE_GROUP));
        Assert.assertEquals("4", data.getOutput(TnmStagingData.TnmOutput.COMBINED_STAGE_GROUP));
        Assert.assertEquals("c0", data.getOutput(TnmStagingData.TnmOutput.COMBINED_T));
        Assert.assertEquals("1", data.getOutput(TnmStagingData.TnmOutput.SOURCE_T));
        Assert.assertEquals("c1", data.getOutput(TnmStagingData.TnmOutput.COMBINED_N));
        Assert.assertEquals("1", data.getOutput(TnmStagingData.TnmOutput.SOURCE_N));
        Assert.assertEquals("p1", data.getOutput(TnmStagingData.TnmOutput.COMBINED_M));
        Assert.assertEquals("2", data.getOutput(TnmStagingData.TnmOutput.SOURCE_M));
        //        Assert.assertEquals("D", data.getOutput(TnmStagingData.TnmOutput.SS2017));
        //        Assert.assertEquals("U", data.getOutput(TnmStagingData.TnmOutput.SS2017_T));
        //        Assert.assertEquals("RN", data.getOutput(TnmStagingData.TnmOutput.SS2017_N));
        //        Assert.assertEquals("D", data.getOutput(TnmStagingData.TnmOutput.SS2017_M));
    }

    @Test
    public void testBadLookupInStage() {
        TnmStagingData data = new TnmStagingData();

        // if site/hist are not supplied, no lookup
        _STAGING.stage(data);
        Assert.assertEquals(StagingData.Result.FAILED_MISSING_SITE_OR_HISTOLOGY, data.getResult());

        // add hist only and it should fail with same result
        data.setInput(TnmStagingData.TnmInput.PRIMARY_SITE, "C489");
        _STAGING.stage(data);
        Assert.assertEquals(StagingData.Result.FAILED_MISSING_SITE_OR_HISTOLOGY, data.getResult());

        // put a site/hist combo that doesn't match a schema
        data.setInput(TnmStagingData.TnmInput.HISTOLOGY, "9898");
        _STAGING.stage(data);
        Assert.assertEquals(StagingData.Result.FAILED_NO_MATCHING_SCHEMA, data.getResult());

        // now a site/hist that returns multiple results
        data.setInput(TnmStagingData.TnmInput.PRIMARY_SITE, "C111");
        data.setInput(TnmStagingData.TnmInput.HISTOLOGY, "8200");
        _STAGING.stage(data);
        Assert.assertEquals(StagingData.Result.FAILED_MULITPLE_MATCHING_SCHEMAS, data.getResult());
    }

    @Test
    public void testInvolvedTables() {
        Set<String> tables = _STAGING.getInvolvedTables("adnexa_uterine_other");

        Assert.assertEquals(
                Sets.newHashSet("extension_bcn", "histology", "nodes_dcc", "primary_site", "schema_selection_adnexa_uterine_other", "seer_mets_48348", "summary_stage_rpa", "year_dx_validation"),
                tables);
    }

    @Test
    public void testInvolvedSchemas() {
        Set<String> schemas = _STAGING.getInvolvedSchemas("ssf1_jpd");

        Assert.assertEquals(new HashSet<>(Arrays.asList("kidney_renal_pelvis", "bladder", "urethra")), schemas);
    }

    @Test
    public void testGetInputs() {
        Assert.assertEquals(Sets.newHashSet("site", "hist", "seer_primary_tumor", "seer_nodes", "seer_mets"), _STAGING.getInputs(_STAGING.getSchema("adnexa_uterine_other")));

        Assert.assertEquals(
                Sets.newHashSet("site", "hist", "behavior", "systemic_surg_seq", "radiation_surg_seq", "nodes_pos", "clin_t", "clin_n", "clin_m", "path_t", "path_n", "path_m", "seer_primary_tumor",
                        "seer_nodes", "seer_mets", "ssf13", "ssf15", "ssf16", "clin_stage_group_direct", "path_stage_group_direct"), _STAGING.getInputs(_STAGING.getSchema("testis")));

        // test with and without context
        Assert.assertEquals(
                Sets.newHashSet("site", "hist", "systemic_surg_seq", "radiation_surg_seq", "nodes_pos", "clin_t", "clin_n", "clin_m", "path_t", "path_n", "path_m", "seer_primary_tumor", "seer_nodes",
                        "seer_mets", "ssf1", "ssf8", "ssf10", "clin_stage_group_direct", "path_stage_group_direct"), _STAGING.getInputs(_STAGING.getSchema("prostate")));

        Map<String, String> context = new HashMap<>();
        context.put(StagingData.PRIMARY_SITE_KEY, "C619");
        context.put(StagingData.HISTOLOGY_KEY, "8120");
        context.put(StagingData.YEAR_DX_KEY, "2004");

        // for that context, only summary stage is calculated
        Assert.assertEquals(Sets.newHashSet("site", "hist", "seer_primary_tumor", "seer_nodes", "seer_mets"), _STAGING.getInputs(_STAGING.getSchema("prostate"), context));
    }

    @Test
    public void testIsCodeValid() {
        // test bad parameters for schema or field
        Assert.assertFalse(_STAGING.isCodeValid("bad_schema_name", "site", "C509"));
        Assert.assertFalse(_STAGING.isCodeValid("testis", "bad_field_name", "C509"));

        // test null values
        Assert.assertFalse(_STAGING.isCodeValid(null, null, null));
        Assert.assertFalse(_STAGING.isCodeValid("urethra", null, null));
        Assert.assertFalse(_STAGING.isCodeValid("urethra", "site", null));

        // test fields that have a "value" specified
        Assert.assertFalse(_STAGING.isCodeValid("urethra", "year_dx", null));
        Assert.assertFalse(_STAGING.isCodeValid("urethra", "year_dx", "200"));
        Assert.assertFalse(_STAGING.isCodeValid("urethra", "year_dx", "2003"));
        Assert.assertFalse(_STAGING.isCodeValid("urethra", "year_dx", "2145"));
        Assert.assertTrue(_STAGING.isCodeValid("urethra", "year_dx", "2016"));

        // test valid and invalid fields
        Assert.assertTrue(_STAGING.isCodeValid("urethra", "seer_primary_tumor", "100"));
        Assert.assertFalse(_STAGING.isCodeValid("urethra", "seer_primary_tumor", "150"));
        Assert.assertTrue(_STAGING.isCodeValid("urethra", "ssf1", "020"));
        Assert.assertFalse(_STAGING.isCodeValid("urethra", "ssf1", "030"));
    }

    @Test
    public void testIsContextValid() {
        TnmStagingData data = new TnmStagingData();

        data.setInput(Staging.CTX_YEAR_CURRENT, "2016");

        // test valid year
        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "2016");
        Assert.assertTrue(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput()));

        // test invalid year
        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "2014");
        Assert.assertFalse(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput()));
    }

    @Test
    public void testGetSchemaIds() {
        Set<String> algorithms = _STAGING.getSchemaIds();

        Assert.assertTrue(algorithms.size() > 0);
        Assert.assertTrue(algorithms.contains("testis"));
    }

    @Test
    public void testGetTableIds() {
        Set<String> tables = _STAGING.getTableIds();

        Assert.assertTrue(tables.size() > 0);
        Assert.assertTrue(tables.contains("determine_default_n"));
    }

    @Test
    public void testGetSchema() {
        Assert.assertNull(_STAGING.getSchema("bad_schema_name"));
        Assert.assertNotNull(_STAGING.getSchema("brain"));
        Assert.assertEquals("Brain", _STAGING.getSchema("brain").getName());
    }

    @Test
    public void testLookupInputs() {
        // test valid combinations that do not require a discriminator
        StagingSchema schema = _STAGING.getSchema("prostate");
        TnmSchemaLookup lookup = new TnmSchemaLookup("C619", "8000");
        Assert.assertTrue(_STAGING.getInputs(schema, lookup.getInputs()).contains("clin_t"));

        lookup = new TnmSchemaLookup("C619", "8120");
        Assert.assertFalse(_STAGING.getInputs(schema, lookup.getInputs()).contains("clin_t"));
    }

    @Test
    public void testLookupOutputs() {
        TnmSchemaLookup lookup = new TnmSchemaLookup("C680", "8590");
        List<StagingSchema> lookups = _STAGING.lookupSchema(lookup);
        Assert.assertEquals(1, lookups.size());

        StagingSchema schema = _STAGING.getSchema(lookups.get(0).getId());
        Assert.assertEquals("urethra", schema.getId());

        // build list of output keys
        Set<String> definedOutputs = new HashSet<>();
        for (StagingSchemaOutput output : schema.getOutputs())
            definedOutputs.add(output.getKey());

        // test without context
        Assert.assertEquals(definedOutputs, _STAGING.getOutputs(schema));

        // test with context
        Assert.assertEquals(definedOutputs, _STAGING.getOutputs(schema, lookup.getInputs()));
    }

    @Test
    public void testRangeParsing() {
        StagingTable table = _STAGING.getTable("path_n_daj");

        Assert.assertNotNull(table);
        Assert.assertEquals("p0I-", table.getRawRows().get(2).get(0));
        Assert.assertEquals("p0I-", table.getTableRows().get(2).getInputs().get("path_n").get(0).getLow());
        Assert.assertEquals("p0I-", table.getTableRows().get(2).getInputs().get("path_n").get(0).getHigh());
    }

}
