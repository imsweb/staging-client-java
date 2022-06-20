/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.update;

public class NotAuthorizedException extends SeerApiException {

    public NotAuthorizedException() {
        super();
    }

    public NotAuthorizedException(String message) {
        super(message);
    }

    public NotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotAuthorizedException(Throwable cause) {
        super(cause);
    }

}
