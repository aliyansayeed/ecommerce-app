package uniblox.ai.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CheckoutItem(
        String productId,
        String name,
        Integer quantity,
        Double price,
        String userId,
        LocalDateTime addedAt

        //String productId,
        //String name,
       // int quantity,
        //double price,
        //String userId,
        //LocalDateTime addedAt
) {}
