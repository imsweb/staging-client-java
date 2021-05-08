/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

import java.util.Set;

/**
 * An output field in the definition
 */
public interface Output {

    /**
     * The key representing the field name
     *
     * @return a String name
     */
    String getKey();

    String getName();

    String getDescription();

    Integer getNaaccrItem();

    String getNaaccrXmlId();

    /**
     * If supplied, the value of the field is verified to be contained in the table
     *
     * @return a String representing the lookup table name
     */
    String getTable();

    /**
     * If supplied, a default value to give the field at the beginning of the staging process
     *
     * @return a default value to be set for the output
     */
    String getDefault();

    Set<String> getMetadata();

}
