package ch.carve.microprofile.config.consul;

import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.kv.model.GetValue;

public class ConsulConfigSource implements ConfigSource {

    private static final Logger logger = LoggerFactory.getLogger(ConsulConfigSource.class);

    Configuration config = new Configuration();
    ExpiringMap<String, String> cache = new ExpiringMap<>(config.getValidity());

    ConsulClient client = new ConsulClient(config.getConsulHost());

    @Override
    public Map<String, String> getProperties() {
        return cache.getMap().entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().getValue()));
    }

    @Override
    public String getValue(String propertyName) {
        return cache.getOrCompute(propertyName,
                p -> getConsulValue(p),
                p -> logger.debug("consul getKV failed for key {}", p));
    }

    private String getConsulValue(String propertyName) {
        GetValue value = client.getKVValue(config.getPrefix() + propertyName).getValue();
        if (value != null) {
            return value.getDecodedValue();
        } else {
            return null;
        }
    }

    @Override
    public String getName() {
        return "ConsulConfigSource";
    }

    @Override
    public int getOrdinal() {
        return 550;
    }

}
