# S-VAL-01: Validate Photo Timestamp

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## System Behavior

**The system** validates that the dog photo was taken within the last 24 hours
**To ensure** submissions represent current sightings, not old or recycled photos.

---

## Description

When a submission is received, the system extracts the timestamp from the photo's EXIF metadata and validates it against the current server time. Photos older than 24 hours are automatically flagged or rejected to ensure teachers are reporting current dog sightings.

---

## Acceptance Criteria

### Validation Rules

- [ ] Extract timestamp from photo EXIF data (DateTimeOriginal)
- [ ] Compare against server timestamp (not client time)
- [ ] Allow maximum 24-hour window from capture to submission
- [ ] Add 1-hour grace period for timezone differences
- [ ] Photos without EXIF timestamp use device capture time
- [ ] Flag submissions with suspicious timestamp patterns
- [ ] Auto-reject photos older than 25 hours (24h + 1h grace)
- [ ] Log all timestamp validations for audit

### Validation Outcomes

| Scenario | Photo Age | Action |
|----------|-----------|--------|
| Fresh | 0-12 hours | Accept |
| Valid | 12-24 hours | Accept with note |
| Grace | 24-25 hours | Accept with warning |
| Expired | >25 hours | Auto-reject |
| No EXIF | N/A | Use device time, flag for review |
| Future timestamp | <0 | Auto-reject (invalid) |

### Edge Cases

| Case | Handling |
|------|----------|
| Timezone mismatch | Use UTC for all comparisons |
| Device clock wrong | Compare EXIF vs device time, flag anomalies |
| Screenshot of photo | Reject - no valid EXIF |
| Edited photo | Detect stripped/modified EXIF, flag |
| Gallery upload | Extract EXIF from original file |

---

## Technical Implementation

### EXIF Timestamp Extraction

```javascript
const ExifParser = require('exif-parser');

async function extractPhotoTimestamp(photoBuffer) {
  try {
    const parser = ExifParser.create(photoBuffer);
    const result = parser.parse();

    // Priority: DateTimeOriginal > DateTime > CreateDate
    const timestamp = result.tags.DateTimeOriginal
      || result.tags.DateTime
      || result.tags.CreateDate;

    if (timestamp) {
      // EXIF timestamps are in local time, convert to Date
      return new Date(timestamp * 1000);
    }

    return null;
  } catch (error) {
    console.error('EXIF parsing failed:', error);
    return null;
  }
}
```

### Timestamp Validation Service

```javascript
const MAX_AGE_HOURS = 24;
const GRACE_PERIOD_HOURS = 1;

class TimestampValidator {
  validate(photoTimestamp, deviceTimestamp, serverTimestamp) {
    const result = {
      isValid: false,
      photoAge: null,
      source: null,
      warnings: [],
      errors: []
    };

    // Use EXIF timestamp if available, otherwise device time
    const effectiveTimestamp = photoTimestamp || deviceTimestamp;
    result.source = photoTimestamp ? 'exif' : 'device';

    if (!effectiveTimestamp) {
      result.errors.push('NO_TIMESTAMP');
      return result;
    }

    // Calculate age in hours
    const ageMs = serverTimestamp - new Date(effectiveTimestamp).getTime();
    result.photoAge = ageMs / (1000 * 60 * 60);

    // Check for future timestamp (device clock issue)
    if (result.photoAge < -0.1) { // Allow 6 minutes for processing
      result.errors.push('FUTURE_TIMESTAMP');
      return result;
    }

    // Check for expired photo
    const maxAge = MAX_AGE_HOURS + GRACE_PERIOD_HOURS;
    if (result.photoAge > maxAge) {
      result.errors.push('PHOTO_TOO_OLD');
      return result;
    }

    // Warnings for edge cases
    if (result.photoAge > MAX_AGE_HOURS) {
      result.warnings.push('USED_GRACE_PERIOD');
    }

    if (!photoTimestamp) {
      result.warnings.push('NO_EXIF_TIMESTAMP');
    }

    // Check for anomalies
    if (photoTimestamp && deviceTimestamp) {
      const diff = Math.abs(photoTimestamp - deviceTimestamp) / (1000 * 60);
      if (diff > 60) { // More than 1 hour difference
        result.warnings.push('TIMESTAMP_ANOMALY');
      }
    }

    result.isValid = true;
    return result;
  }
}
```

### Validation Logging

```javascript
async function logTimestampValidation(submission, validationResult) {
  await auditLog.create({
    type: 'TIMESTAMP_VALIDATION',
    applicationId: submission.applicationId,
    teacherId: submission.teacherId,
    data: {
      photoTimestamp: submission.photoExifTimestamp,
      deviceTimestamp: submission.deviceCaptureTime,
      serverTimestamp: new Date().toISOString(),
      photoAgeHours: validationResult.photoAge,
      timestampSource: validationResult.source,
      isValid: validationResult.isValid,
      warnings: validationResult.warnings,
      errors: validationResult.errors
    },
    timestamp: new Date()
  });
}
```

### Integration in Submission Pipeline

```javascript
async function processSubmission(submission) {
  const serverTimestamp = Date.now();

  // Extract EXIF timestamp from photo
  const photoBuffer = await fetchPhotoBuffer(submission.dogPhoto);
  const exifTimestamp = await extractPhotoTimestamp(photoBuffer);

  // Validate timestamp
  const validator = new TimestampValidator();
  const validation = validator.validate(
    exifTimestamp,
    submission.deviceCaptureTime,
    serverTimestamp
  );

  // Log the validation
  await logTimestampValidation(submission, validation);

  if (!validation.isValid) {
    // Auto-reject with specific reason
    const reason = validation.errors.includes('PHOTO_TOO_OLD')
      ? 'TIMESTAMP_OLD'
      : 'INVALID_TIMESTAMP';

    await autoRejectSubmission(submission, reason);
    return { success: false, reason };
  }

  // Continue with other validations
  submission.timestampValidation = validation;
  return { success: true, validation };
}
```

### Client-Side Pre-validation

```javascript
// Warn user before submission if photo is old
async function prevalidatePhoto(photoFile) {
  const exifTimestamp = await extractClientExifTimestamp(photoFile);
  const ageHours = (Date.now() - exifTimestamp) / (1000 * 60 * 60);

  if (ageHours > 24) {
    return {
      valid: false,
      message: 'This photo is more than 24 hours old. Please take a new photo.'
    };
  }

  if (ageHours > 20) {
    return {
      valid: true,
      warning: 'This photo will expire soon. Please submit quickly.'
    };
  }

  return { valid: true };
}
```

---

## Error Messages

| Error Code | User Message |
|------------|--------------|
| PHOTO_TOO_OLD | This photo was taken more than 24 hours ago. Please take a new photo of the dog. |
| FUTURE_TIMESTAMP | The photo timestamp is invalid. Please ensure your device date/time is correct. |
| NO_TIMESTAMP | Could not verify photo timestamp. Please take a photo using the app camera. |
| INVALID_TIMESTAMP | Photo timestamp could not be verified. Please try again with a fresh photo. |

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| exif-parser | Library | Parse EXIF metadata |
| Audit Log | Service | Record validations |
| NTP | Service | Server time synchronization |

---

## Related Stories

- [S-VAL-02](./S-VAL-02.md) - GPS boundary validation
- [S-REJ-01](./S-REJ-01.md) - Auto-reject old photos
- [T-SUB-01](../teacher/T-SUB-01.md) - Capture dog photo
