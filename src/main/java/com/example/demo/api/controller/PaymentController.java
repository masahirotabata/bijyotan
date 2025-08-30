package com.example.demo.api.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final String stripeSecretKey;
    private final String stripePriceId;
    private final String successUrl;
    private final String cancelUrl;

    public PaymentController(
        @Value("${stripe.secret-key:}") String stripeSecretKey,
        @Value("${stripe.price-id:}")  String stripePriceId,
        @Value("${stripe.success-url:https://example.com/premium/success?session_id={CHECKOUT_SESSION_ID}}") String successUrl,
        @Value("${stripe.cancel-url:https://example.com/premium/cancel}") String cancelUrl
    ) {
        this.stripeSecretKey = stripeSecretKey;
        this.stripePriceId = stripePriceId;
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;

        if (!stripeSecretKey.isBlank()) {
            com.stripe.Stripe.apiKey = stripeSecretKey;
        }
    }

    @PostMapping("/create-checkout-session")
    public Map<String, String> createCheckoutSession(@RequestBody(required = false) Map<String, Object> payload) {
        if (stripeSecretKey.isBlank() || stripePriceId.isBlank()) {
            return Map.of("error", "Stripe is not configured on this environment.");
        }

        try {
            var params = com.stripe.param.checkout.SessionCreateParams.builder()
                .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                    com.stripe.param.checkout.SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPrice(stripePriceId) // 例: price_xxx
                        .build()
                )
                .build();

            var session = com.stripe.model.checkout.Session.create(params);
            return Map.of("checkoutUrl", session.getUrl());
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }
}
