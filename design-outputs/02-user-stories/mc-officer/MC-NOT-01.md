# MC-NOT-01: Notify Teacher on Capture

[← Back to User Stories Master](../user-stories-master.md) | [← Back to MC Officer Stories](./index.md)

## User Story

**As the** System (triggered by MC Officer action),
**I want to** notify the teacher when their reported dog has been successfully captured/resolved,
**So that** the teacher knows their report was addressed and they will receive their payout.

## Description

When an MC Officer marks an incident as "Captured" or "Resolved", the system automatically notifies the original reporting teacher. This notification:
- Confirms the successful resolution
- Informs the teacher that their payout is being processed
- Provides closure on their report
- Maintains engagement with the reporting community

This is a **critical notification** as it directly relates to teacher compensation.

## Acceptance Criteria

### Functional Requirements
- [ ] System sends notification within 1 minute of capture confirmation
- [ ] Notification includes: incident ID, resolution type, capture timestamp
- [ ] Multi-channel delivery: push notification + in-app + SMS (if configured)
- [ ] Teacher can view notification in app notification center
- [ ] Notification links to payout status (when available)
- [ ] Notification persists until acknowledged

### Non-Functional Requirements
- [ ] Delivery rate > 99% within 5 minutes
- [ ] Notification queue with retry mechanism
- [ ] Failed notifications logged for manual follow-up
- [ ] Audit trail maintained for all notifications

## UI/UX Design

### Push Notification (Teacher's Device)
```
┌─────────────────────────────────────────┐
│ 🐕 Stray Dog Capture                    │
├─────────────────────────────────────────┤
│ Great news! The dog you reported has    │
│ been captured.                          │
│                                         │
│ Your payout of ₹50 is being processed.  │
│                                         │
│ Tap to view details                     │
│                               Just now  │
└─────────────────────────────────────────┘
```

### In-App Notification Center
```
┌─────────────────────────────────────────┐
│ ← Notifications                         │
├─────────────────────────────────────────┤
│ Today                                   │
│ ┌─────────────────────────────────────┐ │
│ │ ✓ Dog Captured Successfully    🔵  │ │
│ │                                     │ │
│ │ The stray dog you reported on       │ │
│ │ Dec 7 at 10:30 AM has been          │ │
│ │ successfully captured by our team.  │ │
│ │                                     │ │
│ │ 📍 Near Government School, Ward 5   │ │
│ │ 🕐 Captured at 2:45 PM              │ │
│ │ 💰 Payout: ₹50 (Processing)         │ │
│ │                                     │ │
│ │ [View Report Details]               │ │
│ │                                     │ │
│ │                          2:46 PM    │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ Yesterday                               │
│ ┌─────────────────────────────────────┐ │
│ │ 📋 Report Verified                  │ │
│ │ Your report INC-2026-00041 has      │ │
│ │ been verified and assigned...       │ │
│ │                          3:20 PM    │ │
│ └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

### Teacher Report Details (Post-Capture)
```
┌─────────────────────────────────────────┐
│ ← Report Details                        │
├─────────────────────────────────────────┤
│                                         │
│  Status: ✓ RESOLVED                     │
│  ════════════════════════════════════   │
│                                         │
│  ┌─────────────────────────────────────┐│
│  │         🎉 Dog Captured!           ││
│  │                                     ││
│  │  Thank you for your report.        ││
│  │  The stray dog has been safely     ││
│  │  captured by our MC team.          ││
│  └─────────────────────────────────────┘│
│                                         │
│  Timeline                               │
│  ─────────────────────────────────────  │
│  📝 Reported      Dec 7, 10:30 AM       │
│  ✓  Verified      Dec 7, 11:15 AM       │
│  🚗 Assigned      Dec 7, 11:20 AM       │
│  📍 In Progress   Dec 7, 2:30 PM        │
│  ✓  Captured      Dec 7, 2:45 PM        │
│                                         │
│  Payout Status                          │
│  ─────────────────────────────────────  │
│  ┌─────────────────────────────────────┐│
│  │ Amount: ₹50                        ││
│  │ Status: Processing                 ││
│  │ Expected: Within 24-48 hours       ││
│  │                                     ││
│  │ Payment will be credited to your   ││
│  │ registered bank account.           ││
│  └─────────────────────────────────────┘│
│                                         │
│  Resolution Details                     │
│  ─────────────────────────────────────  │
│  Type: Dog captured by MC team          │
│  Officer: MC-OFF-045                    │
│  Time: 15 minutes (response)            │
│                                         │
└─────────────────────────────────────────┘
```

## Technical Implementation

### Notification Event Listener
```javascript
// src/services/CaptureNotificationService.js
import NotificationService from './NotificationService';
import PayoutService from './PayoutService';

class CaptureNotificationService {
  static NOTIFICATION_TYPE = 'DOG_CAPTURED';

  static RESOLUTION_MESSAGES = {
    CAPTURED: {
      title: 'Dog Captured Successfully',
      body: 'Great news! The stray dog you reported has been captured by our team.',
      emoji: '🎉'
    },
    OWNER_CLAIMED: {
      title: 'Dog Claimed by Owner',
      body: 'The dog you reported has been claimed by its owner. Thank you for your report!',
      emoji: '🏠'
    },
    ALREADY_REMOVED: {
      title: 'Issue Already Resolved',
      body: 'The stray dog situation was already resolved when our team arrived. Thank you for reporting!',
      emoji: '✓'
    }
  };

  // Main handler called when MC marks capture
  static async onCaptureConfirmed(incident, resolution) {
    try {
      // Get teacher details
      const teacherId = incident.reportedBy;
      const teacher = await this.getTeacherDetails(teacherId);

      if (!teacher) {
        console.error('Teacher not found for notification:', teacherId);
        await this.logFailedNotification(incident.id, 'TEACHER_NOT_FOUND');
        return;
      }

      // Get payout amount
      const payoutAmount = PayoutService.PAYOUT_AMOUNT;

      // Build notification payload
      const notificationPayload = this.buildNotificationPayload(
        incident,
        resolution,
        teacher,
        payoutAmount
      );

      // Send via multiple channels
      const results = await this.sendMultiChannel(teacher, notificationPayload);

      // Log notification attempt
      await this.logNotificationAttempt(incident.id, teacher.id, results);

      // Store in-app notification
      await this.storeInAppNotification(teacher.id, notificationPayload);

      return results;
    } catch (error) {
      console.error('Failed to send capture notification:', error);
      await this.queueForRetry(incident.id, error);
      throw error;
    }
  }

  static buildNotificationPayload(incident, resolution, teacher, payoutAmount) {
    const messageTemplate = this.RESOLUTION_MESSAGES[resolution.type] ||
                           this.RESOLUTION_MESSAGES.CAPTURED;

    return {
      type: this.NOTIFICATION_TYPE,
      title: messageTemplate.title,
      body: messageTemplate.body,
      data: {
        incidentId: incident.id,
        resolutionType: resolution.type,
        capturedAt: resolution.timestamp,
        location: incident.locationDescription,
        payoutAmount: payoutAmount,
        payoutStatus: 'PROCESSING'
      },
      // For push notification
      push: {
        title: `${messageTemplate.emoji} ${messageTemplate.title}`,
        body: `${messageTemplate.body}\n\nYour payout of ₹${payoutAmount} is being processed.`,
        icon: '/icons/capture-success.png',
        badge: '/icons/badge.png',
        tag: `capture-${incident.id}`,
        requireInteraction: true,
        actions: [
          { action: 'view', title: 'View Details' }
        ]
      },
      // For SMS
      sms: {
        message: `[SDCRS] ${messageTemplate.title}! The dog you reported (${incident.id}) has been captured. Payout of Rs.${payoutAmount} is being processed.`
      }
    };
  }

  static async sendMultiChannel(teacher, payload) {
    const results = {
      push: { sent: false, error: null },
      inApp: { sent: false, error: null },
      sms: { sent: false, error: null }
    };

    // 1. Push Notification (if token exists)
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

    // 3. SMS (if enabled and phone exists)
    if (teacher.smsEnabled && teacher.phone) {
      try {
        await NotificationService.sendSMS(teacher.phone, payload.sms.message);
        results.sms.sent = true;
      } catch (error) {
        results.sms.error = error.message;
      }
    }

    return results;
  }

  static async getTeacherDetails(teacherId) {
    const response = await fetch(`/api/users/${teacherId}`);
    if (!response.ok) return null;
    return response.json();
  }

  static async storeInAppNotification(teacherId, payload) {
    const notification = {
      id: `notif-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      userId: teacherId,
      type: payload.type,
      title: payload.title,
      body: payload.body,
      data: payload.data,
      read: false,
      createdAt: new Date().toISOString()
    };

    const response = await fetch('/api/notifications', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(notification)
    });

    return response.json();
  }

  static async logNotificationAttempt(incidentId, teacherId, results) {
    const logEntry = {
      incidentId,
      teacherId,
      type: this.NOTIFICATION_TYPE,
      timestamp: new Date().toISOString(),
      results,
      success: results.push.sent || results.inApp.sent || results.sms.sent
    };

    await fetch('/api/notification-logs', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(logEntry)
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
    // Add to retry queue for background processing
    await fetch('/api/notification-retry-queue', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        incidentId,
        type: this.NOTIFICATION_TYPE,
        error: error.message,
        retryCount: 0,
        nextRetry: new Date(Date.now() + 60000).toISOString() // 1 min
      })
    });
  }
}

export default CaptureNotificationService;
```

### Push Notification Service Worker Handler
```javascript
// public/sw.js (Service Worker)

self.addEventListener('push', function(event) {
  const data = event.data ? event.data.json() : {};

  const options = {
    body: data.body || 'You have a new notification',
    icon: data.icon || '/icons/app-icon.png',
    badge: data.badge || '/icons/badge.png',
    tag: data.tag,
    requireInteraction: data.requireInteraction || false,
    data: data.data || {},
    actions: data.actions || []
  };

  event.waitUntil(
    self.registration.showNotification(data.title || 'SDCRS', options)
  );
});

self.addEventListener('notificationclick', function(event) {
  event.notification.close();

  const data = event.notification.data;
  let targetUrl = '/';

  // Handle specific notification types
  if (data.incidentId) {
    targetUrl = `/reports/${data.incidentId}`;
  }

  // Handle action clicks
  if (event.action === 'view') {
    targetUrl = `/reports/${data.incidentId}`;
  }

  event.waitUntil(
    clients.matchAll({ type: 'window' }).then(function(clientList) {
      // Focus existing window if available
      for (const client of clientList) {
        if (client.url.includes(targetUrl) && 'focus' in client) {
          return client.focus();
        }
      }
      // Open new window
      if (clients.openWindow) {
        return clients.openWindow(targetUrl);
      }
    })
  );
});
```

### Teacher Notification Center Component
```jsx
// src/components/teacher/NotificationCenter.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

function NotificationCenter() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchNotifications();
  }, []);

  const fetchNotifications = async () => {
    try {
      const response = await fetch('/api/notifications/me');
      const data = await response.json();
      setNotifications(data.notifications);
    } catch (error) {
      console.error('Failed to fetch notifications:', error);
    } finally {
      setLoading(false);
    }
  };

  const markAsRead = async (notificationId) => {
    try {
      await fetch(`/api/notifications/${notificationId}/read`, {
        method: 'PUT'
      });
      setNotifications(prev =>
        prev.map(n => n.id === notificationId ? { ...n, read: true } : n)
      );
    } catch (error) {
      console.error('Failed to mark as read:', error);
    }
  };

  const handleNotificationClick = (notification) => {
    markAsRead(notification.id);

    if (notification.data?.incidentId) {
      navigate(`/reports/${notification.data.incidentId}`);
    }
  };

  const groupByDate = (notifications) => {
    const groups = {};
    const today = new Date().toDateString();
    const yesterday = new Date(Date.now() - 86400000).toDateString();

    notifications.forEach(notification => {
      const date = new Date(notification.createdAt).toDateString();
      let label = date;

      if (date === today) label = 'Today';
      else if (date === yesterday) label = 'Yesterday';

      if (!groups[label]) groups[label] = [];
      groups[label].push(notification);
    });

    return groups;
  };

  const getNotificationIcon = (type) => {
    switch (type) {
      case 'DOG_CAPTURED': return '✓';
      case 'DOG_UTL': return '⚠';
      case 'REPORT_VERIFIED': return '📋';
      case 'PAYOUT_COMPLETED': return '💰';
      default: return '📬';
    }
  };

  const formatTime = (timestamp) => {
    return new Date(timestamp).toLocaleTimeString('en-IN', {
      hour: 'numeric',
      minute: '2-digit',
      hour12: true
    });
  };

  if (loading) {
    return <div className="loading-spinner">Loading notifications...</div>;
  }

  const groupedNotifications = groupByDate(notifications);

  return (
    <div className="notification-center">
      <header className="notification-header">
        <button className="back-btn" onClick={() => navigate(-1)}>←</button>
        <h1>Notifications</h1>
      </header>

      <div className="notification-list">
        {Object.keys(groupedNotifications).length === 0 ? (
          <div className="empty-state">
            <span className="empty-icon">📭</span>
            <p>No notifications yet</p>
          </div>
        ) : (
          Object.entries(groupedNotifications).map(([date, items]) => (
            <div key={date} className="notification-group">
              <h3 className="group-date">{date}</h3>
              {items.map(notification => (
                <div
                  key={notification.id}
                  className={`notification-item ${!notification.read ? 'unread' : ''}`}
                  onClick={() => handleNotificationClick(notification)}
                >
                  <div className="notification-icon">
                    {getNotificationIcon(notification.type)}
                  </div>
                  <div className="notification-content">
                    <div className="notification-title">
                      {notification.title}
                      {!notification.read && <span className="unread-dot">🔵</span>}
                    </div>
                    <p className="notification-body">{notification.body}</p>

                    {notification.data?.location && (
                      <p className="notification-meta">
                        📍 {notification.data.location}
                      </p>
                    )}
                    {notification.data?.capturedAt && (
                      <p className="notification-meta">
                        🕐 Captured at {formatTime(notification.data.capturedAt)}
                      </p>
                    )}
                    {notification.data?.payoutAmount && (
                      <p className="notification-meta payout">
                        💰 Payout: ₹{notification.data.payoutAmount} ({notification.data.payoutStatus})
                      </p>
                    )}

                    <button className="view-details-btn">
                      View Report Details
                    </button>
                  </div>
                  <div className="notification-time">
                    {formatTime(notification.createdAt)}
                  </div>
                </div>
              ))}
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default NotificationCenter;
```

### Integration with Capture Flow
```javascript
// src/services/CaptureService.js (addition)
import CaptureNotificationService from './CaptureNotificationService';

class CaptureService {
  // ... existing code ...

  static async markCaptured(incidentId, resolution) {
    const response = await fetch(`/api/incidents/${incidentId}/capture`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(resolution)
    });

    if (!response.ok) {
      throw new Error('Failed to mark as captured');
    }

    const result = await response.json();

    // Trigger notification to teacher (non-blocking)
    CaptureNotificationService.onCaptureConfirmed(result.incident, resolution)
      .catch(error => {
        console.error('Notification failed, but capture was successful:', error);
        // Notification failure should not affect capture success
      });

    return result;
  }
}
```

### Backend Notification API
```javascript
// Backend: routes/notifications.js
const express = require('express');
const router = express.Router();

// Get user's notifications
router.get('/me', async (req, res) => {
  try {
    const notifications = await Notification.find({
      userId: req.user.id
    })
    .sort({ createdAt: -1 })
    .limit(50);

    const unreadCount = await Notification.countDocuments({
      userId: req.user.id,
      read: false
    });

    res.json({ notifications, unreadCount });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Mark notification as read
router.put('/:id/read', async (req, res) => {
  try {
    await Notification.findByIdAndUpdate(req.params.id, { read: true });
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Create notification (internal use)
router.post('/', async (req, res) => {
  try {
    const notification = new Notification(req.body);
    await notification.save();
    res.json(notification);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
```

## PWA Considerations

### Push Notification Permission
- Request push permission after first successful report
- Explain value proposition before requesting
- Handle permission denial gracefully
- Fall back to SMS/in-app if push denied

### Offline Handling
- Queue notifications if offline during capture
- Sync notification queue when connection restored
- Show pending notification count in UI

### Background Sync
- Use Background Sync API for notification delivery
- Retry failed notifications automatically

## Dependencies

- **Firebase Cloud Messaging (FCM)** or similar push service
- **Twilio/MSG91**: SMS gateway for fallback notifications
- **Service Worker**: Push notification handling
- **MC-CAP-01**: Capture confirmation (trigger event)
- [S-PTS-01](../system/S-PTS-01.md): Payout trigger (linked status)

## Related Stories

- [MC-CAP-01](./MC-CAP-01.md) - Mark Captured/Resolved (trigger)
- [MC-NOT-02](./MC-NOT-02.md) - Notify Teacher on UTL
- [T-STAT-01](../teacher/T-STAT-01.md) - View Report Status
- [S-NOT-01](../system/S-NOT-01.md) - System Notifications

## Notes

- This is a critical notification - use multiple channels
- Teacher engagement depends heavily on timely notifications
- Include payout status to reinforce incentive
- Keep message tone positive and appreciative
