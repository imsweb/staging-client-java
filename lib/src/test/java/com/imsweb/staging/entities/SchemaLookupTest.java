package com.imsweb.staging.entities;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchemaLookupTest {

    @Test
    void testConstructorMissingValues() {
        assertEquals(new HashSet<>(), new SchemaLookup().getKeys());
        assertEquals(new HashSet<>(), new SchemaLookup(null, null).getKeys());
        assertEquals(new HashSet<>(), new SchemaLookup("", null).getKeys());
        assertEquals(new HashSet<>(), new SchemaLookup(null, "").getKeys());
        assertEquals(new HashSet<>(), new SchemaLookup("", "").getKeys());

        assertEquals(new SchemaLookup("C629", null).getKeys(), Stream.of("site").collect(Collectors.toSet()));
        assertEquals(new SchemaLookup("C629", "").getKeys(), Stream.of("site").collect(Collectors.toSet()));
        assertEquals(new SchemaLookup(null, "9100").getKeys(), Stream.of("hist").collect(Collectors.toSet()));
        assertEquals(new SchemaLookup("", "9100").getKeys(), Stream.of("hist").collect(Collectors.toSet()));
        assertEquals(new SchemaLookup("C629", "9100").getKeys(), Stream.of("site", "hist").collect(Collectors.toSet()));
    }

}