package com.microkubes.tools.gateway;

/**
 * ServiceRegistry is the main interface for interaction with the Service registry on the platform and self-registration
 * of the microservices.
 */
public interface ServiceRegistry {
    /**
     * Does registration of the given service (with {@link ServiceInfo}) on the underlying service registry of the
     * platform.
     *
     * @param service the definition of the service contained in {@link ServiceInfo}
     */
    void register(ServiceInfo service);
}
