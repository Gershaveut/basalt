package dev.code_offline.basalt_server;

import dev.code_offline.basalt_share.Util;
import org.h2.store.fs.FilePath;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Properties;

@EntityScan("dev.code_offline.basalt_share.model")
@SpringBootApplication
public class SpringApplication {
    static void main(String[] args) {
        startServer(args);
    }
    
    public static ConfigurableApplicationContext startServer(String[] args) {
		try {
			CertificateGenerator.generate();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		FilePath.register(new ApplicationFilePathWrapper());
		
		var application = new org.springframework.boot.SpringApplication(SpringApplication.class);
		
		var properties = new Properties();
		properties.put("spring.application.name", Util.APPLICATION_NAME + "-server");
		properties.put("server.ssl.key-store", "file:" + CertificateGenerator.FILE_NAME);
		
		
		application.setDefaultProperties(properties);
		
		return application.run(args);
    }
}
