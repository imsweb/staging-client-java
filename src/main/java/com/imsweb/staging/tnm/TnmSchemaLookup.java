/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.tnm;

import java.util.Set;

import com.google.common.collect.Sets;

import com.imsweb.staging.SchemaLookup;

public class TnmSchemaLookup extends SchemaLookup {

    private static final Set<String> _ALLOWED_KEYS = Sets.newHashSet(TnmStagingData.PRIMARY_SITE_KEY, TnmStagingData.HISTOLOGY_KEY, TnmStagingData.SSF25_KEY);

    /**
     * Default constructor
     */
    public TnmSchemaLookup() {
    }

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
