# MC-VIEW-05: Teacher Privacy Protection

> **Back to:** [User Stories Master](../../design-outputs/02-user-stories.md)

## User Story

**As a** System Administrator,
**I want to** ensure teacher identity is hidden from MC Officers,
**So that** reporter privacy is protected and potential retaliation is prevented.

## Description

Teacher privacy is a critical requirement of the SDCRS system. MC Officers should be able to effectively respond to incidents without knowing who reported them. This prevents potential conflicts between municipal workers and community members, and encourages teachers to report without fear of identification. The system implements privacy at multiple layers: API, database views, and UI.

## Acceptance Criteria

| # | Criterion | Priority |
|---|-----------|----------|
| 1 | MC Officer API responses exclude teacher PII | Must |
| 2 | Teacher name never displayed to MC Officers | Must |
| 3 | Teacher phone/email never displayed to MC Officers | Must |
| 4 | Photos metadata stripped of reporter identifiers | Must |
| 5 | Database views restrict PII access by role | Must |
| 6 | Audit logs record all data access attempts | Must |
| 7 | Admin can view reporter info if needed (separate permission) | Should |
| 8 | System prevents accidental PII exposure in error messages | Must |
| 9 | Export/report functions respect role-based PII rules | Should |
| 10 | Privacy policy displayed to teachers on report submission | Should |

## Privacy Architecture

### Data Flow with Privacy Filters

```
┌─────────────────────────────────────────────────────────────┐
│                    SDCRS Privacy Architecture               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────┐       ┌─────────────┐       ┌───────────┐ │
│  │   Teacher   │──────▶│  Incident   │──────▶│  Storage  │ │
│  │    (PWA)    │       │   Service   │       │    (DB)   │ │
│  └─────────────┘       └─────────────┘       └───────────┘ │
│                              │                     │        │
│                              │                     │        │
│                              ▼                     │        │
│                     ┌─────────────────┐           │        │
│                     │  Privacy Filter │◀──────────┘        │
│                     │     Service     │                    │
│                     └────────┬────────┘                    │
│                              │                             │
│           ┌──────────────────┼──────────────────┐         │
│           │                  │                  │         │
│           ▼                  ▼                  ▼         │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐   │
│  │  Verifier   │    │ MC Officer  │    │    Admin    │   │
│  │    (PWA)    │    │   (PWA)     │    │   (PWA)     │   │
│  │             │    │             │    │             │   │
│  │ ✓ Full data │    │ ✗ No PII   │    │ ✓ Full data │   │
│  │ ✓ Teacher   │    │ ✓ Location  │    │ (with audit)│   │
│  │   info      │    │ ✓ Photos    │    │             │   │
│  └─────────────┘    └─────────────┘    └─────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Role-Based Data Access Matrix

```
┌───────────────────┬───────────┬───────────┬──────────┬─────────┐
│ Data Field        │ Teacher   │ Verifier  │MC Officer│ Admin   │
├───────────────────┼───────────┼───────────┼──────────┼─────────┤
│ Incident ID       │ ✓ Own     │ ✓ All     │ ✓ Zone   │ ✓ All   │
│ Location          │ ✓ Own     │ ✓ All     │ ✓ Zone   │ ✓ All   │
│ Photos            │ ✓ Own     │ ✓ All     │ ✓ Zone   │ ✓ All   │
│ Dog Description   │ ✓ Own     │ ✓ All     │ ✓ Zone   │ ✓ All   │
│ Status            │ ✓ Own     │ ✓ All     │ ✓ Zone   │ ✓ All   │
│ Reporter Name     │ ✓ Own     │ ✓ All     │ ✗ Hidden │ ✓ Audit │
│ Reporter Phone    │ ✓ Own     │ ✓ All     │ ✗ Hidden │ ✓ Audit │
│ Reporter Email    │ ✓ Own     │ ✓ All     │ ✗ Hidden │ ✓ Audit │
│ Points Earned     │ ✓ Own     │ ✓ All     │ ✗ Hidden │ ✓ All   │
│ Payout Status     │ ✓ Own     │ ✗ Hidden  │ ✗ Hidden │ ✓ All   │
└───────────────────┴───────────┴───────────┴──────────┴─────────┘
```

## Technical Implementation

### Privacy Filter Service

```javascript
// services/PrivacyFilterService.js
class PrivacyFilterService {
  /**
   * PII fields that must be filtered for MC Officers
   */
  static PII_FIELDS = [
    'reporterName',
    'reporterPhone',
    'reporterEmail',
    'reporterId',
    'teacherName',
    'teacherPhone',
    'teacherEmail',
    'teacherId',
    'submittedBy',
    'createdBy'
  ];

  /**
   * Fields to redact from photo metadata
   */
  static PHOTO_METADATA_PII = [
    'uploadedBy',
    'uploadedByName',
    'deviceId',
    'deviceOwner'
  ];

  /**
   * Role-based permission configuration
   */
  static ROLE_PERMISSIONS = {
    TEACHER: {
      canViewOwnPII: true,
      canViewOthersPII: false,
      canViewAllIncidents: false
    },
    VERIFIER: {
      canViewOwnPII: true,
      canViewOthersPII: true, // Needed for verification
      canViewAllIncidents: true
    },
    MC_OFFICER: {
      canViewOwnPII: true,
      canViewOthersPII: false, // Critical: NO access to reporter PII
      canViewAllIncidents: false // Only jurisdiction
    },
    DISTRICT_ADMIN: {
      canViewOwnPII: true,
      canViewOthersPII: true,
      canViewAllIncidents: false // Only district
    },
    SUPER_ADMIN: {
      canViewOwnPII: true,
      canViewOthersPII: true,
      canViewAllIncidents: true
    }
  };

  /**
   * Filter incident data based on user role
   * @param {Object} incident - Full incident object
   * @param {string} userRole - User's role
   * @param {string} userId - User's ID (for own data check)
   * @returns {Object} Filtered incident
   */
  static filterIncident(incident, userRole, userId) {
    const permissions = this.ROLE_PERMISSIONS[userRole];

    if (!permissions) {
      throw new Error(`Unknown role: ${userRole}`);
    }

    // Create a deep copy to avoid mutation
    const filtered = JSON.parse(JSON.stringify(incident));

    // Check if user can view PII
    const isOwnIncident = incident.reporterId === userId;
    const canViewPII = permissions.canViewOthersPII ||
      (permissions.canViewOwnPII && isOwnIncident);

    if (!canViewPII) {
      // Remove all PII fields
      this.PII_FIELDS.forEach(field => {
        if (filtered.hasOwnProperty(field)) {
          delete filtered[field];
        }
      });

      // Filter photo metadata
      if (filtered.photos) {
        filtered.photos = filtered.photos.map(photo =>
          this.filterPhotoMetadata(photo)
        );
      }

      // Filter audit history to remove names
      if (filtered.auditHistory) {
        filtered.auditHistory = filtered.auditHistory.map(event =>
          this.filterAuditEvent(event, userRole)
        );
      }
    }

    return filtered;
  }

  /**
   * Filter photo metadata to remove PII
   */
  static filterPhotoMetadata(photo) {
    const filtered = { ...photo };

    this.PHOTO_METADATA_PII.forEach(field => {
      delete filtered[field];
    });

    // Keep only safe fields
    return {
      id: filtered.id,
      url: filtered.url,
      thumbnailUrl: filtered.thumbnailUrl,
      uploadedAt: filtered.uploadedAt,
      fileSize: filtered.fileSize
    };
  }

  /**
   * Filter audit event to hide actor names for certain roles
   */
  static filterAuditEvent(event, viewerRole) {
    if (viewerRole === 'MC_OFFICER') {
      return {
        action: event.action,
        timestamp: event.timestamp,
        description: event.description
        // actorName and actorId removed
      };
    }
    return event;
  }

  /**
   * Filter array of incidents
   */
  static filterIncidents(incidents, userRole, userId) {
    return incidents.map(incident =>
      this.filterIncident(incident, userRole, userId)
    );
  }

  /**
   * Validate that no PII leaked into response
   * Used as safety check before sending response
   */
  static validateNoPII(data, userRole) {
    if (this.ROLE_PERMISSIONS[userRole]?.canViewOthersPII) {
      return true; // PII allowed for this role
    }

    const dataStr = JSON.stringify(data);

    // Check for common PII patterns
    const piiPatterns = [
      /reporterName/i,
      /teacherName/i,
      /reporterPhone/i,
      /reporterEmail/i,
      /@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}/, // Email pattern
      /\+?[0-9]{10,}/ // Phone pattern
    ];

    for (const pattern of piiPatterns) {
      if (pattern.test(dataStr)) {
        console.error('PII LEAK DETECTED in response for role:', userRole);
        return false;
      }
    }

    return true;
  }
}

export default PrivacyFilterService;
```

### API Middleware for Privacy

```javascript
// middleware/privacyMiddleware.js
import PrivacyFilterService from '../services/PrivacyFilterService';
import AuditLogger from '../services/AuditLogger';

/**
 * Express middleware to filter PII from responses
 */
function privacyMiddleware(req, res, next) {
  const originalJson = res.json.bind(res);

  res.json = (data) => {
    const userRole = req.user?.role;
    const userId = req.user?.id;

    // Log the data access
    AuditLogger.logDataAccess({
      userId,
      userRole,
      endpoint: req.originalUrl,
      method: req.method,
      resourceType: detectResourceType(req.originalUrl),
      timestamp: new Date().toISOString()
    });

    // Filter data based on role
    let filteredData = data;

    if (data.incident) {
      filteredData = {
        ...data,
        incident: PrivacyFilterService.filterIncident(
          data.incident,
          userRole,
          userId
        )
      };
    }

    if (data.incidents) {
      filteredData = {
        ...data,
        incidents: PrivacyFilterService.filterIncidents(
          data.incidents,
          userRole,
          userId
        )
      };
    }

    // Safety validation
    if (!PrivacyFilterService.validateNoPII(filteredData, userRole)) {
      // Log security event and return sanitized error
      AuditLogger.logSecurityEvent({
        type: 'PII_LEAK_PREVENTED',
        userId,
        userRole,
        endpoint: req.originalUrl,
        timestamp: new Date().toISOString()
      });

      return originalJson({
        error: 'Data processing error',
        message: 'Unable to process request'
      });
    }

    return originalJson(filteredData);
  };

  next();
}

function detectResourceType(url) {
  if (url.includes('/incidents')) return 'INCIDENT';
  if (url.includes('/teachers')) return 'TEACHER';
  if (url.includes('/reports')) return 'REPORT';
  return 'UNKNOWN';
}

export default privacyMiddleware;
```

### Database View for MC Officers

```sql
-- PostgreSQL view that excludes PII for MC Officers
CREATE OR REPLACE VIEW mc_officer_incidents_view AS
SELECT
    i.id,
    i.incident_number,
    i.status,
    i.reported_date,
    i.verified_date,
    i.assigned_date,
    i.resolved_date,

    -- Location (allowed)
    i.latitude,
    i.longitude,
    i.address,
    i.landmark,
    i.locality,

    -- Dog details (allowed)
    i.dog_count,
    i.dog_description,
    i.behavior_flags,
    i.additional_notes,

    -- Jurisdiction (allowed)
    i.jurisdiction_id,
    j.name as jurisdiction_name,

    -- Photos (IDs only, fetch separately)
    i.photo_ids,

    -- MC Officer assignment
    i.assigned_mc_officer_id,
    i.resolution_notes,
    i.resolution_type

    -- EXCLUDED: reporter_id, reporter_name, reporter_phone, reporter_email
    -- EXCLUDED: points_awarded, payout_status, payout_amount

FROM incidents i
LEFT JOIN jurisdictions j ON i.jurisdiction_id = j.id
WHERE i.status IN ('NEW', 'ASSIGNED', 'IN_PROGRESS', 'CAPTURED', 'UTL');

-- Grant access only to MC Officer role
GRANT SELECT ON mc_officer_incidents_view TO mc_officer_role;

-- Revoke direct table access
REVOKE ALL ON incidents FROM mc_officer_role;
```

### Photo Upload with Metadata Stripping

```javascript
// services/PhotoUploadService.js
import ExifReader from 'exifreader';

class PhotoUploadService {
  /**
   * Process uploaded photo and strip identifying metadata
   */
  async processPhoto(file, incidentId, uploaderId) {
    // Read and strip EXIF data
    const cleanedBuffer = await this.stripExifData(file.buffer);

    // Generate safe filename (no identifiers)
    const safeFilename = this.generateSafeFilename(incidentId);

    // Store with minimal metadata
    const stored = await this.storePhoto(cleanedBuffer, safeFilename);

    // Create database record with privacy-safe metadata
    const photoRecord = {
      id: generateUUID(),
      incidentId,
      url: stored.url,
      thumbnailUrl: stored.thumbnailUrl,
      uploadedAt: new Date().toISOString(),
      fileSize: cleanedBuffer.length,
      // Internal only - not exposed in MC Officer API
      _internal: {
        uploaderId,
        originalFilename: file.originalname,
        deviceInfo: this.extractDeviceInfo(file)
      }
    };

    return photoRecord;
  }

  /**
   * Strip EXIF data that could identify the uploader
   */
  async stripExifData(buffer) {
    // Fields to remove
    const sensitiveFields = [
      'GPSLatitude', 'GPSLongitude', // Strip GPS from photo metadata
      'Artist', 'Copyright', 'ImageDescription',
      'UserComment', 'XPAuthor', 'XPComment',
      'OwnerName', 'SerialNumber', 'CameraOwnerName',
      'BodySerialNumber', 'LensSerialNumber'
    ];

    try {
      const tags = ExifReader.load(buffer);

      // Log what we're removing for audit
      sensitiveFields.forEach(field => {
        if (tags[field]) {
          console.log(`Stripping EXIF field: ${field}`);
        }
      });

      // Use image processing library to create clean copy
      const sharp = require('sharp');
      const cleanedBuffer = await sharp(buffer)
        .rotate() // Auto-rotate based on EXIF, then strip
        .withMetadata({
          // Keep only safe metadata
          orientation: undefined
        })
        .toBuffer();

      return cleanedBuffer;
    } catch (error) {
      console.error('Error stripping EXIF:', error);
      // Return original if stripping fails, but log for security review
      return buffer;
    }
  }

  /**
   * Generate filename that doesn't contain identifiers
   */
  generateSafeFilename(incidentId) {
    const timestamp = Date.now();
    const random = Math.random().toString(36).substring(7);
    return `${incidentId}_${timestamp}_${random}.jpg`;
  }
}

export default PhotoUploadService;
```

### Privacy-Safe Error Handling

```javascript
// middleware/errorHandler.js

/**
 * Error handler that prevents PII leakage in error messages
 */
function privacySafeErrorHandler(err, req, res, next) {
  // Log full error for debugging
  console.error('Error:', {
    message: err.message,
    stack: err.stack,
    userId: req.user?.id,
    path: req.path
  });

  // Sanitize error message to remove potential PII
  const sanitizedMessage = sanitizeErrorMessage(err.message);

  // Generic error response
  res.status(err.status || 500).json({
    error: err.name || 'Error',
    message: sanitizedMessage,
    code: err.code || 'UNKNOWN_ERROR'
  });
}

/**
 * Remove potential PII from error messages
 */
function sanitizeErrorMessage(message) {
  if (!message) return 'An error occurred';

  // Patterns that might contain PII
  const piiPatterns = [
    { pattern: /user\s+['"]?[\w@.]+['"]?/gi, replacement: 'user [REDACTED]' },
    { pattern: /email\s+['"]?[\w@.]+['"]?/gi, replacement: 'email [REDACTED]' },
    { pattern: /phone\s+['"]?[\d+\-\s]+['"]?/gi, replacement: 'phone [REDACTED]' },
    { pattern: /name\s+['"]?[\w\s]+['"]?/gi, replacement: 'name [REDACTED]' },
    { pattern: /reporter\s+[\w\s@.]+/gi, replacement: 'reporter [REDACTED]' }
  ];

  let sanitized = message;
  piiPatterns.forEach(({ pattern, replacement }) => {
    sanitized = sanitized.replace(pattern, replacement);
  });

  return sanitized;
}

export default privacySafeErrorHandler;
```

### Admin PII Access with Audit

```javascript
// services/AdminPIIAccessService.js
class AdminPIIAccessService {
  constructor(auditLogger) {
    this.auditLogger = auditLogger;
  }

  /**
   * Get reporter PII for admin (with audit logging)
   * Requires explicit reason for access
   */
  async getReporterPII(incidentId, adminId, accessReason) {
    // Validate admin has PII access permission
    const hasPermission = await this.validatePIIPermission(adminId);
    if (!hasPermission) {
      throw new Error('Unauthorized: PII access permission required');
    }

    // Validate access reason
    if (!accessReason || accessReason.length < 10) {
      throw new Error('Access reason must be provided');
    }

    // Log the access
    await this.auditLogger.log({
      action: 'PII_ACCESS',
      actorId: adminId,
      resourceType: 'INCIDENT_REPORTER',
      resourceId: incidentId,
      reason: accessReason,
      timestamp: new Date().toISOString(),
      ipAddress: this.getClientIP(),
      userAgent: this.getUserAgent()
    });

    // Fetch PII data
    const incident = await this.fetchIncidentWithPII(incidentId);

    return {
      incidentId,
      reporter: {
        id: incident.reporterId,
        name: incident.reporterName,
        phone: incident.reporterPhone,
        email: incident.reporterEmail
      },
      accessedBy: adminId,
      accessedAt: new Date().toISOString(),
      accessReason
    };
  }

  /**
   * Validate admin has explicit PII access permission
   */
  async validatePIIPermission(adminId) {
    const permissions = await this.fetchUserPermissions(adminId);
    return permissions.includes('VIEW_REPORTER_PII');
  }
}

export default AdminPIIAccessService;
```

## UI/UX Design

### Privacy Notice for Teachers

```
┌─────────────────────────────────────────────────────────────┐
│                    Privacy Information                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  🔒 Your Privacy is Protected                              │
│                                                             │
│  When you submit a stray dog report:                       │
│                                                             │
│  ✓ Your name and contact information will NOT be          │
│    shared with Municipal Corporation officers              │
│                                                             │
│  ✓ Only the location and dog details are visible          │
│    to field officers                                       │
│                                                             │
│  ✓ Verification staff can see your info only to           │
│    validate reports                                        │
│                                                             │
│  ✓ Your identity is protected to prevent any              │
│    potential retaliation                                   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              [ I Understand, Continue ]             │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  [View Full Privacy Policy]                                │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Admin PII Access Dialog

```
┌─────────────────────────────────────────────────────────────┐
│              ⚠️ Access Reporter Information                 │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  You are requesting access to protected personal           │
│  information for incident INC-2026-0156.                   │
│                                                             │
│  This access will be logged and audited.                   │
│                                                             │
│  Reason for Access: *                                      │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Investigating complaint about incident response...  │   │
│  │                                                     │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│  (Minimum 10 characters required)                          │
│                                                             │
│  ☐ I confirm this access is for legitimate business       │
│    purposes and will be handled in accordance with         │
│    data protection policies.                               │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │    [Cancel]              [Access Information]       │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Dependencies

| Dependency | Purpose | Required |
|------------|---------|----------|
| MC-VIEW-04 | Incident details view | Yes |
| V-REV-01 | Verifier needs full access | Yes |
| exifreader | Strip photo EXIF data | Yes |
| sharp | Image processing | Yes |

## Related Stories

- [MC-VIEW-04](./MC-VIEW-04.md) - Incident Details View
- [V-NOT-02](../verifier/V-NOT-02.md) - Audit Logging
- [T-SUB-01](../teacher/T-SUB-01.md) - Teacher Report Submission

---

*Last Updated: 2026-01-15*
