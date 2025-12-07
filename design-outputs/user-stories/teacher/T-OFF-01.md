# T-OFF-01: Offline Submission Queue

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** have my submission saved locally if upload fails,
**So that** I don't lose my work in areas with poor connectivity.

---

## Description

Teachers often work in areas with unreliable network connectivity. The PWA must support offline submission by storing the complete submission locally and automatically uploading when connectivity is restored. This ensures no data loss and a smooth user experience regardless of network conditions.

---

## Acceptance Criteria

### Functional

- [ ] Submission data (photos + metadata) stored locally on upload failure
- [ ] Storage uses IndexedDB for reliability and capacity
- [ ] Maximum 10 pending submissions allowed in queue
- [ ] Each queued submission shows "Pending Upload" status
- [ ] Queued submissions visible in "My Applications" with offline indicator
- [ ] Local submissions include all data: photos, GPS, tags, notes, timestamp
- [ ] Photos compressed before local storage to save space
- [ ] Queue persists across app restarts and device reboots
- [ ] Clear warning when offline queue is near capacity (8+ items)
- [ ] Teacher can manually delete queued submissions

### Storage Limits

| Attribute | Limit |
|-----------|-------|
| Max queued submissions | 10 |
| Max photo size (compressed) | 2MB per photo |
| Max total storage | ~50MB |
| Auto-expiry | 48 hours from capture |

### Offline Indicators

| State | Indicator |
|-------|-----------|
| No connectivity | 🔴 "Offline" banner at top |
| Queued submission | ⏳ "Pending Upload" status |
| Upload in progress | 🔄 "Uploading..." with progress |
| Upload failed | ⚠️ "Retry" option |

---

## UI/UX Requirements (PWA)

### Offline Banner

```
┌─────────────────────────────────┐
│ 🔴 You're offline. Submissions  │
│    will be saved locally.       │
└─────────────────────────────────┘
```

### Submission Queue Screen

```
┌─────────────────────────────────┐
│  ← Pending Uploads (3)          │
├─────────────────────────────────┤
│                                 │
│  ┌───────────────────────────┐  │
│  │ 📸 [Thumbnail]            │  │
│  │ ⏳ Pending Upload         │  │
│  │ 📍 Central Block          │  │
│  │ 🕐 2 hours ago            │  │
│  │                   [🗑️]    │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ 📸 [Thumbnail]            │  │
│  │ 🔄 Uploading... 45%       │  │
│  │ 📍 North Block            │  │
│  │ 🕐 3 hours ago            │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ 📸 [Thumbnail]            │  │
│  │ ⚠️ Upload Failed [Retry]  │  │
│  │ 📍 South Block            │  │
│  │ 🕐 5 hours ago            │  │
│  └───────────────────────────┘  │
│                                 │
│  ℹ️ Submissions auto-upload     │
│     when you're back online     │
│                                 │
└─────────────────────────────────┘
```

### Queue Full Warning

```
┌─────────────────────────────────┐
│  ⚠️ Queue Almost Full           │
├─────────────────────────────────┤
│                                 │
│  You have 8 pending uploads.    │
│  Maximum is 10.                 │
│                                 │
│  Please connect to internet     │
│  to upload pending submissions. │
│                                 │
│  [  OK  ]                       │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Register Service Worker for offline capability
- [ ] Use IndexedDB for structured data storage
- [ ] Store photos as Blobs in IndexedDB
- [ ] Implement Background Sync API for auto-upload
- [ ] Show offline indicator in app header
- [ ] Disable "Submit" if queue full and offline
- [ ] Progressive upload with resume capability

---

## Technical Notes

### IndexedDB Schema

```javascript
const DB_NAME = 'sdcrs-offline';
const DB_VERSION = 1;

const schema = {
  submissions: {
    keyPath: 'localId',
    indexes: ['status', 'createdAt'],
    fields: {
      localId: 'string', // UUID generated locally
      dogPhoto: 'Blob',
      selfiePhoto: 'Blob',
      latitude: 'number',
      longitude: 'number',
      conditionTags: 'array',
      notes: 'string',
      captureTimestamp: 'number',
      createdAt: 'number',
      status: 'string', // 'pending', 'uploading', 'failed', 'uploaded'
      uploadProgress: 'number',
      serverId: 'string', // Populated after successful upload
      retryCount: 'number'
    }
  }
};
```

### Service Worker Registration

```javascript
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/sw.js')
    .then(registration => {
      console.log('SW registered:', registration);
    });
}
```

### Background Sync

```javascript
// In Service Worker
self.addEventListener('sync', event => {
  if (event.tag === 'upload-submissions') {
    event.waitUntil(uploadPendingSubmissions());
  }
});

// Register sync when submission queued
async function queueSubmission(data) {
  await saveToIndexedDB(data);
  const registration = await navigator.serviceWorker.ready;
  await registration.sync.register('upload-submissions');
}
```

### Online/Offline Detection

```javascript
function setupConnectivityListeners() {
  window.addEventListener('online', () => {
    hideOfflineBanner();
    triggerPendingUploads();
  });

  window.addEventListener('offline', () => {
    showOfflineBanner();
  });
}
```

### Upload Queue Processing

```javascript
async function uploadPendingSubmissions() {
  const pending = await getPendingSubmissions();

  for (const submission of pending) {
    try {
      await updateStatus(submission.localId, 'uploading');
      const response = await uploadSubmission(submission);
      await updateStatus(submission.localId, 'uploaded', response.applicationId);
    } catch (error) {
      await incrementRetryCount(submission.localId);
      await updateStatus(submission.localId, 'failed');
    }
  }
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| IndexedDB | Browser | Local data storage |
| Service Worker | Browser | Offline capability |
| Background Sync API | Browser | Auto-upload when online |
| idb | Library | IndexedDB wrapper |

---

## Related Stories

- [T-OFF-02](./T-OFF-02.md) - Auto-retry uploads
- [T-OFF-03](./T-OFF-03.md) - Pending upload status
- [T-SUB-01](./T-SUB-01.md) - Capture dog photo
