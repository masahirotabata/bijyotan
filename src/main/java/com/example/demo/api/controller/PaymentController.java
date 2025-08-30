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

        // キーがあれば Stripe SDK を初期化（未設定なら起動は通し、エンドポイント側で弾く）
        if (stripeSecretKey != null && !stripeSecretKey.isBlank()) {
            Stripe.apiKey = stripeSecretKey;
        }
    }

    @PostMapping("/create-checkout-session")
    public Map<String, String> createCheckoutSession(@RequestBody(required = false) Map<String, Object> payload) {
        // 未設定ガード（起動は通しつつ、呼ばれたら 503 相当のメッセージを返す）
        if (stripeSecretKey == null || stripeSecretKey.isBlank()
                || stripePriceId == null || stripePriceId.isBlank()) {
            return Map.of("error", "Stripe is not configured on this environment.");
        }

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(successUrl) // {CHECKOUT_SESSION_ID} を含めると便利
                .setCancelUrl(cancelUrl)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPrice(stripePriceId)   // ← フィールド名修正
                        .build()
                )
                .build();

            Session session = Session.create(params);
            return Map.of("checkoutUrl", session.getUrl());

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }
}
