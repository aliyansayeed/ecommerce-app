package uniblox.ai.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.event.RetryOnErrorEvent;
import io.github.resilience4j.retry.event.RetryOnSuccessEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceEventLogger {

    private static final Logger log = LoggerFactory.getLogger(ResilienceEventLogger.class);

    @PostConstruct
    public void registerRetryEvents() {
        Retry retry = Retry.ofDefaults("default");

        retry.getEventPublisher()
                .onRetry(event -> log.warn("Retry attempt {} for {}", event.getNumberOfRetryAttempts(), event.getName()))
                .onError(event -> log.error("Retry error for {}: {}", event.getName(), event.getLastThrowable().getMessage()))
                .onSuccess(event -> log.info("Retry succeeded for {}", event.getName()));
    }
}
