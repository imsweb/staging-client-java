/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.List;
import java.util.Set;

/**
 * A single row of data in a Table
 */
public interface TableRow {

    /**
     * Return the list of columns
     * @return a Set of columns
     */
    Set<String> getColumns();

    /**
     * A list of values from the input column represented by the passed key
     * @param key the field name of the column
     * @return a List of Range objects
     */
    List<? extends Range> getColumnInput(String key);

    /**
     * A list of endpoints on the row
     * @return a List of Endpoint objects
     */
    List<? extends Endpoint> getEndpoints();

    void addInput(String key, List<? extends Range> range);

    void addEndpoint(Endpoint endpoint);

}
