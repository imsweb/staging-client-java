/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

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

    /**
     * If supplied, the value a field will be assigned when it is not supplied
     * @return A String representing the default value
     */
    String getDefault();

    /**
     * If supplied, the value of the field is verified to be contained in the table
     * @return a String representing the lookup table name
     */
    String getTable();

    /**
     * If supplied, the value of the field must be contained in the list of string ranges
     * @return a List of StringRange objects representing allowed values
     */
    List<? extends StringRange> getValues();

    /**
     * Return true if the field is used in the calculation
     * @return a Boolean representing whether the field is used in the calculation
     */
    Boolean getUsedForStaging();

    /**
     * Return true if staging should not be attempted for invalid input values
     * @return
     */
    Boolean getFailOnInvalid();

}
