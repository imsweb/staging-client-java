/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.update;

public class BadRequestException extends SeerApiException {

    public BadRequestException() {
        super();
    }

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadRequestException(Throwable cause) {
        super(cause);
    }

}
