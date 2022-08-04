/*
 * Copyright (C) 2022 Information Management Services, Inc.
 */
package com.imsweb.staging.zip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.zeroturnaround.zip.ZipUtil;

import com.imsweb.staging.cs.CsDataProvider.CsVersion;
import com.imsweb.staging.eod.EodDataProvider.EodVersion;
import com.imsweb.staging.tnm.TnmDataProvider.TnmVersion;

/**
 * Create ZIP files of algorithms to include in releases
 */
public class ZipAlgorithms {

    public static void main(String[] args) throws IOException {
        zipAlgorithm("cs", CsVersion.LATEST.getVersion());
        zipAlgorithm("tnm", TnmVersion.LATEST.getVersion());
        zipAlgorithm("eod_public", EodVersion.LATEST.getVersion());
        //zipAlgorithm("toronto", TorontoVersion.LATEST.getVersion());
    }

    private static void zipAlgorithm(String algorithm, String version) throws IOException {
        String algoDir = Paths.get("src", "main", "resources").toFile().getAbsolutePath() + "/algorithms/" + algorithm + "/" + version;
        Path buildDir = Paths.get("build", "algorithms");
        String outputFile = buildDir.toFile().getAbsolutePath() + "/" + algorithm + "-" + version + ".zip";

        // make sure directory exists
        Files.createDirectories(buildDir);

        ZipUtil.pack(new File(algoDir), new File(outputFile));
    }

}
