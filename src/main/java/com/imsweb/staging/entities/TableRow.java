/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.List;
import java.util.Map;

/**
 * A single row of data in a Table
 */
public interface TableRow {

    /**
     * A list of values from the input column represented by the passed key
     * @param key the field name of the column
     * @return a List of Range objects
     */
    List<? extends Range> getColumnInput(String key);

    /**
     * Return the inputs
     * @return a Map of field name to list of Range objects
     */
    Map<String, List<? extends Range>> getInputs();

    /**
     * A list of endpoints on the row
     * @return a List of Endpoint objects
     */
    List<? extends Endpoint> getEndpoints();

    void addInput(String key, List<? extends Range> range);

    void addEndpoint(Endpoint endpoint);

}
