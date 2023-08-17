package com.imsweb.staging.pediatric;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import com.imsweb.staging.updater.UpdaterUtils;

import static com.imsweb.staging.pediatric.PediatricDataProvider.PediatricVersion.LATEST;

/**
 * Update the Toronto data from the API
 */
public class PediatricUpdateFromAPI {

    public static void main(String[] args) throws IOException {
        UpdaterUtils.update("pediatric", LATEST.getVersion(), new HashSet<>(Collections.singletonList("STAGING")));
    }

}
