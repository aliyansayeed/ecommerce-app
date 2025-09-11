package uniblox.ai.checkoutservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import uniblox.ai.checkoutservice.model.Order;

/**
 * Wrapper response for checkout operation.
 * Includes the created Order and, if applicable,
 * a newly generated discount coupon code for the user.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CheckoutResponse(
        Order order,
        String newCoupon
) {}
