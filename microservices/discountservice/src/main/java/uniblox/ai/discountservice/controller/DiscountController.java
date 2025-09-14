package uniblox.ai.discountservice.controller;

import org.springframework.web.bind.annotation.*;
import uniblox.ai.common.model.Discount;
import uniblox.ai.discountservice.service.DiscountService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/discount")
public class DiscountController {

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    /**
     * Generate a discount (internal, called from checkout)
     */
    @PostMapping("/generate/{userId}")
    public Discount generateDiscount(@PathVariable String userId) {
        Optional<Discount> discount = discountService.generateDiscountForOrder(userId);
        return discount.orElse(null); // null means no coupon generated this time
    }

    /**
     * Validate discount code for a given user
     */
    @GetMapping("/validate/{userId}/{code}")
    public boolean validateDiscount(@PathVariable String userId, @PathVariable String code) {
        return discountService.validateDiscountCode(userId, code);
    }

    /**
     * Admin reporting: get all discount codes
     */
    @GetMapping("/all")
    public List<Discount> allDiscounts() {
        return discountService.getAllDiscountCodes();
    }
}
