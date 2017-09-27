package com.imsweb.staging;

import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExternalStagingFileDataProviderTest {

    private static Staging _STAGING;

    @BeforeClass
    public static void setup() throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("external_algorithm.zip");

        _STAGING = Staging.getInstance(new ExternalStagingFileDataProvider(is));
    }

    @Test
    public void testExternalLoad() throws IOException {
        assertEquals("testing", _STAGING.getAlgorithm());
        assertEquals("99.99", _STAGING.getVersion());
        assertEquals(1, _STAGING.getSchemaIds().size());
        assertEquals(62, _STAGING.getTableIds().size());
    }

}