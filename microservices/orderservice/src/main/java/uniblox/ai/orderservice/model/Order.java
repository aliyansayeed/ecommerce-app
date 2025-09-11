package uniblox.ai.orderservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
@JsonInclude(JsonInclude.Include.NON_EMPTY)
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
