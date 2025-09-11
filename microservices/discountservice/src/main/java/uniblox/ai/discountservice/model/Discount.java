package uniblox.ai.discountservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record Discount(
        String code,
        double percentage, // 10% discount
        boolean used,
        LocalDateTime createdAt
) {}
