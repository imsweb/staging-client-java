/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.List;

/**
 * An input field in the definition
 */
public interface Input {

    /**
     * The key representing the field name
     * @return a String name
     */
    String getKey();

    String getName();

    String getDescription();

    Integer getNaaccrItem();

    String getNaaccrXmlId();

    /**
     * If supplied, the value a field will be assigned when it is not supplied
     * @return A String representing the default value
     */
    String getDefault();

    /**
     * If supplied, a table lookup (using current context) will occur to determine
     * the default value when it is not supplied. If both a default and a default table
     * are specified, the default table will be ignored.
     * @return A String representing the default value
     */
    String getDefaultTable();

    /**
     * If supplied, the value of the field is verified to be contained in the table
     * @return a String representing the lookup table name
     */
    String getTable();

    /**
     * Return true if the field is used in the calculation
     * @return a Boolean representing whether the field is used in the calculation
     */
    Boolean getUsedForStaging();

    String getUnit();

    Integer getDecimalPlaces();

    List<? extends Metadata> getMetadata();

}
