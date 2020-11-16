/*
 * Copyright (C) 2020 Information Management Services, Inc.
 */
package lab;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import com.imsweb.staging.update.UpdaterUtils;

public class UpdateTest {

    /**
     * Run the update
     */
    public static void main(String[] args) throws IOException {
        UpdaterUtils.update("eod_public", "2.0", "c:/tmp/algorithms", new HashSet<>(Collections.singletonList("STAGING")));
    }

}
