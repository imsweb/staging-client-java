package com.imsweb.staging.cs;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CsSchemaLookupTest {

    @Test
    void testDescriminator() {
        Assertions.assertFalse(new CsSchemaLookup("C629", "9100", null).hasDiscriminator());
        Assertions.assertFalse(new CsSchemaLookup("C629", "9100", "").hasDiscriminator());
        Assertions.assertTrue(new CsSchemaLookup("C629", "9100", "001").hasDiscriminator());

        CsSchemaLookup lookup = new CsSchemaLookup("C509", "8000");
        Assertions.assertFalse(lookup.hasDiscriminator());
        lookup.setInput(CsStagingData.SSF25_KEY, "111");
        Assertions.assertTrue(lookup.hasDiscriminator());
    }

    @Test
    void testBadKey() {
        CsSchemaLookup lookup = new CsSchemaLookup("C509", "8000");
        Assertions.assertThrows(IllegalStateException.class, () -> lookup.setInput("bad_key", "111"));
    }

    @Test
    void testGetKeys() {
        Assertions.assertEquals(new CsSchemaLookup("C629", "9100").getKeys(), Stream.of("site", "hist").collect(Collectors.toSet()));
        Assertions.assertEquals(new CsSchemaLookup("C629", "9100", "001").getKeys(), Stream.of("site", "hist", "ssf25").collect(Collectors.toSet()));
    }

}