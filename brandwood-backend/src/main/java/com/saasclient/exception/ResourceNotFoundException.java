package com.saasclient.exception;

import org.springframework.http.HttpStatus;

/** Ressource inexistante (404). */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
