/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

/**
 * Represents a mapping of a key value to another key value.
 */
public interface KeyMapping {

    /**
     * The original key
     * @return a String key
     */
    String getFrom();

    /**
     * The new key
     * @return a String key
     */
    String getTo();

}
