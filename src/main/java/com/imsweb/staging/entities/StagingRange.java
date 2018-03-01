/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

import com.imsweb.decisionengine.DecisionEngine;
import com.imsweb.decisionengine.Range;

/**
 * For some reason, the className was being stored for every StagingRange in MongoDB.  I think it was getting confused
 * because the StagingTableRow has a Map of String to a List of StagingStringRanges. Since the string range was buried two levels deep it thought it
 * needed to add the className.  The workaround I used was to add "@Entity(noClassnameStored = true)" even though you don't normally
 * need to add the @Entity to an @Embedded class.  It seems to fix the problem.
 */
@Embedded
@Entity(noClassnameStored = true)
public class StagingRange extends Range {

    @Property("low")
    private String _low;
    @Property("high")
    private String _high;

    /**
     * Construct a string range that matches any string
     */
    public StagingRange() {
    }

    /**
     * Construct a string range with a low and high bound
     * @param low low value
     * @param high high value
     */
    public StagingRange(String low, String high) {
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
    public boolean matchesAll() {
        return _low == null && _high == null;
    }

    private boolean isNumeric(String value) {
        return NumberUtils.isParsable(value);
    }

    @Override
    public boolean contains(String value, Map<String, String> context) {
        if (matchesAll())
            return true;

        // make null values match the same as if they were blank
        if (value == null)
            value = "";

        // translate the context values if they are there
        String low = DecisionEngine.translateValue(_low, context);
        String high = DecisionEngine.translateValue(_high, context);

        // if input, low and high values represent decimal numbers then do a float comparison
        if (!low.equals(high) && isNumeric(low) && isNumeric(high) && isNumeric(value)) {
            Float converted = NumberUtils.createFloat(value);

            return converted >= NumberUtils.createFloat(low) && converted <= NumberUtils.createFloat(high);
        }

        // if the context value(s) failed or the low and high values are different length, return false
        if (low.length() != high.length() || low.length() != value.length())
            return false;

        // compare value to low and high
        return low.compareTo(value) <= 0 && high.compareTo(value) >= 0;
    }
}
