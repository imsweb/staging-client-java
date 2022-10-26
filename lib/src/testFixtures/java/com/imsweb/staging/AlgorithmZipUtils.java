/*
 * Copyright (C) 2022 Information Management Services, Inc.
 */
package com.imsweb.staging;

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
public class AlgorithmZipUtils {

    private static final Logger _LOG = LoggerFactory.getLogger(AlgorithmZipUtils.class);

    public static void zipAlgorithm(String algorithm, String version) throws IOException {
        Path algoDir = Paths.get("src", "main", "resources", "algorithms", algorithm, version);
        Path buildDir = Paths.get("build", "algorithms");
        String outputFile = buildDir.toFile().getAbsolutePath() + File.separatorChar + algorithm + "-" + version + ".zip";

        // make sure directory exists
        Files.createDirectories(buildDir);

        ZipUtil.pack(new File(algoDir.toFile().getAbsolutePath()), new File(outputFile));

        _LOG.info("Created {}", outputFile);
    }

}
