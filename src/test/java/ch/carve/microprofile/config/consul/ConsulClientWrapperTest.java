package ch.carve.microprofile.config.consul;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map.Entry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

@ExtendWith(MockServerExtension.class)
public class ConsulClientWrapperTest {

    ConsulClientWrapper clientWrapper;

    ClientAndServer clientServer;

    @BeforeEach
    public void init(ClientAndServer client) {
        clientServer = client;
        clientServer.when(HttpRequest.request().withPath("/v1/status/leader")).respond(HttpResponse.response().withBody("localhost"));
        clientServer.when(HttpRequest.request().withPath("/v1/status/peers")).respond(HttpResponse.response().withBody("[\"localhost:8300\"]"));
        clientServer.when(HttpRequest.request().withPath("/v1/kv/test")).respond(HttpResponse.response().withBody("[{\"LockIndex\":0,\"Key\":\"test\",\"Flags\":0,\"Value\":\"aGVsbG8=\",\"CreateIndex\":1,\"ModifyIndex\":2}]"));
        clientServer.when(HttpRequest.request().withPath("/v1/kv/myapp")).respond(HttpResponse.response().withBody("[{\"LockIndex\":0,\"Key\":\"test\",\"Flags\":0,\"Value\":\"aGVsbG8=\",\"CreateIndex\":1,\"ModifyIndex\":2}]"));
        clientWrapper = new ConsulClientWrapper("localhost", null, clientServer.getLocalPort());
    }

    @Test
    public void testGetClient_singleHost() {
        assertNotNull(clientWrapper.getClient());
    }

    @Test
    public void testGetClient_peers() {
        clientWrapper = new ConsulClientWrapper(null, "localhost", clientServer.getLocalPort());
        assertNotNull(clientWrapper.getClient());
    }

    @Test
    public void testGetClient_hosts_1stnotAvail() {
        clientWrapper = new ConsulClientWrapper(null, "localhost2,localhost", clientServer.getLocalPort());
        assertNotNull(clientWrapper.getClient());
    }

    @Test
    public void testGetValue_found() {
        String value = clientWrapper.getValue("test");
        assertEquals("hello", value);
    }

    @Test
    public void testGetValue_not_found() {
        String value = clientWrapper.getValue("nope");
        assertNull(value);
    }

    @Test
    public void testGetKeyValuePairs() {
        List<Entry<String, String>> value = clientWrapper.getKeyValuePairs("myapp");
        assertEquals(1, value.size());
    }

}
