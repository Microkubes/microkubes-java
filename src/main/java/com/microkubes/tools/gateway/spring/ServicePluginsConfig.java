package com.microkubes.tools.gateway.spring;

import com.microkubes.tools.gateway.ServicePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Configuration loader for service plugins.
 * <p>
 * Plugins for services can be defined in a properties file or via ENV variables.
 * <p>
 * The plugin properties must start with:
 * <ul>
 *     <li><code>com.microkubes.service.plugins</code> -  when specified in a .properites file; or</li>
 *     <li><code>com_microkubes_service_plugins</code> - when specified as an ENV variable.</li>
 * </ul>
 */
@Component
public class ServicePluginsConfig implements ApplicationContextAware {

    public static final String PLUGINS_PREFIX = "com.microkubes.service.plugins";
    private static final String PLUGINS_PREFIX_CAMEL_CASE = PLUGINS_PREFIX.replaceAll("\\.", "_");

    private Map<String, ServicePlugin> plugins = new HashMap<>();

    private static Logger logger = LoggerFactory.getLogger(ServicePluginsConfig.class);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        buildPlugins(getNormalizedPluginsProperties(applicationContext));
        logger.debug("Found {} plugins: {}", plugins.size(), plugins.keySet());
    }

    /**
     * Returns a map of the currently configured plugins for the service.
     *
     * @return
     */
    public Map<String, ServicePlugin> getPlugins() {
        return plugins;
    }

    /**
     * Provides a list of normalized properties for processing.
     * <p>
     * Properties that are set via properties file (or other means) and which start with
     * <code>com.microkubes.service.plugins</code> are taken into consideration.
     * <p>
     * Properties that are set as ENV variables and if the variable name  starts with
     * <code>com_microkubes_service_plugins</code> are normalized to dot-notation. For example:
     * <code>com_microkubes_service_plugins_cors_config_retry__timeout</code> is normalized to
     * <code>com.microkubes.service.plugins.cors.config.retry_timeout</code>
     *
     * @param applicationContext
     * @return
     */
    private static Map<String, String> getNormalizedPluginsProperties(ApplicationContext applicationContext) {
        Map<String, String> propsMap = new TreeMap<>();
        if (applicationContext.getEnvironment() instanceof AbstractEnvironment) {
            AbstractEnvironment environment = (AbstractEnvironment) applicationContext.getEnvironment();
            Iterator<PropertySource<?>> it = environment.getPropertySources().iterator();
            while (it.hasNext()) {
                PropertySource<?> propertySource = it.next();
                if (propertySource instanceof MapPropertySource) {
                    MapPropertySource mp = (MapPropertySource) propertySource;
                    for (String name : mp.getPropertyNames()) {
                        if (name.startsWith(PLUGINS_PREFIX_CAMEL_CASE)) {
                            logger.debug("Found ENV Property: {}", name);
                            propsMap.put(normalizePropertyName(name), environment.getProperty(name));
                        } else if (name.startsWith(PLUGINS_PREFIX)) {
                            logger.debug("Found Property: {}", name);
                            propsMap.put(name, environment.getProperty(name));
                        }
                    }
                }
            }
        }
        return propsMap;
    }

    /**
     * Builds the plugins from the given normalized properties.
     *
     * @param pluginsProperties a map of the names/value for the plugins properties.
     */
    protected void buildPlugins(Map<String, String> pluginsProperties) {
        for (Map.Entry<String, String> prop : pluginsProperties.entrySet()) {
            String noPrefixName = stripPrefix(prop.getKey(), PLUGINS_PREFIX + ".").trim();
            if (noPrefixName.equals("")) {
                continue;
            }
            String pluginName = getPluginNameFromProperty(noPrefixName);
            if (pluginName == null) {
                continue;
            }
            if (!plugins.containsKey(pluginName)) {
                plugins.put(pluginName, new ServicePlugin(pluginName));
            }
            ServicePlugin plugin = plugins.get(pluginName);
            String propertyName = stripPrefix(noPrefixName, pluginName + ".");
            plugin.setProperty(propertyName, prop.getValue());
        }
    }

    private static String stripPrefix(String str, String prefix) {
        if (str.startsWith(prefix)) {
            return str.substring(prefix.length());
        }
        return str;
    }

    private static String getPluginNameFromProperty(String propName) {
        int idx = propName.indexOf('.');
        if (idx <= 0) {
            return null;
        }
        return propName.substring(0, idx);
    }

    /**
     * Normalizes a property name as loaded from a property file or given in the current process environment. <br/>
     * <p>
     * The name is normalized in the following way:
     * <ul>
     *     <li>If the property follows a dot notation - it is left unchanged.
     *     For example: <code>test.prop</code> will not be changed</li>
     *     <li>Property name with underscore notation will be changed to dot notation. Examples:
     *     <ul>
     *         <li><code>test_prop</code> → <code>test.prop</code></li>
     *         <li><code>test__prop</code> → <code>test_prop</code></li>
     *         <li><code>test-prop</code> → <code>test_prop</code></li>
     *     </ul>
     *     </li>
     *
     * </ul>
     *
     * @param name
     * @return
     */
    public static String normalizePropertyName(String name) {
        return kebabCaseToUnderscoreNotation(underscoreCaseToDotNotation(name));
    }

    /**
     * Converts a string in underscore notation (<code>test_prop</code>) to dot notation (<code>test.prop</code>).
     *
     * @param val
     * @return
     */
    public static String underscoreCaseToDotNotation(String val) {
        return val.replaceAll("([^_])(_)([^_])", "$1.$3").replaceAll("([^_])(__)([^_])", "$1_$3");
    }

    /**
     * Converts a string in kebab-case notation (<code>test-prop</code>) to underscore notation (<code>test_prop</code>).
     *
     * @param val
     * @return
     */
    public static String kebabCaseToUnderscoreNotation(String val) {
        return val.replaceAll("-", "_");
    }
}
