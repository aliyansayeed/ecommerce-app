package uniblox.ai.adminservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import uniblox.ai.adminservice.model.AdminReportResponse;
import uniblox.ai.common.api.path.AdminApiPaths;
import uniblox.ai.common.model.dto.AdminOrdersResponse;
import uniblox.ai.common.model.dto.ApiResponse;
import uniblox.ai.common.model.entity.Discount;
import uniblox.ai.common.model.entity.Order;
import uniblox.ai.common.model.entity.OrderItem;
import uniblox.ai.utils.MessageSourceUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Admin operations: generate discounts and build reports.
 * Uses:
 *  - API paths from AdminApiPaths
 *  - User-facing + log messages from messages.properties
 *  - Resilience4j for retries/circuit breakers
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final RestTemplate restTemplate;
    private final WebClient.Builder webClientBuilder;
    private final Logger logger;
    private final MessageSourceUtils messageSourceUtils;

    // Defaults kick in if not provided in application.yml / config-server
    @Value("${order-service.url:http://localhost:8084/order/all-orders}")
    private String orderServiceUrl;

    @Value("${discount-service.url:http://localhost:8083/discount}")
    private String discountServiceUrl;

    /**
     * Generate discount for a user (admin-triggered).
     */
    @Retry(name = "default")
    @CircuitBreaker(name = "default", fallbackMethod = "generateDiscountFallback")
    public ApiResponse<?> generateDiscount(String userId) {
        logger.info(messageSourceUtils.getMessage("log.discount.request", userId));

        Discount discount = restTemplate.postForObject(
                discountServiceUrl + AdminApiPaths.DISCOUNT_GENERATE_PATH + userId,
                null,
                Discount.class
        );

        if (discount == null) {
            logger.warn(messageSourceUtils.getMessage("log.discount.none", userId));
            return ApiResponse.failure(messageSourceUtils.getMessage("discount.not.eligible"));
        }

        logger.info(messageSourceUtils.getMessage("log.discount.success", discount.code()));
        return ApiResponse.success(messageSourceUtils.getMessage("discount.success"), discount);
    }

    private ApiResponse<?> generateDiscountFallback(String userId, Throwable t) {
        logger.error(messageSourceUtils.getMessage("log.discount.unavailable", userId, t.getMessage()));
        return ApiResponse.failure(messageSourceUtils.getMessage("discount.unavailable"));
    }

    /**
     * Admin report aggregating from OrderService + DiscountService.
     */
    @Retry(name = "default")
    @CircuitBreaker(name = "default", fallbackMethod = "getReportFallback")
    public ApiResponse<AdminReportResponse> getReport() {
        logger.info(messageSourceUtils.getMessage("log.orders.fetch"));

        // --- fetch all orders (RestTemplate) ---
        AdminOrdersResponse[] ordersResponse = restTemplate.getForObject(
                orderServiceUrl, AdminOrdersResponse[].class);

        List<AdminOrdersResponse> orders = ordersResponse != null
                ? Arrays.asList(ordersResponse)
                : List.of();

        long totalOrders = orders.size();
        long totalItems = 0;
        double totalPurchaseAmount = 0;
        double totalDiscountAmount = 0;

        for (AdminOrdersResponse resp : orders) {
            Order order = resp.getOrders();
            if (order != null) {
                for (OrderItem item : order.items()) {
                    totalItems += item.quantity();
                }
                totalPurchaseAmount += order.totalAmount();
                totalDiscountAmount += order.discountAmount();
            }
        }

        logger.info(messageSourceUtils.getMessage("log.discounts.fetch"));

        // --- fetch all discounts (WebClient) ---
        List<Discount> discounts = webClientBuilder.build()
                .get()
                .uri(discountServiceUrl + AdminApiPaths.DISCOUNT_ALL_PATH)
                .retrieve()
                .bodyToFlux(Discount.class)
                .collectList()
                .block();

        if (discounts == null) {
            discounts = List.of();
        }

        AdminReportResponse report = new AdminReportResponse(
                totalOrders,
                totalItems,
                totalPurchaseAmount,
                totalDiscountAmount,
                discounts
        );

        logger.info(messageSourceUtils.getMessage("log.report.success"));
        return ApiResponse.success(messageSourceUtils.getMessage("report.success"), report);
    }

    private ApiResponse<AdminReportResponse> getReportFallback(Throwable t) {
        logger.error(messageSourceUtils.getMessage("log.report.failed", t.getMessage()));
        return ApiResponse.failure(messageSourceUtils.getMessage("report.unavailable"));
    }
}
