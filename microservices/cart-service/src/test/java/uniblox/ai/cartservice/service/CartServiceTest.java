package uniblox.ai.cartservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;
import uniblox.ai.cartservice.model.CartItem;
import uniblox.ai.common.model.CheckoutResponse;
import uniblox.ai.common.model.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CartServiceTest {

    private RestTemplate restTemplate;
    private CartService cartService;

    @BeforeEach
    void setUp() {
        // Mock the RestTemplate dependency
        restTemplate = Mockito.mock(RestTemplate.class);
        cartService = new CartService(restTemplate);
    }

    @Test
    void addItem_shouldAddToCart() {
        CartItem item = new CartItem("P1001", "Laptop", 1, 999.0, "user1", LocalDateTime.now());

        Map<String, Object> result = cartService.addItem("user1", item);

        assertThat(result).containsEntry("userId", "user1");
        List<CartItem> items = (List<CartItem>) result.get("items");
        assertThat(items).hasSize(1);
        assertThat(items.get(0).name()).isEqualTo("Laptop");
    }

    @Test
    void getCart_whenEmpty_shouldReturnEmptyList() {
        Map<String, Object> result = cartService.getCart("userX");

        assertThat(result).containsEntry("userId", "userX");
        List<CartItem> items = (List<CartItem>) result.get("items");
        assertThat(items).isEmpty();
    }

    @Test
    void checkout_whenCartEmpty_shouldReturnMessage() {
        String result = cartService.checkout("userX", null);

        assertThat(result).isEqualTo("Cart is empty");
    }

    @Test
    void checkout_whenSuccessful_shouldClearCartAndReturnMessage() {
        // Given a cart with an item
        CartItem item = new CartItem("P1001", "Laptop", 1, 999.0, "user1", LocalDateTime.now());
        cartService.addItem("user1", item);

        // Mock response from Checkout service
        Order order = new Order("O123", "user1", List.of(), 999.0, null, 0.0, 999.0, null, LocalDateTime.now());
        CheckoutResponse response = new CheckoutResponse(order, "NEWCOUPON123");

        when(restTemplate.postForObject(anyString(), any(), eq(CheckoutResponse.class)))
                .thenReturn(response);

        // When
        String result = cartService.checkout("user1", "");

        // Then
        assertThat(result).contains("Checkout successful for user: user1");
        assertThat(result).contains("NEWCOUPON123");

        // Cart should be cleared
        Map<String, Object> cart = cartService.getCart("user1");
        assertThat((List<CartItem>) cart.get("items")).isEmpty();
    }

    @Test
    void checkout_whenRestTemplateFails_shouldReturnErrorMessage() {
        CartItem item = new CartItem("P1001", "Laptop", 1, 999.0, "user1", LocalDateTime.now());
        cartService.addItem("user1", item);

        when(restTemplate.postForObject(anyString(), any(), eq(CheckoutResponse.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        String result = cartService.checkout("user1", "");

        assertThat(result).contains("Checkout failed: Service unavailable");
    }
}
