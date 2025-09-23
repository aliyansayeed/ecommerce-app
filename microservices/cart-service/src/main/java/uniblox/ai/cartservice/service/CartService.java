package uniblox.ai.cartservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.core.ParameterizedTypeReference;
import uniblox.ai.cartservice.model.CartItem;
import uniblox.ai.common.model.dto.ApiResponse;
import uniblox.ai.common.model.dto.CheckoutResponse;
import uniblox.ai.common.model.value.CheckoutItem;
import uniblox.ai.utils.MessageSourceUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final RestTemplate restTemplate;
    private final MessageSourceUtils messageSourceUtils;

    // in-memory store userId -> List<CartItem>
    private final Map<String, List<CartItem>> cartDB = new ConcurrentHashMap<>();

    /**
     * For local: defaults to http://localhost:8082/api/v1/cart
     * For Docker: overridden via cart-service.yml → http://checkout-service:8080/api/v1/cart
     */
    @Value("${checkout-service.url:http://localhost:8082/api/v1/checkout}")
    private String checkoutServiceUrl;

    public ApiResponse<?> addItem(String userId, CartItem item) {
        log.info(messageSourceUtils.getMessage(
                "log.cart.add",
                new Object[]{userId, item.productId(), item.quantity()}
        ));

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

    public ApiResponse<?> getCart(String userId) {
        log.info(messageSourceUtils.getMessage("log.cart.fetch", new Object[]{userId}));

        List<CartItem> items = cartDB.getOrDefault(userId, Collections.emptyList());
        return ApiResponse.success(
                messageSourceUtils.getMessage("cart.item.fetch.success"),
                Map.of("userId", userId, "items", items)
        );
    }

    @Retry(name = "default")
    @CircuitBreaker(name = "default", fallbackMethod = "checkoutFallback")
    public ApiResponse<?> checkout(String userId, String discountCode) {
        log.info(messageSourceUtils.getMessage("log.cart.checkout.start", new Object[]{userId}));
        log.info(" zubedi 11111111111111111111111");

        List<CartItem> items = cartDB.getOrDefault(userId, Collections.emptyList());
        log.info(" zubedi 2222222222222222222222222222222");

        if (items.isEmpty()) {
            log.info(" zubedi empty ");

            return ApiResponse.failure(messageSourceUtils.getMessage("cart.empty"));
        }
        log.info(" zubedi 333333333333333333333333333");

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
        log.info(" zubedi 4444444444444444444444444");
        //  build URL safely (avoid discountCode=null)
        String url = checkoutServiceUrl + "/" + userId + "/checkout";
        if (discountCode != null) {
            url += "?discountCode=" + discountCode;
        }
        log.info(" zubedi 5555555555555555555555 {}",url);

        // ✅ FIX: expect ApiResponse<CheckoutResponse> instead of CheckoutResponse
        ResponseEntity<ApiResponse<CheckoutResponse>> responseEntity =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        new HttpEntity<>(checkoutItems),
                        new ParameterizedTypeReference<ApiResponse<CheckoutResponse>>() {}
                );

        ApiResponse<CheckoutResponse> apiResponse = responseEntity.getBody();
        CheckoutResponse response = (apiResponse != null) ? apiResponse.data() : null;

        log.info(" zubedi 666666666666666666666 {}",response);

        if (response != null && response.order() != null) {
            log.info(" zubedi inside response block");

            cartDB.remove(userId);

            log.info(messageSourceUtils.getMessage(
                    "log.cart.checkout.success",
                    new Object[]{userId, response.order().orderId()}
            ));

            return ApiResponse.success(
                    messageSourceUtils.getMessage("cart.checkout.success", new Object[]{userId}),
                    response
            );
        }
        log.info(" zubedi 77777777777777777777777777");

        log.error(messageSourceUtils.getMessage(
                "log.cart.checkout.failed",
                new Object[]{userId, "empty response"}
        ));
        log.info(" zubedi 8888888888888888888888888888888888");

        return ApiResponse.failure(
                messageSourceUtils.getMessage(
                        "cart.checkout.failed",
                        new Object[]{userId, "empty response"}
                )
        );
    }


    private ApiResponse<?> checkoutFallback(String userId, String discountCode, Throwable t) {
        log.error("Checkout fallback triggered for userId={} discountCode={} reason={}",
                userId, discountCode, t.getMessage(), t);
        // fixme - messageSourceUtils object not getting instaintiang in resillience // required deep analysis.
        log.info(" zubedi 99999999999999999 fail here ");

        return ApiResponse.failure("This is for testing and need a fix -cart.checkout.failed");
       /* return ApiResponse.failure(
                messageSourceUtils.getMessage(
                        "cart.checkout.failed",
                        new Object[]{userId, t.getMessage()}
                )*/
        // );
    }
}
