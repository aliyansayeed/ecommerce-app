package uniblox.ai.checkoutservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uniblox.ai.checkoutservice.model.CheckoutItem;
import uniblox.ai.checkoutservice.model.CheckoutResponse;
import uniblox.ai.checkoutservice.service.CheckoutService;

import java.util.List;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    private static final Logger log_ = LoggerFactory.getLogger(CheckoutController.class);

    @Autowired
    private CheckoutService checkoutService;

    @PostMapping("/{userId}")
    public CheckoutResponse checkout(
            @PathVariable String userId,
            @RequestBody List<CheckoutItem> items,
            @RequestParam(required = false) String discountCode
    ) {
        log_.info("discountCode  = " + discountCode);
        return checkoutService.checkout(userId.trim(), items, discountCode);
    }
}
