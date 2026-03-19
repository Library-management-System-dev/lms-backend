package com.krish.service;

import com.krish.payload.dto.PaymentDTO;
import com.krish.payload.request.PaymentInitiateRequest;
import com.krish.payload.request.PaymentVerifyRequest;
import com.krish.payload.response.PaymentInitiateResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    PaymentInitiateResponse initiatePayment(PaymentInitiateRequest req) throws Exception;

    PaymentDTO verifyPayment(PaymentVerifyRequest req) throws Exception;

    Page<PaymentDTO> getAllPayments(Pageable pageable);
}
