# T-OFF-03: Pending Upload Status

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** see which of my submissions are still pending upload,
**So that** I know my work is saved and will be submitted.

---

## Description

Teachers need visibility into the status of their queued submissions. The app should clearly show which submissions are pending, uploading, or failed. This provides reassurance that data isn't lost and helps teachers manage their offline submissions effectively.

---

## Acceptance Criteria

### Functional

- [ ] Pending uploads badge shown on "My Applications" menu item
- [ ] Pending count shown in app header when items queued
- [ ] Each queued submission shows clear status indicator
- [ ] Differentiate between: Pending, Uploading, Failed, Uploaded
- [ ] Show capture timestamp for each queued item
- [ ] Show thumbnail preview of dog photo
- [ ] Show location summary (reverse geocoded address)
- [ ] Teacher can manually delete pending submissions
- [ ] Teacher can manually trigger retry for failed uploads
- [ ] Status updates in real-time when viewing the queue

### Status Indicators

| Status | Icon | Description |
|--------|------|-------------|
| Pending | ⏳ | Waiting in queue |
| Uploading | 🔄 | Currently uploading |
| Failed | ⚠️ | Upload failed, retry available |
| Uploaded | ✓ | Successfully submitted |

### Queue Visibility Locations

| Location | Display |
|----------|---------|
| App Header | Badge count when offline items exist |
| Bottom Navigation | Badge on "My Applications" icon |
| My Applications Screen | "Pending Uploads" section at top |
| Home Screen | Banner if pending items exist |

---

## UI/UX Requirements (PWA)

### App Header with Pending Badge

```
┌─────────────────────────────────┐
│  ☰  SDCRS           ⏳ 3  👤    │
└─────────────────────────────────┘
```

### Bottom Navigation with Badge

```
┌─────────────────────────────────┐
│                                 │
│  🏠      📝        📋      ⚙️   │
│ Home   Submit   Apps(3)  More  │
│                   🔴            │
└─────────────────────────────────┘
```

### Pending Uploads Section

```
┌─────────────────────────────────┐
│  ← My Applications              │
├─────────────────────────────────┤
│                                 │
│  ┌───────────────────────────┐  │
│  │  ⏳ 3 Pending Uploads      │  │
│  │  Will auto-upload when     │  │
│  │  you're back online        │  │
│  │                   [View]   │  │
│  └───────────────────────────┘  │
│                                 │
│  ─ Submitted Applications ─    │
│                                 │
│  ┌───────────────────────────┐  │
│  │ SDCRS-20241207-A1B2C      │  │
│  │ 🟢 Verified                │  │
│  │ 📍 Central Block           │  │
│  │ 🕐 Dec 7, 2024             │  │
│  └───────────────────────────┘  │
│                                 │
└─────────────────────────────────┘
```

### Detailed Queue View

```
┌─────────────────────────────────┐
│  ← Pending Uploads (3)          │
├─────────────────────────────────┤
│                                 │
│  ℹ️ These submissions are saved │
│     locally and will upload     │
│     automatically when online.  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ ┌─────┐                   │  │
│  │ │ 📸  │ ⏳ Pending         │  │
│  │ │     │ 📍 Central Block   │  │
│  │ └─────┘ 🕐 2h ago          │  │
│  │              [🗑️ Delete]   │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ ┌─────┐                   │  │
│  │ │ 📸  │ 🔄 Uploading 67%  │  │
│  │ │     │ 📍 North Block     │  │
│  │ └─────┘ 🕐 3h ago          │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ ┌─────┐                   │  │
│  │ │ 📸  │ ⚠️ Failed          │  │
│  │ │     │ 📍 South Block     │  │
│  │ └─────┘ 🕐 5h ago          │  │
│  │         [Retry] [Delete]   │  │
│  └───────────────────────────┘  │
│                                 │
│  ───────────────────────────── │
│  Storage: 12MB / 50MB used     │
│                                 │
└─────────────────────────────────┘
```

### Delete Confirmation

```
┌─────────────────────────────────┐
│  🗑️ Delete Submission?          │
├─────────────────────────────────┤
│                                 │
│  This pending submission will   │
│  be permanently deleted.        │
│                                 │
│  📸 [Thumbnail]                 │
│  📍 South Block                  │
│  🕐 5 hours ago                  │
│                                 │
│  This action cannot be undone.  │
│                                 │
│  [  Cancel  ]  [  Delete  ]    │
│                                 │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Badge API for favicon badge count
- [ ] Real-time status updates using state management
- [ ] Pull-to-refresh to check upload status
- [ ] Swipe to delete on individual items
- [ ] Show storage usage from IndexedDB
- [ ] Animate status transitions
- [ ] Persist expanded/collapsed queue view state

---

## Technical Notes

### Badge API for Pending Count

```javascript
// Update app badge with pending count
async function updateBadge() {
  const pendingCount = await getPendingCount();

  if ('setAppBadge' in navigator) {
    if (pendingCount > 0) {
      await navigator.setAppBadge(pendingCount);
    } else {
      await navigator.clearAppBadge();
    }
  }
}

// Call when queue changes
async function onQueueChange() {
  await updateBadge();
  notifyUIComponents();
}
```

### Queue Status Subscription

```javascript
// React hook for queue status
function usePendingSubmissions() {
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadSubmissions = async () => {
      const pending = await getAllFromIndexedDB('submissions');
      setSubmissions(pending.filter(s => s.status !== 'uploaded'));
      setLoading(false);
    };

    loadSubmissions();

    // Listen for changes
    const channel = new BroadcastChannel('submission-updates');
    channel.onmessage = () => loadSubmissions();

    return () => channel.close();
  }, []);

  return { submissions, loading };
}
```

### Storage Usage Calculation

```javascript
async function getStorageUsage() {
  if ('storage' in navigator && 'estimate' in navigator.storage) {
    const { usage, quota } = await navigator.storage.estimate();
    return {
      used: usage,
      total: quota,
      percentage: Math.round((usage / quota) * 100)
    };
  }

  // Fallback: calculate from IndexedDB
  const submissions = await getAllFromIndexedDB('submissions');
  const totalSize = submissions.reduce((sum, s) => {
    return sum + (s.dogPhoto?.size || 0) + (s.selfiePhoto?.size || 0);
  }, 0);

  return {
    used: totalSize,
    total: 50 * 1024 * 1024, // 50MB limit
    percentage: Math.round((totalSize / (50 * 1024 * 1024)) * 100)
  };
}
```

### Delete Pending Submission

```javascript
async function deletePendingSubmission(localId) {
  // Remove from IndexedDB
  await deleteFromIndexedDB('submissions', localId);

  // Update badge
  await updateBadge();

  // Notify UI
  const channel = new BroadcastChannel('submission-updates');
  channel.postMessage({ type: 'deleted', localId });

  // Show toast
  showToast('Submission deleted');
}
```

### Manual Retry

```javascript
async function retryUpload(localId) {
  // Reset status and retry count
  await updateInIndexedDB('submissions', localId, {
    status: 'pending',
    retryCount: 0
  });

  // Trigger sync
  if ('serviceWorker' in navigator && 'SyncManager' in window) {
    const registration = await navigator.serviceWorker.ready;
    await registration.sync.register('upload-submissions');
  } else {
    uploadPendingSubmissions();
  }

  showToast('Retrying upload...');
}
```

### Real-time Progress Updates

```javascript
// Service Worker posts progress updates
async function uploadWithProgress(submission) {
  const formData = createFormData(submission);

  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();

    xhr.upload.onprogress = (event) => {
      if (event.lengthComputable) {
        const progress = Math.round((event.loaded / event.total) * 100);

        // Notify main thread
        const channel = new BroadcastChannel('submission-updates');
        channel.postMessage({
          type: 'progress',
          localId: submission.localId,
          progress
        });
      }
    };

    xhr.onload = () => {
      if (xhr.status === 200) {
        resolve(JSON.parse(xhr.responseText));
      } else {
        reject(new Error(`HTTP ${xhr.status}`));
      }
    };

    xhr.onerror = () => reject(new Error('Network error'));

    xhr.open('POST', '/api/submissions');
    xhr.setRequestHeader('Authorization', `Bearer ${getToken()}`);
    xhr.send(formData);
  });
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Badging API | Browser | App badge count |
| BroadcastChannel | Browser | Cross-context messaging |
| Storage API | Browser | Storage usage estimation |
| IndexedDB | Browser | Queue persistence |

---

## Related Stories

- [T-OFF-01](./T-OFF-01.md) - Offline submission queue
- [T-OFF-02](./T-OFF-02.md) - Auto-retry uploads
- [T-STAT-01](./T-STAT-01.md) - View submission status
