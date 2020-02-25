[![Action Status](https://github.com/rikcarve/mp-config-consul/workflows/ci/badge.svg)](https://github.com/rikcarve/mp-config-consul/actions)
[![codecov](https://codecov.io/gh/rikcarve/mp-config-consul/branch/master/graph/badge.svg)](https://codecov.io/gh/rikcarve/mp-config-consul)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ch.carve/mp-config-consul/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/ch.carve/mp-config-consul/)

# mp-config-consul
A eclipse microprofile config (1.3) extension which uses [Consul](https://www.consul.io/) as source.

> This project has been integrated in https://github.com/microprofile-extensions/config-ext

## Overview
The eclipse microprofile config framework is a simple yet powerful configuration framework for Jakarta EE. But most implementations only provide the system/env properties or property files as configuration source. Consuls key/value store is a widely used configuration source, so this small library provides an ConfigSource implementation which takes the values from consul. For performance reasons, the config values are cached.

## Add dependency
```xml
        <dependency>
            <groupId>ch.carve</groupId>
            <artifactId>mp-config-consul</artifactId>
            <version>0.6</version>
        </dependency>
```

## Configuration
Currently there are 3 values you can configure, either through Java system properties or environment variables:
* **configsource.consul.host** url of your consul instance, e.g. "192.168.99.100:8500", default value is "localhost", variable substitution available.
* **configsource.consul.validity** how long to cache values (in seconds), default is 30s
* **configsource.consul.prefix** key prefix to search value in consul, variable substitution available
Note: these config values cannot be set in Quarkus application.properties!

## Ordinal
Config sources have priorities called ordinal. This config source has ordinal 550, but can be overriden with setting 'config_ordinal' in this source (including prefix if defined)

## Links
* https://microprofile.io/project/eclipse/microprofile-config
* https://github.com/rikcarve/consulkv-maven-plugin
* https://github.com/rikcarve/mp-config-db
