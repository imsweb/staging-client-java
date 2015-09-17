/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SchemaLookup {

    private Map<String, String> _inputs = new HashMap<>();

    /**
     * Default constructor
     */
    public SchemaLookup() {
    }

    /**
     * Constructor
     * @param inputs a Map of key-value pairs
     */
    public SchemaLookup(Map<String, String> inputs) {
        _inputs = inputs;
    }

    /**
     * Constructor
     * @param site primary site
     * @param histology histology
     */
    public SchemaLookup(String site, String histology) {
        setInput(StagingData.PRIMARY_SITE_KEY, site);
        setInput(StagingData.HISTOLOGY_KEY, histology);
    }

    /**
     * Returns a list of allowable keys.  If null, all keys are allowed
     * @return a set of keys
     */
    public Set<String> getAllowedKeys() {
        return null;
    }

    /**
     * Return a Map of all inputs
     * @return a Map of all inputs
     */
    public Map<String, String> getInputs() {
        return _inputs;
    }

    /**
     * Get the value of a single input
     * @param key key of input
     * @return value of input
     */
    public String getInput(String key) {
        return _inputs.get(key);
    }

    /**
     * Set the value of a single input.
     * @param key key of input
     * @param value value of input
     */
    public void setInput(String key, String value) {
        if (getAllowedKeys() != null && !getAllowedKeys().contains(key))
            throw new IllegalStateException("The input key " + key + " is not allowed for lookups");

        _inputs.put(key, value);
    }

    /**
     * Return all elements from the input
     */
    protected void clearInputs() {
        _inputs.clear();
    }

    /**
     * Get primary site
     * @return primary site
     */
    public String getSite() {
        return _inputs.get(StagingData.PRIMARY_SITE_KEY);
    }

    /**
     * Set primary site
     * @param site primary site
     */
    public void setSite(String site) {
        _inputs.put(StagingData.PRIMARY_SITE_KEY, site);
    }

    /**
     * Get histology
     * @return histology
     */
    public String getHistology() {
        return _inputs.get(StagingData.HISTOLOGY_KEY);
    }

    /**
     * Set histology
     * @param hist histology
     */
    public void setHistology(String hist) {
        _inputs.put(StagingData.HISTOLOGY_KEY, hist);
    }

    /**
     * Return true if the inputs contain a discriminator.  A key that is not site or hist which has a non-null/non-empty value is considered a discriminator
     * @return true or false indicating whether a discriminator exists
     */
    public boolean hasDiscriminator() {
        boolean hasDiscriminator = false;

        for (Entry<String, String> entry : _inputs.entrySet()) {
            String key = entry.getKey();

            if (StagingData.STANDARD_LOOKUP_KEYS.contains(key))
                continue;

            String value = entry.getValue();
            if (value != null && !value.isEmpty()) {
                hasDiscriminator = true;
                break;
            }
        }

        return hasDiscriminator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SchemaLookup lookup = (SchemaLookup)o;

        return _inputs.equals(lookup._inputs);

    }

    @Override
    public int hashCode() {
        return _inputs.hashCode();
    }
}
