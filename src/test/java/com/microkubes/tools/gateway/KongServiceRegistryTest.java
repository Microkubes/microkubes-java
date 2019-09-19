package com.microkubes.tools.gateway;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class KongServiceRegistryTest {

    @Rule
    public WireMockRule rule = new WireMockRule();

    @Test
    public void testRegister_addAPI() throws ValidationException {
        stubFor(get(urlEqualTo("/apis/test-service"))
                .willReturn(aResponse().withStatus(404)));

        stubFor(post(urlEqualTo("/apis/"))
                .withRequestBody(equalToJson("{\"name\":\"test-service\", \"uris\":\"/test\",\"upstream_url\":\"http://test-service.local:8080\"}"))
                .willReturn(okJson("{}").withStatus(201)));

        KongServiceRegistry serviceRegistry = new KongServiceRegistry(rule.url("/"));

        serviceRegistry.register(ServiceInfo.NewService("test-service")
                .host("test-service.local")
                .port(8080)
                .addPath("/test")
                .getServiceInfo());
    }

    @Test
    public void testRegister_updateAPI() throws ValidationException {
        stubFor(get(urlEqualTo("/apis/test-service"))
                .willReturn(okJson("{}")));

        stubFor(patch(urlEqualTo("/apis/test-service"))
                .withRequestBody(equalToJson("{\"name\":\"test-service\", \"uris\":\"/test\",\"upstream_url\":\"http://test-service.local:8080\"}"))
                .willReturn(okJson("{}").withStatus(200)));

        KongServiceRegistry serviceRegistry = new KongServiceRegistry(rule.url("/"));

        serviceRegistry.register(ServiceInfo.NewService("test-service")
                .host("test-service.local")
                .port(8080)
                .addPath("/test")
                .getServiceInfo());
    }
}
