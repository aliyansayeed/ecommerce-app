package uniblox.ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
    public class CoreConfig {

        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper;
        }

        @Bean
        public RestTemplateBuilder restTemplateBuilder(
                @Value("${http.connect-timeout-ms:2000}") long connectTimeoutMs,
                @Value("${http.read-timeout-ms:5000}") long readTimeoutMs) {
            return new RestTemplateBuilder()
                    .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                    .setReadTimeout(Duration.ofMillis(readTimeoutMs));
        }
    }


