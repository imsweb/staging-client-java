package com.imsweb.staging.cs;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
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

    @Test
    public void testGetKeys() {
        assertEquals(new CsSchemaLookup("C629", "9100").getKeys(), Stream.of("site", "hist").collect(Collectors.toSet()));
        assertEquals(new CsSchemaLookup("C629", "9100", "001").getKeys(), Stream.of("site", "hist", "ssf25").collect(Collectors.toSet()));
    }

}