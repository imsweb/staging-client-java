/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * A table definition used in the Algorithm
 */
public interface Table {

    /**
     * A unique identifier for the table
     * @return a String representing the table identifier
     */
    String getId();

    String getAlgorithm();

    String getVersion();

    String getName();

    String getTitle();

    String getDescription();

    String getSubtitle();

    String getNotes();

    String getFootnotes();

    String getRationale();

    String getAdditionalInfo();

    String getCodingGuidelines();

    Date getLastModified();

    /**
     * Return a list of the column definitions
     * @return a List of ColumnDefinition
     */
    List<? extends ColumnDefinition> getColumnDefinitions();

    /**
     * Returns a list of input keys that are references in the table rows.  References are in the format "{{key}}".
     * @return a list of input keys
     */
    Set<String> getExtraInput();

    void setExtraInput(Set<String> extraInput);

    List<List<String>> getRawRows();

    /**
     * Return the data of the table as a list of rows
     * @return a List of TableRow objects
     */
    List<? extends TableRow> getTableRows();

    void addTableRow(TableRow row);

    void clearTableRows();

}
