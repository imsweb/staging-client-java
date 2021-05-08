package com.imsweb.staging;

import com.imsweb.decisionengine.Schema;
import com.imsweb.decisionengine.Table;
import com.imsweb.staging.StagingData.Result;
import com.imsweb.staging.entities.GlossaryDefinition;
import com.imsweb.staging.entities.GlossaryHit;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

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

}