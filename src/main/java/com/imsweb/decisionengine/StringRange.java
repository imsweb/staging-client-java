/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

import java.util.Map;

/**
 * An interface representing a string range
 */
public abstract class StringRange {

    /**
     * Returns true of the passed value is contained in the range
     * @param value a value to test for
     * @param context a context which is used if the range contains a variable, designated by {{variable}}
     * @return true if the value is in the range
     */
    public abstract boolean contains(String value, Map<String, String> context);

}
