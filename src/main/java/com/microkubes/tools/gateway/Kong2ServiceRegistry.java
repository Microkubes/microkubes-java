package com.microkubes.tools.gateway;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class Kong2ServiceRegistry implements ServiceRegistry {

    private String kongAdminUrl;

    private Logger logger = LoggerFactory.getLogger(Kong2ServiceRegistry.class);

    /**
     * Builds new empty {@link KongServiceRegistry}.
     */
    public Kong2ServiceRegistry() {
    }

    /**
     * Builds new {@link KongServiceRegistry} with the given URL to the Admin port on the Kong Gateway.
     *
     * @param kongAdminUrl the URL to the admin port on the Kong Gateway.
     */
    public Kong2ServiceRegistry(String kongAdminUrl) {
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
            addOrUpdateService(service);
            logger.info("Service '{}' registered on Kong v2 API Gateway.", service.getName());
            logger.debug("Service registration info: {}", service.toString());
        } catch (Exception e) {
            throw new ServiceRegistryException(e);
        }
    }

    /**
     * The logic for adding or updating an exiting service on Kong API Gateway.
     *
     * @param service the definition of the service contained in {@link ServiceInfo}
     */
    private void addOrUpdateService(ServiceInfo service) {
        if (serviceAlreadyExists(service)) {
            updateService(service);
        }
        addService(service);
    }


    /**
     * Checking the existence of service by provided service definition
     *
     * @param service the definition of the service contained in {@link ServiceInfo}
     * @return flag for existence
     */
    private boolean serviceAlreadyExists(ServiceInfo service) {
        String serviceName = service.getName();
        String path = String.format("/services/%s", serviceName);
        String url = getKongUrl(path);
        try {
            HttpResponse<String> response = Unirest.get(url).asString();
            if (response.getStatus() == 200) {
                return true;
            }
            if (response.getStatus() == 404) {
                return false;
            }
            throw new ServiceRegistryException(response.getBody());
        } catch (Exception e) {
            throw new ServiceRegistryException(e);
        }
    }

    /**
     * Adding new service entry in Kong Api Gateway and adding all routes & plugins to that service.
     *
     * @param service the definition of the service contained in {@link ServiceInfo}
     */
    private void addService(ServiceInfo service) {
        String url = getKongUrl("/service");
        try {
            String body = getServiceBody(service);
            HttpResponse<JsonNode> response = Unirest.post(url)
                    .header("Content-Type", "application/json").body(body).asJson();
            if (response.getStatus() != 201) {
                String responseBody = response.getBody().toString();
                throw new ServiceRegistryException(responseBody);
            }
            String name = service.getName();
            String path = String.format("/service/%s/routes", name);
            url = getKongUrl(path);
            body = getRouteBody(service);
            response = Unirest.post(url)
                    .header("Content-Type", "application/json").body(body).asJson();
            if (response.getStatus() != 201) {
                String responseBody = response.getBody().toString();
                throw new ServiceRegistryException(responseBody);
            }
        } catch (Exception e) {
            throw new ServiceRegistryException(e);
        }
        // TODO: add plugins
    }

    /**
     * Updating already existing service entry in Kong Api Gateway and updating / adding / deleting a routes and plugins
     *
     * @param service the definition of the service contained in {@link ServiceInfo}
     */
    private void updateService(ServiceInfo service) {
        String name = service.getName();
        String path = String.format("/service/%s", name);
        String url = getKongUrl(path);
        try {
            String body = getServiceBody(service);
            HttpResponse<JsonNode> response = Unirest.patch(url)
                    .header("Content-Type", "application/json").body(body).asJson();
            if (response.getStatus() != 200) {
                String responseBody = response.getBody().toString();
                throw new ServiceRegistryException(responseBody);
            }
            path = String.format("/service/%s/routes");
            url = getKongUrl(path);
            response = Unirest.get(url)
                    .header("Content-Type", "application/json").asJson();
            JSONArray routeList = response.getBody().getArray();
            body = getRouteBody(service);
            if (routeList.length() > 0) {
                JSONObject routeBody = routeList.getJSONObject(0);
                String id = routeBody.getString("id");
                path = String.format("/service/%s/routes/%s", name, id);
                url = getKongUrl(path);
                response = Unirest.patch(url)
                        .header("Content-Type", "application/json").body(body).asJson();
                if (response.getStatus() != 200) {
                    String responseBody = response.getBody().toString();
                    throw new ServiceRegistryException(responseBody);
                }
            } else {
                path = String.format("/service/%s/routes", name);
                url = getKongUrl(path);
                response = Unirest.post(url)
                        .header("Content-Type", "application/json").body(body).asJson();
                if (response.getStatus() != 201) {
                    String responseBody = response.getBody().toString();
                    throw new ServiceRegistryException(responseBody);
                }
            }
        } catch (Exception e) {
            throw new ServiceRegistryException(e);
        }
        for (ServicePlugin plugin : service.getPlugins()) {
            // TODO: add/update/delete plugins
        }
    }

    private String getServiceBody(ServiceInfo service) throws ValidationException {
        JSONObject obj = new JSONObject();
        String name = service.getName();
        String url = getUpstreamUrl(service);
        Integer retries = (Integer) service.getProperties().get("retries");
        Integer connectTimeout = (Integer) service.getProperties().get("upstream_connect_timeout");
        Integer readTimeout = (Integer) service.getProperties().get("upstream_read_timeout");
        Integer writeTimeout = (Integer) service.getProperties().get("upstream_send_timeout");

        obj.put("name", name);
        obj.put("url", url);
        obj.put("retries", retries);
        obj.put("connect_timeout", connectTimeout);
        obj.put("read_timeout", readTimeout);
        obj.put("write_timeout", writeTimeout);

        return obj.toString();
    }

    private String getRouteBody(ServiceInfo service) {
        JSONObject obj = new JSONObject();
        String[] paths = service.getPaths();
        Boolean preserveHost = (Boolean) service.getProperties().get("preserve_host");
        Boolean stripPath = (Boolean) service.getProperties().get("strip_uri");
        Boolean httpsOnly = (Boolean) service.getProperties().get("https_only");
        ArrayList<String> protocols = new ArrayList<>();
        protocols.add("https");
        if (!httpsOnly) {
            protocols.add("http");
        }

        obj.put("paths", paths);
        obj.put("preserve_host", preserveHost);
        obj.put("strip_path", stripPath);
        obj.put("protocols", protocols);

        return obj.toString();
    }

    private String getUpstreamUrl(ServiceInfo service) throws ValidationException {
        if (service.getHost() == null || service.getPort() <= 0 || service.getPort() > 65535) {
            throw new ValidationException("Service host or port missing");
        }
        String host = service.getHost();
        int port = service.getPort();
        return String.format("http://%s:%s", host, port);
    }

    /**
     * Gets an URL to a path on the admin endpoint on Kong.
     * Basically a helper method for appendig and constructing URLs to specific admin endpoints on Kong.
     *
     * @param path the path to the admin endpoint for which the whole URL is constructed.
     * @return the constructed URL.
     */
    private String getKongUrl(String path) {
        return kongAdminUrl + path;
    }

    public String getKongAdminUrl() {
        return kongAdminUrl;
    }

    public void setKongAdminUrl(String kongAdminUrl) {
        this.kongAdminUrl = kongAdminUrl;
    }
}
