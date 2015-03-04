/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

import java.util.List;

/**
 * A single row of data in a Table
 */
public interface TableRow {

    /**
     * A list of values from the input column represented by the passed key
     * @param key the field name of the column
     * @return a List of StringRange objects
     */
    List<? extends StringRange> getColumnInput(String key);

    /**
     * A list of endpoints on the row
     * @return a List of Endpoint objects
     */
    List<? extends Endpoint> getEndpoints();

}
