/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.tnm;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.imsweb.staging.Staging;
import com.imsweb.staging.StagingDataProvider;
import com.imsweb.staging.StagingFileDataProvider;
import com.imsweb.staging.StagingTest;
import com.imsweb.staging.entities.Input;
import com.imsweb.staging.entities.Output;
import com.imsweb.staging.entities.Schema;
import com.imsweb.staging.entities.SchemaLookup;
import com.imsweb.staging.entities.StagingData;
import com.imsweb.staging.entities.StagingData.Result;
import com.imsweb.staging.entities.Table;
import com.imsweb.staging.entities.impl.StagingMetadata;
import com.imsweb.staging.tnm.TnmDataProvider.TnmVersion;
import com.imsweb.staging.tnm.TnmStagingData.TnmOutput;

class TnmStagingTest extends StagingTest {

    @BeforeAll
    public static void init() {
        _STAGING = Staging.getInstance(TnmDataProvider.getInstance(TnmDataProvider.TnmVersion.LATEST));
    }

    @Override
    public String getAlgorithm() {
        return "tnm";
    }

    @Override
    public String getVersion() {
        return TnmVersion.V1_9.getVersion();
    }

    @Override
    public StagingFileDataProvider getProvider() {
        return TnmDataProvider.getInstance(TnmDataProvider.TnmVersion.LATEST);
    }

    @Test
    void testBasicInitialization() {
        Assertions.assertEquals(153, _STAGING.getSchemaIds().size());
        Assertions.assertTrue(_STAGING.getTableIds().size() > 0);

        Assertions.assertNotNull(_STAGING.getSchema("urethra"));
        Assertions.assertNotNull(_STAGING.getTable("ssf4_mpn"));
    }

    @Test
    void testVersionInitializationTypes() {
        Staging staging10 = Staging.getInstance(TnmDataProvider.getInstance(TnmDataProvider.TnmVersion.V1_9));
        Assertions.assertEquals(TnmVersion.LATEST.getVersion(), staging10.getVersion());

        Staging stagingLatest = Staging.getInstance(TnmDataProvider.getInstance());
        Assertions.assertEquals(TnmVersion.LATEST.getVersion(), stagingLatest.getVersion());
    }

    @Test
    void testDescriminatorKeys() {
        Assertions.assertEquals(new HashSet<>(Collections.singletonList("ssf25")), _STAGING.getSchema("nasopharynx").getSchemaDiscriminators());
        Assertions.assertEquals(new HashSet<>(Collections.singletonList("sex")), _STAGING.getSchema("peritoneum_female_gen").getSchemaDiscriminators());
    }

    @Test
    void testSchemaSelection() {
        // test bad values
        List<Schema> lookup = _STAGING.lookupSchema(new SchemaLookup());
        Assertions.assertEquals(0, lookup.size());

        lookup = _STAGING.lookupSchema(new TnmSchemaLookup("XXX", "YYY"));
        Assertions.assertEquals(0, lookup.size());

        // test valid combinations that do not require a discriminator
        TnmSchemaLookup schemaLookup = new TnmSchemaLookup("C629", "9231");
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "");
        lookup = _STAGING.lookupSchema(schemaLookup);
        Assertions.assertEquals(1, lookup.size());
        Assertions.assertEquals("testis", lookup.get(0).getId());
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, null);
        lookup = _STAGING.lookupSchema(schemaLookup);
        Assertions.assertEquals(1, lookup.size());
        Assertions.assertEquals("testis", lookup.get(0).getId());

        // now test one that does do AJCC7
        lookup = _STAGING.lookupSchema(new TnmSchemaLookup("C629", "9231"));
        Assertions.assertEquals(1, lookup.size());

        // test value combinations that do not require a discriminator and are supplied 988
        schemaLookup = new TnmSchemaLookup("C629", "9231");
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "988");
        lookup = _STAGING.lookupSchema(schemaLookup);
        Assertions.assertEquals(1, lookup.size());
        Assertions.assertEquals("testis", lookup.get(0).getId());

        // test valid combination that requires a discriminator but is not supplied one
        lookup = _STAGING.lookupSchema(new TnmSchemaLookup("C111", "8200"));
        Assertions.assertEquals(2, lookup.size());
        for (Schema schema : lookup)
            Assertions.assertEquals(new HashSet<>(Collections.singletonList("ssf25")), schema.getSchemaDiscriminators());

        // test valid combination that requires discriminator and a good discriminator is supplied
        schemaLookup = new TnmSchemaLookup("C111", "8200");
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "010");
        lookup = _STAGING.lookupSchema(schemaLookup);
        Assertions.assertEquals(1, lookup.size());
        for (Schema schema : lookup)
            Assertions.assertEquals(new HashSet<>(Collections.singletonList("ssf25")), schema.getSchemaDiscriminators());
        Assertions.assertEquals("nasopharynx", lookup.get(0).getId());

        // test valid combination that requires a discriminator but is supplied a bad disciminator value
        schemaLookup = new TnmSchemaLookup("C111", "8200");
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "999");
        lookup = _STAGING.lookupSchema(schemaLookup);
        Assertions.assertEquals(0, lookup.size());

        // test searching on only site
        lookup = _STAGING.lookupSchema(new TnmSchemaLookup("C401", null));
        Assertions.assertEquals(5, lookup.size());

        // test searching on only hist
        lookup = _STAGING.lookupSchema(new TnmSchemaLookup(null, "9702"));
        Assertions.assertEquals(2, lookup.size());

        // test that searching on only ssf25 returns no results
        schemaLookup = new TnmSchemaLookup(null, null);
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "001");
        lookup = _STAGING.lookupSchema(schemaLookup);
        Assertions.assertEquals(0, lookup.size());
        schemaLookup = new TnmSchemaLookup("", null);
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "001");
        lookup = _STAGING.lookupSchema(schemaLookup);
        Assertions.assertEquals(0, lookup.size());
        schemaLookup = new TnmSchemaLookup(null, "");
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "001");
        lookup = _STAGING.lookupSchema(schemaLookup);
        Assertions.assertEquals(0, lookup.size());
    }

    @Test
    void testLookupCache() {
        // do the same lookup twice
        List<Schema> lookup = _STAGING.lookupSchema(new TnmSchemaLookup("C629", "9231"));
        Assertions.assertEquals(1, lookup.size());
        Assertions.assertEquals("testis", lookup.get(0).getId());

        lookup = _STAGING.lookupSchema(new TnmSchemaLookup("C629", "9231"));
        Assertions.assertEquals(1, lookup.size());
        Assertions.assertEquals("testis", lookup.get(0).getId());

        // now invalidate the cache
        TnmDataProvider.getInstance(TnmDataProvider.TnmVersion.V1_9).invalidateCache();

        // try the lookup again
        lookup = _STAGING.lookupSchema(new TnmSchemaLookup("C629", "9231"));
        Assertions.assertEquals(1, lookup.size());
        Assertions.assertEquals("testis", lookup.get(0).getId());
    }

    @Test
    void testFindTableRow() {
        Assertions.assertNull(_STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "cZ"));
        Assertions.assertNull(_STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "c9"));

        // null maps to blank
        Assertions.assertEquals(Integer.valueOf(7), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", null));

        Assertions.assertEquals(Integer.valueOf(0), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "cX"));
        Assertions.assertEquals(Integer.valueOf(1), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "c0"));
        Assertions.assertEquals(Integer.valueOf(2), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "c1"));
        Assertions.assertEquals(Integer.valueOf(3), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "c2"));
        Assertions.assertEquals(Integer.valueOf(4), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "c3"));
        Assertions.assertEquals(Integer.valueOf(5), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "c4"));

        Map<String, String> context = new HashMap<>();
        context.put("clin_t", "c4");
        Assertions.assertEquals(Integer.valueOf(5), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", context));

        // test a table that has multiple inputs
        context = new HashMap<>();
        context.put("m_prefix", "p");
        context.put("root_m", "0");
        Assertions.assertEquals(Integer.valueOf(8), _STAGING.findMatchingTableRow("concatenate_m_40642", context));
    }

    @Test
    void testInputBuilder() {
        TnmStagingData data1 = new TnmStagingData();
        data1.setInput(TnmStagingData.TnmInput.PRIMARY_SITE, "C680");
        data1.setInput(TnmStagingData.TnmInput.HISTOLOGY, "8000");
        data1.setInput(TnmStagingData.TnmInput.BEHAVIOR, "3");
        data1.setInput(TnmStagingData.TnmInput.GRADE, "9");
        data1.setInput(TnmStagingData.TnmInput.DX_YEAR, "2013");
        data1.setInput(TnmStagingData.TnmInput.REGIONAL_NODES_POSITIVE, "99");
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

        TnmStagingData data2 = new TnmStagingData.TnmStagingInputBuilder()
                .withInput(TnmStagingData.TnmInput.PRIMARY_SITE, "C680")
                .withInput(TnmStagingData.TnmInput.HISTOLOGY, "8000")
                .withInput(TnmStagingData.TnmInput.BEHAVIOR, "3")
                .withInput(TnmStagingData.TnmInput.GRADE, "9")
                .withInput(TnmStagingData.TnmInput.DX_YEAR, "2013")
                .withInput(TnmStagingData.TnmInput.REGIONAL_NODES_POSITIVE, "99")
                .withInput(TnmStagingData.TnmInput.AGE_AT_DX, "060")
                .withInput(TnmStagingData.TnmInput.SEX, "1")
                .withInput(TnmStagingData.TnmInput.RX_SUMM_SURGERY, "8")
                .withInput(TnmStagingData.TnmInput.RX_SUMM_RADIATION, "9")
                .withInput(TnmStagingData.TnmInput.CLIN_T, "1")
                .withInput(TnmStagingData.TnmInput.CLIN_N, "2")
                .withInput(TnmStagingData.TnmInput.CLIN_M, "3")
                .withInput(TnmStagingData.TnmInput.PATH_T, "4")
                .withInput(TnmStagingData.TnmInput.PATH_N, "5")
                .withInput(TnmStagingData.TnmInput.PATH_M, "6")
                .withSsf(1, "020").build();

        Assertions.assertEquals(data1.getInput(), data2.getInput());
    }

    @Test
    void testStageUrethra() {
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

        Assertions.assertEquals(Result.STAGED, data.getResult());
        Assertions.assertEquals("urethra", data.getSchemaId());
        Assertions.assertEquals(0, data.getErrors().size());
        Assertions.assertEquals(25, data.getPath().size());
        Assertions.assertEquals(10, data.getOutput().size());

        // check outputs
        Assertions.assertEquals(TnmVersion.LATEST.getVersion(), data.getOutput(TnmOutput.DERIVED_VERSION));
        Assertions.assertEquals("3", data.getOutput(TnmOutput.CLIN_STAGE_GROUP));
        Assertions.assertEquals("4", data.getOutput(TnmOutput.PATH_STAGE_GROUP));
        Assertions.assertEquals("4", data.getOutput(TnmOutput.COMBINED_STAGE_GROUP));
        Assertions.assertEquals("c0", data.getOutput(TnmOutput.COMBINED_T));
        Assertions.assertEquals("1", data.getOutput(TnmOutput.SOURCE_T));
        Assertions.assertEquals("c1", data.getOutput(TnmOutput.COMBINED_N));
        Assertions.assertEquals("1", data.getOutput(TnmOutput.SOURCE_N));
        Assertions.assertEquals("p1", data.getOutput(TnmOutput.COMBINED_M));
        Assertions.assertEquals("2", data.getOutput(TnmOutput.SOURCE_M));
    }

    @Test
    void testBadLookupInStage() {
        TnmStagingData data = new TnmStagingData();

        // if site/hist are not supplied, no lookup
        _STAGING.stage(data);
        Assertions.assertEquals(Result.FAILED_MISSING_SITE_OR_HISTOLOGY, data.getResult());

        // add hist only and it should fail with same result
        data.setInput(TnmStagingData.TnmInput.PRIMARY_SITE, "C489");
        _STAGING.stage(data);
        Assertions.assertEquals(Result.FAILED_MISSING_SITE_OR_HISTOLOGY, data.getResult());

        // put a site/hist combo that doesn't match a schema
        data.setInput(TnmStagingData.TnmInput.HISTOLOGY, "9898");
        _STAGING.stage(data);
        Assertions.assertEquals(Result.FAILED_NO_MATCHING_SCHEMA, data.getResult());

        // now a site/hist that returns multiple results
        data.setInput(TnmStagingData.TnmInput.PRIMARY_SITE, "C111");
        data.setInput(TnmStagingData.TnmInput.HISTOLOGY, "8200");
        _STAGING.stage(data);
        Assertions.assertEquals(Result.FAILED_MULITPLE_MATCHING_SCHEMAS, data.getResult());
    }

    @Test
    void testInvolvedTables() {
        Set<String> tables = _STAGING.getInvolvedTables("adnexa_uterine_other");

        Assertions.assertEquals(new HashSet<>(Arrays.asList("primary_site", "histology", "schema_selection_adnexa_uterine_other", "year_dx_validation")), tables);
    }

    @Test
    void testInvolvedSchemas() {
        Set<String> schemas = _STAGING.getInvolvedSchemas("ssf1_jpd");

        Assertions.assertEquals(new HashSet<>(Arrays.asList("kidney_renal_pelvis", "bladder", "urethra")), schemas);
    }

    @Test
    void testGetInputs() {
        Assertions.assertEquals(new HashSet<>(Arrays.asList("site", "hist")), _STAGING.getInputs(_STAGING.getSchema("adnexa_uterine_other")));

        Assertions.assertEquals(new HashSet<>(Arrays.asList("site", "hist", "behavior", "systemic_surg_seq", "radiation_surg_seq", "nodes_pos", "clin_t", "clin_n", "clin_m",
                "path_t", "path_n", "path_m", "ssf13", "ssf15", "ssf16", "clin_stage_group_direct",
                "path_stage_group_direct")), _STAGING.getInputs(_STAGING.getSchema("testis")));

        // test with and without context
        Assertions.assertEquals(new HashSet<>(Arrays.asList("site", "hist", "systemic_surg_seq", "radiation_surg_seq", "nodes_pos", "clin_t", "clin_n", "clin_m", "path_t",
                "path_n", "path_m", "ssf1", "ssf8", "ssf10", "clin_stage_group_direct",
                "path_stage_group_direct")), _STAGING.getInputs(_STAGING.getSchema("prostate")));

        Map<String, String> context = new HashMap<>();
        context.put(StagingData.PRIMARY_SITE_KEY, "C619");
        context.put(StagingData.HISTOLOGY_KEY, "8120");
        context.put(StagingData.YEAR_DX_KEY, "2004");

        // for that context, only summary stage is calculated
        Assertions.assertEquals(new HashSet<>(Arrays.asList("site", "hist")), _STAGING.getInputs(_STAGING.getSchema("prostate"), context));
    }

    @Test
    void testIsCodeValid() {
        // test bad parameters for schema or field
        Assertions.assertFalse(_STAGING.isCodeValid("bad_schema_name", "site", "C509"));
        Assertions.assertFalse(_STAGING.isCodeValid("testis", "bad_field_name", "C509"));

        // test null values
        Assertions.assertFalse(_STAGING.isCodeValid(null, null, null));
        Assertions.assertFalse(_STAGING.isCodeValid("urethra", null, null));
        Assertions.assertFalse(_STAGING.isCodeValid("urethra", "site", null));

        // test fields that have a "value" specified
        Assertions.assertFalse(_STAGING.isCodeValid("urethra", "year_dx", null));
        Assertions.assertFalse(_STAGING.isCodeValid("urethra", "year_dx", "200"));
        Assertions.assertFalse(_STAGING.isCodeValid("urethra", "year_dx", "2003"));
        Assertions.assertFalse(_STAGING.isCodeValid("urethra", "year_dx", "2145"));
        Assertions.assertTrue(_STAGING.isCodeValid("urethra", "year_dx", "2016"));

        // test valid and invalid fields
        Assertions.assertTrue(_STAGING.isCodeValid("urethra", "clin_t", "c4"));
        Assertions.assertFalse(_STAGING.isCodeValid("urethra", "clin_t", "c5"));
        Assertions.assertTrue(_STAGING.isCodeValid("urethra", "ssf1", "020"));
        Assertions.assertFalse(_STAGING.isCodeValid("urethra", "ssf1", "030"));
    }

    @Test
    void testIsContextValid() {
        TnmStagingData data = new TnmStagingData();

        data.setInput(Staging.CTX_YEAR_CURRENT, "2016");

        // test valid year
        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "2016");
        Assertions.assertTrue(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput()));

        // test invalid year
        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "2014");
        Assertions.assertFalse(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput()));
    }

    @Test
    void testGetSchemaIds() {
        Set<String> algorithms = _STAGING.getSchemaIds();

        Assertions.assertTrue(algorithms.size() > 0);
        Assertions.assertTrue(algorithms.contains("testis"));
    }

    @Test
    void testGetTableIds() {
        Set<String> tables = _STAGING.getTableIds();

        Assertions.assertTrue(tables.size() > 0);
        Assertions.assertTrue(tables.contains("determine_default_n"));
    }

    @Test
    void testGetSchema() {
        Assertions.assertNull(_STAGING.getSchema("bad_schema_name"));
        Assertions.assertNotNull(_STAGING.getSchema("brain"));
        Assertions.assertEquals("Brain", _STAGING.getSchema("brain").getName());
    }

    @Test
    void testLookupInputs() {
        // test valid combinations that do not require a discriminator
        Schema schema = _STAGING.getSchema("prostate");
        TnmSchemaLookup lookup = new TnmSchemaLookup("C619", "8000");
        Assertions.assertTrue(_STAGING.getInputs(schema, lookup.getInputs()).contains("clin_t"));

        lookup = new TnmSchemaLookup("C619", "8120");
        Assertions.assertFalse(_STAGING.getInputs(schema, lookup.getInputs()).contains("clin_t"));
    }

    @Test
    void testLookupOutputs() {
        TnmSchemaLookup lookup = new TnmSchemaLookup("C680", "8590");
        List<Schema> lookups = _STAGING.lookupSchema(lookup);
        Assertions.assertEquals(1, lookups.size());

        Schema schema = _STAGING.getSchema(lookups.get(0).getId());
        Assertions.assertEquals("urethra", schema.getId());

        // build list of output keys
        Set<String> definedOutputs = schema.getOutputs().stream().map(Output::getKey).collect(Collectors.toSet());

        // test without context
        Assertions.assertEquals(definedOutputs, _STAGING.getOutputs(schema));

        // test with context
        Assertions.assertEquals(definedOutputs, _STAGING.getOutputs(schema, lookup.getInputs()));
    }

    @Test
    void testRangeParsing() {
        Table table = _STAGING.getTable("path_n_daj");

        Assertions.assertNotNull(table);
        Assertions.assertEquals("p0I-", table.getRawRows().get(2).get(0));
        Assertions.assertEquals("p0I-", table.getTableRows().get(2).getColumnInput("path_n").get(0).getLow());
        Assertions.assertEquals("p0I-", table.getTableRows().get(2).getColumnInput("path_n").get(0).getHigh());
    }

    @Test
    void testEncoding() {
        Table table = _STAGING.getTable("thyroid_t_6166");

        Assertions.assertNotNull(table);

        // the notes of this table contain UTF-8 characters, specifically the single quote character in this phrase: "You may use a physician’s statement"

        // converting to UTF-8 should change nothing
        Assertions.assertEquals(table.getNotes(), new String(table.getNotes().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        // converting to other encoding should change the text
        Assertions.assertNotEquals(table.getNotes(), new String(table.getNotes().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1));
        Assertions.assertNotEquals(table.getNotes(), new String(table.getNotes().getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII));
    }

    @Test
    void testMetadata() {
        Schema urethra = _STAGING.getSchema("urethra");
        Assertions.assertNotNull(urethra);

        Input ssf1 = urethra.getInputMap().get("ssf1");
        Assertions.assertNotNull(ssf1);

        Assertions.assertEquals(3, ssf1.getMetadata().size());
        Assertions.assertTrue(ssf1.getMetadata().contains(new StagingMetadata("COC_REQUIRED")));
        Assertions.assertTrue(ssf1.getMetadata().contains(new StagingMetadata("CCCR_REQUIRED")));
        Assertions.assertTrue(ssf1.getMetadata().contains(new StagingMetadata("SEER_REQUIRED")));
    }

    @Test
    void testCachedSiteAndHistology() {
        StagingDataProvider provider = getProvider();
        Assertions.assertTrue(provider.getValidSites().size() > 0);
        Assertions.assertTrue(provider.getValidHistologies().size() > 0);

        // site tests
        List<String> validSites = Arrays.asList("C000", "C809");
        List<String> invalidSites = Arrays.asList("C727", "C810");
        for (String site : validSites)
            Assertions.assertTrue(provider.getValidSites().contains(site));
        for (String site : invalidSites)
            Assertions.assertFalse(provider.getValidSites().contains(site));

        // hist tests
        List<String> validHist = Arrays.asList("8000", "8002", "8005", "8290", "9992");
        List<String> invalidHist = Arrays.asList("8006", "9993");
        for (String hist : validHist)
            Assertions.assertTrue(provider.getValidHistologies().contains(hist));
        for (String hist : invalidHist)
            Assertions.assertFalse(provider.getValidHistologies().contains(hist));
    }

}
