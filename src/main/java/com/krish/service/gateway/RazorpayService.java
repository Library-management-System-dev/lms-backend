package com.krish.service.gateway;

import com.krish.domain.PaymentType;
import com.krish.modal.Payment;
import com.krish.modal.SubscriptionPlan;
import com.krish.modal.User;
import com.krish.payload.response.PaymentLinkResponse;
import com.krish.service.SubscriptionPlanService;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RazorpayService {

    private final SubscriptionPlanService subscriptionPlanService;

    @Value("${razorpay.key.id:}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:}")
    private String razorpayKeySecret;

    @Value("${razorpay.callback.base-url:http://localhost:5173}")
    private String callbackBaseUrl;

    public PaymentLinkResponse createPaymentLink(User user, Payment payment) {

        try{
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId,
                    razorpayKeySecret);

            Long amountInPaisa = payment.getAmount()*100;

            JSONObject paymentLinkRequest = new JSONObject();
            paymentLinkRequest.put("amount", amountInPaisa);
            paymentLinkRequest.put("currency", "INR");
            paymentLinkRequest.put("description", payment.getDescription());

            JSONObject customer = new JSONObject();
            customer.put("name", user.getFullName());
            customer.put("email", user.getEmail());
            if(user.getPhone()!=null){
                customer.put("contact", user.getPhone());
            }

            paymentLinkRequest.put("customer", customer);

            JSONObject notify = new JSONObject();
            notify.put("email", true);
            notify.put("sms", user.getPhone()!=null);
            paymentLinkRequest.put("notify", notify);

            paymentLinkRequest.put("reminder_enable", true);

            String successUrl = callbackBaseUrl + "/payment-success/" + payment.getId();
            String cancelUrl = callbackBaseUrl + "/payment-cancelled/" + payment.getId();

            paymentLinkRequest.put("callback_url", successUrl);
            paymentLinkRequest.put("callback_method", "get");

            JSONObject notes = new JSONObject();
            notes.put("userId", user.getId());
            notes.put("paymentId", payment.getId());

            if(payment.getPaymentType()== PaymentType.MEMBERSHIP){
                notes.put("subscription_id", payment.getSubscription().getId());
                notes.put("plan", payment.getSubscription().getPlan().getPlanCode());
                notes.put("type",PaymentType.MEMBERSHIP);

            } else if (payment.getPaymentType()==PaymentType.FINE) {
                //todo
               // notes.put("fine_id", payment.getFine().getId());
                notes.put("type",PaymentType.FINE);
            }
            paymentLinkRequest.put("notes", notes);

            PaymentLink paymentLink= razorpayClient.paymentLink.create(paymentLinkRequest);

            String paymentUrl = paymentLink.get("short_url");
            String paymentLinkId = paymentLink.get("id");

            PaymentLinkResponse response = new PaymentLinkResponse();
            response.setPayment_Link_url(paymentUrl);
            response.setPayment_Link_id(paymentLinkId);
            return response;

        }catch (RazorpayException e){
            throw new RuntimeException(e);
        }

    }

    public JSONObject fetchPaymentDetails(String paymentId) throws Exception {
        try{
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            com.razorpay.Payment payment = razorpay.payments.fetch(paymentId);
            return payment.toJson();
        } catch (RazorpayException e){
            log.error("Failed to fetch payment details for paymentId: {}", paymentId, e);
            throw new Exception("Failed to fetch payment details: " + e.getMessage(), e);
        }
    }

    public boolean isValidPayment(String paymentId){
        try{
            JSONObject paymentDetails = fetchPaymentDetails(paymentId);

            String status = paymentDetails.optString("status");
            long amount = paymentDetails.optLong("amount");
            long amountInRupees = amount / 100;

            JSONObject notes = paymentDetails.optJSONObject("notes");
            String  paymentType = notes.optString("type");

            if(!"captured".equalsIgnoreCase(status)){
                return false;
            }

            if(paymentType.equals(PaymentType.MEMBERSHIP.toString())){
                String planCode = notes.optString("plan");
                SubscriptionPlan subscriptionPlan = subscriptionPlanService
                        .getBySubscriptionPlanCode(planCode);

                return amountInRupees == subscriptionPlan.getPrice();
            } else if (paymentType.equals(PaymentType.FINE.toString())) {
                Long fineId = notes.getLong("fine_id");
                //todo
            }

            return false;
        } catch (Exception e){
            log.error("Payment validation failed for paymentId: {}", paymentId, e);
            return false;
        }
    }

}

