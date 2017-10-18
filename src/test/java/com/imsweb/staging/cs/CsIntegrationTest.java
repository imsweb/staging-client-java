/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.cs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import com.imsweb.staging.IntegrationUtils;
import com.imsweb.staging.IntegrationUtils.IntegrationResult;
import com.imsweb.staging.Staging;
import com.imsweb.staging.cs.CsDataProvider.CsVersion;
import com.imsweb.staging.util.Stopwatch;

public class CsIntegrationTest {

    // set this to null to process all, or a list of schema filename to process
    private static final List<String> _SCHEMA_FILES = Collections.emptyList();
    //private static final List<String> _SCHEMA_FILES = Arrays.asList("corpus_carcinoma.gz");

    public static void main(String[] args) throws IOException, InterruptedException {
        // hard-code data directory based on Windows vs Linux
        List<String> dataDirectories;
        if (System.getProperty("os.name").toLowerCase().contains("windows"))
            dataDirectories = Collections.singletonList("p:/csb/Staging/CS");
        else
            dataDirectories = Collections.singletonList("/prj/csb/Staging/CS");

        for (String dataDirectory : dataDirectories) {
            System.out.println("*******************************************");
            System.out.println("EXECUTING CASES IN " + dataDirectory);
            System.out.println("*******************************************");
            execute(dataDirectory);
        }
    }

    private static void execute(String dataDirectory) throws IOException, InterruptedException {
        Staging staging = Staging.getInstance(CsDataProvider.getInstance(CsVersion.v020550));

        // only do schema selection test if running all schemas
        if (_SCHEMA_FILES.isEmpty()) {
            IntegrationUtils.processSchemaSelection(staging, "cs_schema_identification.txt.gz", new GZIPInputStream(new FileInputStream(new File(
                    dataDirectory + "/schema_selection/cs_schema_identification.txt.gz"))));

            System.out.println("-----------------------------------------------");
        }

        Stopwatch stopwatch = Stopwatch.create();

        long totalFiles = 0;
        long totalCases = 0;
        long totalFailures = 0;

        // get the complete list of files
        File folder = new File(dataDirectory);
        File[] files = folder.listFiles();

        if (files != null) {
            // sort the files by name
            Arrays.sort(files);

            for (File f : files) {
                if (f.isFile() && f.getName().endsWith(".gz")) {
                    if (_SCHEMA_FILES.isEmpty() || _SCHEMA_FILES.contains(f.getName())) {
                        totalFiles += 1;
                        IntegrationResult result = IntegrationUtils.processSchema(staging, f.getName(), new GZIPInputStream(new FileInputStream(f)));
                        totalCases += result.getNumCases();
                        totalFailures += result.getNumFailures();
                    }
                }
            }
        }

        stopwatch.stop();

        String perMs = String.format("%.3f", ((float)stopwatch.elapsed(TimeUnit.MILLISECONDS) / totalCases));
        System.out.println("");
        System.out.println("Completed " + NumberFormat.getNumberInstance(Locale.US).format(totalCases) + " cases (" + totalFiles + " files) in " + stopwatch + " (" + perMs + "ms/case).");
        if (totalFailures > 0)
            System.out.println("There were " + totalFailures + " failing cases.");
    }
}
