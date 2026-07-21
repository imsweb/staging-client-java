package com.imsweb.staging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ExternalStagingFileDataProviderTest extends FileDataProviderTest {

    private static Staging _STAGING;

    @BeforeAll
    static void setup() throws IOException {
        try (
            InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("external_algorithm.zip")
        ) {
            _STAGING = Staging.getInstance(is);
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

    @Test
    void testGlossaryFromInMemoryZip() throws IOException {
        ExternalStagingFileDataProvider provider = provider(
            "tables/site.json",
            tableJson("site", "TESTING", "1.0"),
            "glossary/cortex.json",
            "{\"name\":\"Cortex\",\"definition\":\"Outer tissue\",\"alternate_names\":[\"Cortical\"]}"
        );

        assertThat(provider.getAlgorithm()).isEqualTo("testing");
        assertThat(provider.getGlossaryTerms()).containsExactly("Cortex");
        assertThat(provider.getGlossaryDefinition("Cortex").getDefinition()).isEqualTo("Outer tissue");
        assertThat(provider.getGlossaryDefinition("missing")).isNull();
        assertThat(provider.getGlossaryMatches("The cortex is present")).extracting("term").containsExactly("Cortex");
        assertThat(provider.getGlossaryMatches("Cortexlike")).isEmpty();
    }

    @Test
    void testMalformedJsonIsRejected() throws IOException {
        byte[] zip = zip("tables/broken.json", "{not-json");

        assertThatThrownBy(() -> new ExternalStagingFileDataProvider(new ByteArrayInputStream(zip))).isInstanceOf(
            IOException.class
        );
    }

    @Test
    void testArchiveRequiresAlgorithmData() throws IOException {
        byte[] zip = zip("notes/readme.txt", "ignored", "glossary/term.json", "{\"name\":\"Term\"}");

        assertThatThrownBy(() -> new ExternalStagingFileDataProvider(new ByteArrayInputStream(zip)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Error initializing provider; only a single algorithm should be included in file");
    }

    @Test
    void testInconsistentAlgorithmsAndVersionsAreRejected() throws IOException {
        byte[] algorithms = zip(
            "tables/one.json",
            tableJson("one", "FIRST", "1.0"),
            "tables/two.json",
            tableJson("two", "SECOND", "1.0")
        );
        assertThatThrownBy(() -> new ExternalStagingFileDataProvider(new ByteArrayInputStream(algorithms)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Error initializing provider; only a single algorithm should be included in file");

        byte[] versions = zip(
            "tables/one.json",
            tableJson("one", "TEST", "1.0"),
            "tables/two.json",
            tableJson("two", "TEST", "2.0")
        );
        assertThatThrownBy(() -> new ExternalStagingFileDataProvider(new ByteArrayInputStream(versions)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Error initializing provider; only a single version should be included in file");
    }

    private static ExternalStagingFileDataProvider provider(String... entries) throws IOException {
        return new ExternalStagingFileDataProvider(new ByteArrayInputStream(zip(entries)));
    }

    private static byte[] zip(String... entries) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (int i = 0; i < entries.length; i += 2) {
                zip.putNextEntry(new ZipEntry(entries[i]));
                zip.write(entries[i + 1].getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

    private static String tableJson(String id, String algorithm, String version) {
        return """
        {"id":"%s","algorithm":"%s","version":"%s","definition":[{"key":"value","type":"INPUT"}],"rows":[["1"]]}
        """.formatted(id, algorithm, version);
    }
}
