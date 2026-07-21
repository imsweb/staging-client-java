/*
 * Copyright (C) 2026 Information Management Services, Inc.
 */
package com.imsweb.staging.cs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsIntegrationTestTest {

    @Test
    void testNoFailures() {
        assertDoesNotThrow(() -> CsIntegrationTest.failIfNecessary(0));
    }

    @Test
    void testFailures() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> CsIntegrationTest.failIfNecessary(1234));
        assertEquals("CS comparison failed with 1,234 failing cases.", exception.getMessage());
    }
}
