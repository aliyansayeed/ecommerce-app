package uniblox.ai.cartservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CartItem(
        @NotBlank String productId,
        @NotBlank String name,
        @NotNull @Min(1) Integer quantity,
        @NotNull @Min(0) Double price,
        String userId, // removing userId notBlank as it check before controller set it via pathvariable
        LocalDateTime addedAt
) {}
