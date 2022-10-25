/*
 * Copyright (C) 2022 Information Management Services, Inc.
 */
package com.imsweb.staging.zip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;

/**
 * Create ZIP files of algorithms to include in releases
 */
public class ZipAlgorithms {

    private static final Logger _LOG = LoggerFactory.getLogger(ZipAlgorithms.class);

    public static void main(String[] args) throws IOException {
        zipAlgorithm("algorithm-cs", "cs", "02.05.50");
        zipAlgorithm("algorithm-tnm", "tnm", "2.0");
        zipAlgorithm("algorithm-eod", "eod_public", "3.0");
        zipAlgorithm("algorithm-toronto", "toronto", "0.3");
    }

    private static void zipAlgorithm(String subproject, String algorithm, String version) throws IOException {
        Path algoDir = Paths.get("..", subproject, "src", "main", "resources", "algorithms", algorithm, version);
        Path buildDir = Paths.get("..", "build", "algorithms");
        String outputFile = buildDir.toFile().getAbsolutePath() + File.separatorChar + algorithm + "-" + version + ".zip";

        // make sure directory exists
        Files.createDirectories(buildDir);

        ZipUtil.pack(new File(algoDir.toFile().getAbsolutePath()), new File(outputFile));

        _LOG.info("Created " + outputFile);
    }

}
