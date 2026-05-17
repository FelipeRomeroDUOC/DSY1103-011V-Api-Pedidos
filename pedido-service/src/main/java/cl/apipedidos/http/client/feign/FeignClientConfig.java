package cl.apipedidos.http.client.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Bean
    public ErrorDecoder feignErrorDecoder(ObjectMapper objectMapper) {
        return new FeignErrorDecoder(objectMapper);
    }

    @Bean
    public RequestInterceptor defaultHeadersInterceptor() {
        return template -> {
            template.header("Accept", "application/json");
            if (!template.headers().containsKey("Content-Type")) {
                template.header("Content-Type", "application/json");
            }
        };
    }
}
