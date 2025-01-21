package com.imsweb.staging.entities;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.imsweb.staging.entities.impl.StagingRange;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StagingRangeTest {

    @Test
    void testRanges() {
        assertFalse(new StagingRange("100", "103").contains("099", new HashMap<>()));
        assertTrue(new StagingRange("100", "103").contains("100", new HashMap<>()));
        assertTrue(new StagingRange("100", "103").contains("102", new HashMap<>()));
        assertTrue(new StagingRange("100", "103").contains("103", new HashMap<>()));
        assertFalse(new StagingRange("100", "103").contains("104", new HashMap<>()));

        // test that if the value is a shorter length it is not found to be a match
        assertFalse(new StagingRange("020500", "999999").contains("1", new HashMap<>()));
    }

    @Test
    void testContext() {
        Map<String, String> context = new HashMap<>();
        Range range = new StagingRange("2000", "{{current_year}}");

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

    @Test
    @SuppressWarnings("ConstantConditions")
    void testNull() {
        assertThatThrownBy(() -> new StagingRange(null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid range");
    }

    @Test
    void testEmptyBothValues() {
        assertFalse(new StagingRange("", "").contains("099", new HashMap<>()));
    }

    @Test
    void testEmptyOneValues() {
        assertFalse(new StagingRange("999", "").contains("999", new HashMap<>()));
        assertFalse(new StagingRange("", "999").contains("999", new HashMap<>()));
    }

    @Test
    void testDifferentLength() {
        // string ranges must be the same length
        assertFalse(new StagingRange("AA", "AAA").contains("AAA", new HashMap<>()));
        assertFalse(new StagingRange("BBB", "BB").contains("BBB", new HashMap<>()));
        assertFalse(new StagingRange("CCC", "CC").contains("CC", new HashMap<>()));
        assertFalse(new StagingRange("DD", "DDD").contains("DD", new HashMap<>()));

        // numeric ranges do not have to be the same length
        assertTrue(new StagingRange("99", "999").contains("150", new HashMap<>()));
        assertFalse(new StagingRange("999", "99").contains("150", new HashMap<>()));
    }

    @Test
    void testNumericRanges() {
        assertFalse(new StagingRange("0.1", "99999.9").contains("0.0", new HashMap<>()));
        assertFalse(new StagingRange("0.1", "99999.9").contains("100000", new HashMap<>()));
        assertFalse(new StagingRange("0.1", "99999.9").contains("100000.1", new HashMap<>()));

        assertTrue(new StagingRange("0.1", "99999.9").contains("0.1", new HashMap<>()));
        assertTrue(new StagingRange("0.1", "99999.9").contains("500.1", new HashMap<>()));
        assertTrue(new StagingRange("0.1", "99999.9").contains("99999.9", new HashMap<>()));

        // nothing checks that a decimal is there.  Non-decimal value will still be considered in the range.
        assertTrue(new StagingRange("0.1", "99999.9").contains("1000", new HashMap<>()));

        assertFalse(new StagingRange("1.0", "999.999").contains("0.1", new HashMap<>()));
        assertTrue(new StagingRange("1.0", "999.999").contains("1.000001", new HashMap<>()));
        assertTrue(new StagingRange("1.0", "999.999").contains("1.9", new HashMap<>()));
        assertTrue(new StagingRange("1.0", "999.999").contains("10.934215", new HashMap<>()));
        assertFalse(new StagingRange("1.0", "999.999").contains("999.9991", new HashMap<>()));
    }

}