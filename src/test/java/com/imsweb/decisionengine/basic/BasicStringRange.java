/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine.basic;

import java.util.Map;

import com.imsweb.decisionengine.DecisionEngine;
import com.imsweb.decisionengine.StringRange;

public class BasicStringRange extends StringRange {

    private String _low;
    private String _high;
    private boolean _usesContext;

    /**
     * Construct a BasicString range that matches any string
     */
    public BasicStringRange() {
        _usesContext = false;
    }

    /**
     * Construct a BasicStringRange with a low and high bound
     * @param low
     * @param high
     */
    public BasicStringRange(String low, String high) {
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

    /**
     * If low and high are both null, then this range matches all strings
     * @return
     */
    private boolean matchesAll() {
        return _low == null && _high == null;
    }

    @Override
    public boolean contains(String value, Map<String, String> context) {
        if (_usesContext)
            return (matchesAll() || (value != null && DecisionEngine.translateValue(_low, context).compareTo(value) <= 0 && DecisionEngine.translateValue(_high, context).compareTo(value) >= 0));
        else
            return (matchesAll() || (value != null && _low.compareTo(value) <= 0 && _high.compareTo(value) >= 0));
    }
}
