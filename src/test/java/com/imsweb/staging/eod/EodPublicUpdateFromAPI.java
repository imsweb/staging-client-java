package com.imsweb.staging.eod;

import java.io.IOException;

import com.imsweb.staging.UpdaterUtils;

/**
 * Update the EOD data from the API
 */
public class EodPublicUpdateFromAPI {

    private static final String _ALGORITHM = "eod_public";
    private static final String _VERSION = "1.1";

    public static void main(String[] args) throws IOException {
        UpdaterUtils.update(_ALGORITHM, _VERSION);
    }

}
