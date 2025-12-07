# V-NOT-02: Audit Logging for Verifier Actions

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** System Administrator
**I want to** maintain comprehensive audit logs of all verifier actions
**So that** I can ensure accountability, investigate disputes, and monitor quality.

---

## Description

Every action taken by a verifier must be logged for audit purposes. This includes approvals, rejections, escalations, flag acknowledgments, and even view actions. The audit log provides an immutable record for dispute resolution, fraud investigation, and performance monitoring. Logs are retained according to compliance requirements and can be queried by authorized administrators.

---

## Acceptance Criteria

### Actions to Log

- [ ] Submission view (with duration)
- [ ] Photo zoom/pan actions
- [ ] Map interaction
- [ ] Flag acknowledgment
- [ ] Duplicate comparison view
- [ ] Approval action
- [ ] Rejection action (with reason)
- [ ] Escalation action
- [ ] Skip action
- [ ] Bulk actions (with item count)

### Log Entry Requirements

- [ ] Unique log entry ID
- [ ] Timestamp (server time, UTC)
- [ ] Action type
- [ ] Actor ID (verifier)
- [ ] Target ID (submission/queue item)
- [ ] Action-specific data
- [ ] Session ID (for grouping)
- [ ] IP address (hashed for privacy)
- [ ] Device/browser info

### Log Integrity

- [ ] Logs are append-only (no modifications)
- [ ] Tamper-evident (checksums)
- [ ] Retention: 7 years minimum
- [ ] Backup to separate storage
- [ ] Encrypted at rest

### Query Capabilities

- [ ] Query by verifier
- [ ] Query by submission
- [ ] Query by date range
- [ ] Query by action type
- [ ] Export to CSV/JSON
- [ ] Admin-only access

---

## UI/UX Requirements

### Audit Log Viewer (Admin)

```
┌─────────────────────────────────────────────────────────────┐
│  Audit Log                                        🔍 Export │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Filters:                                                   │
│  ┌────────────┐ ┌────────────────┐ ┌────────────────────┐  │
│  │ Verifier ▼ │ │ Action Type ▼  │ │ Dec 1 - Dec 7    ▼│  │
│  └────────────┘ └────────────────┘ └────────────────────┘  │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Application ID: [ SDC-2024-001234            ] 🔍     │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Timestamp              Action       Verifier  Target │  │
│  ├───────────────────────────────────────────────────────┤  │
│  │  2024-12-07 10:45:32   APPROVED     V-012    001234  │  │
│  │  2024-12-07 10:44:15   FLAG_ACK     V-012    001234  │  │
│  │  2024-12-07 10:43:01   PHOTO_ZOOM   V-012    001234  │  │
│  │  2024-12-07 10:42:45   VIEW_START   V-012    001234  │  │
│  │  2024-12-07 10:41:12   REJECTED     V-008    001233  │  │
│  │  2024-12-07 10:40:55   FLAG_ACK     V-008    001233  │  │
│  │  2024-12-07 10:39:22   VIEW_START   V-008    001233  │  │
│  │  2024-12-07 10:38:01   ESCALATED    V-015    001232  │  │
│  │  ...                                                  │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  Showing 1-50 of 12,847              [ < ] [ 1 ] [ > ]     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Audit Log Detail View

```
┌─────────────────────────────────────────────────────────────┐
│  Audit Log Entry                                    Close ✕ │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Entry ID: AUD-2024120710453201234                          │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │  Action: SUBMISSION_APPROVED                          │  │
│  │                                                       │  │
│  │  ─────────────────────────────────────────────────    │  │
│  │                                                       │  │
│  │  Timestamp:      2024-12-07T10:45:32.451Z (UTC)       │  │
│  │  Verifier:       V-012 (Marie D.)                     │  │
│  │  Application:    SDC-2024-001234                      │  │
│  │  Session:        SES-abc123def456                     │  │
│  │                                                       │  │
│  │  ─────────────────────────────────────────────────    │  │
│  │                                                       │  │
│  │  Action Details:                                      │  │
│  │  {                                                    │  │
│  │    "reviewDuration": 185,                             │  │
│  │    "flagsAcknowledged": ["gallery_upload"],           │  │
│  │    "photoZoomCount": 2,                               │  │
│  │    "mapViewed": true,                                 │  │
│  │    "duplicatesChecked": 0,                            │  │
│  │    "slaRemaining": 64800                              │  │
│  │  }                                                    │  │
│  │                                                       │  │
│  │  ─────────────────────────────────────────────────    │  │
│  │                                                       │  │
│  │  Device:         Chrome 120 / macOS                   │  │
│  │  IP Hash:        sha256:7f83b1657...                  │  │
│  │  Checksum:       sha256:2c26b46b68...                 │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  [ View Related Logs ]  [ Export Entry ]                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Submission Audit Trail

```
┌─────────────────────────────────────────────────────────────┐
│  Audit Trail: SDC-2024-001234                    📥 Export  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Complete history of all actions on this submission:        │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │  ● 2024-12-07 10:45:32  APPROVED                      │  │
│  │  │  Verifier: V-012 (Marie D.)                        │  │
│  │  │  Duration: 3m 5s                                   │  │
│  │  │  Flags acknowledged: gallery_upload                │  │
│  │  │                                                    │  │
│  │  ● 2024-12-07 10:44:15  FLAG_ACKNOWLEDGED             │  │
│  │  │  Verifier: V-012                                   │  │
│  │  │  Flag: gallery_upload                              │  │
│  │  │                                                    │  │
│  │  ● 2024-12-07 10:43:50  PHOTO_ZOOMED                  │  │
│  │  │  Verifier: V-012                                   │  │
│  │  │  Photo: dog_photo (2x zoom)                        │  │
│  │  │                                                    │  │
│  │  ● 2024-12-07 10:43:01  MAP_VIEWED                    │  │
│  │  │  Verifier: V-012                                   │  │
│  │  │  Interaction: pan, zoom                            │  │
│  │  │                                                    │  │
│  │  ● 2024-12-07 10:42:45  VIEW_STARTED                  │  │
│  │  │  Verifier: V-012                                   │  │
│  │  │  Queue position: #23                               │  │
│  │  │                                                    │  │
│  │  ● 2024-12-07 09:20:00  QUEUED_FOR_VERIFICATION       │  │
│  │  │  System                                            │  │
│  │  │  Auto-queued after submission                      │  │
│  │  │                                                    │  │
│  │  ● 2024-12-07 09:15:00  SUBMITTED                     │  │
│  │     Teacher: T-456 (Jean M.)                          │  │
│  │     Location: Boulaos                                 │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  7 events total                                             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Verifier Activity Report

```
┌─────────────────────────────────────────────────────────────┐
│  Verifier Activity: V-012 (Marie D.)              📥 Export │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Period: Dec 1 - Dec 7, 2024                                │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │  Summary                                              │  │
│  │  ─────────────────────────────────────────────────    │  │
│  │  Total Actions:         487                           │  │
│  │  Submissions Reviewed:  89                            │  │
│  │  Approved:              72 (81%)                      │  │
│  │  Rejected:              14 (16%)                      │  │
│  │  Escalated:             3 (3%)                        │  │
│  │  Avg Review Time:       2m 47s                        │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │  Action Breakdown                                     │  │
│  │  ─────────────────────────────────────────────────    │  │
│  │  VIEW_START:           89                             │  │
│  │  PHOTO_ZOOM:          156                             │  │
│  │  MAP_VIEW:             78                             │  │
│  │  FLAG_ACK:             34                             │  │
│  │  DUPLICATE_CHECK:      23                             │  │
│  │  APPROVED:             72                             │  │
│  │  REJECTED:             14                             │  │
│  │  ESCALATED:             3                             │  │
│  │  SKIPPED:              18                             │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  [ View Detailed Logs ]  [ Compare to Team Avg ]            │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Technical Implementation

### Audit Logger Service

```javascript
class AuditLoggerService {
  constructor(db, config) {
    this.db = db;
    this.config = config;
    this.pendingLogs = [];
    this.flushInterval = null;

    // Batch writes for performance
    this.startBatchFlush();
  }

  async log(entry) {
    const enrichedEntry = await this.enrichLogEntry(entry);

    // Add to pending batch
    this.pendingLogs.push(enrichedEntry);

    // Immediate flush for critical actions
    if (this.isCriticalAction(entry.action)) {
      await this.flush();
    }

    return enrichedEntry.logId;
  }

  async enrichLogEntry(entry) {
    const logId = this.generateLogId(entry);
    const timestamp = new Date().toISOString();

    const enrichedEntry = {
      logId,
      timestamp,
      timestampUnix: Date.now(),
      action: entry.action,
      actorId: entry.actorId,
      actorRole: entry.actorRole || 'VERIFIER',
      targetId: entry.targetId,
      targetType: entry.targetType || 'submission',
      tenantId: entry.tenantId,
      sessionId: entry.sessionId,
      data: entry.data || {},
      metadata: {
        ipHash: entry.ipAddress ? this.hashIP(entry.ipAddress) : null,
        userAgent: entry.userAgent ? this.parseUserAgent(entry.userAgent) : null,
        source: entry.source || 'web'
      },
      checksum: null // Will be computed before storage
    };

    // Compute checksum for integrity
    enrichedEntry.checksum = this.computeChecksum(enrichedEntry);

    return enrichedEntry;
  }

  generateLogId(entry) {
    const date = new Date().toISOString().replace(/[-:T.]/g, '').slice(0, 14);
    const random = crypto.randomBytes(4).toString('hex');
    return `AUD-${date}-${entry.actorId}-${random}`;
  }

  hashIP(ipAddress) {
    return crypto.createHash('sha256')
      .update(ipAddress + this.config.ipSalt)
      .digest('hex')
      .substring(0, 16);
  }

  parseUserAgent(userAgent) {
    // Simplified UA parsing
    const browser = userAgent.match(/(Chrome|Firefox|Safari|Edge)\/[\d.]+/)?.[0] || 'Unknown';
    const os = userAgent.match(/(Windows|Mac OS X|Linux|Android|iOS)[\s\d._]*/)?.[0] || 'Unknown';
    return { browser, os };
  }

  computeChecksum(entry) {
    const dataToHash = JSON.stringify({
      logId: entry.logId,
      timestamp: entry.timestamp,
      action: entry.action,
      actorId: entry.actorId,
      targetId: entry.targetId,
      data: entry.data
    });

    return crypto.createHash('sha256')
      .update(dataToHash + this.config.checksumSecret)
      .digest('hex');
  }

  isCriticalAction(action) {
    const criticalActions = [
      'SUBMISSION_APPROVED',
      'SUBMISSION_REJECTED',
      'SUBMISSION_ESCALATED',
      'BULK_DUPLICATE_COMPLETED',
      'ESCALATION_RESOLVED'
    ];
    return criticalActions.includes(action);
  }

  startBatchFlush() {
    // Flush every 5 seconds
    this.flushInterval = setInterval(() => {
      if (this.pendingLogs.length > 0) {
        this.flush();
      }
    }, 5000);
  }

  async flush() {
    if (this.pendingLogs.length === 0) return;

    const toWrite = [...this.pendingLogs];
    this.pendingLogs = [];

    try {
      await this.db.auditLog.insertMany(toWrite, { ordered: false });
    } catch (error) {
      console.error('Audit log flush failed:', error);
      // Re-queue failed logs
      this.pendingLogs = [...toWrite, ...this.pendingLogs];
    }
  }

  async shutdown() {
    if (this.flushInterval) {
      clearInterval(this.flushInterval);
    }
    await this.flush();
  }
}

// Singleton instance
let auditLogger = null;

function getAuditLogger() {
  if (!auditLogger) {
    auditLogger = new AuditLoggerService(db, {
      ipSalt: process.env.IP_HASH_SALT,
      checksumSecret: process.env.AUDIT_CHECKSUM_SECRET
    });
  }
  return auditLogger;
}
```

### Action-Specific Logging Functions

```javascript
// Log verifier starting to view a submission
async function logViewStart(verifierId, queueEntry, sessionId, metadata) {
  return getAuditLogger().log({
    action: 'VIEW_STARTED',
    actorId: verifierId,
    targetId: queueEntry.applicationId,
    tenantId: queueEntry.tenantId,
    sessionId,
    data: {
      queueId: queueEntry.queueId,
      queuePosition: queueEntry.position,
      priority: queueEntry.priority,
      flagCount: queueEntry.submissionSummary?.flags?.length || 0
    },
    ...metadata
  });
}

// Log photo interaction
async function logPhotoInteraction(verifierId, applicationId, interaction, sessionId, metadata) {
  return getAuditLogger().log({
    action: 'PHOTO_INTERACTION',
    actorId: verifierId,
    targetId: applicationId,
    sessionId,
    data: {
      interactionType: interaction.type, // 'zoom', 'pan', 'swipe'
      photoType: interaction.photoType, // 'dog_photo', 'selfie'
      zoomLevel: interaction.zoomLevel,
      duration: interaction.duration
    },
    ...metadata
  });
}

// Log flag acknowledgment
async function logFlagAcknowledgment(verifierId, applicationId, flagCode, sessionId, metadata) {
  return getAuditLogger().log({
    action: 'FLAG_ACKNOWLEDGED',
    actorId: verifierId,
    targetId: applicationId,
    sessionId,
    data: {
      flagCode,
      acknowledgedAt: new Date().toISOString()
    },
    ...metadata
  });
}

// Log approval
async function logApproval(verifierId, submission, queueEntry, sessionId, metadata) {
  const viewStartLog = await db.auditLog.findOne({
    action: 'VIEW_STARTED',
    actorId: verifierId,
    targetId: submission.applicationId,
    sessionId
  }, { sort: { timestamp: -1 } });

  const reviewDuration = viewStartLog
    ? (Date.now() - new Date(viewStartLog.timestamp).getTime()) / 1000
    : null;

  return getAuditLogger().log({
    action: 'SUBMISSION_APPROVED',
    actorId: verifierId,
    targetId: submission.applicationId,
    tenantId: submission.tenantId,
    sessionId,
    data: {
      queueId: queueEntry.queueId,
      reviewDuration,
      flagsPresent: queueEntry.submissionSummary?.flags?.map(f => f.code) || [],
      flagsAcknowledged: await getAcknowledgedFlags(queueEntry.queueId, verifierId),
      priority: queueEntry.priority,
      slaRemaining: calculateSLARemaining(queueEntry.timestamps.slaDeadline),
      photoInteractions: await getSessionPhotoInteractions(sessionId),
      mapViewed: await wasMapViewed(sessionId),
      duplicatesChecked: await getDuplicateCheckCount(sessionId)
    },
    ...metadata
  });
}

// Log rejection
async function logRejection(verifierId, submission, queueEntry, rejectionData, sessionId, metadata) {
  const viewStartLog = await db.auditLog.findOne({
    action: 'VIEW_STARTED',
    actorId: verifierId,
    targetId: submission.applicationId,
    sessionId
  }, { sort: { timestamp: -1 } });

  const reviewDuration = viewStartLog
    ? (Date.now() - new Date(viewStartLog.timestamp).getTime()) / 1000
    : null;

  return getAuditLogger().log({
    action: 'SUBMISSION_REJECTED',
    actorId: verifierId,
    targetId: submission.applicationId,
    tenantId: submission.tenantId,
    sessionId,
    data: {
      queueId: queueEntry.queueId,
      reviewDuration,
      rejectionReason: rejectionData.reasonCode,
      hasNotes: !!rejectionData.notes,
      notesLength: rejectionData.notes?.length || 0,
      flagsPresent: queueEntry.submissionSummary?.flags?.map(f => f.code) || [],
      priority: queueEntry.priority,
      isDuplicate: rejectionData.isDuplicate || false,
      originalId: rejectionData.originalId || null
    },
    ...metadata
  });
}

// Log escalation
async function logEscalation(verifierId, escalation, sessionId, metadata) {
  return getAuditLogger().log({
    action: 'SUBMISSION_ESCALATED',
    actorId: verifierId,
    targetId: escalation.applicationId,
    tenantId: escalation.tenantId,
    sessionId,
    data: {
      escalationId: escalation.escalationId,
      reason: escalation.reason,
      notesLength: escalation.notes?.length || 0,
      escalationCount: escalation.escalationCount,
      verifierActionsCount: escalation.verifierActions?.length || 0
    },
    ...metadata
  });
}

// Log bulk action
async function logBulkDuplicateAction(verifierId, bulkResult, sessionId, metadata) {
  return getAuditLogger().log({
    action: 'BULK_DUPLICATE_COMPLETED',
    actorId: verifierId,
    targetId: bulkResult.originalId,
    tenantId: metadata.tenantId,
    sessionId,
    data: {
      bulkActionId: bulkResult.bulkActionId,
      originalId: bulkResult.originalId,
      duplicatesRequested: bulkResult.totalRequested,
      duplicatesSuccessful: bulkResult.successful,
      duplicatesFailed: bulkResult.failed,
      failedIds: bulkResult.results.failed.map(f => f.applicationId)
    },
    ...metadata
  });
}
```

### Audit Query API

```javascript
// GET /api/admin/audit-log
async function queryAuditLog(req, res) {
  const {
    verifierId,
    applicationId,
    action,
    startDate,
    endDate,
    page = 1,
    limit = 50
  } = req.query;

  // Build query
  const query = {};

  if (verifierId) query.actorId = verifierId;
  if (applicationId) query.targetId = applicationId;
  if (action) query.action = action;
  if (startDate || endDate) {
    query.timestamp = {};
    if (startDate) query.timestamp.$gte = new Date(startDate).toISOString();
    if (endDate) query.timestamp.$lte = new Date(endDate).toISOString();
  }

  // Execute query
  const logs = await db.auditLog.find(query)
    .sort({ timestamp: -1 })
    .skip((page - 1) * limit)
    .limit(parseInt(limit))
    .toArray();

  const total = await db.auditLog.countDocuments(query);

  // Enrich with verifier names (for display)
  const verifierIds = [...new Set(logs.map(l => l.actorId))];
  const verifiers = await db.users.find(
    { id: { $in: verifierIds } },
    { projection: { id: 1, name: 1 } }
  ).toArray();
  const verifierMap = Object.fromEntries(verifiers.map(v => [v.id, v.name]));

  const enrichedLogs = logs.map(log => ({
    ...log,
    actorName: verifierMap[log.actorId] || 'Unknown'
  }));

  return res.json({
    logs: enrichedLogs,
    pagination: {
      page: parseInt(page),
      limit: parseInt(limit),
      total,
      pages: Math.ceil(total / limit)
    }
  });
}

// GET /api/admin/audit-log/:logId
async function getAuditLogEntry(req, res) {
  const { logId } = req.params;

  const entry = await db.auditLog.findOne({ logId });

  if (!entry) {
    return res.status(404).json({ error: 'Log entry not found' });
  }

  // Verify checksum
  const expectedChecksum = computeChecksum(entry);
  const isValid = entry.checksum === expectedChecksum;

  // Get verifier name
  const verifier = await db.users.findOne({ id: entry.actorId });

  return res.json({
    entry: {
      ...entry,
      actorName: verifier?.name || 'Unknown'
    },
    integrity: {
      checksumValid: isValid,
      verifiedAt: new Date().toISOString()
    }
  });
}

// GET /api/admin/audit-log/submission/:applicationId
async function getSubmissionAuditTrail(req, res) {
  const { applicationId } = req.params;

  const logs = await db.auditLog.find({
    targetId: applicationId
  }).sort({ timestamp: 1 }).toArray();

  // Include submission events too
  const submissionEvents = await db.timelineEvents.find({
    applicationId
  }).sort({ timestamp: 1 }).toArray();

  // Merge and sort
  const combined = [...logs, ...submissionEvents.map(e => ({
    ...e,
    action: e.status?.toUpperCase() || 'EVENT',
    actorId: e.actor || 'SYSTEM'
  }))].sort((a, b) =>
    new Date(a.timestamp) - new Date(b.timestamp)
  );

  return res.json({
    applicationId,
    events: combined,
    totalEvents: combined.length
  });
}

// POST /api/admin/audit-log/export
async function exportAuditLog(req, res) {
  const { query, format = 'csv' } = req.body;

  const logs = await db.auditLog.find(query)
    .sort({ timestamp: -1 })
    .limit(10000) // Max export size
    .toArray();

  if (format === 'csv') {
    const csv = convertToCSV(logs);
    res.setHeader('Content-Type', 'text/csv');
    res.setHeader('Content-Disposition', 'attachment; filename=audit-log.csv');
    return res.send(csv);
  }

  res.setHeader('Content-Type', 'application/json');
  res.setHeader('Content-Disposition', 'attachment; filename=audit-log.json');
  return res.json(logs);
}
```

### Verifier Activity Analytics

```javascript
async function getVerifierActivityReport(verifierId, dateRange) {
  const logs = await db.auditLog.aggregate([
    {
      $match: {
        actorId: verifierId,
        timestamp: { $gte: dateRange.start, $lte: dateRange.end }
      }
    },
    {
      $group: {
        _id: '$action',
        count: { $sum: 1 }
      }
    }
  ]).toArray();

  // Calculate review times
  const reviewTimes = await db.auditLog.aggregate([
    {
      $match: {
        actorId: verifierId,
        action: { $in: ['SUBMISSION_APPROVED', 'SUBMISSION_REJECTED'] },
        'data.reviewDuration': { $exists: true },
        timestamp: { $gte: dateRange.start, $lte: dateRange.end }
      }
    },
    {
      $group: {
        _id: null,
        avgReviewTime: { $avg: '$data.reviewDuration' },
        minReviewTime: { $min: '$data.reviewDuration' },
        maxReviewTime: { $max: '$data.reviewDuration' }
      }
    }
  ]).toArray();

  // Get unique submissions reviewed
  const uniqueSubmissions = await db.auditLog.distinct('targetId', {
    actorId: verifierId,
    action: { $in: ['SUBMISSION_APPROVED', 'SUBMISSION_REJECTED', 'SUBMISSION_ESCALATED'] },
    timestamp: { $gte: dateRange.start, $lte: dateRange.end }
  });

  const actionCounts = Object.fromEntries(logs.map(l => [l._id, l.count]));

  return {
    verifierId,
    dateRange,
    summary: {
      totalActions: logs.reduce((acc, l) => acc + l.count, 0),
      submissionsReviewed: uniqueSubmissions.length,
      approved: actionCounts['SUBMISSION_APPROVED'] || 0,
      rejected: actionCounts['SUBMISSION_REJECTED'] || 0,
      escalated: actionCounts['SUBMISSION_ESCALATED'] || 0
    },
    reviewTimes: reviewTimes[0] || {
      avgReviewTime: 0,
      minReviewTime: 0,
      maxReviewTime: 0
    },
    actionBreakdown: actionCounts
  };
}
```

---

## Data Retention & Compliance

```javascript
// Audit log retention policy
const RETENTION_POLICY = {
  auditLog: {
    retentionDays: 7 * 365, // 7 years
    archiveAfterDays: 365,   // Archive to cold storage after 1 year
    deleteAfterDays: 7 * 365 // Delete after 7 years
  }
};

// Archive old logs (run monthly)
async function archiveOldLogs() {
  const archiveDate = new Date();
  archiveDate.setDate(archiveDate.getDate() - RETENTION_POLICY.auditLog.archiveAfterDays);

  const logsToArchive = await db.auditLog.find({
    timestamp: { $lt: archiveDate.toISOString() },
    archived: { $ne: true }
  }).toArray();

  if (logsToArchive.length > 0) {
    // Write to archive storage (e.g., S3 Glacier)
    await archiveStorage.writeBatch('audit-logs', logsToArchive);

    // Mark as archived
    await db.auditLog.updateMany(
      { logId: { $in: logsToArchive.map(l => l.logId) } },
      { $set: { archived: true, archivedAt: new Date().toISOString() } }
    );
  }

  return { archivedCount: logsToArchive.length };
}

// Delete expired logs (run monthly)
async function deleteExpiredLogs() {
  const deleteDate = new Date();
  deleteDate.setDate(deleteDate.getDate() - RETENTION_POLICY.auditLog.deleteAfterDays);

  const result = await db.auditLog.deleteMany({
    timestamp: { $lt: deleteDate.toISOString() },
    archived: true
  });

  return { deletedCount: result.deletedCount };
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| MongoDB | Database | Log storage |
| Archive Storage (S3) | External | Long-term retention |
| Crypto | Built-in | Hashing & checksums |

---

## Related Stories

- [V-APP-01](./V-APP-01.md) - Approval action
- [V-REJ-01](./V-REJ-01.md) - Rejection action
- [V-REJ-03](./V-REJ-03.md) - Escalation action
- [DA-REP-01](../admin/DA-REP-01.md) - Admin reporting
