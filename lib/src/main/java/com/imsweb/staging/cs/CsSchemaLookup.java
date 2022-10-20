/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.cs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.imsweb.staging.entities.SchemaLookup;

import static com.imsweb.staging.entities.StagingData.HISTOLOGY_KEY;
import static com.imsweb.staging.entities.StagingData.PRIMARY_SITE_KEY;

public class CsSchemaLookup extends SchemaLookup {

    private static final Set<String> _ALLOWED_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(PRIMARY_SITE_KEY, HISTOLOGY_KEY, CsStagingData.SSF25_KEY)));

    /**
     * Constructor
     * @param site primary site
     * @param histology histology
     */
    public CsSchemaLookup(String site, String histology) {
        super(site, histology);
    }

    /**
     * Constructor
     * @param site primary site
     * @param histology histology
     * @param discriminator ssf25
     */
    public CsSchemaLookup(String site, String histology, String discriminator) {
        super(site, histology);

        setInput(CsStagingData.SSF25_KEY, discriminator);
    }

    @Override
    public Set<String> getAllowedKeys() {
        return _ALLOWED_KEYS;
    }

}
