# MC-CAP-01: Mark Captured/Resolved

> **Back to:** [User Stories Master](../../design-outputs/02-user-stories.md)

## User Story

**As an** MC Officer,
**I want to** mark an incident as "Captured" when I successfully capture or resolve the stray dog situation,
**So that** the incident is closed and the teacher can receive their payout.

## Description

When an MC Officer successfully captures a stray dog or resolves the situation (e.g., dog was already removed, owner claimed), they mark the incident as "Captured/Resolved". This is a critical action as it triggers the teacher's payout eligibility. The system requires confirmation and optionally resolution notes before completing the action.

**Important Business Rule:** Teacher payout is triggered ONLY after MC marks the incident as Captured/Resolved, not on verification approval.

## Acceptance Criteria

| # | Criterion | Priority |
|---|-----------|----------|
| 1 | "Mark Captured" button visible during IN_PROGRESS status | Must |
| 2 | Confirmation dialog before status change | Must |
| 3 | Resolution type selection (Captured, Owner Claimed, Already Removed) | Must |
| 4 | Optional photo of captured dog | Should |
| 5 | Optional resolution notes | Should |
| 6 | Timestamp and location recorded | Must |
| 7 | Status changes to CAPTURED | Must |
| 8 | Teacher payout process triggered | Must |
| 9 | Response time calculated and stored | Should |
| 10 | Works offline with sync when connected | Must |
| 11 | Success confirmation shown to officer | Must |

## UI/UX Design

### Mark Captured Confirmation

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│               ✅ Mark as Captured/Resolved                  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  INC-2024-0156                                             │
│  Response time: 45 minutes                                 │
│                                                             │
│  Resolution Type: *                                        │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ● Dog captured by MC team                          │   │
│  │  ○ Dog claimed by owner                             │   │
│  │  ○ Dog already removed from location                │   │
│  │  ○ Other resolution                                 │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Add Photo (Optional):                                     │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │              [📷 Take Photo]                        │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Resolution Notes (Optional):                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Dog was found near the reported location and       │   │
│  │ captured without incident. Transported to          │   │
│  │ municipal shelter.                                  │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │     [Cancel]           [✅ Confirm Captured]        │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Success Confirmation

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                    ✅ Incident Resolved                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│                         ╭─────╮                            │
│                        │  ✓  │                             │
│                        │     │                             │
│                         ╰─────╯                            │
│                                                             │
│              INC-2024-0156 has been resolved               │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Resolution: Dog captured by MC team                │   │
│  │  Response time: 45 minutes                          │   │
│  │  Completed at: 11:30 AM                            │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  The reporter will be notified and their payout           │
│  will be processed.                                        │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │           [🏠 Return to Incident List]              │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │           [📍 View Next Nearby Incident]            │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Active Response with Capture Button

```
┌─────────────────────────────────────────────────────────────┐
│ ◀ Back         Active Response           ⏱️ 00:45:12       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  INC-2024-0156                     🟢 IN PROGRESS   │   │
│  │  Route de l'Aéroport, Sector 4                      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Response started: 10:45 AM                                │
│  Current duration: 45 minutes                              │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │              [Map showing current position]         │   │
│  │                 📍 You are here                     │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                                                      │  │
│  │   ✅ Mark Captured                                   │  │
│  │   Dog captured or situation resolved                 │  │
│  │                                                      │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                                                      │  │
│  │   ❌ Unable to Locate                                │  │
│  │   Dog not found at location                         │  │
│  │                                                      │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Technical Implementation

### Capture Service

```javascript
// services/CaptureService.js
class CaptureService {
  constructor(baseUrl, offlineQueue) {
    this.baseUrl = baseUrl;
    this.offlineQueue = offlineQueue;
  }

  /**
   * Resolution types
   */
  static RESOLUTION_TYPES = {
    CAPTURED: {
      code: 'CAPTURED',
      label: 'Dog captured by MC team',
      triggersPayout: true
    },
    OWNER_CLAIMED: {
      code: 'OWNER_CLAIMED',
      label: 'Dog claimed by owner',
      triggersPayout: true
    },
    ALREADY_REMOVED: {
      code: 'ALREADY_REMOVED',
      label: 'Dog already removed from location',
      triggersPayout: true
    },
    OTHER: {
      code: 'OTHER',
      label: 'Other resolution',
      triggersPayout: true,
      requiresNotes: true
    }
  };

  /**
   * Mark incident as captured/resolved
   * @param {string} incidentId - Incident ID
   * @param {Object} resolution - Resolution details
   * @returns {Promise<Object>} Updated incident
   */
  async markCaptured(incidentId, resolution) {
    const {
      resolutionType,
      notes,
      photoFile,
      officerId,
      location
    } = resolution;

    // Validate resolution type
    const resolutionConfig = CaptureService.RESOLUTION_TYPES[resolutionType];
    if (!resolutionConfig) {
      throw new Error(`Invalid resolution type: ${resolutionType}`);
    }

    if (resolutionConfig.requiresNotes && !notes) {
      throw new Error('Resolution notes are required for this type');
    }

    // Prepare payload
    const payload = {
      incidentId,
      status: 'CAPTURED',
      resolutionType,
      resolutionNotes: notes || null,
      resolvedAt: new Date().toISOString(),
      resolvedBy: officerId,
      resolvedLocation: location ? {
        latitude: location.latitude,
        longitude: location.longitude,
        accuracy: location.accuracy
      } : null
    };

    // Upload photo if provided
    let photoId = null;
    if (photoFile) {
      photoId = await this.uploadResolutionPhoto(incidentId, photoFile);
      payload.resolutionPhotoId = photoId;
    }

    // Check online status
    if (!navigator.onLine) {
      return this.queueOfflineCapture(payload, photoFile);
    }

    try {
      const response = await fetch(
        `${this.baseUrl}/sdcrs/incidents/v1/${incidentId}/resolve`,
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${this.getAuthToken()}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(payload)
        }
      );

      if (!response.ok) {
        throw new Error(`Failed to mark captured: ${response.status}`);
      }

      const data = await response.json();

      // Clear response timer
      this.clearResponseTimer(incidentId);

      // Cache result
      await this.cacheResolution(data.incident);

      return {
        incident: data.incident,
        responseTime: data.responseTimeMinutes,
        payoutTriggered: data.payoutTriggered
      };

    } catch (error) {
      if (error.message.includes('network') || error.message.includes('fetch')) {
        return this.queueOfflineCapture(payload, photoFile);
      }
      throw error;
    }
  }

  /**
   * Upload resolution photo
   */
  async uploadResolutionPhoto(incidentId, photoFile) {
    const formData = new FormData();
    formData.append('file', photoFile);
    formData.append('type', 'RESOLUTION');
    formData.append('incidentId', incidentId);

    const response = await fetch(
      `${this.baseUrl}/sdcrs/photos/v1/upload`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${this.getAuthToken()}`
        },
        body: formData
      }
    );

    if (!response.ok) {
      console.warn('Photo upload failed, continuing without photo');
      return null;
    }

    const data = await response.json();
    return data.photoId;
  }

  /**
   * Queue capture action for offline sync
   */
  async queueOfflineCapture(payload, photoFile) {
    const queuedAction = {
      id: generateUUID(),
      type: 'MARK_CAPTURED',
      payload,
      photoFile: photoFile ? await this.fileToBase64(photoFile) : null,
      queuedAt: new Date().toISOString(),
      status: 'PENDING'
    };

    await this.offlineQueue.add(queuedAction);

    // Optimistically update local state
    await this.updateLocalIncidentStatus(payload.incidentId, 'CAPTURED');

    // Clear response timer locally
    this.clearResponseTimer(payload.incidentId);

    return {
      incident: {
        id: payload.incidentId,
        status: 'CAPTURED',
        resolutionType: payload.resolutionType,
        _offline: true,
        _pendingSync: true
      },
      responseTime: this.calculateResponseTime(payload.incidentId),
      payoutTriggered: true // Will be confirmed on sync
    };
  }

  /**
   * Convert file to base64 for offline storage
   */
  fileToBase64(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  /**
   * Calculate response time from stored timer
   */
  calculateResponseTime(incidentId) {
    const startTime = localStorage.getItem(`response_start_${incidentId}`);
    if (!startTime) return null;

    const elapsed = Date.now() - parseInt(startTime, 10);
    return Math.round(elapsed / (1000 * 60)); // Minutes
  }

  /**
   * Clear response timer
   */
  clearResponseTimer(incidentId) {
    localStorage.removeItem(`response_start_${incidentId}`);
  }

  getAuthToken() {
    return localStorage.getItem('auth_token');
  }
}

export default CaptureService;
```

### Mark Captured Component

```javascript
// components/MarkCapturedDialog.jsx
import React, { useState, useRef } from 'react';
import CaptureService from '../services/CaptureService';
import { useAuth } from '../hooks/useAuth';
import { useGeolocation } from '../hooks/useGeolocation';

const RESOLUTION_OPTIONS = Object.values(CaptureService.RESOLUTION_TYPES);

function MarkCapturedDialog({ incident, onSuccess, onCancel }) {
  const [resolutionType, setResolutionType] = useState('CAPTURED');
  const [notes, setNotes] = useState('');
  const [photo, setPhoto] = useState(null);
  const [photoPreview, setPhotoPreview] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fileInputRef = useRef(null);
  const { user } = useAuth();
  const { position } = useGeolocation();

  const captureService = new CaptureService('/api');

  const handlePhotoCapture = (event) => {
    const file = event.target.files[0];
    if (file) {
      setPhoto(file);
      setPhotoPreview(URL.createObjectURL(file));
    }
  };

  const handleSubmit = async () => {
    setLoading(true);
    setError(null);

    try {
      const result = await captureService.markCaptured(incident.id, {
        resolutionType,
        notes: notes.trim() || null,
        photoFile: photo,
        officerId: user.id,
        location: position ? {
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
          accuracy: position.coords.accuracy
        } : null
      });

      onSuccess(result);

    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const selectedType = RESOLUTION_OPTIONS.find(r => r.code === resolutionType);
  const requiresNotes = selectedType?.requiresNotes;

  return (
    <div className="dialog-overlay">
      <div className="dialog-content mark-captured-dialog">
        <h2>✅ Mark as Captured/Resolved</h2>

        <div className="incident-summary">
          <strong>{incident.incidentNumber}</strong>
          <p>Response time: {getResponseTimeDisplay(incident.id)}</p>
        </div>

        {/* Resolution Type */}
        <div className="form-group">
          <label>Resolution Type: *</label>
          <div className="radio-group">
            {RESOLUTION_OPTIONS.map(option => (
              <label key={option.code} className="radio-option">
                <input
                  type="radio"
                  name="resolutionType"
                  value={option.code}
                  checked={resolutionType === option.code}
                  onChange={(e) => setResolutionType(e.target.value)}
                />
                {option.label}
              </label>
            ))}
          </div>
        </div>

        {/* Photo Capture */}
        <div className="form-group">
          <label>Add Photo (Optional):</label>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            capture="environment"
            onChange={handlePhotoCapture}
            style={{ display: 'none' }}
          />

          {photoPreview ? (
            <div className="photo-preview">
              <img src={photoPreview} alt="Resolution photo" />
              <button
                className="btn-remove-photo"
                onClick={() => {
                  setPhoto(null);
                  setPhotoPreview(null);
                }}
              >
                ✕ Remove
              </button>
            </div>
          ) : (
            <button
              className="btn-capture-photo"
              onClick={() => fileInputRef.current?.click()}
            >
              📷 Take Photo
            </button>
          )}
        </div>

        {/* Resolution Notes */}
        <div className="form-group">
          <label>
            Resolution Notes {requiresNotes ? '*' : '(Optional)'}:
          </label>
          <textarea
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="Add any notes about the resolution..."
            rows={3}
            required={requiresNotes}
          />
        </div>

        {error && (
          <div className="error-message">
            ⚠️ {error}
          </div>
        )}

        <div className="dialog-actions">
          <button
            className="btn-secondary"
            onClick={onCancel}
            disabled={loading}
          >
            Cancel
          </button>
          <button
            className="btn-primary btn-success"
            onClick={handleSubmit}
            disabled={loading || (requiresNotes && !notes.trim())}
          >
            {loading ? 'Submitting...' : '✅ Confirm Captured'}
          </button>
        </div>
      </div>
    </div>
  );
}

function getResponseTimeDisplay(incidentId) {
  const startTime = localStorage.getItem(`response_start_${incidentId}`);
  if (!startTime) return 'N/A';

  const elapsed = Date.now() - parseInt(startTime, 10);
  const minutes = Math.round(elapsed / (1000 * 60));

  if (minutes < 60) {
    return `${minutes} minutes`;
  }
  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes % 60;
  return `${hours}h ${remainingMinutes}m`;
}

export default MarkCapturedDialog;
```

### Success Screen Component

```javascript
// components/CaptureSuccessScreen.jsx
import React from 'react';

function CaptureSuccessScreen({ result, onGoToList, onViewNextIncident }) {
  const { incident, responseTime, payoutTriggered } = result;

  return (
    <div className="success-screen">
      <div className="success-icon">
        <span className="checkmark">✓</span>
      </div>

      <h1>Incident Resolved</h1>
      <p className="incident-number">{incident.incidentNumber} has been resolved</p>

      <div className="resolution-summary">
        <div className="summary-item">
          <span className="label">Resolution:</span>
          <span className="value">
            {CaptureService.RESOLUTION_TYPES[incident.resolutionType]?.label}
          </span>
        </div>
        <div className="summary-item">
          <span className="label">Response time:</span>
          <span className="value">{responseTime} minutes</span>
        </div>
        <div className="summary-item">
          <span className="label">Completed at:</span>
          <span className="value">
            {new Date(incident.resolvedAt).toLocaleTimeString()}
          </span>
        </div>
      </div>

      {payoutTriggered && (
        <div className="payout-notice">
          <p>The reporter will be notified and their payout will be processed.</p>
        </div>
      )}

      {incident._pendingSync && (
        <div className="offline-notice">
          <span className="icon">📶</span>
          <p>Resolution saved offline. Will sync when connected.</p>
        </div>
      )}

      <div className="action-buttons">
        <button className="btn-primary" onClick={onGoToList}>
          🏠 Return to Incident List
        </button>
        <button className="btn-secondary" onClick={onViewNextIncident}>
          📍 View Next Nearby Incident
        </button>
      </div>
    </div>
  );
}

export default CaptureSuccessScreen;
```

## API Endpoints

### POST /sdcrs/incidents/v1/{incidentId}/resolve

Mark incident as captured/resolved.

**Request:**
```json
{
  "status": "CAPTURED",
  "resolutionType": "CAPTURED",
  "resolutionNotes": "Dog captured and transported to shelter",
  "resolutionPhotoId": "PHT-RES-001",
  "resolvedAt": "2024-01-15T11:30:00Z",
  "resolvedBy": "MC001",
  "resolvedLocation": {
    "latitude": 11.5879,
    "longitude": 43.1456,
    "accuracy": 10
  }
}
```

**Response:**
```json
{
  "ResponseInfo": { "status": "success" },
  "incident": {
    "id": "INC-2024-0156",
    "status": "CAPTURED",
    "resolutionType": "CAPTURED",
    "resolvedAt": "2024-01-15T11:30:00Z",
    "resolvedBy": "MC001"
  },
  "responseTimeMinutes": 45,
  "payoutTriggered": true,
  "payoutDetails": {
    "teacherId": "T001",
    "amount": 100,
    "status": "PENDING"
  }
}
```

## Dependencies

| Dependency | Purpose | Required |
|------------|---------|----------|
| MC-FLD-01 | Start response context | Yes |
| S-PTS-01 | Points calculation | Yes |
| T-PAY-01 | Payout triggering | Yes |
| idb | Offline queue | Yes |

## Related Stories

- [MC-FLD-01](./MC-FLD-01.md) - Update Status to In Progress
- [MC-CAP-02](./MC-CAP-02.md) - Add Resolution Notes
- [MC-NOT-01](./MC-NOT-01.md) - Notify Teacher on Capture
- [T-PAY-01](../teacher/T-PAY-01.md) - View Payout History

---

*Last Updated: 2024-01-15*
