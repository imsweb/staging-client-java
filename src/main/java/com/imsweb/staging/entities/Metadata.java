/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

public interface Metadata {

    /**
     * Name of metadata item
     * @return String
     */
    String getName();

    /**
     * Start year of metadata
     * @return Integer representing year
     */
    Integer getStart();

    /**
     * End year of metadata
     * @return Integer representing year
     */
    Integer getEnd();

}
