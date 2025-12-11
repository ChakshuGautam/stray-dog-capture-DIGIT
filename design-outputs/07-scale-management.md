# SDCRS Scale Management Guide

## Overview

This document identifies potential system hotspots in the Stray Dog Capture & Reporting System (SDCRS) and provides mitigations to ensure the system can scale effectively under high load.

**Target Scale:**
- 50,000+ teachers as reporters
- 500+ MC Officers in the field
- 100+ Verifiers processing reports
- Peak load: 10,000 reports/day during campaigns

---

## Table of Contents

1. [Load Profile Analysis](#1-load-profile-analysis)
2. [Hotspot #1: Photo Upload & Processing](#2-hotspot-1-photo-upload--processing)
3. [Hotspot #2: Duplicate Detection](#3-hotspot-2-duplicate-detection)
4. [Hotspot #3: Verification Queue Bottleneck](#4-hotspot-3-verification-queue-bottleneck)
5. [Hotspot #4: Kafka Consumer Lag](#5-hotspot-4-kafka-consumer-lag)
6. [Hotspot #5: Database Write Contention](#6-hotspot-5-database-write-contention)
7. [Hotspot #6: Elasticsearch Indexing Lag](#7-hotspot-6-elasticsearch-indexing-lag)
8. [Hotspot #7: Public Tracking API Abuse](#8-hotspot-7-public-tracking-api-abuse)
9. [Hotspot #8: Payout Processing Spikes](#9-hotspot-8-payout-processing-spikes)
10. [Hotspot #9: Notification Gateway Limits](#10-hotspot-9-notification-gateway-limits)
11. [Hotspot #10: Auto-Validation Bottleneck](#11-hotspot-10-auto-validation-bottleneck)
12. [Infrastructure Recommendations](#12-infrastructure-recommendations)
13. [Monitoring & Alerting](#13-monitoring--alerting)
14. [Capacity Planning](#14-capacity-planning)

---

## 1. Load Profile Analysis

### 1.1 Traffic Patterns

```
Reports per Day Timeline (Expected)
═══════════════════════════════════════════════════════════

       Peak: 8AM-2PM (School Hours)
         ▲
    1000 │    ╭────────╮
         │   ╱          ╲
     800 │  ╱            ╲
         │ ╱              ╲
     600 │╱                ╲
         │                  ╲
     400 │                   ╲
         │                    ╲╭──╮
     200 │                    ╰╯  ╲
         │                        ╲
       0 ├──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──▶
         6  8  10 12 14 16 18 20 22 24  2  4  Hour

Legend: Reports submitted per hour
Peak: ~1,000 reports/hour (8AM-2PM)
Off-peak: ~50-200 reports/hour
```

### 1.2 User Distribution

| Role | Count | Concurrent Peak | Primary Activity |
|------|-------|-----------------|------------------|
| Teachers | 50,000 | 5,000 | Report submission (8AM-2PM) |
| Verifiers | 100 | 50 | Queue processing (8AM-6PM) |
| MC Officers | 500 | 200 | Field updates (9AM-5PM) |
| MC Supervisors | 50 | 20 | Assignment & monitoring |
| Public Tracking | - | 1,000 | Anonymous status checks |

### 1.3 Request Volume Estimates

| Endpoint | Daily Requests | Peak RPS | Data Size |
|----------|----------------|----------|-----------|
| `/_create` | 10,000 | 5 | 2KB + 2x2MB photos |
| `/_update` | 30,000 | 10 | 1KB |
| `/_search` | 100,000 | 50 | Response: 10-50KB |
| `/_track` | 50,000 | 100 | 2KB |
| File uploads | 20,000 | 10 | 2-5MB each |

---

## 2. Hotspot #1: Photo Upload & Processing

### 2.1 Problem Description

Each report requires 2 photos (dog photo + selfie), each 2-5MB. Processing includes:
- File storage (FileStore Service)
- EXIF metadata extraction (GPS, timestamp)
- Image hash calculation (pHash for duplicates)
- GPS coordinate validation

**Bottleneck:** Synchronous image processing during `/_create` API call.

### 2.2 Impact

| Metric | Without Mitigation | With Mitigation |
|--------|-------------------|-----------------|
| Create API latency | 3-8 seconds | 500ms |
| Timeout risk | High (30% at peak) | Low (<1%) |
| Server CPU | 100% spike | Distributed |
| User experience | Frustrating | Smooth |

### 2.3 Mitigations

#### M1.1: Pre-upload Photos via FileStore

```
Current Flow (Synchronous):
┌──────────┐    ┌──────────┐    ┌───────────┐
│ Teacher  │───▶│ Create   │───▶│ FileStore │
│ App      │    │ Report   │    │ (Upload)  │
└──────────┘    └────┬─────┘    └───────────┘
                     │
                     ▼ Wait 3-8s
              ┌─────────────┐
              │ Process     │
              │ EXIF/Hash   │
              └─────────────┘

Improved Flow (Async):
┌──────────┐    ┌───────────┐    FileStore IDs
│ Teacher  │───▶│ FileStore │───────────────┐
│ App      │    │ (Upload)  │               │
└──────────┘    └───────────┘               │
                                            ▼
                ┌──────────┐    ┌─────────────┐
                │ Create   │◀──│ Create API  │
                │ Report   │    │ (validate   │
                └────┬─────┘    │ fileStoreIds)
                     │          └─────────────┘
                     ▼ Async
              ┌─────────────┐
              │ Kafka:      │
              │ Process     │
              │ EXIF/Hash   │
              └─────────────┘
```

**Implementation:**
1. Mobile app uploads photos to FileStore first (returns `fileStoreId`)
2. Create API only validates `fileStoreId` exists
3. EXIF extraction & hash calculation via Kafka consumer

#### M1.2: Resize Images Client-Side

```javascript
// Mobile app: Resize before upload
const MAX_DIMENSION = 1920; // pixels
const QUALITY = 0.8;        // 80% JPEG quality

async function preparePhoto(photo) {
  const resized = await ImageResizer.resize({
    image: photo,
    maxWidth: MAX_DIMENSION,
    maxHeight: MAX_DIMENSION,
    quality: QUALITY
  });
  return resized; // 200-500KB instead of 2-5MB
}
```

**Impact:** 80% reduction in upload size (2MB → 400KB average)

#### M1.3: CDN for File Storage

```yaml
# FileStore with CDN integration
filestore:
  storage:
    type: s3
    bucket: sdcrs-evidence
  cdn:
    enabled: true
    provider: cloudfront
    cache-control: "public, max-age=31536000"
    signed-urls: true
    expires: 3600  # 1 hour signed URL expiry
```

---

## 3. Hotspot #2: Duplicate Detection

### 3.1 Problem Description

Image hash (pHash) comparison against existing reports to detect duplicate submissions.

**Current Flow:**
1. Calculate pHash of new photo
2. Query database: `SELECT * FROM eg_sdcrs_report WHERE image_hash = ?`
3. If exact match → reject as duplicate
4. If similar (Hamming distance < 10) → flag for review

**Bottleneck:** Full table scan as database grows (1M+ records).

### 3.2 Impact

| Records | Query Time (No Index) | Query Time (With Index) |
|---------|----------------------|------------------------|
| 10,000 | 50ms | 5ms |
| 100,000 | 500ms | 8ms |
| 1,000,000 | 5s | 12ms |
| 10,000,000 | 50s | 20ms |

### 3.3 Mitigations

#### M2.1: B-tree Index on image_hash

```sql
-- Already defined in schema
CREATE INDEX idx_sdcrs_image_hash ON eg_sdcrs_report(image_hash);
```

#### M2.2: Hash Prefix Bucketing

Store first 8 characters as separate column for faster filtering:

```sql
ALTER TABLE eg_sdcrs_report ADD COLUMN image_hash_prefix VARCHAR(8);
CREATE INDEX idx_sdcrs_hash_prefix ON eg_sdcrs_report(image_hash_prefix);

-- Query: First filter by prefix, then exact match
SELECT * FROM eg_sdcrs_report
WHERE image_hash_prefix = LEFT(:hash, 8)
  AND image_hash = :hash;
```

#### M2.3: Bloom Filter for Fast Rejection

```java
// In-memory Bloom filter for quick "definitely not duplicate" check
public class DuplicateDetector {
    private BloomFilter<String> bloomFilter;

    public DuplicateDetector(int expectedInsertions) {
        this.bloomFilter = BloomFilter.create(
            Funnels.stringFunnel(Charset.defaultCharset()),
            expectedInsertions,
            0.01  // 1% false positive rate
        );
    }

    public boolean mightBeDuplicate(String imageHash) {
        return bloomFilter.mightContain(imageHash);
    }

    public void addHash(String imageHash) {
        bloomFilter.put(imageHash);
    }
}
```

**Flow with Bloom Filter:**
```
                    ┌─────────────┐
                    │ New Image   │
                    │ Hash: ABC.. │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │ Bloom Filter│
                    │ Check       │
                    └──────┬──────┘
                           │
              ┌────────────┴────────────┐
              │                         │
        Not in Bloom               Might be in Bloom
        (Definitely new)           (Check DB)
              │                         │
              ▼                         ▼
        ┌───────────┐           ┌───────────────┐
        │ Accept    │           │ DB Query      │
        │ (Skip DB) │           │ Confirm dup   │
        └───────────┘           └───────────────┘
```

#### M2.4: Redis Cache for Recent Hashes

```yaml
# Cache last 100K hashes (covers 10 days at 10K/day)
redis:
  duplicate-cache:
    ttl: 864000  # 10 days in seconds
    max-entries: 100000
```

```java
public boolean isDuplicate(String imageHash) {
    // 1. Check Redis cache first
    if (redisTemplate.hasKey("hash:" + imageHash)) {
        return true;
    }

    // 2. Bloom filter check
    if (!bloomFilter.mightContain(imageHash)) {
        return false;
    }

    // 3. Database check (only if Bloom filter positive)
    boolean exists = repository.existsByImageHash(imageHash);
    if (exists) {
        redisTemplate.opsForValue().set("hash:" + imageHash, "1", 10, TimeUnit.DAYS);
    }
    return exists;
}
```

---

## 4. Hotspot #3: Verification Queue Bottleneck

### 4.1 Problem Description

All validated reports queue for human verification. With 100 verifiers and 10,000 daily reports, each verifier handles ~100 reports/day.

**Bottleneck:** Single FIFO queue causes:
- No geographic locality (verifier sees reports from all areas)
- No load balancing (some verifiers overloaded)
- No priority handling (aggressive dogs treated same as standard)

### 4.2 Impact

| Scenario | Queue Depth | Avg Wait Time | SLA Breach |
|----------|-------------|---------------|------------|
| Normal (5K/day) | 200 | 2 hours | 5% |
| Peak (10K/day) | 1,000 | 8 hours | 25% |
| Campaign (20K/day) | 5,000 | 24+ hours | 60% |

### 4.3 Mitigations

#### M3.1: Priority Queue by Report Type

```java
public class VerificationQueue {
    private PriorityQueue<Report> queue = new PriorityQueue<>(
        Comparator.comparing(Report::getPriority)
                  .thenComparing(Report::getCreatedTime)
    );

    // Priority mapping from MDMS
    // AGGRESSIVE: 1, INJURED: 1, PACK: 2, STANDARD: 3
}
```

#### M3.2: Geographic Sharding

Assign verifiers to specific districts/zones:

```sql
-- Verifier assignment table
CREATE TABLE eg_sdcrs_verifier_assignment (
    verifier_id VARCHAR(64),
    district_code VARCHAR(64),
    zone_code VARCHAR(64),
    max_daily_capacity INTEGER DEFAULT 150,
    current_load INTEGER DEFAULT 0
);

-- Query: Find least-loaded verifier for a district
SELECT verifier_id
FROM eg_sdcrs_verifier_assignment
WHERE district_code = :district
  AND current_load < max_daily_capacity
ORDER BY current_load ASC
LIMIT 1;
```

#### M3.3: Auto-Verification for Low-Risk Reports

```java
public class AutoVerifier {

    public boolean canAutoVerify(Report report) {
        return report.getReporterTrustScore() > 0.9   // High-trust teacher
            && report.getGpsConfidence() > 0.95       // Strong GPS match
            && report.getPhotoQualityScore() > 0.8    // Clear photo
            && !report.isAggressive()                 // Not urgent
            && report.getDogCount() == 1;             // Single dog
    }
}
```

**Impact:** 30-40% of reports auto-verified, reducing human queue.

#### M3.4: Workload Dashboard for Supervisors

```json
{
  "dashboard": "SDCRS_VERIFIER_WORKLOAD",
  "metrics": [
    {
      "name": "Queue Depth by District",
      "type": "bar",
      "query": "SELECT district_code, COUNT(*) FROM eg_sdcrs_report WHERE status='PENDING_VERIFICATION' GROUP BY district_code"
    },
    {
      "name": "Verifier Utilization",
      "type": "heatmap",
      "query": "SELECT verifier_id, current_load, max_daily_capacity FROM eg_sdcrs_verifier_assignment"
    },
    {
      "name": "Predicted SLA Breach",
      "type": "metric",
      "formula": "queue_depth / (verifiers_online * avg_throughput)"
    }
  ]
}
```

---

## 5. Hotspot #4: Kafka Consumer Lag

### 5.1 Problem Description

Multiple Kafka topics with consumers that may fall behind during peak loads:

| Topic | Producer Rate (peak) | Consumer | Risk |
|-------|---------------------|----------|------|
| `save-sdcrs-report` | 5/s | Persister | Data loss if crashes |
| `update-sdcrs-report` | 10/s | Persister | Stale data |
| `sdcrs-report-indexer` | 15/s | Indexer | Search inconsistency |
| `sdcrs-notification` | 10/s | Notification | Delayed alerts |
| `sdcrs-payout-trigger` | 2/s | SDCRS | Payout delays |

### 5.2 Impact

| Consumer Lag | User Impact |
|--------------|-------------|
| Persister 5min | Report visible in API but not searchable |
| Indexer 10min | Dashboard shows stale counts |
| Notification 30min | Teacher doesn't know report was rejected |
| Payout 1hr | Teacher complains about missing payment |

### 5.3 Mitigations

#### M4.1: Partition Scaling

```yaml
# Kafka topic configuration
kafka:
  topics:
    save-sdcrs-report:
      partitions: 12        # Scale with persister instances
      replication-factor: 3
      retention-ms: 604800000  # 7 days

    sdcrs-report-indexer:
      partitions: 6         # Match indexer instances
      replication-factor: 3

    sdcrs-notification:
      partitions: 4
      replication-factor: 2
```

#### M4.2: Consumer Group Scaling

```yaml
# Persister deployment
persister:
  replicas: 3
  consumer:
    group-id: sdcrs-persister-group
    max-poll-records: 100
    session-timeout-ms: 30000

# Indexer deployment
indexer:
  replicas: 2
  consumer:
    group-id: sdcrs-indexer-group
    max-poll-records: 50
```

#### M4.3: Dead Letter Queue (DLQ)

```java
@KafkaListener(topics = "save-sdcrs-report")
public void consume(ConsumerRecord<String, String> record) {
    try {
        processRecord(record);
    } catch (Exception e) {
        // Send to DLQ after 3 retries
        if (retryCount(record) >= 3) {
            kafkaTemplate.send("save-sdcrs-report-dlq", record.value());
            log.error("Moved to DLQ: {}", record.key());
        } else {
            throw e; // Trigger retry
        }
    }
}
```

#### M4.4: Lag Monitoring & Auto-scaling

```yaml
# Prometheus alert rules
alerts:
  - name: KafkaConsumerLag
    expr: kafka_consumergroup_lag > 1000
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "Consumer lag exceeds 1000 for {{ $labels.topic }}"

  - name: KafkaConsumerLagCritical
    expr: kafka_consumergroup_lag > 5000
    for: 2m
    labels:
      severity: critical
    annotations:
      summary: "Critical consumer lag - trigger auto-scale"
      runbook: "Scale consumer pods to 2x current"
```

---

## 6. Hotspot #5: Database Write Contention

### 6.1 Problem Description

PostgreSQL write contention during peak hours:
- INSERT: 5/second (new reports)
- UPDATE: 10/second (status changes)
- SELECT: 50/second (searches, dashboards)

**Bottleneck:** Single primary database with WAL write locks.

### 6.2 Impact

| Concurrent Writes | Avg Latency | P99 Latency | Deadlock Risk |
|-------------------|-------------|-------------|---------------|
| 10 | 5ms | 20ms | Low |
| 50 | 25ms | 200ms | Medium |
| 100 | 100ms | 2s | High |

### 6.3 Mitigations

#### M5.1: Connection Pooling (PgBouncer)

```yaml
# PgBouncer configuration
pgbouncer:
  pool_mode: transaction
  max_client_conn: 1000
  default_pool_size: 50
  reserve_pool_size: 10
  reserve_pool_timeout: 5
```

#### M5.2: Read Replicas

```
                     ┌─────────────┐
                     │   Primary   │
                     │ (Writes)    │
                     └──────┬──────┘
                            │ WAL Streaming
              ┌─────────────┼─────────────┐
              │             │             │
              ▼             ▼             ▼
        ┌──────────┐  ┌──────────┐  ┌──────────┐
        │ Replica 1│  │ Replica 2│  │ Replica 3│
        │ (Reads)  │  │ (Reads)  │  │ (Analytics)
        └──────────┘  └──────────┘  └──────────┘
```

**Routing Logic:**
```java
@Transactional(readOnly = true)
public List<Report> searchReports(SearchCriteria criteria) {
    // Routes to read replica
    return repository.search(criteria);
}

@Transactional
public Report updateReport(Report report) {
    // Routes to primary
    return repository.save(report);
}
```

#### M5.3: Batch Inserts via Persister

```yaml
# Persister batching configuration
persister:
  batch:
    enabled: true
    size: 50           # Batch 50 records
    timeout-ms: 1000   # Or flush every 1 second
```

#### M5.4: Table Partitioning by Month

```sql
-- Partition by created_time (monthly)
CREATE TABLE eg_sdcrs_report (
    id VARCHAR(64),
    tenant_id VARCHAR(64) NOT NULL,
    report_number VARCHAR(64) NOT NULL,
    created_time BIGINT NOT NULL,
    -- ... other columns
) PARTITION BY RANGE (created_time);

-- Monthly partitions
CREATE TABLE eg_sdcrs_report_2024_01
    PARTITION OF eg_sdcrs_report
    FOR VALUES FROM (1704067200000) TO (1706745600000);

CREATE TABLE eg_sdcrs_report_2024_02
    PARTITION OF eg_sdcrs_report
    FOR VALUES FROM (1706745600000) TO (1709251200000);
-- ... create partitions for each month
```

**Benefits:**
- Faster queries on recent data (partition pruning)
- Easy archival (drop old partitions)
- Parallel vacuum on partitions

---

## 7. Hotspot #6: Elasticsearch Indexing Lag

### 7.1 Problem Description

Elasticsearch indexing via Kafka consumer falls behind, causing:
- Dashboard counts don't match database
- Search results miss recent reports
- Geographic hotspot analysis delayed

### 7.2 Impact

| Indexing Lag | User Impact |
|--------------|-------------|
| 1 minute | Minor - users rarely notice |
| 10 minutes | Dashboard counts stale |
| 1 hour | Search misses many reports |
| 6+ hours | Analytics completely unreliable |

### 7.3 Mitigations

#### M6.1: Bulk Indexing

```yaml
# Indexer configuration
indexer:
  bulk:
    enabled: true
    size: 100          # Bulk index 100 docs
    flush-interval: 5s # Or flush every 5 seconds
    concurrent-requests: 2
```

#### M6.2: Index Sharding

```json
{
  "settings": {
    "number_of_shards": 5,
    "number_of_replicas": 1,
    "refresh_interval": "5s"
  },
  "mappings": {
    "properties": {
      "tenantId": { "type": "keyword" },
      "status": { "type": "keyword" },
      "createdTime": { "type": "date", "format": "epoch_millis" },
      "location": { "type": "geo_point" },
      "reportNumber": { "type": "keyword" },
      "imageHash": { "type": "keyword" }
    }
  }
}
```

#### M6.3: Index Lifecycle Management (ILM)

```json
{
  "policy": {
    "phases": {
      "hot": {
        "actions": {
          "rollover": {
            "max_size": "50GB",
            "max_age": "30d"
          }
        }
      },
      "warm": {
        "min_age": "30d",
        "actions": {
          "shrink": { "number_of_shards": 1 },
          "forcemerge": { "max_num_segments": 1 }
        }
      },
      "cold": {
        "min_age": "90d",
        "actions": {
          "freeze": {}
        }
      },
      "delete": {
        "min_age": "365d",
        "actions": {
          "delete": {}
        }
      }
    }
  }
}
```

#### M6.4: Dual-Write for Critical Data

For dashboard counts, maintain Redis counters alongside ES:

```java
public void onReportCreated(Report report) {
    // Increment Redis counter immediately
    redisTemplate.opsForHash()
        .increment("report:count:" + report.getTenantId(),
                   report.getStatus(), 1);

    // Async ES indexing continues normally
    kafkaTemplate.send("sdcrs-report-indexer", report);
}
```

---

## 8. Hotspot #7: Public Tracking API Abuse

### 8.1 Problem Description

`/_track` endpoint is publicly accessible (no auth). Vulnerable to:
- DDoS attacks
- Brute-force enumeration of report IDs
- Bot scraping for data harvesting

### 8.2 Impact

| Attack Type | Without Protection | With Protection |
|-------------|-------------------|-----------------|
| DDoS (10K RPS) | Service down | Blocked at gateway |
| ID enumeration | 1000 reports/min | 100 req/min limit |
| Bot scraping | All data exposed | Rate limited + CAPTCHA |

### 8.3 Mitigations

#### M7.1: Rate Limiting (Already Configured)

```yaml
# From service design - reinforce in gateway
zuul:
  ratelimit:
    enabled: true
    repository: BUCKET4J_HAZELCAST
    policy-list:
      sdcrs-track:
        - limit: 100
          refresh-interval: 60
          type:
            - url=/sdcrs-services/v1/report/_track
            - origin  # Per IP
```

#### M7.2: Non-Sequential Tracking IDs

```java
// IDGen: Random alphanumeric instead of sequential
public String generateTrackingId() {
    // Pattern: [A-Z]{3}[0-9]{3} = 17,576,000 combinations
    // Brute force at 100/min = 121 days to enumerate all
    return RandomStringUtils.random(3, "ABCDEFGHJKLMNPQRSTUVWXYZ")
         + RandomStringUtils.random(3, "0123456789");
}
```

#### M7.3: CAPTCHA for Repeated Requests

```java
@GetMapping("/_track")
public TrackingResponse track(
    @RequestParam String trackingId,
    @RequestHeader("X-Forwarded-For") String clientIp,
    @RequestParam(required = false) String captchaToken
) {
    int requestCount = rateLimiter.getRequestCount(clientIp, 300); // 5 min window

    if (requestCount > 20 && captchaToken == null) {
        throw new CaptchaRequiredException("Please verify you are human");
    }

    if (captchaToken != null && !captchaService.verify(captchaToken)) {
        throw new InvalidCaptchaException("CAPTCHA verification failed");
    }

    return trackingService.getStatus(trackingId);
}
```

#### M7.4: Response Caching

```yaml
# Cache tracking responses for 5 minutes
spring:
  cache:
    type: redis
    redis:
      time-to-live: 300000  # 5 minutes

# Controller
@Cacheable(value = "tracking", key = "#trackingId")
public TrackingResponse track(String trackingId) {
    return repository.findByTrackingId(trackingId);
}
```

---

## 9. Hotspot #8: Payout Processing Spikes

### 9.1 Problem Description

Payout processing triggered when reports reach CAPTURED status. If many dogs captured in a short period (e.g., mass capture operation):
- Billing Service overwhelmed with demand creation
- Bank integration rate limits hit
- Teachers complain about delayed payments

### 9.2 Impact

| Captures/Hour | Payout Processing Time | Bank API Calls |
|---------------|------------------------|----------------|
| 50 | Real-time | 50/hour |
| 200 | 5 min delay | 200/hour (near limit) |
| 500 | 30+ min delay | Rate limited |

### 9.3 Mitigations

#### M8.1: Payout Batching

```java
// Instead of immediate payout, batch every 15 minutes
@Scheduled(fixedRate = 900000) // 15 minutes
public void processPendingPayouts() {
    List<Report> pending = repository.findByPayoutStatus("PENDING");

    // Group by teacher to reduce API calls
    Map<String, List<Report>> byTeacher = pending.stream()
        .collect(Collectors.groupingBy(Report::getReporterId));

    for (var entry : byTeacher.entrySet()) {
        BigDecimal totalAmount = entry.getValue().stream()
            .map(Report::getPayoutAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Single API call for all reports by this teacher
        billingService.createDemand(entry.getKey(), totalAmount, entry.getValue());
    }
}
```

#### M8.2: Daily Payout Windows

```yaml
# Payout processing windows
payout:
  windows:
    - start: "10:00"
      end: "12:00"
      description: "Morning batch"
    - start: "15:00"
      end: "17:00"
      description: "Afternoon batch"
```

#### M8.3: Payout Queue with Priority

```java
public class PayoutQueue {
    private PriorityBlockingQueue<PayoutRequest> queue =
        new PriorityBlockingQueue<>(1000,
            Comparator.comparing(PayoutRequest::getTeacherPayoutCount) // Low-count first
                      .thenComparing(PayoutRequest::getCapturedTime));
}
```

#### M8.4: Fallback to Manual Processing

```java
public void processPayout(Report report) {
    try {
        billingService.createDemand(report);
        report.setPayoutStatus("PENDING");
    } catch (RateLimitException e) {
        // Queue for manual processing
        report.setPayoutStatus("QUEUED_MANUAL");
        notifyAdmin("Payout rate limit hit - manual queue growing");
    }
}
```

---

## 10. Hotspot #9: Notification Gateway Limits

### 10.1 Problem Description

SMS gateway has rate limits and costs:
- Rate limit: 50 SMS/second
- Cost: ₹0.15/SMS
- Peak: 10,000+ notifications/hour

### 10.2 Impact

| Notifications/Hour | Gateway Load | Cost/Day |
|-------------------|--------------|----------|
| 5,000 | 50% capacity | ₹7,500 |
| 10,000 | 100% capacity | ₹15,000 |
| 20,000 | Delayed/dropped | ₹30,000 |

### 10.3 Mitigations

#### M9.1: Notification Batching

```java
// Batch similar notifications
@Scheduled(fixedRate = 60000) // Every minute
public void sendBatchedNotifications() {
    // Group by template and status
    List<Notification> pending = notificationRepository.findPending();

    Map<String, List<Notification>> byTemplate = pending.stream()
        .collect(Collectors.groupingBy(Notification::getTemplateId));

    for (var entry : byTemplate.entrySet()) {
        smsGateway.sendBulk(entry.getKey(), entry.getValue());
    }
}
```

#### M9.2: Priority Notifications

```java
public enum NotificationPriority {
    CRITICAL(0),   // Fraud alert, suspension
    HIGH(1),       // Payout completed, report rejected
    MEDIUM(2),     // Status update, assignment
    LOW(3);        // Reminders, tips
}

// High priority: Send immediately
// Low priority: Batch and send in off-peak hours (6PM-8AM)
```

#### M9.3: Push Notifications Instead of SMS

```yaml
# For app users, prefer push over SMS
notification:
  channels:
    - type: push
      priority: 1
      cost: 0
    - type: sms
      priority: 2
      cost: 0.15
      fallback: true  # Only if push fails
```

#### M9.4: Notification Preferences

```sql
-- Let users control notification frequency
CREATE TABLE eg_sdcrs_notification_prefs (
    user_id VARCHAR(64) PRIMARY KEY,
    channel VARCHAR(32) DEFAULT 'sms',  -- sms, push, email, none
    frequency VARCHAR(32) DEFAULT 'immediate',  -- immediate, daily-digest, weekly
    quiet_hours_start TIME DEFAULT '22:00',
    quiet_hours_end TIME DEFAULT '07:00'
);
```

---

## 11. Hotspot #10: Auto-Validation Bottleneck

### 11.1 Problem Description

Auto-validation runs on every new report:
1. GPS validation (EXIF vs submitted coordinates)
2. Timestamp validation (photo < 48 hours old)
3. Boundary check (within tenant geography)
4. Image quality check (blur detection)

**Bottleneck:** CPU-intensive operations in synchronous request path.

### 11.2 Impact

| Validation Step | CPU Time | Can Fail Request? |
|-----------------|----------|-------------------|
| GPS validation | 5ms | Yes |
| Timestamp check | 1ms | Yes |
| Boundary check | 50ms (geo query) | Yes |
| Image quality | 200ms | No (just flag) |
| **Total** | **256ms** | - |

### 11.3 Mitigations

#### M10.1: Async Validation Pipeline

```
Synchronous (Block Request):       Async (Non-Blocking):
┌─────────────────────────┐        ┌─────────────────────────┐
│ 1. GPS Validation       │        │ 1. Basic validation     │
│    (5ms)                │        │    (10ms)               │
├─────────────────────────┤        ├─────────────────────────┤
│ 2. Timestamp Check      │        │ 2. Return 202 Accepted  │
│    (1ms)                │        │    (status: VALIDATING) │
├─────────────────────────┤        └─────────────────────────┘
│ 3. Boundary Check       │                   │
│    (50ms)               │                   ▼
├─────────────────────────┤        ┌─────────────────────────┐
│ 4. Image Quality        │        │ Kafka: validation-queue │
│    (200ms)              │        └───────────┬─────────────┘
├─────────────────────────┤                    │
│ 5. Return 201 Created   │                    ▼
│    or 400 Error         │        ┌─────────────────────────┐
└─────────────────────────┘        │ Async Validators:       │
                                   │ - GPS (5ms)             │
Total: 256ms blocking              │ - Timestamp (1ms)       │
                                   │ - Boundary (50ms)       │
                                   │ - Quality (200ms)       │
                                   └───────────┬─────────────┘
                                               │
                                               ▼
                                   ┌─────────────────────────┐
                                   │ Update status:          │
                                   │ PENDING_VERIFICATION    │
                                   │ or AUTO_REJECTED        │
                                   └─────────────────────────┘

Total: 10ms blocking, 256ms async
```

#### M10.2: Geo-Boundary Cache

```java
// Cache tenant boundaries in memory
@Cacheable("tenant-boundaries")
public Polygon getTenantBoundary(String tenantId) {
    return geoRepository.findBoundaryByTenant(tenantId);
}

// Use JTS for fast in-memory point-in-polygon check
public boolean isWithinBoundary(double lat, double lon, String tenantId) {
    Polygon boundary = getTenantBoundary(tenantId);
    Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
    return boundary.contains(point);  // ~1ms instead of 50ms DB query
}
```

#### M10.3: Validation Result Caching

```java
// If same teacher submits from same location, reuse boundary check
String cacheKey = teacherId + ":" + gridCell(lat, lon); // 100m grid
Boolean cachedResult = cache.get("boundary:" + cacheKey);
if (cachedResult != null) {
    return cachedResult; // Skip geo check
}
```

---

## 12. Infrastructure Recommendations

### 12.1 Kubernetes Horizontal Pod Autoscaler (HPA)

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: sdcrs-services-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: sdcrs-services
  minReplicas: 3
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
  behavior:
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
        - type: Pods
          value: 2
          periodSeconds: 60
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
        - type: Pods
          value: 1
          periodSeconds: 120
```

### 12.2 Resource Requests/Limits

```yaml
# sdcrs-services deployment
resources:
  requests:
    cpu: "500m"
    memory: "1Gi"
  limits:
    cpu: "2000m"
    memory: "4Gi"

# persister
resources:
  requests:
    cpu: "200m"
    memory: "512Mi"
  limits:
    cpu: "1000m"
    memory: "2Gi"

# indexer
resources:
  requests:
    cpu: "300m"
    memory: "1Gi"
  limits:
    cpu: "1000m"
    memory: "2Gi"
```

### 12.3 Database Sizing

| Load Level | PostgreSQL | Read Replicas | Connection Pool |
|------------|------------|---------------|-----------------|
| Low (1K/day) | db.t3.medium | 0 | 20 |
| Medium (5K/day) | db.r5.large | 1 | 50 |
| High (10K/day) | db.r5.xlarge | 2 | 100 |
| Peak (20K/day) | db.r5.2xlarge | 3 | 200 |

### 12.4 Elasticsearch Sizing

| Load Level | Nodes | Shards | Heap |
|------------|-------|--------|------|
| Low | 1 | 1 | 2GB |
| Medium | 3 | 3 | 4GB |
| High | 5 | 5 | 8GB |

---

## 13. Monitoring & Alerting

### 13.1 Key Metrics to Monitor

```yaml
# Prometheus metrics
metrics:
  sdcrs:
    # Request metrics
    - name: sdcrs_requests_total
      type: counter
      labels: [endpoint, status, method]
    - name: sdcrs_request_duration_seconds
      type: histogram
      labels: [endpoint]

    # Queue metrics
    - name: sdcrs_verification_queue_depth
      type: gauge
      labels: [district]
    - name: sdcrs_payout_pending_count
      type: gauge

    # Processing metrics
    - name: sdcrs_validation_duration_seconds
      type: histogram
      labels: [step]
    - name: sdcrs_duplicate_check_duration_seconds
      type: histogram
```

### 13.2 Alert Rules

```yaml
groups:
  - name: sdcrs-alerts
    rules:
      - alert: SDCRSHighLatency
        expr: histogram_quantile(0.95, rate(sdcrs_request_duration_seconds_bucket[5m])) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "SDCRS P95 latency > 2s"

      - alert: SDCRSVerificationQueueHigh
        expr: sdcrs_verification_queue_depth > 1000
        for: 15m
        labels:
          severity: warning
        annotations:
          summary: "Verification queue depth > 1000"

      - alert: SDCRSPayoutBacklog
        expr: sdcrs_payout_pending_count > 500
        for: 30m
        labels:
          severity: critical
        annotations:
          summary: "Payout backlog > 500 teachers waiting"

      - alert: SDCRSKafkaLag
        expr: kafka_consumergroup_lag{group=~"sdcrs.*"} > 5000
        for: 10m
        labels:
          severity: critical
        annotations:
          summary: "Kafka consumer lag > 5000 for {{ $labels.topic }}"
```

### 13.3 Dashboard Panels

```json
{
  "dashboard": "SDCRS_OPS",
  "panels": [
    {
      "title": "Request Rate",
      "type": "graph",
      "query": "rate(sdcrs_requests_total[5m])"
    },
    {
      "title": "P95 Latency",
      "type": "graph",
      "query": "histogram_quantile(0.95, rate(sdcrs_request_duration_seconds_bucket[5m]))"
    },
    {
      "title": "Verification Queue",
      "type": "gauge",
      "query": "sum(sdcrs_verification_queue_depth)"
    },
    {
      "title": "Kafka Consumer Lag",
      "type": "table",
      "query": "kafka_consumergroup_lag{group=~\"sdcrs.*\"}"
    },
    {
      "title": "Error Rate",
      "type": "graph",
      "query": "rate(sdcrs_requests_total{status=~\"5..\"}[5m]) / rate(sdcrs_requests_total[5m])"
    }
  ]
}
```

---

## 14. Capacity Planning

### 14.1 Growth Projections

| Year | Teachers | Daily Reports | Storage/Month | DB Size |
|------|----------|---------------|---------------|---------|
| Y1 | 10,000 | 2,000 | 120GB | 5GB |
| Y2 | 30,000 | 6,000 | 360GB | 20GB |
| Y3 | 50,000 | 10,000 | 600GB | 50GB |
| Y5 | 100,000 | 20,000 | 1.2TB | 150GB |

### 14.2 Infrastructure Scaling Milestones

| Daily Reports | Trigger | Action |
|---------------|---------|--------|
| 5,000 | Baseline | 3 pods, 1 read replica |
| 10,000 | +100% | 5 pods, 2 read replicas, Redis cluster |
| 20,000 | +100% | 8 pods, 3 read replicas, ES cluster expansion |
| 50,000 | +150% | Microservice split, dedicated queues |

### 14.3 Cost Optimization

| Optimization | Monthly Savings | Trade-off |
|--------------|-----------------|-----------|
| Spot instances for workers | 60-70% | Interruption handling |
| Reserved DB instances | 40% | 1-year commitment |
| S3 Intelligent-Tiering | 30% | Access latency for old files |
| Off-peak notification batching | 20% SMS cost | 6-hour delay for low priority |

---

## Summary: Priority Mitigations

| Priority | Hotspot | Mitigation | Effort | Impact |
|----------|---------|------------|--------|--------|
| **P0** | Photo Processing | Async EXIF/hash via Kafka | Medium | High |
| **P0** | Public API Abuse | Rate limiting + CAPTCHA | Low | High |
| **P1** | Duplicate Detection | Bloom filter + Redis cache | Medium | High |
| **P1** | Kafka Lag | Partition scaling + DLQ | Low | Medium |
| **P1** | Database Writes | PgBouncer + read replicas | Medium | High |
| **P2** | Verification Queue | Priority queue + auto-verify | Medium | Medium |
| **P2** | Payout Processing | Batching + windows | Low | Medium |
| **P2** | Notifications | Push priority + batching | Low | Medium |
| **P3** | ES Indexing | Bulk indexing + ILM | Low | Low |
| **P3** | Auto-Validation | Geo cache + async pipeline | High | Medium |

---

*Document Version: 1.0*
*Last Updated: December 2025*
*Related Documents:*
- [`03-service-design.md`](./03-service-design.md)
- [`05-sequence-diagrams.md`](./05-sequence-diagrams.md)
- [`04b-no-ccrs/db/V1__create_sdcrs_tables.sql`](./04b-no-ccrs/db/V1__create_sdcrs_tables.sql)
