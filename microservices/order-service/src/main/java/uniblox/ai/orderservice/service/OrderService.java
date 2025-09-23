package uniblox.ai.orderservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final MessageSourceUtils messageSourceUtils;
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    @Retry(name = "default")
    @CircuitBreaker(name = "default", fallbackMethod = "createOrderFallback")
    public ApiResponse<Order> createOrder(String userId, List<OrderItem> items,
                                          double totalAmount, String discountCode,
                                          double discountAmount, double finalAmount) {  // ✅ finalAmount added

        log.info("zubedi inside ordercreation...");
        log.info(messageSourceUtils.getMessage("log.order.create", userId));
        log.info("zubedi order creation block 11111111111111111111");
        String orderId = UUID.randomUUID().toString();
        log.info("zubedi order creation block 2222222222222222");

        Order order = new Order(
                orderId,
                userId,
                List.copyOf(items),
                totalAmount,
                discountCode,
                discountAmount,
                finalAmount,   // ✅ use passed finalAmount
                OrderStatus.PLACED,
                LocalDateTime.now()
        );
        log.info("zubedi order creation block 333333333333333333333333");

        orders.put(orderId, order);
        log.info("zubedi order creation block 44444444444444444444444");

      //  log.info(messageSourceUtils.getMessage("log.order.success", orderId));
        log.info("order successfully created {}", orderId);
        log.info("zubedi order creation block 555555555555555555555555555555");

      //  return ApiResponse.success(messageSourceUtils.getMessage("order.create.success"), order);
        //fixme v-  issue with messageSourceUtils - will handle this fix later
        return ApiResponse.success(("order.create.success"), order);
    }

    private ApiResponse<Order> createOrderFallback(String userId, List<OrderItem> items,
                                                   double totalAmount, String discountCode,
                                                   double discountAmount, double finalAmount, Throwable t) {
        log.info("zubedi order creation block createOrderFallback");

        //.error(messageSourceUtils.getMessage("log.order.failed", t.getMessage()));
      //  return ApiResponse.failure(messageSourceUtils.getMessage("order.unavailable"));
        return ApiResponse.failure(" zubedi order.unavailable");
    }

    public ApiResponse<List<AdminOrdersResponse>> getAllOrders() {
        log.info(messageSourceUtils.getMessage("log.order.fetch.all"));

        List<AdminOrdersResponse> list = new ArrayList<>();
        for (Order o : orders.values()) {
            AdminOrdersResponse resp = new AdminOrdersResponse();
            resp.setOrderId(o.orderId());
            resp.setOrders(o);
            list.add(resp);
        }

        return ApiResponse.success(messageSourceUtils.getMessage("order.fetch.success"), list);
    }

    public ApiResponse<List<Order>> getOrdersByUser(String userId) {
        log.info(messageSourceUtils.getMessage("log.order.fetch.byuser", userId));

        List<Order> list = orders.values().stream()
                .filter(o -> Objects.equals(o.userId(), userId))
                .sorted(Comparator.comparing(Order::createdAt).reversed())
                .toList();

        if (list.isEmpty()) {
            return ApiResponse.failure(messageSourceUtils.getMessage("order.user.notfound", userId));
        }

        return ApiResponse.success(messageSourceUtils.getMessage("order.fetch.byuser.success", userId), list);
    }

    public ApiResponse<Order> getOrderById(String orderId) {
        log.info(messageSourceUtils.getMessage("log.order.fetch.byid", orderId));

        Order order = orders.get(orderId);
        if (order == null) {
            return ApiResponse.failure(messageSourceUtils.getMessage("order.notfound", orderId));
        }

        return ApiResponse.success(messageSourceUtils.getMessage("order.fetch.byid.success", orderId), order);
    }

    public ApiResponse<Order> updateOrderStatus(String orderId, OrderStatus status) {
        Order old = orders.get(orderId);
        if (old == null) {
            log.warn(messageSourceUtils.getMessage("log.order.notfound.update", orderId));
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
        log.info(messageSourceUtils.getMessage("log.order.update", orderId, status));
        return ApiResponse.success(messageSourceUtils.getMessage("order.update.success", orderId), updated);
    }
}
