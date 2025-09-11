package uniblox.ai.checkoutservice.model;

import uniblox.ai.checkoutservice.model.CheckoutItem;

import java.time.LocalDateTime;
import java.util.List;

public record Order(
        String orderId,
        String userId,
        List<OrderItem> items,
        double totalAmount,
        String discountCode,
        double discountAmount,
        double finalAmount,
        OrderStatus status,
        LocalDateTime createdAt
) {}
