package ch.carve.microprofile.config.consul;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsulConfigSourceTest {

    private ConsulConfigSource configSource;

    @BeforeEach
    public void init() {
        configSource = new ConsulConfigSource();
        configSource.config = new Configuration();
        configSource.client = mock(ConsulClientWrapper.class);
    }

    @Test
    void testGetProperties_empty() {
        ConsulConfigSource configSource = new ConsulConfigSource();
        assertTrue(configSource.getProperties().isEmpty());
    }

    @Test
    void testGetProperties_one_from_cache() {
        when(configSource.client.getValue(anyString(), isNull())).thenReturn("hello");
        configSource.getValue("test");
        assertEquals(1, configSource.getProperties().size());
    }

    @Test
    void testGetProperties_from_consul() {
        System.setProperty("configsource.consul.list-all", "true");
        configSource.config = new Configuration();
        when(configSource.client.getKeyValuePairs(anyString(), isNull())).thenReturn(Arrays.asList(new SimpleEntry<String, String>("test", "hello")));
        assertEquals(1, configSource.getProperties().size());
        System.clearProperty("configsource.consul.list-all");
    }

    @Test
    void testGetProperties_with_null() {
        when(configSource.client.getValue(anyString(), isNull())).thenReturn(null);
        assertEquals(0, configSource.getProperties().size());
    }

    @Test
    void testGetValue_null() {
        when(configSource.client.getValue(anyString(), isNull())).thenReturn(null);
        assertNull(configSource.getValue("test"));
    }

    @Test
    void testGetValue() {
        when(configSource.client.getValue(anyString(), isNull())).thenReturn("hello");
        assertEquals("hello", configSource.getValue("test"));
    }

    @Test
    void testGetValue_token_configured() {
        System.setProperty("configsource.consul.token", "token");
        configSource.config = new Configuration();
        when(configSource.client.getValue(anyString(), eq("token"))).thenReturn("hello");
        assertEquals("hello", configSource.getValue("test"));
        System.clearProperty("configsource.consul.token");
    }

    @Test
    void testGetValue_cache() {
        when(configSource.client.getValue(anyString(), isNull())).thenReturn("hello");
        configSource.getValue("test");
        configSource.getValue("test");
        verify(configSource.client, times(1)).getValue(anyString(), isNull());
    }

    @Test
    void testGetValue_exception() {
        when(configSource.client.getValue(anyString(), isNull())).thenThrow(RuntimeException.class);
        assertNull(configSource.getValue("test"));
    }

    @Test
    void testOrdinal_default() {
        when(configSource.client.getValue(anyString(), isNull())).thenReturn(null);
        assertEquals(550, configSource.getOrdinal());
    }

    @Test
    void testOrdinal_overwrite() {
        when(configSource.client.getValue(anyString(), isNull())).thenReturn("200");
        assertEquals(200, configSource.getOrdinal());
    }

}
