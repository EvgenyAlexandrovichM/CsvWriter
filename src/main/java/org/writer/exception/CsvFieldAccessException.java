package org.writer.exception;

public class CsvFieldAccessException extends CsvException {
    public CsvFieldAccessException(String fieldName, Throwable cause) {
        super("Field access error: " + fieldName, cause);
    }
}
