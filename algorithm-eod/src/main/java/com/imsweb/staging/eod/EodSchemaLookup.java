/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.eod;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.imsweb.staging.entities.SchemaLookup;

import static com.imsweb.staging.entities.StagingData.HISTOLOGY_KEY;
import static com.imsweb.staging.entities.StagingData.PRIMARY_SITE_KEY;
import static com.imsweb.staging.eod.EodStagingData.EodInput.BEHAVIOR;
import static com.imsweb.staging.eod.EodStagingData.EodInput.DISCRIMINATOR_1;
import static com.imsweb.staging.eod.EodStagingData.EodInput.DISCRIMINATOR_2;
import static com.imsweb.staging.eod.EodStagingData.EodInput.SEX;

public class EodSchemaLookup extends SchemaLookup {

    private static final Set<String> _ALLOWED_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            PRIMARY_SITE_KEY,
            HISTOLOGY_KEY,
            SEX.toString(),
            BEHAVIOR.toString(),
            DISCRIMINATOR_1.toString(),
            DISCRIMINATOR_2.toString())));

    /**
     * Constructor
     * @param site primary site
     * @param histology histology
     */
    public EodSchemaLookup(String site, String histology) {
        super(site, histology);
    }

    @Override
    public Set<String> getAllowedKeys() {
        return _ALLOWED_KEYS;
    }

}
