package uniblox.ai.discountservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uniblox.ai.common.model.dto.ApiResponse;
import uniblox.ai.common.model.entity.Discount;
import uniblox.ai.discountservice.service.DiscountService;
import uniblox.ai.utils.MessageSourceUtils;

import java.util.List;
import java.util.Optional;

import static uniblox.ai.common.api.path.DiscountApiPaths.*;

/**
 * Discount REST endpoints.
 * Base path: /api/v1/discounts
 */
@RestController
@RequestMapping(API_BASE_PATH)
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;
    private final MessageSourceUtils messageSourceUtils;

    /**
     * Generate a discount (internal, called from checkout).
     */
    @PostMapping(GENERATE_PATH)
    public ResponseEntity<ApiResponse<?>> generateDiscount(@PathVariable String userId) {
        Optional<Discount> discount = discountService.generateDiscountForOrder(userId);

        if (discount.isPresent()) {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            messageSourceUtils.getMessage("discountsvc.generated", userId),
                            discount.get()
                    )
            );
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.failure(messageSourceUtils.getMessage("discountsvc.not.eligible")));
    }

    /**
     * Validate discount code for a given user.
     */
    @GetMapping("/validate/{userId}/{code}")
    public ResponseEntity<ApiResponse<Boolean>> validateDiscount(
            @PathVariable String userId,
            @PathVariable String code) {

        boolean isValid = discountService.validateDiscountCode(userId, code);

        if (isValid) {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            messageSourceUtils.getMessage("discountsvc.validation.success", code, userId),
                            true
                    )
            );
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.failure(
                        messageSourceUtils.getMessage("discountsvc.validation.failed", code, userId))
        );
    }

    /**
     * Admin reporting: get all discount codes.
     */
    @GetMapping(ALL_PATH)
    public ResponseEntity<ApiResponse<List<Discount>>> allDiscounts() {
        List<Discount> discounts = discountService.getAllDiscountCodes();

        if (discounts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.failure(messageSourceUtils.getMessage("discountsvc.fetch.empty")));
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        messageSourceUtils.getMessage("discountsvc.fetch.success"),
                        discounts
                )
        );
    }
}
