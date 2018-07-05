/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.decisionengine.basic;

import com.imsweb.decisionengine.Endpoint;

public class BasicEndpoint implements Endpoint {

    private EndpointType _type;
    private String _value;
    private String _resultKey;

    /**
     * Default constructor
     */
    public BasicEndpoint() {
    }

    /**
     * Constructor
     */
    public BasicEndpoint(EndpointType type, String value) {
        _type = type;
        _value = value;
    }

    /**
     * Constructor
     */
    public BasicEndpoint(EndpointType type, String value, String resultKey) {
        _type = type;
        _value = value;
        _resultKey = resultKey;
    }

    @Override
    public EndpointType getType() {
        return _type;
    }

    public void setType(EndpointType type) {
        _type = type;
    }

    @Override
    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }

    @Override
    public String getResultKey() {
        return _resultKey;
    }

    public void setResultKey(String resultKey) {
        _resultKey = resultKey;
    }

}
