package com.imsweb.staging;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ExternalStagingFileDataProviderTest extends FileDataProviderTest {

    private static Staging _STAGING;

    @BeforeAll
    static void setup() throws IOException {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("external_algorithm.zip")) {
            _STAGING = Staging.getInstance(new ExternalStagingFileDataProvider(is));
        }
    }

    @Override
    public Staging getStaging() {
        return _STAGING;
    }

    @Test
    void testConstructorWithPath() throws IOException {
        Path zipPath = Paths.get("src/test/resources/external_algorithm.zip");
        ExternalStagingFileDataProvider provider = new ExternalStagingFileDataProvider(zipPath);
        assertThat(provider.getAlgorithm()).isNotBlank();
        assertThat(provider.getVersion()).isNotBlank();
        assertThat(provider.getSchemaIds()).isNotEmpty();
        assertThat(provider.getTableIds()).isNotEmpty();

        // test the direct Staging instance
        Staging staging = Staging.getInstance(zipPath);
        assertThat(staging.getSchemaIds()).isNotEmpty();
        assertThat(staging.getTableIds()).isNotEmpty();
    }

    @Test
    void testConstructorWithString() throws IOException {
        String zipFileName = "src/test/resources/external_algorithm.zip";
        ExternalStagingFileDataProvider provider = new ExternalStagingFileDataProvider(zipFileName);

        assertThat(provider.getAlgorithm()).isNotBlank();
        assertThat(provider.getVersion()).isNotBlank();
        assertThat(provider.getSchemaIds()).isNotEmpty();
        assertThat(provider.getTableIds()).isNotEmpty();

        // test the direct Staging instance
        Staging staging = Staging.getInstance(zipFileName);
        assertThat(staging.getSchemaIds()).isNotEmpty();
        assertThat(staging.getTableIds()).isNotEmpty();
    }

    @Test
    void testInvalidPathThrowsException() {
        Path invalidPath = Paths.get("src/test/resources/missing.zip");
        assertThatThrownBy(() -> new ExternalStagingFileDataProvider(invalidPath))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("missing.zip");
    }

}