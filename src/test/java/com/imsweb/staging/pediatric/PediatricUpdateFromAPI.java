package com.imsweb.staging.pediatric;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;

import com.imsweb.staging.updater.UpdaterUtils;

/**
 * Update the pediatric data from the API
 */
public class PediatricUpdateFromAPI {

    public static void main(String[] args) throws IOException {
        UpdaterUtils.update("pediatric", "1.3", new HashSet<>(Collections.singletonList("STAGING")), Paths.get("c:/dev/tmp"));
    }

}
