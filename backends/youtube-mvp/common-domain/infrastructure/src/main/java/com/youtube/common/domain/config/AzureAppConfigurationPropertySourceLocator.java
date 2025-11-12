package com.youtube.common.domain.config;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.identity.DefaultAzureCredentialBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EnvironmentPostProcessor that loads properties from Azure App Configuration and adds them to Spring Environment.
 * This allows services to inject values from Azure App Configuration using @Value or @ConfigurationProperties.
 * 
 * <p>Properties loaded from Azure App Configuration will be available in the Spring Environment
 * and can be injected in application-common.yml or via @Value annotations.</p>
 * 
 * <p>The PropertySource is added with high priority so that Azure App Configuration values
 * can override local application-common.yml values if needed.</p>
 * 
 * <p>This processor runs early in the Spring Boot startup process, before beans are created,
 * so properties are available for injection during application initialization.</p>
 */
@Slf4j
public class AzureAppConfigurationPropertySourceLocator implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "azure-app-configuration";
    private static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 10; // Run early, but after system properties
    private static final String COMMON_DOMAIN_PROPERTY_SOURCE_NAME = "common-domain-application-yml";


    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            // loadCommonDomainApplicationYml(environment);
            String enabled = environment.getProperty("azure.appconfig.enabled");
            if (enabled == null || !Boolean.parseBoolean(enabled)) {
                log.debug("Azure App Configuration is disabled, skipping property loading");
                return;
            }

            log.info("Loading properties from Azure App Configuration");
            
            // Create ConfigurationClient from environment properties
            ConfigurationClient configurationClient = createConfigurationClient(environment);
            if (configurationClient == null) {
                log.warn("Could not create ConfigurationClient, skipping property loading");
                return;
            }
            
            Map<String, Object> properties = new HashMap<>();
            
            // Get label configuration - supports both YAML list and comma-separated string
            String[] labels = parseLabels(environment);
            
            // Load configuration settings for each label (or all if no label specified)
            if (labels.length == 0) {
                // No label specified - load all settings without label filter
                log.info("No labels specified, loading all configuration settings");
                SettingSelector selector = new SettingSelector();
                loadSettings(configurationClient, selector, properties, null);
            } else {
                // Load settings for each specified label
                log.info("Loading configuration settings for labels: {}", String.join(", ", labels));
                for (String label : labels) {
                    SettingSelector selector = new SettingSelector().setLabelFilter(label);
                    loadSettings(configurationClient, selector, properties, label);
                }
            }
            
            log.info("Loaded {} properties from Azure App Configuration", properties.size());
            
            // Add PropertySource to environment with high priority (before application-common.yml)
            MutablePropertySources propertySources = environment.getPropertySources();
            MapPropertySource propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, properties);
            
            // Add after system properties but before application properties
            // This allows Azure App Config to override application-common.yml values
            if (propertySources.contains("systemProperties")) {
                propertySources.addAfter("systemProperties", propertySource);
            } else if (propertySources.contains("systemEnvironment")) {
                propertySources.addAfter("systemEnvironment", propertySource);
            } else {
                propertySources.addFirst(propertySource);
            }
            
            log.info("Azure App Configuration PropertySource registered successfully");
        } catch (Exception e) {
            log.error("Failed to load properties from Azure App Configuration", e);
            // Don't fail startup if App Configuration is unavailable
        }
    }

    /**
     * Creates a ConfigurationClient from environment properties.
     */
    private ConfigurationClient createConfigurationClient(ConfigurableEnvironment environment) {
        try {
            String connectionString = environment.getProperty("azure.appconfig.connection-string");
            String endpoint = environment.getProperty("azure.appconfig.endpoint");
            
            ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            
            if (connectionString != null && !connectionString.isEmpty()) {
                builder.connectionString(connectionString);
            } else if (endpoint != null && !endpoint.isEmpty()) {
                builder.endpoint(endpoint)
                    .credential(new DefaultAzureCredentialBuilder().build());
            } else {
                log.warn("Neither azure.appconfig.connection-string nor azure.appconfig.endpoint is configured");
                return null;
            }
            
            return builder.buildClient();
        } catch (Exception e) {
            log.error("Failed to create ConfigurationClient", e);
            return null;
        }
    }

    /**
     * Loads configuration settings using the provided selector and adds them to the properties map.
     * 
     * @param configurationClient The Azure App Configuration client
     * @param selector The setting selector with filters
     * @param properties The map to add loaded properties to
     * @param label The label being loaded (for logging purposes, null if loading all)
     */
    private void loadSettings(ConfigurationClient configurationClient, SettingSelector selector, 
                              Map<String, Object> properties, String label) {
        String labelInfo = label != null ? " (label: " + label + ")" : " (no label)";
        log.debug("Loading configuration settings{}", labelInfo);
        
        configurationClient.listConfigurationSettings(selector)
            .forEach(setting -> {
                String key = normalizeKey(setting.getKey());
                String value = setting.getValue();
                if (key != null && value != null) {
                    // If key already exists, log a warning (label precedence)
                    if (properties.containsKey(key)) {
                        log.debug("Property '{}' already exists, overwriting with value from label '{}'", 
                                 key, setting.getLabel() != null ? setting.getLabel() : "default");
                    }
                    properties.put(key, value);
                    log.debug("Loaded property: {} = {} [label: {}]", 
                             key, maskSensitiveValue(key, value), 
                             setting.getLabel() != null ? setting.getLabel() : "default");
                }
            });
    }

    /**
     * Parses the labels configuration from the environment.
     * Supports multiple formats:
     * 1. YAML list format: labels: [dev, common] or labels: - dev - common
     * 2. Comma-separated string: labels: "dev,common"
     * 3. Single label: labels: "dev"
     * 4. Empty/null: loads all settings
     * 
     * @param environment The Spring environment
     * @return Array of label strings, empty array if no labels specified
     */
    private String[] parseLabels(ConfigurableEnvironment environment) {
        // First, try to get as a List (YAML list format)
        Object labelsObj = environment.getProperty("azure.appconfig.labels", Object.class);
        
        if (labelsObj == null) {
            return new String[0];
        }
        
        // If it's already a List (YAML list format)
        if (labelsObj instanceof java.util.List) {
            java.util.List<?> labelsList = (java.util.List<?>) labelsObj;
            return labelsList.stream()
                .map(Object::toString)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        }
        
        // If it's a String (comma-separated or single label)
        if (labelsObj instanceof String) {
            String labelsConfig = (String) labelsObj;
            if (labelsConfig.trim().isEmpty()) {
                return new String[0];
            }
            
            // Split by comma and trim whitespace
            return java.util.Arrays.stream(labelsConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        }
        
        // Fallback: convert to string and parse
        String labelsConfig = labelsObj.toString();
        if (labelsConfig.trim().isEmpty()) {
            return new String[0];
        }
        
        return java.util.Arrays.stream(labelsConfig.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);
    }

    /**
     * Normalizes the key from Azure App Configuration format to Spring property format.
     * Converts keys like "my-service:database:url" to "my-service.database.url"
     */
    private String normalizeKey(String key) {
        if (key == null) {
            return null;
        }
        // Replace colons with dots for nested properties
        // Azure App Configuration uses colons, Spring uses dots
        return key.replace(":", ".");
    }

    /**
     * Masks sensitive values in logs (passwords, connection strings, etc.)
     */
    private String maskSensitiveValue(String key, String value) {
        if (key == null || value == null) {
            return value;
        }
        String lowerKey = key.toLowerCase();
        if (lowerKey.contains("password") || 
            lowerKey.contains("secret") || 
            lowerKey.contains("key") || 
            lowerKey.contains("connection") ||
            lowerKey.contains("token")) {
            return "***";
        }
        return value;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    /**
     * Loads the common-domain application-common.yml file as a property source if not already loaded.
     * This ensures that azure.appconfig.* properties from common-domain are available.
     * Spring Boot loads application-common.yml from the service's own resources (identity-auth-service) but not from dependency JARs (common-domain)
     * at the time the EnvironmentPostProcessor runs.
     * Added as lowest priority property source to ensure it's loaded after all other property sources.
     */
    /*private void loadCommonDomainApplicationYml(ConfigurableEnvironment environment) {
        try {
            MutablePropertySources propertySources = environment.getPropertySources();

            // Check if already loaded
            if (propertySources.contains(COMMON_DOMAIN_PROPERTY_SOURCE_NAME)) {
                log.debug("Common-domain application-common.yml already loaded");
                return;
            }

            Resource resource = new ClassPathResource("application-common.yml");
            if (resource.exists()) {
                // Use YamlPropertySourceLoader to properly parse YAML
                org.springframework.boot.env.YamlPropertySourceLoader loader =
                    new org.springframework.boot.env.YamlPropertySourceLoader();

                List<org.springframework.core.env.PropertySource<?>> propertySourcesList =
                    loader.load(COMMON_DOMAIN_PROPERTY_SOURCE_NAME, resource);

                if (!propertySourcesList.isEmpty()) {
                    // Add all property sources from the YAML file
                    for (org.springframework.core.env.PropertySource<?> propertySource : propertySourcesList) {
                        propertySources.addLast(propertySource);
                    }

                    log.debug("Loaded common-domain application-common.yml as property source");
                }
            } else {
                log.debug("Common-domain application-common.yml not found on classpath");
            }
        } catch (Exception e) {
            log.warn("Failed to load common-domain application-common.yml: {}", e.getMessage());
            // Don't fail - this is optional, properties might be in service's application-common.yml
        }
    }*/
}

