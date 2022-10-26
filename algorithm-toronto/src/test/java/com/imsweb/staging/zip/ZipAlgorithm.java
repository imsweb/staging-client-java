package com.imsweb.staging.zip;

import java.io.IOException;

import com.imsweb.staging.AlgorithmZipUtils;
import com.imsweb.staging.toronto.TorontoDataProvider.TorontoVersion;

public class ZipAlgorithm {

    public static void main(String[] args) throws IOException {
        AlgorithmZipUtils.zipAlgorithm("toronto", TorontoVersion.LATEST.getVersion());
    }

}
