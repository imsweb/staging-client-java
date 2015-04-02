/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A definition which can be processed by DecisionEngine
 */
public interface Definition {

    /**
     * A unique identifier for the definition
     * @return a String representing the definition identifier
     */
    String getId();

    /**
     * The full list of inputs needed for the definition.
     * @return a Map of input key to Input
     */
    Map<String, ? extends Input> getInputMap();

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

}
