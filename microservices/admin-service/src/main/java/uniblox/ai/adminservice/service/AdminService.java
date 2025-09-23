package uniblox.ai.adminservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final RestTemplate restTemplate;
    private final WebClient.Builder webClientBuilder;
    private final MessageSourceUtils messageSourceUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${order-service.url:http://localhost:8084/orders/all}")
    private String orderServiceUrl;

    @Value("${discount-service.url:http://localhost:8083/discounts}")
    private String discountServiceUrl;

    @Retry(name = "default")
    @CircuitBreaker(name = "default", fallbackMethod = "generateDiscountFallback")
    public ApiResponse<?> generateDiscount(String userId) {
      //  log.info(messageSourceUtils.getMessage("log.discount.request", new Object[]{userId}));

        Discount discount = restTemplate.postForObject(
                discountServiceUrl + AdminApiPaths.DISCOUNT_GENERATE_PATH + userId,
                null,
                Discount.class
        );

        if (discount == null) {
          //  log.warn(messageSourceUtils.getMessage("log.discount.none", new Object[]{userId}));
            return ApiResponse.failure(messageSourceUtils.getMessage("discount.not.eligible"));
        }

     //   log.info(messageSourceUtils.getMessage("log.discount.success", new Object[]{discount.code()}));
        return ApiResponse.success(messageSourceUtils.getMessage("discount.success"), discount);
    }

    private ApiResponse<?> generateDiscountFallback(String userId, Throwable t) {
       // log.error(messageSourceUtils.getMessage("log.discount.unavailable", new Object[]{userId, t.getMessage()}));
        return ApiResponse.failure(messageSourceUtils.getMessage("discount.unavailable"));
    }

    @Retry(name = "default")
    @CircuitBreaker(name = "default", fallbackMethod = "getReportFallback")
    @Cacheable(value = "adminReports", key = "'latestReport'")
    public ApiResponse<AdminReportResponse> getReport() {
       // log.info(messageSourceUtils.getMessage("log.orders.fetch"));

        AdminOrdersResponse[] ordersResponse = restTemplate.getForObject(orderServiceUrl, AdminOrdersResponse[].class);
        List<AdminOrdersResponse> orders = ordersResponse != null ? Arrays.asList(ordersResponse) : List.of();

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

      //  log.info(messageSourceUtils.getMessage("log.discounts.fetch"));

        List<Discount> discounts = webClientBuilder.build()
                .get()
                .uri(discountServiceUrl + AdminApiPaths.DISCOUNT_ALL_PATH)
                .retrieve()
                .bodyToFlux(Discount.class)
                .collectList()
                .block();

        if (discounts == null) discounts = List.of();

        AdminReportResponse report = new AdminReportResponse(
                totalOrders,
                totalItems,
                totalPurchaseAmount,
                totalDiscountAmount,
                discounts
        );

      //  log.info(messageSourceUtils.getMessage("log.report.success"));
        return ApiResponse.success("successful",report);//messageSourceUtils.getMessage("report.success"), report);
    }

    private ApiResponse<AdminReportResponse> getReportFallback(Throwable t) {
     //   log.error(messageSourceUtils.getMessage("log.report.failed", new Object[]{t.getMessage()}));
        return ApiResponse.failure("unavailable orders");//messageSourceUtils.getMessage("report.unavailable"));
    }

    @CacheEvict(value = "adminReports", key = "'latestReport'")
    public void evictReportCache() {
        log.info("🧹 Admin report cache cleared");
    }
}
