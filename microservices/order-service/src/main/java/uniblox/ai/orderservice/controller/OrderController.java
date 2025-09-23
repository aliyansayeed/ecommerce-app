package uniblox.ai.orderservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uniblox.ai.common.model.dto.AdminOrdersResponse;
import uniblox.ai.common.model.dto.ApiResponse;
import uniblox.ai.common.model.entity.Order;
import uniblox.ai.common.model.entity.OrderItem;
import uniblox.ai.common.model.value.OrderStatus;
import uniblox.ai.orderservice.service.OrderService;

import java.util.List;

import static uniblox.ai.orderservice.model.OrderApiPaths.*;

@RestController
@RequestMapping(API_BASE_PATH)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
  //  checkout-service  | 2025-09-22T04:12:35.750Z  INFO 1 --
    //  - [CHECKOUT-SERVICE] [nio-8080-exec-2] u.a.c.service.CheckoutService
    // url=http://order-service:8080/api/v1/orders/user/mohammed@uniblox.com?totalAmount=1200.0&discountCode=&discountAmount=0.0&finalAmount=1200.0

    @PostMapping(CREATE_PATH)
    public ResponseEntity<ApiResponse<?>> createOrder(
            @PathVariable String userId,
            @RequestBody List<OrderItem> items,
            @RequestParam double totalAmount,
            @RequestParam(required = false) String discountCode,
            @RequestParam(defaultValue = "0") double discountAmount,
            @RequestParam double finalAmount   //  Added finalAmount
    ) {
        ApiResponse<Order> response =
                orderService.createOrder(userId.trim(), items, totalAmount, discountCode, discountAmount, finalAmount);

        return buildResponse(response);
    }

    @GetMapping(BY_USER_PATH)
    public ResponseEntity<ApiResponse<?>> getOrders(@PathVariable String userId) {
        ApiResponse<List<Order>> response = orderService.getOrdersByUser(userId.trim());
        return buildResponse(response);
    }

    @GetMapping(BY_ID_PATH)
    public ResponseEntity<ApiResponse<?>> getOrderById(@PathVariable String orderId) {
        ApiResponse<Order> response = orderService.getOrderById(orderId);
        return buildResponse(response);
    }

    @GetMapping(ALL_PATH)
    public ResponseEntity<ApiResponse<?>> getAllOrders() {
        ApiResponse<List<AdminOrdersResponse>> response = orderService.getAllOrders();
        return buildResponse(response);
    }

    @PutMapping(STATUS_UPDATE_PATH)
    public ResponseEntity<ApiResponse<?>> updateStatus(@PathVariable String orderId,
                                                       @RequestParam OrderStatus status) {
        ApiResponse<Order> response = orderService.updateOrderStatus(orderId, status);
        return buildResponse(response);
    }

    private ResponseEntity<ApiResponse<?>> buildResponse(ApiResponse<?> response) {
        HttpStatus status = (response != null && response.success())
                ? HttpStatus.OK
                : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(response);
    }
}
