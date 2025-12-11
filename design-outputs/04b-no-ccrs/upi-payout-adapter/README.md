# UPI Payout Adapter

A DIGIT-compliant adapter for processing UPI-based beneficiary payouts via payment aggregators (Razorpay, PayU, etc.).

## Overview

This adapter enables the Stray Dog Capture & Reporting System (SDCRS) to process teacher payouts via UPI. When a dog report is marked as "CAPTURED", this adapter:

1. Creates a contact and fund account with the payment provider
2. Initiates a UPI payout to the teacher's VPA
3. Handles webhooks for payment status updates
4. Notifies SDCRS when payment completes or fails

## Architecture

```
┌─────────────────┐     ┌───────────────────┐     ┌─────────────────┐
│  SDCRS Service  │────▶│  UPI Payout       │────▶│   Razorpay X    │
│                 │     │  Adapter          │     │   (Payouts)     │
└─────────────────┘     └───────────────────┘     └─────────────────┘
        │                       │                         │
        │                       │                         │
        ▼                       ▼                         ▼
┌─────────────────┐     ┌───────────────────┐     ┌─────────────────┐
│     Kafka       │     │   PostgreSQL      │     │   Webhook       │
│   (Events)      │     │   (Persistence)   │     │   (Callbacks)   │
└─────────────────┘     └───────────────────┘     └─────────────────┘
```

## Payment Flow

```
Dog CAPTURED
     │
     ▼
┌─────────────────────────────────────┐
│ 1. SDCRS pushes to upi-payout-create│
└─────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────┐
│ 2. Adapter creates Contact in       │
│    Razorpay (if not exists)         │
└─────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────┐
│ 3. Adapter creates Fund Account     │
│    (VPA) linked to Contact          │
└─────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────┐
│ 4. Adapter creates Payout           │
│    (UPI transfer initiated)         │
└─────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────┐
│ 5. Razorpay processes payment       │
│    (real-time UPI transfer)         │
└─────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────┐
│ 6. Webhook callback received        │
│    (payout.processed/failed)        │
└─────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────┐
│ 7. SDCRS notified via Kafka         │
│    (Dog Report → RESOLVED)          │
└─────────────────────────────────────┘
```

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/payout/v1/_create` | POST | Create new payout(s) |
| `/payout/v1/_search` | POST | Search payouts |
| `/payout/v1/_retry` | POST | Retry failed payout |
| `/payout/v1/{id}` | GET | Get payout by ID |
| `/webhook/v1/razorpay` | POST | Razorpay webhook handler |
| `/webhook/v1/health` | GET | Webhook health check |

## Kafka Topics

| Topic | Producer | Consumer | Purpose |
|-------|----------|----------|---------|
| `upi-payout-create` | SDCRS | Adapter | Trigger payout |
| `upi-payout-status` | Webhook | Adapter | Status updates |
| `upi-payout-persist` | Adapter | Persister | Database writes |
| `sdcrs-payout-callback` | Adapter | SDCRS | Completion notification |

## Configuration

### Environment Variables

```bash
# Razorpay Credentials
RAZORPAY_KEY_ID=rzp_live_xxxxx
RAZORPAY_KEY_SECRET=xxxxx
RAZORPAY_ACCOUNT_NUMBER=1234567890

# Webhook
UPI_WEBHOOK_SECRET=xxxxx

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=egov_upi
DB_USER=postgres
DB_PASSWORD=postgres

# Kafka
KAFKA_BROKERS=localhost:9092
```

### Razorpay X Setup

1. Enable Razorpay X (Payouts) on your Razorpay dashboard
2. Create a contact and linked bank account for your organization
3. Generate API keys with payout permissions
4. Configure webhook URL: `https://your-domain/upi-payout-adapter/webhook/v1/razorpay`
5. Subscribe to events: `payout.processed`, `payout.failed`, `payout.reversed`

## Payout Status

| Status | Description |
|--------|-------------|
| `INITIATED` | Payout created, queued for processing |
| `PROCESSING` | Payment in progress |
| `COMPLETED` | UPI transfer successful |
| `FAILED` | Payment failed (can retry) |
| `REVERSED` | Payment reversed by bank |

## Building

```bash
# Build
mvn clean install

# Run locally
mvn spring-boot:run

# Docker build
docker build -t upi-payout-adapter:latest .
```

## Database Schema

```sql
-- Main payout table
eg_upi_payout (
  id, tenant_id, payout_number, reference_id,
  beneficiary_id, amount, status, utr, ...
)

-- Fund account (VPA) details
eg_upi_fund_account (
  id, payout_id, vpa, beneficiary_name, ...
)

-- Webhook event log
eg_upi_webhook_log (
  id, event_type, payload, processed, ...
)
```

## Error Handling

- **VPA Validation**: Regex pattern matching for valid UPI addresses
- **Duplicate Detection**: Idempotency key prevents duplicate payouts
- **Auto-Retry**: Failed payouts can be retried up to 3 times
- **DLQ**: Failed Kafka messages pushed to dead-letter queue

## Monitoring

- Health endpoint: `/actuator/health`
- Prometheus metrics: `/actuator/prometheus`
- Structured logging with correlation IDs

## Security

- Webhook signature verification (HMAC-SHA256)
- API key/secret stored as environment variables
- No sensitive data in logs
- TLS for all external API calls

## Dependencies

- Spring Boot 2.7.x
- Razorpay Java SDK 1.4.x
- PostgreSQL 12+
- Apache Kafka 2.8+
- DIGIT Common Libraries

---

**Version**: 1.0.0
**Last Updated**: December 2024
