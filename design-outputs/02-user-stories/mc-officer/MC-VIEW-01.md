# MC-VIEW-01: Map View with Incident Markers

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As an** MC Officer
**I want to** see a map view with verified incident markers in my jurisdiction
**So that** I can visualize where stray dogs have been reported.

---

## Description

The MC Officer's primary interface is a map-centric view showing all verified incidents in their assigned jurisdiction. Each incident appears as a marker on the map, color-coded by status and priority. The map enables quick visual assessment of incident distribution and helps plan efficient field routes.

---

## Acceptance Criteria

### Map Display

- [ ] Map loads centered on officer's assigned jurisdiction
- [ ] Map shows only incidents within officer's assigned blocks/zones
- [ ] Incidents displayed as clickable markers
- [ ] Markers color-coded by status (new/in-progress/completed)
- [ ] Markers sized by priority (larger = higher priority)
- [ ] Map supports pinch-to-zoom and pan gestures
- [ ] Map auto-refreshes every 5 minutes
- [ ] Pull-to-refresh for manual update

### Marker States

| Status | Color | Icon |
|--------|-------|------|
| New (Unassigned) | Red | 🔴 |
| Assigned (to this officer) | Orange | 🟠 |
| In Progress | Yellow | 🟡 |
| Captured/Resolved | Green | 🟢 |
| Unable to Locate | Gray | ⚫ |

### Marker Interactions

- [ ] Tap marker to show quick preview
- [ ] Double-tap to open full details
- [ ] Long-press to get directions
- [ ] Cluster markers when zoomed out
- [ ] Show count in cluster markers

### Jurisdiction Boundary

- [ ] Officer's jurisdiction boundary shown as overlay
- [ ] Boundary color indicates active zone
- [ ] Out-of-jurisdiction incidents not shown

---

## UI/UX Requirements

### Map View (Main Screen)

```
┌─────────────────────────────────────────────────────────────┐
│  My Assignments                          🔍 📊 ⚙️           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │                    ┌────────────────┐                 │  │
│  │                    │   🔴  (3)      │                 │  │
│  │         🟠         └────────────────┘                 │  │
│  │                                                       │  │
│  │    🔴                                   🟡            │  │
│  │                                                       │  │
│  │              ╭─────────────────────╮                  │  │
│  │         🔴   │   Boulaos Zone      │     🟠           │  │
│  │              ╰─────────────────────╯                  │  │
│  │                                                       │  │
│  │    🟢                       🔴                        │  │
│  │                                                       │  │
│  │                                                       │  │
│  │         ⚫               🔴                           │  │
│  │                                                       │  │
│  │                                                       │  │
│  │  ┌──────┐                                             │  │
│  │  │ + −  │                                             │  │
│  │  └──────┘                                             │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  📍 Boulaos Zone    New: 5   In Progress: 2           │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                                                      │   │
│  │  🗺️ Map    📋 List    🔥 Hotspots    📈 Stats        │   │
│  │                                                      │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Marker Quick Preview

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  │                         │                                │
│  │    (Map continues)      │                                │
│  │                         │                                │
│  │                         │                                │
│  │         🔴 ←────────────┤                                │
│  │                         │                                │
│  │                         │                                │
│  └─────────────────────────┘                                │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │  SDC-2026-001234                          🔴 NEW      │  │
│  │                                                       │  │
│  │  ┌────────────┐                                       │  │
│  │  │            │  📍 Boulaos, near school              │  │
│  │  │  [Photo    │  ⏱️ Reported 45 mins ago              │  │
│  │  │  preview]  │  🏷️ Injured, Aggressive               │  │
│  │  │            │                                       │  │
│  │  └────────────┘                                       │  │
│  │                                                       │  │
│  │  ┌────────────────────┐  ┌────────────────────────┐   │  │
│  │  │    🧭 Navigate     │  │    📄 View Details     │   │  │
│  │  └────────────────────┘  └────────────────────────┘   │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Clustered Markers (Zoomed Out)

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │                                                       │  │
│  │              ┌───────┐                                │  │
│  │              │  12   │            ┌───────┐           │  │
│  │              │  🔴   │            │   5   │           │  │
│  │              └───────┘            │  🟠   │           │  │
│  │                                   └───────┘           │  │
│  │                                                       │  │
│  │     ┌───────┐                                         │  │
│  │     │   8   │                           ┌───────┐     │  │
│  │     │  🔴   │                           │   3   │     │  │
│  │     └───────┘                           │  🟢   │     │  │
│  │                                         └───────┘     │  │
│  │                                                       │  │
│  │                                                       │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  Tap cluster to zoom in                                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Technical Implementation

### Map Component with Markers

```javascript
class IncidentMapView extends Component {
  constructor(props) {
    super(props);
    this.state = {
      incidents: [],
      selectedIncident: null,
      isLoading: true,
      mapCenter: null,
      zoomLevel: 13,
      jurisdictionBoundary: null
    };
    this.map = null;
    this.refreshInterval = null;
  }

  async componentDidMount() {
    await this.initializeMap();
    await this.loadJurisdiction();
    await this.loadIncidents();
    this.startAutoRefresh();
  }

  componentWillUnmount() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  }

  async initializeMap() {
    const { officerLocation } = this.props;

    this.map = new maplibregl.Map({
      container: 'incident-map',
      style: 'https://tiles.stadiamaps.com/styles/osm_bright.json',
      center: [officerLocation.longitude, officerLocation.latitude],
      zoom: this.state.zoomLevel
    });

    this.map.on('load', () => {
      this.addMapControls();
      this.addClusterSource();
    });

    // Handle marker clicks
    this.map.on('click', 'incident-markers', (e) => {
      const incident = e.features[0].properties;
      this.showQuickPreview(incident);
    });

    // Handle cluster clicks
    this.map.on('click', 'clusters', (e) => {
      const features = this.map.queryRenderedFeatures(e.point, {
        layers: ['clusters']
      });
      const clusterId = features[0].properties.cluster_id;
      this.map.getSource('incidents').getClusterExpansionZoom(
        clusterId,
        (err, zoom) => {
          if (err) return;
          this.map.easeTo({
            center: features[0].geometry.coordinates,
            zoom: zoom + 1
          });
        }
      );
    });
  }

  addMapControls() {
    // Zoom controls
    this.map.addControl(new maplibregl.NavigationControl(), 'bottom-left');

    // Geolocation
    this.map.addControl(
      new maplibregl.GeolocateControl({
        positionOptions: { enableHighAccuracy: true },
        trackUserLocation: true
      }),
      'bottom-left'
    );
  }

  addClusterSource() {
    this.map.addSource('incidents', {
      type: 'geojson',
      data: { type: 'FeatureCollection', features: [] },
      cluster: true,
      clusterMaxZoom: 14,
      clusterRadius: 50
    });

    // Cluster circles
    this.map.addLayer({
      id: 'clusters',
      type: 'circle',
      source: 'incidents',
      filter: ['has', 'point_count'],
      paint: {
        'circle-color': [
          'step',
          ['get', 'point_count'],
          '#ff6b6b', // Red for small clusters
          10, '#ffa500', // Orange for medium
          25, '#ff4444'  // Darker red for large
        ],
        'circle-radius': [
          'step',
          ['get', 'point_count'],
          20,
          10, 30,
          25, 40
        ]
      }
    });

    // Cluster count labels
    this.map.addLayer({
      id: 'cluster-count',
      type: 'symbol',
      source: 'incidents',
      filter: ['has', 'point_count'],
      layout: {
        'text-field': '{point_count_abbreviated}',
        'text-font': ['Open Sans Bold'],
        'text-size': 14
      },
      paint: {
        'text-color': '#ffffff'
      }
    });

    // Individual markers
    this.map.addLayer({
      id: 'incident-markers',
      type: 'circle',
      source: 'incidents',
      filter: ['!', ['has', 'point_count']],
      paint: {
        'circle-color': [
          'match',
          ['get', 'status'],
          'new', '#ff4444',
          'assigned', '#ffa500',
          'in_progress', '#ffcc00',
          'captured', '#44ff44',
          'unable_to_locate', '#888888',
          '#ff4444'
        ],
        'circle-radius': [
          'match',
          ['get', 'priority'],
          'high', 12,
          'medium', 10,
          'low', 8,
          10
        ],
        'circle-stroke-width': 2,
        'circle-stroke-color': '#ffffff'
      }
    });
  }

  async loadJurisdiction() {
    const { officerId } = this.props;

    try {
      const response = await fetch(`/api/mc-officer/${officerId}/jurisdiction`);
      const { boundary, center } = await response.json();

      this.setState({
        jurisdictionBoundary: boundary,
        mapCenter: center
      });

      // Add jurisdiction boundary to map
      this.map.addSource('jurisdiction', {
        type: 'geojson',
        data: boundary
      });

      this.map.addLayer({
        id: 'jurisdiction-fill',
        type: 'fill',
        source: 'jurisdiction',
        paint: {
          'fill-color': '#4285f4',
          'fill-opacity': 0.1
        }
      });

      this.map.addLayer({
        id: 'jurisdiction-line',
        type: 'line',
        source: 'jurisdiction',
        paint: {
          'line-color': '#4285f4',
          'line-width': 2,
          'line-dasharray': [2, 2]
        }
      });

      // Fit map to jurisdiction
      const bounds = this.getBoundsFromGeoJSON(boundary);
      this.map.fitBounds(bounds, { padding: 50 });

    } catch (error) {
      console.error('Failed to load jurisdiction:', error);
    }
  }

  async loadIncidents() {
    const { officerId } = this.props;

    this.setState({ isLoading: true });

    try {
      const response = await fetch(
        `/api/mc-officer/${officerId}/incidents?includeCompleted=false`
      );
      const { incidents } = await response.json();

      // Convert to GeoJSON
      const geojson = {
        type: 'FeatureCollection',
        features: incidents.map(incident => ({
          type: 'Feature',
          geometry: {
            type: 'Point',
            coordinates: [incident.longitude, incident.latitude]
          },
          properties: {
            id: incident.applicationId,
            status: incident.status,
            priority: incident.priority,
            conditionTags: incident.conditionTags,
            reportedAt: incident.reportedAt,
            block: incident.block,
            photoUrl: incident.dogPhotoUrl
          }
        }))
      };

      // Update map source
      this.map.getSource('incidents').setData(geojson);

      this.setState({
        incidents,
        isLoading: false
      });

    } catch (error) {
      console.error('Failed to load incidents:', error);
      this.setState({ isLoading: false });
    }
  }

  startAutoRefresh() {
    this.refreshInterval = setInterval(() => {
      this.loadIncidents();
    }, 5 * 60 * 1000); // 5 minutes
  }

  showQuickPreview(incidentProperties) {
    const incident = this.state.incidents.find(
      i => i.applicationId === incidentProperties.id
    );

    this.setState({ selectedIncident: incident });
  }

  async navigateToIncident(incident) {
    const { latitude, longitude } = incident;

    // Open in device maps app
    const url = Platform.OS === 'ios'
      ? `maps://app?daddr=${latitude},${longitude}`
      : `google.navigation:q=${latitude},${longitude}`;

    try {
      await Linking.openURL(url);
    } catch (error) {
      // Fallback to Google Maps web
      window.open(
        `https://www.google.com/maps/dir/?api=1&destination=${latitude},${longitude}`,
        '_blank'
      );
    }
  }

  render() {
    const { incidents, selectedIncident, isLoading } = this.state;
    const newCount = incidents.filter(i => i.status === 'new').length;
    const inProgressCount = incidents.filter(i => i.status === 'in_progress').length;

    return (
      <div className="incident-map-container">
        <header className="map-header">
          <h1>My Assignments</h1>
          <div className="header-actions">
            <button onClick={() => this.props.onSearch()}>🔍</button>
            <button onClick={() => this.props.onHeatmap()}>📊</button>
            <button onClick={() => this.props.onSettings()}>⚙️</button>
          </div>
        </header>

        <div id="incident-map" className="map-container" />

        <div className="map-summary">
          <span className="zone-name">📍 {this.props.zoneName}</span>
          <span className="count new">New: {newCount}</span>
          <span className="count progress">In Progress: {inProgressCount}</span>
        </div>

        {selectedIncident && (
          <QuickPreviewCard
            incident={selectedIncident}
            onNavigate={() => this.navigateToIncident(selectedIncident)}
            onViewDetails={() => this.props.onViewDetails(selectedIncident)}
            onClose={() => this.setState({ selectedIncident: null })}
          />
        )}

        <BottomNavigation
          active="map"
          onMapClick={() => {}}
          onListClick={() => this.props.onShowList()}
          onHotspotsClick={() => this.props.onShowHotspots()}
          onStatsClick={() => this.props.onShowStats()}
        />
      </div>
    );
  }
}
```

### Quick Preview Card Component

```javascript
function QuickPreviewCard({ incident, onNavigate, onViewDetails, onClose }) {
  const statusColors = {
    new: '#ff4444',
    assigned: '#ffa500',
    in_progress: '#ffcc00',
    captured: '#44ff44',
    unable_to_locate: '#888888'
  };

  const statusLabels = {
    new: 'NEW',
    assigned: 'ASSIGNED',
    in_progress: 'IN PROGRESS',
    captured: 'CAPTURED',
    unable_to_locate: 'UTL'
  };

  const timeAgo = formatTimeAgo(incident.reportedAt);

  return (
    <div className="quick-preview-card">
      <button className="close-btn" onClick={onClose}>×</button>

      <div className="preview-header">
        <span className="incident-id">{incident.applicationId}</span>
        <span
          className="status-badge"
          style={{ backgroundColor: statusColors[incident.status] }}
        >
          {statusLabels[incident.status]}
        </span>
      </div>

      <div className="preview-body">
        <div className="photo-container">
          <img src={incident.dogPhotoUrl} alt="Reported dog" />
        </div>

        <div className="incident-info">
          <div className="info-row">
            <span className="icon">📍</span>
            <span>{incident.block}, {incident.address}</span>
          </div>
          <div className="info-row">
            <span className="icon">⏱️</span>
            <span>Reported {timeAgo}</span>
          </div>
          <div className="info-row">
            <span className="icon">🏷️</span>
            <span>{incident.conditionTags.join(', ')}</span>
          </div>
        </div>
      </div>

      <div className="preview-actions">
        <button className="action-btn navigate" onClick={onNavigate}>
          🧭 Navigate
        </button>
        <button className="action-btn details" onClick={onViewDetails}>
          📄 View Details
        </button>
      </div>
    </div>
  );
}
```

### API Endpoints

```javascript
// GET /api/mc-officer/:officerId/incidents
async function getMCOfficerIncidents(req, res) {
  const { officerId } = req.params;
  const { includeCompleted = 'false', status, priority } = req.query;

  // Verify officer
  const officer = await db.users.findOne({
    id: officerId,
    role: 'MC_OFFICER'
  });

  if (!officer) {
    return res.status(404).json({ error: 'Officer not found' });
  }

  // Build query
  const query = {
    tenantId: officer.tenantId,
    $or: [
      { mcOfficerId: officerId },
      { status: 'approved', block: { $in: officer.assignedBlocks } }
    ]
  };

  if (includeCompleted !== 'true') {
    query.status = { $nin: ['captured', 'resolved', 'unable_to_locate'] };
  }

  if (status) {
    query.status = status;
  }

  if (priority) {
    query.priority = parseInt(priority);
  }

  const incidents = await db.submissions.find(query)
    .sort({ priority: -1, reportedAt: -1 })
    .toArray();

  // Hide teacher info for privacy
  const sanitized = incidents.map(incident => ({
    applicationId: incident.applicationId,
    status: incident.mcStatus || incident.status,
    priority: incident.priority,
    latitude: incident.latitude,
    longitude: incident.longitude,
    block: incident.block,
    address: incident.reverseGeocodedAddress,
    conditionTags: incident.conditionTags,
    notes: incident.notes,
    dogPhotoUrl: incident.dogPhotoUrl,
    reportedAt: incident.submittedAt,
    assignedAt: incident.assignedToMCAt,
    // Note: No teacher info included
  }));

  return res.json({ incidents: sanitized });
}

// GET /api/mc-officer/:officerId/jurisdiction
async function getMCOfficerJurisdiction(req, res) {
  const { officerId } = req.params;

  const officer = await db.users.findOne({ id: officerId });

  if (!officer) {
    return res.status(404).json({ error: 'Officer not found' });
  }

  // Get boundary from egov-location
  const boundaries = await Promise.all(
    officer.assignedBlocks.map(block =>
      egov.location.getBoundary(officer.tenantId, block)
    )
  );

  // Merge boundaries into single GeoJSON
  const mergedBoundary = mergeBoundaries(boundaries);

  // Calculate center
  const center = calculateCentroid(mergedBoundary);

  return res.json({
    boundary: mergedBoundary,
    center,
    blocks: officer.assignedBlocks
  });
}
```

---

## Performance Considerations

```javascript
// Marker optimization for large datasets
const MARKER_LIMIT = 500;

function optimizeMarkers(incidents, zoomLevel) {
  if (incidents.length <= MARKER_LIMIT) {
    return incidents;
  }

  // At low zoom, show only high priority
  if (zoomLevel < 10) {
    return incidents
      .filter(i => i.priority >= 3 || i.status === 'new')
      .slice(0, MARKER_LIMIT);
  }

  // At medium zoom, prioritize by recency and priority
  if (zoomLevel < 13) {
    return incidents
      .sort((a, b) => {
        if (b.priority !== a.priority) return b.priority - a.priority;
        return new Date(b.reportedAt) - new Date(a.reportedAt);
      })
      .slice(0, MARKER_LIMIT);
  }

  // At high zoom, show all in viewport
  return incidents;
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| MapLibre GL | Library | Map rendering |
| egov-location | DIGIT Service | Jurisdiction boundaries |
| Device GPS | Device API | Officer location |

---

## Related Stories

- [MC-VIEW-02](./MC-VIEW-02.md) - Heatmap overlay
- [MC-VIEW-03](./MC-VIEW-03.md) - Filter incidents
- [MC-VIEW-04](./MC-VIEW-04.md) - Incident details
- [MC-FLD-02](./MC-FLD-02.md) - Navigate to location
