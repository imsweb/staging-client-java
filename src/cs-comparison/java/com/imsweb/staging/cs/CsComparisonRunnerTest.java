/*
 * Copyright (C) 2026 Information Management Services, Inc.
 */
package com.imsweb.staging.cs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CsComparisonRunnerTest {

    @Test
    void testNoFailures() {
        assertDoesNotThrow(() -> CsComparisonRunner.failIfNecessary(0));
    }

    @Test
    void testFailures() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            CsComparisonRunner.failIfNecessary(1234)
        );
        assertEquals("CS comparison failed with 1,234 failing cases.", exception.getMessage());
    }
}
