# T-AUTH-01: OTP Login

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** log in using OTP sent to my registered mobile number,
**So that** I can securely access the application without remembering passwords.

---

## Description

Teachers should be able to authenticate using their mobile number registered in the HRMS database. The system sends a one-time password (OTP) via SMS, which the teacher enters to complete authentication. This eliminates password management overhead and leverages the existing verified mobile number.

---

## Acceptance Criteria

### Functional

- [ ] User can enter their 10-digit mobile number on the login screen
- [ ] System validates mobile number format before sending OTP
- [ ] System checks if mobile number exists in HRMS database
- [ ] If mobile not found, display error: "Mobile number not registered. Contact your school administrator."
- [ ] OTP is sent via SMS within 10 seconds of request
- [ ] OTP is 6 digits and valid for 5 minutes
- [ ] User can request OTP resend after 30-second cooldown
- [ ] Maximum 5 OTP requests per hour per mobile number
- [ ] After 3 failed OTP attempts, account is temporarily locked for 15 minutes
- [ ] Successful OTP verification creates a session with JWT token
- [ ] Session remains valid for 30 days with refresh token mechanism

### Error Handling

- [ ] Display clear error message for invalid mobile number format
- [ ] Display clear error message for unregistered mobile number
- [ ] Display clear error message for incorrect OTP
- [ ] Display clear error message for expired OTP
- [ ] Display remaining attempts after each failed OTP entry
- [ ] Display countdown timer for OTP resend

---

## UI/UX Requirements (PWA)

### Login Screen

```
┌─────────────────────────────────┐
│         SDCRS Logo              │
│    Stray Dog Capture System     │
│                                 │
│  ┌───────────────────────────┐  │
│  │ Enter Mobile Number       │  │
│  │ +91 [__________]          │  │
│  └───────────────────────────┘  │
│                                 │
│  [    Send OTP    ]             │
│                                 │
│  ─────────────────────────────  │
│  Need help? Contact support     │
└─────────────────────────────────┘
```

### OTP Entry Screen

```
┌─────────────────────────────────┐
│         ← Back                  │
│                                 │
│  OTP sent to +91 98XXX XXX45    │
│                                 │
│  Enter 6-digit OTP              │
│  ┌─┐ ┌─┐ ┌─┐ ┌─┐ ┌─┐ ┌─┐        │
│  │ │ │ │ │ │ │ │ │ │ │ │        │
│  └─┘ └─┘ └─┘ └─┘ └─┘ └─┘        │
│                                 │
│  [    Verify OTP    ]           │
│                                 │
│  Resend OTP in 0:28             │
│  [Resend OTP] (disabled)        │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Auto-focus on mobile number input field on page load
- [ ] Numeric keyboard for mobile number and OTP inputs
- [ ] Auto-advance to next OTP digit box on input
- [ ] Auto-submit when 6th OTP digit entered
- [ ] SMS autofill support (WebOTP API) for supported browsers
- [ ] "Remember this device" option for trusted devices
- [ ] Offline indicator if network unavailable
- [ ] Loading spinner during OTP send/verify

---

## Technical Notes

### API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/auth/otp/request` | POST | Request OTP for mobile number |
| `/auth/otp/verify` | POST | Verify OTP and get JWT token |
| `/auth/token/refresh` | POST | Refresh expired access token |

### Request/Response Examples

**Request OTP:**
```json
POST /auth/otp/request
{
  "mobile": "9876543210",
  "tenantId": "dj.djibouti"
}
```

**Verify OTP:**
```json
POST /auth/otp/verify
{
  "mobile": "9876543210",
  "otp": "123456",
  "tenantId": "dj.djibouti"
}
```

**Response:**
```json
{
  "access_token": "eyJhbGc...",
  "refresh_token": "dGhpcyBp...",
  "expires_in": 3600,
  "token_type": "Bearer",
  "user": {
    "id": "uuid",
    "name": "Teacher Name",
    "mobile": "9876543210"
  }
}
```

### Security Considerations

- OTP should be hashed before storage
- Rate limiting on OTP requests to prevent SMS bombing
- JWT tokens should use RS256 algorithm
- Refresh tokens should be rotated on use
- Device fingerprinting for suspicious login detection

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| HRMS Integration | External | Validate mobile number against teacher database |
| SMS Gateway | External | Send OTP via SMS |
| egov-user service | DIGIT | User authentication and session management |

---

## Related Stories

- [T-AUTH-02](./T-AUTH-02.md) - Profile auto-population from HRMS
- [T-AUTH-03](./T-AUTH-03.md) - Bank account linkage via Aadhaar eKYC
