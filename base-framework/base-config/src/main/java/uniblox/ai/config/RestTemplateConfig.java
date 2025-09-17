package uniblox.ai.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate rt = new RetryTemplate();

        FixedBackOffPolicy backoff = new FixedBackOffPolicy();
        backoff.setBackOffPeriod(500L);
        rt.setBackOffPolicy(backoff);

        SimpleRetryPolicy policy = new SimpleRetryPolicy();
        policy.setMaxAttempts(3);
        rt.setRetryPolicy(policy);

        return rt;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
