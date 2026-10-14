package org.example.engine.exception;

public class FatalException extends RuntimeException {
    public FatalException(String message) {
        super(message);
    }
}