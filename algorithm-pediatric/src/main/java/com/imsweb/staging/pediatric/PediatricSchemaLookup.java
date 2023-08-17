/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.pediatric;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.imsweb.staging.entities.SchemaLookup;

import static com.imsweb.staging.entities.StagingData.HISTOLOGY_KEY;
import static com.imsweb.staging.entities.StagingData.PRIMARY_SITE_KEY;
import static com.imsweb.staging.pediatric.PediatricStagingData.PediatricInput.AGE_DX;
import static com.imsweb.staging.pediatric.PediatricStagingData.PediatricInput.BEHAVIOR;

public class PediatricSchemaLookup extends SchemaLookup {

    private static final Set<String> _ALLOWED_KEYS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(PRIMARY_SITE_KEY, HISTOLOGY_KEY, AGE_DX.toString(), BEHAVIOR.toString()))
    );

    /**
     * Constructor
     * @param site primary site
     * @param histology histology
     */
    public PediatricSchemaLookup(String site, String histology) {
        super(site, histology);
    }

    public PediatricSchemaLookup(String site, String histology, String ageDx, String behavior) {
        super(site, histology);

        setInput(AGE_DX.toString(), ageDx);
        setInput(BEHAVIOR.toString(), behavior);
    }

    @Override
    public Set<String> getAllowedKeys() {
        return _ALLOWED_KEYS;
    }

}
