package ch.carve.microprofile.config.consul;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.kv.model.GetValue;

public class ConsulConfigSource implements ConfigSource {

    private static final Logger logger = LoggerFactory.getLogger(ConsulConfigSource.class);
    private static final String DEFAULT_CONSUL_CONFIGSOURCE_ORDINAL = "550";

    Configuration config = new Configuration();
    ExpiringMap<String, String> cache = new ExpiringMap<>(config.getValidity());

    ConsulClient client = new ConsulClient(config.getConsulHost());

    @Override
    public Map<String, String> getProperties() {
        // only query for values if explicitly enabled
        if (config.listAll()) {
            List<GetValue> values = client.getKVValues(config.getPrefix()).getValue();
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
    public String getValue(String propertyName) {
        String value = cache.getOrCompute(propertyName,
                p -> getConsulValue(p),
                p -> logger.debug("consul getKV failed for key {}", p));
        // use default if config_ordinal not found
        if (CONFIG_ORDINAL.equals(propertyName)) {
            return Optional.ofNullable(value).orElse(DEFAULT_CONSUL_CONFIGSOURCE_ORDINAL);
        }
        return value;
    }

    private String getConsulValue(String propertyName) {
        GetValue value = client.getKVValue(config.getPrefix() + propertyName).getValue();
        return value == null ? null : value.getDecodedValue();
    }

    @Override
    public String getName() {
        return "ConsulConfigSource";
    }

}
