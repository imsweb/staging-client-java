/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.tnm;

import java.io.IOException;
import java.util.ArrayList;
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

import com.imsweb.decisionengine.ColumnDefinition;
import com.imsweb.staging.SchemaLookup;
import com.imsweb.staging.Staging;
import com.imsweb.staging.StagingData;
import com.imsweb.staging.entities.StagingColumnDefinition;
import com.imsweb.staging.entities.StagingMapping;
import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingSchemaInput;
import com.imsweb.staging.entities.StagingTable;

public class TnmStagingTest {

    private static Staging _STAGING;

    @BeforeClass
    public static void init() throws IOException {
        _STAGING = Staging.getInstance(TnmDataProvider.getInstance(TnmDataProvider.TnmVersion.v1_0));
    }

    @Test
    public void testBasicInitialization() {
        Assert.assertEquals("tnm", _STAGING.getAlgorithm());
        Assert.assertEquals("1.0", _STAGING.getVersion());

        Assert.assertEquals(153, _STAGING.getSchemaIds().size());
        Assert.assertTrue(_STAGING.getTableIds().size() > 0);

        Assert.assertNotNull(_STAGING.getSchema("urethra"));
        Assert.assertNotNull(_STAGING.getTable("extension_bdi"));

        // all inputs for all schemas will have null unit and decimal places
        for (String id : _STAGING.getSchemaIds()) {
            StagingSchema schema = _STAGING.getSchema(id);
            for (StagingSchemaInput input : schema.getInputs()) {
                Assert.assertNull("No TNM schemas should have units", input.getUnit());
                Assert.assertNull("No TNM schemas should have decimal places", input.getDecimalPlaces());
            }
        }
    }

    @Test
    public void testVersionInitiaizationTypes() {
        Staging staging10 = Staging.getInstance(TnmDataProvider.getInstance(TnmDataProvider.TnmVersion.v1_0));
        Assert.assertEquals("1.0", staging10.getVersion());

        Staging stagingLatest = Staging.getInstance(TnmDataProvider.getInstance());
        Assert.assertEquals("1.0", stagingLatest.getVersion());
    }

    @Test
    public void testInitAllTables() {
        for (String id : _STAGING.getTableIds()) {
            StagingTable table = _STAGING.getTable(id);

            Assert.assertNotNull(table);
            Assert.assertNotNull(table.getAlgorithm());
            Assert.assertNotNull(table.getVersion());
            Assert.assertNotNull(table.getName());
        }
    }

    @Test
    public void testValidSite() {
        Assert.assertFalse(_STAGING.isValidSite(null));
        Assert.assertFalse(_STAGING.isValidSite(""));
        Assert.assertFalse(_STAGING.isValidSite("C21"));
        Assert.assertFalse(_STAGING.isValidSite("C115"));

        Assert.assertTrue(_STAGING.isValidSite("C509"));
    }

    @Test
    public void testValidHistology() {
        Assert.assertFalse(_STAGING.isValidHistology(null));
        Assert.assertFalse(_STAGING.isValidHistology(""));
        Assert.assertFalse(_STAGING.isValidHistology("810"));
        Assert.assertFalse(_STAGING.isValidHistology("8176"));

        Assert.assertTrue(_STAGING.isValidHistology("8000"));
        Assert.assertTrue(_STAGING.isValidHistology("8201"));
    }

    @Test
    public void testValidCode() {
        Map<String, String> context = new HashMap<String, String>();
        context.put("hist", "8000");
        Assert.assertTrue(_STAGING.isContextValid("prostate", "hist", context));
        context.put("hist", "8542");
        Assert.assertTrue(_STAGING.isContextValid("prostate", "hist", context));
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

        // test specific failure case:  Line #1995826 [C695,9701,100,lacrimal_gland] --> The schema selection should have found a schema, lacrimal_gland, but did not.
        schemaLookup = new TnmSchemaLookup("C695", "9701");
        schemaLookup.setInput(TnmStagingData.SSF25_KEY, "100");
        lookup = _STAGING.lookupSchema(schemaLookup);
        Assert.assertEquals(1, lookup.size());
        Assert.assertEquals("lacrimal_gland", lookup.get(0).getId());

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
    public void testSchemaSelectionIntegration() throws IOException, InterruptedException {
        // TODO need to tweak this since the disciminator is not always SSF25 
        // test complete file of cases
        //        IntegrationUtils.IntegrationResult result = IntegrationUtils.processSchemaSelection(_STAGING, "cs_schema_identification_unit_test.txt.gz",
        //                new GZIPInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("test-data/cs/020550/cs_schema_identification_unit_test.txt.gz")));
        //        Assert.assertEquals(0, result.getNumFailures());
    }

    @Test
    public void testFindTableRow() {
        Assert.assertNull(_STAGING.findMatchingTableRow("adrenal_gland_t_18187", "size", null));
        Assert.assertNull(_STAGING.findMatchingTableRow("adrenal_gland_t_18187", "size", "Z"));
        Assert.assertNull(_STAGING.findMatchingTableRow("adrenal_gland_t_18187", "size", "9"));

        Assert.assertEquals(Integer.valueOf(0), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "X"));
        Assert.assertEquals(Integer.valueOf(1), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "0"));
        Assert.assertEquals(Integer.valueOf(2), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "1"));
        Assert.assertEquals(Integer.valueOf(3), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "2"));
        Assert.assertEquals(Integer.valueOf(4), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "3"));
        Assert.assertEquals(Integer.valueOf(5), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", "clin_t", "4"));

        Map<String, String> context = new HashMap<String, String>();
        context.put("clin_t", "4");
        Assert.assertEquals(Integer.valueOf(5), _STAGING.findMatchingTableRow("adrenal_gland_t_18187", context));

        // test a table that has multiple inputs
        context = new HashMap<String, String>();
        context.put("ss2017_t", "L");
        context.put("ss2017_n", "RE");
        context.put("ss2017_m", "D");
        Assert.assertEquals(Integer.valueOf(136), _STAGING.findMatchingTableRow("summary_stage_rpa", context));
    }

    @Test
    public void testInputBuilder() {
        TnmStagingData data1 = new TnmStagingData();
        data1.setInput(TnmStagingData.TnmInput.INPUT_VERSION, "1.0");
        data1.setInput(TnmStagingData.TnmInput.PRIMARY_SITE, "C680");
        data1.setInput(TnmStagingData.TnmInput.HISTOLOGY, "8000");
        data1.setInput(TnmStagingData.TnmInput.BEHAVIOR, "3");
        data1.setInput(TnmStagingData.TnmInput.GRADE, "9");
        data1.setInput(TnmStagingData.TnmInput.DX_YEAR, "2013");
        data1.setInput(TnmStagingData.TnmInput.INPUT_VERSION, "1.0");
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

        TnmStagingData data2 = new TnmStagingData.TnmStagingInputBuilder()
                .withInput(TnmStagingData.TnmInput.INPUT_VERSION, "1.0")
                .withInput(TnmStagingData.TnmInput.PRIMARY_SITE, "C680")
                .withInput(TnmStagingData.TnmInput.HISTOLOGY, "8000")
                .withInput(TnmStagingData.TnmInput.BEHAVIOR, "3")
                .withInput(TnmStagingData.TnmInput.GRADE, "9")
                .withInput(TnmStagingData.TnmInput.DX_YEAR, "2013")
                .withInput(TnmStagingData.TnmInput.INPUT_VERSION, "1.0")
                .withInput(TnmStagingData.TnmInput.SEER_PRIMARY_TUMOR, "100")
                .withInput(TnmStagingData.TnmInput.SEER_REGIONAL_NODES, "100")
                .withInput(TnmStagingData.TnmInput.REGIONAL_NODES_POSITIVE, "99")
                .withInput(TnmStagingData.TnmInput.SEER_METS, "10")
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

        Assert.assertEquals(data1.getInput(), data2.getInput());
    }

    @Test
    public void testBlankValues() {
        //        TnmStagingData data = new TnmStagingData();
        //        data.setInput(TnmStagingData.TnmInput.PRIMARY_SITE, "C700");
        //        data.setInput(TnmStagingData.TnmInput.HISTOLOGY, "9530");
        //        data.setInput(TnmStagingData.TnmInput.BEHAVIOR, "0");
        //        data.setInput(TnmStagingData.TnmInput.GRADE, "9");
        //        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "2010");
        //        data.setInput(TnmStagingData.TnmInput.INPUT_VERSION, "1.0");
        //        data.setInput(TnmStagingData.TnmInput.EXTENSION, "050");
        //        data.setInput(TnmStagingData.TnmInput.LYMPH_NODES, "988");
        //        data.setInput(TnmStagingData.TnmInput.REGIONAL_NODES_POSITIVE, "99");
        //        data.setInput(TnmStagingData.TnmInput.METS_AT_DX, "00");
        //        data.setInput(TnmStagingData.TnmInput.LVI, "8");
        //        data.setInput(TnmStagingData.TnmInput.AGE_AT_DX, "060");
        //        data.setSsf(1, "999");
        //        data.setSsf(2, "999");
        //        data.setSsf(3, "999");
        //        // do not supply SSF4
        //        data.setSsf(5, "999");
        //        data.setSsf(6, "999");
        //        data.setSsf(7, "000");
        //        data.setSsf(8, "001");
        //
        //        // perform the staging
        //        _STAGING.stage(data);
        //
        //        Assert.assertEquals(StagingData.Result.STAGED, data.getResult());
        //        Assert.assertEquals("brain", data.getSchemaId());
        //        Assert.assertEquals(0, data.getErrors().size());
        //
        //        // now change SSF4 to blank; blank values are not validated and since this is not used in staging there should be no errors
        //        data.setSsf(4, "");
        //
        //        // perform the staging
        //        _STAGING.stage(data);
        //
        //        Assert.assertEquals(StagingData.Result.STAGED, data.getResult());
        //        Assert.assertEquals("brain", data.getSchemaId());
        //        Assert.assertEquals(0, data.getErrors().size());
        //
        //        // now change extension to blank; the only errors we get should be of type MATCH_NOT_FOUND
        //        data.setInput(TnmStagingData.TnmInput.EXTENSION, "");
        //
        //        // perform the staging
        //        _STAGING.stage(data);
        //
        //        Assert.assertEquals(StagingData.Result.STAGED, data.getResult());
        //        Assert.assertEquals("brain", data.getSchemaId());
        //        for (Error error : data.getErrors())
        //            Assert.assertEquals(Error.Type.MATCH_NOT_FOUND, error.getType());
    }

    @Test
    public void testStageUrethra() {
        // test this case:  http://seer.cancer.gov/seertools/cstest/?mets=10&lnexam=99&diagnosis_year=2013&grade=9&exteval=9&age=060&site=C680&metseval=9&hist=8000&ext=100&version=020550&nodeseval=9&behav=3&lnpos=99&nodes=100&csver_original=020440&lvi=9&ssf1=020&size=075
        //        TnmStagingData data = new TnmStagingData();
        //        data.setInput(TnmStagingData.TnmInput.PRIMARY_SITE, "C680");
        //        data.setInput(TnmStagingData.TnmInput.HISTOLOGY, "8000");
        //        data.setInput(TnmStagingData.TnmInput.BEHAVIOR, "3");
        //        data.setInput(TnmStagingData.TnmInput.GRADE, "9");
        //        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "2013");
        //        data.setInput(TnmStagingData.TnmInput.INPUT_VERSION, "020550");
        //        data.setInput(TnmStagingData.TnmInput.TUMOR_SIZE, "075");
        //        data.setInput(TnmStagingData.TnmInput.EXTENSION, "100");
        //        data.setInput(TnmStagingData.TnmInput.EXTENSION_EVAL, "9");
        //        data.setInput(TnmStagingData.TnmInput.LYMPH_NODES, "100");
        //        data.setInput(TnmStagingData.TnmInput.LYMPH_NODES_EVAL, "9");
        //        data.setInput(TnmStagingData.TnmInput.REGIONAL_NODES_POSITIVE, "99");
        //        data.setInput(TnmStagingData.TnmInput.REGIONAL_NODES_EXAMINED, "99");
        //        data.setInput(TnmStagingData.TnmInput.METS_AT_DX, "10");
        //        data.setInput(TnmStagingData.TnmInput.METS_EVAL, "9");
        //        data.setInput(TnmStagingData.TnmInput.LVI, "9");
        //        data.setInput(TnmStagingData.TnmInput.AGE_AT_DX, "060");
        //        data.setSsf(1, "020");
        //
        //        // perform the staging
        //        _STAGING.stage(data);
        //
        //        Assert.assertEquals(StagingData.Result.STAGED, data.getResult());
        //        Assert.assertEquals("urethra", data.getSchemaId());
        //        Assert.assertEquals(0, data.getErrors().size());
        //        Assert.assertEquals(37, data.getPath().size());
        //
        //        // check output
        //        Assert.assertEquals("129", data.getOutput(TnmStagingData.TnmOutput.SCHEMA_NUMBER));
        //        Assert.assertEquals("020550", data.getOutput(TnmStagingData.TnmOutput.CSVER_DERIVED));
        //
        //        // AJCC 6
        //        Assert.assertEquals("T1", data.getOutput(TnmStagingData.TnmOutput.AJCC6_T));
        //        Assert.assertEquals("c", data.getOutput(TnmStagingData.TnmOutput.AJCC6_TDESCRIPTOR));
        //        Assert.assertEquals("N1", data.getOutput(TnmStagingData.TnmOutput.AJCC6_N));
        //        Assert.assertEquals("c", data.getOutput(TnmStagingData.TnmOutput.AJCC6_NDESCRIPTOR));
        //        Assert.assertEquals("M1", data.getOutput(TnmStagingData.TnmOutput.AJCC6_M));
        //        Assert.assertEquals("c", data.getOutput(TnmStagingData.TnmOutput.AJCC6_MDESCRIPTOR));
        //        Assert.assertEquals("IV", data.getOutput(TnmStagingData.TnmOutput.AJCC6_STAGE));
        //        Assert.assertEquals("10", data.getOutput(TnmStagingData.TnmOutput.STOR_AJCC6_T));
        //        Assert.assertEquals("c", data.getOutput(TnmStagingData.TnmOutput.STOR_AJCC6_TDESCRIPTOR));
        //        Assert.assertEquals("10", data.getOutput(TnmStagingData.TnmOutput.STOR_AJCC6_N));
        //        Assert.assertEquals("c", data.getOutput(TnmStagingData.TnmOutput.STOR_AJCC6_NDESCRIPTOR));
        //        Assert.assertEquals("10", data.getOutput(TnmStagingData.TnmOutput.STOR_AJCC6_M));
        //        Assert.assertEquals("c", data.getOutput(TnmStagingData.TnmOutput.STOR_AJCC6_MDESCRIPTOR));
        //        Assert.assertEquals("70", data.getOutput(TnmStagingData.TnmOutput.STOR_AJCC6_STAGE));
        //
        //        // AJCC 7
        //        Assert.assertEquals("T1", data.getOutput(TnmStagingData.TnmOutput.AJCC7_T));
        //        Assert.assertEquals("c", data.getOutput(TnmStagingData.TnmOutput.AJCC7_TDESCRIPTOR));
        //        Assert.assertEquals("N1", data.getOutput(TnmStagingData.TnmOutput.AJCC7_N));
        //        Assert.assertEquals("c", data.getOutput(TnmStagingData.TnmOutput.AJCC7_NDESCRIPTOR));
        //        Assert.assertEquals("M1", data.getOutput(TnmStagingData.TnmOutput.AJCC7_M));
        //        Assert.assertEquals("c", data.getOutput(TnmStagingData.TnmOutput.AJCC7_MDESCRIPTOR));
        //        Assert.assertEquals("IV", data.getOutput(TnmStagingData.TnmOutput.AJCC7_STAGE));
        //        Assert.assertEquals("100", data.getOutput(TnmStagingData.TnmOutput.STOR_AJCC7_T));
        //        Assert.assertEquals("c", data.getOutput(TnmStagingData.TnmOutput.STOR_AJCC6_TDESCRIPTOR));
        //        Assert.assertEquals("100", data.getOutput(TnmStagingData.TnmOutput.STOR_AJCC7_N));
        //        Assert.assertEquals("c", data.getOutput(TnmStagingData.TnmOutput.STOR_AJCC7_NDESCRIPTOR));
        //        Assert.assertEquals("100", data.getOutput(TnmStagingData.TnmOutput.STOR_AJCC7_M));
        //        Assert.assertEquals("c", data.getOutput(TnmStagingData.TnmOutput.STOR_AJCC7_MDESCRIPTOR));
        //        Assert.assertEquals("700", data.getOutput(TnmStagingData.TnmOutput.STOR_AJCC7_STAGE));
        //
        //        // Summary Stage
        //        Assert.assertEquals("L", data.getOutput(TnmStagingData.TnmOutput.SS1977_T));
        //        Assert.assertEquals("RN", data.getOutput(TnmStagingData.TnmOutput.SS1977_N));
        //        Assert.assertEquals("D", data.getOutput(TnmStagingData.TnmOutput.SS1977_M));
        //        Assert.assertEquals("D", data.getOutput(TnmStagingData.TnmOutput.SS1977_STAGE));
        //        Assert.assertEquals("L", data.getOutput(TnmStagingData.TnmOutput.SS2000_T));
        //        Assert.assertEquals("RN", data.getOutput(TnmStagingData.TnmOutput.SS2000_N));
        //        Assert.assertEquals("D", data.getOutput(TnmStagingData.TnmOutput.SS2000_M));
        //        Assert.assertEquals("D", data.getOutput(TnmStagingData.TnmOutput.SS2000_STAGE));
        //        Assert.assertEquals("7", data.getOutput(TnmStagingData.TnmOutput.STOR_SS1977_STAGE));
        //        Assert.assertEquals("7", data.getOutput(TnmStagingData.TnmOutput.STOR_SS2000_STAGE));
        //
        //        // make sure defaulted inputs are not in the output
        //        Set<String> outputKeys = data.getOutput().keySet();
        //        for (TnmStagingData.TnmOutput output : TnmStagingData.TnmOutput.values())
        //            outputKeys.remove(output.toString());
        //        Assert.assertTrue("The keys " + outputKeys + " were in the output but are not CS output fields.", outputKeys.isEmpty());
        //
        //        // test case with valid year_dx and invalid version original
        //        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "2013");
        //        data.setInput(TnmStagingData.TnmInput.INPUT_VERSION, "1111");
        //        _STAGING.stage(data);
        //        Assert.assertEquals(StagingData.Result.STAGED, data.getResult());
        //        Assert.assertEquals("urethra", data.getSchemaId());
        //        Assert.assertEquals(1, data.getErrors().size());
        //        Assert.assertEquals(Error.Type.INVALID_REQUIRED_INPUT, data.getErrors().get(0).getType());
        //
        //        // test case with missing year_dx and valid version original
        //        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "");
        //        data.setInput(TnmStagingData.TnmInput.INPUT_VERSION, "020550");
        //        _STAGING.stage(data);
        //        Assert.assertEquals(StagingData.Result.STAGED, data.getResult());
        //        Assert.assertEquals("urethra", data.getSchemaId());
        //        Assert.assertEquals(0, data.getErrors().size());
        //
        //        // test case with missing year_dx and valid version original
        //        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "");
        //        data.setInput(TnmStagingData.TnmInput.INPUT_VERSION, "020001");
        //        _STAGING.stage(data);
        //        Assert.assertEquals(StagingData.Result.STAGED, data.getResult());
        //        Assert.assertEquals("urethra", data.getSchemaId());
        //        Assert.assertEquals(0, data.getErrors().size());
        //
        //        // test case with space-filled year_dx and valid version original
        //        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "    ");
        //        data.setInput(TnmStagingData.TnmInput.INPUT_VERSION, "020001");
        //        _STAGING.stage(data);
        //        Assert.assertEquals(StagingData.Result.STAGED, data.getResult());
        //        Assert.assertEquals("urethra", data.getSchemaId());
        //        Assert.assertEquals(0, data.getErrors().size());
        //
        //        // test case with missing year_dx and invalid version original
        //        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "");
        //        data.setInput(TnmStagingData.TnmInput.INPUT_VERSION, "012345");
        //        _STAGING.stage(data);
        //        Assert.assertEquals(StagingData.Result.FAILED_INVALID_YEAR_DX, data.getResult());
        //        Assert.assertEquals("urethra", data.getSchemaId());
        //        Assert.assertEquals(0, data.getErrors().size());
        //
        //        // test case with missing year_dx and invalid version original
        //        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "");
        //        data.setInput(TnmStagingData.TnmInput.INPUT_VERSION, "1");
        //        _STAGING.stage(data);
        //        Assert.assertEquals(StagingData.Result.FAILED_INVALID_YEAR_DX, data.getResult());
        //        Assert.assertEquals("urethra", data.getSchemaId());
        //        Assert.assertEquals(0, data.getErrors().size());
        //
        //        // test case with space-filled year_dx and invalid version original
        //        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "    ");
        //        data.setInput(TnmStagingData.TnmInput.INPUT_VERSION, "012345");
        //        _STAGING.stage(data);
        //        Assert.assertEquals(StagingData.Result.FAILED_INVALID_YEAR_DX, data.getResult());
        //        Assert.assertEquals("urethra", data.getSchemaId());
        //        Assert.assertEquals(0, data.getErrors().size());
        //
        //        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "2003");
        //        data.setInput(TnmStagingData.TnmInput.INPUT_VERSION, "020550");
        //        _STAGING.stage(data);
        //        Assert.assertEquals(StagingData.Result.FAILED_INVALID_YEAR_DX, data.getResult());
        //        Assert.assertEquals("urethra", data.getSchemaId());
        //        Assert.assertEquals(0, data.getOutput().size());
        //        Assert.assertEquals(0, data.getErrors().size());
        //        Assert.assertEquals(0, data.getPath().size());
        //
        //        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "2050");
        //        data.setInput(TnmStagingData.TnmInput.INPUT_VERSION, "020550");
        //        _STAGING.stage(data);
        //        Assert.assertEquals(StagingData.Result.FAILED_INVALID_YEAR_DX, data.getResult());
        //        Assert.assertEquals("urethra", data.getSchemaId());
        //        Assert.assertEquals(0, data.getOutput().size());
        //        Assert.assertEquals(0, data.getErrors().size());
        //        Assert.assertEquals(0, data.getPath().size());
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

        Assert.assertEquals(Sets.newHashSet("extension_bcn", "histology", "input_version_validation", "nodes_dcc", "primary_site", "schema_selection_adnexa_uterine_other",
                "seer_mets_48348", "seer_mets_copy_adnexauterineother_56278", "summary_stage_rpa", "year_dx_validation"), tables);
    }

    @Test
    public void testInvolvedSchemas() {
        Set<String> schemas = _STAGING.getInvolvedSchemas("ssf1_jpd");

        Assert.assertEquals(new HashSet<String>(Arrays.asList("kidney_renal_pelvis", "bladder", "urethra")), schemas);
    }

    @Test
    public void testGetInputs() {
        //        Assert.assertEquals(Sets.newHashSet("extension", "site", "extension_eval", "mets_eval", "nodes_eval", "nodes", "hist", "year_dx", "cs_input_version_original", "mets"),
        //                _STAGING.getInputs(_STAGING.getSchema("adnexa_uterine_other")));
        //
        //        Assert.assertEquals(Sets.newHashSet("site", "nodes_pos", "mets_eval", "nodes_eval", "ssf16", "ssf15", "ssf13", "cs_input_version_original", "lvi",
        //                        "extension", "extension_eval", "ssf1", "ssf2", "ssf3", "hist", "ssf4", "nodes", "ssf5", "year_dx", "mets"),
        //                _STAGING.getInputs(_STAGING.getSchema("testis")));
        //
        //        // test with and without context
        //        Assert.assertEquals(Sets.newHashSet("site", "nodes_eval", "mets_eval", "ssf10", "cs_input_version_original", "ssf8", "extension", "extension_eval", "ssf1", "ssf3",
        //                        "hist", "nodes", "year_dx", "grade", "mets"),
        //                _STAGING.getInputs(_STAGING.getSchema("prostate")));
        //
        //        Map<String, String> context = new HashMap<String, String>();
        //        context.put(StagingData.PRIMARY_SITE_KEY, "C619");
        //        context.put(StagingData.HISTOLOGY_KEY, "8120");
        //        context.put(StagingData.YEAR_DX_KEY, "2004");
        //
        //        // for that context, neither AJCC6 or 7 should be calculated so "grade" and "ssf1" should not be list of inputs
        //        Assert.assertEquals(Sets.newHashSet("site", "nodes_eval", "mets_eval", "ssf10", "cs_input_version_original", "ssf8", "extension", "extension_eval", "ssf3",
        //                        "hist", "nodes", "year_dx", "mets"),
        //                _STAGING.getInputs(_STAGING.getSchema("prostate"), context));
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
        Assert.assertTrue(_STAGING.isCodeValid("urethra", "year_dx", "2015"));

        // test valid and invalid fields
        Assert.assertTrue(_STAGING.isCodeValid("urethra", "seer_primary_tumor", "100"));
        Assert.assertFalse(_STAGING.isCodeValid("urethra", "seer_primary_tumor", "150"));
        Assert.assertTrue(_STAGING.isCodeValid("urethra", "ssf1", "020"));
        Assert.assertFalse(_STAGING.isCodeValid("urethra", "ssf1", "030"));
    }

    @Test
    public void testIsContextValid() {
        TnmStagingData data = new TnmStagingData();

        data.setInput(Staging.CTX_YEAR_CURRENT, "2015");

        // test valid year
        data.setInput(TnmStagingData.TnmInput.DX_YEAR, "2015");
        data.setInput(TnmStagingData.TnmInput.INPUT_VERSION, "1.0");
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

    @Test(expected = IllegalStateException.class)
    public void testGetTable() {
        _STAGING.getTable("bad_table_name");
    }

    @Test
    public void testStagingInputsAndOutputs() {
        //        StagingSchema schema = _STAGING.getSchema("testis");
        //
        //        Assert.assertEquals("Inputs do not match", Sets.newHashSet("cs_input_version_original", "extension", "extension_eval", "site", "hist", "lvi",
        //                "mets_eval", "mets", "nodes", "nodes_eval", "nodes_pos", "ssf1", "ssf2", "ssf3", "ssf4", "ssf5", "ssf13", "ssf15", "ssf16",
        //                "year_dx"), _STAGING.getInputs(schema));
        //
        //        // note that outputs should NOT include values produced by staging that are not in the defined output list (if an output list exists on the schema)
        //        Assert.assertEquals("Outputs do not match", Sets.newHashSet("schema_number", "csver_derived", "ss77", "stor_ajcc7_m", "t2000", "stor_ajcc7_n",
        //                "stor_ajcc6_tdescriptor", "ajcc7_stage", "stor_ajcc6_mdescriptor", "stor_ss2000", "ajcc6_tdescriptor", "stor_ajcc7_t", "ajcc6_stage", "n2000",
        //                "ajcc7_ndescriptor", "ajcc6_ndescriptor", "ajcc7_mdescriptor", "ajcc6_mdescriptor", "stor_ajcc7_stage", "m77", "ajcc6_m", "ss2000",
        //                "stor_ajcc7_ndescriptor", "ajcc7_m", "ajcc7_n", "stor_ajcc7_mdescriptor", "t77", "ajcc6_n", "stor_ss77", "ajcc6_t", "stor_ajcc6_ndescriptor",
        //                "stor_ajcc6_stage", "m2000", "ajcc7_t", "n77", "ajcc7_tdescriptor", "stor_ajcc6_m", "stor_ajcc6_n", "stor_ajcc6_t", "stor_ajcc7_tdescriptor"), _STAGING.getOutputs(schema));
        //
        //        // test used for staging
        //        Assert.assertFalse(schema.getInputMap().get("ssf14").getUsedForStaging());
        //        Assert.assertTrue(schema.getInputMap().get("ssf15").getUsedForStaging());
        //
        //        // test metadata
        //        Assert.assertNull(schema.getInputMap().get("ssf11").getMetadata());
        //        Assert.assertTrue(schema.getInputMap().get("ssf17").getMetadata().contains("UNDEFINED_SSF"));
        //        Assert.assertTrue(schema.getInputMap().get("ssf7").getMetadata().contains("SEER_CLINICALLY_SIGNIFICANT"));
        //
        //        Map<String, String> context = new HashMap<String, String>();
        //        context.put(StagingData.PRIMARY_SITE_KEY, "C629");
        //        context.put(StagingData.HISTOLOGY_KEY, "9231");
        //        Set<String> inputs = _STAGING.getInputs(schema, context);
        //
        //        // this is a case that summary stages only.  Testing to make sure "hist", which is used in the inclusion/exclusion criteria
        //        // is included in the list even though the mappings for AJCC6 and 7 are not included
        //        Assert.assertTrue("Inclusion/exclusion input is not included", inputs.contains("hist"));
        //
        //        Assert.assertTrue(inputs.contains("mets"));
        //
        //        // these are no used when only doing summary stage
        //        Assert.assertFalse(inputs.contains("ssf1"));
        //        Assert.assertFalse(inputs.contains("ssf2"));
        //        Assert.assertFalse(inputs.contains("ssf3"));
        //        Assert.assertFalse(inputs.contains("ssf13"));
        //        Assert.assertFalse(inputs.contains("ssf15"));
        //        Assert.assertFalse(inputs.contains("ssf16"));
        //
        //        // now test one that does do AJCC7 (inputs should include extra SSFs used in AJCC calculations)
        //        context.put(StagingData.HISTOLOGY_KEY, "9100");
        //        inputs = _STAGING.getInputs(schema, context);
        //        Assert.assertTrue(inputs.contains("hist"));
        //        Assert.assertTrue(inputs.contains("ssf1"));
        //        Assert.assertTrue(inputs.contains("ssf2"));
        //        Assert.assertTrue(inputs.contains("ssf3"));
        //        Assert.assertTrue(inputs.contains("ssf13"));
        //        Assert.assertTrue(inputs.contains("ssf15"));
        //        Assert.assertTrue(inputs.contains("ssf16"));
        //
        //        // the prostate schema tables use a reference to {{ssf8}} and {{ssf10}}; make sure they are picked up in the list of required inputs
        //        inputs = _STAGING.getInputs(_STAGING.getSchema("prostate"));
        //        Assert.assertTrue(inputs.contains("ssf8"));
        //        Assert.assertTrue(inputs.contains("ssf10"));
    }

    @Test
    public void verifyInputs() {
        List<String> errors = new ArrayList<String>();

        for (String id : _STAGING.getSchemaIds()) {
            StagingSchema schema = _STAGING.getSchema(id);

            // loop over all the inputs returned by processing the schema and make sure they are all part of the main list of inputs on the schema
            for (String input : _STAGING.getInputs(schema))
                if (!schema.getInputMap().containsKey(input))
                    errors.add("Error processing schema " + schema.getId() + ": Table input '" + input + "' not in master list of inputs");
        }

        if (!errors.isEmpty()) {
            System.out.println("There were " + errors.size() + " issues with input values.");
            for (String error : errors)
                System.out.println(error);
            Assert.fail();
        }
    }

    @Test
    public void testExpectedOutput() throws Exception {
        //        IntegrationUtils.IntegrationResult ajcc6Result = IntegrationUtils.processSchema(_STAGING, "AJCC_6.V020550.txt.gz",
        //                new GZIPInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("test-data/cs/020550/AJCC_6.V020550.txt.gz")));
        //        IntegrationUtils.IntegrationResult ajcc7Result = IntegrationUtils.processSchema(_STAGING, "AJCC_7.V020550.txt.gz",
        //                new GZIPInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("test-data/cs/020550/AJCC_7.V020550.txt.gz")));
        //
        //        // make sure there were no errors returned
        //        Assert.assertEquals("There were failures in the AJCC6 tests", 0, ajcc6Result.getNumFailures());
        //        Assert.assertEquals("There were failures in the AJCC7 tests", 0, ajcc7Result.getNumFailures());
    }

    @Test
    public void testAllValidInputs() throws IOException {
        //        GZIPInputStream is = new GZIPInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("test-data/cs/020550/valid_inputs.020550.txt.gz"));
        //        LineNumberReader reader = new LineNumberReader(new InputStreamReader(is, "UTF-8"));
        //
        //        // cache a list of schemas by name
        //        Map<String, String> nameMap = new HashMap<String, String>();
        //        for (String id : _STAGING.getSchemaIds())
        //            nameMap.put(_STAGING.getSchema(id).getName(), id);
        //
        //        String line = reader.readLine();
        //        while (line != null) {
        //            // split the CSV record
        //            String[] values = line.split("\\|");
        //            Assert.assertEquals(3, values.length);
        //
        //            // get schema by schema name
        //            StagingSchema schema = _STAGING.getSchema(nameMap.get(values[0]));
        //            Assert.assertTrue("The value '" + values[2] + "' is not valid for table '" + values[1] + "' and schema '" + values[0] + "'", _STAGING.isCodeValid(schema.getId(), values[1], values[2]));
        //
        //            line = reader.readLine();
        //        }
        //
        //        System.out.println("Processed " + reader.getLineNumber() + " lines.");
        //
        //        reader.close();
    }

    @Test
    public void testCachedSiteAndHistology() {
        TnmDataProvider provider = TnmDataProvider.getInstance(TnmDataProvider.TnmVersion.v1_0);
        Assert.assertTrue(provider.getValidSites().size() > 0);
        Assert.assertTrue(provider.getValidHistologies().size() > 0);

        // site tests
        List<String> validSites = Arrays.asList("C000", "C809");
        List<String> invalidSites = Arrays.asList("C727", "C810");
        for (String site : validSites)
            Assert.assertTrue(provider.getValidSites().contains(site));
        for (String site : invalidSites)
            Assert.assertFalse(provider.getValidSites().contains(site));

        // hist tests
        List<String> validHist = Arrays.asList("8000", "8002", "8005", "8290", "9992");
        List<String> invalidHist = Arrays.asList("8006", "9993");
        for (String hist : validHist)
            Assert.assertTrue(provider.getValidHistologies().contains(hist));
        for (String hist : invalidHist)
            Assert.assertFalse(provider.getValidHistologies().contains(hist));
    }

    @Test
    public void testForUnusedTables() {
        Set<String> usedTables = new HashSet<String>();
        for (String id : _STAGING.getSchemaIds())
            usedTables.addAll(_STAGING.getSchema(id).getInvolvedTables());

        Set<String> unusedTables = new HashSet<String>();
        for (String id : _STAGING.getTableIds())
            if (!usedTables.contains(id))
                unusedTables.add(id);

        if (!unusedTables.isEmpty())
            Assert.fail("There are " + unusedTables.size() + " tables that are not used in any schema: " + unusedTables);
    }

    @Test
    public void testInputTables() {
        Set<String> errors = new HashSet<String>();

        for (String schemaId : _STAGING.getSchemaIds()) {
            StagingSchema schema = _STAGING.getSchema(schemaId);

            // build a list of input tables that should be excluded
            for (StagingSchemaInput input : schema.getInputs()) {
                if (input.getTable() != null) {
                    Set<String> inputKeys = new HashSet<String>();
                    StagingTable table = _STAGING.getTable(input.getTable());
                    for (StagingColumnDefinition def : table.getColumnDefinitions())
                        if (ColumnDefinition.ColumnType.INPUT.equals(def.getType()))
                            inputKeys.add(def.getKey());

                    // make sure the input key matches the an input column
                    if (!inputKeys.contains(input.getKey()))
                        errors.add("Input key " + schemaId + ":" + input.getKey() + " does not match validation table " + table.getId() + ": " + inputKeys.toString());
                }
            }
        }

        if (!errors.isEmpty()) {
            System.out.println("There were " + errors.size() + " issues with input values and their assocated validation tables.");
            for (String error : errors)
                System.out.println(error);
            Assert.fail();
        }
    }

    @Test
    public void testMappingIdUniqueness() {
        Set<String> errors = new HashSet<String>();

        for (String schemaId : _STAGING.getSchemaIds()) {
            StagingSchema schema = _STAGING.getSchema(schemaId);

            // build a list of input tables that should be excluded
            Set<String> ids = new HashSet<String>();
            for (StagingMapping mapping : schema.getMappings()) {
                if (ids.contains(mapping.getId()))
                    errors.add("The mapping id " + schemaId + ":" + mapping.getId() + " is duplicated.  This should never happen");
                ids.add(mapping.getId());
            }
        }

        if (!errors.isEmpty()) {
            System.out.println("There were " + errors.size() + " issues with input values and their assocated validation tables.");
            for (String error : errors)
                System.out.println(error);
            Assert.fail();
        }

    }

}
