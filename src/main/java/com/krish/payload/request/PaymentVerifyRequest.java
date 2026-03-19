package com.krish.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentVerifyRequest {

    @NotBlank(message = "razorpayPaymentId is required")
    private  String razorpayPaymentId;

    private String stripePaymentIntentId;
    private String stripePaymentIntentStatus;



}
