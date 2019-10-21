package com.microkubes.tools.gateway;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * {@link ServiceRegistry} for Kong Gateway.
 * Registers microservices as APIs on the Kong API Gateway.
 */
public class KongServiceRegistry implements ServiceRegistry {
    private String kongAdminUrl;

    private Logger logger = LoggerFactory.getLogger(KongServiceRegistry.class);

    /**
     * Builds new empty {@link KongServiceRegistry}.
     */
    public KongServiceRegistry() {
    }

    /**
     * Builds new {@link KongServiceRegistry} with the given URL to the Admin port on the Kong Gateway.
     *
     * @param kongAdminUrl the URL to the admin port on the Kong Gateway.
     */
    public KongServiceRegistry(String kongAdminUrl) {
        this.kongAdminUrl = kongAdminUrl;
    }

    /**
     * Registers the service as an API on Kong API Gateway.
     * A preliminary check is performed for the existence of the API. If it exists and it has already been registered,
     * then the API definition will be updated. Otherwise, new API is added to the list of APIs on Kong Gateway.
     *
     * @param service the definition of the service contained in {@link ServiceInfo}
     */
    @Override
    public void register(ServiceInfo service) {
        try {
            service.validate();
            addOrUpdateApi(service.getName(), toKongAPIBody(service));
            logger.info("Service '{}' registered on Kong API Gateway.", service.getName());
            logger.debug("Service registration info: {}", service.toString());
            registerPlugins(service);
        } catch (ValidationException e) {
            throw new ServiceRegistryException(e);
        }
    }

    private JSONObject toKongAPIBody(ServiceInfo service) throws ValidationException {
        JSONObject obj = new JSONObject();

        obj.put("name", service.getName());
        obj.put("upstream_url", getUpstreamUrl(service));
        obj.put("uris", String.join(",", service.getPaths()));

        for (Map.Entry<String, Object> entry : service.getProperties().entrySet()) {
            obj.put(entry.getKey(), entry.getValue());
        }

        return obj;
    }

    private String getUpstreamUrl(ServiceInfo service) throws ValidationException {
        if (service.getHost() == null || service.getPort() <= 0 || service.getPort() > 65535) {
            throw new ValidationException("Service host or port missing");
        }
        return String.format("http://%s:%s", service.getHost(), service.getPort());
    }

    private boolean apiAlreadyExists(String apiName) {
        try {
            HttpResponse<String> response = Unirest.get(getKongUrl("/apis/" + apiName)).asString();
            if (response.getStatus() == 200) {
                return true;
            }
            if (response.getStatus() == 404) {
                return false;
            }
            throw new ServiceRegistryException(response.getBody());
        } catch (UnirestException e) {
            throw new ServiceRegistryException(e);
        }
    }

    private JsonNode addApi(JSONObject apiDef) {
        try {
            HttpResponse<JsonNode> response = Unirest.post(getKongUrl("/apis/"))
                    .header("Content-Type", "application/json")
                    .body(apiDef.toString())
                    .asJson();
            if (response.getStatus() != 201) {
                throw new ServiceRegistryException(response.getBody().toString());
            }
            return response.getBody();
        } catch (UnirestException e) {
            throw new ServiceRegistryException(e);
        }
    }

    private JsonNode updateApi(String apiName, JSONObject apiDef) {
        try {
            HttpResponse<JsonNode> response = Unirest.patch(getKongUrl("/apis/" + apiName))
                    .header("Content-Type", "application/json")
                    .body(apiDef.toString())
                    .asJson();
            if (response.getStatus() != 200) {
                throw new ServiceRegistryException(response.getBody().toString());
            }
            return response.getBody();
        } catch (UnirestException e) {
            throw new ServiceRegistryException(e);
        }
    }

    /**
     * Implements the logic for adding or updating an exiting API on Kong API Gateway.
     *
     * @param apiName the name of the API. This is extracted from the {@link ServiceInfo#getName()}.
     * @param apiDef  the API definition as JSON object to be send to Kong.
     * @return the response as JSON received from Kong.
     */
    protected JsonNode addOrUpdateApi(String apiName, JSONObject apiDef) {
        if (apiAlreadyExists(apiName)) {
            return updateApi(apiName, apiDef);
        }
        return addApi(apiDef);
    }


    /**
     * Adds (registers/installs) the plugin for the given service.
     *
     * @param apiName the name of the service.
     * @param plugin  the plugin to install
     * @return JsonNode of the Kong response for the new plugin.
     */
    protected JsonNode registerPlugin(String apiName, ServicePlugin plugin) {
        JSONObject pluginData = toJson(plugin);
        try {
            HttpResponse<JsonNode> response = Unirest
                    .post(getKongUrl(String.format("/apis/%s/plugins", apiName)))
                    .header("Content-Type", "application/json")
                    .body(pluginData)
                    .asJson();
            if (response.getStatus() != 200 && response.getStatus() != 201) {
                logger.debug("Failed to install plugin. Response code was: {} {}", response.getStatus(), response.getStatusText());
                throw new ServiceRegistryException(response.getBody().toString());
            }
            logger.info("API {}: Installed plugin: {}", apiName, plugin);
            return response.getBody();
        } catch (UnirestException e) {
            throw new ServiceRegistryException(e);
        }
    }

    /**
     * Clears all plugins registered for this service.<br/>
     * <p>
     * This deletes all plugins for the given service on Kong.
     *
     * @param service
     */
    protected void clearPlugins(ServiceInfo service) {
        try {
            HttpResponse<JsonNode> response = Unirest
                    .get(getKongUrl(String.format("/apis/%s/plugins", service.getName())))
                    .asJson();
            if (response.getStatus() != 200) {
                throw new ServiceRegistryException(response.getBody().toString());
            }
            JsonNode resp = response.getBody();
            for (Object plugin : resp.getObject().getJSONArray("data")) {
                if (plugin instanceof JSONObject) {
                    deletePlugin(service.getName(), ((JSONObject) plugin).getString("id"));
                }
            }
        } catch (UnirestException e) {
            throw new ServiceRegistryException(e);
        }
        logger.debug("Earlier plugins cleared.");
    }

    /**
     * Deletes single plugin for the given service.
     *
     * @param apiName  the name of the service
     * @param pluginId the id of the plugin to delete.
     * @throws UnirestException
     */
    private void deletePlugin(String apiName, String pluginId) throws UnirestException {
        logger.debug("API {}: removing plugin: {}", apiName, pluginId);
        HttpResponse<String> response = Unirest.delete(getKongUrl(String.format("/apis/%s/plugins/%s", apiName, pluginId))).asString();
        if (response.getStatus() != 200 && response.getStatus() != 204) {
            logger.debug("Failed to remove plugin. The response code was: {} {}", response.getStatus(), response.getStatusText());
            throw new ServiceRegistryException(response.getBody());
        }
    }


    /**
     * Registers the plugins defined for this service.
     *
     * @param service {@link ServiceInfo} representing the service.
     */
    protected void registerPlugins(ServiceInfo service) {
        logger.debug("Registering plugins for service...");
        clearPlugins(service);
        if (service.getPlugins() == null || service.getPlugins().length == 0) {
            return;
        }
        for (ServicePlugin plugin : service.getPlugins()) {
            registerPlugin(service.getName(), plugin);
        }
    }

    private JSONObject toJson(ServicePlugin plugin) {
        JSONObject data = new JSONObject();
        data.put("name", plugin.getName());

        JSONObject config = new JSONObject();
        data.put("config", config);

        for (Map.Entry<String, String> entry : plugin.getProperties().entrySet()) {
            if (entry.getKey().startsWith("config.")) {
                config.put(entry.getKey().substring("config.".length()), entry.getValue());
            }
        }
        logger.debug("Service plugin JSON: {}", data.toString(2));
        return data;
    }

    /**
     * Gets an URL to a path on the admin endpoint on Kong.
     * Basically a helper method for appendig and constructing URLs to specific admin endpoints on Kong.
     *
     * @param path the path to the admin endpoint for which the whole URL is constructed.
     * @return the constructed URL.
     */
    protected String getKongUrl(String path) {
        return kongAdminUrl + path;
    }

    public String getKongAdminUrl() {
        return kongAdminUrl;
    }

    public void setKongAdminUrl(String kongAdminUrl) {
        this.kongAdminUrl = kongAdminUrl;
    }
}
