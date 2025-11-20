package dev.code_offline.basalt_server;

import dev.code_offline.basalt_share.Util;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Properties;

@EnableAutoConfiguration(exclude = RepositoryRestMvcAutoConfiguration.class)
@EntityScan("dev.code_offline.basalt_share.model")
@SpringBootApplication
public class SpringApplication {
    public static void main(String[] args) {
        startServer(args);
    }
    
    public static ConfigurableApplicationContext startServer(String[] args) {
		try {
			CertificateGenerator.generate();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		var application = new org.springframework.boot.SpringApplication(SpringApplication.class);
		
		var properties = new Properties();
		properties.put("spring.application.name", Util.APPLICATION_NAME + "-server");
		properties.put("server.ssl.key-store", "file:" + CertificateGenerator.FILE_NAME);
		
		
		application.setDefaultProperties(properties);
		
		return application.run(args);
    }
}
