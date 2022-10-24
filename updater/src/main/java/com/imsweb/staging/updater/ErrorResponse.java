package com.imsweb.staging.updater;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {

    @JsonProperty("code")
    protected Integer _id;
    @JsonProperty("message")
    protected String _message;

    /**
     * Return the error identifier
     * @return an error identifier
     */
    public Integer getId() {
        return _id;
    }

    /**
     * Return the error message
     * @return an error message
     */
    public String getMessage() {
        return _message;
    }
}
