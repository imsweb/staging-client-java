package com.imsweb.staging;

import org.junit.jupiter.api.BeforeAll;

public class StagingFileDataProviderTest extends FileDataProviderTest {

    private static Staging _STAGING;

    @BeforeAll
    public static void setup() {
        _STAGING = Staging.getInstance(new StagingFileDataProvider("testing", "99.99"));
    }

    @Override
    public Staging getStaging() {
        return _STAGING;
    }

}