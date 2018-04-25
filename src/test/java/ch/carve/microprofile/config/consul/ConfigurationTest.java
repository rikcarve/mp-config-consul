package ch.carve.microprofile.config.consul;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ConfigurationTest {

    @Test
    public void testGetValidity() throws Exception {
        Configuration config = new Configuration();
        assertEquals(30000, config.getValidity());
    }

    @Test
    public void testGetValidity_fromSys() throws Exception {
        System.setProperty("consul.configsource.validity", "10");
        Configuration config = new Configuration();
        assertEquals(10000, config.getValidity());
        System.clearProperty("consul.configsource.validity");
    }

    @Test
    public void testGetPrefix() throws Exception {
        Configuration config = new Configuration();
        assertEquals("", config.getPrefix());
    }

    @Test
    public void testGetPrefix_withSlash() throws Exception {
        System.setProperty("consul.prefix", "jax");
        Configuration config = new Configuration();
        assertEquals("jax/", config.getPrefix());
        System.clearProperty("consul.prefix");
    }

    @Test
    public void testGetPrefix_withSubstitution() throws Exception {
        System.setProperty("consul.prefix", "applications/${appname}");
        System.setProperty("appname", "jax");
        Configuration config = new Configuration();
        assertEquals("applications/jax/", config.getPrefix());
        System.clearProperty("consul.prefix");
        System.clearProperty("appName");
    }

    @Test
    public void testGetConsulHost() throws Exception {
        Configuration config = new Configuration();
        assertEquals("localhost", config.getConsulHost());
    }

    @Test
    public void testGetConsulHost_fromSys() throws Exception {
        System.setProperty("consul.host", "jax");
        Configuration config = new Configuration();
        assertEquals("jax", config.getConsulHost());
        System.clearProperty("consul.host");
    }

    @Test
    public void testGetConsulHost_fromSys_withSubstitution() throws Exception {
        System.setProperty("consul.host", "${docker.host}");
        System.setProperty("docker.host", "sub");
        Configuration config = new Configuration();
        assertEquals("sub", config.getConsulHost());
        System.clearProperty("consul.host");
        System.clearProperty("docker.host");
    }

}
