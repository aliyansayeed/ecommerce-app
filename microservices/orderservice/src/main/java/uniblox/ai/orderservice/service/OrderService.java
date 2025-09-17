package uniblox.ai.orderservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import uniblox.ai.common.model.dto.AdminOrdersResponse;
import uniblox.ai.common.model.dto.ApiResponse;
import uniblox.ai.common.model.entity.Order;
import uniblox.ai.common.model.entity.OrderItem;
import uniblox.ai.common.model.value.OrderStatus;
import uniblox.ai.utils.MessageSourceUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core Order service with resilience, externalized messages, and ApiResponse wrapper.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final Logger logger;
    private final MessageSourceUtils messageSourceUtils;

    // in-memory store orderId -> Order
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    // --- Create Order ---
    @Retry(name = "default")
    @CircuitBreaker(name = "default", fallbackMethod = "createOrderFallback")
    public ApiResponse<Order> createOrder(String userId, List<OrderItem> items,
                                          double totalAmount, String discountCode, double discountAmount) {

        logger.info(messageSourceUtils.getMessage("log.order.create", userId));

        String orderId = UUID.randomUUID().toString();
        double finalAmount = totalAmount - discountAmount;

        Order order = new Order(
                orderId,
                userId,
                List.copyOf(items),
                totalAmount,
                discountCode,
                discountAmount,
                finalAmount,
                OrderStatus.PLACED,
                LocalDateTime.now()
        );

        orders.put(orderId, order);

        logger.info(messageSourceUtils.getMessage("log.order.success", orderId));
        return ApiResponse.success(messageSourceUtils.getMessage("order.create.success"), order);
    }

    private ApiResponse<Order> createOrderFallback(String userId, List<OrderItem> items,
                                                   double totalAmount, String discountCode, double discountAmount, Throwable t) {
        logger.error(messageSourceUtils.getMessage("log.order.failed", t.getMessage()));
        return ApiResponse.failure(messageSourceUtils.getMessage("order.unavailable"));
    }

    // --- Fetch all (for Admin) ---
    public ApiResponse<List<AdminOrdersResponse>> getAllOrders() {
        logger.info(messageSourceUtils.getMessage("log.order.fetch.all"));

        List<AdminOrdersResponse> list = new ArrayList<>();
        for (Order o : orders.values()) {
            AdminOrdersResponse resp = new AdminOrdersResponse();
            resp.setOrderId(o.orderId());
            resp.setOrders(o);
            list.add(resp);
        }

        return ApiResponse.success(messageSourceUtils.getMessage("order.fetch.success"), list);
    }

    // --- Fetch by User ---
    public ApiResponse<List<Order>> getOrdersByUser(String userId) {
        logger.info(messageSourceUtils.getMessage("log.order.fetch.byuser", userId));

        List<Order> list = orders.values().stream()
                .filter(o -> Objects.equals(o.userId(), userId))
                .sorted(Comparator.comparing(Order::createdAt).reversed())
                .toList();

        if (list.isEmpty()) {
            return ApiResponse.failure(messageSourceUtils.getMessage("order.user.notfound", userId));
        }

        return ApiResponse.success(messageSourceUtils.getMessage("order.fetch.byuser.success", userId), list);
    }

    // --- Fetch by ID ---
    public ApiResponse<Order> getOrderById(String orderId) {
        logger.info(messageSourceUtils.getMessage("log.order.fetch.byid", orderId));

        Order order = orders.get(orderId);
        if (order == null) {
            return ApiResponse.failure(messageSourceUtils.getMessage("order.notfound", orderId));
        }

        return ApiResponse.success(messageSourceUtils.getMessage("order.fetch.byid.success", orderId), order);
    }

    // --- Update order status ---
    public ApiResponse<Order> updateOrderStatus(String orderId, OrderStatus status) {
        Order old = orders.get(orderId);
        if (old == null) {
            logger.warn(messageSourceUtils.getMessage("log.order.notfound.update", orderId));
            return ApiResponse.failure(messageSourceUtils.getMessage("order.notfound", orderId));
        }

        Order updated = new Order(
                old.orderId(),
                old.userId(),
                old.items(),
                old.totalAmount(),
                old.discountCode(),
                old.discountAmount(),
                old.finalAmount(),
                status,
                old.createdAt()
        );

        orders.put(orderId, updated);
        logger.info(messageSourceUtils.getMessage("log.order.update", orderId, status));
        return ApiResponse.success(messageSourceUtils.getMessage("order.update.success", orderId), updated);
    }
}
