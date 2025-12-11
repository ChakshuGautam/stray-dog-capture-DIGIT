# T-SUB-01: Capture Dog Photo

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** capture and upload a photo of a stray dog,
**So that** I can report its presence for Municipal Corporation action.

---

## Description

The core submission action starts with capturing a photo of the stray dog. The photo serves as primary evidence for verification and helps MC Officers identify the dog during field visits. The PWA should provide an intuitive camera interface optimized for quick capture in field conditions.

---

## Acceptance Criteria

### Functional

- [ ] Teacher can access camera directly from home screen "Report" button
- [ ] Camera opens in rear-facing mode by default
- [ ] Teacher can switch to front camera if needed
- [ ] Teacher can capture photo using on-screen shutter button
- [ ] Photo preview shown after capture with Retake/Use options
- [ ] GPS coordinates extracted from device at capture time
- [ ] Timestamp embedded in photo EXIF data
- [ ] Photo compressed to <2MB while maintaining quality
- [ ] Teacher can select existing photo from gallery (within 48 hours)
- [ ] Gallery photos validated for EXIF GPS and timestamp
- [ ] Photos older than 48 hours rejected with clear message
- [ ] Flash toggle available for low-light conditions
- [ ] Grid overlay option for better framing

### Photo Requirements

| Attribute | Requirement |
|-----------|-------------|
| Format | JPEG |
| Max Size | 2MB (compressed) |
| Min Resolution | 640x480 |
| Max Resolution | 4096x4096 |
| GPS | Required (from EXIF or device) |
| Timestamp | Required (within 48 hours) |

### Validation Rules

- [ ] Photo must contain GPS coordinates (EXIF or device location)
- [ ] Photo timestamp must be within 48 hours of upload
- [ ] Photo must meet minimum resolution requirements
- [ ] Photo file must be valid JPEG format
- [ ] Photo must not be a duplicate (checked server-side via pHash)

---

## UI/UX Requirements (PWA)

### Camera Screen

```
┌─────────────────────────────────┐
│  ✕                    [📷⟳]    │
│                                 │
│                                 │
│                                 │
│     ┌─────────────────────┐     │
│     │                     │     │
│     │    Camera Preview   │     │
│     │                     │     │
│     │    [Grid Overlay]   │     │
│     │                     │     │
│     └─────────────────────┘     │
│                                 │
│  📍 GPS: Capturing...           │
│                                 │
│  ┌─────┐           ┌─────┐      │
│  │ ⚡  │    [◉]    │ 🖼️ │      │
│  │Flash│  Capture  │Gallery     │
│  └─────┘           └─────┘      │
└─────────────────────────────────┘
```

### Photo Preview Screen

```
┌─────────────────────────────────┐
│  ← Retake                       │
│                                 │
│  ┌─────────────────────────┐    │
│  │                         │    │
│  │    Captured Photo       │    │
│  │                         │    │
│  │                         │    │
│  └─────────────────────────┘    │
│                                 │
│  📍 Location captured           │
│  🕐 Dec 7, 2026 2:34 PM         │
│                                 │
│  [    Use This Photo    ]       │
│                                 │
│  Next: Take selfie at location  │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Use `navigator.mediaDevices.getUserMedia()` for camera access
- [ ] Request camera permission with clear explanation
- [ ] Handle permission denied gracefully
- [ ] Fallback to file input if camera API unavailable
- [ ] Use `navigator.geolocation` for GPS capture
- [ ] Show GPS accuracy indicator
- [ ] Compress image client-side using Canvas API
- [ ] Store captured photo in IndexedDB for offline queue
- [ ] Progressive upload with retry on failure

### Camera Permission Flow

```
┌─────────────────────────────────┐
│                                 │
│  📷 Camera Access Required      │
│                                 │
│  SDCRS needs camera access to   │
│  capture photos of stray dogs   │
│  for your reports.              │
│                                 │
│  [    Allow Camera    ]         │
│  [    Not Now    ]              │
│                                 │
└─────────────────────────────────┘
```

---

## Technical Notes

### Camera API Implementation

```javascript
async function openCamera() {
  const constraints = {
    video: {
      facingMode: 'environment', // Rear camera
      width: { ideal: 1920 },
      height: { ideal: 1080 }
    }
  };

  const stream = await navigator.mediaDevices.getUserMedia(constraints);
  videoElement.srcObject = stream;
}
```

### GPS Capture

```javascript
async function captureGPS() {
  return new Promise((resolve, reject) => {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
          accuracy: position.coords.accuracy
        });
      },
      (error) => reject(error),
      { enableHighAccuracy: true, timeout: 10000 }
    );
  });
}
```

### Image Compression

```javascript
async function compressImage(file, maxSizeMB = 2) {
  const options = {
    maxSizeMB: maxSizeMB,
    maxWidthOrHeight: 1920,
    useWebWorker: true
  };
  return await imageCompression(file, options);
}
```

### EXIF Extraction

Use `exif-js` library to extract GPS and timestamp from gallery photos:

```javascript
EXIF.getData(imageFile, function() {
  const lat = EXIF.getTag(this, 'GPSLatitude');
  const lng = EXIF.getTag(this, 'GPSLongitude');
  const timestamp = EXIF.getTag(this, 'DateTimeOriginal');
});
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Camera API | Browser | `getUserMedia` for camera access |
| Geolocation API | Browser | GPS coordinate capture |
| IndexedDB | Browser | Offline photo storage |
| image-compression | Library | Client-side image compression |
| exif-js | Library | EXIF data extraction |

---

## Related Stories

- [T-SUB-02](./T-SUB-02.md) - Capture selfie at location
- [T-SUB-03](./T-SUB-03.md) - GPS auto-extraction
- [T-SUB-04](./T-SUB-04.md) - Dog condition tagging
- [T-OFF-01](./T-OFF-01.md) - Offline submission queue
