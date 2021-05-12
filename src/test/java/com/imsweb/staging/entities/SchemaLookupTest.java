package com.imsweb.staging.entities;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SchemaLookupTest {

    @Test
    public void testConstructorMissingValues() {
        assertEquals(new SchemaLookup().getKeys(), new HashSet<>());
        assertEquals(new SchemaLookup(null, null).getKeys(), new HashSet<>());
        assertEquals(new SchemaLookup("", null).getKeys(), new HashSet<>());
        assertEquals(new SchemaLookup(null, "").getKeys(), new HashSet<>());
        assertEquals(new SchemaLookup("", "").getKeys(), new HashSet<>());

        assertEquals(new SchemaLookup("C629", null).getKeys(), Stream.of("site").collect(Collectors.toSet()));
        assertEquals(new SchemaLookup("C629", "").getKeys(), Stream.of("site").collect(Collectors.toSet()));
        assertEquals(new SchemaLookup(null, "9100").getKeys(), Stream.of("hist").collect(Collectors.toSet()));
        assertEquals(new SchemaLookup("", "9100").getKeys(), Stream.of("hist").collect(Collectors.toSet()));
        assertEquals(new SchemaLookup("C629", "9100").getKeys(), Stream.of("site", "hist").collect(Collectors.toSet()));
    }

}