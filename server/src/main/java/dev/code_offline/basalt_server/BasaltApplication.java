package dev.code_offline.basalt_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

@EnableAutoConfiguration(exclude = RepositoryRestMvcAutoConfiguration.class)
@SpringBootApplication
public class BasaltApplication {
    public static final byte NETWORK_VERSION = 1;
    
    public static void main(String[] args) {
        startServer(args);
    }
    
    public static ConfigurableApplicationContext startServer(String[] args) {
        return SpringApplication.run(BasaltApplication.class, args);
    }
}
