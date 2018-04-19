package ch.carve.microprofile.config.consul;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.kv.model.GetValue;

public class ConsulConfigSource implements ConfigSource {

    private static final Logger logger = LoggerFactory.getLogger(ConsulConfigSource.class);

    private Map<String, TimedEntry> cache = new ConcurrentHashMap<>();
    private ConsulClient client = new ConsulClient(getEnvOrSystemProperty("consul.host", "localhost"));
    private long validity = Long.valueOf(getEnvOrSystemProperty("consul.configsource.validity", "30")) * 1000L;
    private String prefix = addSlash(getEnvOrSystemProperty("consul.prefix", ""));

    @Override
    public Map<String, String> getProperties() {
        return cache.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().getValue()));
    }

    @Override
    public String getValue(String propertyName) {
        TimedEntry entry = cache.get(propertyName);
        if (entry == null || entry.isExpired()) {
            logger.debug("load {} from consul", propertyName);
            GetValue value = null;
            try {
                value = client.getKVValue(prefix + propertyName).getValue();
            } catch (Exception e) {
                logger.warn("consul getKVValue() failed", e);
            }
            if (value == null) {
                cache.put(propertyName, new TimedEntry(null));
                return null;
            }
            String decodedValue = value.getDecodedValue();
            cache.put(propertyName, new TimedEntry(decodedValue));
            return decodedValue;
        }
        return entry.getValue();
    }

    @Override
    public String getName() {
        return "ConsulConfigSource";
    }

    @Override
    public int getOrdinal() {
        return 120;
    }

    private static String getEnvOrSystemProperty(String key, String defaultValue) {
        return Optional.ofNullable(System.getenv(key)).orElse(System.getProperty(key, defaultValue));
    }

    private String addSlash(String envOrSystemProperty) {
        return envOrSystemProperty.isEmpty() ? "" : envOrSystemProperty + "/";
    }

    class TimedEntry {
        private final String value;
        private final long timestamp;

        public TimedEntry(String value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        public String getValue() {
            return value;
        }

        public boolean isExpired() {
            return (timestamp + validity) < System.currentTimeMillis();
        }
    }
}
