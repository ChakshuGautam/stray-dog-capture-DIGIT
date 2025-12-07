Q# T-AUTH-03: Bank Account Linkage via Aadhaar eKYC

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** link my bank account via Aadhaar eKYC,
**So that** I can receive payouts directly to my verified bank account.

---

## Description

Teachers must link a verified bank account to receive payouts. The system uses Aadhaar-based eKYC to verify identity and fetch bank account details linked to the teacher's Aadhaar. This ensures payouts go to the correct, verified account and prevents fraud.

---

## Acceptance Criteria

### Functional

- [ ] Teacher can initiate bank account linkage from Profile screen
- [ ] System displays Aadhaar consent screen before proceeding
- [ ] Teacher enters 12-digit Aadhaar number
- [ ] System validates Aadhaar number format (Verhoeff checksum)
- [ ] OTP sent to Aadhaar-linked mobile number
- [ ] Teacher enters Aadhaar OTP to complete verification
- [ ] System fetches bank account details from Aadhaar (if linked)
- [ ] If no bank linked to Aadhaar, allow manual entry with NPCI validation
- [ ] Display masked bank account number after successful linkage
- [ ] Teacher can update bank account (requires re-verification)
- [ ] Bank account change triggers admin notification for audit

### Verification Flow

1. Teacher initiates eKYC
2. Consent screen displayed (UIDAI terms)
3. Teacher enters Aadhaar number
4. OTP sent to Aadhaar-linked mobile
5. Teacher enters OTP
6. System fetches eKYC data
7. Bank account details displayed for confirmation
8. Teacher confirms → Account linked

### Error Handling

- [ ] Invalid Aadhaar format → Show validation error
- [ ] Aadhaar OTP expired → Allow resend
- [ ] Aadhaar OTP mismatch → Show error, allow retry (max 3)
- [ ] No bank linked to Aadhaar → Prompt manual entry
- [ ] NPCI validation failed → Show "Invalid account" error
- [ ] eKYC service unavailable → Show retry option

---

## UI/UX Requirements (PWA)

### Initiate eKYC Screen

```
┌─────────────────────────────────┐
│  ← Link Bank Account            │
├─────────────────────────────────┤
│                                 │
│  🏦 Link your bank account      │
│                                 │
│  To receive payouts, please     │
│  verify your identity using     │
│  Aadhaar eKYC.                  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ Aadhaar Number            │  │
│  │ [____________]            │  │
│  └───────────────────────────┘  │
│                                 │
│  ☑ I consent to share my       │
│    Aadhaar details for eKYC    │
│    verification as per UIDAI   │
│    guidelines.                  │
│                                 │
│  [    Verify with OTP    ]      │
│                                 │
└─────────────────────────────────┘
```

### Bank Confirmation Screen

```
┌─────────────────────────────────┐
│  ← Confirm Bank Account         │
├─────────────────────────────────┤
│                                 │
│  ✓ Aadhaar Verified             │
│                                 │
│  Bank account found:            │
│                                 │
│  ┌───────────────────────────┐  │
│  │ 🏦 State Bank of India    │  │
│  │    A/C: XXXXXX1234        │  │
│  │    IFSC: SBIN0001234      │  │
│  └───────────────────────────┘  │
│                                 │
│  Is this your account?          │
│                                 │
│  [  Yes, Link This  ]           │
│  [  No, Enter Manually  ]       │
│                                 │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Secure input field for Aadhaar (no autocomplete)
- [ ] Mask Aadhaar number after entry (show last 4 digits)
- [ ] Clear sensitive data from memory after use
- [ ] No caching of Aadhaar or bank details
- [ ] HTTPS-only for all eKYC API calls
- [ ] Show security indicators during verification

---

## Technical Notes

### API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/ekyc/aadhaar/otp/request` | POST | Request Aadhaar OTP |
| `/ekyc/aadhaar/otp/verify` | POST | Verify OTP and fetch eKYC |
| `/user/bank-account` | POST | Link bank account |
| `/user/bank-account` | PUT | Update bank account |

### eKYC Integration

```json
POST /ekyc/aadhaar/otp/request
{
  "aadhaarNumber": "123456789012",
  "consent": true,
  "tenantId": "dj.djibouti"
}
```

### NPCI Bank Validation

For manual bank entry, validate using NPCI's Account Validation API:
- Verify account number + IFSC combination
- Confirm account holder name matches eKYC name
- Flag mismatch for manual review

### Security Considerations

- Aadhaar number encrypted in transit and at rest
- eKYC data not stored; only bank account details retained
- Bank account changes logged with old/new values
- Admin notification on bank account change
- Rate limiting on eKYC requests (3 per day per user)

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| UIDAI eKYC API | External | Aadhaar verification |
| NPCI API | External | Bank account validation |
| egov-user service | DIGIT | Store bank account details |

---

## Related Stories

- [T-AUTH-01](./T-AUTH-01.md) - OTP Login
- [T-AUTH-02](./T-AUTH-02.md) - Profile auto-population
- [T-PAY-02](./T-PAY-02.md) - Payout history
