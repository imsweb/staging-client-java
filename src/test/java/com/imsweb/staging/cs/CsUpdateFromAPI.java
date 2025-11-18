package com.imsweb.staging.cs;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import com.imsweb.staging.updater.UpdaterUtils;

/**
 * Update the CS data from the API
 */
public class CsUpdateFromAPI {

    public static void main(String[] args) throws IOException {
        UpdaterUtils.update("cs", "02.05.50", new HashSet<>(Collections.singletonList("STAGING")));
    }

}
