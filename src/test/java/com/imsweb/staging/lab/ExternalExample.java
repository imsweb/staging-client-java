/*
 * Copyright (C) 2018 Information Management Services, Inc.
 */
package com.imsweb.staging.lab;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.imsweb.staging.ExternalStagingFileDataProvider;
import com.imsweb.staging.Staging;

public class ExternalExample {

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("C:/prj/staging-client-java/src/test/resources", "external_algorithm.zip");
        try (InputStream is = Files.newInputStream(path)) {
            Staging staging = Staging.getInstance(new ExternalStagingFileDataProvider(is));

            System.out.println(staging.getAlgorithm());
        }
    }

}
