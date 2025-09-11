package uniblox.ai.adminservice.model;

import uniblox.ai.adminservice.model.Discount;
import java.util.List;

public record AdminReportResponse(
        long totalOrders,
        long totalItems,
        double totalPurchaseAmount,
        double totalDiscountAmount,
        List<Discount> discountCodes
) {}
