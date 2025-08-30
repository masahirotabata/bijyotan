package com.example.demo.api.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    public PaymentController(
        @Value("${stripe.secret-key}") String secretKey
    ) {
        Stripe.apiKey = secretKey;
    }

    @Value("${stripe.price-id}")
    private String priceId;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @PostMapping("/create-checkout-session")
    public Map<String, String> createCheckoutSession(@RequestBody Map<String, Object> payload) {
        // 必要なら userId を検証
        // Long userId = Long.valueOf(String.valueOf(payload.get("userId")));

        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setSuccessUrl(successUrl) // {CHECKOUT_SESSION_ID} を含めると便利
            .setCancelUrl(cancelUrl)
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPrice(priceId) // ex: price_XXXXXXXX
                    .build()
            )
            .build();

        try {
            Session session = Session.create(params);
            return Map.of("checkoutUrl", session.getUrl());
        } catch (Exception e) {
            // ログにも出すと原因特定が早い
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }
}
