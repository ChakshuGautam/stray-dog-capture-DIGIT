# T-SUB-03: GPS Auto-Extraction

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** have GPS coordinates automatically extracted from my photos,
**So that** the exact location is recorded without manual entry.

---

## Description

GPS coordinates should be automatically captured and associated with each submission. The system should extract GPS from photo EXIF data when available, or capture device GPS at the time of photo capture. This ensures accurate location data without requiring teachers to manually enter addresses.

---

## Acceptance Criteria

### Functional

- [ ] GPS captured automatically when photo is taken in-app
- [ ] GPS extracted from EXIF data for gallery photos
- [ ] Device GPS used as fallback if EXIF GPS unavailable
- [ ] GPS accuracy indicator shown to user (High/Medium/Low)
- [ ] Location displayed as human-readable address (reverse geocoding)
- [ ] Map preview showing captured location
- [ ] Teacher cannot manually override GPS coordinates
- [ ] GPS validated against tenant boundary before submission
- [ ] Submissions outside boundary rejected with clear message
- [ ] GPS coordinates stored with 6 decimal precision

### GPS Sources (Priority Order)

| Priority | Source | When Used |
|----------|--------|-----------|
| 1 | Photo EXIF GPS | Always preferred if available |
| 2 | Device GPS (High Accuracy) | Fallback, captured at photo time |
| 3 | Device GPS (Low Accuracy) | Last resort, flagged for review |

### Accuracy Thresholds

| Accuracy | Range | Indicator | Action |
|----------|-------|-----------|--------|
| High | ≤50m | 🟢 | Accept |
| Medium | 51-100m | 🟡 | Accept with flag |
| Low | >100m | 🔴 | Warn user, suggest retry |

---

## UI/UX Requirements (PWA)

### Location Capture Indicator

```
┌─────────────────────────────────┐
│  📍 Location                    │
├─────────────────────────────────┤
│                                 │
│  ┌─────────────────────────┐    │
│  │      [Map Preview]      │    │
│  │         📍              │    │
│  │    Your location        │    │
│  └─────────────────────────┘    │
│                                 │
│  🟢 High accuracy (±15m)        │
│                                 │
│  Near: Rue de Venise,           │
│        Djibouti City            │
│                                 │
│  Coordinates captured ✓         │
│                                 │
└─────────────────────────────────┘
```

### Low Accuracy Warning

```
┌─────────────────────────────────┐
│  ⚠️ Low GPS Accuracy            │
├─────────────────────────────────┤
│                                 │
│  🔴 Current accuracy: ±250m     │
│                                 │
│  Your GPS signal is weak. This  │
│  may affect your submission.    │
│                                 │
│  Tips to improve:               │
│  • Move to an open area         │
│  • Wait a few seconds           │
│  • Ensure location is enabled   │
│                                 │
│  [  Try Again  ]                │
│  [  Continue Anyway  ]          │
│                                 │
└─────────────────────────────────┘
```

### Outside Boundary Error

```
┌─────────────────────────────────┐
│  ❌ Location Outside Area       │
├─────────────────────────────────┤
│                                 │
│  This location is outside the   │
│  program service area.          │
│                                 │
│  Your location:                 │
│  📍 [Location on map]           │
│                                 │
│  Service area:                  │
│  📍 Djibouti City boundaries    │
│                                 │
│  Submissions can only be made   │
│  within the service area.       │
│                                 │
│  [  OK  ]                       │
│                                 │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Request location permission with clear rationale
- [ ] Use high accuracy mode for GPS (`enableHighAccuracy: true`)
- [ ] Set reasonable timeout (10 seconds) for GPS fix
- [ ] Cache last known location for quick display
- [ ] Use reverse geocoding API for address display
- [ ] Validate GPS against boundary client-side before upload
- [ ] Store GPS in submission payload, not as separate request

---

## Technical Notes

### GPS Capture Implementation

```javascript
const gpsOptions = {
  enableHighAccuracy: true,
  timeout: 10000,
  maximumAge: 0
};

async function captureGPS() {
  return new Promise((resolve, reject) => {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          latitude: parseFloat(position.coords.latitude.toFixed(6)),
          longitude: parseFloat(position.coords.longitude.toFixed(6)),
          accuracy: position.coords.accuracy,
          timestamp: position.timestamp,
          source: 'device'
        });
      },
      (error) => reject(error),
      gpsOptions
    );
  });
}
```

### EXIF GPS Extraction

```javascript
function extractExifGPS(imageFile) {
  return new Promise((resolve) => {
    EXIF.getData(imageFile, function() {
      const latRef = EXIF.getTag(this, 'GPSLatitudeRef');
      const lat = EXIF.getTag(this, 'GPSLatitude');
      const lngRef = EXIF.getTag(this, 'GPSLongitudeRef');
      const lng = EXIF.getTag(this, 'GPSLongitude');

      if (lat && lng) {
        resolve({
          latitude: convertDMSToDD(lat, latRef),
          longitude: convertDMSToDD(lng, lngRef),
          source: 'exif'
        });
      } else {
        resolve(null);
      }
    });
  });
}
```

### Boundary Validation (Client-side)

```javascript
// Using Turf.js for point-in-polygon check
import * as turf from '@turf/turf';

function isWithinBoundary(lat, lng, boundaryGeoJSON) {
  const point = turf.point([lng, lat]);
  return turf.booleanPointInPolygon(point, boundaryGeoJSON);
}
```

### Reverse Geocoding

```javascript
async function reverseGeocode(lat, lng) {
  const response = await fetch(
    `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`
  );
  const data = await response.json();
  return data.display_name;
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Geolocation API | Browser | Device GPS capture |
| exif-js | Library | Extract GPS from photos |
| @turf/turf | Library | Geospatial calculations |
| Nominatim API | External | Reverse geocoding |
| egov-location | DIGIT | Tenant boundary GeoJSON |

---

## Related Stories

- [T-SUB-01](./T-SUB-01.md) - Capture dog photo
- [T-SUB-02](./T-SUB-02.md) - Capture selfie
- [S-VAL-02](../system/S-VAL-02.md) - Boundary validation
- [S-REJ-02](../system/S-REJ-02.md) - Auto-reject outside boundary
