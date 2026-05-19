package cl.apipedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MetricaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetricaServiceApplication.class, args);
    }
}
