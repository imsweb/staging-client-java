/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.cs;

import java.util.Set;

import com.google.common.collect.Sets;

import com.imsweb.staging.SchemaLookup;

public class CsSchemaLookup extends SchemaLookup {

    private static final Set<String> _ALLOWED_KEYS = Sets.newHashSet(CsStagingData.PRIMARY_SITE_KEY, CsStagingData.HISTOLOGY_KEY, CsStagingData.SSF25_KEY);

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
