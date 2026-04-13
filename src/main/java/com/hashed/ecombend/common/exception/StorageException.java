package com.hashed.ecombend.common.exception;

/**
 * Raised when file storage operations fail due to server-side I/O issues.
 */
public class StorageException extends RuntimeException {

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

