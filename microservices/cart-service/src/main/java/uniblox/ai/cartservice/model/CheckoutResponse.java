package uniblox.ai.cartservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CheckoutResponse(
        Order order,
        String newCoupon // will be null unless it's the nth order
) {}
