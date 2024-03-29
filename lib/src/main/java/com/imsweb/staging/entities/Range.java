/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.Map;

/**
 * An interface representing a string range
 */
public interface Range {

    /**
     * Get low value
     * @return String representing low value of range
     */
    String getLow();

    /**
     * Get high value
     * @return String representing high value of range
     */
    String getHigh();

    /**
     * Returns true of the passed value is contained in the range
     * @param value a value to test for
     * @param context a context which is used if the range contains a variable, designated by {{variable}}
     * @return true if the value is in the range
     */
    boolean contains(String value, Map<String, String> context);

    /**
     * Does this match all values (low and high are null)
     * @return true if it matches all
     */
    boolean matchesAll();

}
