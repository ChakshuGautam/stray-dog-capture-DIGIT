# MC-FLD-01: Update Status to "In Progress"

> **Back to:** [User Stories Master](../../design-outputs/02-user-stories.md)

## User Story

**As an** MC Officer,
**I want to** update the incident status to "In Progress" when I start responding,
**So that** other officers know the incident is being handled.

## Description

When an MC Officer begins responding to an incident, they mark it as "In Progress" to prevent duplicate responses and to provide visibility into active field work. This status change is logged with timestamp and officer details, and triggers notifications to relevant parties.

## Acceptance Criteria

| # | Criterion | Priority |
|---|-----------|----------|
| 1 | One-tap "Start Response" button on incident details | Must |
| 2 | Confirmation dialog before status change | Should |
| 3 | Status changes to IN_PROGRESS immediately | Must |
| 4 | Timestamp and officer ID recorded in audit log | Must |
| 5 | Other officers see incident as "Being handled" | Must |
| 6 | Officer's current location recorded at start | Should |
| 7 | Works offline with sync when connected | Must |
| 8 | Only one officer can have incident in progress | Must |
| 9 | Timer starts to track response time | Should |
| 10 | Quick access to navigation after starting | Should |

## UI/UX Design

### Start Response Confirmation

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                   Start Response?                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  You are about to start responding to:                     │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  INC-2026-0156                                      │   │
│  │  Route de l'Aéroport, Sector 4                      │   │
│  │  Distance: 2.3 km                                   │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  This will:                                                │
│  • Mark the incident as "In Progress"                      │
│  • Assign you as the responding officer                    │
│  • Start the response timer                                │
│  • Record your current location                            │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │       [Cancel]         [▶️ Start Response]          │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Active Response View

```
┌─────────────────────────────────────────────────────────────┐
│ ◀ Back         Active Response           ⏱️ 00:15:32       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  INC-2026-0156                     🟢 IN PROGRESS   │   │
│  │  ─────────────────────────────────────────────      │   │
│  │  Route de l'Aéroport, Sector 4                      │   │
│  │  Started: 10:45 AM                                  │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │              [Map with route to incident]           │   │
│  │                   📍 ─────────→ 🐕                  │   │
│  │                                                     │   │
│  │  ETA: 8 minutes • 2.3 km                           │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Quick Actions:                                            │
│  ┌────────────────┐ ┌────────────────┐                    │
│  │  🧭 Navigate   │ │  📷 View Photos │                    │
│  └────────────────┘ └────────────────┘                    │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ⚠️ Aggressive behavior reported                    │   │
│  │  Medium-sized brown dog, possibly injured           │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────────────┐ ┌──────────────────────┐        │
│  │   ✅ Mark Captured   │ │   ❌ Unable to Locate │        │
│  └──────────────────────┘ └──────────────────────┘        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Already In Progress Warning

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                  ⚠️ Already In Progress                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  This incident is already being handled by another         │
│  officer.                                                  │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  INC-2026-0156                                      │   │
│  │  Status: IN_PROGRESS                                │   │
│  │  Started: 10:30 AM (15 minutes ago)                │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Please select a different incident to respond to.         │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              [ View Other Incidents ]               │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Technical Implementation

### Response Service

```javascript
// services/ResponseService.js
class ResponseService {
  constructor(baseUrl, offlineQueue) {
    this.baseUrl = baseUrl;
    this.offlineQueue = offlineQueue;
  }

  /**
   * Start response to an incident
   * @param {string} incidentId - Incident ID
   * @param {Object} options - Additional options
   * @returns {Promise<Object>} Updated incident
   */
  async startResponse(incidentId, options = {}) {
    const {
      officerId,
      officerLocation,
      notes
    } = options;

    // Prepare request payload
    const payload = {
      incidentId,
      action: 'START_RESPONSE',
      officerId,
      timestamp: new Date().toISOString(),
      location: officerLocation ? {
        latitude: officerLocation.latitude,
        longitude: officerLocation.longitude,
        accuracy: officerLocation.accuracy
      } : null,
      notes
    };

    // Check if online
    if (!navigator.onLine) {
      return this.queueOfflineAction(payload);
    }

    try {
      const response = await fetch(
        `${this.baseUrl}/sdcrs/incidents/v1/${incidentId}/status`,
        {
          method: 'PATCH',
          headers: {
            'Authorization': `Bearer ${this.getAuthToken()}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            status: 'IN_PROGRESS',
            assignedOfficerId: officerId,
            startedAt: payload.timestamp,
            startLocation: payload.location
          })
        }
      );

      if (response.status === 409) {
        // Conflict - already in progress
        const error = await response.json();
        throw new ConflictError('Incident already in progress', error);
      }

      if (!response.ok) {
        throw new Error(`Failed to start response: ${response.status}`);
      }

      const data = await response.json();

      // Start local response timer
      this.startResponseTimer(incidentId);

      // Cache updated incident
      await this.cacheIncident(data.incident);

      return data.incident;

    } catch (error) {
      if (error.name === 'ConflictError') {
        throw error;
      }

      // Network error - queue for later
      console.warn('Network error, queuing action:', error);
      return this.queueOfflineAction(payload);
    }
  }

  /**
   * Queue action for offline sync
   */
  async queueOfflineAction(payload) {
    const queuedAction = {
      id: generateUUID(),
      type: 'START_RESPONSE',
      payload,
      queuedAt: new Date().toISOString(),
      status: 'PENDING'
    };

    await this.offlineQueue.add(queuedAction);

    // Optimistically update local state
    await this.updateLocalIncidentStatus(
      payload.incidentId,
      'IN_PROGRESS',
      payload.officerId
    );

    return {
      id: payload.incidentId,
      status: 'IN_PROGRESS',
      _offline: true,
      _pendingSync: true
    };
  }

  /**
   * Start response timer
   */
  startResponseTimer(incidentId) {
    const startTime = Date.now();

    // Store start time
    localStorage.setItem(`response_start_${incidentId}`, startTime.toString());

    // Dispatch timer started event
    window.dispatchEvent(new CustomEvent('responseTimerStarted', {
      detail: { incidentId, startTime }
    }));
  }

  /**
   * Get elapsed response time
   */
  getElapsedTime(incidentId) {
    const startTime = localStorage.getItem(`response_start_${incidentId}`);
    if (!startTime) return null;

    const elapsed = Date.now() - parseInt(startTime, 10);
    return {
      milliseconds: elapsed,
      formatted: this.formatDuration(elapsed)
    };
  }

  /**
   * Format duration as HH:MM:SS
   */
  formatDuration(ms) {
    const seconds = Math.floor(ms / 1000) % 60;
    const minutes = Math.floor(ms / (1000 * 60)) % 60;
    const hours = Math.floor(ms / (1000 * 60 * 60));

    return [hours, minutes, seconds]
      .map(v => v.toString().padStart(2, '0'))
      .join(':');
  }

  getAuthToken() {
    return localStorage.getItem('auth_token');
  }
}

class ConflictError extends Error {
  constructor(message, details) {
    super(message);
    this.name = 'ConflictError';
    this.details = details;
  }
}

export default ResponseService;
```

### Start Response Component

```javascript
// components/StartResponseButton.jsx
import React, { useState } from 'react';
import ResponseService from '../services/ResponseService';
import { useGeolocation } from '../hooks/useGeolocation';
import { useAuth } from '../hooks/useAuth';

function StartResponseButton({ incident, onStarted, onError }) {
  const [showConfirm, setShowConfirm] = useState(false);
  const [loading, setLoading] = useState(false);
  const { user } = useAuth();
  const { position, error: geoError } = useGeolocation();

  const responseService = new ResponseService('/api');

  const handleStart = async () => {
    setLoading(true);

    try {
      const result = await responseService.startResponse(incident.id, {
        officerId: user.id,
        officerLocation: position ? {
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
          accuracy: position.coords.accuracy
        } : null
      });

      onStarted?.(result);
      setShowConfirm(false);

    } catch (error) {
      if (error.name === 'ConflictError') {
        onError?.({
          type: 'CONFLICT',
          message: 'This incident is already being handled by another officer.'
        });
      } else {
        onError?.({
          type: 'ERROR',
          message: error.message
        });
      }
    } finally {
      setLoading(false);
    }
  };

  const isDisabled = incident.status !== 'NEW' && incident.status !== 'ASSIGNED';

  return (
    <>
      <button
        className="btn-primary btn-start-response"
        onClick={() => setShowConfirm(true)}
        disabled={isDisabled || loading}
      >
        {loading ? 'Starting...' : '▶️ Start Response'}
      </button>

      {showConfirm && (
        <ConfirmationDialog
          title="Start Response?"
          incident={incident}
          position={position}
          onConfirm={handleStart}
          onCancel={() => setShowConfirm(false)}
          loading={loading}
        />
      )}
    </>
  );
}

function ConfirmationDialog({
  title,
  incident,
  position,
  onConfirm,
  onCancel,
  loading
}) {
  const distance = position
    ? calculateDistance(
        position.coords.latitude,
        position.coords.longitude,
        incident.location.latitude,
        incident.location.longitude
      )
    : null;

  return (
    <div className="dialog-overlay">
      <div className="dialog-content">
        <h2>{title}</h2>

        <p>You are about to start responding to:</p>

        <div className="incident-summary">
          <strong>{incident.incidentNumber}</strong>
          <p>{incident.location.address}</p>
          {distance && <p>Distance: {distance.toFixed(1)} km</p>}
        </div>

        <div className="info-list">
          <p>This will:</p>
          <ul>
            <li>Mark the incident as "In Progress"</li>
            <li>Assign you as the responding officer</li>
            <li>Start the response timer</li>
            {position && <li>Record your current location</li>}
          </ul>
        </div>

        <div className="dialog-actions">
          <button
            className="btn-secondary"
            onClick={onCancel}
            disabled={loading}
          >
            Cancel
          </button>
          <button
            className="btn-primary"
            onClick={onConfirm}
            disabled={loading}
          >
            {loading ? 'Starting...' : '▶️ Start Response'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default StartResponseButton;
```

### Active Response Timer Component

```javascript
// components/ResponseTimer.jsx
import React, { useState, useEffect } from 'react';
import ResponseService from '../services/ResponseService';

function ResponseTimer({ incidentId }) {
  const [elapsed, setElapsed] = useState('00:00:00');
  const responseService = new ResponseService('/api');

  useEffect(() => {
    const updateTimer = () => {
      const time = responseService.getElapsedTime(incidentId);
      if (time) {
        setElapsed(time.formatted);
      }
    };

    // Update immediately
    updateTimer();

    // Update every second
    const interval = setInterval(updateTimer, 1000);

    return () => clearInterval(interval);
  }, [incidentId]);

  return (
    <div className="response-timer">
      <span className="timer-icon">⏱️</span>
      <span className="timer-value">{elapsed}</span>
    </div>
  );
}

export default ResponseTimer;
```

### Offline Queue Service

```javascript
// services/OfflineQueueService.js
import { openDB } from 'idb';

const DB_NAME = 'sdcrs-offline';
const STORE_NAME = 'action-queue';

class OfflineQueueService {
  constructor() {
    this.dbPromise = this.initDB();
  }

  async initDB() {
    return openDB(DB_NAME, 1, {
      upgrade(db) {
        if (!db.objectStoreNames.contains(STORE_NAME)) {
          const store = db.createObjectStore(STORE_NAME, { keyPath: 'id' });
          store.createIndex('status', 'status');
          store.createIndex('queuedAt', 'queuedAt');
        }
      }
    });
  }

  async add(action) {
    const db = await this.dbPromise;
    await db.put(STORE_NAME, action);
  }

  async getPending() {
    const db = await this.dbPromise;
    return db.getAllFromIndex(STORE_NAME, 'status', 'PENDING');
  }

  async markSynced(actionId) {
    const db = await this.dbPromise;
    const action = await db.get(STORE_NAME, actionId);
    if (action) {
      action.status = 'SYNCED';
      action.syncedAt = new Date().toISOString();
      await db.put(STORE_NAME, action);
    }
  }

  async markFailed(actionId, error) {
    const db = await this.dbPromise;
    const action = await db.get(STORE_NAME, actionId);
    if (action) {
      action.status = 'FAILED';
      action.error = error;
      action.failedAt = new Date().toISOString();
      await db.put(STORE_NAME, action);
    }
  }

  /**
   * Sync pending actions when back online
   */
  async syncPendingActions() {
    const pending = await this.getPending();

    for (const action of pending) {
      try {
        await this.syncAction(action);
        await this.markSynced(action.id);
      } catch (error) {
        console.error('Failed to sync action:', action.id, error);
        await this.markFailed(action.id, error.message);
      }
    }
  }

  async syncAction(action) {
    // Implement based on action type
    switch (action.type) {
      case 'START_RESPONSE':
        return this.syncStartResponse(action.payload);
      case 'MARK_CAPTURED':
        return this.syncMarkCaptured(action.payload);
      case 'MARK_UTL':
        return this.syncMarkUTL(action.payload);
      default:
        throw new Error(`Unknown action type: ${action.type}`);
    }
  }
}

export default OfflineQueueService;
```

## API Endpoints

### PATCH /sdcrs/incidents/v1/{incidentId}/status

Update incident status.

**Request:**
```json
{
  "status": "IN_PROGRESS",
  "assignedOfficerId": "MC001",
  "startedAt": "2026-01-15T10:45:00Z",
  "startLocation": {
    "latitude": 11.5850,
    "longitude": 43.1420,
    "accuracy": 10
  }
}
```

**Success Response (200):**
```json
{
  "ResponseInfo": { "status": "success" },
  "incident": {
    "id": "INC-2026-0156",
    "status": "IN_PROGRESS",
    "assignedOfficerId": "MC001",
    "startedAt": "2026-01-15T10:45:00Z"
  }
}
```

**Conflict Response (409):**
```json
{
  "ResponseInfo": { "status": "error" },
  "error": {
    "code": "INCIDENT_ALREADY_IN_PROGRESS",
    "message": "This incident is already being handled",
    "currentOfficerId": "MC002",
    "startedAt": "2026-01-15T10:30:00Z"
  }
}
```

## Dependencies

| Dependency | Purpose | Required |
|------------|---------|----------|
| MC-VIEW-04 | Incident details context | Yes |
| idb | IndexedDB for offline queue | Yes |
| Geolocation API | Officer location capture | No |

## Related Stories

- [MC-VIEW-04](./MC-VIEW-04.md) - Incident Details View
- [MC-FLD-02](./MC-FLD-02.md) - Navigate to Incident Location
- [MC-CAP-01](./MC-CAP-01.md) - Mark Captured/Resolved

---

*Last Updated: 2026-01-15*
