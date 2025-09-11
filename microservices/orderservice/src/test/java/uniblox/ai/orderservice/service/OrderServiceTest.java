package uniblox.ai.orderservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uniblox.ai.orderservice.model.AdminOrdersResponse;
import uniblox.ai.orderservice.model.Order;
import uniblox.ai.orderservice.model.OrderItem;
import uniblox.ai.orderservice.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OrderServiceTest {

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService();
    }

    private OrderItem sampleItem() {
        return new OrderItem("P1001", "Laptop", 1, 999.0, "user1", LocalDateTime.now());
    }

    @Test
    void createOrder_shouldStoreOrder() {
        Order order = orderService.createOrder("user1", List.of(sampleItem()), 999.0, null, 0.0);

        assertThat(order.orderId()).isNotNull();
        assertThat(order.userId()).isEqualTo("user1");
        assertThat(order.totalAmount()).isEqualTo(999.0);
        assertThat(order.discountAmount()).isEqualTo(0.0);
    }

    @Test
    void getOrdersByUser_shouldReturnOrdersForThatUser() {
        orderService.createOrder("user1", List.of(sampleItem()), 999.0, null, 0.0);
        orderService.createOrder("user2", List.of(sampleItem()), 1200.0, null, 0.0);

        List<Order> user1Orders = orderService.getOrdersByUser("user1");

        assertThat(user1Orders).hasSize(1);
        assertThat(user1Orders.get(0).userId()).isEqualTo("user1");
    }

    @Test
    void getOrderById_shouldReturnCorrectOrder() {
        Order created = orderService.createOrder("user1", List.of(sampleItem()), 999.0, null, 0.0);

        Optional<Order> found = orderService.getOrderById(created.orderId());

        assertThat(found).isPresent();
        assertThat(found.get().orderId()).isEqualTo(created.orderId());
    }

    @Test
    void updateOrderStatus_shouldChangeStatus() {
        Order created = orderService.createOrder("user1", List.of(sampleItem()), 999.0, null, 0.0);

        Optional<Order> updated = orderService.updateOrderStatus(created.orderId(), OrderStatus.SHIPPED);

        assertThat(updated).isPresent();
        assertThat(updated.get().status()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void getAllOrders_shouldReturnAdminOrderResponses() {
        orderService.createOrder("user1", List.of(sampleItem()), 999.0, null, 0.0);
        orderService.createOrder("user2", List.of(sampleItem()), 1200.0, null, 0.0);

        List<AdminOrdersResponse> all = orderService.getAllOrders();

        assertThat(all).hasSize(2);
        assertThat(all.get(0).getOrders()).isNotNull();
    }
}
