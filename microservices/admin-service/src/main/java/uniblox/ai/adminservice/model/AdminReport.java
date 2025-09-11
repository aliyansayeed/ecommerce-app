package uniblox.ai.adminservice.model;

import uniblox.ai.adminservice.model.OrderStatus;

import java.util.List;
import java.util.Map;

public record AdminReport(
        long totalItemsPurchased,
        double totalPurchaseAmount,
        List<Discount> discountCodes,
        double totalDiscountAmount,
        Map<OrderStatus, Long> ordersByStatus
) {}
