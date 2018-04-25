package ch.carve.microprofile.config.consul;

import java.util.Optional;

import org.apache.commons.text.StringSubstitutor;

public class Configuration {
    private StringSubstitutor substitutor = new StringSubstitutor(s -> getEnvOrSystemProperty(s, ""));
    private String consulHost = substitutor.replace(getEnvOrSystemProperty("consul.host", "localhost"));
    private long validity = Long.valueOf(getEnvOrSystemProperty("consul.configsource.validity", "30")) * 1000L;
    private String prefix = addSlash((getEnvOrSystemProperty("consul.prefix", "")));

    public long getValidity() {
        return validity;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getConsulHost() {
        return consulHost;
    }

    private static String getEnvOrSystemProperty(String key, String defaultValue) {
        return Optional.ofNullable(System.getenv(key)).orElse(System.getProperty(key, defaultValue));
    }

    private String addSlash(String envOrSystemProperty) {
        return envOrSystemProperty.isEmpty() ? "" : envOrSystemProperty + "/";
    }

}
