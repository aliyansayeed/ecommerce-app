package uniblox.ai.common.model.entity;

import uniblox.ai.common.model.value.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record Order(
        String orderId,
        String userId,
        List<OrderItem> items,
        double totalAmount,       // total before discount
        String discountCode,      // applied discount (nullable)
        double discountAmount,    // discount value
        double finalAmount,       // totalAmount - discountAmount
        OrderStatus status,       // CREATED, PLACED, PAID, etc.
        LocalDateTime createdAt
) {}
