package uniblox.ai.orderservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uniblox.ai.common.model.AdminOrdersResponse;
import uniblox.ai.common.model.Order;
import uniblox.ai.common.model.OrderItem;
import uniblox.ai.common.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    // in-memory store orderId -> Order
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    // create and persist order; returns saved Order
    public Order createOrder(String userId, List<OrderItem> items,
                             double totalAmount, String discountCode, double discountAmount) {
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

        if (discountAmount > 0) {
            log.info("✅ Order created with DISCOUNT | orderId={} | userId={} | total={} | discount={} | final={}",
                    orderId, userId, totalAmount, discountAmount, finalAmount);
        } else {
            log.info("📦 Order created without discount | orderId={} | userId={} | total={} | final={}",
                    orderId, userId, totalAmount, finalAmount);
        }

        return order;
    }
    // add orders for admin //

    public List<AdminOrdersResponse> getAllOrders() {
        log.info("🔍 Retrieving all orders");
        AdminOrdersResponse response = null;//new AdminOrdersResponse();
        var list = new ArrayList<AdminOrdersResponse>();
        for (Order o : orders.values()) {
            response = new AdminOrdersResponse();
            response.setOrderId(o.orderId());
            response.setOrders(o);
             list.add(response);
        }
       // list.sort(Comparator.comparing(Order::createdAt).reversed());
       // log.info("📊 Found {} orders for user {}", list.size(), userId);
        return list;
    }

    public List<Order> getOrdersByUser(String userId) {
        log.info("🔍 Retrieving orders for user {}", userId);
        var list = new ArrayList<Order>();
        for (Order o : orders.values()) {
            if (Objects.equals(o.userId(), userId)) list.add(o);
        }
        list.sort(Comparator.comparing(Order::createdAt).reversed());
        log.info("📊 Found {} orders for user {}", list.size(), userId);
        return list;
    }

    public Optional<Order> getOrderById(String orderId) {
        log.info("🔍 Fetching order by ID {}", orderId);
        return Optional.ofNullable(orders.get(orderId));
    }

    public Optional<Order> updateOrderStatus(String orderId, OrderStatus status) {
        Order old = orders.get(orderId);
        if (old == null) {
            log.warn("⚠️ Attempted to update status for non-existent orderId={}", orderId);
            return Optional.empty();
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
        log.info("🔄 Updated order status | orderId={} | newStatus={}", orderId, status);
        return Optional.of(updated);
    }

   //  todo below must remove after test
    public Map<String, Order> getOrdersAllOrders() {
        return  orders;
    }
}
