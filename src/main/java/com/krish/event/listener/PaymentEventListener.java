package com.krish.event.listener;

import com.krish.exception.SubscriptionException;
import com.krish.modal.Payment;
import com.krish.service.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final SubscriptionService subscriptionService;

    @Async
    @EventListener
    @Transactional
    public void handlePaymentSuccessEvent(Payment payment) throws SubscriptionException {
        switch (payment.getPaymentType()){
            case FINE :
            case LOST_BOOK_PENALTY:
            case DAMAGED_BOOK_PENALTY:
                break;

            case MEMBERSHIP:
                subscriptionService.activateSubscription(payment.getSubscription().getId(),
                        payment.getId());
        }

    }
}
