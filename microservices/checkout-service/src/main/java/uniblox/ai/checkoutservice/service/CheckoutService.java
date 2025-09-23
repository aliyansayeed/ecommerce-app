package uniblox.ai.checkoutservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final RestTemplate restTemplate;
    private final MessageSourceUtils messageSourceUtils;

    @Value("${order-service.url}")
    private String orderServiceUrl;

    @Value("${discount-service.url}")
    private String discountServiceUrl;

    @Retry(name = "default")
    @CircuitBreaker(name = "default", fallbackMethod = "checkoutFallback")
    public ApiResponse<CheckoutResponse> checkout(String userId, List<CheckoutItem> items, String discountCode) {
        log.info("zubedi 111111111111111111111111");
        if (items == null || items.isEmpty()) {
            log.warn("Cart is empty for userId={}", userId);
            return ApiResponse.failure(messageSourceUtils.getMessage("checkout.empty", userId));
        }
        log.info("zubedi 2222222222222222222222222222222");
        // Calculate totals
        double totalAmount = items.stream().mapToDouble(i -> i.price() * i.quantity()).sum();
        double discountAmount = 0.0;
        log.info("zubedi 333333333333333333333333333");

        // ✅ FIX: Validate coupon using ApiResponse<Boolean>
        if (discountCode != null && !discountCode.isBlank()) {
            ApiResponse<Boolean> validationResponse = restTemplate.getForObject(
                    discountServiceUrl + "/validate/" + userId + "/" + discountCode,
                    (Class<ApiResponse<Boolean>>) (Class<?>) ApiResponse.class
            );

            Boolean valid = (validationResponse != null) ? validationResponse.data() : false;

            log.info("zubedi 4444444444444444444444444444444");
            if (Boolean.TRUE.equals(valid)) {
                discountAmount = totalAmount * 0.10;
                log.info("✅ Valid coupon applied | userId={} | code={} | discount={} | final={}",
                        userId, discountCode, discountAmount, (totalAmount - discountAmount));
            } else {
                log.warn("⚠️ Invalid or already used coupon | userId={} | code={}", userId, discountCode);
            }
            log.info("zubedi 55555555555555555555555");
        }

        log.info("zubedi 66666666666666666666666666");
        double finalAmount = totalAmount - discountAmount;
        log.info("zubedi final amount={}", finalAmount);

        log.info("zubedi starting creating order ");
        // Save order
        String url = orderServiceUrl + "/user/" + userId
                + "?totalAmount=" + totalAmount
                + "&discountCode=" + (discountCode != null ? discountCode : "")
                + "&discountAmount=" + discountAmount
                + "&finalAmount=" + finalAmount;

        log.info("zubedi url={}", url);

        log.info(" now going to hit order service");
        ///  fixme ---i m failing...

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<OrderItem>> requestEntity = new HttpEntity<>(toOrderItems(items), headers);

        log.info("zubedi request entity ------------ now going to hit order service");
        // Order createdOrder = restTemplate.postForObject(url, requestEntity, Order.class);

        // ✅ FIX: use exchange with ParameterizedTypeReference to properly deserialize ApiResponse<Order>
        ResponseEntity<ApiResponse<Order>> responseEntity =
                restTemplate.exchange(
                        url,
                        org.springframework.http.HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<ApiResponse<Order>>() {}
                );

        ApiResponse<Order> orderApiResponse = responseEntity.getBody();

        log.info("zubedi api response got for order created successfuly ");

        Order createdOrder = (orderApiResponse != null) ? orderApiResponse.data() : null;
        log.info("zubedi -successfully loaded to order objject ----order created successfuly ");
        log.info("zubedi orderserviceurl {}", orderServiceUrl);

        // Generate new coupon
        log.info("zubedi starting for newCoupon "); // fixme i m giving trouble

        //  http://discount-service:8080/api/v1/discounts/generate/userId

        // ✅ FIX: Generate coupon using ApiResponse<Discount>
        ApiResponse<Discount> apiResponse = restTemplate.postForObject(
                discountServiceUrl + "/generate/" + userId,
                null,
                (Class<ApiResponse<Discount>>) (Class<?>) ApiResponse.class
        );
        log.info(" zubedi --- submitted post request for discount ");
        Discount newCoupon = (apiResponse != null && apiResponse.data() != null)
                ? apiResponse.data()
                : null;

        log.info("zubedi newcoupong pass successfully ");
        CheckoutResponse response = new CheckoutResponse(
                createdOrder, newCoupon != null ? newCoupon.code() : null
        );
        log.info("zubedi return successfull");
        //fixme here also //
        //  return ApiResponse.success(messageSourceUtils.getMessage("checkout.success"), response);
        return ApiResponse.success("successful checout", response);
    }

    private ApiResponse<CheckoutResponse> checkoutFallback(
            String userId, List<CheckoutItem> items, String discountCode, Throwable t) {
        log.info("Fallback for userId={}", userId);
        log.info(" returning back");
        //   log.error(messageSourceUtils.getMessage("log.checkout.failed", userId, t.getMessage()));
        //     return ApiResponse.failure(messageSourceUtils.getMessage("checkout.failed"));
        return ApiResponse.failure(("checkout fail"));
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
