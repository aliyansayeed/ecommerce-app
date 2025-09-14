package uniblox.ai.adminservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uniblox.ai.adminservice.dto.AdminReportResponseDto;
import uniblox.ai.common.model.AdminOrdersResponse;
import uniblox.ai.common.model.Discount;
import uniblox.ai.common.model.Order;
import uniblox.ai.common.model.OrderItem;

import java.util.Arrays;
import java.util.List;

@Service
public class AdminService {

    private final RestTemplate restTemplate;

    private final String orderServiceUrl = "http://localhost:8084/order/all-orders";
    private final String discountServiceUrl = "http://localhost:8083/discount";

    public AdminService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Generate discount for a user (admin-triggered).
     */
    public Object generateDiscount(String userId) {
        Discount discount = restTemplate.postForObject(
                discountServiceUrl + "/generate/" + userId,
                null,
                Discount.class
        );

        if (discount == null) {
            return new MessageResponse("No discount generated — user not eligible yet.");
        }

        return discount;
    }

    /**
     * Admin report aggregating from OrderService + DiscountService.
     */
    public AdminReportResponseDto getReport() {
        // fetch all orders
        AdminOrdersResponse[] ordersResponse = restTemplate.getForObject(
                orderServiceUrl, AdminOrdersResponse[].class);

        List<AdminOrdersResponse> orders = ordersResponse != null
                ? Arrays.asList(ordersResponse)
                : List.of();

        long totalOrders = orders.size();
        long totalItems = 0;
        double totalPurchaseAmount = 0;
        double totalDiscountAmount = 0;

        for (AdminOrdersResponse resp : orders) {
            Order order = resp.getOrders();
            if (order != null) {
                for (OrderItem item : order.items()) {
                    totalItems += item.quantity();
                }
                totalPurchaseAmount += order.totalAmount();
                totalDiscountAmount += order.discountAmount();
            }
        }

        // fetch all discounts
        Discount[] discountsArr = restTemplate.getForObject(
                discountServiceUrl + "/all", Discount[].class);

        List<Discount> discounts = discountsArr != null
                ? Arrays.asList(discountsArr)
                : List.of();

        return new AdminReportResponseDto(
                totalOrders,
                totalItems,
                totalPurchaseAmount,
                totalDiscountAmount,
                discounts
        );
    }

    /**
     * Small DTO for messages.
     */
    public record MessageResponse(String message) {}
}
