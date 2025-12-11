# S-REJ-02: Auto-Reject Outside Boundary

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## System Behavior

**The system** automatically rejects submissions with GPS coordinates outside the tenant boundary
**To ensure** only submissions from the operational service area are processed.

---

## Description

When GPS validation (S-VAL-02) determines coordinates are outside the configured tenant boundary, the system automatically rejects the submission. This prevents wasting resources on submissions that cannot be serviced by the local MC team.

---

## Acceptance Criteria

### Rejection Rules

- [ ] Auto-reject triggered when GPS is >50m outside boundary
- [ ] Rejection is immediate - no verifier queue entry
- [ ] Teacher notified via SMS and in-app notification
- [ ] Rejection shows distance to nearest boundary point
- [ ] Map visualization of service area boundary
- [ ] Submission logged but not stored for processing
- [ ] Teacher can submit from valid location

### Auto-Reject Scenarios

| GPS Location | Action | Shows |
|--------------|--------|-------|
| >50m outside boundary | Auto-reject | Distance to boundary |
| In water/invalid | Auto-reject | Invalid location error |
| Missing GPS | Auto-reject | GPS required error |
| 0-50m outside (buffer) | Accept with flag | Warning to verifier |

---

## Technical Implementation

### Auto-Rejection for Boundary

```javascript
async function autoRejectForBoundary(submission, validationResult) {
  const rejection = {
    applicationId: submission.applicationId,
    status: 'auto_rejected',
    rejectedAt: new Date().toISOString(),
    rejectionType: 'AUTOMATIC',
    rejection: {
      reasonCode: 'OUTSIDE_AREA',
      reasonLabel: 'Outside Service Area',
      description: 'This location is outside the program service area.',
      systemReason: validationResult.errors[0],
      distanceToBoundary: validationResult.distanceToBoundary,
      coordinates: {
        latitude: submission.latitude,
        longitude: submission.longitude
      }
    }
  };

  // Update submission status
  await db.submissions.updateOne(
    { applicationId: submission.applicationId },
    {
      $set: {
        status: 'auto_rejected',
        rejectedAt: rejection.rejectedAt,
        rejection: rejection.rejection,
        processedAt: new Date().toISOString()
      }
    }
  );

  // Send notifications
  await sendBoundaryRejectionNotifications(submission, rejection);

  // Log for audit
  await logAutoRejection(submission, rejection);

  return rejection;
}
```

### Notification with Map Link

```javascript
async function sendBoundaryRejectionNotifications(submission, rejection) {
  const teacher = await getTeacher(submission.teacherId);

  // SMS notification
  await smsService.send(teacher.phone, SMS_TEMPLATES.auto_rejected_boundary, {
    ID: submission.applicationId,
    DISTANCE: Math.round(rejection.rejection.distanceToBoundary)
  });

  // In-app notification with map data
  await createInAppNotification({
    userId: teacher.id,
    type: 'submission_auto_rejected',
    title: 'Location Outside Service Area',
    body: `Your submission was not accepted. The location is ${Math.round(rejection.rejection.distanceToBoundary)}m outside the service area.`,
    data: {
      applicationId: submission.applicationId,
      reasonCode: rejection.rejection.reasonCode,
      showMap: true,
      userLocation: rejection.rejection.coordinates,
      boundaryUrl: `/api/boundaries/${submission.tenantId}`
    },
    priority: 'high'
  });
}

const SMS_TEMPLATES = {
  auto_rejected_boundary: {
    en: 'SDCRS: Your report {ID} could not be accepted. The location is {DISTANCE}m outside the service area.',
    fr: 'SDCRS: Votre rapport {ID} n\'a pas pu être accepté. L\'emplacement est à {DISTANCE}m hors de la zone de service.'
  }
};
```

### Client Response with Map Data

```javascript
function buildBoundaryRejectResponse(rejection, tenantBoundary) {
  return {
    success: false,
    status: 'auto_rejected',
    applicationId: rejection.applicationId,
    error: {
      code: 'OUTSIDE_SERVICE_AREA',
      message: 'This location is outside the program service area.',
      userMessage: 'You can only submit reports from within the designated service area.',
      details: {
        distance: `${Math.round(rejection.rejection.distanceToBoundary)} meters outside`,
        yourLocation: rejection.rejection.coordinates
      }
    },
    mapData: {
      center: rejection.rejection.coordinates,
      userMarker: rejection.rejection.coordinates,
      boundary: tenantBoundary,
      zoom: 13
    },
    actions: [
      {
        label: 'View Service Area',
        action: 'SHOW_MAP'
      },
      {
        label: 'Back to Home',
        action: 'GO_HOME'
      }
    ]
  };
}
```

---

## User Interface Impact

### Boundary Rejection Screen (PWA)

```
┌─────────────────────────────────┐
│                                 │
│            ❌                   │
│   Location Outside Area         │
│                                 │
├─────────────────────────────────┤
│                                 │
│  ┌───────────────────────────┐  │
│  │      [Map View]           │  │
│  │                           │  │
│  │   📍 Your location        │  │
│  │                           │  │
│  │   ═══ Service boundary    │  │
│  │                           │  │
│  └───────────────────────────┘  │
│                                 │
│  Your location is 850 meters    │
│  outside the service area.      │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  The SDCRS program currently    │
│  operates only within:          │
│                                 │
│  📍 Djibouti City               │
│                                 │
│  Reports from other areas       │
│  cannot be processed at this    │
│  time.                          │
│                                 │
│  [  View Service Area Map  ]    │
│                                 │
│  [  Back to Home  ]             │
│                                 │
└─────────────────────────────────┘
```

### Service Area Map View

```
┌─────────────────────────────────┐
│  ← Service Area                 │
├─────────────────────────────────┤
│                                 │
│  ┌───────────────────────────┐  │
│  │                           │  │
│  │   [Full Screen Map]       │  │
│  │                           │  │
│  │   🟢 Service area         │  │
│  │   (highlighted polygon)    │  │
│  │                           │  │
│  │   🔴 Your location        │  │
│  │   (outside area)          │  │
│  │                           │  │
│  │                           │  │
│  │                           │  │
│  │                           │  │
│  └───────────────────────────┘  │
│                                 │
│  ℹ️ You must be within the      │
│     green area to submit        │
│     reports.                    │
│                                 │
└─────────────────────────────────┘
```

---

## Error Codes

| Code | Description | Recovery Action |
|------|-------------|-----------------|
| OUTSIDE_AREA | GPS outside tenant boundary | Move to service area |
| INVALID_COORDINATES | GPS coordinates invalid | Enable location services |
| GPS_REQUIRED | No GPS coordinates | Allow location permission |
| WATER_LOCATION | Coordinates in water | Check GPS accuracy |

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| S-VAL-02 | System Story | Boundary validation |
| egov-location | DIGIT | Tenant boundary data |
| Map SDK | Library | Display boundary map |
| SMS Gateway | External | Send rejection SMS |

---

## Related Stories

- [S-VAL-02](./S-VAL-02.md) - GPS boundary validation
- [S-REJ-01](./S-REJ-01.md) - Auto-reject old photos
- [T-SUB-03](../teacher/T-SUB-03.md) - GPS auto-extraction
