package com.microkubes.tools.gateway;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a service plugin.
 * <p>
 * A plugin is an extension that can be added to the API Gateway definition for the service that adds extra
 * functionality on the gateway for this specific service, like: CORS, authorization etc.
 */
public class ServicePlugin {
    private String name;
    private Map<String, String> properties = new HashMap<>();

    /**
     * Service plugin with the specified name.
     *
     * @param name
     */
    public ServicePlugin(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * The properties for the plugin to be configured on the API Gateway.
     *
     * @return
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Sets new property for the plugin with the given name and value.
     *
     * @param name  the property name.
     * @param value the value of the property.
     */
    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    @Override
    public String toString() {
        return "ServicePlugin{" +
                "name='" + name + '\'' +
                ", properties=" + properties +
                '}';
    }
}
