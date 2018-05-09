package com.imsweb.staging.eod;

import java.io.IOException;

import com.imsweb.staging.UpdaterUtils;

/**
 * Update the EOD data from the API
 */
public class EodUpdateFromAPI {

    private static final String _ALGORITHM = "eod";
    private static final String _VERSION = "1.2";

    public static void main(String[] args) throws IOException {
        UpdaterUtils.update(_ALGORITHM, _VERSION);
    }

}
