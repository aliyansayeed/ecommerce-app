package uniblox.ai.checkoutservice.model;

import java.time.LocalDateTime;

public record CheckoutItem(
        String productId,
        String name,
        int quantity,
        double price,
        String userId,
        LocalDateTime addedAt
) {}
