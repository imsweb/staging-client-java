package com.imsweb.staging.eod;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import com.imsweb.staging.update.UpdaterUtils;

/**
 * Update the EOD data from the API
 */
public class EodUpdateFromAPI {

    private static final String _ALGORITHM = "eod";

    public static void main(String[] args) throws IOException {
        UpdaterUtils.update(_ALGORITHM, "2.1", "c:/tmp/algorithms", new HashSet<>(Collections.singletonList("STAGING")));
    }

}
