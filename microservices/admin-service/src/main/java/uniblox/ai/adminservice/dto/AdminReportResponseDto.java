package uniblox.ai.adminservice.dto;

import uniblox.ai.common.model.Discount;

import java.util.List;

public record AdminReportResponseDto(
        long totalOrders,
        long totalItems,
        double totalPurchaseAmount,
        double totalDiscountAmount,
        List<Discount> discountCodes
) {}
