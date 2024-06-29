package org.sola.services.common.faults;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Open Tenure exception object
 */
public class OTRestException extends WebApplicationException {

    public OTRestException(int statusCode, String message) {
        super(Response.status(statusCode)
                .entity(message)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON + "; charset=UTF-8")
                .build());
    }
}
