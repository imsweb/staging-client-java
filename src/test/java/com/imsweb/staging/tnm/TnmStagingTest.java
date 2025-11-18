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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TnmStagingTest extends StagingTest {

    @BeforeAll
    static void init() {
        _STAGING = Staging.getInstance(TnmDataProvider.getInstance(TnmVersion.LATEST));
    }

    @Override
    public String getAlgorithm() {
        return "tnm";
    }

    @Override
    public String getVersion() {
        return TnmVersion.V2_0.getVersion();
    }

    @Override
    public StagingFileDataProvider getProvider() {
        return TnmDataProvider.getInstance(TnmVersion.LATEST);
    }

    @Test
    void testBasicInitialization() {
        assertEquals(153, _STAGING.getSchemaIds().size());
        assertFalse(_STAGING.getTableIds().isEmpty());

        assertNotNull(_STAGING.getSchema("urethra"));
        assertNotNull(_STAGING.getTable("ssf4_mpn"));
    }

    @Test
    void testVersionInitializationTypes() {
        Staging staging10 = Staging.getInstance(TnmDataProvider.getInstance(TnmVersion.V2_0));
        assertEquals(TnmVersion.LATEST.getVersion(), staging10.getVersion());

        Staging stagingLatest = Staging.getInstance(TnmDataProvider.getInstance());
        assertEquals(TnmVersion.LATEST.getVersion(), stagingLatest.getVersion());
    }

    @Test
    void testDescriminatorKeys() {
        assertEquals(new HashSet<>(Collections.singletonList("ssf25")), _STAGING.getSchema("nasopharynx").getSchemaDiscriminators());
        assertEquals(new HashSet<>(Collections.singletonList("sex")), _STAGING.getSchema("peritoneum_female_gen").getSchemaDiscriminators());
    }

    @Test
    void testSchemaSelection() {
        // test bad values
        List<Schema> lookup = _STAGING.lookupSchema(new SchemaLookup());
        assertEquals(0, lookup.size());

        lookup = _STAGING.lookupSchema(new TnmSchemaLookup("XXX", "YYY"));
        assertEquals(0, lookup.size());

        // test valid combinations that do not require a discriminator
        TnmSchemaLookup schemaLookup = new TnmSchemaLookup("C629", "9231");
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertEquals(1, lookup.size());
        assertEquals("testis", lookup.getFirst().getId());
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, null);
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertEquals(1, lookup.size());
        assertEquals("testis", lookup.getFirst().getId());

        // now test one that does do AJCC7
        lookup = _STAGING.lookupSchema(new TnmSchemaLookup("C629", "9231"));
        assertEquals(1, lookup.size());

        // test value combinations that do not require a discriminator and are supplied 988
        schemaLookup = new TnmSchemaLookup("C629", "9231");
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "988");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertEquals(1, lookup.size());
        assertEquals("testis", lookup.getFirst().getId());

        // test valid combination that requires a discriminator but is not supplied one
        lookup = _STAGING.lookupSchema(new TnmSchemaLookup("C111", "8200"));
        assertEquals(2, lookup.size());
        for (Schema schema : lookup)
            assertEquals(new HashSet<>(Collections.singletonList("ssf25")), schema.getSchemaDiscriminators());

        // test valid combination that requires discriminator and a good discriminator is supplied
        schemaLookup = new TnmSchemaLookup("C111", "8200");
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "010");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertEquals(1, lookup.size());
        for (Schema schema : lookup)
            assertEquals(new HashSet<>(Collections.singletonList("ssf25")), schema.getSchemaDiscriminators());
        assertEquals("nasopharynx", lookup.getFirst().getId());

        // test valid combination that requires a discriminator but is supplied a bad disciminator value
        schemaLookup = new TnmSchemaLookup("C111", "8200");
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "999");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertEquals(0, lookup.size());

        // test searching on only site
        lookup = _STAGING.lookupSchema(new TnmSchemaLookup("C401", null));
        assertEquals(5, lookup.size());

        // test searching on only hist
        lookup = _STAGING.lookupSchema(new TnmSchemaLookup(null, "9702"));
        assertEquals(2, lookup.size());

        // test that searching on only ssf25 returns no results
        schemaLookup = new TnmSchemaLookup(null, null);
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "001");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertEquals(0, lookup.size());
        schemaLookup = new TnmSchemaLookup("", null);
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "001");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertEquals(0, lookup.size());
        schemaLookup = new TnmSchemaLookup(null, "");
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "001");
        lookup = _STAGING.lookupSchema(schemaLookup);
        assertEquals(0, lookup.size());
    }

    @Test
    void testLookupCache() {
        // do the same lookup twice
        List<Schema> lookup = _STAGING.lookupSchema(new TnmSchemaLookup("C629", "9231"));
        assertEquals(1, lookup.size());
        assertEquals("testis", lookup.getFirst().getId());

        lookup = _STAGING.lookupSchema(new TnmSchemaLookup("C629", "9231"));
        assertEquals(1, lookup.size());
        assertEquals("testis", lookup.getFirst().getId());

        // now invalidate the cache
        TnmDataProvider.getInstance(TnmVersion.V2_0).invalidateCache();

        // try the lookup again
        lookup = _STAGING.lookupSchema(new TnmSchemaLookup("C629", "9231"));
        assertEquals(1, lookup.size());
        assertEquals("testis", lookup.getFirst().getId());
    }

    @Test
    void testFindTableRow() {
        assertNull(_STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "cZ"));
        assertNull(_STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "c9"));

        // null maps to blank
        assertEquals(Integer.valueOf(7), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", null));

        assertEquals(Integer.valueOf(0), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "cX"));
        assertEquals(Integer.valueOf(1), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "c0"));
        assertEquals(Integer.valueOf(2), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "c1"));
        assertEquals(Integer.valueOf(3), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "c2"));
        assertEquals(Integer.valueOf(4), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "c3"));
        assertEquals(Integer.valueOf(5), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "c4"));

        Map<String, String> context = new HashMap<>();
        context.put("clin_t", "c4");
        assertEquals(Integer.valueOf(5), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", context));

        // test a table that has multiple inputs
        context = new HashMap<>();
        context.put("m_prefix", "p");
        context.put("root_m", "0");
        assertEquals(Integer.valueOf(8), _STAGING.findMatchingTableRow("concatenate_m_40642", context));
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

        assertEquals(data1.getInput(), data2.getInput());
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

        assertEquals(Result.STAGED, data.getResult());
        assertEquals("urethra", data.getSchemaId());
        assertEquals(0, data.getErrors().size());
        assertEquals(25, data.getPath().size());
        assertEquals(10, data.getOutput().size());

        // check outputs
        assertEquals(TnmVersion.LATEST.getVersion(), data.getOutput(TnmOutput.DERIVED_VERSION));
        assertEquals("3", data.getOutput(TnmOutput.CLIN_STAGE_GROUP));
        assertEquals("4", data.getOutput(TnmOutput.PATH_STAGE_GROUP));
        assertEquals("4", data.getOutput(TnmOutput.COMBINED_STAGE_GROUP));
        assertEquals("c0", data.getOutput(TnmOutput.COMBINED_T));
        assertEquals("1", data.getOutput(TnmOutput.SOURCE_T));
        assertEquals("c1", data.getOutput(TnmOutput.COMBINED_N));
        assertEquals("1", data.getOutput(TnmOutput.SOURCE_N));
        assertEquals("p1", data.getOutput(TnmOutput.COMBINED_M));
        assertEquals("2", data.getOutput(TnmOutput.SOURCE_M));
    }

    @Test
    void testBadLookupInStage() {
        TnmStagingData data = new TnmStagingData();

        // if site/hist are not supplied, no lookup
        _STAGING.stage(data);
        assertEquals(Result.FAILED_MISSING_SITE_OR_HISTOLOGY, data.getResult());

        // add hist only and it should fail with same result
        data.setInput(TnmStagingData.TnmInput.PRIMARY_SITE, "C489");
        _STAGING.stage(data);
        assertEquals(Result.FAILED_MISSING_SITE_OR_HISTOLOGY, data.getResult());

        // put a site/hist combo that doesn't match a schema
        data.setInput(TnmStagingData.TnmInput.HISTOLOGY, "9898");
        _STAGING.stage(data);
        assertEquals(Result.FAILED_NO_MATCHING_SCHEMA, data.getResult());

        // now a site/hist that returns multiple results
        data.setInput(TnmStagingData.TnmInput.PRIMARY_SITE, "C111");
        data.setInput(TnmStagingData.TnmInput.HISTOLOGY, "8200");
        _STAGING.stage(data);
        assertEquals(Result.FAILED_MULITPLE_MATCHING_SCHEMAS, data.getResult());
    }

    @Test
    void testInvolvedTables() {
        Set<String> tables = _STAGING.getInvolvedTables("adnexa_uterine_other");

        assertEquals(new HashSet<>(Arrays.asList("primary_site", "histology", "schema_selection_adnexa_uterine_other", "year_dx_validation")), tables);
    }

    @Test
    void testInvolvedSchemas() {
        Set<String> schemas = _STAGING.getInvolvedSchemas("ssf1_jpd");

        assertEquals(new HashSet<>(Arrays.asList("kidney_renal_pelvis", "bladder", "urethra")), schemas);
    }

    @Test
    void testGetInputs() {
        assertEquals(new HashSet<>(Arrays.asList("site", "hist")), _STAGING.getInputs(_STAGING.getSchema("adnexa_uterine_other")));

        assertEquals(new HashSet<>(Arrays.asList("site", "hist", "behavior", "systemic_surg_seq", "radiation_surg_seq", "nodes_pos", "clin_t", "clin_n", "clin_m",
                "path_t", "path_n", "path_m", "ssf13", "ssf15", "ssf16", "clin_stage_group_direct",
                "path_stage_group_direct")), _STAGING.getInputs(_STAGING.getSchema("testis")));

        // test with and without context
        assertEquals(new HashSet<>(Arrays.asList("site", "hist", "systemic_surg_seq", "radiation_surg_seq", "nodes_pos", "clin_t", "clin_n", "clin_m", "path_t",
                "path_n", "path_m", "ssf1", "ssf8", "ssf10", "clin_stage_group_direct",
                "path_stage_group_direct")), _STAGING.getInputs(_STAGING.getSchema("prostate")));

        Map<String, String> context = new HashMap<>();
        context.put(StagingData.PRIMARY_SITE_KEY, "C619");
        context.put(StagingData.HISTOLOGY_KEY, "8120");
        context.put(StagingData.YEAR_DX_KEY, "2004");

        // for that context, only summary stage is calculated
        assertEquals(new HashSet<>(Arrays.asList("site", "hist")), _STAGING.getInputs(_STAGING.getSchema("prostate"), context));
    }

    @Test
    void testIsCodeValid() {
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
        assertTrue(_STAGING.isCodeValid("urethra", "year_dx", "2016"));

        // test valid and invalid fields
        assertTrue(_STAGING.isCodeValid("urethra", "clin_t", "c4"));
        assertFalse(_STAGING.isCodeValid("urethra", "clin_t", "c5"));
        assertTrue(_STAGING.isCodeValid("urethra", "ssf1", "020"));
        assertFalse(_STAGING.isCodeValid("urethra", "ssf1", "030"));
    }

    @Test
    void testIsContextValid() {
        TnmStagingData data = new TnmStagingData();

        data.setInput(Staging.CTX_YEAR_CURRENT, "2016");

        // test valid year
        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "2016");
        assertTrue(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput()));

        // test invalid year
        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "2014");
        assertFalse(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput()));
    }

    @Test
    void testGetSchemaIds() {
        Set<String> algorithms = _STAGING.getSchemaIds();

        assertFalse(algorithms.isEmpty());
        assertTrue(algorithms.contains("testis"));
    }

    @Test
    void testGetTableIds() {
        Set<String> tables = _STAGING.getTableIds();

        assertFalse(tables.isEmpty());
        assertTrue(tables.contains("determine_default_n"));
    }

    @Test
    void testGetSchema() {
        assertNull(_STAGING.getSchema("bad_schema_name"));
        assertNotNull(_STAGING.getSchema("brain"));
        assertEquals("Brain", _STAGING.getSchema("brain").getName());
    }

    @Test
    void testLookupInputs() {
        // test valid combinations that do not require a discriminator
        Schema schema = _STAGING.getSchema("prostate");
        TnmSchemaLookup lookup = new TnmSchemaLookup("C619", "8000");
        assertTrue(_STAGING.getInputs(schema, lookup.getInputs()).contains("clin_t"));

        lookup = new TnmSchemaLookup("C619", "8120");
        assertFalse(_STAGING.getInputs(schema, lookup.getInputs()).contains("clin_t"));
    }

    @Test
    void testLookupOutputs() {
        TnmSchemaLookup lookup = new TnmSchemaLookup("C680", "8590");
        List<Schema> lookups = _STAGING.lookupSchema(lookup);
        assertEquals(1, lookups.size());

        Schema schema = _STAGING.getSchema(lookups.getFirst().getId());
        assertEquals("urethra", schema.getId());

        // build list of output keys
        Set<String> definedOutputs = schema.getOutputs().stream().map(Output::getKey).collect(Collectors.toSet());

        // test without context
        assertEquals(definedOutputs, _STAGING.getOutputs(schema));

        // test with context
        assertEquals(definedOutputs, _STAGING.getOutputs(schema, lookup.getInputs()));
    }

    @Test
    void testRangeParsing() {
        Table table = _STAGING.getTable("path_n_daj");

        assertNotNull(table);
        assertEquals("p0I-", table.getRawRows().get(2).getFirst());
        assertEquals("p0I-", table.getTableRows().get(2).getColumnInput("path_n").getFirst().getLow());
        assertEquals("p0I-", table.getTableRows().get(2).getColumnInput("path_n").getFirst().getHigh());
    }

    @Test
    void testEncoding() {
        Table table = _STAGING.getTable("thyroid_t_6166");

        assertNotNull(table);

        // the notes of this table contain UTF-8 characters, specifically the single quote character in this phrase: "You may use a physician’s statement"

        // converting to UTF-8 should change nothing
        assertEquals(table.getNotes(), new String(table.getNotes().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        // converting to other encoding should change the text
        assertNotEquals(table.getNotes(), new String(table.getNotes().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1));
        assertNotEquals(table.getNotes(), new String(table.getNotes().getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII));
    }

    @Test
    void testMetadata() {
        Schema urethra = _STAGING.getSchema("urethra");
        assertNotNull(urethra);

        Input ssf1 = urethra.getInputMap().get("ssf1");
        assertNotNull(ssf1);

        assertEquals(3, ssf1.getMetadata().size());
        assertTrue(ssf1.getMetadata().contains(new StagingMetadata("COC_REQUIRED")));
        assertTrue(ssf1.getMetadata().contains(new StagingMetadata("CCCR_REQUIRED")));
        assertTrue(ssf1.getMetadata().contains(new StagingMetadata("SEER_REQUIRED")));
    }

    @Test
    void testCachedSiteAndHistology() {
        StagingDataProvider provider = getProvider();
        assertFalse(provider.getValidSites().isEmpty());
        assertFalse(provider.getValidHistologies().isEmpty());

        // site tests
        List<String> validSites = Arrays.asList("C000", "C809");
        List<String> invalidSites = Arrays.asList("C727", "C810");
        for (String site : validSites)
            assertTrue(provider.getValidSites().contains(site));
        for (String site : invalidSites)
            assertFalse(provider.getValidSites().contains(site));

        // hist tests
        List<String> validHist = Arrays.asList("8000", "8002", "8005", "8290", "9992");
        List<String> invalidHist = Arrays.asList("8006", "9993");
        for (String hist : validHist)
            assertTrue(provider.getValidHistologies().contains(hist));
        for (String hist : invalidHist)
            assertFalse(provider.getValidHistologies().contains(hist));
    }

}
