package uniblox.ai.orderservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uniblox.ai.orderservice.model.AdminOrdersResponse;
import uniblox.ai.orderservice.model.Order;
import uniblox.ai.orderservice.model.OrderItem;
import uniblox.ai.orderservice.model.OrderStatus;
import uniblox.ai.orderservice.service.OrderService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Order> createOrder(
            @PathVariable String userId,
            @RequestBody List<OrderItem> items,
            @RequestParam double totalAmount,
            @RequestParam(required = false) String discountCode,
            @RequestParam(defaultValue = "0") double discountAmount) {

        return ResponseEntity.ok(orderService.createOrder(userId.trim(), items, totalAmount, discountCode, discountAmount));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Order>> getOrders(@PathVariable String userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId.trim()));
    }

    @GetMapping("/id/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable String orderId) {
        Optional<Order> order = orderService.getOrderById(orderId);
        return order.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/all-orders")
    public ResponseEntity<List<AdminOrdersResponse>> getAllOrders() {
        List<AdminOrdersResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }



    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable String orderId,
                                              @RequestParam OrderStatus status) {
        Optional<Order> updated = orderService.updateOrderStatus(orderId, status);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
