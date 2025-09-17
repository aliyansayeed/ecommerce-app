package uniblox.ai.cartservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uniblox.ai.cartservice.model.CartItem;
import uniblox.ai.cartservice.service.CartService;
import uniblox.ai.common.model.dto.ApiResponse;

import static uniblox.ai.common.api.path.CartApiPaths.*;

@RestController
@RequestMapping(API_BASE_PATH)
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // Add item to cart
    @PostMapping(ITEMS_PATH)
    public ResponseEntity<ApiResponse<?>> addItem(@PathVariable String userId, @RequestBody CartItem item) {
        ApiResponse<?> response = cartService.addItem(userId.trim(), item);
        return buildResponse(response);
    }

    // Get cart items
    @GetMapping(CART_PATH)
    public ResponseEntity<ApiResponse<?>> getCart(@PathVariable String userId) {
        ApiResponse<?> response = cartService.getCart(userId.trim());
        return buildResponse(response);
    }

    // Checkout
    @PostMapping(CHECKOUT_PATH)
    public ResponseEntity<ApiResponse<?>> checkout(
            @PathVariable String userId,
            @RequestParam(required = false) String discountCode) {

        ApiResponse<?> response = cartService.checkout(userId.trim(), discountCode);
        return buildResponse(response);
    }

    private ResponseEntity<ApiResponse<?>> buildResponse(ApiResponse<?> response) {
        HttpStatus status = (response != null && response.success())
                ? HttpStatus.OK
                : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(response);
    }
}
