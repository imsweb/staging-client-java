package com.imsweb.staging.zip;

import java.io.IOException;

import com.imsweb.staging.AlgorithmZipUtils;
import com.imsweb.staging.cs.CsDataProvider.CsVersion;

/**
 * Create ZIP files of algorithms to include in releases
 */
public class ZipAlgorithm {

    public static void main(String[] args) throws IOException {
        AlgorithmZipUtils.zipAlgorithm("cs", CsVersion.LATEST.getVersion());
    }

}
