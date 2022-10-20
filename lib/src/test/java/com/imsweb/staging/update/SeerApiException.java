/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.update;

public class SeerApiException extends RuntimeException {

    public SeerApiException() {
        super();
    }

    public SeerApiException(String message) {
        super(message);
    }

    public SeerApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public SeerApiException(Throwable cause) {
        super(cause);
    }

}
