package com.example.config;

import org.aeonbits.owner.Config;

@Config.Sources({"classpath:config-${env}.properties"})
public interface EnvironmentConfig extends Config {

    @Key("baseUrl")
    String baseUrl();

    @Key("browser")
    @DefaultValue("chromium")
    String browser();

    @Key("headless")
    @DefaultValue("false")
    Boolean headless();

    @Key("timeout")
    @DefaultValue("30000")
    Integer timeout();
}