package com.imsweb.staging.toronto;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import com.imsweb.staging.updater.UpdaterUtils;

/**
 * Update the Toronto data from the API
 */
public class TorontoUpdateFromAPI {

    public static void main(String[] args) throws IOException {
        UpdaterUtils.update("toronto", "0.3", new HashSet<>(Collections.singletonList("STAGING")));
    }

}
