package ru.itmo.calls.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "auth.provider.api")
public class ApiAuthConfig {
    private String url;
    private int connectTimeout = 500;
    private int readTimeout = 500;
}
