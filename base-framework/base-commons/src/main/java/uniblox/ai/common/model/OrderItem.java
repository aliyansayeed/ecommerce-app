package uniblox.ai.common.model;

import java.time.LocalDateTime;

public record OrderItem(
        String productId,
        String name,
        Integer quantity,
        Double price,
        String userId,
        LocalDateTime addedAt
) {}
