package ch.carve.microprofile.config.consul;

import org.apache.commons.text.StringSubstitutor;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

public class Configuration {

    private Config config = ConfigProviderResolver.instance()
            .getBuilder()
            .addDefaultSources()
            .build();

    private StringSubstitutor substitutor = new StringSubstitutor(s -> getConfigValue(s, ""));
    private String consulHost = substitutor.replace(getConfigValue("configsource.consul.host", "localhost"));
    private long validity = Long.valueOf(getConfigValue("configsource.consul.validity", "30")) * 1000L;
    private String prefix = addSlash(substitutor.replace(getConfigValue("configsource.consul.prefix", "")));
    private boolean listAll = Boolean.valueOf(getConfigValue("configsource.consul.list-all", "false"));

    public long getValidity() {
        return validity;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getConsulHost() {
        return consulHost;
    }

    public boolean listAll() {
        return listAll;
    }

    private String getConfigValue(String key, String defaultValue) {
        return config.getOptionalValue(key, String.class).orElse(defaultValue);
    }

    private String addSlash(String envOrSystemProperty) {
        return envOrSystemProperty.isEmpty() ? "" : envOrSystemProperty + "/";
    }

}
