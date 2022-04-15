package com.imsweb.staging;

import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;

public class ExternalStagingFileDataProviderTest extends FileDataProviderTest {

    private static Staging _STAGING;

    @BeforeClass
    public static void setup() throws IOException {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("external_algorithm.zip")) {
            _STAGING = Staging.getInstance(new ExternalStagingFileDataProvider(is));
        }
    }

    @Override
    public Staging getStaging() {
        return _STAGING;
    }

}