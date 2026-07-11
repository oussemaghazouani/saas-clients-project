package com.saasclient.exception;

import org.springframework.http.HttpStatus;

/** Opération interdite pour l'utilisateur courant (403). */
public class AccessForbiddenException extends BusinessException {

    public AccessForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
