package com.imsweb.staging;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.imsweb.staging.entities.StagingStringRange;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StagingStringRangeTest {

    @Test
    public void testRanges() {
        assertFalse(new StagingStringRange("100", "103").contains("099", new HashMap<>()));
        assertTrue(new StagingStringRange("100", "103").contains("100", new HashMap<>()));
        assertTrue(new StagingStringRange("100", "103").contains("102", new HashMap<>()));
        assertTrue(new StagingStringRange("100", "103").contains("103", new HashMap<>()));
        assertFalse(new StagingStringRange("100", "103").contains("104", new HashMap<>()));

        // test that if the value is a shorter length it is not found to be a match
        assertFalse(new StagingStringRange("020500", "999999").contains("1", new HashMap<>()));
    }

    @Test
    public void testContext() {
        Map<String, String> context = new HashMap<>();
        StagingStringRange range = new StagingStringRange("2000", "{{current_year}}");

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
        assertFalse(new StagingStringRange(null, null).contains("099", new HashMap<>()));
    }

    @Test
    public void testEmptyBothValues() {
        assertFalse(new StagingStringRange("", "").contains("099", new HashMap<>()));
    }

    @Test
    public void testEmptyOneValues() {
        assertFalse(new StagingStringRange("999", "").contains("999", new HashMap<>()));
        assertFalse(new StagingStringRange("", "999").contains("999", new HashMap<>()));
    }

    @Test
    public void testDifferentLength() {
        assertFalse(new StagingStringRange("99", "999").contains("099", new HashMap<>()));
        assertFalse(new StagingStringRange("999", "99").contains("099", new HashMap<>()));

        assertFalse(new StagingStringRange("999", "99").contains("99", new HashMap<>()));
        assertFalse(new StagingStringRange("99", "999").contains("99", new HashMap<>()));

    }
}