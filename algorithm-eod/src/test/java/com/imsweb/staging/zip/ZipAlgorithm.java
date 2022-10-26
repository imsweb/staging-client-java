package com.imsweb.staging.zip;

import java.io.IOException;

import com.imsweb.staging.AlgorithmZipUtils;
import com.imsweb.staging.eod.EodDataProvider.EodVersion;

public class ZipAlgorithm {

    public static void main(String[] args) throws IOException {
        AlgorithmZipUtils.zipAlgorithm("eod_public", EodVersion.LATEST.getVersion());
    }

}
