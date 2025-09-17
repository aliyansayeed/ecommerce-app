package uniblox.ai.common.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import uniblox.ai.common.model.entity.Order;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CheckoutResponse(
        Order order,
        String newCoupon // will be null unless it's the nth order
) {}
