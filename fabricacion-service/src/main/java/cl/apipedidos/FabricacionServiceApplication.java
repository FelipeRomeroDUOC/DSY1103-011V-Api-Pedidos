package cl.apipedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "cl.apipedidos.fabricacion")
public class FabricacionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FabricacionServiceApplication.class, args);
    }
}
