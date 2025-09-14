/*
package uniblox.ai.adminservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;
import uniblox.ai.common.model.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AdminServiceTest {

    private RestTemplate restTemplate;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        restTemplate = Mockito.mock(RestTemplate.class);
        adminService = new AdminService(restTemplate);
    }

    private Order sampleOrder(String orderId, String userId, double total, double discount) {
        OrderItem item = new OrderItem("P1001", "Laptop", 1, total, userId, LocalDateTime.now());
        return new Order(orderId, userId, List.of(item), total, null, discount,
                total - discount, OrderStatus.PLACED, LocalDateTime.now());
    }

    private Discount sampleDiscount(String code, boolean used) {
        return new Discount(code, 10.0, used, LocalDateTime.now());
    }

    @Test
    void getReport_shouldAggregateOrdersAndDiscounts() {
        // Mock orders from OrderService
        List<Order> orders = List.of(
                sampleOrder("O1", "user1", 1000.0, 0.0),
                sampleOrder("O2", "user2", 2000.0, 200.0)
        );

        when(restTemplate.getForObject("http://localhost:8084/order/all-orders", List.class))
                .thenReturn(orders);

        // Mock discounts from DiscountService
        List<Discount> discounts = List.of(
                sampleDiscount("CODE123", true),
                sampleDiscount("CODE456", false)
        );

        when(restTemplate.getForObject("http://localhost:8083/discount/all", List.class))
                .thenReturn(discounts);

        // When
        AdminReportResponseDto report = adminService.getReport();

        // Then
        assertThat(report.totalOrders()).isEqualTo(2);
        assertThat(report.totalItems()).isEqualTo(2); // 1 item per order
        assertThat(report.totalPurchaseAmount()).isEqualTo(3000.0);
        assertThat(report.totalDiscountAmount()).isEqualTo(200.0);
        assertThat(report.discountCodes()).hasSize(2);
    }
}
*/
