package ch.carve.microprofile.config.consul;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsulConfigSource implements ConfigSource {

    private static final Logger logger = LoggerFactory.getLogger(ConsulConfigSource.class);
    private static final String DEFAULT_CONSUL_CONFIGSOURCE_ORDINAL = "550";

    Configuration config = new Configuration();
    ExpiringMap<String, String> cache = new ExpiringMap<>(config.getValidity());
    boolean isDisabled = config.getConsulHost().isEmpty() && config.getConsulHostList().isEmpty();
    ConsulClientWrapper client = new ConsulClientWrapper(config.getConsulHost(), config.getConsulHostList(), config.getConsulPort(), config.getToken());

    @Override
    public Map<String, String> getProperties() {
        // only query for values if explicitly enabled
        if (!isDisabled && config.listAll()) {
            List<Entry<String, String>> values = client.getKeyValuePairs(config.getPrefix());
            values.forEach(v -> cache.put(v.getKey(), v.getValue()));
        }
        return cache.getMap().entrySet()
                .stream()
                .filter(e -> e.getValue().get() != null)
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().get()));
    }

    @Override
    public Set<String> getPropertyNames() {
        return getProperties().keySet();
    }

    @Override
    public String getValue(String propertyName) {
        if (isDisabled) {
            return null;
        }
        // quarkus queries with and without profile
        if (propertyName != null && propertyName.startsWith("%")) {
            return null;
        }
        String value = cache.getOrCompute(propertyName,
                p -> client.getValue(config.getPrefix() + propertyName),
                p -> logger.debug("consul getKV failed for key {}", p));
        // use default if config_ordinal not found
        if (CONFIG_ORDINAL.equals(propertyName)) {
            return Optional.ofNullable(value).orElse(DEFAULT_CONSUL_CONFIGSOURCE_ORDINAL);
        }
        return value;
    }

    @Override
    public String getName() {
        return "ConsulConfigSource";
    }

}
