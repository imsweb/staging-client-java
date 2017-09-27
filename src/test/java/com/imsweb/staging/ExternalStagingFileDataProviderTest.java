package com.imsweb.staging;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExternalStagingFileDataProviderTest {

    private static Staging _STAGING;

    @BeforeClass
    public static void setup() throws IOException {
        _STAGING = Staging.getInstance(new ExternalStagingFileDataProvider("c:/tmp/eod1.0.zip"));
    }

    @Test
    public void testExternalLoad() throws IOException {
        assertEquals("eod", _STAGING.getAlgorithm());
        assertEquals("1.0", _STAGING.getVersion());
        assertEquals(116, _STAGING.getSchemaIds().size());
        assertEquals(1630, _STAGING.getTableIds().size());
    }

}