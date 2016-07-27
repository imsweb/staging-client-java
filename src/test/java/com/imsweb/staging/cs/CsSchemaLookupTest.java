package com.imsweb.staging.cs;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CsSchemaLookupTest {

    @Test
    public void testDescriminator() {
        assertFalse(new CsSchemaLookup("C629", "9100", null).hasDiscriminator());
        assertFalse(new CsSchemaLookup("C629", "9100", "").hasDiscriminator());
        assertTrue(new CsSchemaLookup("C629", "9100", "001").hasDiscriminator());

        CsSchemaLookup lookup = new CsSchemaLookup("C509", "8000");
        assertFalse(lookup.hasDiscriminator());
        lookup.setInput(CsStagingData.SSF25_KEY, "111");
        assertTrue(lookup.hasDiscriminator());
    }

    @Test(expected = IllegalStateException.class)
    public void testBadKey() {
        CsSchemaLookup lookup = new CsSchemaLookup("C509", "8000");
        lookup.setInput("bad_key", "111");
    }

}