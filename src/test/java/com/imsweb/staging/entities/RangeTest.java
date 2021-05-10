package com.imsweb.staging.entities;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RangeTest {

    @Test
    public void testRanges() {
        assertFalse(new Range("100", "103").contains("099", new HashMap<>()));
        assertTrue(new Range("100", "103").contains("100", new HashMap<>()));
        assertTrue(new Range("100", "103").contains("102", new HashMap<>()));
        assertTrue(new Range("100", "103").contains("103", new HashMap<>()));
        assertFalse(new Range("100", "103").contains("104", new HashMap<>()));

        // test that if the value is a shorter length it is not found to be a match
        assertFalse(new Range("020500", "999999").contains("1", new HashMap<>()));
    }

    @Test
    public void testContext() {
        Map<String, String> context = new HashMap<>();
        Range range = new Range("2000", "{{current_year}}");

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
        assertFalse(new Range(null, null).contains("099", new HashMap<>()));
    }

    @Test
    public void testEmptyBothValues() {
        assertFalse(new Range("", "").contains("099", new HashMap<>()));
    }

    @Test
    public void testEmptyOneValues() {
        assertFalse(new Range("999", "").contains("999", new HashMap<>()));
        assertFalse(new Range("", "999").contains("999", new HashMap<>()));
    }

    @Test
    public void testDifferentLength() {
        // string ranges must be the same length
        assertFalse(new Range("AA", "AAA").contains("AAA", new HashMap<>()));
        assertFalse(new Range("BBB", "BB").contains("BBB", new HashMap<>()));
        assertFalse(new Range("CCC", "CC").contains("CC", new HashMap<>()));
        assertFalse(new Range("DD", "DDD").contains("DD", new HashMap<>()));

        // numeric ranges do not have to be the same length
        assertTrue(new Range("99", "999").contains("150", new HashMap<>()));
        assertFalse(new Range("999", "99").contains("150", new HashMap<>()));
    }

    @Test
    public void testNumericRanges() {
        assertFalse(new Range("0.1", "99999.9").contains("0.0", new HashMap<>()));
        assertFalse(new Range("0.1", "99999.9").contains("100000", new HashMap<>()));
        assertFalse(new Range("0.1", "99999.9").contains("100000.1", new HashMap<>()));

        assertTrue(new Range("0.1", "99999.9").contains("0.1", new HashMap<>()));
        assertTrue(new Range("0.1", "99999.9").contains("500.1", new HashMap<>()));
        assertTrue(new Range("0.1", "99999.9").contains("99999.9", new HashMap<>()));

        // nothing checks that a decimal is there.  Non-decimal value will still be considered in the range.
        assertTrue(new Range("0.1", "99999.9").contains("1000", new HashMap<>()));
    }

    @Test
    public void testIsNumeric() {
        assertTrue(Range.isNumeric("0"));
        assertTrue(Range.isNumeric("1"));
        assertTrue(Range.isNumeric("-1"));
        assertTrue(Range.isNumeric("1.1"));

        assertFalse(Range.isNumeric(null));
        assertFalse(Range.isNumeric(""));
        assertFalse(Range.isNumeric("1.1.1"));
        assertFalse(Range.isNumeric("NAN"));
    }
}