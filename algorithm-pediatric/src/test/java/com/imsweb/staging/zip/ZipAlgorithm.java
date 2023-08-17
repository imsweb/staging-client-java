package com.imsweb.staging.zip;

import java.io.IOException;

import com.imsweb.staging.AlgorithmZipUtils;
import com.imsweb.staging.pediatric.PediatricDataProvider.PediatricVersion;

public class ZipAlgorithm {

    public static void main(String[] args) throws IOException {
        AlgorithmZipUtils.zipAlgorithm("pediatric", PediatricVersion.LATEST.getVersion());
    }

}
