package uniblox.ai.discountservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uniblox.ai.discountservice.model.Discount;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DiscountService {

    private static final Logger log = LoggerFactory.getLogger(DiscountService.class);

    // store all discount codes (code -> Discount)
    private final Map<String, Discount> discountCodes = new ConcurrentHashMap<>();

    // per-user order counters (userId -> number of orders placed)
    private final Map<String, Integer> orderCounters = new ConcurrentHashMap<>();

    // every Nth order gets a coupon
    private static final int NTH_ORDER = 3;

    /**
     * Generate discount code for a user after their nth order.
     * The coupon is created at checkout time and can be applied on a future order.
     */
    public Optional<Discount> generateDiscountForOrder(String userId) {
        int currentCount = orderCounters.getOrDefault(userId, 0) + 1;
        orderCounters.put(userId, currentCount);

        log.info("📊 User {} has now placed {} orders", userId, currentCount);

        if (currentCount % NTH_ORDER == 0) {
            String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Discount discount = new Discount(code, 10.0, false, LocalDateTime.now());
            discountCodes.put(code, discount);

            log.info("🎉 Generated discount for user {} | code={} | percentage={}%",
                    userId, code, discount.percentage());
            return Optional.of(discount);
        }

        return Optional.empty();
    }

    /**
     * Validate and mark discount code as used.
     */
    public boolean validateDiscountCode(String userId, String code) {
        Discount discount = discountCodes.get(code);

        if (discount != null && !discount.used()) {
            // mark as used
            discountCodes.put(
                    code,
                    new Discount(discount.code(), discount.percentage(), true, discount.createdAt())
            );

            log.info("✅ Discount code={} applied successfully for user={}", code, userId);
            return true;
        }

        log.warn("❌ Invalid or already used discount code={} for user={}", code, userId);
        return false;
    }

    /**
     * For admin reporting: get all discount codes.
     */
    public List<Discount> getAllDiscountCodes() {
        return new ArrayList<>(discountCodes.values());
    }
}
