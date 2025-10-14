package dev.code_offline.basalt_server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class BasaltApplication {
    public static final Logger logger = LoggerFactory.getLogger(BasaltApplication.class);

    public static void main(String[] args) {
        startServer(args);
    }
    
    public static ConfigurableApplicationContext startServer(String[] args) {
        return SpringApplication.run(BasaltApplication.class, args);
    }
}
