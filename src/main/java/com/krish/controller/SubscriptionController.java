package com.krish.controller;

import com.krish.exception.SubscriptionException;
import com.krish.payload.dto.SubscriptionDTO;
import com.krish.payload.response.ApiResponse;
import com.krish.payload.response.PaymentInitiateResponse;
import com.krish.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.data.domain.Pageable;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscription")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(
            @Valid @RequestBody SubscriptionDTO subscription
    ) throws Exception {
        PaymentInitiateResponse dto = subscriptionService.subscribe(subscription);
        return ResponseEntity.ok(dto);

    }

    @GetMapping("/user/active")
    public ResponseEntity<?> getUsersActiveSubscription(
            @RequestParam(required = false) Long userId
    ) throws Exception {


        SubscriptionDTO dto = subscriptionService
                .getUsersActiveSubscription(userId);
        return ResponseEntity.ok(dto);
    }


    @GetMapping("/admin")
    public ResponseEntity<?> getAllSubscriptions(){
        int page=0;
        int size=10;

        Pageable pageable = PageRequest.of(page,size);
        List<SubscriptionDTO> dtoList = subscriptionService.getAllSubscriptions(pageable);
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/admin/deactivate-expired")
    public ResponseEntity<?> deactivateExpiredSubscriptions(
            @RequestParam Long userId
    ) throws Exception {
        subscriptionService.deactivateExpiredSubscriptions(userId);
        ApiResponse res = new ApiResponse("task done!", true);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/cancel/{subscriptionId}")
    public ResponseEntity<?> cancelSubscription(
            @PathVariable Long subscriptionId,
            @RequestParam(required = false) String reason) throws SubscriptionException {

            SubscriptionDTO subscription = subscriptionService
                    .cancelSubscription(subscriptionId, reason);
            return ResponseEntity.ok(subscription);
    }

    @PostMapping("/activate")
    public ResponseEntity<?> activateSubscription(
            @RequestParam Long subscriptionId,
            @RequestParam Long paymentId) throws SubscriptionException {

            SubscriptionDTO subscription = subscriptionService
                    .activateSubscription(subscriptionId, paymentId);
            return ResponseEntity.ok(subscription);
    }



}
