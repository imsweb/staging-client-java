/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

/**
 * Defines a column in a Table
 */
public interface ColumnDefinition {

    /**
     * The key representing the field name
     * @return a String name
     */
    String getKey();

    /**
     * The type of column
     * @return a ColumnType representing the type
     */
    ColumnType getType();

    enum ColumnType {
        INPUT,
        DESCRIPTION,
        ENDPOINT
    }

}
