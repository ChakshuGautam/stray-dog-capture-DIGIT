# MC-UTL-01: Mark Unable to Locate

> **Back to:** [User Stories Master](../../design-outputs/02-user-stories.md)

## User Story

**As an** MC Officer,
**I want to** mark an incident as "Unable to Locate" when I cannot find the dog at the reported location,
**So that** the incident is properly closed and accurate records are maintained.

## Description

Sometimes MC Officers arrive at a reported location but cannot find the stray dog. This may happen because the dog moved, was already removed by someone else, or the location information was inaccurate. The "Unable to Locate" (UTL) status allows officers to close these incidents properly while maintaining accurate statistics.

**Important:** UTL incidents do NOT trigger teacher payout, as the dog was not successfully captured.

## Acceptance Criteria

| # | Criterion | Priority |
|---|-----------|----------|
| 1 | "Unable to Locate" button visible during IN_PROGRESS status | Must |
| 2 | Confirmation dialog with reason selection | Must |
| 3 | Mandatory UTL reason selection | Must |
| 4 | Optional additional notes field | Should |
| 5 | Search time recorded (time spent looking) | Should |
| 6 | Final search location recorded | Should |
| 7 | Status changes to UTL | Must |
| 8 | Teacher notified of UTL outcome | Must |
| 9 | No payout triggered for UTL | Must |
| 10 | Works offline with sync when connected | Must |
| 11 | Option to add search area photos | Should |

## UI/UX Design

### UTL Confirmation Dialog

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                 ❌ Unable to Locate                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  INC-2026-0156                                             │
│  Search time: 35 minutes                                   │
│                                                             │
│  Reason: *                                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ● Dog not at reported location                     │   │
│  │  ○ Dog left area during response                    │   │
│  │  ○ Location information unclear/incorrect           │   │
│  │  ○ Area inaccessible                               │   │
│  │  ○ Other (specify in notes)                        │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Search Area Covered:                                      │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ☑ Reported location (within 50m)                  │   │
│  │  ☑ Surrounding streets                             │   │
│  │  ☐ Nearby buildings/compounds                      │   │
│  │  ☐ Extended area (500m+ radius)                    │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Additional Notes:                                         │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Searched the area for 35 minutes. Asked nearby    │   │
│  │ shopkeepers, no one has seen the dog recently.    │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ⚠️ Note: UTL status will not trigger reporter payout.     │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │     [Cancel]            [❌ Confirm UTL]            │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### UTL Success Screen

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                    ❌ Incident Closed                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│                         ╭─────╮                            │
│                        │  ✗  │                             │
│                        │     │                             │
│                         ╰─────╯                            │
│                                                             │
│        INC-2026-0156 marked as Unable to Locate           │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Status: UTL (Unable to Locate)                     │   │
│  │  Reason: Dog not at reported location               │   │
│  │  Search time: 35 minutes                            │   │
│  │  Closed at: 11:45 AM                               │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  The reporter will be notified of this outcome.           │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │           [🏠 Return to Incident List]              │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Search Timer Display

```
┌─────────────────────────────────────────────────────────────┐
│ ◀ Back         Active Response           ⏱️ 00:35:12       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  INC-2026-0156                     🟢 IN PROGRESS   │   │
│  │  Route de l'Aéroport, Sector 4                      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ⏱️ Search Time: 35 minutes                                │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  💡 Search Tips:                                    │   │
│  │  • Check behind nearby buildings                    │   │
│  │  • Ask local shopkeepers or residents               │   │
│  │  • Look in shaded areas during hot weather         │   │
│  │  • Check garbage collection points                  │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Can't find the dog?                                       │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                                                      │  │
│  │   ❌ Mark Unable to Locate                           │  │
│  │   Close incident after thorough search              │  │
│  │                                                      │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Technical Implementation

### UTL Service

```javascript
// services/UTLService.js
class UTLService {
  constructor(baseUrl, offlineQueue) {
    this.baseUrl = baseUrl;
    this.offlineQueue = offlineQueue;
  }

  /**
   * UTL reason codes
   */
  static UTL_REASONS = {
    NOT_AT_LOCATION: {
      code: 'NOT_AT_LOCATION',
      label: 'Dog not at reported location',
      description: 'Dog was not found at or near the reported coordinates'
    },
    LEFT_AREA: {
      code: 'LEFT_AREA',
      label: 'Dog left area during response',
      description: 'Dog was spotted but moved away before capture'
    },
    LOCATION_UNCLEAR: {
      code: 'LOCATION_UNCLEAR',
      label: 'Location information unclear/incorrect',
      description: 'Could not determine exact location from report details'
    },
    INACCESSIBLE: {
      code: 'INACCESSIBLE',
      label: 'Area inaccessible',
      description: 'Location is gated, private property, or otherwise inaccessible'
    },
    OTHER: {
      code: 'OTHER',
      label: 'Other',
      description: 'Other reason - specify in notes',
      requiresNotes: true
    }
  };

  /**
   * Search area coverage options
   */
  static SEARCH_AREAS = [
    { id: 'immediate', label: 'Reported location (within 50m)', default: true },
    { id: 'streets', label: 'Surrounding streets', default: true },
    { id: 'buildings', label: 'Nearby buildings/compounds', default: false },
    { id: 'extended', label: 'Extended area (500m+ radius)', default: false }
  ];

  /**
   * Mark incident as UTL
   */
  async markUTL(incidentId, utlData) {
    const {
      reasonCode,
      notes,
      searchAreasCovered,
      officerId,
      location
    } = utlData;

    // Validate reason
    const reason = UTLService.UTL_REASONS[reasonCode];
    if (!reason) {
      throw new Error(`Invalid UTL reason: ${reasonCode}`);
    }

    if (reason.requiresNotes && !notes) {
      throw new Error('Notes are required for this UTL reason');
    }

    // Calculate search time
    const searchTimeMinutes = this.calculateSearchTime(incidentId);

    const payload = {
      incidentId,
      status: 'UTL',
      utlReason: reasonCode,
      utlNotes: notes || null,
      searchAreasCovered,
      searchTimeMinutes,
      closedAt: new Date().toISOString(),
      closedBy: officerId,
      closedLocation: location ? {
        latitude: location.latitude,
        longitude: location.longitude,
        accuracy: location.accuracy
      } : null
    };

    if (!navigator.onLine) {
      return this.queueOfflineUTL(payload);
    }

    try {
      const response = await fetch(
        `${this.baseUrl}/sdcrs/incidents/v1/${incidentId}/utl`,
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
        throw new Error(`Failed to mark UTL: ${response.status}`);
      }

      const data = await response.json();

      // Clear response timer
      this.clearResponseTimer(incidentId);

      return {
        incident: data.incident,
        searchTime: searchTimeMinutes,
        teacherNotified: data.teacherNotified
      };

    } catch (error) {
      if (error.message.includes('network')) {
        return this.queueOfflineUTL(payload);
      }
      throw error;
    }
  }

  /**
   * Calculate search time from response start
   */
  calculateSearchTime(incidentId) {
    const startTime = localStorage.getItem(`response_start_${incidentId}`);
    if (!startTime) return null;

    const elapsed = Date.now() - parseInt(startTime, 10);
    return Math.round(elapsed / (1000 * 60)); // Minutes
  }

  /**
   * Queue UTL for offline sync
   */
  async queueOfflineUTL(payload) {
    const queuedAction = {
      id: generateUUID(),
      type: 'MARK_UTL',
      payload,
      queuedAt: new Date().toISOString(),
      status: 'PENDING'
    };

    await this.offlineQueue.add(queuedAction);

    // Optimistically update local state
    await this.updateLocalIncidentStatus(payload.incidentId, 'UTL');

    // Clear timer
    this.clearResponseTimer(payload.incidentId);

    return {
      incident: {
        id: payload.incidentId,
        status: 'UTL',
        utlReason: payload.utlReason,
        _offline: true,
        _pendingSync: true
      },
      searchTime: payload.searchTimeMinutes,
      teacherNotified: false // Will notify on sync
    };
  }

  clearResponseTimer(incidentId) {
    localStorage.removeItem(`response_start_${incidentId}`);
  }

  getAuthToken() {
    return localStorage.getItem('auth_token');
  }
}

export default UTLService;
```

### Mark UTL Component

```javascript
// components/MarkUTLDialog.jsx
import React, { useState } from 'react';
import UTLService from '../services/UTLService';
import { useAuth } from '../hooks/useAuth';
import { useGeolocation } from '../hooks/useGeolocation';

const UTL_REASONS = Object.values(UTLService.UTL_REASONS);
const SEARCH_AREAS = UTLService.SEARCH_AREAS;

function MarkUTLDialog({ incident, onSuccess, onCancel }) {
  const [reasonCode, setReasonCode] = useState('NOT_AT_LOCATION');
  const [notes, setNotes] = useState('');
  const [searchAreas, setSearchAreas] = useState(
    SEARCH_AREAS.filter(a => a.default).map(a => a.id)
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const { user } = useAuth();
  const { position } = useGeolocation();

  const utlService = new UTLService('/api');

  const toggleSearchArea = (areaId) => {
    setSearchAreas(prev =>
      prev.includes(areaId)
        ? prev.filter(id => id !== areaId)
        : [...prev, areaId]
    );
  };

  const handleSubmit = async () => {
    setLoading(true);
    setError(null);

    try {
      const result = await utlService.markUTL(incident.id, {
        reasonCode,
        notes: notes.trim() || null,
        searchAreasCovered: searchAreas,
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

  const searchTime = utlService.calculateSearchTime(incident.id);
  const selectedReason = UTL_REASONS.find(r => r.code === reasonCode);
  const requiresNotes = selectedReason?.requiresNotes;

  return (
    <div className="dialog-overlay">
      <div className="dialog-content mark-utl-dialog">
        <h2>❌ Unable to Locate</h2>

        <div className="incident-summary">
          <strong>{incident.incidentNumber}</strong>
          {searchTime && <p>Search time: {searchTime} minutes</p>}
        </div>

        {/* Reason Selection */}
        <div className="form-group">
          <label>Reason: *</label>
          <div className="radio-group">
            {UTL_REASONS.map(reason => (
              <label key={reason.code} className="radio-option">
                <input
                  type="radio"
                  name="utlReason"
                  value={reason.code}
                  checked={reasonCode === reason.code}
                  onChange={(e) => setReasonCode(e.target.value)}
                />
                <div className="option-content">
                  <span className="option-label">{reason.label}</span>
                  <span className="option-desc">{reason.description}</span>
                </div>
              </label>
            ))}
          </div>
        </div>

        {/* Search Areas */}
        <div className="form-group">
          <label>Search Area Covered:</label>
          <div className="checkbox-group">
            {SEARCH_AREAS.map(area => (
              <label key={area.id} className="checkbox-option">
                <input
                  type="checkbox"
                  checked={searchAreas.includes(area.id)}
                  onChange={() => toggleSearchArea(area.id)}
                />
                {area.label}
              </label>
            ))}
          </div>
        </div>

        {/* Notes */}
        <div className="form-group">
          <label>
            Additional Notes {requiresNotes ? '*' : '(Optional)'}:
          </label>
          <textarea
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="Describe the search efforts and any relevant observations..."
            rows={3}
            required={requiresNotes}
          />
        </div>

        <div className="warning-notice">
          <span className="icon">⚠️</span>
          <span>UTL status will not trigger reporter payout.</span>
        </div>

        {error && (
          <div className="error-message">⚠️ {error}</div>
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
            className="btn-danger"
            onClick={handleSubmit}
            disabled={loading || (requiresNotes && !notes.trim())}
          >
            {loading ? 'Submitting...' : '❌ Confirm UTL'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default MarkUTLDialog;
```

## API Endpoints

### POST /sdcrs/incidents/v1/{incidentId}/utl

Mark incident as Unable to Locate.

**Request:**
```json
{
  "status": "UTL",
  "utlReason": "NOT_AT_LOCATION",
  "utlNotes": "Searched area for 35 minutes, asked local shopkeepers",
  "searchAreasCovered": ["immediate", "streets"],
  "searchTimeMinutes": 35,
  "closedAt": "2026-01-15T11:45:00Z",
  "closedBy": "MC001",
  "closedLocation": {
    "latitude": 11.5879,
    "longitude": 43.1456
  }
}
```

**Response:**
```json
{
  "ResponseInfo": { "status": "success" },
  "incident": {
    "id": "INC-2026-0156",
    "status": "UTL",
    "utlReason": "NOT_AT_LOCATION",
    "closedAt": "2026-01-15T11:45:00Z"
  },
  "teacherNotified": true,
  "payoutTriggered": false
}
```

## Dependencies

| Dependency | Purpose | Required |
|------------|---------|----------|
| MC-FLD-01 | Start response context | Yes |
| MC-NOT-02 | Teacher UTL notification | Yes |
| idb | Offline queue | Yes |

## Related Stories

- [MC-FLD-01](./MC-FLD-01.md) - Update Status to In Progress
- [MC-UTL-02](./MC-UTL-02.md) - Add UTL Notes
- [MC-NOT-02](./MC-NOT-02.md) - Notify Teacher on UTL

---

*Last Updated: 2026-01-15*
