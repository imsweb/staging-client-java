package com.imsweb.staging.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

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

    @Test
    void testSiteAndHistologyMutation() {
        SchemaLookup lookup = new SchemaLookup("C629", "9100");

        lookup.setSite("C509");
        lookup.setHistology("8000");

        assertEquals("C509", lookup.getSite());
        assertEquals("C509", lookup.getInput(StagingData.PRIMARY_SITE_KEY));
        assertEquals("8000", lookup.getHistology());
        assertEquals("8000", lookup.getInput(StagingData.HISTOLOGY_KEY));
    }

    @Test
    void testEqualsAndHashCode() {
        SchemaLookup lookup1 = new SchemaLookup("C629", "9100");
        SchemaLookup lookup2 = new SchemaLookup("C629", "9100");
        SchemaLookup lookup3 = new SchemaLookup("C629", "9100");

        assertEquals(lookup1, lookup1);
        assertEquals(lookup1, lookup2);
        assertEquals(lookup2, lookup1);
        assertEquals(lookup2, lookup3);
        assertEquals(lookup1, lookup3);
        assertEquals(lookup1.hashCode(), lookup2.hashCode());
    }

    @Test
    void testNotEquals() {
        SchemaLookup lookup = new SchemaLookup("C629", "9100");

        assertNotEquals(lookup, null);
        assertNotEquals(lookup, "C629");
        assertNotEquals(lookup, new TestSchemaLookup("C629", "9100"));
        assertNotEquals(new SchemaLookup("C509", "9100"), lookup);
        assertNotEquals(new SchemaLookup("C629", "8000"), lookup);
    }

    @Test
    void testClearInputs() {
        TestSchemaLookup lookup = new TestSchemaLookup("C629", "9100");
        lookup.setInput("allowed", "value");

        lookup.clear();

        assertTrue(lookup.getInputs().isEmpty());
    }

    @Test
    void testAllowedKeys() {
        SchemaLookup unrestrictedLookup = new SchemaLookup();
        unrestrictedLookup.setInput("anything", "value");
        assertEquals("value", unrestrictedLookup.getInput("anything"));

        TestSchemaLookup restrictedLookup = new TestSchemaLookup();
        restrictedLookup.setInput("allowed", "value");
        assertEquals("value", restrictedLookup.getInput("allowed"));
        assertThrows(IllegalStateException.class, () -> restrictedLookup.setInput("disallowed", "value"));
    }

    @Test
    void testDiscriminator() {
        SchemaLookup lookup = new SchemaLookup("C629", "9100");
        assertFalse(lookup.hasDiscriminator());

        lookup.setInput("discriminator", null);
        assertFalse(lookup.hasDiscriminator());

        lookup.setInput("discriminator", "");
        assertFalse(lookup.hasDiscriminator());

        lookup.setInput("discriminator", "001");
        assertTrue(lookup.hasDiscriminator());
    }

    private static class TestSchemaLookup extends SchemaLookup {

        TestSchemaLookup() {}

        TestSchemaLookup(String site, String histology) {
            super(site, histology);
        }

        @Override
        public Set<String> getAllowedKeys() {
            return Set.of(StagingData.PRIMARY_SITE_KEY, StagingData.HISTOLOGY_KEY, "allowed");
        }

        void clear() {
            clearInputs();
        }
    }
}
