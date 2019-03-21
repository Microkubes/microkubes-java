package com.microkubes.tools.gateway;

/**
 * Represents an error that occured during the service registration process.
 */
public class ServiceRegistryException extends RuntimeException {
    public ServiceRegistryException() {
    }

    public ServiceRegistryException(String message) {
        super(message);
    }

    public ServiceRegistryException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceRegistryException(Throwable cause) {
        super(cause);
    }

    protected ServiceRegistryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
