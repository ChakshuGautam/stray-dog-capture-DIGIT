# MC-NOT-02: Notify Teacher on UTL

[← Back to User Stories Master](../user-stories-master.md) | [← Back to MC Officer Stories](./index.md)

## User Story

**As the** System (triggered by MC Officer action),
**I want to** notify the teacher when their reported dog could not be located,
**So that** the teacher understands the outcome and can provide additional information if needed.

## Description

When an MC Officer marks an incident as "Unable to Locate" (UTL), the system notifies the original reporting teacher. This notification is important for:
- Transparency about report outcomes
- Encouraging teachers to provide better location data in future
- Giving teachers opportunity to provide updated information
- Maintaining trust in the system even when capture fails

**Key difference from capture notification**: UTL notifications do NOT mention payout (since no payout is triggered for UTL outcomes).

## Acceptance Criteria

### Functional Requirements
- [ ] System sends notification within 1 minute of UTL confirmation
- [ ] Notification includes: incident ID, UTL reason, MC officer's notes (summary)
- [ ] Notification provides option to "Update Location" if dog is re-sighted
- [ ] Multi-channel delivery: push notification + in-app
- [ ] SMS only for first-time UTL (to ensure awareness)
- [ ] Tone is informative, not blaming

### Non-Functional Requirements
- [ ] Delivery within 5 minutes for all channels
- [ ] UTL notifications have lower priority than capture notifications
- [ ] Notification does not mention payout/payment

## UI/UX Design

### Push Notification (Teacher's Device)
```
┌─────────────────────────────────────────┐
│ 🐕 Stray Dog Capture                    │
├─────────────────────────────────────────┤
│ Update on your report: Our team         │
│ couldn't locate the dog at the          │
│ reported location.                      │
│                                         │
│ Tap to view details or update location  │
│                               Just now  │
└─────────────────────────────────────────┘
```

### In-App Notification
```
┌─────────────────────────────────────────┐
│ ← Notifications                         │
├─────────────────────────────────────────┤
│ Today                                   │
│ ┌─────────────────────────────────────┐ │
│ │ ⚠ Unable to Locate Dog        🔵  │ │
│ │                                     │ │
│ │ Our team visited the location you   │ │
│ │ reported but couldn't find the      │ │
│ │ dog. This can happen if the dog     │ │
│ │ has moved on.                       │ │
│ │                                     │ │
│ │ 📍 Near Government School, Ward 5   │ │
│ │ 🕐 Visited at 2:45 PM               │ │
│ │                                     │ │
│ │ Reason: Dog not at reported         │ │
│ │ location                            │ │
│ │                                     │ │
│ │ ┌───────────────────────────────┐   │ │
│ │ │   🔄 Update Dog Location     │   │ │
│ │ └───────────────────────────────┘   │ │
│ │                                     │ │
│ │ [View Full Details]                 │ │
│ │                          3:46 PM    │ │
│ └─────────────────────────────────────┘ │
│                                         │
└─────────────────────────────────────────┘
```

### Teacher Report Details (Post-UTL)
```
┌─────────────────────────────────────────┐
│ ← Report Details                        │
├─────────────────────────────────────────┤
│                                         │
│  Status: ⚠ UNABLE TO LOCATE            │
│  ════════════════════════════════════   │
│                                         │
│  ┌─────────────────────────────────────┐│
│  │  ℹ️ Dog Could Not Be Located       ││
│  │                                     ││
│  │  Our team searched the reported    ││
│  │  location but couldn't find the    ││
│  │  dog. Stray dogs often move around ││
│  │  so this can happen.               ││
│  │                                     ││
│  │  If you see the dog again, you     ││
│  │  can update the location below.    ││
│  └─────────────────────────────────────┘│
│                                         │
│  Timeline                               │
│  ─────────────────────────────────────  │
│  📝 Reported      Dec 7, 10:30 AM       │
│  ✓  Verified      Dec 7, 11:15 AM       │
│  🚗 Assigned      Dec 7, 11:20 AM       │
│  📍 In Progress   Dec 7, 2:30 PM        │
│  ⚠  UTL           Dec 7, 2:45 PM        │
│                                         │
│  Team's Notes                           │
│  ─────────────────────────────────────  │
│  "Searched the area for 20 minutes.    │
│   Asked local residents - no one had   │
│   seen the dog recently. The dog may   │
│   have moved on from the location."    │
│                                         │
│  ─────────────────────────────────────  │
│                                         │
│  ┌─────────────────────────────────────┐│
│  │       🔄 Update Dog Location       ││
│  │                                     ││
│  │  Saw the dog again? Update the     ││
│  │  location to help our team find it.││
│  └─────────────────────────────────────┘│
│                                         │
│  Tips for Accurate Reporting            │
│  ─────────────────────────────────────  │
│  • Include nearby landmarks             │
│  • Note if dog seems stationary         │
│  • Report as soon as possible           │
│  • Take a photo if safe to do so        │
│                                         │
└─────────────────────────────────────────┘
```

### Update Location Flow (From UTL Notification)
```
┌─────────────────────────────────────────┐
│ ← Update Dog Location                   │
├─────────────────────────────────────────┤
│                                         │
│  Original Report: INC-2026-00045        │
│  ─────────────────────────────────────  │
│                                         │
│  Have you seen the dog again?           │
│  Update the location to reactivate      │
│  this report.                           │
│                                         │
│  ┌─────────────────────────────────────┐│
│  │                                     ││
│  │            [Map View]               ││
│  │                                     ││
│  │     📍 Original Location (X)        ││
│  │                                     ││
│  │     Tap to mark new location        ││
│  │                                     ││
│  └─────────────────────────────────────┘│
│                                         │
│  ┌─────────────────────────────────────┐│
│  │ Additional Notes (Optional)        ││
│  │                                     ││
│  │ Saw the dog near the temple...     ││
│  │                                     ││
│  └─────────────────────────────────────┘│
│                                         │
│  ┌─────────────────────────────────────┐│
│  │        Submit Updated Location      │
│  └─────────────────────────────────────┘│
│                                         │
│  ⓘ This will reactivate your report    │
│    for re-assignment.                   │
│                                         │
└─────────────────────────────────────────┘
```

## Technical Implementation

### UTL Notification Service
```javascript
// src/services/UTLNotificationService.js
import NotificationService from './NotificationService';

class UTLNotificationService {
  static NOTIFICATION_TYPE = 'DOG_UTL';

  static UTL_REASON_MESSAGES = {
    NOT_AT_LOCATION: {
      title: 'Dog Not Found at Location',
      body: 'Our team searched the reported location but the dog was not there. Stray dogs often move, so this can happen.'
    },
    LEFT_AREA: {
      title: 'Dog Left the Area',
      body: 'Our team arrived but the dog had already left the area. If you see it again, please update the location.'
    },
    LOCATION_UNCLEAR: {
      title: 'Location Needs Clarification',
      body: 'Our team had difficulty finding the exact location. Please provide more specific landmarks if reporting again.'
    },
    INACCESSIBLE: {
      title: 'Location Was Inaccessible',
      body: 'Our team couldn\'t access the reported location. If you see the dog in an accessible area, please update.'
    },
    OTHER: {
      title: 'Unable to Locate Dog',
      body: 'Our team couldn\'t locate the dog at this time. If you see it again, please update the location.'
    }
  };

  // Main handler called when MC marks UTL
  static async onUTLConfirmed(incident, utlDetails) {
    try {
      const teacherId = incident.reportedBy;
      const teacher = await this.getTeacherDetails(teacherId);

      if (!teacher) {
        console.error('Teacher not found for UTL notification:', teacherId);
        await this.logFailedNotification(incident.id, 'TEACHER_NOT_FOUND');
        return;
      }

      const notificationPayload = this.buildNotificationPayload(
        incident,
        utlDetails,
        teacher
      );

      // Send notifications (lower priority than capture)
      const results = await this.sendMultiChannel(teacher, notificationPayload, incident);

      await this.logNotificationAttempt(incident.id, teacher.id, results);
      await this.storeInAppNotification(teacher.id, notificationPayload, incident.id);

      return results;
    } catch (error) {
      console.error('Failed to send UTL notification:', error);
      await this.queueForRetry(incident.id, error);
      throw error;
    }
  }

  static buildNotificationPayload(incident, utlDetails, teacher) {
    const reasonMessage = this.UTL_REASON_MESSAGES[utlDetails.reason] ||
                         this.UTL_REASON_MESSAGES.OTHER;

    // Summarize notes (max 100 chars for notification)
    const notesSummary = utlDetails.notes
      ? (utlDetails.notes.length > 100
          ? utlDetails.notes.substring(0, 97) + '...'
          : utlDetails.notes)
      : null;

    return {
      type: this.NOTIFICATION_TYPE,
      title: reasonMessage.title,
      body: reasonMessage.body,
      data: {
        incidentId: incident.id,
        utlReason: utlDetails.reason,
        utlTimestamp: utlDetails.timestamp,
        location: incident.locationDescription,
        notesSummary,
        canUpdateLocation: true
      },
      // Push notification
      push: {
        title: `⚠ ${reasonMessage.title}`,
        body: reasonMessage.body,
        icon: '/icons/utl-notification.png',
        badge: '/icons/badge.png',
        tag: `utl-${incident.id}`,
        requireInteraction: false, // Less urgent than capture
        actions: [
          { action: 'update_location', title: 'Update Location' },
          { action: 'view', title: 'View Details' }
        ]
      },
      // SMS (only for first UTL)
      sms: {
        message: `[SDCRS] Your report (${incident.id}): Our team couldn't find the dog at the location. If you see it again, please update in the app.`
      }
    };
  }

  static async sendMultiChannel(teacher, payload, incident) {
    const results = {
      push: { sent: false, error: null },
      inApp: { sent: false, error: null },
      sms: { sent: false, error: null }
    };

    // 1. Push Notification
    if (teacher.pushToken) {
      try {
        await NotificationService.sendPush(teacher.pushToken, payload.push);
        results.push.sent = true;
      } catch (error) {
        results.push.error = error.message;
      }
    }

    // 2. In-App Notification (always)
    try {
      await NotificationService.createInApp(teacher.id, {
        type: payload.type,
        title: payload.title,
        body: payload.body,
        data: payload.data,
        read: false,
        createdAt: new Date().toISOString()
      });
      results.inApp.sent = true;
    } catch (error) {
      results.inApp.error = error.message;
    }

    // 3. SMS only for first-time UTL (to ensure awareness)
    const isFirstUTL = await this.isFirstUTLForTeacher(teacher.id);
    if (isFirstUTL && teacher.smsEnabled && teacher.phone) {
      try {
        await NotificationService.sendSMS(teacher.phone, payload.sms.message);
        results.sms.sent = true;
      } catch (error) {
        results.sms.error = error.message;
      }
    }

    return results;
  }

  static async isFirstUTLForTeacher(teacherId) {
    // Check if teacher has received UTL notification before
    const response = await fetch(
      `/api/notification-logs?userId=${teacherId}&type=${this.NOTIFICATION_TYPE}&limit=1`
    );
    const data = await response.json();
    return data.logs.length === 0;
  }

  static async getTeacherDetails(teacherId) {
    const response = await fetch(`/api/users/${teacherId}`);
    if (!response.ok) return null;
    return response.json();
  }

  static async storeInAppNotification(teacherId, payload, incidentId) {
    const notification = {
      id: `notif-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      userId: teacherId,
      type: payload.type,
      title: payload.title,
      body: payload.body,
      data: {
        ...payload.data,
        actions: [
          {
            id: 'update_location',
            label: 'Update Dog Location',
            route: `/reports/${incidentId}/update-location`
          }
        ]
      },
      read: false,
      createdAt: new Date().toISOString()
    };

    await fetch('/api/notifications', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(notification)
    });
  }

  static async logNotificationAttempt(incidentId, teacherId, results) {
    await fetch('/api/notification-logs', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        incidentId,
        teacherId,
        type: this.NOTIFICATION_TYPE,
        timestamp: new Date().toISOString(),
        results,
        success: results.push.sent || results.inApp.sent
      })
    });
  }

  static async logFailedNotification(incidentId, reason) {
    await fetch('/api/notification-logs', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        incidentId,
        type: this.NOTIFICATION_TYPE,
        timestamp: new Date().toISOString(),
        success: false,
        failureReason: reason
      })
    });
  }

  static async queueForRetry(incidentId, error) {
    await fetch('/api/notification-retry-queue', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        incidentId,
        type: this.NOTIFICATION_TYPE,
        error: error.message,
        retryCount: 0,
        nextRetry: new Date(Date.now() + 120000).toISOString() // 2 min (lower priority)
      })
    });
  }
}

export default UTLNotificationService;
```

### Update Location Component
```jsx
// src/components/teacher/UpdateLocationForm.jsx
import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import maplibregl from 'maplibre-gl';

function UpdateLocationForm() {
  const { incidentId } = useParams();
  const navigate = useNavigate();

  const [originalIncident, setOriginalIncident] = useState(null);
  const [newLocation, setNewLocation] = useState(null);
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const mapContainer = useRef(null);
  const mapRef = useRef(null);
  const markerRef = useRef(null);

  useEffect(() => {
    fetchIncident();
  }, [incidentId]);

  useEffect(() => {
    if (originalIncident && mapContainer.current) {
      initializeMap();
    }
  }, [originalIncident]);

  const fetchIncident = async () => {
    try {
      const response = await fetch(`/api/incidents/${incidentId}`);
      if (!response.ok) throw new Error('Incident not found');
      const data = await response.json();
      setOriginalIncident(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const initializeMap = () => {
    const map = new maplibregl.Map({
      container: mapContainer.current,
      style: 'https://tiles.openfreemap.org/styles/liberty',
      center: [originalIncident.longitude, originalIncident.latitude],
      zoom: 15
    });

    // Original location marker (gray X)
    new maplibregl.Marker({ color: '#9e9e9e' })
      .setLngLat([originalIncident.longitude, originalIncident.latitude])
      .setPopup(new maplibregl.Popup().setHTML('<b>Original Location</b>'))
      .addTo(map);

    // Click handler for new location
    map.on('click', (e) => {
      const { lng, lat } = e.lngLat;

      // Remove existing marker
      if (markerRef.current) {
        markerRef.current.remove();
      }

      // Add new marker
      markerRef.current = new maplibregl.Marker({ color: '#4caf50' })
        .setLngLat([lng, lat])
        .setPopup(new maplibregl.Popup().setHTML('<b>New Location</b>'))
        .addTo(map);

      setNewLocation({ latitude: lat, longitude: lng });
    });

    // Geolocation control
    map.addControl(
      new maplibregl.GeolocateControl({
        positionOptions: { enableHighAccuracy: true },
        trackUserLocation: false
      })
    );

    mapRef.current = map;
  };

  const handleSubmit = async () => {
    if (!newLocation) {
      setError('Please tap on the map to mark the new location');
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      const response = await fetch(`/api/incidents/${incidentId}/update-location`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          latitude: newLocation.latitude,
          longitude: newLocation.longitude,
          notes: notes.trim() || null,
          updatedAt: new Date().toISOString()
        })
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to update location');
      }

      // Success - navigate to report details
      navigate(`/reports/${incidentId}`, {
        state: { message: 'Location updated! Your report has been reactivated.' }
      });
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className="loading-spinner">Loading...</div>;
  }

  if (error && !originalIncident) {
    return (
      <div className="error-container">
        <p className="error-message">{error}</p>
        <button onClick={() => navigate(-1)}>Go Back</button>
      </div>
    );
  }

  return (
    <div className="update-location-form">
      <header className="form-header">
        <button className="back-btn" onClick={() => navigate(-1)}>←</button>
        <h1>Update Dog Location</h1>
      </header>

      <div className="form-content">
        <div className="incident-reference">
          <span className="label">Original Report:</span>
          <span className="value">{incidentId}</span>
        </div>

        <p className="instruction">
          Have you seen the dog again? Tap on the map to mark the new location
          to reactivate this report.
        </p>

        <div className="map-container" ref={mapContainer}>
          {/* MapLibre GL map renders here */}
        </div>

        <div className="map-legend">
          <div className="legend-item">
            <span className="marker original">●</span>
            <span>Original Location</span>
          </div>
          {newLocation && (
            <div className="legend-item">
              <span className="marker new">●</span>
              <span>New Location</span>
            </div>
          )}
        </div>

        <div className="notes-section">
          <label htmlFor="update-notes">Additional Notes (Optional)</label>
          <textarea
            id="update-notes"
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="E.g., Saw the dog near the temple about 10 minutes ago..."
            rows={3}
            maxLength={300}
          />
          <span className="char-count">{notes.length}/300</span>
        </div>

        {error && (
          <div className="error-banner">
            <p>{error}</p>
          </div>
        )}

        <button
          className="submit-btn"
          onClick={handleSubmit}
          disabled={!newLocation || submitting}
        >
          {submitting ? 'Submitting...' : 'Submit Updated Location'}
        </button>

        <p className="info-note">
          ℹ️ This will reactivate your report for re-assignment to an MC team.
        </p>
      </div>
    </div>
  );
}

export default UpdateLocationForm;
```

### Update Location API (Backend)
```javascript
// Backend: routes/incidents.js (addition)

// Update location after UTL
router.post('/:id/update-location', async (req, res) => {
  try {
    const { id } = req.params;
    const { latitude, longitude, notes, updatedAt } = req.body;

    // Verify incident exists and is in UTL status
    const incident = await Incident.findById(id);
    if (!incident) {
      return res.status(404).json({ error: 'Incident not found' });
    }

    if (incident.status !== 'UTL') {
      return res.status(400).json({
        error: 'Can only update location for UTL incidents'
      });
    }

    // Verify user is the original reporter
    if (incident.reportedBy !== req.user.id) {
      return res.status(403).json({
        error: 'Only the original reporter can update location'
      });
    }

    // Update incident
    incident.latitude = latitude;
    incident.longitude = longitude;
    incident.status = 'VERIFIED'; // Reset to verified for re-assignment
    incident.locationHistory.push({
      latitude: incident.latitude,
      longitude: incident.longitude,
      source: 'TEACHER_UPDATE',
      notes,
      timestamp: updatedAt
    });
    incident.utlCount = (incident.utlCount || 0) + 1;

    await incident.save();

    // Add to workflow history
    await WorkflowLog.create({
      incidentId: id,
      action: 'LOCATION_UPDATED',
      performedBy: req.user.id,
      previousStatus: 'UTL',
      newStatus: 'VERIFIED',
      notes: `Location updated by teacher after UTL. UTL count: ${incident.utlCount}`,
      timestamp: updatedAt
    });

    // Trigger re-routing to MC
    await RoutingService.routeIncident(incident);

    res.json({
      success: true,
      incident,
      message: 'Location updated. Report reactivated for assignment.'
    });
  } catch (error) {
    console.error('Update location error:', error);
    res.status(500).json({ error: error.message });
  }
});
```

### Integration with UTL Flow
```javascript
// src/services/UTLService.js (addition)
import UTLNotificationService from './UTLNotificationService';

class UTLService {
  // ... existing code ...

  static async markUTL(incidentId, utlDetails) {
    const response = await fetch(`/api/incidents/${incidentId}/utl`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(utlDetails)
    });

    if (!response.ok) {
      throw new Error('Failed to mark as UTL');
    }

    const result = await response.json();

    // Trigger notification to teacher (non-blocking)
    UTLNotificationService.onUTLConfirmed(result.incident, utlDetails)
      .catch(error => {
        console.error('UTL notification failed:', error);
      });

    return result;
  }
}
```

## PWA Considerations

### Deep Linking
- UTL notification should deep-link to report details
- "Update Location" action opens update form directly
- Handle deep links even when app is closed

### Offline Handling
- Update location form should work offline
- Queue location update for sync
- Show optimistic UI with pending indicator

### Notification Actions
- Support rich notification actions (Update Location, View Details)
- Handle action clicks in service worker

## Dependencies

- **MapLibre GL**: Map for location update
- **MC-UTL-01**: UTL confirmation (trigger event)
- **S-RTE-01**: Re-routing after location update

## Related Stories

- [MC-UTL-01](./MC-UTL-01.md) - Mark Unable to Locate (trigger)
- [MC-NOT-01](./MC-NOT-01.md) - Notify Teacher on Capture
- [T-STAT-01](../teacher/T-STAT-01.md) - View Report Status
- [T-SUB-01](../teacher/T-SUB-01.md) - Submit Report (similar location selection)

## Notes

- UTL is not a failure - it's a valid outcome that should be communicated respectfully
- Do NOT mention payout in UTL notifications (no payout for UTL)
- Provide actionable next steps (update location)
- Track UTL count per incident to identify problematic reports
- Educational tips help improve future report quality
