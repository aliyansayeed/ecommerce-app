package uniblox.ai.cartservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uniblox.ai.cartservice.model.CartItem;
import uniblox.ai.common.model.dto.ApiResponse;
import uniblox.ai.common.model.value.CheckoutItem;
import uniblox.ai.common.model.dto.CheckoutResponse;
import uniblox.ai.utils.MessageSourceUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CartService {

    private final RestTemplate restTemplate;
    private final Logger logger;
    private final MessageSourceUtils messageSourceUtils;

    // in-memory store userId -> List<CartItem>
    private final Map<String, List<CartItem>> cartDB = new ConcurrentHashMap<>();

    @Value("${checkout-service.url:http://localhost:8080/checkout}")
    private String checkoutServiceUrl;

    /**
     * Add item to the user's cart
     */
    public ApiResponse<?> addItem(String userId, CartItem item) {
        logger.info(messageSourceUtils.getMessage("log.cart.add", userId, item.productId(), item.quantity()));

        CartItem newItem = new CartItem(
                item.productId(),
                item.name(),
                item.quantity(),
                item.price(),
                item.userId(),
                LocalDateTime.now()
        );

        cartDB.computeIfAbsent(userId, k -> new ArrayList<>()).add(newItem);

        return ApiResponse.success(
                messageSourceUtils.getMessage("cart.item.added"),
                Map.of("userId", userId, "items", cartDB.get(userId))
        );
    }

    /**
     * Get all items from a user's cart
     */
    public ApiResponse<?> getCart(String userId) {
        logger.info(messageSourceUtils.getMessage("log.cart.fetch", userId));

        List<CartItem> items = cartDB.getOrDefault(userId, Collections.emptyList());
        return ApiResponse.success(
                messageSourceUtils.getMessage("cart.item.fetch.success"),
                Map.of("userId", userId, "items", items)
        );
    }

    /**
     * Checkout all items in the user's cart
     */
    @Retry(name = "default")
    @CircuitBreaker(name = "default", fallbackMethod = "checkoutFallback")
    public ApiResponse<?> checkout(String userId, String discountCode) {
        logger.info(messageSourceUtils.getMessage("log.cart.checkout.start", userId));

        List<CartItem> items = cartDB.getOrDefault(userId, Collections.emptyList());

        if (items.isEmpty()) {
            return ApiResponse.failure(messageSourceUtils.getMessage("cart.empty"));
        }

        // Transform CartItem -> CheckoutItem
        List<CheckoutItem> checkoutItems = new ArrayList<>();
        for (CartItem item : items) {
            checkoutItems.add(new CheckoutItem(
                    item.productId(),
                    item.name(),
                    item.quantity(),
                    item.price(),
                    userId,
                    LocalDateTime.now()
            ));
        }

        CheckoutResponse response = restTemplate.postForObject(
                checkoutServiceUrl + "/" + userId + "?discountCode=" + discountCode,
                checkoutItems,
                CheckoutResponse.class
        );

        if (response != null && response.order() != null) {
            // Clear cart after successful checkout
            cartDB.remove(userId);

            logger.info(messageSourceUtils.getMessage("log.cart.checkout.success", userId, response.order().orderId()));
            return ApiResponse.success(
                    messageSourceUtils.getMessage("cart.checkout.success", userId),
                    response
            );
        }

        logger.error(messageSourceUtils.getMessage("log.cart.checkout.failed", userId, "empty response"));
        return ApiResponse.failure(messageSourceUtils.getMessage("cart.checkout.failed", userId, "empty response"));
    }

    private ApiResponse<?> checkoutFallback(String userId, String discountCode, Throwable t) {
        logger.error(messageSourceUtils.getMessage("log.cart.checkout.failed", userId, t.getMessage()));
        return ApiResponse.failure(messageSourceUtils.getMessage("cart.checkout.failed", userId, t.getMessage()));
    }
}
