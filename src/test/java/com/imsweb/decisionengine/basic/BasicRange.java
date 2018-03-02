/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine.basic;

import java.util.Map;

import com.imsweb.decisionengine.DecisionEngine;
import com.imsweb.decisionengine.Range;

public class BasicRange extends Range {

    private String _low;
    private String _high;
    private boolean _usesContext;

    /**
     * Construct a BasicString range that matches any string
     */
    public BasicRange() {
        _usesContext = false;
    }

    /**
     * Construct a BasicRange with a low and high bound
     * @param low low value
     * @param high high value
     */
    public BasicRange(String low, String high) {
        if (low == null || high == null)
            throw new IllegalStateException("Invalid range");
        if (low.length() != high.length())
            throw new IllegalStateException("Range strings must be the same length: '" + low + "-" + high + "'");
        if (low.compareTo(high) > 0)
            throw new IllegalStateException("Low value of range is greater than high value: '" + low + "-" + high + "'");

        _low = low;
        _high = high;

        _usesContext = ((_low.startsWith("{{") && _low.endsWith("}}")) || (_high.startsWith("{{") && _high.endsWith("}}")));
    }

    public String getLow() {
        return _low;
    }

    public String getHigh() {
        return _high;
    }

    /**
     * If low and high are both null, then this range matches all strings
     * @return true if range matches anything
     */
    private boolean matchesAll() {
        return _low == null && _high == null;
    }

    @Override
    public boolean contains(String value, Map<String, String> context) {
        // make null values match the same as if they were blank
        if (value == null)
            value = "";

        if (_usesContext)
            return (matchesAll() || (DecisionEngine.translateValue(_low, context).compareTo(value) <= 0 && DecisionEngine.translateValue(_high, context).compareTo(value) >= 0));
        else
            return (matchesAll() || (_low.compareTo(value) <= 0 && _high.compareTo(value) >= 0));
    }
}
