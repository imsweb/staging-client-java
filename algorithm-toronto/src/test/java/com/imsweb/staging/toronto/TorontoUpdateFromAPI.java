package com.imsweb.staging.toronto;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import com.imsweb.staging.updater.UpdaterUtils;

import static com.imsweb.staging.toronto.TorontoDataProvider.TorontoVersion.V0_5;

/**
 * Update the Toronto data from the API
 */
public class TorontoUpdateFromAPI {

    public static void main(String[] args) throws IOException {
        UpdaterUtils.update("toronto", V0_5.getVersion(), new HashSet<>(Collections.singletonList("STAGING")));
    }

}
