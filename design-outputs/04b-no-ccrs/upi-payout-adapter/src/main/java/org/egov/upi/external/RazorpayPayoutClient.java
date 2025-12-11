package org.egov.upi.external;

import com.razorpay.Contact;
import com.razorpay.FundAccount;
import com.razorpay.Payout;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.egov.upi.config.UpiAdapterConfiguration;
import org.egov.upi.util.PayoutConstants;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@Slf4j
public class RazorpayPayoutClient {

    private final RazorpayClient razorpayClient;
    private final UpiAdapterConfiguration config;

    @Autowired
    public RazorpayPayoutClient(RazorpayClient razorpayClient, UpiAdapterConfiguration config) {
        this.razorpayClient = razorpayClient;
        this.config = config;
    }

    /**
     * Create a contact (beneficiary) in Razorpay
     */
    public String createContact(String name, String email, String mobile, String referenceId) {
        try {
            JSONObject contactRequest = new JSONObject();
            contactRequest.put("name", name);
            contactRequest.put("email", email);
            contactRequest.put("contact", mobile);
            contactRequest.put("type", "employee");
            contactRequest.put("reference_id", referenceId);

            Contact contact = razorpayClient.contacts.create(contactRequest);
            log.info("Created Razorpay contact: {}", contact.get("id"));
            return contact.get("id");
        } catch (RazorpayException e) {
            log.error("Error creating Razorpay contact: {}", e.getMessage());
            throw new CustomException(PayoutConstants.ERR_PROVIDER_ERROR,
                "Failed to create contact: " + e.getMessage());
        }
    }

    /**
     * Create a fund account (VPA) linked to a contact
     */
    public String createFundAccount(String contactId, String vpa, String beneficiaryName) {
        try {
            JSONObject fundAccountRequest = new JSONObject();
            fundAccountRequest.put("contact_id", contactId);
            fundAccountRequest.put("account_type", "vpa");

            JSONObject vpaDetails = new JSONObject();
            vpaDetails.put("address", vpa);
            fundAccountRequest.put("vpa", vpaDetails);

            FundAccount fundAccount = razorpayClient.fundAccount.create(fundAccountRequest);
            log.info("Created Razorpay fund account: {}", fundAccount.get("id"));
            return fundAccount.get("id");
        } catch (RazorpayException e) {
            log.error("Error creating Razorpay fund account: {}", e.getMessage());
            throw new CustomException(PayoutConstants.ERR_PROVIDER_ERROR,
                "Failed to create fund account: " + e.getMessage());
        }
    }

    /**
     * Create a payout to a fund account
     */
    public PayoutResult createPayout(String fundAccountId, BigDecimal amount, String mode,
                                      String purpose, String narration, String referenceId,
                                      Map<String, String> notes) {
        try {
            JSONObject payoutRequest = new JSONObject();
            payoutRequest.put("account_number", getAccountNumber());
            payoutRequest.put("fund_account_id", fundAccountId);
            payoutRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue()); // Convert to paise
            payoutRequest.put("currency", config.getDefaultCurrency());
            payoutRequest.put("mode", mode);
            payoutRequest.put("purpose", purpose);
            payoutRequest.put("queue_if_low_balance", true);
            payoutRequest.put("reference_id", referenceId);
            payoutRequest.put("narration", narration);

            if (notes != null && !notes.isEmpty()) {
                JSONObject notesJson = new JSONObject(notes);
                payoutRequest.put("notes", notesJson);
            }

            Payout payout = razorpayClient.payouts.create(payoutRequest);

            PayoutResult result = new PayoutResult();
            result.setPayoutId(payout.get("id"));
            result.setStatus(payout.get("status"));
            result.setUtr(payout.has("utr") ? payout.get("utr") : null);
            result.setFees(payout.has("fees") ? ((Number) payout.get("fees")).longValue() : 0L);

            log.info("Created Razorpay payout: {} with status: {}", result.getPayoutId(), result.getStatus());
            return result;

        } catch (RazorpayException e) {
            log.error("Error creating Razorpay payout: {}", e.getMessage());
            throw new CustomException(PayoutConstants.ERR_PROVIDER_ERROR,
                "Failed to create payout: " + e.getMessage());
        }
    }

    /**
     * Fetch payout details by ID
     */
    public PayoutResult fetchPayout(String payoutId) {
        try {
            Payout payout = razorpayClient.payouts.fetch(payoutId);

            PayoutResult result = new PayoutResult();
            result.setPayoutId(payout.get("id"));
            result.setStatus(payout.get("status"));
            result.setUtr(payout.has("utr") ? payout.get("utr") : null);
            result.setFees(payout.has("fees") ? ((Number) payout.get("fees")).longValue() : 0L);
            result.setFailureReason(payout.has("failure_reason") ? payout.get("failure_reason") : null);

            return result;
        } catch (RazorpayException e) {
            log.error("Error fetching Razorpay payout {}: {}", payoutId, e.getMessage());
            throw new CustomException(PayoutConstants.ERR_PROVIDER_ERROR,
                "Failed to fetch payout: " + e.getMessage());
        }
    }

    /**
     * Cancel a queued payout
     */
    public void cancelPayout(String payoutId) {
        try {
            razorpayClient.payouts.cancel(payoutId);
            log.info("Cancelled Razorpay payout: {}", payoutId);
        } catch (RazorpayException e) {
            log.error("Error cancelling Razorpay payout {}: {}", payoutId, e.getMessage());
            throw new CustomException(PayoutConstants.ERR_PROVIDER_ERROR,
                "Failed to cancel payout: " + e.getMessage());
        }
    }

    private String getAccountNumber() {
        // This should come from configuration - the Razorpay X account number
        return System.getenv("RAZORPAY_ACCOUNT_NUMBER");
    }

    @lombok.Data
    public static class PayoutResult {
        private String payoutId;
        private String status;
        private String utr;
        private Long fees;
        private String failureReason;
    }
}
