package uniblox.ai.cartservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uniblox.ai.cartservice.model.CartItem;
import uniblox.ai.cartservice.service.CartService;

import java.util.List;

@RestController
@RequestMapping("/cart/{userId}")
public class CartController {

    private static final Logger log_ = LoggerFactory.getLogger(CartController.class);


    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Add item to cart
    @PostMapping("/items")
    public ResponseEntity<?> addItem(@PathVariable String userId, @RequestBody CartItem item) {
        return ResponseEntity.ok(cartService.addItem(userId.trim(), item));
    }

    // Get cart items
    @GetMapping
    public ResponseEntity<?> getCart(@PathVariable String userId) {
        return ResponseEntity.ok(cartService.getCart(userId.trim()));
    }

    // Checkout
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@PathVariable String userId, @RequestParam(required = false) String discountCode
    ) {
        log_.info("discountCode from cartController "+discountCode);
        return ResponseEntity.ok(cartService.checkout(userId.trim(), discountCode));
    }
}
