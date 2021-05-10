/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

/**
 * Abstract class which provides tables and starting points to algorithm
 */
public interface DataProvider {

    /**
     * Get a table by identifier
     * @param id the table id
     * @return a Table instance or null if identifier was not found
     */
    Table getTable(String id);

    /**
     * Get a schema by identifier
     * @param id the schema id
     * @return a Schema instance or null if identifier was not found
     */
    Schema getSchema(String id);

}
