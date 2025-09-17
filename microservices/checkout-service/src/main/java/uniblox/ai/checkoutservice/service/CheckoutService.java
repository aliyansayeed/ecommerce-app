package uniblox.ai.checkoutservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uniblox.ai.common.model.dto.ApiResponse;
import uniblox.ai.common.model.dto.CheckoutResponse;
import uniblox.ai.common.model.entity.Discount;
import uniblox.ai.common.model.entity.Order;
import uniblox.ai.common.model.entity.OrderItem;
import uniblox.ai.common.model.value.CheckoutItem;
import uniblox.ai.common.model.value.OrderStatus;
import uniblox.ai.utils.MessageSourceUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final RestTemplate restTemplate;
    private final Logger log;
    private final MessageSourceUtils messageSourceUtils;

    @Value("${order-service.url}")
    private String orderServiceUrl;

    @Value("${discount-service.url}")
    private String discountServiceUrl;

    @Retry(name = "default")
    @CircuitBreaker(name = "default", fallbackMethod = "checkoutFallback")
    public ApiResponse<CheckoutResponse> checkout(String userId, List<CheckoutItem> items, String discountCode) {
        if (items == null || items.isEmpty()) {
            log.warn("Cart is empty for userId={}", userId);
            return ApiResponse.failure(messageSourceUtils.getMessage("checkout.empty", userId));
        }

        log.info(" inside checkout discountCod {} ", discountCode);

        // 1️⃣ Calculate total amount
        double totalAmount = items.stream()
                .mapToDouble(item -> item.price() * item.quantity())
                .sum();

        double discountAmount = 0.0;

        // 2️⃣ Validate and apply discount if coupon is given
        if (discountCode != null && !discountCode.isBlank()) {
            Boolean valid = restTemplate.getForObject(
                    discountServiceUrl + "/validate/" + userId + "/" + discountCode,
                    Boolean.class
            );
            log.info("zubedi.1111111111111111111..............................................");
            log.info("zubedi.1111111111111111111..............................................");
            log.info("zubedi.1111111111111111111..............................................");

            if (Boolean.TRUE.equals(valid)) {
                log.info("zubedi.inside validate block..............................................");

                discountAmount = totalAmount * 0.10; // apply 10% discount
                log.info("✅ Valid coupon applied | userId={} | code={} | discount={} | final={}",
                        userId, discountCode, discountAmount, (totalAmount - discountAmount));
            } else {
                log.warn("⚠️ Invalid or already used coupon | userId={} | code={}", userId, discountCode);
            }
            log.info("zubedi.2222222222222222..............................................");

        } else {
            log.info("ℹ️ No coupon provided | userId={}", userId);
        }

        log.info("zubedi.setting final amout..............................................");
        log.info("zubedi.discount amout is.." + discountAmount);
        log.info("zubedi.total amout is.." + totalAmount);

        double finalAmount = totalAmount - discountAmount;
        log.info("zubedi.final amout is.." + finalAmount);

        // 3️⃣ Build order request (kept intact, even if not directly used)
        Order orderRequest = new Order(
                null,
                userId,
                toOrderItems(items),
                totalAmount,
                discountCode,
                discountAmount,
                finalAmount,
                OrderStatus.PLACED,
                LocalDateTime.now()
        );

        // 4️⃣ Save order via OrderService (now sending finalAmount too)
        Order createdOrder = restTemplate.postForObject(
                orderServiceUrl + "/" + userId
                        + "?totalAmount=" + totalAmount
                        + "&discountCode=" + (discountCode != null ? discountCode : "")
                        + "&discountAmount=" + discountAmount
                        + "&finalAmount=" + finalAmount,
                toOrderItems(items),
                Order.class
        );

        // 5️⃣ Generate new coupon if nth order
        Discount newCoupon = restTemplate.postForObject(
                discountServiceUrl + "/generate/" + userId,
                null,
                Discount.class
        );

        if (newCoupon != null) {
            log.info("🎁 New coupon generated for userId={} | code={}", userId, newCoupon.code());
        }

        CheckoutResponse response = new CheckoutResponse(createdOrder, newCoupon != null ? newCoupon.code() : null);

        return ApiResponse.success(messageSourceUtils.getMessage("checkout.success"), response);
    }

    private ApiResponse<CheckoutResponse> checkoutFallback(String userId, List<CheckoutItem> items, String discountCode, Throwable t) {
        log.error(messageSourceUtils.getMessage("log.checkout.failed", userId, t.getMessage()));
        return ApiResponse.failure(messageSourceUtils.getMessage("checkout.failed"));
    }

    private List<OrderItem> toOrderItems(List<CheckoutItem> items) {
        return items.stream()
                .map(i -> new OrderItem(
                        i.productId(),
                        i.name(),
                        i.quantity(),
                        i.price(),
                        i.userId(),
                        i.addedAt()
                ))
                .collect(Collectors.toList());
    }
}
