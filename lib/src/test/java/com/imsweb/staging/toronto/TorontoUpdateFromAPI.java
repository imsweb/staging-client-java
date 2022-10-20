package com.imsweb.staging.toronto;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import com.imsweb.staging.update.UpdaterUtils;

/**
 * Update the Toronto data from the API
 */
public class TorontoUpdateFromAPI {

    private static final String _ALGORITHM = "toronto";

    public static void main(String[] args) throws IOException {
        UpdaterUtils.update(_ALGORITHM, "0.2", new HashSet<>(Collections.singletonList("STAGING")));
    }

}
