package com.krish.service;

import com.krish.exception.SubscriptionException;
import com.krish.payload.dto.SubscriptionDTO;

import com.krish.payload.response.PaymentInitiateResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SubscriptionService {

    PaymentInitiateResponse subscribe(SubscriptionDTO subscriptionDTO) throws Exception;

    SubscriptionDTO getUsersActiveSubscription(Long userId) throws Exception;

    SubscriptionDTO cancelSubscription(Long subscriptionId, String reason) throws SubscriptionException;

    SubscriptionDTO activateSubscription(Long subscriptionId, Long paymentId) throws SubscriptionException;

    List<SubscriptionDTO> getAllSubscriptions(Pageable pageable);

    void deactivateExpiredSubscriptions(Long userId) throws Exception;


}
