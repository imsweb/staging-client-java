package com.imsweb.staging.tnm;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TnmSchemaLookupTest {

    @Test
    public void testDescriminator() {
        TnmSchemaLookup lookup = new TnmSchemaLookup("C629", "9100");
        assertFalse(lookup.hasDiscriminator());
        lookup.setInput(TnmStagingData.SSF25_KEY, "");
        assertFalse(lookup.hasDiscriminator());
        lookup.setInput(TnmStagingData.SSF25_KEY, "001");
        assertTrue(lookup.hasDiscriminator());
    }

    @Test(expected = IllegalStateException.class)
    public void testBadKey() {
        TnmSchemaLookup lookup = new TnmSchemaLookup("C509", "8000");
        lookup.setInput("bad_key", "111");
    }

}