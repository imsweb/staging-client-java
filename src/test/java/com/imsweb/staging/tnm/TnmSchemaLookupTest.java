package com.imsweb.staging.tnm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TnmSchemaLookupTest {

    @Test
    void testDescriminator() {
        TnmSchemaLookup lookup = new TnmSchemaLookup("C629", "9100");
        assertFalse(lookup.hasDiscriminator());
        lookup.setInput(TnmStagingData.SSF25_KEY, "");
        assertFalse(lookup.hasDiscriminator());
        lookup.setInput(TnmStagingData.SSF25_KEY, "001");
        assertTrue(lookup.hasDiscriminator());
    }

    @Test
    void testBadKey() {
        TnmSchemaLookup lookup = new TnmSchemaLookup("C509", "8000");

        assertThrows(IllegalStateException.class, () -> lookup.setInput("bad_key", "111"));
    }

}