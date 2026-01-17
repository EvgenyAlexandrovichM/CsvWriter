package org.writer.exception;

public class CsvException extends RuntimeException {
    public CsvException(String message, Throwable cause) {
        super(message, cause);
    }
}
