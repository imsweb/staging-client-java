/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.cs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.imsweb.staging.Staging;
import com.imsweb.staging.cs.CsStagingData.CsInput;
import com.imsweb.staging.cs.CsStagingData.CsOutput;
import com.imsweb.staging.entities.Schema;
import com.imsweb.staging.entities.SchemaLookup;
import com.imsweb.staging.util.Stopwatch;

public final class IntegrationUtils {

    private static final Logger _LOG = LoggerFactory.getLogger(IntegrationUtils.class);

    /**
     * Private constructor
     */
    private IntegrationUtils() {
    }

    public static IntegrationResult processSchemaSelection(final Staging staging, String fileName, InputStream is) throws IOException, InterruptedException {
        // initialize the threads pool (don't use more than 9 threads)
        int n = Math.min(9, Runtime.getRuntime().availableProcessors() + 1);
        ExecutorService pool = new ThreadPoolExecutor(n, n, 5, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000), new CallerRunsPolicy());

        Stopwatch stopwatch = Stopwatch.create();

        AtomicInteger processedCases = new AtomicInteger(0);
        final AtomicInteger failedCases = new AtomicInteger(0);
        _LOG.info("Starting schema selection tests from {} [{} threads]", fileName, n);

        LineNumberReader reader = new LineNumberReader(new InputStreamReader(is, StandardCharsets.UTF_8));

        // loop over each line in the file
        String line = reader.readLine();
        while (line != null) {
            if (!line.startsWith("#")) {
                processedCases.getAndIncrement();

                // split the string; important to keep empty trailing values in the resulting array
                String[] parts = Arrays.stream(line.split(",", -1)).map(String::trim).toArray(String[]::new);

                if (parts.length != 4)
                    throw new IllegalStateException("Bad record in schema_selection.txt on line number" + reader.getLineNumber());

                final int lineNum = reader.getLineNumber();
                final String fullLine = line;

                pool.submit(() -> {
                    try {
                        SchemaLookup lookup = new SchemaLookup(parts[0], parts[1]);
                        lookup.setInput(CsStagingData.SSF25_KEY, parts[2]);

                        List<Schema> lookups = staging.lookupSchema(lookup);
                        if (parts[3].isEmpty()) {
                            if (lookups.size() == 1) {
                                _LOG.info("Line #{} [{}] --> The schema selection should not have found any schema but did: {}", lineNum, fullLine, lookups.getFirst().getId());
                                failedCases.getAndIncrement();
                            }
                        }
                        else {
                            if (lookups.size() != 1) {
                                _LOG.info("Line #{} [{}] --> The schema selection should have found a schema, {}, but did not.", lineNum, fullLine, parts[3]);
                                failedCases.getAndIncrement();
                            }
                            else if (!Objects.equals(lookups.getFirst().getId(), parts[3])) {
                                _LOG.info("Line #{} [{}] --> The schema selection found schema {} but it should have found {}.", lineNum, fullLine, lookups.getFirst().getId(), parts[3]);
                                failedCases.getAndIncrement();
                            }
                        }
                    }
                    catch (Throwable t) {
                        if (failedCases.get() == 0)
                            _LOG.info("Line #{} --> Exception processing schema selection: {}", lineNum, t.getMessage());
                        failedCases.getAndIncrement();
                    }

                    return null;
                });
            }

            line = reader.readLine();
        }

        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        stopwatch.stop();
        String perMs = String.format("%.3f", ((float)stopwatch.elapsed(TimeUnit.MILLISECONDS) / processedCases.get()));
        _LOG.info("Completed {} cases in {} ({}ms/case).", NumberFormat.getNumberInstance(Locale.US).format(processedCases.get()), stopwatch, perMs);
        if (failedCases.get() > 0)
            _LOG.info("There were {} failures.", NumberFormat.getNumberInstance(Locale.US).format(failedCases.get()));
        else
            _LOG.info("");

        return new IntegrationResult(processedCases.get(), failedCases.get());
    }

    /**
     * Process all schemas in file
     * @param staging Staging object
     * @param fileName name of file
     * @param is InputStream
     * @return IntegrationResult
     */
    public static IntegrationResult processSchema(Staging staging, String fileName, InputStream is) throws IOException, InterruptedException {
        return processSchema(staging, fileName, is, null);
    }

    /**
     * Process all schemas in file
     * @param staging Staging object
     * @param fileName name of file
     * @param is InputStream
     * @param singleLineNumber if not null, only process this line number
     * @return IntegrationResult
     */
    public static IntegrationResult processSchema(final Staging staging, final String fileName, InputStream is, Integer singleLineNumber) throws IOException, InterruptedException {
        // set up a mapping of output field positions in the CSV file
        final Map<CsOutput, Integer> mappings = new EnumMap<>(CsOutput.class);
        mappings.put(CsOutput.AJCC6_T, 42);
        mappings.put(CsOutput.AJCC6_TDESCRIPTOR, 43);
        mappings.put(CsOutput.AJCC6_N, 44);
        mappings.put(CsOutput.AJCC6_NDESCRIPTOR, 45);
        mappings.put(CsOutput.AJCC6_M, 46);
        mappings.put(CsOutput.AJCC6_MDESCRIPTOR, 47);
        mappings.put(CsOutput.AJCC6_STAGE, 48);
        mappings.put(CsOutput.AJCC7_T, 49);
        mappings.put(CsOutput.AJCC7_TDESCRIPTOR, 50);
        mappings.put(CsOutput.AJCC7_N, 51);
        mappings.put(CsOutput.AJCC7_NDESCRIPTOR, 52);
        mappings.put(CsOutput.AJCC7_M, 53);
        mappings.put(CsOutput.AJCC7_MDESCRIPTOR, 54);
        mappings.put(CsOutput.AJCC7_STAGE, 55);
        mappings.put(CsOutput.SS1977_T, 56);
        mappings.put(CsOutput.SS1977_N, 57);
        mappings.put(CsOutput.SS1977_M, 58);
        mappings.put(CsOutput.SS1977_STAGE, 59);
        mappings.put(CsOutput.SS2000_T, 60);
        mappings.put(CsOutput.SS2000_N, 61);
        mappings.put(CsOutput.SS2000_M, 62);
        mappings.put(CsOutput.SS2000_STAGE, 63);
        mappings.put(CsOutput.STOR_AJCC6_T, 64);
        mappings.put(CsOutput.STOR_AJCC6_TDESCRIPTOR, 65);
        mappings.put(CsOutput.STOR_AJCC6_N, 66);
        mappings.put(CsOutput.STOR_AJCC6_NDESCRIPTOR, 67);
        mappings.put(CsOutput.STOR_AJCC6_M, 68);
        mappings.put(CsOutput.STOR_AJCC6_MDESCRIPTOR, 69);
        mappings.put(CsOutput.STOR_AJCC6_STAGE, 70);
        mappings.put(CsOutput.STOR_AJCC7_T, 71);
        mappings.put(CsOutput.STOR_AJCC7_TDESCRIPTOR, 72);
        mappings.put(CsOutput.STOR_AJCC7_N, 73);
        mappings.put(CsOutput.STOR_AJCC7_NDESCRIPTOR, 74);
        mappings.put(CsOutput.STOR_AJCC7_M, 75);
        mappings.put(CsOutput.STOR_AJCC7_MDESCRIPTOR, 76);
        mappings.put(CsOutput.STOR_AJCC7_STAGE, 77);
        mappings.put(CsOutput.STOR_SS1977_STAGE, 78);
        mappings.put(CsOutput.STOR_SS2000_STAGE, 79);

        // initialize the threads pool
        int n = Math.min(9, Runtime.getRuntime().availableProcessors() + 1);
        ExecutorService pool = new ThreadPoolExecutor(n, n, 5, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000), new CallerRunsPolicy());

        // go over each file
        final AtomicInteger processedCases = new AtomicInteger(0);
        final AtomicInteger failedCases = new AtomicInteger(0);

        Stopwatch stopwatch = Stopwatch.create();

        if (singleLineNumber != null)
            _LOG.info("Starting {}, line # {} [{} threads]", fileName, singleLineNumber, n);
        else
            _LOG.info("Starting {} [{} threads]", fileName, n);

        LineNumberReader reader = new LineNumberReader(new InputStreamReader(is, StandardCharsets.UTF_8));

        // loop over each line in the file
        String line = reader.readLine();
        while (line != null) {

            // split the CSV record
            final String[] values = line.split(",");

            final int lineNum = reader.getLineNumber();

            // if a single line was requested, skip all other lines
            if (singleLineNumber != null && singleLineNumber != lineNum)
                continue;

            if (values.length != 80)
                _LOG.info("Line {} has {} cells; it should be 80.", lineNum, values.length);
            else
                pool.submit(() -> {
                    // load up inputs
                    CsStagingData data = new CsStagingData();
                    data.setInput(CsInput.PRIMARY_SITE, values[0]);
                    data.setInput(CsInput.HISTOLOGY, values[1]);

                    // the test data was originally set up to use 2019 as an invalid year.  Since it is now 2019 that causes failures.  Manually setting to 2049.
                    data.setInput(CsInput.DX_YEAR, ("2019".equals(values[2]) || "2020".equals(values[2])) ? "2049" : values[2]);

                    data.setInput(CsInput.CS_VERSION_ORIGINAL, values[3]);
                    data.setInput(CsInput.BEHAVIOR, values[4]);
                    data.setInput(CsInput.GRADE, values[5]);
                    data.setInput(CsInput.AGE_AT_DX, values[6]);
                    data.setInput(CsInput.LVI, values[7]);
                    data.setInput(CsInput.TUMOR_SIZE, values[8]);
                    data.setInput(CsInput.EXTENSION, values[9]);
                    data.setInput(CsInput.EXTENSION_EVAL, values[10]);
                    data.setInput(CsInput.LYMPH_NODES, values[11]);
                    data.setInput(CsInput.LYMPH_NODES_EVAL, values[12]);
                    data.setInput(CsInput.REGIONAL_NODES_POSITIVE, values[13]);
                    data.setInput(CsInput.REGIONAL_NODES_EXAMINED, values[14]);
                    data.setInput(CsInput.METS_AT_DX, values[15]);
                    data.setInput(CsInput.METS_EVAL, values[16]);
                    data.setInput(CsInput.SSF1, values[17]);
                    data.setInput(CsInput.SSF2, values[18]);
                    data.setInput(CsInput.SSF3, values[19]);
                    data.setInput(CsInput.SSF4, values[20]);
                    data.setInput(CsInput.SSF5, values[21]);
                    data.setInput(CsInput.SSF6, values[22]);
                    data.setInput(CsInput.SSF7, values[23]);
                    data.setInput(CsInput.SSF8, values[24]);
                    data.setInput(CsInput.SSF9, values[25]);
                    data.setInput(CsInput.SSF10, values[26]);
                    data.setInput(CsInput.SSF11, values[27]);
                    data.setInput(CsInput.SSF12, values[28]);
                    data.setInput(CsInput.SSF13, values[29]);
                    data.setInput(CsInput.SSF14, values[30]);
                    data.setInput(CsInput.SSF15, values[31]);
                    data.setInput(CsInput.SSF16, values[32]);
                    data.setInput(CsInput.SSF17, values[33]);
                    data.setInput(CsInput.SSF18, values[34]);
                    data.setInput(CsInput.SSF19, values[35]);
                    data.setInput(CsInput.SSF20, values[36]);
                    data.setInput(CsInput.SSF21, values[37]);
                    data.setInput(CsInput.SSF22, values[38]);
                    data.setInput(CsInput.SSF23, values[39]);
                    data.setInput(CsInput.SSF24, values[40]);
                    data.setInput(CsInput.SSF25, values[41]);

                    try {
                        // save the expected outputs
                        Map<String, String> output = new HashMap<>();
                        for (Map.Entry<CsOutput, Integer> entry : mappings.entrySet())
                            output.put(entry.getKey().toString(), values[entry.getValue()]);

                        // run collaborative stage; if no schema found, set the output to empty
                        SchemaLookup lookup = new SchemaLookup(data.getInput(CsInput.PRIMARY_SITE), data.getInput(CsInput.HISTOLOGY));
                        lookup.setInput(CsStagingData.SSF25_KEY, data.getInput(CsInput.SSF25));
                        List<Schema> schemas = staging.lookupSchema(lookup);

                        if (schemas.size() == 1)
                            staging.stage(data);
                        else {
                            Map<String, String> out = new HashMap<>();
                            out.put("schema_id", "<invalid>");
                            data.setOutput(out);
                        }

                        List<String> mismatches = new ArrayList<>();

                        // compare results
                        for (Map.Entry<String, String> entry : output.entrySet()) {
                            String expected = output.get(entry.getKey()).trim();
                            String actual = data.getOutput().get(entry.getKey());
                            if (actual == null)
                                actual = "";

                            if (!expected.equals(actual))
                                mismatches.add("   " + lineNum + " --> " + entry.getKey() + ": EXPECTED '" + expected + "' ACTUAL: '" + actual + "'");
                        }

                        if (!mismatches.isEmpty()) {
                            if (failedCases.get() == 0) {
                                _LOG.error("   {} --> [{}] Mismatches in {}", lineNum, data.getOutput().get("schema_id"), fileName);
                                for (String mismatch : mismatches)
                                    _LOG.error(mismatch);
                                _LOG.error("   {} *** RESULT: {}", lineNum, data.getResult());
                                _LOG.error("   {} --> {}", lineNum, convertInputMap(data.getInput()));
                                if (!data.getErrors().isEmpty()) {
                                    _LOG.error("   {} --> ERRORS: ", lineNum);
                                    for (com.imsweb.staging.entities.Error e : data.getErrors())
                                        _LOG.error("({}: {}) ", e.getTable(), e.getMessage());
                                    _LOG.error("");
                                }
                            }

                            failedCases.getAndIncrement();
                        }
                    }
                    catch (Throwable t) {
                        if (failedCases.get() == 0)
                            _LOG.error("   {} --> Exception processing {} : {}", lineNum, fileName, t.getMessage());
                        failedCases.getAndIncrement();
                    }

                    processedCases.getAndIncrement();

                    return null;
                });

            line = reader.readLine();
        }

        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        stopwatch.stop();
        String perMs = String.format("%.3f", ((float)stopwatch.elapsed(TimeUnit.MILLISECONDS) / processedCases.get()));
        _LOG.info("Completed {} cases for {} in {} ({}ms/case).", NumberFormat.getNumberInstance(Locale.US).format(processedCases.get()), fileName, stopwatch, perMs);
        if (failedCases.get() > 0)
            _LOG.error("  There were {} failures.", NumberFormat.getNumberInstance(Locale.US).format(failedCases));
        else
            _LOG.info("");
        _LOG.info("-----------------------------------------------");

        reader.close();
        is.close();

        return new IntegrationResult(processedCases.get(), failedCases.get());
    }

    private static String convertInputMap(Map<String, String> input) {
        List<String> inputValues = new ArrayList<>();
        for (Map.Entry<String, String> entry : input.entrySet())
            inputValues.add("\"" + entry.getKey() + "\": \"" + entry.getValue() + "\"");
        return "{ " + String.join(",", inputValues) + " }";
    }

    /**
     * Result object
     */
    public static class IntegrationResult {

        private final long _numCases;
        private final long _numFailures;

        public IntegrationResult(long numCases, long numFailures) {
            _numCases = numCases;
            _numFailures = numFailures;
        }

        public long getNumCases() {
            return _numCases;
        }

        public long getNumFailures() {
            return _numFailures;
        }
    }
}
