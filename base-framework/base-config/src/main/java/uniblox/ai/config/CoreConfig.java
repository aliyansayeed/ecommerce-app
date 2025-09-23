package uniblox.ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

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

    // place RestTemplateBuild inside restTemplate- as it wasn't able override//

    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder builder,
            @Value("${http.connect-timeout-ms:2000}") long connectTimeoutMs,
            @Value("${http.read-timeout-ms:5000}") long readTimeoutMs) {

        return builder
                .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                .setReadTimeout(Duration.ofMillis(readTimeoutMs))
                .build();
    }

    @Bean
    public RetryTemplate retryTemplate(
            @Value("${http.retry.max-attempts:3}") int maxAttempts,
            @Value("${http.retry.backoff-ms:500}") long backoffMs) {

        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy backoff = new FixedBackOffPolicy();
        backoff.setBackOffPeriod(backoffMs);
        retryTemplate.setBackOffPolicy(backoff);

        SimpleRetryPolicy policy = new SimpleRetryPolicy();
        policy.setMaxAttempts(maxAttempts);
        retryTemplate.setRetryPolicy(policy);

        return retryTemplate;
    }
}
