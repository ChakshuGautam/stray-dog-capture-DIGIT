# T-OFF-02: Auto-Retry Uploads

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** have my queued submissions automatically retry upload when I'm online,
**So that** I don't need to manually intervene for each pending submission.

---

## Description

When connectivity is restored, the PWA should automatically detect the online status and begin uploading pending submissions from the offline queue. This happens in the background without requiring the teacher to open the app or manually trigger uploads. The Background Sync API enables this even when the app is closed.

---

## Acceptance Criteria

### Functional

- [ ] Auto-upload triggered when device comes online
- [ ] Background Sync API used for reliable upload even if app is closed
- [ ] Uploads processed sequentially (one at a time) to avoid failures
- [ ] Failed uploads retried with exponential backoff
- [ ] Maximum 3 retry attempts per submission
- [ ] After 3 failures, submission marked as "Manual Retry Required"
- [ ] Progress visible when app is open during upload
- [ ] Successful uploads removed from offline queue automatically
- [ ] Notification shown when background upload completes

### Retry Strategy

| Attempt | Delay | Action |
|---------|-------|--------|
| 1 | Immediate | First try on connectivity restore |
| 2 | 5 minutes | Wait and retry |
| 3 | 15 minutes | Wait and retry |
| 4+ | Manual | Requires user action |

### Network Conditions

| Condition | Behavior |
|-----------|----------|
| WiFi connected | Upload immediately |
| Mobile data | Upload immediately (small payload) |
| Metered connection | Upload with user consent |
| Offline | Queue and wait |

---

## UI/UX Requirements (PWA)

### Background Upload Progress (When App Open)

```
┌─────────────────────────────────┐
│  ← My Applications              │
├─────────────────────────────────┤
│                                 │
│  ┌───────────────────────────┐  │
│  │ 🔄 Uploading 2 of 3...    │  │
│  │ ████████░░░░░░░░ 45%      │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ 📸 [Thumbnail]            │  │
│  │ ✓ Uploaded                │  │
│  │ 📍 Central Block          │  │
│  │ ID: SDCRS-20241207-A1B2C  │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ 📸 [Thumbnail]            │  │
│  │ 🔄 Uploading... 45%       │  │
│  │ 📍 North Block            │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ 📸 [Thumbnail]            │  │
│  │ ⏳ Queued                 │  │
│  │ 📍 South Block            │  │
│  └───────────────────────────┘  │
│                                 │
└─────────────────────────────────┘
```

### Background Upload Notification

```
┌─────────────────────────────────┐
│  ✓ SDCRS                        │
│  3 submissions uploaded         │
│  successfully while offline     │
│                                 │
│  Tap to view details            │
└─────────────────────────────────┘
```

### Manual Retry Required

```
┌─────────────────────────────────┐
│  ⚠️ Upload Failed               │
├─────────────────────────────────┤
│                                 │
│  This submission failed after   │
│  multiple retry attempts.       │
│                                 │
│  📸 [Thumbnail]                 │
│  📍 East Block                  │
│  🕐 Captured 2 days ago         │
│                                 │
│  Error: Network timeout         │
│                                 │
│  [  Retry Now  ]                │
│  [  Delete Submission  ]        │
│                                 │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Register Background Sync for 'upload-submissions' tag
- [ ] Use periodic sync for checking stuck uploads
- [ ] Show notification permission prompt for background alerts
- [ ] Implement exponential backoff timer
- [ ] Track retry count in IndexedDB
- [ ] Clear queue item only after server confirms receipt
- [ ] Handle partial upload failures gracefully

---

## Technical Notes

### Background Sync Registration

```javascript
// Register sync when submission queued
async function queueSubmissionForSync(submission) {
  await saveToIndexedDB(submission);

  if ('serviceWorker' in navigator && 'SyncManager' in window) {
    const registration = await navigator.serviceWorker.ready;
    await registration.sync.register('upload-submissions');
  } else {
    // Fallback: try immediate upload
    uploadPendingSubmissions();
  }
}
```

### Service Worker Sync Handler

```javascript
// sw.js
self.addEventListener('sync', event => {
  if (event.tag === 'upload-submissions') {
    event.waitUntil(uploadAllPending());
  }
});

async function uploadAllPending() {
  const pending = await getPendingFromIndexedDB();

  for (const submission of pending) {
    try {
      await uploadWithRetry(submission);
      await markAsUploaded(submission.localId);
      await showNotification('Upload Complete', {
        body: `Submission uploaded successfully`,
        tag: 'upload-success'
      });
    } catch (error) {
      await incrementRetryCount(submission.localId);
      if (submission.retryCount >= 3) {
        await markAsFailedPermanent(submission.localId);
      }
    }
  }
}
```

### Exponential Backoff

```javascript
async function uploadWithRetry(submission, attempt = 1) {
  const maxAttempts = 3;
  const baseDelay = 5 * 60 * 1000; // 5 minutes

  try {
    const response = await fetch('/api/submissions', {
      method: 'POST',
      body: createFormData(submission),
      headers: { 'Authorization': `Bearer ${getToken()}` }
    });

    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();

  } catch (error) {
    if (attempt >= maxAttempts) throw error;

    const delay = baseDelay * Math.pow(2, attempt - 1);
    await sleep(delay);
    return uploadWithRetry(submission, attempt + 1);
  }
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}
```

### Online/Offline Detection

```javascript
// Detect connectivity changes
window.addEventListener('online', async () => {
  console.log('Back online - triggering sync');

  if ('serviceWorker' in navigator && 'SyncManager' in window) {
    const registration = await navigator.serviceWorker.ready;
    await registration.sync.register('upload-submissions');
  } else {
    uploadPendingSubmissions();
  }
});

window.addEventListener('offline', () => {
  console.log('Gone offline - uploads will queue');
  showOfflineBanner();
});
```

### Notification for Background Uploads

```javascript
// In Service Worker
async function showUploadNotification(count) {
  const options = {
    body: `${count} submission(s) uploaded successfully`,
    icon: '/icons/icon-192.png',
    badge: '/icons/badge-72.png',
    tag: 'upload-complete',
    data: { url: '/my-applications' },
    requireInteraction: false
  };

  await self.registration.showNotification('SDCRS Upload Complete', options);
}

// Handle notification click
self.addEventListener('notificationclick', event => {
  event.notification.close();
  event.waitUntil(
    clients.openWindow(event.notification.data.url)
  );
});
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Background Sync API | Browser | Reliable background uploads |
| Service Worker | Browser | Background processing |
| Notifications API | Browser | Alert on completion |
| IndexedDB | Browser | Queue persistence |

---

## Related Stories

- [T-OFF-01](./T-OFF-01.md) - Offline submission queue
- [T-OFF-03](./T-OFF-03.md) - Pending upload status
- [T-STAT-01](./T-STAT-01.md) - View submission status
