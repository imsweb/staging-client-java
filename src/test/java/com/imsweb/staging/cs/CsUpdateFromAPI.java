package com.imsweb.staging.cs;

import java.io.IOException;

import us.monoid.json.JSONException;

import com.imsweb.staging.UpdaterUtils;

/**
 * Update the CS data from the API
 */
public class CsUpdateFromAPI {

    private static final String _ALGORITHM = "cs";
    private static final String _VERSION = "02.05.50";

    public static void main(String[] args) throws IOException, JSONException {
        UpdaterUtils.update(_ALGORITHM, _VERSION);
    }

}
