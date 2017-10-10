/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.cs;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.imsweb.decisionengine.Error.Type;
import com.imsweb.staging.IntegrationUtils;
import com.imsweb.staging.IntegrationUtils.IntegrationResult;
import com.imsweb.staging.SchemaLookup;
import com.imsweb.staging.Staging;
import com.imsweb.staging.StagingData;
import com.imsweb.staging.StagingData.Result;
import com.imsweb.staging.StagingFileDataProvider;
import com.imsweb.staging.StagingTest;
import com.imsweb.staging.cs.CsDataProvider.CsVersion;
import com.imsweb.staging.cs.CsStagingData.CsOutput;
import com.imsweb.staging.cs.CsStagingData.CsStagingInputBuilder;
import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingSchemaInput;
import com.imsweb.staging.entities.StagingStringRange;
import com.imsweb.staging.entities.StagingTable;
import com.imsweb.staging.entities.StagingTableRow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CsStagingTest extends StagingTest {

    @Override
    public String getAlgorithm() {
        return "cs";
    }

    @Override
    public String getVersion() {
        return "02.05.50";
    }

    @Override
    public StagingFileDataProvider getProvider() {
        return CsDataProvider.getInstance(CsVersion.v020550);
    }

    @BeforeClass
    public static void init() throws IOException {
        _STAGING = Staging.getInstance(CsDataProvider.getInstance(CsVersion.v020550));
    }

    @Test
    public void testBasicInitialization() {
        assertEquals(153, _STAGING.getSchemaIds().size());
        assertTrue(_STAGING.getTableIds().size() > 0);

        assertNotNull(_STAGING.getSchema("urethra"));
        assertNotNull(_STAGING.getTable("extension_bdi"));
    }

    @Test
    public void testVersionInitiaizationTypes() {
        Staging staging020550 = Staging.getInstance(CsDataProvider.getInstance(CsVersion.v020550));
        assertEquals("02.05.50", staging020550.getVersion());

        Staging stagingLatest = Staging.getInstance(CsDataProvider.getInstance());
        assertEquals("02.05.50", stagingLatest.getVersion());
    }

    @Test
    public void testDescriminatorKeys() {
        assertEquals(new HashSet<>(Collections.singletonList("ssf25")), _STAGING.getSchema("nasopharynx").getSchemaDiscriminators());
        assertEquals(new HashSet<>(Collections.singletonList("ssf25")), _STAGING.getSchema("peritoneum_female_gen").getSchemaDiscriminators());
    }

    @Test
    public void testSchemaSelection() throws IOException, InterruptedException {
        // test bad values
        List<StagingSchema> lookup = _STAGING.lookupSchema(new SchemaLookup());
        assertEquals(0, lookup.size());

        lookup = _STAGING.lookupSchema(new CsSchemaLookup("XXX", "YYY"));
        assertEquals(0, lookup.size());

        // test valid combinations that do not require a discriminator
        lookup = _STAGING.lookupSchema(new CsSchemaLookup("C629", "9231", ""));
        assertEquals(1, lookup.size());
        assertEquals("testis", lookup.get(0).getId());
        assertEquals(Integer.valueOf(122), lookup.get(0).getSchemaNum());
        lookup = _STAGING.lookupSchema(new CsSchemaLookup("C629", "9231", null));
        assertEquals(1, lookup.size());
        assertEquals("testis", lookup.get(0).getId());
        assertEquals(Integer.valueOf(122), lookup.get(0).getSchemaNum());

        // now test one that does do AJCC7
        lookup = _STAGING.lookupSchema(new CsSchemaLookup("C629", "9100", ""));
        assertEquals(1, lookup.size());

        // test value combinations that do not require a discriminator and are supplied 988
        lookup = _STAGING.lookupSchema(new CsSchemaLookup("C629", "9231", "988"));
        assertEquals(1, lookup.size());
        assertEquals("testis", lookup.get(0).getId());

        // test valid combination that requires a discriminator but is not supplied one
        lookup = _STAGING.lookupSchema(new CsSchemaLookup("C111", "8200"));
        assertEquals(2, lookup.size());
        for (StagingSchema schema : lookup)
            assertEquals(new HashSet<>(Collections.singletonList("ssf25")), schema.getSchemaDiscriminators());

        // test valid combination that requires discriminator and a good discriminator is supplied
        lookup = _STAGING.lookupSchema(new CsSchemaLookup("C111", "8200", "010"));
        assertEquals(1, lookup.size());
        for (StagingSchema schema : lookup)
            assertEquals(new HashSet<>(Collections.singletonList("ssf25")), schema.getSchemaDiscriminators());
        assertEquals("nasopharynx", lookup.get(0).getId());
        assertEquals(Integer.valueOf(34), lookup.get(0).getSchemaNum());

        // test valid combination that requires a discriminator but is supplied a bad disciminator value
        lookup = _STAGING.lookupSchema(new CsSchemaLookup("C111", "8200", "999"));
        assertEquals(0, lookup.size());

        // test specific failure case:  Line #1995826 [C695,9701,100,lacrimal_gland] --> The schema selection should have found a schema, lacrimal_gland, but did not.
        lookup = _STAGING.lookupSchema(new CsSchemaLookup("C695", "9701", "100"));
        assertEquals(1, lookup.size());
        assertEquals("lacrimal_gland", lookup.get(0).getId());
        assertEquals(Integer.valueOf(138), lookup.get(0).getSchemaNum());

        // test searching on only site
        lookup = _STAGING.lookupSchema(new CsSchemaLookup("C401", null, null));
        assertEquals(5, lookup.size());

        // test searching on only hist
        lookup = _STAGING.lookupSchema(new CsSchemaLookup(null, "9702", null));
        assertEquals(2, lookup.size());

        // test that searching on only ssf25 returns no results
        lookup = _STAGING.lookupSchema(new CsSchemaLookup(null, null, "001"));
        assertEquals(0, lookup.size());
        lookup = _STAGING.lookupSchema(new CsSchemaLookup("", null, "001"));
        assertEquals(0, lookup.size());
        lookup = _STAGING.lookupSchema(new CsSchemaLookup(null, "", "001"));
        assertEquals(0, lookup.size());
    }

    @Test
    public void testLookupCache() {
        // do the same lookup twice
        List<StagingSchema> lookup = _STAGING.lookupSchema(new CsSchemaLookup("C629", "9231", ""));
        assertEquals(1, lookup.size());
        assertEquals("testis", lookup.get(0).getId());

        lookup = _STAGING.lookupSchema(new CsSchemaLookup("C629", "9231", ""));
        assertEquals(1, lookup.size());
        assertEquals("testis", lookup.get(0).getId());

        // now invalidate the cache
        getProvider().invalidateCache();

        // try the lookup again
        lookup = _STAGING.lookupSchema(new CsSchemaLookup("C629", "9231", ""));
        assertEquals(1, lookup.size());
        assertEquals("testis", lookup.get(0).getId());
    }

    @Test
    public void testSchemaSelectionIntegration() throws IOException, InterruptedException {
        // test complete file of cases
        IntegrationResult result = IntegrationUtils.processSchemaSelection(_STAGING, "cs_schema_identification_unit_test.txt.gz",
                new GZIPInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("test-data/cs/020550/cs_schema_identification_unit_test.txt.gz")));
        assertEquals(0, result.getNumFailures());
    }

    @Test
    public void testOldSchemaNamesExist() {
        List<String> oldNames = Arrays.asList("AdnexaUterineOther", "AdrenalGland", "AmpullaVater", "Anus", "Appendix", "BileDuctsDistal", "BileDuctsIntraHepat", "BileDuctsPerihilar", "BiliaryOther",
                "Bladder", "Bone", "Brain", "Breast", "BuccalMucosa", "CarcinoidAppendix", "Cervix", "CNSOther", "Colon", "Conjunctiva", "CorpusAdenosarcoma", "CorpusCarcinoma", "CorpusSarcoma",
                "CysticDuct", "DigestiveOther", "EndocrineOther", "EpiglottisAnterior", "Esophagus", "EsophagusGEJunction", "EyeOther", "FallopianTube", "FloorMouth", "Gallbladder",
                "GenitalFemaleOther", "GenitalMaleOther", "GISTAppendix", "GISTColon", "GISTEsophagus", "GISTPeritoneum", "GISTRectum", "GISTSmallIntestine", "GISTStomach", "GumLower", "GumOther",
                "GumUpper", "HeartMediastinum", "HemeRetic", "Hypopharynx", "IllDefinedOther", "IntracranialGland", "KaposiSarcoma", "KidneyParenchyma", "KidneyRenalPelvis", "LacrimalGland",
                "LacrimalSac", "LarynxGlottic", "LarynxOther", "LarynxSubglottic", "LarynxSupraglottic", "LipLower", "LipOther", "LipUpper", "Liver", "Lung", "Lymphoma", "LymphomaOcularAdnexa",
                "MelanomaBuccalMucosa", "MelanomaChoroid", "MelanomaCiliaryBody", "MelanomaConjunctiva", "MelanomaEpiglottisAnterior", "MelanomaEyeOther", "MelanomaFloorMouth", "MelanomaGumLower",
                "MelanomaGumOther", "MelanomaGumUpper", "MelanomaHypopharynx", "MelanomaIris", "MelanomaLarynxGlottic", "MelanomaLarynxOther", "MelanomaLarynxSubglottic", "MelanomaLarynxSupraglottic",
                "MelanomaLipLower", "MelanomaLipOther", "MelanomaLipUpper", "MelanomaMouthOther", "MelanomaNasalCavity", "MelanomaNasopharynx", "MelanomaOropharynx", "MelanomaPalateHard",
                "MelanomaPalateSoft", "MelanomaPharynxOther", "MelanomaSinusEthmoid", "MelanomaSinusMaxillary", "MelanomaSinusOther", "MelanomaSkin", "MelanomaTongueAnterior", "MelanomaTongueBase",
                "MerkelCellPenis", "MerkelCellScrotum", "MerkelCellSkin", "MerkelCellVulva", "MiddleEar", "MouthOther", "MycosisFungoides", "MyelomaPlasmaCellDisorder", "NasalCavity", "Nasopharynx",
                "NETAmpulla", "NETColon", "NETRectum", "NETSmallIntestine", "NETStomach", "Orbit", "Oropharynx", "Ovary", "PalateHard", "PalateSoft", "PancreasBodyTail", "PancreasHead",
                "PancreasOther", "ParotidGland", "Penis", "Peritoneum", "PeritoneumFemaleGen", "PharyngealTonsil", "PharynxOther", "Placenta", "Pleura", "Prostate", "Rectum", "RespiratoryOther",
                "Retinoblastoma", "Retroperitoneum", "SalivaryGlandOther", "Scrotum", "SinusEthmoid", "SinusMaxillary", "SinusOther", "Skin", "SkinEyelid", "SmallIntestine", "SoftTissue", "Stomach",
                "SubmandibularGland", "Testis", "Thyroid", "TongueAnterior", "TongueBase", "Trachea", "Urethra", "UrinaryOther", "Vagina", "Vulva");

        for (String id : _STAGING.getSchemaIds()) {
            StagingSchema schema = _STAGING.getSchema(id);
            if (!oldNames.contains(schema.getName()))
                fail("The schema name " + schema.getName() + " is not one of the original names.");
        }
    }

    @Test
    public void testFindTableRow() {
        assertNull(_STAGING.findMatchingTableRow("size_apa", "size", null));
        assertNull(_STAGING.findMatchingTableRow("size_apa", "size", "X"));
        assertNull(_STAGING.findMatchingTableRow("size_apa", "size", "996"));

        assertEquals(Integer.valueOf(0), _STAGING.findMatchingTableRow("size_apa", "size", "000"));
        assertEquals(Integer.valueOf(1), _STAGING.findMatchingTableRow("size_apa", "size", "055"));
        assertEquals(Integer.valueOf(1), _STAGING.findMatchingTableRow("size_apa", "size", "988"));
        assertEquals(Integer.valueOf(2), _STAGING.findMatchingTableRow("size_apa", "size", "989"));
        assertEquals(Integer.valueOf(9), _STAGING.findMatchingTableRow("size_apa", "size", "999"));

        Map<String, String> context = new HashMap<>();
        context.put("size", "992");
        assertEquals(Integer.valueOf(5), _STAGING.findMatchingTableRow("size_apa", context));

        // test a table that has multiple inputs
        context = new HashMap<>();
        context.put("t", "RE");
        context.put("n", "U");
        context.put("m", "U");
        assertEquals(Integer.valueOf(167), _STAGING.findMatchingTableRow("summary_stage_rpa", context));
    }

    @Test
    public void testInputBuilder() {
        CsStagingData data1 = new CsStagingData();
        data1.setInput(CsStagingData.CsInput.PRIMARY_SITE, "C680");
        data1.setInput(CsStagingData.CsInput.HISTOLOGY, "8000");
        data1.setInput(CsStagingData.CsInput.BEHAVIOR, "3");
        data1.setInput(CsStagingData.CsInput.GRADE, "9");
        data1.setInput(CsStagingData.CsInput.DX_YEAR, "2013");
        data1.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "020550");
        data1.setInput(CsStagingData.CsInput.TUMOR_SIZE, "075");
        data1.setInput(CsStagingData.CsInput.EXTENSION, "100");
        data1.setInput(CsStagingData.CsInput.EXTENSION_EVAL, "9");
        data1.setInput(CsStagingData.CsInput.LYMPH_NODES, "100");
        data1.setInput(CsStagingData.CsInput.LYMPH_NODES_EVAL, "9");
        data1.setInput(CsStagingData.CsInput.REGIONAL_NODES_POSITIVE, "99");
        data1.setInput(CsStagingData.CsInput.REGIONAL_NODES_EXAMINED, "99");
        data1.setInput(CsStagingData.CsInput.METS_AT_DX, "10");
        data1.setInput(CsStagingData.CsInput.METS_EVAL, "9");
        data1.setInput(CsStagingData.CsInput.LVI, "9");
        data1.setInput(CsStagingData.CsInput.AGE_AT_DX, "060");
        data1.setSsf(1, "020");

        CsStagingData data2 = new CsStagingInputBuilder().withInput(CsStagingData.CsInput.PRIMARY_SITE, "C680").withInput(CsStagingData.CsInput.HISTOLOGY, "8000").withInput(
                CsStagingData.CsInput.BEHAVIOR, "3").withInput(CsStagingData.CsInput.GRADE, "9").withInput(CsStagingData.CsInput.DX_YEAR, "2013").withInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL,
                "020550").withInput(CsStagingData.CsInput.TUMOR_SIZE, "075").withInput(CsStagingData.CsInput.EXTENSION, "100").withInput(CsStagingData.CsInput.EXTENSION_EVAL, "9").withInput(
                CsStagingData.CsInput.LYMPH_NODES, "100").withInput(CsStagingData.CsInput.LYMPH_NODES_EVAL, "9").withInput(CsStagingData.CsInput.REGIONAL_NODES_POSITIVE, "99").withInput(
                CsStagingData.CsInput.REGIONAL_NODES_EXAMINED, "99").withInput(CsStagingData.CsInput.METS_AT_DX, "10").withInput(CsStagingData.CsInput.METS_EVAL, "9").withInput(
                CsStagingData.CsInput.LVI, "9").withInput(CsStagingData.CsInput.AGE_AT_DX, "060").withSsf(1, "020").build();

        assertEquals(data1.getInput(), data2.getInput());
    }

    @Test
    public void testBlankValues() {
        CsStagingData data = new CsStagingData();
        data.setInput(CsStagingData.CsInput.PRIMARY_SITE, "C700");
        data.setInput(CsStagingData.CsInput.HISTOLOGY, "9530");
        data.setInput(CsStagingData.CsInput.BEHAVIOR, "0");
        data.setInput(CsStagingData.CsInput.GRADE, "9");
        data.setInput(CsStagingData.CsInput.DX_YEAR, "2010");
        data.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "020520");
        data.setInput(CsStagingData.CsInput.TUMOR_SIZE, "999");
        data.setInput(CsStagingData.CsInput.EXTENSION, "050");
        data.setInput(CsStagingData.CsInput.EXTENSION_EVAL, "9");
        data.setInput(CsStagingData.CsInput.LYMPH_NODES, "988");
        data.setInput(CsStagingData.CsInput.LYMPH_NODES_EVAL, "9");
        data.setInput(CsStagingData.CsInput.REGIONAL_NODES_POSITIVE, "99");
        data.setInput(CsStagingData.CsInput.REGIONAL_NODES_EXAMINED, "99");
        data.setInput(CsStagingData.CsInput.METS_AT_DX, "00");
        data.setInput(CsStagingData.CsInput.METS_EVAL, "9");
        data.setInput(CsStagingData.CsInput.LVI, "8");
        data.setInput(CsStagingData.CsInput.AGE_AT_DX, "060");
        data.setSsf(1, "999");
        data.setSsf(2, "999");
        data.setSsf(3, "999");
        // do not supply SSF4
        data.setSsf(5, "999");
        data.setSsf(6, "999");
        data.setSsf(7, "000");
        data.setSsf(8, "001");

        // perform the staging
        _STAGING.stage(data);

        assertEquals(Result.STAGED, data.getResult());
        assertEquals("brain", data.getSchemaId());
        assertEquals(0, data.getErrors().size());

        // now change SSF4 to blank; blank values are not validated and since this is not used in staging there should be no errors
        data.setSsf(4, "");

        // perform the staging
        _STAGING.stage(data);

        assertEquals(Result.STAGED, data.getResult());
        assertEquals("brain", data.getSchemaId());
        assertEquals(0, data.getErrors().size());

        // now change extension to blank; the only errors we get should be of type MATCH_NOT_FOUND
        data.setInput(CsStagingData.CsInput.EXTENSION, "");

        // perform the staging
        _STAGING.stage(data);

        assertEquals(Result.STAGED, data.getResult());
        assertEquals("brain", data.getSchemaId());
        for (com.imsweb.decisionengine.Error error : data.getErrors())
            assertEquals(Type.MATCH_NOT_FOUND, error.getType());
    }

    @Test
    public void testStageUrethra() {
        // test this case:  http://seer.cancer.gov/seertools/cstest/?mets=10&lnexam=99&diagnosis_year=2013&grade=9&exteval=9&age=060&site=C680&metseval=9&hist=8000&ext=100&version=020550&nodeseval=9&behav=3&lnpos=99&nodes=100&csver_original=020440&lvi=9&ssf1=020&size=075
        CsStagingData data = new CsStagingData();
        data.setInput(CsStagingData.CsInput.PRIMARY_SITE, "C680");
        data.setInput(CsStagingData.CsInput.HISTOLOGY, "8000");
        data.setInput(CsStagingData.CsInput.BEHAVIOR, "3");
        data.setInput(CsStagingData.CsInput.GRADE, "9");
        data.setInput(CsStagingData.CsInput.DX_YEAR, "2013");
        data.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "020550");
        data.setInput(CsStagingData.CsInput.TUMOR_SIZE, "075");
        data.setInput(CsStagingData.CsInput.EXTENSION, "100");
        data.setInput(CsStagingData.CsInput.EXTENSION_EVAL, "9");
        data.setInput(CsStagingData.CsInput.LYMPH_NODES, "100");
        data.setInput(CsStagingData.CsInput.LYMPH_NODES_EVAL, "9");
        data.setInput(CsStagingData.CsInput.REGIONAL_NODES_POSITIVE, "99");
        data.setInput(CsStagingData.CsInput.REGIONAL_NODES_EXAMINED, "99");
        data.setInput(CsStagingData.CsInput.METS_AT_DX, "10");
        data.setInput(CsStagingData.CsInput.METS_EVAL, "9");
        data.setInput(CsStagingData.CsInput.LVI, "9");
        data.setInput(CsStagingData.CsInput.AGE_AT_DX, "060");
        data.setSsf(1, "020");

        // perform the staging
        _STAGING.stage(data);

        assertEquals(Result.STAGED, data.getResult());
        assertEquals("urethra", data.getSchemaId());
        assertEquals(0, data.getErrors().size());
        assertEquals(37, data.getPath().size());

        // check output
        assertEquals("129", data.getOutput(CsOutput.SCHEMA_NUMBER));
        assertEquals("020550", data.getOutput(CsOutput.CSVER_DERIVED));

        // AJCC 6
        assertEquals("T1", data.getOutput(CsOutput.AJCC6_T));
        assertEquals("c", data.getOutput(CsOutput.AJCC6_TDESCRIPTOR));
        assertEquals("N1", data.getOutput(CsOutput.AJCC6_N));
        assertEquals("c", data.getOutput(CsOutput.AJCC6_NDESCRIPTOR));
        assertEquals("M1", data.getOutput(CsOutput.AJCC6_M));
        assertEquals("c", data.getOutput(CsOutput.AJCC6_MDESCRIPTOR));
        assertEquals("IV", data.getOutput(CsOutput.AJCC6_STAGE));
        assertEquals("10", data.getOutput(CsOutput.STOR_AJCC6_T));
        assertEquals("c", data.getOutput(CsOutput.STOR_AJCC6_TDESCRIPTOR));
        assertEquals("10", data.getOutput(CsOutput.STOR_AJCC6_N));
        assertEquals("c", data.getOutput(CsOutput.STOR_AJCC6_NDESCRIPTOR));
        assertEquals("10", data.getOutput(CsOutput.STOR_AJCC6_M));
        assertEquals("c", data.getOutput(CsOutput.STOR_AJCC6_MDESCRIPTOR));
        assertEquals("70", data.getOutput(CsOutput.STOR_AJCC6_STAGE));

        // AJCC 7
        assertEquals("T1", data.getOutput(CsOutput.AJCC7_T));
        assertEquals("c", data.getOutput(CsOutput.AJCC7_TDESCRIPTOR));
        assertEquals("N1", data.getOutput(CsOutput.AJCC7_N));
        assertEquals("c", data.getOutput(CsOutput.AJCC7_NDESCRIPTOR));
        assertEquals("M1", data.getOutput(CsOutput.AJCC7_M));
        assertEquals("c", data.getOutput(CsOutput.AJCC7_MDESCRIPTOR));
        assertEquals("IV", data.getOutput(CsOutput.AJCC7_STAGE));
        assertEquals("100", data.getOutput(CsOutput.STOR_AJCC7_T));
        assertEquals("c", data.getOutput(CsOutput.STOR_AJCC6_TDESCRIPTOR));
        assertEquals("100", data.getOutput(CsOutput.STOR_AJCC7_N));
        assertEquals("c", data.getOutput(CsOutput.STOR_AJCC7_NDESCRIPTOR));
        assertEquals("100", data.getOutput(CsOutput.STOR_AJCC7_M));
        assertEquals("c", data.getOutput(CsOutput.STOR_AJCC7_MDESCRIPTOR));
        assertEquals("700", data.getOutput(CsOutput.STOR_AJCC7_STAGE));

        // Summary Stage
        assertEquals("L", data.getOutput(CsOutput.SS1977_T));
        assertEquals("RN", data.getOutput(CsOutput.SS1977_N));
        assertEquals("D", data.getOutput(CsOutput.SS1977_M));
        assertEquals("D", data.getOutput(CsOutput.SS1977_STAGE));
        assertEquals("L", data.getOutput(CsOutput.SS2000_T));
        assertEquals("RN", data.getOutput(CsOutput.SS2000_N));
        assertEquals("D", data.getOutput(CsOutput.SS2000_M));
        assertEquals("D", data.getOutput(CsOutput.SS2000_STAGE));
        assertEquals("7", data.getOutput(CsOutput.STOR_SS1977_STAGE));
        assertEquals("7", data.getOutput(CsOutput.STOR_SS2000_STAGE));

        // make sure defaulted inputs are not in the output
        Set<String> outputKeys = data.getOutput().keySet();
        for (CsOutput output : CsOutput.values())
            outputKeys.remove(output.toString());
        assertTrue("The keys " + outputKeys + " were in the output but are not CS output fields.", outputKeys.isEmpty());

        // test case with valid year_dx and invalid version original
        data.setInput(CsStagingData.CsInput.DX_YEAR, "2013");
        data.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "1111");
        _STAGING.stage(data);
        assertEquals(Result.STAGED, data.getResult());
        assertEquals("urethra", data.getSchemaId());
        assertEquals(1, data.getErrors().size());
        assertEquals(Type.INVALID_REQUIRED_INPUT, data.getErrors().get(0).getType());

        // test case with missing year_dx and valid version original
        data.setInput(CsStagingData.CsInput.DX_YEAR, "");
        data.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "020550");
        _STAGING.stage(data);
        assertEquals(Result.STAGED, data.getResult());
        assertEquals("urethra", data.getSchemaId());
        assertEquals(0, data.getErrors().size());

        // test case with missing year_dx and valid version original
        data.setInput(CsStagingData.CsInput.DX_YEAR, "");
        data.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "020001");
        _STAGING.stage(data);
        assertEquals(Result.STAGED, data.getResult());
        assertEquals("urethra", data.getSchemaId());
        assertEquals(0, data.getErrors().size());

        // test case with space-filled year_dx and valid version original
        data.setInput(CsStagingData.CsInput.DX_YEAR, "    ");
        data.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "020001");
        _STAGING.stage(data);
        assertEquals(Result.STAGED, data.getResult());
        assertEquals("urethra", data.getSchemaId());
        assertEquals(0, data.getErrors().size());

        // test case with missing year_dx and invalid version original
        data.setInput(CsStagingData.CsInput.DX_YEAR, "");
        data.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "012345");
        _STAGING.stage(data);
        assertEquals(Result.FAILED_INVALID_YEAR_DX, data.getResult());
        assertEquals("urethra", data.getSchemaId());
        assertEquals(0, data.getErrors().size());

        // test case with missing year_dx and invalid version original
        data.setInput(CsStagingData.CsInput.DX_YEAR, "");
        data.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "1");
        _STAGING.stage(data);
        assertEquals(Result.FAILED_INVALID_YEAR_DX, data.getResult());
        assertEquals("urethra", data.getSchemaId());
        assertEquals(0, data.getErrors().size());

        // test case with space-filled year_dx and invalid version original
        data.setInput(CsStagingData.CsInput.DX_YEAR, "    ");
        data.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "012345");
        _STAGING.stage(data);
        assertEquals(Result.FAILED_INVALID_YEAR_DX, data.getResult());
        assertEquals("urethra", data.getSchemaId());
        assertEquals(0, data.getErrors().size());

        data.setInput(CsStagingData.CsInput.DX_YEAR, "2003");
        data.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "020550");
        _STAGING.stage(data);
        assertEquals(Result.FAILED_INVALID_YEAR_DX, data.getResult());
        assertEquals("urethra", data.getSchemaId());
        assertEquals(0, data.getOutput().size());
        assertEquals(0, data.getErrors().size());
        assertEquals(0, data.getPath().size());

        data.setInput(CsStagingData.CsInput.DX_YEAR, "2050");
        data.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "020550");
        _STAGING.stage(data);
        assertEquals(Result.FAILED_INVALID_YEAR_DX, data.getResult());
        assertEquals("urethra", data.getSchemaId());
        assertEquals(0, data.getOutput().size());
        assertEquals(0, data.getErrors().size());
        assertEquals(0, data.getPath().size());
    }

    @Test
    public void testBadLookupInStage() {
        CsStagingData data = new CsStagingData();

        // if site/hist are not supplied, no lookup
        _STAGING.stage(data);
        assertEquals(Result.FAILED_MISSING_SITE_OR_HISTOLOGY, data.getResult());

        // add hist only and it should fail with same result
        data.setInput(CsStagingData.CsInput.PRIMARY_SITE, "C489");
        _STAGING.stage(data);
        assertEquals(Result.FAILED_MISSING_SITE_OR_HISTOLOGY, data.getResult());

        // put a site/hist combo that doesn't match a schema
        data.setInput(CsStagingData.CsInput.HISTOLOGY, "9898");
        _STAGING.stage(data);
        assertEquals(Result.FAILED_NO_MATCHING_SCHEMA, data.getResult());

        // now a site/hist that returns multiple results
        data.setInput(CsStagingData.CsInput.PRIMARY_SITE, "C111");
        data.setInput(CsStagingData.CsInput.HISTOLOGY, "8200");
        _STAGING.stage(data);
        assertEquals(Result.FAILED_MULITPLE_MATCHING_SCHEMAS, data.getResult());
    }

    @Test
    public void testInvolvedTables() {
        Set<String> tables = _STAGING.getInvolvedTables("brain");

        assertEquals(
                new HashSet<>(
                        Arrays.asList("cs_year_validation", "schema_selection_brain", "ajcc6_m_codes", "ajcc7_m_codes", "ssf22_snq", "nodes_exam_gna", "ssf13_snh",
                                "ssf7_sqk", "lvi", "ajcc6_n_codes", "ssf18_snm", "ssf15_snj", "ssf20_sno", "ssf10_sne", "ssf17_snl", "ssf6_opf", "summary_stage_rpa",
                                "histology", "ss_codes", "mets_haw", "nodes_pos_fna", "ajcc7_stage_una", "ajcc7_year_validation", "ssf4_mpn", "mets_eval_ina", "ssf3_lpm",
                                "primary_site", "nodes_dna", "ajcc6_stage_qna", "ssf8_sql", "ssf19_snn", "ssf2_kpl", "ajcc7_t_codes", "behavior", "nodes_eval_ena",
                                "ajcc_tdescriptor_cleanup", "ssf21_snp", "ajcc_descriptor_codes", "ssf16_snk", "ajcc6_t_codes", "ssf5_nph", "ajcc7_n_codes",
                                "ajcc6_stage_codes", "extension_bcc", "grade", "size_apa", "ajcc_ndescriptor_cleanup", "ssf12_sng", "ssf23_snr", "ajcc7_inclusions_tqf",
                                "ajcc7_stage_codes", "extension_eval_cna", "ajcc_mdescriptor_cleanup", "cs_input_version_original", "ajcc6_year_validation", "ssf1_jpo",
                                "ssf25_snt", "ssf11_snf", "ssf9_snd", "ssf14_sni", "ssf24_sns")), tables);
    }

    @Test
    public void testInvolvedSchemas() {
        Set<String> schemas = _STAGING.getInvolvedSchemas("ssf1_jpd");

        assertEquals(new HashSet<>(Arrays.asList("kidney_renal_pelvis", "bladder", "urethra")), schemas);
    }

    @Test
    public void testGetInputs() {
        assertEquals(new HashSet<>(Arrays.asList("extension", "site", "extension_eval", "mets_eval", "nodes_eval", "nodes", "hist", "year_dx", "cs_input_version_original",
                "mets")), _STAGING.getInputs(_STAGING.getSchema("adnexa_uterine_other")));

        assertEquals(
                new HashSet<>(Arrays.asList("site", "nodes_pos", "mets_eval", "nodes_eval", "ssf16", "ssf15", "ssf13", "cs_input_version_original", "lvi", "extension",
                        "extension_eval", "ssf1", "ssf2", "ssf3", "hist", "ssf4", "nodes", "ssf5", "year_dx", "mets")), _STAGING.getInputs(_STAGING.getSchema("testis")));

        // test with context
        Map<String, String> context = new HashMap<>();
        context.put(StagingData.PRIMARY_SITE_KEY, "C619");
        context.put(StagingData.HISTOLOGY_KEY, "8120");
        context.put(StagingData.YEAR_DX_KEY, "2004");

        // for that context, neither AJCC6 or 7 should be calculated so "grade" and "ssf1" should not be list of inputs
        assertEquals(new HashSet<>(Arrays.asList("site", "nodes_eval", "mets_eval", "ssf10", "cs_input_version_original", "ssf8", "extension", "extension_eval",
                "ssf3", "hist", "nodes", "year_dx", "mets")), _STAGING.getInputs(_STAGING.getSchema("prostate"), context));

        // test without context
        assertEquals(
                new HashSet<>(Arrays.asList("site", "nodes_eval", "mets_eval", "ssf10", "cs_input_version_original", "ssf8", "extension", "extension_eval", "ssf1",
                        "ssf3", "hist", "nodes", "year_dx", "grade", "mets")), _STAGING.getInputs(_STAGING.getSchema("prostate")));
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
        assertTrue(_STAGING.isCodeValid("urethra", "year_dx", "2004"));
        assertTrue(_STAGING.isCodeValid("urethra", "year_dx", "2015"));

        // test valid and invalid fields
        assertTrue(_STAGING.isCodeValid("urethra", "extension", "050"));
        assertFalse(_STAGING.isCodeValid("urethra", "extension", "025"));
        assertTrue(_STAGING.isCodeValid("urethra", "ssf1", "020"));
        assertFalse(_STAGING.isCodeValid("urethra", "ssf1", "030"));
    }

    @Test
    public void testIsContextValid() {
        CsStagingData data = new CsStagingData();

        data.setInput(Staging.CTX_YEAR_CURRENT, "2015");

        // test valid year
        data.setInput(CsStagingData.CsInput.DX_YEAR, "2004");
        data.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "020001");
        assertTrue(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput()));

        // test invalid year
        data.setInput(CsStagingData.CsInput.DX_YEAR, "2003");
        assertFalse(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput()));

        // test blank year with valid version
        data.setInput(CsStagingData.CsInput.DX_YEAR, "");
        data.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "020001");
        assertTrue(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput()));

        // test space-filled year with valid version
        data.setInput(CsStagingData.CsInput.DX_YEAR, "    ");
        data.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "020001");
        assertTrue(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput()));

        // test blank year with invalid version
        data.setInput(CsStagingData.CsInput.DX_YEAR, "");
        data.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "000000");
        assertFalse(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput()));

        // test blank year with invalid version of wrong length
        data.setInput(CsStagingData.CsInput.DX_YEAR, "");
        data.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "1");
        assertFalse(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput()));

        // test space-filled year with invalid version
        data.setInput(CsStagingData.CsInput.DX_YEAR, "    ");
        data.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "000000");
        assertFalse(_STAGING.isContextValid("urethra", StagingData.YEAR_DX_KEY, data.getInput()));

        // test space-filled year with invalid version of wrong length
        data.setInput(CsStagingData.CsInput.DX_YEAR, "    ");
        data.setInput(CsStagingData.CsInput.CS_VERSION_ORIGINAL, "1");
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
        assertTrue(tables.contains("ajcc7_stage_uaz"));
    }

    @Test
    public void testGetSchema() {
        assertNull(_STAGING.getSchema("bad_schema_name"));
        assertNotNull(_STAGING.getSchema("brain"));
        assertEquals(Integer.valueOf(143), _STAGING.getSchema("brain").getSchemaNum());
    }

    @Test
    public void testStagingInputsAndOutputs() {
        StagingSchema schema = _STAGING.getSchema("testis");

        assertEquals("Inputs do not match",
                new HashSet<>(Arrays.asList("cs_input_version_original", "extension", "extension_eval", "site", "hist", "lvi", "mets_eval", "mets", "nodes",
                        "nodes_eval", "nodes_pos", "ssf1", "ssf2", "ssf3", "ssf4", "ssf5", "ssf13", "ssf15", "ssf16", "year_dx")), _STAGING.getInputs(schema));

        // note that outputs should NOT include values produced by staging that are not in the defined output list (if an output list exists on the schema)
        assertEquals("Outputs do not match",
                new HashSet<>(Arrays.asList("schema_number", "csver_derived", "ss77", "stor_ajcc7_m", "t2000", "stor_ajcc7_n", "stor_ajcc6_tdescriptor", "ajcc7_stage",
                        "stor_ajcc6_mdescriptor", "stor_ss2000", "ajcc6_tdescriptor", "stor_ajcc7_t", "ajcc6_stage", "n2000", "ajcc7_ndescriptor", "ajcc6_ndescriptor",
                        "ajcc7_mdescriptor", "ajcc6_mdescriptor", "stor_ajcc7_stage", "m77", "ajcc6_m", "ss2000", "stor_ajcc7_ndescriptor", "ajcc7_m", "ajcc7_n",
                        "stor_ajcc7_mdescriptor", "t77", "ajcc6_n", "stor_ss77", "ajcc6_t", "stor_ajcc6_ndescriptor", "stor_ajcc6_stage", "m2000", "ajcc7_t", "n77",
                        "ajcc7_tdescriptor", "stor_ajcc6_m", "stor_ajcc6_n", "stor_ajcc6_t", "stor_ajcc7_tdescriptor")), _STAGING.getOutputs(schema));

        // test used for staging
        assertFalse(schema.getInputMap().get("ssf14").getUsedForStaging());
        assertTrue(schema.getInputMap().get("ssf15").getUsedForStaging());

        // test metadata
        assertNull(schema.getInputMap().get("ssf11").getMetadata());
        assertTrue(schema.getInputMap().get("ssf17").getMetadata().contains("UNDEFINED_SSF"));
        assertTrue(schema.getInputMap().get("ssf7").getMetadata().contains("SEER_CLINICALLY_SIGNIFICANT"));

        Map<String, String> context = new HashMap<>();
        context.put(StagingData.PRIMARY_SITE_KEY, "C629");
        context.put(StagingData.HISTOLOGY_KEY, "9231");
        Set<String> inputs = _STAGING.getInputs(schema, context);

        // this is a case that summary stages only.  Testing to make sure "hist", which is used in the inclusion/exclusion criteria
        // is included in the list even though the mappings for AJCC6 and 7 are not included
        assertTrue("Inclusion/exclusion input is not included", inputs.contains("hist"));

        assertTrue(inputs.contains("mets"));

        // these are no used when only doing summary stage
        assertFalse(inputs.contains("ssf1"));
        assertFalse(inputs.contains("ssf2"));
        assertFalse(inputs.contains("ssf3"));
        assertFalse(inputs.contains("ssf13"));
        assertFalse(inputs.contains("ssf15"));
        assertFalse(inputs.contains("ssf16"));

        // now test one that does do AJCC7 (inputs should include extra SSFs used in AJCC calculations)
        context.put(StagingData.HISTOLOGY_KEY, "9100");
        inputs = _STAGING.getInputs(schema, context);
        assertTrue(inputs.contains("hist"));
        assertTrue(inputs.contains("ssf1"));
        assertTrue(inputs.contains("ssf2"));
        assertTrue(inputs.contains("ssf3"));
        assertTrue(inputs.contains("ssf13"));
        assertTrue(inputs.contains("ssf15"));
        assertTrue(inputs.contains("ssf16"));

        // the prostate schema tables use a reference to {{ssf8}} and {{ssf10}}; make sure they are picked up in the list of required inputs
        inputs = _STAGING.getInputs(_STAGING.getSchema("prostate"));
        assertTrue(inputs.contains("ssf8"));
        assertTrue(inputs.contains("ssf10"));
    }

    @Test
    public void testExpectedOutput() throws Exception {
        IntegrationResult ajcc6Result = IntegrationUtils.processSchema(_STAGING, "AJCC_6.V020550.txt.gz",
                new GZIPInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("test-data/cs/020550/AJCC_6.V020550.txt.gz")));
        IntegrationResult ajcc7Result = IntegrationUtils.processSchema(_STAGING, "AJCC_7.V020550.txt.gz",
                new GZIPInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("test-data/cs/020550/AJCC_7.V020550.txt.gz")));

        // make sure there were no errors returned
        assertEquals("There were failures in the AJCC6 tests", 0, ajcc6Result.getNumFailures());
        assertEquals("There were failures in the AJCC7 tests", 0, ajcc7Result.getNumFailures());
    }

    @Test
    public void testAllValidInputs() throws IOException {
        GZIPInputStream is = new GZIPInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("test-data/cs/020550/valid_inputs.020550.txt.gz"));
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(is, "UTF-8"));

        // cache a list of schemas by name
        Map<String, String> nameMap = new HashMap<>();
        for (String id : _STAGING.getSchemaIds())
            nameMap.put(_STAGING.getSchema(id).getName(), id);

        String line = reader.readLine();
        while (line != null) {
            // split the CSV record
            String[] values = line.split("\\|");
            assertEquals(3, values.length);

            // get schema by schema name
            StagingSchema schema = _STAGING.getSchema(nameMap.get(values[0]));
            assertTrue("The value '" + values[2] + "' is not valid for table '" + values[1] + "' and schema '" + values[0] + "'", _STAGING.isCodeValid(schema.getId(), values[1], values[2]));

            line = reader.readLine();
        }

        System.out.println("Processed " + reader.getLineNumber() + " lines.");

        reader.close();
    }

    /**
     * This tests that INPUT fields in tables that have a validation table associated with them are the correct length.  In other words,
     * if a table has an INPUT column for "ssf4" but has a value for that column of "00" this would catch that that field should be
     * 3 characters long based on the ssf4_lookup_table.
     */
    @Test
    public void testInvalidTableInputs() {
        Set<String> errors = new HashSet<>();

        for (String schemaId : _STAGING.getSchemaIds()) {
            StagingSchema schema = _STAGING.getSchema(schemaId);

            // build a list of input tables that should be excluded
            Map<String, Integer> inputTableLengths = new HashMap<>();
            for (StagingSchemaInput input : schema.getInputs())
                if (input.getTable() != null)
                    inputTableLengths.put(input.getTable(), getInputLength(input.getTable(), input.getKey()));

            // loop over involved tables
            for (String tableId : schema.getInvolvedTables()) {
                if (inputTableLengths.containsKey(tableId))
                    continue;

                StagingTable table = _STAGING.getTable(tableId);

                // loop over each row
                for (StagingTableRow row : table.getTableRows()) {
                    // loop over all input cells
                    for (Map.Entry<String, List<StagingStringRange>> entry : row.getInputs().entrySet()) {
                        String key = entry.getKey();

                        // only validate keys that are actually INPUT values
                        if (!schema.getInputMap().containsKey(key))
                            continue;

                        // only validate inputs that have an associated table
                        String validationTableId = schema.getInputMap().get(key).getTable();
                        if (validationTableId == null)
                            continue;

                        Integer expectedFieldLength = inputTableLengths.get(validationTableId);

                        // loop over list of ranges
                        for (StagingStringRange range : entry.getValue()) {
                            String low = range.getLow();
                            String high = range.getHigh();

                            // if it matches all, continue
                            if (range.matchesAll() || low.isEmpty())
                                continue;

                            if (low.startsWith("{{") && low.contains(Staging.CTX_YEAR_CURRENT))
                                low = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
                            if (high.startsWith("{{") && high.contains(Staging.CTX_YEAR_CURRENT))
                                high = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

                            // change that ranges are the same length
                            if (low.length() != high.length())
                                errors.add(schemaId + " -> " + tableId + ": " + key + " = '" + low + "-" + high + "' : lengths differ");

                            // make sure the fields that have input validation match the length in that input validation table
                            if (expectedFieldLength != null && (!expectedFieldLength.equals(low.length()) || !expectedFieldLength.equals(high.length()))) {
                                if (low.equals(high))
                                    errors.add(schemaId + " -> " + tableId + ": " + key + " = '" + low + "' : length does not match lookup table " + validationTableId);
                                else
                                    errors.add(schemaId + " -> " + tableId + ": " + key + " = '" + low + "-" + high + "' : lengths do not match lookup table " + validationTableId);
                            }
                        }
                    }
                }
            }
        }

        assertNoErrors(errors, "inputs values in tables which are not valid");
    }
}
