[![Build Status](https://travis-ci.org/rikcarve/mp-config-consul.svg?branch=master)](https://travis-ci.org/rikcarve/mp-config-consul)
[![codecov](https://codecov.io/gh/rikcarve/mp-config-consul/branch/master/graph/badge.svg)](https://codecov.io/gh/rikcarve/mp-config-consul)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ch.carve/mp-config-consul/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/ch.carve/mp-config-consul/)

# mp-config-consul
A eclipse microprofile config (1.2) extension which uses [Consul](https://www.consul.io/) as source.

## Overview
The eclipse microprofile config framework is a simple yet powerful configuration framework for Java EE. But most implementations only provide the system/env properties or property files as configuration source. Consuls key/value store is a widely used configuration source, so this small library provides an ConfigSource implementation which takes the values from consul. For performance reasons, the config values are cached.

## Add dependency
```xml
        <dependency>
            <groupId>ch.carve</groupId>
            <artifactId>mp-config-consul</artifactId>
            <version>0.2</version>
        </dependency>
```

## Configuration
Currently there are 2 values you can configure, either through Java system properties or environment variables:
* **consul.host** url of your consul instance, e.g. "192.168.99.100:8500", default value is "localhost"
* **consul.configsource.validity** how long to cache values (in seconds), default is 10s
* **consul.prefix** key prefix to search value in consul


## Links
* https://microprofile.io/project/eclipse/microprofile-config
* https://github.com/rikcarve/consulkv-maven-plugin
