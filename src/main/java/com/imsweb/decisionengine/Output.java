/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

/**
 * An output field in the definition
 */
public interface Output {

    /**
     * The key representing the field name
     * @return a String name
     */
    String getKey();

    /**
     * If supplied, the value of the field is verified to be contained in the table
     * @return a String representing the lookup table name
     */
    String getTable();

}
