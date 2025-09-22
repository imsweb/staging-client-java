package com.imsweb.staging.eod;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;

import com.imsweb.staging.updater.UpdaterUtils;

/**
 * Update the EOD data from the API
 */
public class EodUpdateFromAPI {

    public static void main(String[] args) throws IOException {
        UpdaterUtils.update("eod", "3.3", new HashSet<>(Collections.singletonList("STAGING")), Paths.get("c:/tmp"));
    }

}
