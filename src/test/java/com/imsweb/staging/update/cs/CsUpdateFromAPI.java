package com.imsweb.staging.update.cs;

import java.io.IOException;

import com.imsweb.staging.update.UpdaterUtils;

/**
 * Update the CS data from the API
 */
public class CsUpdateFromAPI {

    private static final String _ALGORITHM = "cs";
    private static final String _VERSION = "02.05.50";

    public static void main(String[] args) throws IOException {
        UpdaterUtils.update(_ALGORITHM, _VERSION);
    }

}
