package uniblox.ai.discountservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import uniblox.ai.common.model.entity.Discount;
import uniblox.ai.utils.MessageSourceUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountService {

  //  private final Logger logger;
 //   private final MessageSourceUtils messageSourceUtils;

    // store all discount codes (code -> Discount)
    private final Map<String, Discount> discountCodes = new ConcurrentHashMap<>();

    // per-user order counters (userId -> number of orders placed)
    private final Map<String, Integer> orderCounters = new ConcurrentHashMap<>();

    // every Nth order gets a coupon (can move to config later)
    private static final int NTH_ORDER = 3;

    /**
     * Generate discount code for a user after their nth order.
     */
    @Retry(name = "default")
    @CircuitBreaker(name = "default", fallbackMethod = "generateDiscountFallback")
    public Optional<Discount> generateDiscountForOrder(String userId) {
        int currentCount = orderCounters.getOrDefault(userId, 0) + 1;
        orderCounters.put(userId, currentCount);

        log.info("Disoount order Current count is {} {}", userId, currentCount);
      //  logger.info(messageSourceUtils.getMessage("log.discountsvc.orders.count", userId, currentCount));

        if (currentCount % NTH_ORDER == 0) {
            String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Discount discount = new Discount(code, 10.0, false, LocalDateTime.now());
            discountCodes.put(code, discount);
            log.info("Discount code is {} {}", code, discount);
        //    logger.info(messageSourceUtils.getMessage("log.discountsvc.generated", userId, code, discount.percentage()));
            return Optional.of(discount);
        }
        log.info("Discount returnig empty as currentCount vs NTH_ORDER is {} {}", currentCount, NTH_ORDER);
        return Optional.empty();
    }

    private Optional<Discount> generateDiscountFallback(String userId, Throwable t) {
     //   logger.error(messageSourceUtils.getMessage("log.discountsvc.unavailable", userId, t.getMessage()));
        return Optional.empty();
    }

    /**
     * Validate and mark discount code as used.
     */
    @Retry(name = "default")
    @CircuitBreaker(name = "default", fallbackMethod = "validateDiscountFallback")
    public boolean validateDiscountCode(String userId, String code) {
        Discount discount = discountCodes.get(code);

        if (discount != null && !discount.used()) {
            discountCodes.put(
                    code,
                    new Discount(discount.code(), discount.percentage(), true, discount.createdAt())
            );
        //    logger.info(messageSourceUtils.getMessage("log.discountsvc.valid", code, userId));
            return true;
        }

      //  logger.warn(messageSourceUtils.getMessage("log.discountsvc.invalid", code, userId));
        return false;
    }

    private boolean validateDiscountFallback(String userId, String code, Throwable t) {
      //  logger.error(messageSourceUtils.getMessage("log.discountsvc.validation.failed", code, userId, t.getMessage()));
        return false;
    }

    /**
     * For admin reporting: get all discount codes.
     */
    @Retry(name = "default")
    @CircuitBreaker(name = "default", fallbackMethod = "allDiscountsFallback")
    public List<Discount> getAllDiscountCodes() {
        return new ArrayList<>(discountCodes.values());
    }

    private List<Discount> allDiscountsFallback(Throwable t) {
   //     logger.error(messageSourceUtils.getMessage("log.discountsvc.fetch.failed", t.getMessage()));
        return List.of();
    }
}