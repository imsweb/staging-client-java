/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.Map;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

import com.imsweb.decisionengine.DecisionEngine;
import com.imsweb.decisionengine.StringRange;

/**
 * For some reason, the className was being stored for every StagingStringRange in MongoDB.  I think it was getting confused
 * because the StagingTableRow has a Map of String to a List of StagingStringRanges. Since the string range was buried two levels deep it thought it
 * needed to add the className.  The workaround I used was to add "@Entity(noClassnameStored = true)" even though you don't normally
 * need to add the @Entity to an @Embedded class.  It seems to fix the problem.
 */
@Embedded
@Entity(noClassnameStored = true)
public class StagingStringRange extends StringRange {

    @Property("low")
    private String _low;
    @Property("high")
    private String _high;

    /**
     * Construct a string range that matches any string
     */
    public StagingStringRange() {
    }

    /**
     * Construct a string range with a low and high bound
     * @param low low value
     * @param high high value
     */
    public StagingStringRange(String low, String high) {
        if (low == null || high == null)
            throw new IllegalStateException("Invalid range");

        _low = low;
        _high = high;
    }

    public String getLow() {
        return _low;
    }

    public String getHigh() {
        return _high;
    }

    /**
     * If low and high are both null, then this range matches all strings
     * @return true if this range matches all strings
     */
    private boolean matchesAll() {
        return _low == null && _high == null;
    }

    @Override
    public boolean contains(String value, Map<String, String> context) {
        if (matchesAll())
            return true;

        // translate the context values if they are there
        String low = DecisionEngine.translateValue(_low, context);
        String high = DecisionEngine.translateValue(_high, context);

        // if the context value(s) failed or the low and high values are different length, return false
        if (value == null || low.length() != high.length() || low.length() != value.length())
            return false;

        // compare value to low and high
        return low.compareTo(value) <= 0 && high.compareTo(value) >= 0;
    }
}
