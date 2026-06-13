package com.circleguard.auth.client;

public class IdentityServiceUnavailableException extends RuntimeException {
    public IdentityServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
