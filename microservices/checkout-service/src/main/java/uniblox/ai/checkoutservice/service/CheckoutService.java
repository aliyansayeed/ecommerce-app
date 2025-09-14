package uniblox.ai.checkoutservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
//import uniblox.ai.checkoutservice.model.*;
import uniblox.ai.common.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    private final RestTemplate restTemplate;

    // OrderService URL (via API Gateway or direct)
    private final String orderServiceUrl = "http://localhost:8080/order";

    // DiscountService URL
    private final String discountServiceUrl = "http://localhost:8080/discount";

    public CheckoutService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CheckoutResponse checkout(String userId, List<CheckoutItem> items, String discountCode)
    {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        log.info(" inside checkout discountCod {} " ,discountCode);
        // 1️⃣ Calculate total amount
        double totalAmount = items.stream()
                .mapToDouble(item -> item.price() * item.quantity())
                .sum();

        double discountAmount = 0.0;

        // 2️⃣ Validate and apply discount if coupon is given
        if (discountCode != null && !discountCode.isBlank()) {
            Boolean valid = restTemplate.getForObject(
                    discountServiceUrl + "/validate/" + userId + "/" + discountCode,
                    Boolean.class
            );
            log.info("zubedi.1111111111111111111..............................................");
            log.info("zubedi.1111111111111111111..............................................");
            log.info("zubedi.1111111111111111111..............................................");
            log.info("zubedi.1111111111111111111..............................................");
            log.info("zubedi.1111111111111111111..............................................");
            log.info("zubedi.1111111111111111111..............................................");

            if (Boolean.TRUE.equals(valid)) {
                log.info("zubedi.insdie validate block..............................................");

                discountAmount = totalAmount * 0.10; // apply 10% discount
                log.info("✅ Valid coupon applied | userId={} | code={} | discount={} | final={}",
                        userId, discountCode, discountAmount, (totalAmount - discountAmount));
            } else {
                log.warn("⚠️ Invalid or already used coupon | userId={} | code={}", userId, discountCode);
            }
            log.info("zubedi.2222222222222222..............................................");
            log.info("zubedi.2222222222222222..............................................");
            log.info("zubedi.2222222222222222..............................................");
            log.info("zubedi.2222222222222222..............................................");
            log.info("zubedi.2222222222222222..............................................");

        } else {
            log.info("ℹ️ No coupon provided | userId={}", userId);
        }
        log.info("zubedi.setting final amout..............................................");
        log.info("zubedi.discount amout is.."+discountAmount);
        log.info("zubedi.total amout is.."+totalAmount);


        double finalAmount = totalAmount - discountAmount;

        log.info("zubedi.final amout is.."+finalAmount);


        // 3️⃣ Build order request
        Order orderRequest = new Order(
                null,
                userId,
                toOrderItems(items),
                totalAmount,
                discountCode,
                discountAmount,
                finalAmount,
                OrderStatus.PLACED,
                LocalDateTime.now()
        );

        // 4️⃣ Save order via OrderService
        Order createdOrder = restTemplate.postForObject(
                orderServiceUrl + "/" + userId
                        + "?totalAmount=" + totalAmount
                        + "&discountCode=" + (discountCode != null ? discountCode : "")
                        + "&discountAmount=" + discountAmount,
                toOrderItems(items),
                Order.class
        );

        // 5️⃣ Generate new coupon if nth order
        Discount newCoupon = restTemplate.postForObject(
                discountServiceUrl + "/generate/" + userId,
                null,
                Discount.class
        );

        if (newCoupon != null) {
            log.info("🎁 New coupon generated for userId={} | code={}", userId, newCoupon.code());
        }

        return new CheckoutResponse(createdOrder, newCoupon != null ? newCoupon.code() : null);
    }

    private List<OrderItem> toOrderItems(List<CheckoutItem> items) {
        return items.stream()
                .map(i -> new OrderItem(
                        i.productId(),
                        i.name(),
                        i.quantity(),
                        i.price(),
                        i.userId(),
                        i.addedAt()
                ))
                .collect(Collectors.toList());
    }

    // Inner records for response mapping
    public record Discount(String code, double percentage, boolean used, LocalDateTime createdAt) {}

  //  public record CheckoutResponse(Order order, String newCoupon) {}
}
