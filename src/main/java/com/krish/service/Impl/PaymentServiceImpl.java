package com.krish.service.Impl;

import com.krish.domain.PaymentGateway;
import com.krish.domain.PaymentStatus;
import com.krish.event.publisher.PaymentEventPublisher;
import com.krish.mapper.PaymentMapper;
import com.krish.modal.Payment;
import com.krish.modal.Subscription;
import com.krish.modal.User;
import com.krish.payload.dto.PaymentDTO;
import com.krish.payload.request.PaymentInitiateRequest;
import com.krish.payload.request.PaymentVerifyRequest;
import com.krish.payload.response.PaymentInitiateResponse;
import com.krish.payload.response.PaymentLinkResponse;
import com.krish.repository.PaymentRepository;
import com.krish.repository.SubscriptionRepository;
import com.krish.repository.UserRepository;
import com.krish.service.PaymentService;
import com.krish.service.gateway.RazorpayService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final RazorpayService razorpayService;
    private final PaymentMapper paymentMapper;
    private final PaymentEventPublisher paymentEventPublisher;

    @Override
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) throws Exception {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new Exception("User not found with id: " + request.getUserId()));

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setPaymentType(request.getPaymentType());
        payment.setGateway(request.getGateway());
        payment.setAmount(request.getAmount());
        payment.setDescription(request.getDescription());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId("TXN_" + UUID.randomUUID());
        payment.setInitiatedAt(LocalDateTime.now());

        if(request.getSubscriptionId()!=null){
            Subscription sub = subscriptionRepository
                    .findById(request.getSubscriptionId())
                    .orElseThrow(() -> new Exception("Subscription not found"));
            payment.setSubscription(sub);
        }
        payment = paymentRepository.save(payment);

        PaymentInitiateResponse response = new PaymentInitiateResponse();

        if(request.getGateway()== PaymentGateway.RAZORPAY){
            PaymentLinkResponse paymentLinkResponse = razorpayService.createPaymentLink(user, payment);
            response = PaymentInitiateResponse.builder()
                    .paymentId(payment.getId())
                    .gateway(payment.getGateway())
                    .checkoutUrl(paymentLinkResponse.getPayment_Link_url())
                    .transactionId(paymentLinkResponse.getPayment_Link_id())
                    .amount(payment.getAmount())
                    .description(payment.getDescription())
                    .success(true)
                    .message("payment initiated successfully")
                    .build();
            payment.setGatewayOrderId(paymentLinkResponse.getPayment_Link_id());
        }
        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);
        //payment initiate event
        return response;
    }

    @Override
    public PaymentDTO verifyPayment(PaymentVerifyRequest req) throws Exception {

        JSONObject paymentDetails = razorpayService.fetchPaymentDetails(
                req.getRazorpayPaymentId()
        );
        JSONObject notes = paymentDetails.getJSONObject("notes");

        Long paymentId = Long.parseLong(notes.optString("payment_id"));

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new Exception("Payment not found with id: " + paymentId));

        boolean isValid = razorpayService.isValidPayment(req.getRazorpayPaymentId());

        if(PaymentGateway.RAZORPAY==payment.getGateway()){
            if(isValid){
                payment.setGatewayOrderId(req.getRazorpayPaymentId());
            }
        }
        if(isValid){
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setCompletedAt(LocalDateTime.now());
            payment = paymentRepository.save(payment);
            //publish payment success event - todo
            paymentEventPublisher.publishPaymentSuccessEvent(payment);

        }
        return paymentMapper.toDTO(payment);
    }

    @Override
    public Page<PaymentDTO> getAllPayments(Pageable pageable) {
        Page<Payment> payments = paymentRepository.findAll(pageable);
        return payments.map(paymentMapper::toDTO);
    }
}
