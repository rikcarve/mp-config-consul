package ch.carve.microprofile.config.consul;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;

class ConsulConfigSourceTest {

    private ConsulConfigSource configSource;

    @BeforeEach
    public void init() {
        configSource = new ConsulConfigSource();
        configSource.config = new Configuration();
        configSource.client = mock(ConsulClient.class);
    }

    @Test
    void testGetProperties_empty() {
        ConsulConfigSource configSource = new ConsulConfigSource();
        assertTrue(configSource.getProperties().isEmpty());
    }

    @Test
    void testGetProperties_one_from_cache() {
        GetValue value = new GetValue();
        value.setValue(Base64.getEncoder().encodeToString("hello".getBytes()));
        when(configSource.client.getKVValue(anyString())).thenReturn(new Response<GetValue>(value, 0L, true, 0L));
        configSource.getValue("test");
        assertEquals(1, configSource.getProperties().size());
    }

    @Test
    void testGetProperties_from_consul() {
        System.setProperty("configsource.consul.list-all", "true");
        configSource.config = new Configuration();
        GetValue value = new GetValue();
        value.setKey("test");
        value.setValue(Base64.getEncoder().encodeToString("hello".getBytes()));
        when(configSource.client.getKVValues(anyString())).thenReturn(new Response<List<GetValue>>(Arrays.asList(value), 0L, true, 0L));
        assertEquals(1, configSource.getProperties().size());
        System.clearProperty("configsource.consul.list-all");
    }

    @Test
    void testGetProperties_with_null() {
        when(configSource.client.getKVValue(anyString())).thenReturn(new Response<GetValue>(null, 0L, true, 0L));
        assertEquals(0, configSource.getProperties().size());
    }

    @Test
    void testGetValue_null() {
        when(configSource.client.getKVValue(anyString())).thenReturn(new Response<GetValue>(null, 0L, true, 0L));
        assertNull(configSource.getValue("test"));
    }

    @Test
    void testGetValue() {
        GetValue value = new GetValue();
        value.setValue(Base64.getEncoder().encodeToString("hello".getBytes()));
        when(configSource.client.getKVValue(anyString())).thenReturn(new Response<GetValue>(value, 0L, true, 0L));
        assertEquals("hello", configSource.getValue("test"));
    }

    @Test
    void testGetValue_cache() {
        GetValue value = new GetValue();
        value.setValue(Base64.getEncoder().encodeToString("hello".getBytes()));
        when(configSource.client.getKVValue(anyString())).thenReturn(new Response<GetValue>(value, 0L, true, 0L));
        configSource.getValue("test");
        configSource.getValue("test");
        verify(configSource.client, times(1)).getKVValue(anyString());
    }

    @Test
    void testGetValue_exception() {
        when(configSource.client.getKVValue(anyString())).thenThrow(RuntimeException.class);
        assertNull(configSource.getValue("test"));
    }

    @Test
    void testOrdinal_default() {
        when(configSource.client.getKVValue(anyString())).thenReturn(new Response<GetValue>(null, 0L, true, 0L));
        assertEquals(550, configSource.getOrdinal());
    }

    @Test
    void testOrdinal_overwrite() {
        GetValue value = new GetValue();
        value.setValue(Base64.getEncoder().encodeToString("200".getBytes()));
        when(configSource.client.getKVValue(anyString())).thenReturn(new Response<GetValue>(value, 0L, true, 0L));
        assertEquals(200, configSource.getOrdinal());
    }

}
