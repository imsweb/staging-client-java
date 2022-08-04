package com.imsweb.staging.entities;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SchemaLookupTest {

    @Test
    void testConstructorMissingValues() {
        Assertions.assertEquals(new SchemaLookup().getKeys(), new HashSet<>());
        Assertions.assertEquals(new SchemaLookup(null, null).getKeys(), new HashSet<>());
        Assertions.assertEquals(new SchemaLookup("", null).getKeys(), new HashSet<>());
        Assertions.assertEquals(new SchemaLookup(null, "").getKeys(), new HashSet<>());
        Assertions.assertEquals(new SchemaLookup("", "").getKeys(), new HashSet<>());

        Assertions.assertEquals(new SchemaLookup("C629", null).getKeys(), Stream.of("site").collect(Collectors.toSet()));
        Assertions.assertEquals(new SchemaLookup("C629", "").getKeys(), Stream.of("site").collect(Collectors.toSet()));
        Assertions.assertEquals(new SchemaLookup(null, "9100").getKeys(), Stream.of("hist").collect(Collectors.toSet()));
        Assertions.assertEquals(new SchemaLookup("", "9100").getKeys(), Stream.of("hist").collect(Collectors.toSet()));
        Assertions.assertEquals(new SchemaLookup("C629", "9100").getKeys(), Stream.of("site", "hist").collect(Collectors.toSet()));
    }

}