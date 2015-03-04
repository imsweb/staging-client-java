/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

/**
 * Represents a key-value pair
 */
public interface KeyValue {

    /**
     * Return the key
     * @return a String key
     */
    String getKey();

    /**
     * Return the value
     * @return a String value
     */
    String getValue();

}
