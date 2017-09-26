package com.imsweb.staging;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExternalStagingFileDataProviderTest {

    @Test
    public void testExternalLoad() {
        Staging staging = Staging.getInstance(new ExternalStagingFileDataProvider("c:/tmp/eod1.0.zip", "eod", "1.0"));

        assertEquals(116, staging.getSchemaIds().size());
        assertEquals(1630, staging.getTableIds().size());
    }

}