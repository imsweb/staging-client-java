/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities.basic;

import com.imsweb.staging.entities.KeyMapping;

public class BasicKeyMapping implements KeyMapping {

    String _from;
    String _to;

    /**
     * Default constructor
     */
    public BasicKeyMapping() {
    }

    /**
     * Construct with from and to values
     * @param from low value
     * @param to high value
     */
    public BasicKeyMapping(String from, String to) {
        _from = from;
        _to = to;
    }

    @Override
    public String getFrom() {
        return _from;
    }

    /**
     * Set the low value
     * @param from low value
     */
    public void setFrom(String from) {
        _from = from;
    }

    @Override
    public String getTo() {
        return _to;
    }

    /**
     * Set the high value
     * @param to high value
     */
    public void setTo(String to) {
        _to = to;
    }
}
