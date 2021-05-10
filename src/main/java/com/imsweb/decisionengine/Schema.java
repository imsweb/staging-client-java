/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A schema which can be processed by DecisionEngine
 */
public interface Schema {

    enum StagingInputErrorHandler {
        // continue staging
        CONTINUE,

        // stop staging and return an failed result
        FAIL,

        // if the failed input is used for staging, stop staging and return an failed result; otherwise continue staging
        FAIL_WHEN_USED_FOR_STAGING
    }

    /**
     * A unique identifier for the definition
     * @return a String representing the definition identifier
     */
    String getId();

    String getAlgorithm();

    String getVersion();

    String getName();

    String getTitle();

    String getDescription();

    String getSubtitle();

    String getNotes();

    Date getLastModified();

    Integer getSchemaNum();

    String getSchemaSelectionTable();

    Set<String> getSchemaDiscriminators();

    List<? extends Input> getInputs();

    List<? extends Output> getOutputs();

    /**
     * The full list of inputs needed for the definition.
     * @return a Map of input key to Input
     */
    Map<String, ? extends Input> getInputMap();

    void setInputMap(Map<String, ? extends Input> parsedInputMap);

    /**
     * The full list of outputs produced from processing the definition.
     * @return a Map of input key to Output
     */
    Map<String, ? extends Output> getOutputMap();

    void setOutputMap(Map<String, ? extends Output> parsedOutputMap);

    /**
     * A list of initial key/value pairs which will be set at the start of process
     * @return a List of key/value pairs
     */
    Set<? extends KeyValue> getInitialContext();

    /**
     * The list of mappings, in order, which will be processed
     * @return a List of Mapping objects
     */
    List<? extends Mapping> getMappings();

    Set<String> getInvolvedTables();

    /**
     * How are invalid inputs handled during staging.  There are 3 choices.  First, continue processing.  Second, stop processing.  Third,
     * stop processing only if the failed input is needed for staging.
     * @return the way to handle invalid input during staging
     */
    StagingInputErrorHandler getOnInvalidInput();

}
