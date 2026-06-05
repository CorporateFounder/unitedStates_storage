package com.example.website.citu.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebsiteConnectorConfig {
    private final boolean sslEnabled;
    private final boolean additionalHttpEnabled;
    private final int serverPort;
    private final int additionalHttpPort;

    public WebsiteConnectorConfig(
            @Value("${server.ssl.enabled:false}") boolean sslEnabled,
            @Value("${citu.http.additional.enabled:false}") boolean additionalHttpEnabled,
            @Value("${server.port:8080}") int serverPort,
            @Value("${citu.http.additional.port:80}") int additionalHttpPort) {
        this.sslEnabled = sslEnabled;
        this.additionalHttpEnabled = additionalHttpEnabled;
        this.serverPort = serverPort;
        this.additionalHttpPort = additionalHttpPort;
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> cituAdditionalHttpConnectorCustomizer() {
        return factory -> {
            if (shouldAddAdditionalHttpConnector()) {
                factory.addAdditionalTomcatConnectors(createAdditionalHttpConnector());
            }
        };
    }

    boolean shouldAddAdditionalHttpConnector() {
        return sslEnabled
                && additionalHttpEnabled
                && additionalHttpPort > 0
                && additionalHttpPort != serverPort;
    }

    Connector createAdditionalHttpConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setSecure(false);
        connector.setPort(additionalHttpPort);
        return connector;
    }
}
