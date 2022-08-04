package com.imsweb.staging.tnm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TnmSchemaLookupTest {

    @Test
    void testDescriminator() {
        TnmSchemaLookup lookup = new TnmSchemaLookup("C629", "9100");
        Assertions.assertFalse(lookup.hasDiscriminator());
        lookup.setInput(TnmStagingData.SSF25_KEY, "");
        Assertions.assertFalse(lookup.hasDiscriminator());
        lookup.setInput(TnmStagingData.SSF25_KEY, "001");
        Assertions.assertTrue(lookup.hasDiscriminator());
    }

    @Test
    void testBadKey() {
        TnmSchemaLookup lookup = new TnmSchemaLookup("C509", "8000");

        Assertions.assertThrows(IllegalStateException.class, () -> lookup.setInput("bad_key", "111"));
    }

}