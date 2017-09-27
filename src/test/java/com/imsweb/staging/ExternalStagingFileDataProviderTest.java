package com.imsweb.staging;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExternalStagingFileDataProviderTest {

    private static Staging _STAGING;

    @BeforeClass
    public static void setup() throws IOException {
        // need way to get absolute path of the testing external algorithm; it also needs to use forward slashes
        Path resourceDirectory = Paths.get("src/test/resources/external_algorithm.zip");
        String absolutePath = resourceDirectory.toAbsolutePath().toString().replaceAll("\\\\", "/");

        System.out.println(absolutePath);

        _STAGING = Staging.getInstance(new ExternalStagingFileDataProvider(absolutePath));
    }

    @Test
    public void testExternalLoad() throws IOException {
        assertEquals("testing", _STAGING.getAlgorithm());
        assertEquals("99.99", _STAGING.getVersion());
        assertEquals(1, _STAGING.getSchemaIds().size());
        assertEquals(62, _STAGING.getTableIds().size());
    }

}