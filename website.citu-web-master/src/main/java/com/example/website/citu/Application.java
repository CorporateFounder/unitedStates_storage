package com.example.website.citu;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

//	@Bean
//	public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainerCustomizer() {
//		return factory -> {
//			Connector httpConnector = createHttpConnector();
//			factory.addAdditionalTomcatConnectors(httpConnector);
//		};
//	}
//
//	private Connector createHttpConnector() {
//		Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
//		connector.setPort(80); // Настройка порта HTTP
//		return connector;
//	}
}
