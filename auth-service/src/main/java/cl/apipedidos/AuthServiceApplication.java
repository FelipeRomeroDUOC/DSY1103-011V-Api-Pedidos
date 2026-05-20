package cl.apipedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(AuthServiceApplication.class);
        application.setHeadless(false);
        application.run(args);
    }
}
