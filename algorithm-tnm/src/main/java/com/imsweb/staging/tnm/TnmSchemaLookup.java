/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.tnm;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.imsweb.staging.entities.SchemaLookup;

import static com.imsweb.staging.entities.StagingData.HISTOLOGY_KEY;
import static com.imsweb.staging.entities.StagingData.PRIMARY_SITE_KEY;
import static com.imsweb.staging.tnm.TnmStagingData.SEX_KEY;
import static com.imsweb.staging.tnm.TnmStagingData.SSF25_KEY;

public class TnmSchemaLookup extends SchemaLookup {

    private static final Set<String> _ALLOWED_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(PRIMARY_SITE_KEY, HISTOLOGY_KEY, SSF25_KEY, SEX_KEY)));

    /**
     * Constructor
     * @param site primary site
     * @param histology histology
     */
    public TnmSchemaLookup(String site, String histology) {
        super(site, histology);
    }

    @Override
    public Set<String> getAllowedKeys() {
        return _ALLOWED_KEYS;
    }

}
