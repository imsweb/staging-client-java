package com.imsweb.staging;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.imsweb.staging.entities.StagingRange;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StagingRangeTest {

    @Test
    public void testRanges() {
        assertFalse(new StagingRange("100", "103").contains("099", new HashMap<>()));
        assertTrue(new StagingRange("100", "103").contains("100", new HashMap<>()));
        assertTrue(new StagingRange("100", "103").contains("102", new HashMap<>()));
        assertTrue(new StagingRange("100", "103").contains("103", new HashMap<>()));
        assertFalse(new StagingRange("100", "103").contains("104", new HashMap<>()));

        // test that if the value is a shorter length it is not found to be a match
        assertFalse(new StagingRange("020500", "999999").contains("1", new HashMap<>()));
    }

    @Test
    public void testContext() {
        Map<String, String> context = new HashMap<>();
        StagingRange range = new StagingRange("2000", "{{current_year}}");

        assertFalse(range.contains("2004", context));

        context.put("current_year", "X");
        assertFalse(range.contains("2004", context));

        context.put("current_year", "");
        assertFalse(range.contains("2004", context));

        // this is a tricky one; the end result is 2000-XXXX, which 2004 is in the range; it's odd when one side is not a number
        // but since we are doing string ranges, this will match
        context.put("current_year", "XXXX");
        assertTrue(range.contains("2004", context));

        context.put("current_year", "1990");
        assertFalse(range.contains("2004", context));

        context.put("current_year", "2015");
        assertFalse(range.contains("2020", context));
        assertTrue(range.contains("2004", context));
    }

    @Test(expected = IllegalStateException.class)
    public void testNull() {
        assertFalse(new StagingRange(null, null).contains("099", new HashMap<>()));
    }

    @Test
    public void testEmptyBothValues() {
        assertFalse(new StagingRange("", "").contains("099", new HashMap<>()));
    }

    @Test
    public void testEmptyOneValues() {
        assertFalse(new StagingRange("999", "").contains("999", new HashMap<>()));
        assertFalse(new StagingRange("", "999").contains("999", new HashMap<>()));
    }

    @Test
    public void testDifferentLength() {
        assertFalse(new StagingRange("AA", "AAA").contains("AAA", new HashMap<>()));
        assertFalse(new StagingRange("BBB", "BB").contains("BBB", new HashMap<>()));

        assertFalse(new StagingRange("CCC", "CC").contains("CC", new HashMap<>()));
        assertFalse(new StagingRange("DD", "DDD").contains("DD", new HashMap<>()));

    }
}