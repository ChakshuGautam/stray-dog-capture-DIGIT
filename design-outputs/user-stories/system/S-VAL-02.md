# S-VAL-02: Validate GPS Within Boundary

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## System Behavior

**The system** validates that GPS coordinates fall within the configured tenant boundary
**To ensure** submissions are only accepted from the program's operational area.

---

## Description

Each tenant (district/municipality) has a defined geographic boundary stored as a GeoJSON polygon in the DIGIT egov-location service. When a submission is received, the system validates that the GPS coordinates fall within this boundary. Submissions outside the boundary are auto-rejected.

---

## Acceptance Criteria

### Validation Rules

- [ ] Fetch tenant boundary from egov-location service
- [ ] Perform point-in-polygon check using GPS coordinates
- [ ] Cache boundary polygon for performance (refresh daily)
- [ ] Allow small buffer zone (50m) for GPS inaccuracy
- [ ] Auto-reject submissions clearly outside boundary
- [ ] Flag edge cases for manual review
- [ ] Support multi-polygon boundaries (non-contiguous areas)
- [ ] Log all boundary validations for audit

### Validation Outcomes

| GPS Location | Distance to Boundary | Action |
|--------------|---------------------|--------|
| Inside boundary | N/A | Accept |
| Within buffer (50m outside) | 0-50m | Accept with flag |
| Outside boundary | >50m | Auto-reject |
| Invalid coordinates | N/A | Reject - invalid GPS |
| Missing coordinates | N/A | Reject - GPS required |

### Edge Cases

| Case | Handling |
|------|----------|
| GPS near boundary edge | Apply 50m buffer zone |
| Very low GPS accuracy | Flag for review, allow if within 200m buffer |
| Ocean/water coordinates | Auto-reject (invalid location) |
| Known exclusion zones | Separate check against exclusion polygons |
| Multiple tenants | Match to nearest containing tenant |

---

## Technical Implementation

### Fetch Tenant Boundary (DIGIT egov-location)

```javascript
const BOUNDARY_CACHE_TTL = 24 * 60 * 60 * 1000; // 24 hours
let boundaryCache = {};

async function getTenantBoundary(tenantId) {
  // Check cache first
  const cached = boundaryCache[tenantId];
  if (cached && Date.now() - cached.timestamp < BOUNDARY_CACHE_TTL) {
    return cached.boundary;
  }

  // Fetch from egov-location
  const response = await fetch(
    `${DIGIT_BASE_URL}/egov-location/location/v11/boundarys/_search`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        RequestInfo: getRequestInfo(),
        tenantId: tenantId,
        hierarchyTypeCode: 'ADMIN',
        boundaryType: 'City'
      })
    }
  );

  const data = await response.json();
  const boundary = extractGeoJSON(data.TenantBoundary[0]);

  // Cache the boundary
  boundaryCache[tenantId] = {
    boundary,
    timestamp: Date.now()
  };

  return boundary;
}

function extractGeoJSON(tenantBoundary) {
  // Convert DIGIT boundary format to GeoJSON
  return {
    type: 'Feature',
    geometry: tenantBoundary.geometry,
    properties: {
      tenantId: tenantBoundary.tenantId,
      name: tenantBoundary.name
    }
  };
}
```

### Point-in-Polygon Validation

```javascript
const turf = require('@turf/turf');

class BoundaryValidator {
  constructor(boundaryGeoJSON) {
    this.boundary = boundaryGeoJSON;
    // Create buffered boundary for edge cases
    this.bufferedBoundary = turf.buffer(boundaryGeoJSON, 50, { units: 'meters' });
  }

  validate(latitude, longitude, gpsAccuracy = 0) {
    const result = {
      isValid: false,
      isInBoundary: false,
      isInBuffer: false,
      distanceToBoundary: null,
      warnings: [],
      errors: []
    };

    // Validate coordinates
    if (!this.isValidCoordinate(latitude, longitude)) {
      result.errors.push('INVALID_COORDINATES');
      return result;
    }

    const point = turf.point([longitude, latitude]);

    // Check if point is inside boundary
    result.isInBoundary = turf.booleanPointInPolygon(point, this.boundary);

    if (result.isInBoundary) {
      result.isValid = true;
      return result;
    }

    // Check if point is in buffer zone
    result.isInBuffer = turf.booleanPointInPolygon(point, this.bufferedBoundary);

    if (result.isInBuffer) {
      result.isValid = true;
      result.warnings.push('IN_BUFFER_ZONE');
      return result;
    }

    // Calculate distance to boundary for rejection message
    const nearestPoint = turf.nearestPointOnLine(
      turf.polygonToLine(this.boundary),
      point
    );
    result.distanceToBoundary = turf.distance(point, nearestPoint, { units: 'meters' });

    // Check if GPS accuracy might explain being outside
    if (gpsAccuracy > 100 && result.distanceToBoundary < gpsAccuracy + 50) {
      result.isValid = true;
      result.warnings.push('LOW_GPS_ACCURACY');
      result.warnings.push('EDGE_CASE_REVIEW');
      return result;
    }

    result.errors.push('OUTSIDE_BOUNDARY');
    return result;
  }

  isValidCoordinate(lat, lng) {
    return (
      typeof lat === 'number' &&
      typeof lng === 'number' &&
      lat >= -90 && lat <= 90 &&
      lng >= -180 && lng <= 180 &&
      !this.isInWater(lat, lng)
    );
  }

  isInWater(lat, lng) {
    // Simple check for Djibouti context - Gulf of Tadjoura/Red Sea
    // Real implementation would use land/water polygon
    if (lng > 43.5 && lat < 11.3) return true; // Gulf area
    return false;
  }
}
```

### Validation Logging

```javascript
async function logBoundaryValidation(submission, validationResult) {
  await auditLog.create({
    type: 'BOUNDARY_VALIDATION',
    applicationId: submission.applicationId,
    teacherId: submission.teacherId,
    data: {
      latitude: submission.latitude,
      longitude: submission.longitude,
      gpsAccuracy: submission.gpsAccuracy,
      tenantId: submission.tenantId,
      isInBoundary: validationResult.isInBoundary,
      isInBuffer: validationResult.isInBuffer,
      distanceToBoundary: validationResult.distanceToBoundary,
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
async function validateSubmissionBoundary(submission) {
  // Get tenant boundary
  const boundary = await getTenantBoundary(submission.tenantId);
  const validator = new BoundaryValidator(boundary);

  // Validate GPS coordinates
  const result = validator.validate(
    submission.latitude,
    submission.longitude,
    submission.gpsAccuracy
  );

  // Log validation
  await logBoundaryValidation(submission, result);

  if (!result.isValid) {
    // Auto-reject with specific reason
    await autoRejectSubmission(submission, 'OUTSIDE_AREA', {
      distance: result.distanceToBoundary
    });

    return {
      success: false,
      reason: 'OUTSIDE_AREA',
      distance: result.distanceToBoundary
    };
  }

  // Flag edge cases for verifier review
  if (result.warnings.includes('EDGE_CASE_REVIEW')) {
    submission.flags = submission.flags || [];
    submission.flags.push('BOUNDARY_EDGE_CASE');
  }

  return { success: true, result };
}
```

### Client-Side Pre-validation

```javascript
// Pre-validate on client before submission
async function prevalidateBoundary(latitude, longitude, tenantId) {
  // Fetch cached boundary or get from server
  const boundary = await getCachedBoundary(tenantId);

  const point = turf.point([longitude, latitude]);
  const isInside = turf.booleanPointInPolygon(point, boundary);

  if (!isInside) {
    // Check buffer
    const buffered = turf.buffer(boundary, 100, { units: 'meters' });
    const isInBuffer = turf.booleanPointInPolygon(point, buffered);

    if (isInBuffer) {
      return {
        valid: true,
        warning: 'You appear to be near the edge of the service area.'
      };
    }

    return {
      valid: false,
      message: 'This location is outside the SDCRS service area. Submissions can only be made within Djibouti City.'
    };
  }

  return { valid: true };
}
```

---

## Error Messages

| Error Code | User Message |
|------------|--------------|
| OUTSIDE_AREA | This location is outside the program service area. Submissions can only be made within the designated boundaries. |
| INVALID_COORDINATES | The GPS coordinates are invalid. Please ensure location services are enabled. |
| GPS_REQUIRED | GPS location is required for all submissions. Please enable location services and try again. |
| LOW_ACCURACY | Your GPS accuracy is low. Please move to an open area for better accuracy. |

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| egov-location | DIGIT | Tenant boundary data |
| @turf/turf | Library | Geospatial calculations |
| Audit Log | Service | Record validations |

---

## Related Stories

- [S-VAL-01](./S-VAL-01.md) - Timestamp validation
- [S-REJ-02](./S-REJ-02.md) - Auto-reject outside boundary
- [T-SUB-03](../teacher/T-SUB-03.md) - GPS auto-extraction
