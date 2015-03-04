package com.imsweb.staging.cs;

import org.junit.Assert;
import org.junit.Test;

public class CsSchemaLookupTest {

    @Test
    public void testDescriminator() {
        Assert.assertFalse(new CsSchemaLookup("C629", "9100", null).hasDiscriminator());
        Assert.assertFalse(new CsSchemaLookup("C629", "9100", "").hasDiscriminator());
        Assert.assertTrue(new CsSchemaLookup("C629", "9100", "001").hasDiscriminator());

        CsSchemaLookup lookup = new CsSchemaLookup("C509", "8000");
        Assert.assertFalse(lookup.hasDiscriminator());
        lookup.setInput(CsStagingData.SSF25_KEY, "111");
        Assert.assertTrue(lookup.hasDiscriminator());
    }

    @Test(expected = IllegalStateException.class)
    public void testBadKey() {
        CsSchemaLookup lookup = new CsSchemaLookup("C509", "8000");
        lookup.setInput("bad_key", "111");
    }

}