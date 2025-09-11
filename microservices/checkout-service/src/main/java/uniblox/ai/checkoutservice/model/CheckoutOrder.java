package uniblox.ai.checkoutservice.model;

import java.util.List;

public record CheckoutOrder(
        String userId,
        List<CheckoutItem> items,
        double totalAmount,
        String discountCode,   // applied discount if any
        double finalAmount     // after discount
) {}
