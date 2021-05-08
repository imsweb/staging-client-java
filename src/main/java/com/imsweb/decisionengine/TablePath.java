/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

import java.util.Set;

/**
 * A single table which is processed during the algorithm along with any redirection of input and output field values
 */
public interface TablePath {

    /**
     * The table identifier
     *
     * @return a String representing the table identifier
     */
    String getId();

    /**
     * A List of input key redirections.  Each entry maps an input key that the table uses to a new input key to read from instead.  It allows
     * for a single table to be called multiple times and use different input.
     *
     * @return a List of KeyMapping objects
     */
    Set<? extends KeyMapping> getInputMapping();

    /**
     * A List of output key redirections.  Each entry maps an output key that the table uses to a new output key.  It allows for a single table
     * to be called multiple times and produce output on different fields.
     *
     * @return a List of KeyMapping objects
     */
    Set<? extends KeyMapping> getOutputMapping();

    Set<String> getInputs();

    Set<String> getOutputs();

}
