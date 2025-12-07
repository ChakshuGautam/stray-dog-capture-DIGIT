# V-REV-01: Review Submission Evidence

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Verifier
**I want to** see the dog photo, selfie, map location, and EXIF metadata side-by-side
**So that** I can assess the submission's validity efficiently.

---

## Description

The verifier needs a comprehensive view of all submission evidence to make quick, informed decisions. The interface displays photos, location data, metadata, and condition tags in a single view optimized for rapid review.

---

## Acceptance Criteria

### Evidence Display

- [ ] Dog photo displayed prominently with zoom capability
- [ ] Selfie displayed alongside with teacher face visible
- [ ] Map shows pinpoint location from GPS coordinates
- [ ] EXIF metadata displayed (timestamp, device, GPS)
- [ ] Condition tags shown as colored badges
- [ ] Teacher notes displayed if present
- [ ] Submission timestamp clearly visible

### Photo Viewer

- [ ] Pinch-to-zoom on both photos
- [ ] Full-screen view on tap
- [ ] Swipe between dog photo and selfie
- [ ] Photo source indicator (Camera/Gallery)
- [ ] Photo age displayed prominently

### Map View

- [ ] Map centered on submission GPS
- [ ] Satellite view toggle available
- [ ] Zoom controls present
- [ ] Tenant boundary overlay visible
- [ ] Distance to nearest boundary shown
- [ ] Nearby submissions (24h) marked

### Metadata Panel

- [ ] Photo timestamp
- [ ] GPS coordinates
- [ ] GPS accuracy in meters
- [ ] Device make/model
- [ ] Photo dimensions
- [ ] File size
- [ ] Hash fingerprint (truncated)

---

## UI/UX Requirements

### Review Screen Layout

```
┌─────────────────────────────────────────────────────────────┐
│  ← Queue                    Review Submission    [Skip] [•••]│
├─────────────────────────────────────────────────────────────┤
│  Application ID: SDC-2024-001234         Priority: ●●●○○    │
│  Submitted: 2 hours ago                  SLA: 22h remaining │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────────────┐  ┌──────────────────────┐         │
│  │                      │  │                      │         │
│  │    [Dog Photo]       │  │    [Selfie]          │         │
│  │                      │  │                      │         │
│  │                      │  │                      │         │
│  │    📷 Camera         │  │    📷 Camera         │         │
│  │    36 min ago        │  │    35 min ago        │         │
│  └──────────────────────┘  └──────────────────────┘         │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  Condition Tags:                                            │
│  ┌──────────┐ ┌──────────┐ ┌──────────────┐                │
│  │ 🟡 Normal│ │ 🔴 Injured│ │ 🟢 With Puppies│              │
│  └──────────┘ └──────────┘ └──────────────┘                │
├─────────────────────────────────────────────────────────────┤
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │              [Map View with Pin]                      │  │
│  │                                                       │  │
│  │         📍 Block: Boulaos                            │  │
│  │         📍 11.5892°N, 43.1456°E                      │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│  📝 Notes:                                                  │
│  "Dog appears to be limping, found near school gate"        │
├─────────────────────────────────────────────────────────────┤
│  Metadata:                                                  │
│  ├─ Timestamp: 2024-01-15 09:23:45                         │
│  ├─ GPS Accuracy: 8m                                       │
│  ├─ Device: iPhone 12                                      │
│  ├─ Photo Age: 36 minutes                                  │
│  └─ Distance from Selfie: 12m ✓                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌────────────────────┐        ┌────────────────────┐      │
│  │                    │        │                    │      │
│  │     ❌ REJECT      │        │     ✓ APPROVE      │      │
│  │                    │        │                    │      │
│  └────────────────────┘        └────────────────────┘      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Photo Comparison View

```
┌─────────────────────────────────────────────────────────────┐
│  ← Back                     Photo Comparison                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │                                                       │  │
│  │               [Full Screen Dog Photo]                 │  │
│  │                                                       │  │
│  │                                                       │  │
│  │                                                       │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌─────────┐ ┌─────────┐                                   │
│  │ [thumb] │ │ [thumb] │      Swipe or tap to switch      │
│  │   Dog   │ │ Selfie  │                                   │
│  └─────────┘ └─────────┘                                   │
│                                                             │
│  Pinch to zoom • Double-tap to reset                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Technical Implementation

### Fetch Submission Details

```javascript
async function fetchSubmissionForReview(queueId) {
  const queueEntry = await db.verifierQueue.findOne({ queueId });
  const submission = await db.submissions.findOne({
    applicationId: queueEntry.applicationId
  });

  // Get signed URLs for photos (time-limited)
  const dogPhotoUrl = await getSignedUrl(submission.dogPhotoPath, 3600);
  const selfieUrl = await getSignedUrl(submission.selfiePath, 3600);

  // Get nearby submissions for context
  const nearbySubmissions = await getNearbySubmissions(
    submission.location,
    24 // hours
  );

  return {
    queueEntry,
    submission: {
      applicationId: submission.applicationId,
      teacherId: submission.teacherId, // Hidden from MC, visible to Verifier
      dogPhoto: {
        url: dogPhotoUrl,
        timestamp: submission.dogPhotoTimestamp,
        source: submission.dogPhotoSource,
        age: calculateAge(submission.dogPhotoTimestamp)
      },
      selfie: {
        url: selfieUrl,
        timestamp: submission.selfieTimestamp,
        source: submission.selfieSource
      },
      location: {
        latitude: submission.latitude,
        longitude: submission.longitude,
        accuracy: submission.gpsAccuracy,
        block: submission.block,
        address: submission.reverseGeocodedAddress
      },
      conditionTags: submission.conditionTags,
      notes: submission.notes,
      metadata: {
        device: submission.deviceInfo,
        photoHash: submission.dogPhotoHash.substring(0, 16) + '...',
        gpsDistance: submission.selfieToPhotoDistance
      },
      flags: submission.flags || [],
      validationResults: submission.validationResults
    },
    nearbySubmissions,
    sla: {
      deadline: queueEntry.timestamps.slaDeadline,
      remaining: calculateTimeRemaining(queueEntry.timestamps.slaDeadline)
    }
  };
}
```

### Photo Viewer Component

```javascript
class PhotoViewer extends Component {
  constructor(props) {
    super(props);
    this.state = {
      activePhoto: 'dog', // 'dog' | 'selfie'
      zoom: 1,
      panX: 0,
      panY: 0,
      isFullScreen: false
    };
  }

  handlePinchZoom(scale) {
    const newZoom = Math.min(Math.max(this.state.zoom * scale, 1), 5);
    this.setState({ zoom: newZoom });
  }

  handlePan(deltaX, deltaY) {
    if (this.state.zoom > 1) {
      this.setState({
        panX: this.state.panX + deltaX,
        panY: this.state.panY + deltaY
      });
    }
  }

  handleDoubleTap() {
    this.setState({ zoom: 1, panX: 0, panY: 0 });
  }

  handleSwipe(direction) {
    if (direction === 'left' && this.state.activePhoto === 'dog') {
      this.setState({ activePhoto: 'selfie', zoom: 1, panX: 0, panY: 0 });
    } else if (direction === 'right' && this.state.activePhoto === 'selfie') {
      this.setState({ activePhoto: 'dog', zoom: 1, panX: 0, panY: 0 });
    }
  }

  toggleFullScreen() {
    this.setState({ isFullScreen: !this.state.isFullScreen });
  }

  render() {
    const { dogPhoto, selfie } = this.props;
    const currentPhoto = this.state.activePhoto === 'dog' ? dogPhoto : selfie;

    return (
      <div className={`photo-viewer ${this.state.isFullScreen ? 'fullscreen' : ''}`}>
        <GestureHandler
          onPinch={this.handlePinchZoom.bind(this)}
          onPan={this.handlePan.bind(this)}
          onDoubleTap={this.handleDoubleTap.bind(this)}
          onSwipe={this.handleSwipe.bind(this)}
        >
          <img
            src={currentPhoto.url}
            style={{
              transform: `scale(${this.state.zoom}) translate(${this.state.panX}px, ${this.state.panY}px)`
            }}
            onClick={this.toggleFullScreen.bind(this)}
          />
        </GestureHandler>

        <div className="photo-meta">
          <span className="source-badge">
            {currentPhoto.source === 'camera' ? '📷 Camera' : '🖼 Gallery'}
          </span>
          <span className="age-badge">{formatAge(currentPhoto.age)}</span>
        </div>

        <div className="photo-thumbnails">
          <Thumbnail
            photo={dogPhoto}
            active={this.state.activePhoto === 'dog'}
            label="Dog"
            onClick={() => this.setState({ activePhoto: 'dog' })}
          />
          <Thumbnail
            photo={selfie}
            active={this.state.activePhoto === 'selfie'}
            label="Selfie"
            onClick={() => this.setState({ activePhoto: 'selfie' })}
          />
        </div>
      </div>
    );
  }
}
```

### Map Component with Boundary

```javascript
class SubmissionMap extends Component {
  constructor(props) {
    super(props);
    this.state = {
      viewMode: 'roadmap', // 'roadmap' | 'satellite'
      showBoundary: true,
      showNearby: false
    };
  }

  async componentDidMount() {
    const { location, tenantId } = this.props;

    // Initialize map
    this.map = new maplibregl.Map({
      container: 'review-map',
      style: 'mapbox://styles/mapbox/streets-v11',
      center: [location.longitude, location.latitude],
      zoom: 15
    });

    // Add submission marker
    new maplibregl.Marker({ color: '#FF0000' })
      .setLngLat([location.longitude, location.latitude])
      .addTo(this.map);

    // Load and display tenant boundary
    const boundary = await fetchTenantBoundary(tenantId);
    this.map.addSource('boundary', {
      type: 'geojson',
      data: boundary
    });
    this.map.addLayer({
      id: 'boundary-fill',
      type: 'fill',
      source: 'boundary',
      paint: {
        'fill-color': '#00FF00',
        'fill-opacity': 0.1
      }
    });
    this.map.addLayer({
      id: 'boundary-line',
      type: 'line',
      source: 'boundary',
      paint: {
        'line-color': '#00AA00',
        'line-width': 2
      }
    });
  }

  toggleSatellite() {
    const newMode = this.state.viewMode === 'roadmap' ? 'satellite' : 'roadmap';
    this.map.setStyle(
      newMode === 'satellite'
        ? 'mapbox://styles/mapbox/satellite-v9'
        : 'mapbox://styles/mapbox/streets-v11'
    );
    this.setState({ viewMode: newMode });
  }

  showNearbySubmissions() {
    const { nearbySubmissions } = this.props;

    nearbySubmissions.forEach(sub => {
      new maplibregl.Marker({ color: '#FFAA00' })
        .setLngLat([sub.longitude, sub.latitude])
        .setPopup(new maplibregl.Popup().setHTML(
          `<b>${sub.applicationId}</b><br/>
           ${formatTimeAgo(sub.createdAt)}<br/>
           Status: ${sub.status}`
        ))
        .addTo(this.map);
    });

    this.setState({ showNearby: true });
  }

  render() {
    return (
      <div className="submission-map">
        <div id="review-map" />
        <div className="map-controls">
          <button onClick={this.toggleSatellite.bind(this)}>
            {this.state.viewMode === 'roadmap' ? '🛰 Satellite' : '🗺 Map'}
          </button>
          <button onClick={this.showNearbySubmissions.bind(this)}>
            📍 Show Nearby ({this.props.nearbySubmissions.length})
          </button>
        </div>
        <div className="location-info">
          <p>📍 {this.props.location.block}</p>
          <p>🎯 {this.props.location.latitude.toFixed(4)}°N, {this.props.location.longitude.toFixed(4)}°E</p>
          <p>📡 Accuracy: {this.props.location.accuracy}m</p>
        </div>
      </div>
    );
  }
}
```

---

## Performance Considerations

- Lazy load photos only when scrolled into view
- Use progressive image loading (blur to sharp)
- Cache boundary GeoJSON locally
- Preload next submission in queue
- Use WebP format for faster loading

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| MapLibre GL | Library | Interactive maps |
| Hammer.js | Library | Touch gestures |
| S3/Storage | External | Photo storage |
| egov-location | DIGIT | Boundary data |

---

## Related Stories

- [V-REV-02](./V-REV-02.md) - Compare duplicates
- [V-REV-03](./V-REV-03.md) - View fraud flags
- [V-APP-01](./V-APP-01.md) - Approve submission
- [V-REJ-01](./V-REJ-01.md) - Reject submission
