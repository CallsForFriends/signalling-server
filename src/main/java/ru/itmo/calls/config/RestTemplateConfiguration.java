package ru.itmo.calls.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestTemplate defaultRestTemplate(
            RestTemplateBuilder builder,
            ApiAuthConfig config
    ) {
        return builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(config.getUrl()))
                .connectTimeout(Duration.ofMillis(config.getConnectTimeout()))
                .readTimeout(Duration.ofMillis(config.getReadTimeout()))
                .build();
    }
}
