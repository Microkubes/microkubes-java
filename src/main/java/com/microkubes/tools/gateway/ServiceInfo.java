package com.microkubes.tools.gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ServiceInfo holds the registration data for a Microservice on the platform.
 * Usually contains the service name, service host name and port and a list of URL paths for routing.
 */
public class ServiceInfo {
    private String name;
    private String host;
    private int port;
    private String[] paths;

    private Map<String, Object> properties = new HashMap<>();

    /**
     * Constructs new empty {@link ServiceInfo}.
     */
    public ServiceInfo() {
    }

    /**
     * Constructs {@link ServiceInfo} from the given service registration data.
     *
     * @param name       the microservice name. The service will be registered under this name on the API Gateway.
     * @param host       the service container host name. This is used in the name to IP resolution when routing messages to
     *                   the microservice.
     * @param port       the port on which the service listens to. This is the port on the container on which the service can
     *                   can be accessed on.
     * @param paths      list of URI paths used as patters for routing messages to the service.
     * @param properties {@link Map} containing additional service properties for finer control over the registered
     *                   service.
     */
    public ServiceInfo(String name, String host, int port, String[] paths, Map<String, Object> properties) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.paths = paths;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String[] getPaths() {
        return paths;
    }

    public void setPaths(String[] paths) {
        this.paths = paths;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * Performs validation on the {@link ServiceInfo} data.
     *
     * @throws ValidationException if the data is not valid or data is missing or not set.
     */
    public void validate() throws ValidationException {
        if (host == null || "".equals(host)) {
            throw new ValidationException("host cannot be null");
        }
        if (port <= 0 || port > 65535) {
            throw new ValidationException("invalid port");
        }
        if (name == null || "".equals(name)) {
            throw new ValidationException("name cannot be empty or null");
        }
        if (paths == null || paths.length == 0) {
            throw new ValidationException("no paths provided for the service");
        }
    }

    /**
     * Builds new {@link ServiceInfo}.
     */
    public static class ServiceInfoBuilder {
        private String name;
        private String host;
        private int port;
        private List<String> paths;
        private Map<String, Object> properties = new HashMap<>();

        private ServiceInfoBuilder() {
        }

        /**
         * Set the name of the service.
         *
         * @param name the service name.
         * @return reference to this builder.
         */
        public ServiceInfoBuilder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the hostname of the service.
         *
         * @param host the hostname.
         * @return reference to this builder.
         */
        public ServiceInfoBuilder host(String host) {
            this.host = host;
            return this;
        }

        /**
         * Sets the port on which the service is listening on.
         *
         * @param port the service port.
         * @return reference to this builder.
         */
        public ServiceInfoBuilder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Add URL path pattern for routing messages to this servie.
         *
         * @param path the URL path pattern.
         * @return reference to this builder.
         */
        public ServiceInfoBuilder addPath(String path) {
            if (this.paths == null) {
                this.paths = new ArrayList<>();
            }
            this.paths.add(path);
            return this;
        }

        public ServiceInfoBuilder setProperty(String name, Object value) {
            this.properties.put(name, value);
            return this;
        }

        /**
         * Builds the {@link ServiceInfo} from the data collected by this builder object.
         *
         * @return ServiceInfo instance.
         * @throws ValidationException if the data provided is not valid.
         */
        public ServiceInfo getServiceInfo() throws ValidationException {
            ServiceInfo service = new ServiceInfo(name, host, port, paths.toArray(new String[]{}), properties);
            service.validate();
            return service;
        }
    }

    /**
     * Constructs new {@link ServiceInfoBuilder} for the service with the provided name.
     *
     * @param serviceName the service name.
     * @return new {@link ServiceInfoBuilder}
     */
    public static ServiceInfoBuilder NewService(String serviceName) {
        return new ServiceInfoBuilder().setName(serviceName);
    }

}
