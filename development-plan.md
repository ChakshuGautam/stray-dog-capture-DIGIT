# Software Development Plan

This document outlines the implementation roadmap for SDCRS.

---

## Phase 1: Foundation (Weeks 1-2)

### Infrastructure
- [ ] Kubernetes cluster setup
- [ ] Kafka topics creation
- [ ] PostgreSQL database provisioning
- [ ] Elasticsearch cluster setup

### Configuration
- [ ] MDMS data deployment
- [ ] Database schema creation ([eg_sdcrs_report.sql](./design-outputs/03-configs/database/eg_sdcrs_report.sql))
- [ ] Persister config ([sdcrs-persister.yml](./design-outputs/03-configs/persister/sdcrs-persister.yml))
- [ ] Indexer config ([sdcrs-indexer.yml](./design-outputs/03-configs/indexer/sdcrs-indexer.yml))

### Service Scaffold
- [ ] Basic SDCRS service structure
- [ ] Health check endpoints
- [ ] DIGIT service integration (User, MDMS, Workflow)

---

## Phase 2: Core Features (Weeks 3-5)

### APIs
- [ ] Report Create API (`/_create`)
- [ ] Report Update API (`/_update`)
- [ ] Report Search API (`/_search`)
- [ ] Public Track API (`/_track`)

### Integrations
- [ ] Workflow Service integration
- [ ] File Store Service integration (photo upload)
- [ ] Location Service integration (GPS validation)
- [ ] Notification Service integration

### Verification Flow
- [ ] Verifier queue and assignment
- [ ] Approve/Reject/Duplicate actions
- [ ] SLA tracking

---

## Phase 3: Fraud & Payout (Weeks 6-7)

### Fraud Detection
- [ ] Fraud detection service deployment
- [ ] Internal rules implementation (22 rules)
- [ ] External validator integration (12 rules)
- [ ] Risk scoring engine
- [ ] Penalty management

### Duplicate Detection
- [ ] pHash library integration
- [ ] Image hash storage (Redis)
- [ ] Similarity threshold configuration

### UPI Payout
- [ ] UPI Payout Adapter deployment
- [ ] Razorpay X integration
- [ ] Webhook handler
- [ ] Retry mechanism

---

## Phase 4: Dashboards & Polish (Week 8)

### Dashboards
- [ ] DSS configuration deployment
- [ ] Elasticsearch index templates
- [ ] Dashboard role mappings

### Notifications
- [ ] SMS templates
- [ ] Email templates
- [ ] Event triggers

### Public Access
- [ ] URL Shortener integration
- [ ] Gateway whitelist for `/_track`
- [ ] Rate limiting configuration

---

## POCs Required

| POC | Purpose | Priority |
|-----|---------|----------|
| **Image Hash Comparison** | pHash library performance testing | High |
| **UPI Payout** | Razorpay X sandbox integration | High |
| **External ML APIs** | Object detection service integration | Medium |

---

## Key Deliverables by Phase

| Phase | Deliverables |
|-------|--------------|
| 1 | Running service with health checks, DB schema deployed |
| 2 | Full CRUD APIs, workflow integration, verification flow |
| 3 | Fraud detection, duplicate detection, UPI payouts |
| 4 | Dashboards, notifications, public tracking |

---

## Dependencies

### External Services
- Razorpay X account (UPI payouts)
- SMS gateway credentials
- Object detection API (optional)

### DIGIT Services
- User Service
- Workflow Service
- File Store Service
- Location Service
- MDMS Service
- Persister Service
- Indexer Service
- DSS Service

---

## Team Requirements

| Role | Count | Responsibilities |
|------|-------|------------------|
| Backend Developer | 2 | SDCRS service, Fraud detection, UPI adapter |
| DevOps | 1 | Infrastructure, CI/CD, monitoring |
| QA | 1 | API testing, integration testing |
| Frontend Developer | 1 | Dashboard integration (if custom UI) |

---

## Risk Mitigation

| Risk | Mitigation |
|------|------------|
| Razorpay X approval delay | Start sandbox integration in Phase 1 |
| pHash performance issues | Early POC, fallback to simpler hashing |
| DIGIT service compatibility | Test against target DIGIT version early |
| Scale testing | Load test in Phase 3 before dashboards |

---

## Related Documents

- [Service Design](./design-outputs/03-service-design.md)
- [API Specification](./design-outputs/04b-no-ccrs/06-api-specification.yaml)
- [Scale Management](./design-outputs/07-scale-management.md)
- [Configuration Files](./design-outputs/03-configs/)
