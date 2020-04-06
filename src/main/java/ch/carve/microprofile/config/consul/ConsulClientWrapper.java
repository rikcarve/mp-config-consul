package ch.carve.microprofile.config.consul;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;

public class ConsulClientWrapper {

    private static final Logger logger = LoggerFactory.getLogger(ConsulClientWrapper.class);

    private String host;
    private List<String> peers = null;
    private int port;
    ConsulClient client = null;

    public ConsulClientWrapper(String host, String hosts, int port) {
        this.host = host;
        if (hosts != null && !hosts.isEmpty()) {
            peers = Arrays.asList(hosts.split(","));
        }
        this.port = port;
    }

    public ConsulClient getClient() {
        initConsulClient();
        return client;
    }

    public String getValue(String key) {
        GetValue value = getClient().getKVValue(key).getValue();
        return value == null ? null : value.getDecodedValue();

    }

    public List<Entry<String, String>> getKeyValuePairs(String prefix) {
        List<GetValue> values = getClient().getKVValues(prefix).getValue();
        return values.stream()
                .map(v -> new SimpleEntry<String, String>(v.getKey(), v.getDecodedValue()))
                .collect(Collectors.toList());
    }

    void initConsulClient() {
        if (peers == null) {
            client = new ConsulClient(host, port);
            peers = client.getStatusPeers().getValue().stream()
                    .map(s -> s.split(":")[0])
                    .collect(Collectors.toList());
        }
        if (client == null) {
            client = getClientToAnyConsulHost();
        }
    }

    private ConsulClient getClientToAnyConsulHost() {
        return peers.stream()
                .map(host -> new ConsulClient(host, port))
                .filter(this::isConsulReachable)
                .findAny()
                .orElseThrow(() -> new RuntimeException("No Consul host could be reached."));
    }

    private boolean isConsulReachable(ConsulClient client) {
        try {
            Response<String> leader = client.getStatusLeader();
            logger.info("Successfully established connection to Consul. Current cluster leader is {}", leader.getValue());
        } catch (Exception e) {
            logger.info("Could not establish connection to consul: {}", e.getMessage());
            return false;
        }
        return true;
    }

}
