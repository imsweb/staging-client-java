package com.imsweb.staging.tnm;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import com.imsweb.staging.updater.UpdaterUtils;

/**
 * Update the TNM data from the API
 */
public class TnmUpdateFromAPI {

    public static void main(String[] args) throws IOException {
        UpdaterUtils.update("tnm", "2.0", new HashSet<>(Collections.singletonList("STAGING")));
    }

}
