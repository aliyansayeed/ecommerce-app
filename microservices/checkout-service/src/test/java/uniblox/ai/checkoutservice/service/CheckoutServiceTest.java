package uniblox.ai.checkoutservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;
import uniblox.ai.common.model.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CheckoutServiceTest {

    private RestTemplate restTemplate;
    private CheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        restTemplate = Mockito.mock(RestTemplate.class);
        checkoutService = new CheckoutService(restTemplate);
    }

    @Test
    void checkout_withoutDiscount_shouldReturnOrder() {
        // Given cart items
        CheckoutItem item = new CheckoutItem("P1001", "Laptop", 1, 1000.0, "user1", LocalDateTime.now());
        List<CheckoutItem> items = List.of(item);

        // Mock order service response
        Order order = new Order("O1", "user1", List.of(
                new OrderItem(item.productId(), item.name(), item.quantity(), item.price(), item.userId(), item.addedAt())
        ), 1000.0, null, 0.0, 1000.0, OrderStatus.PLACED, LocalDateTime.now());

        when(restTemplate.postForObject(anyString(), any(), eq(Order.class)))
                .thenReturn(order);

        // Mock discount generation response
        CheckoutService.Discount discount = null;
        when(restTemplate.postForObject(anyString(), eq(null), eq(CheckoutService.Discount.class)))
                .thenReturn(discount);

        // When
        CheckoutResponse response = checkoutService.checkout("user1", items, null);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.order().orderId()).isEqualTo("O1");
        assertThat(response.newCoupon()).isNull();
    }

    @Test
    void checkout_withValidDiscount_shouldApply10Percent() {
        // Given cart items
        CheckoutItem item = new CheckoutItem("P2001", "Phone", 1, 500.0, "user1", LocalDateTime.now());
        List<CheckoutItem> items = List.of(item);

        // Mock discount validation = true
        when(restTemplate.getForObject(anyString(), eq(Boolean.class)))
                .thenReturn(true);

        // Mock order service response
        Order order = new Order("O2", "user1", List.of(
                new OrderItem(item.productId(), item.name(), item.quantity(), item.price(), item.userId(), item.addedAt())
        ), 500.0, "CODE123", 50.0, 450.0, OrderStatus.PLACED, LocalDateTime.now());

        when(restTemplate.postForObject(anyString(), any(), eq(Order.class)))
                .thenReturn(order);

        // Mock coupon generation
        CheckoutService.Discount newCoupon = new CheckoutService.Discount("NEWCODE", 10.0, false, LocalDateTime.now());
        when(restTemplate.postForObject(anyString(), eq(null), eq(CheckoutService.Discount.class)))
                .thenReturn(newCoupon);

        // When
        CheckoutResponse response = checkoutService.checkout("user1", items, "CODE123");

        // Then
        assertThat(response.order().discountCode()).isEqualTo("CODE123");
        assertThat(response.order().discountAmount()).isEqualTo(50.0);
        assertThat(response.newCoupon()).isEqualTo("NEWCODE");
    }

    @Test
    void checkout_withEmptyCart_shouldThrowException() {
        // When / Then
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        checkoutService.checkout("user1", List.of(), null)
                ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cart is empty");
    }
}
