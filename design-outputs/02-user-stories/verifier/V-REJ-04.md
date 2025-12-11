# V-REJ-04: Bulk Duplicate Actions

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Verifier
**I want to** mark multiple similar submissions as duplicates in one action
**So that** I can efficiently clear duplicate batches from the queue.

---

## Description

When teachers in the same area report the same dog multiple times, or when multiple teachers photograph the same dog, the queue can contain clusters of duplicates. Instead of marking each one individually, verifiers can select multiple submissions and mark them all as duplicates of an original in a single operation. This dramatically speeds up queue processing during "duplicate storms."

---

## Acceptance Criteria

### Bulk Selection

- [ ] Multi-select mode via toggle or keyboard shortcut
- [ ] Select/deselect individual items
- [ ] Select all visible items
- [ ] Visual indication of selected items
- [ ] Selected count displayed
- [ ] Clear selection option
- [ ] Maximum 20 items per bulk action

### Bulk Duplicate Marking

- [ ] Select original submission from the batch OR external search
- [ ] All selected items (except original) marked as duplicates
- [ ] Single confirmation for entire batch
- [ ] Progress indicator for batch processing
- [ ] Partial success handling (some may fail)
- [ ] Audit trail for bulk action

### Validation Rules

- [ ] Cannot include the original in duplicate selection
- [ ] All selected must belong to same tenant
- [ ] Cannot bulk-mark already processed submissions
- [ ] System validates each duplicate in batch

### Post-Action

- [ ] All marked duplicates removed from queue
- [ ] All affected teachers notified
- [ ] Timeline updated for all submissions
- [ ] Verifier stats updated with batch count

---

## UI/UX Requirements

### Multi-Select Mode Toggle

```
┌─────────────────────────────────────────────────────────────┐
│  Verifier Queue                    ☑️ Multi-Select Mode  ON │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Selected: 5 items                    [ Clear Selection ]   │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ☑️ SDC-2026-001234    📍 Boulaos    ⏱️ 2h ago        │  │
│  │    Similar: 89% match with 001235, 001236            │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ☑️ SDC-2026-001235    📍 Boulaos    ⏱️ 2h ago        │  │
│  │    Similar: 92% match with 001234, 001236            │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ☑️ SDC-2026-001236    📍 Boulaos    ⏱️ 1h ago        │  │
│  │    Similar: 89% match with 001234, 001235            │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ☑️ SDC-2026-001237    📍 Boulaos    ⏱️ 1h ago        │  │
│  │    Similar: 78% match with 001234                     │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ☑️ SDC-2026-001238    📍 Boulaos    ⏱️ 45m ago       │  │
│  │    Similar: 81% match with 001234                     │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌────────────────────────────────────────────────────┐    │
│  │        🔗 Mark Selected as Duplicates (5)          │    │
│  └────────────────────────────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Bulk Duplicate Wizard - Step 1: Select Original

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  🔗 Bulk Mark as Duplicates                    Step 1 of 2  │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  You have selected 5 submissions. Which one is the          │
│  ORIGINAL that others are duplicates of?                    │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │  Select from your selection:                          │  │
│  │                                                       │  │
│  │  ○ SDC-2026-001234  (earliest, 2h ago)    [Preview]  │  │
│  │  ○ SDC-2026-001235  (2h ago)              [Preview]  │  │
│  │  ○ SDC-2026-001236  (1h ago)              [Preview]  │  │
│  │  ○ SDC-2026-001237  (1h ago)              [Preview]  │  │
│  │  ○ SDC-2026-001238  (45m ago)             [Preview]  │  │
│  │                                                       │  │
│  │  ─────────────────── OR ───────────────────           │  │
│  │                                                       │  │
│  │  ○ Search for a different original                    │  │
│  │    ┌─────────────────────────────────────────────┐   │  │
│  │    │ Search by ID or location...                 │   │  │
│  │    └─────────────────────────────────────────────┘   │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  💡 Tip: The original should be the first/best quality      │
│     submission. It will be kept for processing.             │
│                                                             │
│  ┌────────────────────┐  ┌────────────────────────────┐    │
│  │   Cancel           │  │   Next: Confirm →          │    │
│  └────────────────────┘  └────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Bulk Duplicate Wizard - Step 2: Confirm

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  🔗 Bulk Mark as Duplicates                    Step 2 of 2  │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Please confirm this bulk duplicate action:                 │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │  ORIGINAL (will be kept):                             │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │  ✓ SDC-2026-001234                              │  │  │
│  │  │    Teacher: Jean M.  |  Boulaos  |  2h ago      │  │  │
│  │  │    [thumbnail]                                  │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  │                                                       │  │
│  │  DUPLICATES (will be rejected):                       │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │  ✗ SDC-2026-001235  (92% similar)              │  │  │
│  │  │    Teacher: Marie D.                            │  │  │
│  │  │  ✗ SDC-2026-001236  (89% similar)              │  │  │
│  │  │    Teacher: Ahmed K.                            │  │  │
│  │  │  ✗ SDC-2026-001237  (78% similar)              │  │  │
│  │  │    Teacher: Fatima H.                           │  │  │
│  │  │  ✗ SDC-2026-001238  (81% similar)              │  │  │
│  │  │    Teacher: Jean M.                             │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ⚠️ This will:                                              │
│  • Mark 4 submissions as duplicates                         │
│  • Notify 4 teachers their reports were duplicates          │
│  • Remove 4 items from your queue                           │
│  • Keep 1 original for continued processing                 │
│                                                             │
│  ┌────────────────────┐  ┌────────────────────────────┐    │
│  │   ← Back           │  │   🔗 Confirm & Mark (4)    │    │
│  └────────────────────┘  └────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Processing Progress

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                    Processing Bulk Action                   │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Marking 4 submissions as duplicates...                     │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │  ████████████░░░░░░░░░░░░░░░░░░  3/4 complete        │  │
│  │                                                       │  │
│  │  ✓ SDC-2026-001235 marked as duplicate               │  │
│  │  ✓ SDC-2026-001236 marked as duplicate               │  │
│  │  ✓ SDC-2026-001237 marked as duplicate               │  │
│  │  ⏳ SDC-2026-001238 processing...                    │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  Please wait...                                             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Bulk Action Complete

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                    ✓ Bulk Action Complete                   │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │  🎉 Successfully marked 4 duplicates                  │  │
│  │                                                       │  │
│  │  • Original: SDC-2026-001234 (kept for processing)   │  │
│  │  • Duplicates marked: 4                               │  │
│  │  • Teachers notified: 4                               │  │
│  │                                                       │  │
│  │  ─────────────────────────────────────────────────    │  │
│  │                                                       │  │
│  │  Your queue has been updated.                         │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌────────────────────┐  ┌────────────────────────────┐    │
│  │   View Summary     │  │   Back to Queue            │    │
│  └────────────────────┘  └────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Partial Failure Handling

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                    ⚠️ Partial Success                       │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │  3 of 4 submissions marked as duplicates              │  │
│  │                                                       │  │
│  │  Successful:                                          │  │
│  │  ✓ SDC-2026-001235                                   │  │
│  │  ✓ SDC-2026-001236                                   │  │
│  │  ✓ SDC-2026-001237                                   │  │
│  │                                                       │  │
│  │  Failed:                                              │  │
│  │  ✗ SDC-2026-001238                                   │  │
│  │    Reason: Already processed by another verifier      │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  The failed submission will remain in your queue.           │
│                                                             │
│  ┌────────────────────┐  ┌────────────────────────────┐    │
│  │   Review Failed    │  │   Back to Queue            │    │
│  └────────────────────┘  └────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Technical Implementation

### Bulk Duplicate Service

```javascript
class BulkDuplicateService {
  constructor(db, duplicateService, notificationService) {
    this.db = db;
    this.duplicateService = duplicateService;
    this.notifications = notificationService;
  }

  async markBulkDuplicates(verifierId, originalId, duplicateIds, tenantId) {
    // Validate input
    this.validateBulkRequest(originalId, duplicateIds);

    const results = {
      successful: [],
      failed: [],
      originalId
    };

    const bulkActionId = generateUUID();
    const startTime = Date.now();

    // Log bulk action start
    await this.logBulkActionStart(bulkActionId, verifierId, originalId, duplicateIds);

    // Process each duplicate
    for (const duplicateId of duplicateIds) {
      try {
        // Use existing duplicate service for each
        const result = await this.duplicateService.markAsDuplicate(
          duplicateId,
          originalId,
          verifierId,
          { bulkActionId, skipNotification: true } // We'll batch notify later
        );

        results.successful.push({
          applicationId: duplicateId,
          queueId: result.queueId
        });

      } catch (error) {
        results.failed.push({
          applicationId: duplicateId,
          error: error.message
        });
      }
    }

    // Batch notify affected teachers
    await this.batchNotifyTeachers(results.successful, originalId);

    // Update verifier stats
    await this.updateVerifierStats(verifierId, results.successful.length);

    // Log bulk action completion
    await this.logBulkActionComplete(bulkActionId, results, Date.now() - startTime);

    return {
      success: results.failed.length === 0,
      bulkActionId,
      totalRequested: duplicateIds.length,
      successful: results.successful.length,
      failed: results.failed.length,
      results
    };
  }

  validateBulkRequest(originalId, duplicateIds) {
    if (!originalId) {
      throw new Error('Original submission ID is required');
    }

    if (!duplicateIds || duplicateIds.length === 0) {
      throw new Error('At least one duplicate ID is required');
    }

    if (duplicateIds.length > 20) {
      throw new Error('Maximum 20 duplicates per bulk action');
    }

    if (duplicateIds.includes(originalId)) {
      throw new Error('Original cannot be in the duplicate list');
    }

    // Check for duplicate IDs in the list
    const uniqueIds = [...new Set(duplicateIds)];
    if (uniqueIds.length !== duplicateIds.length) {
      throw new Error('Duplicate IDs in the request');
    }
  }

  async batchNotifyTeachers(successfulDuplicates, originalId) {
    // Get teacher info for all successful duplicates
    const applicationIds = successfulDuplicates.map(s => s.applicationId);

    const submissions = await this.db.submissions.find({
      applicationId: { $in: applicationIds }
    }).toArray();

    // Group by teacher to avoid duplicate notifications
    const teacherNotifications = new Map();

    for (const submission of submissions) {
      if (!teacherNotifications.has(submission.teacherId)) {
        teacherNotifications.set(submission.teacherId, {
          teacherId: submission.teacherId,
          teacherPhone: submission.teacherPhone,
          duplicateIds: []
        });
      }
      teacherNotifications.get(submission.teacherId).duplicateIds.push(submission.applicationId);
    }

    // Send notifications
    await Promise.all(
      Array.from(teacherNotifications.values()).map(async (teacher) => {
        // In-app notification
        await this.notifications.create({
          userId: teacher.teacherId,
          type: 'submissions_marked_duplicate',
          title: 'Reports Marked as Duplicate',
          body: `${teacher.duplicateIds.length} of your report(s) were identified as duplicates of an existing report.`,
          data: {
            duplicateIds: teacher.duplicateIds,
            originalId,
            reason: 'duplicate_detected'
          }
        });

        // SMS notification (single even if multiple duplicates)
        await this.notifications.sendSMS(teacher.teacherPhone, 'duplicate_batch', {
          COUNT: teacher.duplicateIds.length,
          IDS: teacher.duplicateIds.slice(0, 3).join(', ') + (teacher.duplicateIds.length > 3 ? '...' : '')
        });
      })
    );
  }

  async updateVerifierStats(verifierId, count) {
    const today = new Date().toISOString().split('T')[0];

    await this.db.verifierStats.updateOne(
      { verifierId, date: today },
      {
        $inc: {
          duplicatesMarked: count,
          totalReviewed: count
        }
      },
      { upsert: true }
    );
  }

  async logBulkActionStart(bulkActionId, verifierId, originalId, duplicateIds) {
    await this.db.auditLog.insertOne({
      type: 'BULK_DUPLICATE_STARTED',
      bulkActionId,
      actorId: verifierId,
      actorRole: 'VERIFIER',
      timestamp: new Date(),
      data: {
        originalId,
        duplicateCount: duplicateIds.length,
        duplicateIds
      }
    });
  }

  async logBulkActionComplete(bulkActionId, results, durationMs) {
    await this.db.auditLog.insertOne({
      type: 'BULK_DUPLICATE_COMPLETED',
      bulkActionId,
      timestamp: new Date(),
      data: {
        originalId: results.originalId,
        successCount: results.successful.length,
        failedCount: results.failed.length,
        successful: results.successful.map(s => s.applicationId),
        failed: results.failed,
        durationMs
      }
    });
  }
}
```

### Bulk Duplicate API Endpoint

```javascript
// POST /api/verifier/bulk/mark-duplicates
async function handleBulkMarkDuplicates(req, res) {
  const { originalId, duplicateIds } = req.body;
  const verifierId = req.user.id;
  const tenantId = req.user.tenantId;

  try {
    // Validate all IDs belong to verifier's queue
    const queueItems = await db.verifierQueue.find({
      applicationId: { $in: [originalId, ...duplicateIds] },
      tenantId,
      status: 'pending'
    }).toArray();

    const queuedIds = queueItems.map(q => q.applicationId);

    // Check if original is in queue or already processed/approved
    const originalSubmission = await db.submissions.findOne({
      applicationId: originalId,
      tenantId
    });

    if (!originalSubmission) {
      return res.status(400).json({
        success: false,
        error: 'Original submission not found'
      });
    }

    // Verify all duplicates are in pending queue
    const missingFromQueue = duplicateIds.filter(id => !queuedIds.includes(id));
    if (missingFromQueue.length > 0) {
      return res.status(400).json({
        success: false,
        error: 'Some submissions are not in your pending queue',
        missingIds: missingFromQueue
      });
    }

    const bulkService = new BulkDuplicateService(
      db,
      duplicateService,
      notificationService
    );

    const result = await bulkService.markBulkDuplicates(
      verifierId,
      originalId,
      duplicateIds,
      tenantId
    );

    return res.json(result);

  } catch (error) {
    console.error('Bulk duplicate marking failed:', error);
    return res.status(500).json({
      success: false,
      error: error.message
    });
  }
}

// GET /api/verifier/queue/similar-clusters
async function getSimilarClusters(req, res) {
  const { tenantId } = req.user;
  const { minSimilarity = 0.7, minClusterSize = 2 } = req.query;

  try {
    // Find submissions with similar hashes
    const clusters = await db.verifierQueue.aggregate([
      {
        $match: {
          tenantId,
          status: 'pending'
        }
      },
      {
        $lookup: {
          from: 'submissions',
          localField: 'applicationId',
          foreignField: 'applicationId',
          as: 'submission'
        }
      },
      {
        $unwind: '$submission'
      },
      {
        $addFields: {
          hashPrefix: { $substr: ['$submission.dogPhotoHash', 0, 8] }
        }
      },
      {
        $group: {
          _id: '$hashPrefix',
          items: {
            $push: {
              applicationId: '$applicationId',
              queueId: '$queueId',
              photoHash: '$submission.dogPhotoHash',
              location: '$submission.location',
              teacherId: '$submission.teacherId',
              submittedAt: '$submission.submittedAt'
            }
          },
          count: { $sum: 1 }
        }
      },
      {
        $match: {
          count: { $gte: parseInt(minClusterSize) }
        }
      },
      {
        $sort: { count: -1 }
      },
      {
        $limit: 10
      }
    ]).toArray();

    // Calculate actual similarity within each cluster
    const enrichedClusters = clusters.map(cluster => {
      const items = cluster.items;

      // Calculate pairwise similarities
      const similarities = [];
      for (let i = 0; i < items.length; i++) {
        for (let j = i + 1; j < items.length; j++) {
          const sim = calculateHashSimilarity(items[i].photoHash, items[j].photoHash);
          if (sim >= minSimilarity) {
            similarities.push({
              id1: items[i].applicationId,
              id2: items[j].applicationId,
              similarity: sim
            });
          }
        }
      }

      return {
        clusterSize: items.length,
        items: items.sort((a, b) => new Date(a.submittedAt) - new Date(b.submittedAt)),
        avgSimilarity: similarities.length > 0
          ? similarities.reduce((acc, s) => acc + s.similarity, 0) / similarities.length
          : 0,
        similarities
      };
    });

    return res.json({
      clusters: enrichedClusters.filter(c => c.avgSimilarity >= minSimilarity)
    });

  } catch (error) {
    console.error('Failed to get similar clusters:', error);
    return res.status(500).json({
      success: false,
      error: error.message
    });
  }
}
```

### Bulk Selection Component

```javascript
function BulkSelectionQueue({ items, onBulkAction }) {
  const [multiSelectMode, setMultiSelectMode] = useState(false);
  const [selectedIds, setSelectedIds] = useState(new Set());
  const [showWizard, setShowWizard] = useState(false);

  const MAX_SELECTION = 20;

  function toggleSelection(applicationId) {
    const newSelection = new Set(selectedIds);
    if (newSelection.has(applicationId)) {
      newSelection.delete(applicationId);
    } else if (newSelection.size < MAX_SELECTION) {
      newSelection.add(applicationId);
    }
    setSelectedIds(newSelection);
  }

  function selectAll() {
    const toSelect = items.slice(0, MAX_SELECTION).map(i => i.applicationId);
    setSelectedIds(new Set(toSelect));
  }

  function clearSelection() {
    setSelectedIds(new Set());
  }

  function handleBulkDuplicate() {
    if (selectedIds.size < 2) {
      alert('Select at least 2 submissions');
      return;
    }
    setShowWizard(true);
  }

  // Keyboard shortcut for multi-select mode
  useEffect(() => {
    function handleKeyPress(e) {
      if (e.key === 'm' && !e.target.matches('input, textarea')) {
        setMultiSelectMode(prev => !prev);
      }
    }
    window.addEventListener('keypress', handleKeyPress);
    return () => window.removeEventListener('keypress', handleKeyPress);
  }, []);

  return (
    <div className="bulk-selection-queue">
      <div className="queue-header">
        <h2>Verifier Queue</h2>
        <label className="multi-select-toggle">
          <input
            type="checkbox"
            checked={multiSelectMode}
            onChange={e => {
              setMultiSelectMode(e.target.checked);
              if (!e.target.checked) clearSelection();
            }}
          />
          Multi-Select Mode (M)
        </label>
      </div>

      {multiSelectMode && (
        <div className="selection-toolbar">
          <span className="selection-count">
            Selected: {selectedIds.size} items
          </span>
          <button onClick={selectAll} disabled={items.length === 0}>
            Select All (max {MAX_SELECTION})
          </button>
          <button onClick={clearSelection} disabled={selectedIds.size === 0}>
            Clear Selection
          </button>
        </div>
      )}

      <div className="queue-items">
        {items.map(item => (
          <QueueItem
            key={item.applicationId}
            item={item}
            multiSelectMode={multiSelectMode}
            isSelected={selectedIds.has(item.applicationId)}
            onToggleSelect={() => toggleSelection(item.applicationId)}
          />
        ))}
      </div>

      {multiSelectMode && selectedIds.size >= 2 && (
        <div className="bulk-actions-bar">
          <button
            className="btn-bulk-duplicate"
            onClick={handleBulkDuplicate}
          >
            🔗 Mark Selected as Duplicates ({selectedIds.size})
          </button>
        </div>
      )}

      {showWizard && (
        <BulkDuplicateWizard
          selectedIds={Array.from(selectedIds)}
          items={items.filter(i => selectedIds.has(i.applicationId))}
          onComplete={result => {
            setShowWizard(false);
            clearSelection();
            setMultiSelectMode(false);
            onBulkAction?.(result);
          }}
          onCancel={() => setShowWizard(false)}
        />
      )}
    </div>
  );
}
```

### Bulk Duplicate Wizard Component

```javascript
function BulkDuplicateWizard({ selectedIds, items, onComplete, onCancel }) {
  const [step, setStep] = useState(1);
  const [originalId, setOriginalId] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [isSearching, setIsSearching] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [progress, setProgress] = useState({ current: 0, total: 0, results: [] });

  const duplicateIds = selectedIds.filter(id => id !== originalId);

  async function searchExternal(query) {
    if (!query || query.length < 3) return;

    setIsSearching(true);
    try {
      const response = await fetch(
        `/api/verifier/search?q=${encodeURIComponent(query)}&exclude=${selectedIds.join(',')}`
      );
      const data = await response.json();
      setSearchResults(data.results);
    } catch (error) {
      console.error('Search failed:', error);
    } finally {
      setIsSearching(false);
    }
  }

  async function handleConfirm() {
    setIsProcessing(true);
    setProgress({ current: 0, total: duplicateIds.length, results: [] });

    try {
      const response = await fetch('/api/verifier/bulk/mark-duplicates', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          originalId,
          duplicateIds
        })
      });

      const result = await response.json();

      // Simulate progress for UX (actual processing is server-side)
      for (let i = 0; i < duplicateIds.length; i++) {
        await new Promise(r => setTimeout(r, 300));
        setProgress(prev => ({
          ...prev,
          current: i + 1,
          results: [
            ...prev.results,
            {
              id: duplicateIds[i],
              success: !result.results.failed.find(f => f.applicationId === duplicateIds[i])
            }
          ]
        }));
      }

      await new Promise(r => setTimeout(r, 500));
      onComplete(result);

    } catch (error) {
      console.error('Bulk action failed:', error);
      setIsProcessing(false);
    }
  }

  // Sort items by submission time (earliest first)
  const sortedItems = [...items].sort(
    (a, b) => new Date(a.submittedAt) - new Date(b.submittedAt)
  );

  if (isProcessing) {
    return (
      <div className="wizard-overlay">
        <div className="wizard-panel processing">
          <h3>Processing Bulk Action</h3>
          <p>Marking {duplicateIds.length} submissions as duplicates...</p>

          <div className="progress-bar">
            <div
              className="progress-fill"
              style={{ width: `${(progress.current / progress.total) * 100}%` }}
            />
          </div>
          <p className="progress-text">{progress.current}/{progress.total} complete</p>

          <div className="progress-list">
            {progress.results.map(r => (
              <div key={r.id} className={`progress-item ${r.success ? 'success' : 'failed'}`}>
                {r.success ? '✓' : '✗'} {r.id}
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="wizard-overlay">
      <div className="wizard-panel">
        <div className="wizard-header">
          <span className="icon">🔗</span>
          <h3>Bulk Mark as Duplicates</h3>
          <span className="step-indicator">Step {step} of 2</span>
        </div>

        {step === 1 && (
          <div className="wizard-body">
            <p>
              You have selected {selectedIds.length} submissions.
              Which one is the ORIGINAL that others are duplicates of?
            </p>

            <div className="original-options">
              <h4>Select from your selection:</h4>
              {sortedItems.map((item, idx) => (
                <label key={item.applicationId} className="original-option">
                  <input
                    type="radio"
                    name="original"
                    value={item.applicationId}
                    checked={originalId === item.applicationId}
                    onChange={() => setOriginalId(item.applicationId)}
                  />
                  <span className="option-label">
                    {item.applicationId}
                    {idx === 0 && <span className="badge">earliest</span>}
                  </span>
                  <button
                    className="preview-btn"
                    onClick={() => window.open(`/verifier/preview/${item.applicationId}`, '_blank')}
                  >
                    Preview
                  </button>
                </label>
              ))}

              <div className="divider">OR</div>

              <div className="external-search">
                <label>
                  <input
                    type="radio"
                    name="original"
                    value="external"
                    checked={originalId && !selectedIds.includes(originalId)}
                    onChange={() => setOriginalId(null)}
                  />
                  Search for a different original
                </label>
                <input
                  type="text"
                  placeholder="Search by ID or location..."
                  value={searchQuery}
                  onChange={e => {
                    setSearchQuery(e.target.value);
                    searchExternal(e.target.value);
                  }}
                  disabled={originalId && selectedIds.includes(originalId)}
                />
                {isSearching && <span className="searching">Searching...</span>}
                {searchResults.length > 0 && (
                  <div className="search-results">
                    {searchResults.map(result => (
                      <button
                        key={result.applicationId}
                        className={`search-result ${originalId === result.applicationId ? 'selected' : ''}`}
                        onClick={() => setOriginalId(result.applicationId)}
                      >
                        {result.applicationId} - {result.block}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            </div>

            <p className="tip">
              💡 Tip: The original should be the first/best quality submission.
              It will be kept for processing.
            </p>
          </div>
        )}

        {step === 2 && (
          <div className="wizard-body">
            <p>Please confirm this bulk duplicate action:</p>

            <div className="confirmation-summary">
              <div className="original-section">
                <h4>ORIGINAL (will be kept):</h4>
                <div className="submission-card original">
                  ✓ {originalId}
                </div>
              </div>

              <div className="duplicates-section">
                <h4>DUPLICATES (will be rejected):</h4>
                {duplicateIds.map(id => {
                  const item = items.find(i => i.applicationId === id);
                  return (
                    <div key={id} className="submission-card duplicate">
                      ✗ {id}
                      {item?.similarity && (
                        <span className="similarity">({Math.round(item.similarity * 100)}% similar)</span>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>

            <div className="warning-box">
              <strong>⚠️ This will:</strong>
              <ul>
                <li>Mark {duplicateIds.length} submissions as duplicates</li>
                <li>Notify {duplicateIds.length} teachers their reports were duplicates</li>
                <li>Remove {duplicateIds.length} items from your queue</li>
                <li>Keep 1 original for continued processing</li>
              </ul>
            </div>
          </div>
        )}

        <div className="wizard-footer">
          {step === 1 ? (
            <>
              <button className="btn-secondary" onClick={onCancel}>
                Cancel
              </button>
              <button
                className="btn-primary"
                onClick={() => setStep(2)}
                disabled={!originalId}
              >
                Next: Confirm →
              </button>
            </>
          ) : (
            <>
              <button className="btn-secondary" onClick={() => setStep(1)}>
                ← Back
              </button>
              <button className="btn-primary" onClick={handleConfirm}>
                🔗 Confirm & Mark ({duplicateIds.length})
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
```

---

## Metrics & Monitoring

```javascript
async function getBulkActionMetrics(tenantId, dateRange) {
  const bulkStats = await db.auditLog.aggregate([
    {
      $match: {
        type: 'BULK_DUPLICATE_COMPLETED',
        timestamp: { $gte: dateRange.start, $lte: dateRange.end }
      }
    },
    {
      $group: {
        _id: null,
        totalBulkActions: { $sum: 1 },
        totalDuplicatesMarked: { $sum: '$data.successCount' },
        totalFailed: { $sum: '$data.failedCount' },
        avgBatchSize: { $avg: { $add: ['$data.successCount', '$data.failedCount'] } },
        avgDuration: { $avg: '$data.durationMs' }
      }
    }
  ]).toArray();

  return {
    bulkActions: bulkStats[0]?.totalBulkActions || 0,
    duplicatesMarked: bulkStats[0]?.totalDuplicatesMarked || 0,
    failedItems: bulkStats[0]?.totalFailed || 0,
    avgBatchSize: Math.round(bulkStats[0]?.avgBatchSize || 0),
    avgDurationMs: Math.round(bulkStats[0]?.avgDuration || 0),
    successRate: bulkStats[0]
      ? ((bulkStats[0].totalDuplicatesMarked /
          (bulkStats[0].totalDuplicatesMarked + bulkStats[0].totalFailed)) * 100).toFixed(1)
      : 0
  };
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Duplicate Service | Internal | Individual duplicate marking |
| Notification Service | Internal | Batch teacher notifications |
| Hash Similarity | Utility | Cluster detection |

---

## Related Stories

- [V-REJ-02](./V-REJ-02.md) - Mark single duplicate
- [V-REV-02](./V-REV-02.md) - Compare potential duplicates
- [S-VAL-02](../system/S-VAL-02.md) - Hash similarity detection
