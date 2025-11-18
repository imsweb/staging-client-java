package com.imsweb.staging.cs;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsSchemaLookupTest {

    @Test
    void testDescriminator() {
        assertFalse(new CsSchemaLookup("C629", "9100", null).hasDiscriminator());
        assertFalse(new CsSchemaLookup("C629", "9100", "").hasDiscriminator());
        assertTrue(new CsSchemaLookup("C629", "9100", "001").hasDiscriminator());

        CsSchemaLookup lookup = new CsSchemaLookup("C509", "8000");
        assertFalse(lookup.hasDiscriminator());
        lookup.setInput(CsStagingData.SSF25_KEY, "111");
        assertTrue(lookup.hasDiscriminator());
    }

    @Test
    void testBadKey() {
        CsSchemaLookup lookup = new CsSchemaLookup("C509", "8000");
        assertThrows(IllegalStateException.class, () -> lookup.setInput("bad_key", "111"));
    }

    @Test
    void testGetKeys() {
        assertEquals(new CsSchemaLookup("C629", "9100").getKeys(), Stream.of("site", "hist").collect(Collectors.toSet()));
        assertEquals(new CsSchemaLookup("C629", "9100", "001").getKeys(), Stream.of("site", "hist", "ssf25").collect(Collectors.toSet()));
    }

}