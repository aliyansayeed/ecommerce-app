package uniblox.ai.checkoutservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uniblox.ai.checkoutservice.service.CheckoutService;
import uniblox.ai.common.model.dto.ApiResponse;
import uniblox.ai.common.model.value.CheckoutItem;

import java.util.List;

import static uniblox.ai.common.api.path.CheckoutApiPaths.API_BASE_PATH;
import static uniblox.ai.common.api.path.CheckoutApiPaths.CHECKOUT_PATH;

@RestController
@RequestMapping(API_BASE_PATH)
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping(CHECKOUT_PATH)
    public ResponseEntity<ApiResponse<?>> checkout(
            @PathVariable String userId,
            @RequestBody List<CheckoutItem> items,
            @RequestParam(required = false) String discountCode
    ) {
        ApiResponse<?> response = checkoutService.checkout(userId.trim(), items, discountCode);

        HttpStatus status = response.success() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(response);
    }
}
