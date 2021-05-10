/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

/**
 * Defines a column in a Table
 */
public interface ColumnDefinition {

    /**
     * The key representing the field name
     * @return a String key
     */
    String getKey();

    /**
     * The name of the column
     * @return a String name
     */
    String getName();

    /**
     * The type of column
     * @return a ColumnType representing the type
     */
    ColumnType getType();

    /**
     * The column source
     * @return the String source
     */
    String getSource();

    enum ColumnType {
        INPUT,
        DESCRIPTION,
        ENDPOINT
    }

}
