package org.egov.upi.util;

public class PayoutConstants {

    private PayoutConstants() {}

    // Payout Modes
    public static final String MODE_UPI = "UPI";
    public static final String MODE_IMPS = "IMPS";
    public static final String MODE_NEFT = "NEFT";

    // Payout Status
    public static final String STATUS_INITIATED = "INITIATED";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_REVERSED = "REVERSED";

    // Razorpay Status Mapping
    public static final String RZP_STATUS_QUEUED = "queued";
    public static final String RZP_STATUS_PROCESSING = "processing";
    public static final String RZP_STATUS_PROCESSED = "processed";
    public static final String RZP_STATUS_FAILED = "failed";
    public static final String RZP_STATUS_REVERSED = "reversed";
    public static final String RZP_STATUS_CANCELLED = "cancelled";

    // Fund Account Types
    public static final String FUND_ACCOUNT_VPA = "vpa";
    public static final String FUND_ACCOUNT_BANK = "bank_account";

    // Error Codes
    public static final String ERR_INVALID_VPA = "UPI_INVALID_VPA";
    public static final String ERR_INSUFFICIENT_BALANCE = "UPI_INSUFFICIENT_BALANCE";
    public static final String ERR_PAYOUT_NOT_FOUND = "UPI_PAYOUT_NOT_FOUND";
    public static final String ERR_DUPLICATE_REQUEST = "UPI_DUPLICATE_REQUEST";
    public static final String ERR_PROVIDER_ERROR = "UPI_PROVIDER_ERROR";

    // Webhook Events
    public static final String EVENT_PAYOUT_PROCESSED = "payout.processed";
    public static final String EVENT_PAYOUT_FAILED = "payout.failed";
    public static final String EVENT_PAYOUT_REVERSED = "payout.reversed";

    // MDMS Module
    public static final String MDMS_MODULE_NAME = "upi-payout-adapter";
    public static final String MDMS_MASTER_CONFIG = "PayoutConfig";
}
