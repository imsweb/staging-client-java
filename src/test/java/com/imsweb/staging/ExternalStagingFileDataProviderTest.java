package com.imsweb.staging;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.imsweb.staging.entities.GlossaryDefinition;
import com.imsweb.staging.entities.GlossaryHit;
import com.imsweb.staging.entities.Input;
import com.imsweb.staging.entities.Schema;
import com.imsweb.staging.entities.StagingData;
import com.imsweb.staging.entities.StagingData.Result;
import com.imsweb.staging.entities.Table;
import com.imsweb.staging.entities.impl.StagingMetadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ExternalStagingFileDataProviderTest {

    private static Staging _STAGING;

    @BeforeClass
    public static void setup() throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("external_algorithm.zip");

        _STAGING = Staging.getInstance(new ExternalStagingFileDataProvider(is));
    }

    @Test
    public void testExternalLoad() {
        assertEquals("testing", _STAGING.getAlgorithm());
        assertEquals("99.99", _STAGING.getVersion());
        assertEquals(1, _STAGING.getSchemaIds().size());
        assertEquals(62, _STAGING.getTableIds().size());

        Schema schema = _STAGING.getSchema("urethra");
        assertNotNull(schema);
        assertEquals("testing", schema.getAlgorithm());
        assertEquals("99.99", schema.getVersion());

        Table table = _STAGING.getTable("ajcc_descriptor_codes");
        assertNotNull(table);
        assertEquals("testing", table.getAlgorithm());
        assertEquals("99.99", table.getVersion());
        assertEquals(6, table.getTableRows().size());

        Set<String> involved = _STAGING.getInvolvedTables("urethra");
        assertEquals(62, involved.size());
        assertTrue(involved.contains("mets_eval_ipa"));

        StagingData data = new StagingData();
        data.setInput("site", "C680");
        data.setInput("hist", "8000");
        data.setInput("behavior", "3");
        data.setInput("grade", "9");
        data.setInput("year_dx", "2013");
        data.setInput("cs_input_version_original", "020550");
        data.setInput("extension", "100");
        data.setInput("extension_eval", "9");
        data.setInput("nodes", "100");
        data.setInput("nodes_eval", "9");
        data.setInput("mets", "10");
        data.setInput("mets_eval", "9");

        // perform the staging
        _STAGING.stage(data);

        assertEquals(Result.STAGED, data.getResult());
        assertEquals("urethra", data.getSchemaId());
        assertEquals(0, data.getErrors().size());
        assertEquals(37, data.getPath().size());

        // check output
        assertEquals("129", data.getOutput("schema_number"));
        assertEquals("020550", data.getOutput("csver_derived"));

        // AJCC 6
        assertEquals("70", data.getOutput("stor_ajcc6_stage"));

        // AJCC 7
        assertEquals("700", data.getOutput("stor_ajcc7_stage"));

        // Summary Stage
        assertEquals("7", data.getOutput("stor_ss77"));
        assertEquals("7", data.getOutput("stor_ss2000"));

        // test glossary items
        assertEquals(13, _STAGING.getGlossaryTerms().size());
        GlossaryDefinition entry = _STAGING.getGlossaryDefinition("Adjacent tissue(s), NOS");
        assertNotNull(entry);
        assertEquals("Adjacent tissue(s), NOS", entry.getName());
        assertTrue(entry.getDefinition().startsWith("The unnamed tissues that immediately surround"));
        assertEquals(Collections.singletonList("Connective tissue"), entry.getAlternateNames());
        assertNotNull(entry.getLastModified());

        Collection<GlossaryHit> hits = _STAGING.getGlossaryMatches("Some text and Cortex should be only match.");
        assertEquals(1, hits.size());
        GlossaryHit hit = hits.iterator().next();
        assertEquals("cortex", hit.getTerm());
        assertEquals(14, (long)hit.getBegin());
        assertEquals(19, (long)hit.getEnd());
        hits = _STAGING.getGlossaryMatches("Cortex and stroma should be two matches.");
        assertEquals(2, hits.size());
        hits = _STAGING.getGlossaryMatches("stromafoo not be a match since the keyword it is not a whole word");
        assertEquals(0, hits.size());

        Set<String> glossary = _STAGING.getSchemaGlossary("urethra");
        assertEquals(1, glossary.size());
        glossary = _STAGING.getTableGlossary("ssf1_jpd");
        assertEquals(1, glossary.size());
    }

    @Test
    public void testMetadata() {
        // verify if reads metadata in old format (List<String>) and new format (List<Metadata>)

        Schema urethra = _STAGING.getSchema("urethra");

        assertThat(urethra).isNotNull();

        // old structure
        Input ssf1 = urethra.getInputMap().get("ssf1");
        assertThat(ssf1.getMetadata()).extracting("name").containsExactlyInAnyOrder("CCCR_IVA_COLLECT_IF_AVAILABLE_IN_PATH_REPORT", "COC_CLINICALLY_SIGNIFICANT", "SEER_CLINICALLY_SIGNIFICANT");

        // new structure
        Input ssf2 = urethra.getInputMap().get("ssf2");
        assertThat(ssf2.getMetadata()).extracting("name").containsExactlyInAnyOrder("UNDEFINED_SSF");

        Input ssf3 = urethra.getInputMap().get("ssf3");
        assertThat(ssf3.getMetadata()).hasSize(2);

        Set<StagingMetadata> expected = new HashSet<>();
        expected.add(new StagingMetadata("FIRST_ITEM", 2017, 2020));
        expected.add(new StagingMetadata("SECOND_ITEM", 2021));
        assertThat(ssf3.getMetadata()).isEqualTo(expected);
    }

}