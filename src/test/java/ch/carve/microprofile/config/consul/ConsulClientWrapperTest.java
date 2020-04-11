package ch.carve.microprofile.config.consul;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;
import java.util.Map.Entry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;

import com.ecwid.consul.v1.OperationException;

@ExtendWith(MockServerExtension.class)
public class ConsulClientWrapperTest {

    ConsulClientWrapper clientWrapper;

    ClientAndServer clientServer;

    @BeforeEach
    public void init(ClientAndServer client) {
        clientServer = client;
        clientServer.reset();
        clientServer.when(request().withPath("/v1/status/leader")).respond(response().withBody("localhost"));
        clientServer.when(request().withPath("/v1/status/peers")).respond(response().withBody("[\"localhost:8300\"]"));
        clientServer.when(request().withPath("/v1/kv/test")).respond(response().withBody("[{\"LockIndex\":0,\"Key\":\"test\",\"Flags\":0,\"Value\":\"aGVsbG8=\",\"CreateIndex\":1,\"ModifyIndex\":2}]"));
        clientServer.when(request().withPath("/v1/kv/myapp")).respond(response().withBody("[{\"LockIndex\":0,\"Key\":\"test\",\"Flags\":0,\"Value\":\"aGVsbG8=\",\"CreateIndex\":1,\"ModifyIndex\":2}]"));
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
        String value = clientWrapper.getValue("test", null);
        assertEquals("hello", value);
    }

    @Test
    public void testGetValue_not_found() {
        String value = clientWrapper.getValue("nope", null);
        assertNull(value);
    }

    @Test
    public void testGetKeyValuePairs() {
        List<Entry<String, String>> value = clientWrapper.getKeyValuePairs("myapp", null);
        assertEquals(1, value.size());
    }

    @Test
    public void testGetValue_force_reconnect() {
        clientServer.clear(request().withPath("/v1/kv/test"));
        clientServer.when(request().withPath("/v1/kv/test")).respond(response().withStatusCode(503));
        assertThrows(OperationException.class, () -> clientWrapper.getValue("test", null));
        clientServer.clear(request().withPath("/v1/kv/test"));
        clientServer.when(request().withPath("/v1/kv/test")).respond(response().withBody("[{\"LockIndex\":0,\"Key\":\"test\",\"Flags\":0,\"Value\":\"aGVsbG8=\",\"CreateIndex\":1,\"ModifyIndex\":2}]"));        
        String value = clientWrapper.getValue("test", null);
        assertEquals("hello", value);
    }

}
