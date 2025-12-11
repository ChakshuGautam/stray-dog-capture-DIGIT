# HRMS-SSO-DIGIT Integration

## Overview

This document details the Single Sign-On (SSO) integration between the State HRMS (Human Resource Management System) and DIGIT platform for the Stray Dog Capture & Reporting System (SDCRS).

**Key Principle**: Teachers authenticate using their existing State HRMS credentials. No separate SDCRS registration is required. UPI payment details are sourced directly from HRMS salary records.

---

## Architecture Context

```mermaid
flowchart LR
    subgraph STATE["STATE SYSTEMS"]
        HRMS["STATE HRMS<br/>─────────<br/>• Teachers<br/>• Schools<br/>• UPI IDs"]
    end

    subgraph DIGIT["DIGIT PLATFORM"]
        SDCRS["DIGIT SDCRS<br/>─────────<br/>• Reports<br/>• Workflow<br/>• Payouts"]
    end

    subgraph PAYMENT["PAYMENT"]
        RAZORPAY["PAYMENT GATEWAY<br/>(Razorpay)"]
    end

    HRMS <-->|"SSO + Sync"| SDCRS
    SDCRS <-->|"UPI Payout"| RAZORPAY
```

**HRMS is the SOURCE OF TRUTH for:**
- Teacher identity (Employee ID, Name, Mobile)
- School/District assignment
- UPI VPA for salary disbursement (reused for SDCRS payouts)
- Employment status (Active/Inactive/Transferred)

---

## Sequence Diagram: Teacher Login Flow

### Phase 1 & 2: Initiate Login & HRMS Authentication

```mermaid
sequenceDiagram
    autonumber
    participant T as Teacher<br/>(Mobile)
    participant App as SDCRS Mobile<br/>App (React)
    participant Backend as SDCRS Backend<br/>(API Gateway)
    participant SSO as HRMS SSO<br/>(OAuth2 IdP)
    participant DB as HRMS DB<br/>(PostgreSQL)

    Note over T,DB: PHASE 1: INITIATE LOGIN

    T->>App: Open SDCRS Mobile App
    App->>App: Check for stored token
    App-->>T: Show Login Screen<br/>[Login with State HRMS]
    T->>App: Click "Login with HRMS"
    App->>Backend: GET /sso/v1/auth-url<br/>?redirect_uri=sdcrs://callback

    Note over Backend: Build OAuth2 Authorization URL<br/>• client_id: sdcrs-mobile<br/>• response_type: code<br/>• scope: openid profile employee<br/>• state: random_csrf_token<br/>• code_challenge: PKCE

    Backend-->>App: Return auth URL + state

    Note over T,DB: PHASE 2: HRMS AUTHENTICATION

    T->>SSO: Open HRMS login page (in-app browser)
    SSO-->>T: Render login form

    Note over T: Enter Employee ID: T12345<br/>Enter password or OTP

    T->>SSO: Submit credentials
    SSO->>DB: Validate credentials

    DB->>DB: SELECT * FROM employees<br/>WHERE emp_id = 'T12345'<br/>AND status = 'ACTIVE'

    DB-->>SSO: Employee Record

    Note over SSO: Employee Record:<br/>emp_id: T12345<br/>name: Ravi Kumar<br/>mobile: 9876543210<br/>school_code: DJGHS001<br/>district_code: DJ01<br/>upi_vpa: ravi@okaxis

    SSO->>SSO: Generate auth code + create session
    SSO-->>T: HTTP 302 Redirect<br/>Location: sdcrs://callback<br/>?code=AUTH_CODE_XYZ&state=abc123
```

### Phase 3 & 4: Token Exchange & User Provisioning

```mermaid
sequenceDiagram
    autonumber
    participant T as Teacher<br/>(Mobile)
    participant App as SDCRS Mobile<br/>App (React)
    participant Backend as SDCRS Backend
    participant SSO as HRMS SSO
    participant User as egov-user<br/>(DIGIT)

    Note over T,User: PHASE 3: TOKEN EXCHANGE

    T->>App: Deep link triggers app<br/>(sdcrs://callback?code=...)
    App->>App: Verify state matches (CSRF check)

    App->>Backend: POST /sso/v1/token-exchange<br/>{code, code_verifier, redirect_uri}

    Backend->>SSO: POST /oauth2/token<br/>grant_type: authorization_code<br/>code: AUTH_CODE_XYZ<br/>code_verifier: pkce_val

    SSO->>SSO: Validate code + PKCE
    SSO-->>Backend: HRMS tokens<br/>{access_token, id_token, refresh_token}

    Backend->>Backend: Decode & verify id_token<br/>(JWT signature via JWKS)

    Note over Backend: ID Token Claims:<br/>sub: T12345<br/>name: Ravi Kumar<br/>email: ravi@edu.gov<br/>phone: 9876543210<br/>upi_vpa: ravi@okaxis<br/>school_code: DJGHS001<br/>district_code: DJ01

    Note over T,User: PHASE 4: DIGIT USER PROVISIONING

    Backend->>User: POST /user/v1/_search<br/>{userName: "T12345"}
    User-->>Backend: User search result

    alt User NOT found
        Backend->>User: POST /user/users/_createnovalidate<br/>{userName, name, mobile, tenantId,<br/>roles: [TEACHER],<br/>additionalDetails: {upiId, hrmsEmpId}}
    else User FOUND
        Backend->>User: POST /user/profile/_update<br/>{uuid, additionalDetails: {upiId, lastHrmsSync}}
    end

    User-->>Backend: User created/updated<br/>{id: 24226, uuid: "11t0e02b-...",<br/>userName: "T12345", roles: [TEACHER]}
```

### Phase 5 & 6: DIGIT Token Generation & Login Complete

```mermaid
sequenceDiagram
    autonumber
    participant T as Teacher<br/>(Mobile)
    participant App as SDCRS Mobile<br/>App (React)
    participant Backend as SDCRS Backend
    participant User as egov-user<br/>(DIGIT)

    Note over T,User: PHASE 5: DIGIT TOKEN GENERATION

    Backend->>User: POST /user/oauth/token<br/>grant_type: sso_exchange<br/>userInfo: {DIGIT user}

    User-->>Backend: DIGIT access + refresh tokens

    Note over Backend: DIGIT JWT Payload:<br/>sub: 24226<br/>userInfo: {<br/>  id: 24226,<br/>  uuid: "11t0e02b-...",<br/>  userName: "T12345",<br/>  name: "Ravi Kumar",<br/>  tenantId: "dj.djibouti-ville",<br/>  roles: [{code: "TEACHER"}]<br/>}<br/>expires_in: 604800 (7 days)

    Backend-->>App: Return tokens + user info<br/>{digitToken, refreshToken, user}

    Note over T,User: PHASE 6: LOGIN COMPLETE

    App->>App: Store tokens in secure storage<br/>(Keychain/Keystore)

    App-->>T: Show Teacher Dashboard

    Note over T: Welcome, Ravi Kumar!<br/>Employee ID: T12345<br/>School: Govt High School<br/>─────────────────<br/>[Report Stray Dog]<br/>My Reports: 12<br/>Payouts: ₹4,500
```

---

## Sequence Diagram: Authenticated API Call Flow

```mermaid
sequenceDiagram
    autonumber
    participant App as SDCRS App
    participant Gateway as Zuul Gateway
    participant AC as Access Control
    participant SDCRS as SDCRS Service
    participant User as egov-user

    App->>Gateway: POST /sdcrs/v1/report/_create<br/>Authorization: Bearer DIGIT_JWT<br/>RequestInfo: {userInfo: {...}}

    Gateway->>Gateway: Extract JWT from header
    Gateway->>Gateway: Verify JWT signature (RS256)
    Gateway->>Gateway: Check token expiry
    Gateway->>Gateway: Extract userInfo from JWT

    Gateway->>AC: POST /access/v1/actions/_authorize<br/>{roles: ["TEACHER"],<br/>action: "/sdcrs/v1/report/_create"}

    AC->>AC: Check RBAC mapping in MDMS
    AC-->>Gateway: {authorized: true}

    Gateway->>SDCRS: Forward request with<br/>userInfo in RequestInfo

    SDCRS->>SDCRS: Process request
    SDCRS->>User: Get teacher details for payout
    User-->>SDCRS: Return user + UPI ID
    SDCRS->>SDCRS: Create report

    SDCRS-->>App: Response<br/>{reportNumber: "SDC-2025-001234",<br/>status: "PENDING_VALIDATION",<br/>reporter: {name, empId}}
```

---

## Sequence Diagram: Token Refresh Flow

```mermaid
sequenceDiagram
    autonumber
    participant App as SDCRS App
    participant Backend as SDCRS Backend
    participant User as egov-user

    App->>App: API call fails with 401<br/>(token expired)

    App->>Backend: POST /sso/v1/token-refresh<br/>{refreshToken: "eyJhbGc..."}

    Backend->>Backend: Validate refresh token

    Backend->>User: POST /user/oauth/token<br/>grant_type: refresh_token

    User-->>Backend: New tokens

    Backend-->>App: New access + refresh tokens

    App->>App: Retry original request<br/>with new token
```

---

## Sequence Diagram: Logout Flow

```mermaid
sequenceDiagram
    autonumber
    participant T as Teacher
    participant App as SDCRS App
    participant Backend as SDCRS Backend
    participant SSO as HRMS SSO
    participant User as egov-user

    T->>App: Click Logout

    App->>Backend: POST /sso/v1/logout<br/>{accessToken: "..."}

    Backend->>User: POST /_logout<br/>(invalidate DIGIT session)
    User-->>Backend: Session invalidated

    Backend->>SSO: POST /oauth2/logout<br/>(end HRMS session - optional)
    SSO-->>Backend: HRMS session ended

    Backend-->>App: Logout success

    App->>App: Clear local storage (tokens)

    App-->>T: Show login screen
```

---

## Sequence Diagram: Payout with HRMS-Synced UPI

```mermaid
sequenceDiagram
    autonumber
    participant MC as MC Officer
    participant SDCRS as SDCRS Service
    participant User as egov-user
    participant HRMS as HRMS API
    participant UPI as UPI Adapter
    participant RZP as Razorpay

    MC->>SDCRS: Update status to CAPTURED

    SDCRS->>User: Get teacher profile
    User-->>SDCRS: User record + UPI ID<br/>{uuid, userName: "T12345",<br/>additionalDetails: {<br/>  upiId: "ravi@okaxis",<br/>  lastHrmsSync: "2025-12-09"<br/>}}

    opt If UPI sync > 24h old
        SDCRS->>HRMS: Refresh UPI from HRMS
        HRMS-->>SDCRS: Latest UPI VPA
    end

    SDCRS->>UPI: Kafka: upi-payout-create<br/>{reportId, teacherId,<br/>upiId: "ravi@okaxis",<br/>amount: 500}

    UPI->>RZP: Create contact
    RZP-->>UPI: contact_id

    UPI->>RZP: Create fund account
    RZP-->>UPI: fund_account_id

    UPI->>RZP: Create payout
    RZP-->>UPI: payout_id (queued)

    Note over RZP: UPI transfer executes

    RZP-->>UPI: webhook: payout.processed

    UPI->>SDCRS: Kafka: sdcrs-payout-callback<br/>{reportId, status: "SUCCESS",<br/>utr: "123456789012", amount: 500}

    SDCRS->>SDCRS: Update report status to RESOLVED

    SDCRS-->>MC: Status: RESOLVED

    Note over MC,RZP: TEACHER receives ₹500 in "ravi@okaxis"<br/>(same as salary account)
```

---

## Data Flow Summary

```mermaid
flowchart TB
    subgraph HRMS["STATE HRMS (Source of Truth)"]
        EMP[("Employee Master<br/>─────────────────<br/>T12345 | Ravi Kumar | ravi@okaxis<br/>T12346 | Priya Singh | priya@ybl<br/>T12347 | Ahmed Ali | ahmed@paytm")]
    end

    subgraph DIGIT["DIGIT egov-user (Synced Copy)"]
        USER[("User Table<br/>─────────────────<br/>abc-123 | T12345 | dj.djibouti-ville<br/>def-456 | T12346 | dj.ali-sabieh<br/>ghi-789 | T12347 | dj.djibouti-ville")]
    end

    subgraph SDCRS_REG["SDCRS Dog Report Registry"]
        REPORT[("Dog Report Table<br/>─────────────────<br/>SDC-2025-001 | abc-123 | RESOLVED<br/>SDC-2025-002 | def-456 | IN_PROGRESS<br/>SDC-2025-003 | abc-123 | CAPTURED")]
    end

    EMP -->|"SSO Login /<br/>Batch Sync"| USER
    USER -->|"Authentication +<br/>Payout"| REPORT
```

---

## Complete Login Flow (Combined View)

```mermaid
sequenceDiagram
    autonumber
    participant T as Teacher
    participant App as SDCRS App
    participant Backend as SDCRS Backend
    participant SSO as HRMS SSO
    participant DB as HRMS DB
    participant User as egov-user

    rect rgb(240, 248, 255)
        Note over T,User: Phase 1: Initiate Login
        T->>App: Open app, click "Login with HRMS"
        App->>Backend: GET /sso/v1/auth-url
        Backend-->>App: OAuth2 authorization URL
    end

    rect rgb(255, 248, 240)
        Note over T,User: Phase 2: HRMS Authentication
        T->>SSO: Open HRMS login (in-app browser)
        T->>SSO: Enter Employee ID + Password/OTP
        SSO->>DB: Validate credentials
        DB-->>SSO: Employee record (T12345, ravi@okaxis)
        SSO-->>T: Redirect with auth code
    end

    rect rgb(240, 255, 240)
        Note over T,User: Phase 3: Token Exchange
        T->>App: Auth code callback
        App->>Backend: POST /sso/v1/token-exchange
        Backend->>SSO: Exchange code for tokens
        SSO-->>Backend: HRMS JWT (id_token)
    end

    rect rgb(255, 240, 255)
        Note over T,User: Phase 4: User Provisioning
        Backend->>User: Search/Create DIGIT user
        User-->>Backend: User with TEACHER role + UPI ID
    end

    rect rgb(255, 255, 240)
        Note over T,User: Phase 5-6: DIGIT Token & Complete
        Backend->>User: Generate DIGIT JWT
        User-->>Backend: Access + Refresh tokens
        Backend-->>App: Tokens + user info
        App-->>T: Show Teacher Dashboard
    end
```

---

## Configuration Requirements

### 1. HRMS SSO Configuration (MDMS)

**File**: `data/dj/SSO-CONFIG/HrmsIntegration.json`

```json
{
  "tenantId": "dj",
  "moduleName": "SSO-CONFIG",
  "HrmsIntegration": [
    {
      "provider": "STATE_HRMS",
      "enabled": true,
      "oauth2": {
        "issuer": "https://hrms.state.gov.in",
        "authorizationEndpoint": "https://hrms.state.gov.in/oauth2/authorize",
        "tokenEndpoint": "https://hrms.state.gov.in/oauth2/token",
        "userInfoEndpoint": "https://hrms.state.gov.in/oauth2/userinfo",
        "jwksUri": "https://hrms.state.gov.in/.well-known/jwks.json",
        "clientId": "sdcrs-mobile",
        "scopes": ["openid", "profile", "employee"],
        "pkceEnabled": true
      },
      "fieldMapping": {
        "sub": "userName",
        "name": "name",
        "phone": "mobileNumber",
        "email": "emailId",
        "emp_id": "additionalDetails.hrmsEmpId",
        "upi_vpa": "additionalDetails.upiId",
        "school_code": "additionalDetails.schoolCode",
        "designation": "additionalDetails.designation",
        "district_code": "tenantMapping"
      },
      "districtToTenantMapping": {
        "DJ01": "dj.djibouti-ville",
        "DJ02": "dj.ali-sabieh",
        "DJ03": "dj.dikhil",
        "DJ04": "dj.tadjourah",
        "DJ05": "dj.obock",
        "DJ06": "dj.arta"
      },
      "defaultRole": "TEACHER",
      "tokenValidity": {
        "accessTokenMinutes": 10080,
        "refreshTokenMinutes": 20160
      }
    }
  ]
}
```

### 2. Environment Variables

```bash
# HRMS SSO Integration
HRMS_SSO_CLIENT_ID=sdcrs-mobile
HRMS_SSO_CLIENT_SECRET=<secret_from_hrms_admin>
HRMS_SSO_ISSUER=https://hrms.state.gov.in

# PKCE code verifier storage (Redis)
REDIS_HOST=redis.backbone.svc.cluster.local
REDIS_PORT=6379
```

### 3. Access Control (MDMS)

**File**: `data/dj/ACCESSCONTROL-ROLES/roles.json` (add TEACHER role)

```json
{
  "code": "TEACHER",
  "name": "Teacher",
  "description": "Government school teacher - can report stray dogs",
  "labelKey": "ACCESSCONTROL_ROLES_TEACHER"
}
```

**File**: `data/dj/ACCESSCONTROL-ROLEACTIONS/roleactions.json` (teacher permissions)

```json
{
  "rolecode": "TEACHER",
  "actionid": 2101,
  "actioncode": "sdcrs-report-create",
  "tenantId": "dj"
}
```

---

## Security Considerations

| Aspect | Implementation |
|--------|----------------|
| **PKCE** | Mandatory for mobile app OAuth2 flow |
| **State Parameter** | CSRF protection during redirect |
| **Token Storage** | iOS Keychain / Android Keystore |
| **JWT Validation** | RS256 signature via JWKS endpoint |
| **Token Refresh** | Silent refresh before expiry |
| **Logout** | Invalidate both DIGIT and HRMS sessions |
| **UPI Validation** | Cross-check with HRMS before payout |

---

## Error Handling

| Scenario | User Experience |
|----------|-----------------|
| HRMS unavailable | Show error: "State HRMS is temporarily unavailable. Please try again later." |
| Invalid credentials | Show HRMS error message |
| Employee not found | "Your Employee ID is not registered in HRMS. Contact your school administrator." |
| Inactive employee | "Your HRMS account is inactive. Contact HR department." |
| Token expired | Silent refresh, if fails redirect to login |
| Network error | "Check your internet connection and try again." |

---

## Implementation Checklist

- [ ] Create SSO Adapter service endpoints
- [ ] Configure HRMS OAuth2 client credentials
- [ ] Set up PKCE code verifier storage (Redis)
- [ ] Implement JWT validation via JWKS
- [ ] Create user provisioning logic (create/update)
- [ ] Configure field mappings in MDMS
- [ ] Add district-to-tenant mapping
- [ ] Update frontend for SSO redirect flow
- [ ] Implement token refresh logic
- [ ] Add logout flow (both sessions)
- [ ] Set up monitoring for SSO failures
- [ ] Document rollback procedure

---

*Document Version: 1.1*
*Created: December 2025*
*Updated: Converted to Mermaid diagrams*
*Status: Design Complete*
