package com.microkubes.tools.gateway;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class Kong2ServiceRegistryTest {

    @Rule
    public WireMockRule rule = new WireMockRule();

    @Test
    public void testRegister_addNewService() throws ValidationException {
        stubFor(get(urlEqualTo("/services/test"))
                .willReturn(aResponse().withStatus(404)));

        stubFor(post(urlEqualTo("/services"))
                .willReturn(okJson("{\"data\": {}}").withStatus(201)));

        stubFor(post(urlEqualTo("/services/test/routes"))
                .willReturn(okJson("{\"data\": []}").withStatus(201)));

        Kong2ServiceRegistry serviceRegistry = new Kong2ServiceRegistry(rule.url("/"));
        ServiceInfo service = ServiceInfo.NewService("test").host("local").port(80).addPath("/")
                .getServiceInfo();
        service.getProperties().put("https_only", false);

        serviceRegistry.register(service);
    }

    @Test
    public void testRegister_updateService() throws ValidationException {
        stubFor(get(urlEqualTo("/services/test"))
                .willReturn(aResponse().withStatus(200)));

        stubFor(patch(urlEqualTo("/services/test"))
                .willReturn(okJson("{\"data\": {}}").withStatus(200)));

        stubFor(get(urlEqualTo("/services/test/routes"))
                .willReturn(okJson("{\"data\": []}").withStatus(200)));

        stubFor(post(urlEqualTo("/services/test/routes"))
                .willReturn(okJson("{\"data\": []}").withStatus(201)));

        Kong2ServiceRegistry serviceRegistry = new Kong2ServiceRegistry(rule.url("/"));
        ServiceInfo service = ServiceInfo.NewService("test").host("local").port(80).addPath("/")
                .getServiceInfo();
        service.getProperties().put("https_only", false);

        serviceRegistry.register(service);
    }
}
