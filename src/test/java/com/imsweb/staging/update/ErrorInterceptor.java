/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.staging.update;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Interceptor to catch all non-200 responses and convert them to exceptions.
 */
class ErrorInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());

        if (response.code() != 200) {
            // convert body to error response
            ErrorResponse error = null;
            if (response.body() != null) {
                try {
                    error = new ObjectMapper().readValue(response.body().byteStream(), ErrorResponse.class);
                }
                catch (IOException e) {
                    // sometimes the error message is not right format (like for 404 errors)
                }
            }

            String message = error == null ? "Error code " + response.code() : error.getMessage();

            switch (response.code()) {
                case 401:  // unauthorized
                    throw new NotAuthorizedException(message);
                case 400:  // bad request
                    throw new BadRequestException(message);
                case 404:
                    throw new NotFoundException(message);
                default:
                    throw new SeerApiException(message);
            }
        }

        return response;
    }
}
