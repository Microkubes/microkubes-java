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

        stubFor(get(urlEqualTo("/apis/test-service/plugins")).willReturn(okJson("{\"data\": []}")));

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

        stubFor(get(urlEqualTo("/apis/test-service/plugins")).willReturn(okJson("{\"data\": []}")));

        KongServiceRegistry serviceRegistry = new KongServiceRegistry(rule.url("/"));

        serviceRegistry.register(ServiceInfo.NewService("test-service")
                .host("test-service.local")
                .port(8080)
                .addPath("/test")
                .getServiceInfo());
    }

    @Test
    public void testInstallPlugin() throws ValidationException {
        stubFor(get(urlEqualTo("/apis/test-service/plugins")).willReturn(okJson("{" +
                "\"data\":" + "[" +
                "{" +
                "\"id\": \"test-plug-id\"," +
                "\"name\": \"test-plug\"" +
                "}" +
                "]" +
                "}")));

        stubFor(delete(urlEqualTo("/apis/test-service/plugins/test-plug-id")).willReturn(noContent()));
        stubFor(post(urlEqualTo("/apis/test-service/plugins")).withRequestBody(equalToJson("{\"name\": \"test-plug\", \"config\": {\"test_prop\": \"test_val\"}}", true, true)));


        KongServiceRegistry serviceRegistry = new KongServiceRegistry(rule.url("/"));


        ServicePlugin plugin = new ServicePlugin("test-plug");
        plugin.setProperty("config.test_prop", "test_val");

        ServiceInfo serviceInfo = ServiceInfo.NewService("test-service").host("test-service.local")
                .port(8080)
                .addPath("/test")
                .addPlugin(plugin)
                .getServiceInfo();

        serviceRegistry.registerPlugins(serviceInfo);


    }
}
