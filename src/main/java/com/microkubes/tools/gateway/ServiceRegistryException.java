package com.microkubes.tools.gateway;

public class ServiceRegistryException extends Exception {
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
