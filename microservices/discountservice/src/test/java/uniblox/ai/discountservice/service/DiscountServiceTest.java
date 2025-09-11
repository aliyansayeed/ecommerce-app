package uniblox.ai.discountservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uniblox.ai.discountservice.model.Discount;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountServiceTest {

    private DiscountService discountService;

    @BeforeEach
    void setUp() {
        discountService = new DiscountService();
    }

    @Test
    void generateDiscountForOrder_shouldReturnEmptyUntilNthOrder() {
        // First two orders → no discount
        Optional<Discount> d1 = discountService.generateDiscountForOrder("user1");
        Optional<Discount> d2 = discountService.generateDiscountForOrder("user1");

        assertThat(d1).isEmpty();
        assertThat(d2).isEmpty();

        // Third order → should generate discount
        Optional<Discount> d3 = discountService.generateDiscountForOrder("user1");

        assertThat(d3).isPresent();
        assertThat(d3.get().percentage()).isEqualTo(10.0);
        assertThat(d3.get().used()).isFalse();
    }

    @Test
    void validateDiscountCode_shouldMarkCodeAsUsed() {
        // Generate code at 3rd order
        discountService.generateDiscountForOrder("user1");
        discountService.generateDiscountForOrder("user1");
        Optional<Discount> discountOpt = discountService.generateDiscountForOrder("user1");

        assertThat(discountOpt).isPresent();
        String code = discountOpt.get().code();

        // Validate the code
        boolean valid = discountService.validateDiscountCode("user1", code);

        assertThat(valid).isTrue();

        // Second validation should fail
        boolean reused = discountService.validateDiscountCode("user1", code);

        assertThat(reused).isFalse();
    }

    @Test
    void validateDiscountCode_withInvalidCode_shouldReturnFalse() {
        boolean valid = discountService.validateDiscountCode("user1", "INVALID123");

        assertThat(valid).isFalse();
    }

    @Test
    void getAllDiscountCodes_shouldReturnGeneratedCodes() {
        // Generate 3 orders → 1 discount
        discountService.generateDiscountForOrder("user1");
        discountService.generateDiscountForOrder("user1");
        discountService.generateDiscountForOrder("user1");

        List<Discount> codes = discountService.getAllDiscountCodes();

        assertThat(codes).hasSize(1);
        assertThat(codes.get(0).percentage()).isEqualTo(10.0);
    }
}
