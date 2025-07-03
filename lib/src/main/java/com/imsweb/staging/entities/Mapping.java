/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.List;
import java.util.Set;

public interface Mapping {

    /**
     * A unique identifier for the mapping
     * @return a String representing the mapping identifier
     */
    String getId();

    /**
     * A name for the mapping
     * @return a String representing the mapping name
     */
    String getName();

    /**
     * Return a list of table names containing the inclusion conditions
     * @return the list of table names
     */
    List<? extends TablePath> getInclusionTables();

    /**
     * Return a list of table names containing the exclusion conditions
     * @return the list of table names
     */
    List<? extends TablePath> getExclusionTables();

    /**
     * A list of initial key/value pairs which will be set at the start of the mapping
     * @return a Set of key/value pairs
     */
    Set<? extends KeyValue> getInitialContext();

    /**
     * The list of table paths, in order, which will be processed
     * @return a List of TablePath objects
     */
    List<? extends TablePath> getTablePaths();

}
