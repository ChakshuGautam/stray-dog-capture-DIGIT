# T-AUTH-02: Profile Auto-Population

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** have my profile auto-populated from the HRMS database,
**So that** I don't have to manually enter my details.

---

## Description

Upon successful OTP verification, the system should automatically fetch and populate the teacher's profile from the HRMS (Human Resource Management System) database. This includes personal details, school assignment, and administrative hierarchy (district, block). Teachers should not need to manually enter this information.

---

## Acceptance Criteria

### Functional

- [ ] After OTP verification, system fetches teacher profile from HRMS
- [ ] Profile fields auto-populated: Name, Employee ID, School Name, School Code
- [ ] Administrative hierarchy populated: District, Block, Cluster
- [ ] Profile photo fetched from HRMS (if available)
- [ ] Teacher cannot edit HRMS-sourced fields (read-only)
- [ ] Display "Profile synced from HRMS" indicator
- [ ] System shows last sync timestamp
- [ ] Profile refresh button to manually trigger HRMS sync
- [ ] If HRMS fetch fails, show cached profile with "Offline" indicator
- [ ] New teachers (first login) see welcome screen after profile load

### Data Fields

| Field | Source | Editable |
|-------|--------|----------|
| Full Name | HRMS | No |
| Employee ID | HRMS | No |
| Mobile Number | HRMS | No |
| Email | HRMS | No |
| School Name | HRMS | No |
| School Code | HRMS | No |
| District | HRMS | No |
| Block | HRMS | No |
| Designation | HRMS | No |
| Profile Photo | HRMS | No |
| Bank Account | User Input | Yes (via eKYC) |
| UPI ID | User Input | Yes |

---

## UI/UX Requirements (PWA)

### Profile Screen

```
┌─────────────────────────────────┐
│  ← Profile          [Refresh]   │
├─────────────────────────────────┤
│      ┌─────────┐                │
│      │  Photo  │                │
│      └─────────┘                │
│     Ramesh Kumar                │
│     EMP-2026-00123              │
│                                 │
│  ─────────────────────────────  │
│  Personal Information           │
│  ─────────────────────────────  │
│  📱 Mobile    +91 98765 43210   │
│  📧 Email     ramesh@edu.gov.in │
│                                 │
│  ─────────────────────────────  │
│  School Information             │
│  ─────────────────────────────  │
│  🏫 School    Govt. High School │
│  📍 District  Djibouti City     │
│  📍 Block     Central           │
│                                 │
│  ─────────────────────────────  │
│  Payment Information            │
│  ─────────────────────────────  │
│  🏦 Bank      [Link via eKYC →] │
│                                 │
│  ─────────────────────────────  │
│   Profile synced from HRMS      │
│   Last updated: 2 hours ago     │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Cache profile data in IndexedDB for offline access
- [ ] Show skeleton loader while fetching profile
- [ ] Pull-to-refresh gesture to sync profile
- [ ] Display sync status indicator in header
- [ ] Graceful degradation if HRMS unavailable

---

## Technical Notes

### API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/user/profile` | GET | Get current user profile |
| `/user/profile/sync` | POST | Force sync with HRMS |

### HRMS Integration

```json
GET /hrms/employees/_search
{
  "RequestInfo": { ... },
  "criteria": {
    "mobileNumber": "9876543210",
    "tenantId": "dj.djibouti"
  }
}
```

### Response Mapping

| HRMS Field | SDCRS Field |
|------------|-------------|
| `user.name` | `name` |
| `user.mobileNumber` | `mobile` |
| `assignments[0].department` | `school_name` |
| `assignments[0].designation` | `designation` |
| `user.tenantId` | `district` |

### Caching Strategy

- Profile cached in IndexedDB with 24-hour TTL
- Background sync on app launch if cache > 1 hour old
- Force sync on user request (refresh button)
- Stale-while-revalidate pattern for profile display

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| HRMS API | External | Source of teacher profile data |
| egov-user service | DIGIT | User profile management |
| IndexedDB | Browser | Offline profile caching |

---

## Related Stories

- [T-AUTH-01](./T-AUTH-01.md) - OTP Login
- [T-AUTH-03](./T-AUTH-03.md) - Bank account linkage via Aadhaar eKYC
