package com.imsweb.staging.zip;

import java.io.IOException;

import com.imsweb.staging.AlgorithmZipUtils;
import com.imsweb.staging.tnm.TnmDataProvider.TnmVersion;

public class ZipAlgorithm {

    public static void main(String[] args) throws IOException {
        AlgorithmZipUtils.zipAlgorithm("tnm", TnmVersion.LATEST.getVersion());
    }

}
