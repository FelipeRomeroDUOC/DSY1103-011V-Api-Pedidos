package cl.apipedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClienteServiceApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ClienteServiceApplication.class);
        application.setHeadless(false);
        application.run(args);
    }
}
