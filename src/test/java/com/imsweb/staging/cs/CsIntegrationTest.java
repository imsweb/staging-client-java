/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.cs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import com.imsweb.staging.IntegrationUtils;
import com.imsweb.staging.IntegrationUtils.IntegrationResult;
import com.imsweb.staging.Staging;
import com.imsweb.staging.cs.CsDataProvider.CsVersion;
import com.imsweb.staging.util.Stopwatch;

@SuppressWarnings("java:S2187")
public class CsIntegrationTest {

    // set this to null to process all, or a list of schema filename to process
    private static final List<String> _SCHEMA_FILES = Collections.emptyList();

    public static void main(String[] args) throws IOException, InterruptedException {
        execute();
    }

    private static void execute() throws IOException, InterruptedException {
        Staging staging = Staging.getInstance(CsDataProvider.getInstance(CsVersion.V020550));

        // only do schema selection test if running all schemas
        if (_SCHEMA_FILES.isEmpty()) {
            IntegrationUtils.processSchemaSelection(staging, "cs_schema_identification.txt.gz",
                    new GZIPInputStream(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("integration/schema_selection/cs_schema_identification.txt.gz"))));

            System.out.println("-----------------------------------------------");
        }

        Stopwatch stopwatch = Stopwatch.create();

        // get list of schema files;
        // NOTE: some of these files are REALLY large so they are not included in the project for now;  all files larger than 2MB (40 of them) are not
        //       part of the repository and can be found at \\omni\btp\csb\Staging\CS
        List<String> schemaFiles;
        try (BufferedReader buffer = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("integration/schemas/index.txt")), StandardCharsets.UTF_8))) {
            schemaFiles = buffer.lines().collect(Collectors.toList());
        }

        long totalFiles = 0;
        long totalCases = 0;
        long totalFailures = 0;

        for (String schemaFile : schemaFiles) {
            if (_SCHEMA_FILES.isEmpty() || _SCHEMA_FILES.contains(schemaFile)) {
                totalFiles += 1;
                IntegrationResult result = IntegrationUtils.processSchema(staging, schemaFile,
                        new GZIPInputStream(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("integration/schemas/" + schemaFile))));
                totalCases += result.getNumCases();
                totalFailures += result.getNumFailures();
            }
        }

        stopwatch.stop();

        String perMs = String.format("%.3f", ((float)stopwatch.elapsed(TimeUnit.MILLISECONDS) / totalCases));
        System.out.println();
        System.out.println("Completed " + NumberFormat.getNumberInstance(Locale.US).format(totalCases) + " cases (" + totalFiles + " files) in " + stopwatch + " (" + perMs + "ms/case).");
        if (totalFailures > 0)
            System.out.println("There were " + NumberFormat.getNumberInstance(Locale.US).format(totalFailures) + " failing cases.");
    }

}
