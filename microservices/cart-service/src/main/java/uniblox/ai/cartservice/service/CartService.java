package uniblox.ai.cartservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uniblox.ai.cartservice.model.CartItem;
import uniblox.ai.common.model.CheckoutItem;
import uniblox.ai.common.model.CheckoutResponse;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final Map<String, List<CartItem>> cartDB = new HashMap<>();
    private final RestTemplate restTemplate;

    // ✅ Constructor injection (works with Spring + Mockito)
    public CartService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Add item to the user's cart
     */
    public Map<String, Object> addItem(String userId, CartItem item) {
        log.info("Adding item to cart | userId={} | productId={} | quantity={}", userId, item.productId(), item.quantity());

        CartItem newItem = new CartItem(
                item.productId(),
                item.name(),
                item.quantity(),
                item.price(),
                item.userId(),
                LocalDateTime.now()
        );

        cartDB.computeIfAbsent(userId, k -> new ArrayList<>()).add(newItem);

        return Map.of("userId", userId, "items", cartDB.get(userId));
    }

    /**
     * Get all items from a user's cart
     */
    public Map<String, Object> getCart(String userId) {
        List<CartItem> items = cartDB.getOrDefault(userId, Collections.emptyList());
        return Map.of("userId", userId, "items", items);
    }

    /**
     * Checkout all items in the user's cart
     */
    public String checkout(String userId, String discountCode) {
        log.info("Initiating checkout | userId={}", userId);

        List<CartItem> items = cartDB.getOrDefault(userId, Collections.emptyList());

        if (items.isEmpty()) {
            log.warn("Checkout attempted with empty cart for userId={}", userId);
            return "Cart is empty";
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

        try {
            String checkoutServiceUrl = "http://localhost:8080/checkout/" + userId + "?discountCode=" + discountCode;

            CheckoutResponse response = restTemplate.postForObject(
                    checkoutServiceUrl, checkoutItems, CheckoutResponse.class);

            if (response != null && response.order() != null) {
                int cartSize = response.order().items() != null ? response.order().items().size() : 0;
                log.info("Checkout successful | userId={} | cartsize={} | orderId={}",
                        userId, cartSize, response.order().orderId());

                // ✅ Clear cart after successful checkout
                cartDB.remove(userId);

                if (response.newCoupon() != null) {
                    return "Checkout successful for user: " + userId +
                            ". You earned a new coupon: " + response.newCoupon();
                }
                return "Checkout successful for user: " + userId;
            } else {
                log.error("Checkout failed: empty response for userId={}", userId);
                return "Checkout failed: empty response";
            }

        } catch (Exception e) {
            log.error("Checkout failed for userId={} | error={}", userId, e.getMessage(), e);
            return "Checkout failed: " + e.getMessage();
        }
    }
}
