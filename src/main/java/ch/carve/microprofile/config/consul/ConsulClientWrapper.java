package ch.carve.microprofile.config.consul;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Supplier;
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
    private String token;
    ConsulClient client = null;

    public ConsulClientWrapper(String host, String hosts, int port, String token) {
        this.host = host;
        if (hosts != null && !hosts.isEmpty()) {
            peers = new ArrayList<>(Arrays.asList(hosts.split(",")));
            // add host (usually local agent) to the front for prioritization
            if (host != null && !host.isEmpty()) {
                peers.add(0, host);
            }
        }
        this.port = port;
        this.token = token;
    }

    public ConsulClient getClient() {
        initConsulClient();
        return client;
    }

    public String getValue(String key) {
        try {
            String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8.name());
            GetValue value = retry(2, () -> getClient().getKVValue(encodedKey, token).getValue(), () -> forceReconnect());
            return value == null ? null : value.getDecodedValue();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            forceReconnect();
            throw e;
        }
    }

    public List<Entry<String, String>> getKeyValuePairs(String prefix) {
        try {
            String encodedPrefix = URLEncoder.encode(prefix, StandardCharsets.UTF_8.name());
            List<GetValue> values = retry(2, () -> getClient().getKVValues(encodedPrefix, token).getValue(), () -> forceReconnect());
            return values.stream()
                    .map(v -> new SimpleEntry<String, String>(v.getKey(), v.getDecodedValue()))
                    .collect(Collectors.toList());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            forceReconnect();
            throw e;
        }
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

    private <T> T retry(int maxRetries, Supplier<T> supplier, Runnable onFailedAttempt) {
        int retries = 0;
        RuntimeException lastException = null;
        while (retries <= maxRetries) {
            try {
                return supplier.get();
            } catch (RuntimeException e) {
                lastException = e;
                onFailedAttempt.run();
                retries++;
            }
        }
        throw lastException;
    }

    private void forceReconnect() {
        client = null;
    }

}
