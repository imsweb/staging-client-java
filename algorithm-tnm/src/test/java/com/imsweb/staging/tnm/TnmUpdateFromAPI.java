package com.imsweb.staging.tnm;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import com.imsweb.staging.updater.UpdaterUtils;

/**
 * Update the TNM data from the API
 */
public class TnmUpdateFromAPI {

    private static final String _ALGORITHM = "tnm";
    private static final String _VERSION = "2.0";

    public static void main(String[] args) throws IOException {
        UpdaterUtils.update(_ALGORITHM, _VERSION, new HashSet<>(Collections.singletonList("STAGING")));
    }

}
