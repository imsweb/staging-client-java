package com.imsweb.staging.tnm;

import org.junit.Assert;
import org.junit.Test;

public class TnmSchemaLookupTest {

    @Test
    public void testDescriminator() {
        TnmSchemaLookup lookup = new TnmSchemaLookup("C629", "9100");
        Assert.assertFalse(lookup.hasDiscriminator());
        lookup.setInput(TnmStagingData.SSF25_KEY, "");
        Assert.assertFalse(lookup.hasDiscriminator());
        lookup.setInput(TnmStagingData.SSF25_KEY, "001");
        Assert.assertTrue(lookup.hasDiscriminator());
    }

    @Test(expected = IllegalStateException.class)
    public void testBadKey() {
        TnmSchemaLookup lookup = new TnmSchemaLookup("C509", "8000");
        lookup.setInput("bad_key", "111");
    }

}