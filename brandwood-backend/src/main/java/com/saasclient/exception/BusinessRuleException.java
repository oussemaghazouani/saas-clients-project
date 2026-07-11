package com.saasclient.exception;

import org.springframework.http.HttpStatus;

/** Violation d'une règle métier (limites de pack, état invalide…) (400/409). */
public class BusinessRuleException extends BusinessException {

    public BusinessRuleException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public BusinessRuleException(String message, HttpStatus status) {
        super(message, status);
    }
}
