package djl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("djl.config")
public class DjlApplication {
    public static void main(String[] args) {
        SpringApplication.run(DjlApplication.class, args);
    }
}

