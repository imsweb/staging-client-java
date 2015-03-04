/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine;

/**
 * An ENDPOINT cell
 */
public interface Endpoint {

    /**
     * The type of endpoint
     * @return an EndPointType object
     */
    EndpointType getType();

    /**
     * The value of the endpoint
     * @return a String value
     */
    String getValue();

    /**
     * The key representing the field that the value is mapped to
     * @return a String field key
     */
    String getResultKey();

    enum EndpointType {
        JUMP,
        VALUE,
        MATCH,
        STOP,
        ERROR
    }

}
