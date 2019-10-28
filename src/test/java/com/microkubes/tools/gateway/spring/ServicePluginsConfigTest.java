package com.microkubes.tools.gateway.spring;

import com.microkubes.tools.gateway.ServicePlugin;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

//@RunWith(SpringRunner.class)
//@ContextConfiguration(classes = {ServicePluginsConfig.class, ServicePluginsConfigTest.class})
//@Configuration
//@PropertySource("classpath:test-plugins-config.properties")
public class ServicePluginsConfigTest extends Assert {


    @Test
    public void testCamelCaseToDotNotation() {
        assertEquals("com.microkubes.service.plugins", ServicePluginsConfig.underscoreCaseToDotNotation("com_microkubes_service_plugins"));
        assertEquals("com.microkubes", ServicePluginsConfig.underscoreCaseToDotNotation("com_microkubes"));
        assertEquals("_", ServicePluginsConfig.underscoreCaseToDotNotation("_"));
        assertEquals("__", ServicePluginsConfig.underscoreCaseToDotNotation("__"));
        assertEquals("___", ServicePluginsConfig.underscoreCaseToDotNotation("___"));

        assertEquals("_property_", ServicePluginsConfig.underscoreCaseToDotNotation("_property_"));
        assertEquals("property.some_value", ServicePluginsConfig.underscoreCaseToDotNotation("property_some__value"));
    }

    @Test
    public void testKebabCaseToUnderscoreNotation() {
        assertEquals("test_prop", ServicePluginsConfig.kebabCaseToUnderscoreNotation("test-prop"));
        assertEquals("_test_prop_", ServicePluginsConfig.kebabCaseToUnderscoreNotation("_test-prop_"));
        assertEquals("_test_prop_", ServicePluginsConfig.kebabCaseToUnderscoreNotation("-test-prop-"));
        assertEquals("_", ServicePluginsConfig.kebabCaseToUnderscoreNotation("-"));
        assertEquals("", ServicePluginsConfig.kebabCaseToUnderscoreNotation(""));
    }

    @Test
    public void testUnderscoreCaseToDotNotation() {
        assertEquals("test.prop", ServicePluginsConfig.underscoreCaseToDotNotation("test_prop"));
        assertEquals("test_prop", ServicePluginsConfig.underscoreCaseToDotNotation("test__prop"));
    }

    @Test
    public void testBuildPlugins() {
        ServicePluginsConfig config = new ServicePluginsConfig();

        Map<String, String> props = new HashMap<>();

        props.put("com.microkubes.service.plugins.cors.config.test_prop", "test_val");
        props.put("com.microkubes.service.plugins.cors.config.max_delay", "100");
        props.put("com.microkubes.service.plugins.cors.config.headers", "h1,h2");

        props.put("com.microkubes.service.plugins.custom.config.test_prop", "other_value");
        props.put("com.microkubes.service.plugins.custom.config.max_delay", "2000");
        props.put("com.microkubes.service.plugins.custom.config.headers", "h3,h4");


        config.buildPlugins(props);

        Map<String, ServicePlugin> plugins = config.getPlugins();
        assertNotNull(plugins);
        assertEquals(2, plugins.size());

        assertTrue(plugins.containsKey("cors"));
        ServicePlugin p = plugins.get("cors");
        assertEquals("cors", p.getName());
        assertNotNull(p.getProperties());
        assertEquals(3, p.getProperties().size());
        assertTrue(p.getProperties().containsKey("config.test_prop"));
        assertTrue(p.getProperties().containsKey("config.max_delay"));
        assertTrue(p.getProperties().containsKey("config.headers"));
        assertEquals("test_val", p.getProperties().get("config.test_prop"));
        assertEquals("100", p.getProperties().get("config.max_delay"));
        assertEquals("h1,h2", p.getProperties().get("config.headers"));

        assertTrue(plugins.containsKey("custom"));
        p = plugins.get("custom");
        assertEquals("custom", p.getName());
        assertNotNull(p.getProperties());
        assertEquals(3, p.getProperties().size());
        assertTrue(p.getProperties().containsKey("config.test_prop"));
        assertTrue(p.getProperties().containsKey("config.max_delay"));
        assertTrue(p.getProperties().containsKey("config.headers"));
        assertEquals("other_value", p.getProperties().get("config.test_prop"));
        assertEquals("2000", p.getProperties().get("config.max_delay"));
        assertEquals("h3,h4", p.getProperties().get("config.headers"));

    }

}