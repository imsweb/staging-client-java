/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.Map;

/**
 * An interface representing a string range
 */
public class Range {

    private String _low;
    private String _high;

    /**
     * Construct a string range that matches any string
     */
    public Range() {
    }

    /**
     * Construct a string range with a low and high bound
     *
     * @param low  low value
     * @param high high value
     */
    public Range(String low, String high) {
        if (low == null || high == null)
            throw new IllegalStateException("Invalid range");

        _low = low;
        _high = high;
    }

    /**
     * Return true if the string can converted into a number
     *
     * @param value string value
     * @return true if string can be parsed to an integer
     */
    public static boolean isNumeric(String value) {
        return NumberUtils.isParsable(value);
    }

    public String getLow() {
        return _low;
    }

    public String getHigh() {
        return _high;
    }

    /**
     * If low and high are both null, then this range matches all strings
     *
     * @return true if this range matches all strings
     */
    public boolean matchesAll() {
        return _low == null && _high == null;
    }

    /**
     * Returns true if the value is contained in the range.  Note that the low and high values will be replaced with context if
     * they are specified that way.  There are two ways in which the values are compared.
     * <p>
     * - If the low and high values ranges are different and are both "numeric", then the value will be compared using
     * floats (which will work for the `Integer` type fields as well).
     * <p>
     * - Otherwise it will be compared using String but the strings must be the same length otherwise consider different
     */
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
        if (!low.equals(high) && isNumeric(low) && isNumeric(high)) {
            if (!isNumeric(value))
                return false;

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
