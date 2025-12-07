# MC-VIEW-02: Heatmap Overlay for Incident Density

> **Back to:** [User Stories Master](../../design-outputs/02-user-stories.md)

## User Story

**As an** MC Officer,
**I want to** see a heatmap overlay showing incident density,
**So that** I can identify hotspot areas and plan efficient patrol routes.

## Description

The heatmap overlay provides a visual representation of incident concentration across the jurisdiction. Dense areas appear in warmer colors (red/orange) while sparse areas show cooler colors (blue/green). This helps MC Officers prioritize areas with recurring stray dog problems and optimize their patrol patterns.

## Acceptance Criteria

| # | Criterion | Priority |
|---|-----------|----------|
| 1 | Heatmap toggle button available on map interface | Must |
| 2 | Heatmap uses gradient from blue (low) → yellow → red (high) | Must |
| 3 | Heatmap intensity based on incident count per area | Must |
| 4 | Heatmap respects current time filter (last 7/30/90 days) | Must |
| 5 | Heatmap updates when panning/zooming the map | Must |
| 6 | Incident markers remain visible on top of heatmap | Should |
| 7 | Heatmap can be combined with status filters | Should |
| 8 | Performance remains smooth with 1000+ data points | Must |
| 9 | Legend shows intensity scale | Should |
| 10 | Heatmap radius adjusts based on zoom level | Should |

## UI/UX Design

### Map with Heatmap Toggle

```
┌─────────────────────────────────────────────────────────────┐
│ ◀ Back          Incidents Map           [Filter] [Layers]  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │     ╭──────╮                                        │   │
│  │    ╱ ░░░░░░ ╲     ← Light blue (1-2 incidents)     │   │
│  │   │ ░░░░░░░░ │                                      │   │
│  │    ╲ ░░░░░░ ╱                                       │   │
│  │     ╰──────╯                                        │   │
│  │                      ╭──────────╮                   │   │
│  │                     ╱ ▓▓▓▓▓▓▓▓▓▓ ╲  ← Orange       │   │
│  │        ╭────╮      │ ██████████ │    (5-10)        │   │
│  │       │ ▒▒▒▒ │     │ ██████████ │                  │   │
│  │        ╰────╯       ╲ ▓▓▓▓▓▓▓▓ ╱  ← Yellow        │   │
│  │       Yellow         ╰────────╯                    │   │
│  │       (3-5)                                        │   │
│  │                                      ╭────╮        │   │
│  │                    [📍]             │ ░░ │         │   │
│  │         [📍]                         ╰────╯        │   │
│  │                         [📍]                       │   │
│  │                                                    │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌────────────────────────────────────┐                    │
│  │ Legend:  Low ░░▒▒▓▓██ High        │                    │
│  │          1   ────────────→  10+    │                    │
│  └────────────────────────────────────┘                    │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  [🗺️ Markers]  [🔥 Heatmap ✓]  [📊 Clusters]        │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Layer Selection Panel

```
┌─────────────────────────────────────────────────────────────┐
│ ◀ Back              Map Layers                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │  BASE LAYERS                                        │   │
│  │  ─────────────────────────────────────────────     │   │
│  │  ○ Standard Map                                     │   │
│  │  ● Satellite                                        │   │
│  │  ○ Terrain                                          │   │
│  │                                                     │   │
│  │  OVERLAYS                                           │   │
│  │  ─────────────────────────────────────────────     │   │
│  │  ☑ Incident Markers                                │   │
│  │  ☑ Heatmap                                         │   │
│  │  ☐ Jurisdiction Boundary                           │   │
│  │  ☐ Cluster View                                    │   │
│  │                                                     │   │
│  │  HEATMAP OPTIONS                                   │   │
│  │  ─────────────────────────────────────────────     │   │
│  │  Time Range: [Last 30 Days ▼]                      │   │
│  │                                                     │   │
│  │  Status Filter:                                    │   │
│  │  ☑ All Statuses                                    │   │
│  │  ☐ Only Pending (New/Assigned/In Progress)         │   │
│  │  ☐ Only Resolved (Captured/UTL)                    │   │
│  │                                                     │   │
│  │  Intensity: ───────●───────                        │   │
│  │             Low         High                        │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              [ Apply Changes ]                      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Heatmap with Hotspot Indicator

```
┌─────────────────────────────────────────────────────────────┐
│ ◀ Back          Incidents Map           [Filter] [Layers]  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │                         ╭────────────╮              │   │
│  │                        ╱ ████████████ ╲             │   │
│  │                       │ ██████████████ │            │   │
│  │                       │ ██████████████ │ ← HOTSPOT  │   │
│  │            ╭────╮     │ ██████████████ │   12 cases │   │
│  │           ╱ ▒▒▒▒ ╲     ╲ ████████████ ╱             │   │
│  │          │ ▓▓▓▓▓▓ │     ╰────────────╯              │   │
│  │           ╲ ▒▒▒▒ ╱                                  │   │
│  │            ╰────╯                                   │   │
│  │                                                     │   │
│  │                              ╭────╮                 │   │
│  │       ╭──╮                  │ ░░ │                  │   │
│  │      │ ░░ │                  ╰────╯                 │   │
│  │       ╰──╯                                         │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ⚠️ Hotspot Detected: Market District               │   │
│  │  12 incidents in last 30 days                       │   │
│  │  [View Details]                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Technical Implementation

### Heatmap Layer Configuration

```javascript
// services/HeatmapService.js
class HeatmapService {
  constructor(map) {
    this.map = map;
    this.sourceId = 'heatmap-source';
    this.layerId = 'heatmap-layer';
    this.isVisible = false;
  }

  /**
   * Initialize heatmap source and layer
   */
  initialize() {
    // Add GeoJSON source for heatmap data
    this.map.addSource(this.sourceId, {
      type: 'geojson',
      data: {
        type: 'FeatureCollection',
        features: []
      }
    });

    // Add heatmap layer with gradient configuration
    this.map.addLayer({
      id: this.layerId,
      type: 'heatmap',
      source: this.sourceId,
      maxzoom: 18,
      paint: {
        // Weight based on incident weight property (default 1)
        'heatmap-weight': [
          'interpolate',
          ['linear'],
          ['get', 'weight'],
          0, 0,
          1, 1,
          5, 2
        ],

        // Intensity increases with zoom
        'heatmap-intensity': [
          'interpolate',
          ['linear'],
          ['zoom'],
          0, 1,
          15, 3
        ],

        // Color gradient: blue → cyan → green → yellow → orange → red
        'heatmap-color': [
          'interpolate',
          ['linear'],
          ['heatmap-density'],
          0, 'rgba(0, 0, 255, 0)',
          0.1, 'rgba(65, 105, 225, 0.5)',   // Royal Blue
          0.3, 'rgba(0, 255, 255, 0.6)',     // Cyan
          0.5, 'rgba(0, 255, 0, 0.7)',       // Green
          0.7, 'rgba(255, 255, 0, 0.8)',     // Yellow
          0.85, 'rgba(255, 165, 0, 0.9)',    // Orange
          1, 'rgba(255, 0, 0, 1)'            // Red
        ],

        // Radius increases with zoom for smoother appearance
        'heatmap-radius': [
          'interpolate',
          ['linear'],
          ['zoom'],
          0, 2,
          10, 20,
          15, 40
        ],

        // Fade out heatmap at high zoom
        'heatmap-opacity': [
          'interpolate',
          ['linear'],
          ['zoom'],
          14, 1,
          18, 0.6
        ]
      }
    }, 'incidents-markers'); // Insert below markers layer

    // Initially hidden
    this.hide();
  }

  /**
   * Update heatmap data with incidents
   * @param {Array} incidents - Array of incident objects
   */
  updateData(incidents) {
    const features = incidents.map(incident => ({
      type: 'Feature',
      properties: {
        id: incident.id,
        weight: this.calculateWeight(incident),
        status: incident.status
      },
      geometry: {
        type: 'Point',
        coordinates: [incident.longitude, incident.latitude]
      }
    }));

    this.map.getSource(this.sourceId).setData({
      type: 'FeatureCollection',
      features
    });
  }

  /**
   * Calculate weight based on incident attributes
   * Recent incidents and unresolved ones have higher weight
   */
  calculateWeight(incident) {
    let weight = 1;

    // Boost weight for recent incidents
    const daysOld = this.getDaysOld(incident.reportedDate);
    if (daysOld <= 7) weight += 1;
    if (daysOld <= 3) weight += 1;

    // Boost weight for pending incidents
    if (['NEW', 'ASSIGNED', 'IN_PROGRESS'].includes(incident.status)) {
      weight += 1;
    }

    return weight;
  }

  getDaysOld(dateString) {
    const reported = new Date(dateString);
    const now = new Date();
    const diffTime = Math.abs(now - reported);
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }

  /**
   * Show heatmap layer
   */
  show() {
    this.map.setLayoutProperty(this.layerId, 'visibility', 'visible');
    this.isVisible = true;
  }

  /**
   * Hide heatmap layer
   */
  hide() {
    this.map.setLayoutProperty(this.layerId, 'visibility', 'none');
    this.isVisible = false;
  }

  /**
   * Toggle heatmap visibility
   */
  toggle() {
    if (this.isVisible) {
      this.hide();
    } else {
      this.show();
    }
    return this.isVisible;
  }

  /**
   * Update heatmap intensity
   * @param {number} intensity - Value between 0 and 1
   */
  setIntensity(intensity) {
    const scaledIntensity = intensity * 5; // Scale to 0-5 range
    this.map.setPaintProperty(this.layerId, 'heatmap-intensity', [
      'interpolate',
      ['linear'],
      ['zoom'],
      0, scaledIntensity * 0.5,
      15, scaledIntensity
    ]);
  }

  /**
   * Cleanup resources
   */
  destroy() {
    if (this.map.getLayer(this.layerId)) {
      this.map.removeLayer(this.layerId);
    }
    if (this.map.getSource(this.sourceId)) {
      this.map.removeSource(this.sourceId);
    }
  }
}

export default HeatmapService;
```

### Hotspot Detection Service

```javascript
// services/HotspotDetectionService.js
class HotspotDetectionService {
  constructor() {
    this.gridSize = 0.005; // ~500m grid cells
    this.hotspotThreshold = 5; // Minimum incidents to be a hotspot
  }

  /**
   * Detect hotspots from incident data
   * @param {Array} incidents - Array of incidents with lat/lng
   * @param {Object} options - Detection options
   * @returns {Array} Array of hotspot objects
   */
  detectHotspots(incidents, options = {}) {
    const {
      threshold = this.hotspotThreshold,
      gridSize = this.gridSize
    } = options;

    // Group incidents into grid cells
    const grid = this.createGrid(incidents, gridSize);

    // Find cells exceeding threshold
    const hotspots = [];

    for (const [key, cell] of Object.entries(grid)) {
      if (cell.count >= threshold) {
        hotspots.push({
          id: key,
          center: cell.center,
          count: cell.count,
          incidents: cell.incidents,
          boundingBox: cell.boundingBox,
          severity: this.calculateSeverity(cell.count, threshold),
          areaName: null // Will be populated by reverse geocoding
        });
      }
    }

    // Sort by severity (highest first)
    hotspots.sort((a, b) => b.count - a.count);

    return hotspots;
  }

  /**
   * Create grid of cells from incidents
   */
  createGrid(incidents, gridSize) {
    const grid = {};

    incidents.forEach(incident => {
      const cellX = Math.floor(incident.longitude / gridSize);
      const cellY = Math.floor(incident.latitude / gridSize);
      const key = `${cellX}_${cellY}`;

      if (!grid[key]) {
        grid[key] = {
          count: 0,
          incidents: [],
          sumLat: 0,
          sumLng: 0,
          boundingBox: {
            minLat: Infinity,
            maxLat: -Infinity,
            minLng: Infinity,
            maxLng: -Infinity
          }
        };
      }

      const cell = grid[key];
      cell.count++;
      cell.incidents.push(incident.id);
      cell.sumLat += incident.latitude;
      cell.sumLng += incident.longitude;

      // Update bounding box
      cell.boundingBox.minLat = Math.min(cell.boundingBox.minLat, incident.latitude);
      cell.boundingBox.maxLat = Math.max(cell.boundingBox.maxLat, incident.latitude);
      cell.boundingBox.minLng = Math.min(cell.boundingBox.minLng, incident.longitude);
      cell.boundingBox.maxLng = Math.max(cell.boundingBox.maxLng, incident.longitude);
    });

    // Calculate center for each cell
    for (const cell of Object.values(grid)) {
      cell.center = {
        latitude: cell.sumLat / cell.count,
        longitude: cell.sumLng / cell.count
      };
    }

    return grid;
  }

  /**
   * Calculate hotspot severity level
   */
  calculateSeverity(count, threshold) {
    const ratio = count / threshold;
    if (ratio >= 3) return 'CRITICAL';
    if (ratio >= 2) return 'HIGH';
    if (ratio >= 1.5) return 'MEDIUM';
    return 'LOW';
  }

  /**
   * Enrich hotspots with area names via reverse geocoding
   */
  async enrichWithAreaNames(hotspots) {
    const enriched = await Promise.all(
      hotspots.map(async (hotspot) => {
        try {
          const areaName = await this.reverseGeocode(
            hotspot.center.latitude,
            hotspot.center.longitude
          );
          return { ...hotspot, areaName };
        } catch (error) {
          console.warn('Reverse geocoding failed for hotspot:', hotspot.id);
          return { ...hotspot, areaName: 'Unknown Area' };
        }
      })
    );
    return enriched;
  }

  /**
   * Reverse geocode coordinates to area name
   */
  async reverseGeocode(lat, lng) {
    // Integration with DIGIT location service or OSM Nominatim
    const response = await fetch(
      `/egov-location/v1/reverseGeocode?lat=${lat}&lng=${lng}`
    );
    const data = await response.json();
    return data.locality || data.district || 'Unknown Area';
  }
}

export default HotspotDetectionService;
```

### Heatmap Filter Component

```javascript
// components/HeatmapFilterPanel.jsx
import React, { useState } from 'react';

const TIME_RANGES = [
  { value: 7, label: 'Last 7 Days' },
  { value: 30, label: 'Last 30 Days' },
  { value: 90, label: 'Last 90 Days' },
  { value: 365, label: 'Last Year' }
];

const STATUS_FILTERS = [
  { value: 'ALL', label: 'All Statuses' },
  { value: 'PENDING', label: 'Only Pending' },
  { value: 'RESOLVED', label: 'Only Resolved' }
];

function HeatmapFilterPanel({ onFilterChange, initialFilters }) {
  const [timeRange, setTimeRange] = useState(initialFilters?.timeRange || 30);
  const [statusFilter, setStatusFilter] = useState(initialFilters?.statusFilter || 'ALL');
  const [intensity, setIntensity] = useState(initialFilters?.intensity || 0.5);

  const handleApply = () => {
    onFilterChange({
      timeRange,
      statusFilter,
      intensity
    });
  };

  return (
    <div className="heatmap-filter-panel">
      <h3>Heatmap Options</h3>

      <div className="filter-group">
        <label>Time Range</label>
        <select
          value={timeRange}
          onChange={(e) => setTimeRange(Number(e.target.value))}
        >
          {TIME_RANGES.map(range => (
            <option key={range.value} value={range.value}>
              {range.label}
            </option>
          ))}
        </select>
      </div>

      <div className="filter-group">
        <label>Status Filter</label>
        {STATUS_FILTERS.map(filter => (
          <label key={filter.value} className="radio-label">
            <input
              type="radio"
              name="statusFilter"
              value={filter.value}
              checked={statusFilter === filter.value}
              onChange={(e) => setStatusFilter(e.target.value)}
            />
            {filter.label}
          </label>
        ))}
      </div>

      <div className="filter-group">
        <label>Intensity: {Math.round(intensity * 100)}%</label>
        <input
          type="range"
          min="0"
          max="1"
          step="0.1"
          value={intensity}
          onChange={(e) => setIntensity(Number(e.target.value))}
        />
        <div className="range-labels">
          <span>Low</span>
          <span>High</span>
        </div>
      </div>

      <button className="btn-primary" onClick={handleApply}>
        Apply Changes
      </button>
    </div>
  );
}

export default HeatmapFilterPanel;
```

### Hotspot Alert Component

```javascript
// components/HotspotAlert.jsx
import React from 'react';

const SEVERITY_STYLES = {
  CRITICAL: { bg: '#ffebee', border: '#f44336', icon: '🔴' },
  HIGH: { bg: '#fff3e0', border: '#ff9800', icon: '🟠' },
  MEDIUM: { bg: '#fffde7', border: '#ffeb3b', icon: '🟡' },
  LOW: { bg: '#e3f2fd', border: '#2196f3', icon: '🔵' }
};

function HotspotAlert({ hotspot, onViewDetails, onDismiss }) {
  const style = SEVERITY_STYLES[hotspot.severity] || SEVERITY_STYLES.LOW;

  return (
    <div
      className="hotspot-alert"
      style={{
        backgroundColor: style.bg,
        borderLeft: `4px solid ${style.border}`
      }}
    >
      <div className="hotspot-alert-header">
        <span className="severity-icon">{style.icon}</span>
        <span className="alert-title">
          Hotspot Detected: {hotspot.areaName || 'Area'}
        </span>
        <button className="dismiss-btn" onClick={onDismiss}>✕</button>
      </div>

      <div className="hotspot-alert-body">
        <p>{hotspot.count} incidents in selected time range</p>
        <p className="severity-label">Severity: {hotspot.severity}</p>
      </div>

      <div className="hotspot-alert-actions">
        <button
          className="btn-link"
          onClick={() => onViewDetails(hotspot)}
        >
          View Details →
        </button>
      </div>
    </div>
  );
}

export default HotspotAlert;
```

### Integration with Main Map

```javascript
// components/IncidentMapView.jsx (heatmap integration)
import HeatmapService from '../services/HeatmapService';
import HotspotDetectionService from '../services/HotspotDetectionService';

class IncidentMapView {
  constructor(containerId, options) {
    // ... existing initialization
    this.heatmapService = null;
    this.hotspotService = new HotspotDetectionService();
    this.activeHotspots = [];
  }

  onMapLoad() {
    // ... existing setup

    // Initialize heatmap layer
    this.heatmapService = new HeatmapService(this.map);
    this.heatmapService.initialize();
  }

  /**
   * Toggle heatmap and detect hotspots
   */
  async toggleHeatmap(filters = {}) {
    const isNowVisible = this.heatmapService.toggle();

    if (isNowVisible) {
      // Fetch incidents for heatmap based on filters
      const incidents = await this.fetchIncidentsForHeatmap(filters);
      this.heatmapService.updateData(incidents);

      // Detect and display hotspots
      const hotspots = this.hotspotService.detectHotspots(incidents);
      this.activeHotspots = await this.hotspotService.enrichWithAreaNames(hotspots);

      // Notify UI about detected hotspots
      if (this.activeHotspots.length > 0) {
        this.onHotspotsDetected?.(this.activeHotspots);
      }
    }

    return isNowVisible;
  }

  /**
   * Fetch incidents for heatmap with time range filter
   */
  async fetchIncidentsForHeatmap(filters) {
    const { timeRange = 30, statusFilter = 'ALL' } = filters;

    const startDate = new Date();
    startDate.setDate(startDate.getDate() - timeRange);

    const params = new URLSearchParams({
      jurisdictionId: this.jurisdictionId,
      fromDate: startDate.toISOString(),
      toDate: new Date().toISOString()
    });

    if (statusFilter !== 'ALL') {
      const statuses = statusFilter === 'PENDING'
        ? ['NEW', 'ASSIGNED', 'IN_PROGRESS']
        : ['CAPTURED', 'UTL'];
      params.append('statuses', statuses.join(','));
    }

    const response = await fetch(`/sdcrs/incidents/v1/_search?${params}`);
    const data = await response.json();
    return data.incidents || [];
  }

  /**
   * Update heatmap filters
   */
  async updateHeatmapFilters(filters) {
    if (this.heatmapService.isVisible) {
      const incidents = await this.fetchIncidentsForHeatmap(filters);
      this.heatmapService.updateData(incidents);
      this.heatmapService.setIntensity(filters.intensity || 0.5);

      // Re-detect hotspots
      const hotspots = this.hotspotService.detectHotspots(incidents, {
        threshold: filters.hotspotThreshold || 5
      });
      this.activeHotspots = await this.hotspotService.enrichWithAreaNames(hotspots);
      this.onHotspotsDetected?.(this.activeHotspots);
    }
  }

  /**
   * Zoom to hotspot location
   */
  zoomToHotspot(hotspot) {
    this.map.flyTo({
      center: [hotspot.center.longitude, hotspot.center.latitude],
      zoom: 15,
      duration: 1000
    });
  }
}
```

## PWA-Specific Considerations

### Performance Optimization

```javascript
// utils/heatmapOptimization.js

/**
 * Downsample incidents for large datasets to maintain performance
 * @param {Array} incidents - Full incident array
 * @param {number} maxPoints - Maximum points for heatmap
 * @returns {Array} Downsampled incidents
 */
export function downsampleForHeatmap(incidents, maxPoints = 2000) {
  if (incidents.length <= maxPoints) {
    return incidents;
  }

  // Use spatial sampling to maintain density representation
  const samplingRate = maxPoints / incidents.length;
  const sampled = [];

  // Grid-based sampling to preserve spatial distribution
  const gridSize = 0.001; // ~100m cells
  const grid = {};

  incidents.forEach(incident => {
    const key = `${Math.floor(incident.latitude / gridSize)}_${Math.floor(incident.longitude / gridSize)}`;
    if (!grid[key]) {
      grid[key] = [];
    }
    grid[key].push(incident);
  });

  // Sample proportionally from each cell
  for (const cell of Object.values(grid)) {
    const cellSampleCount = Math.max(1, Math.round(cell.length * samplingRate));
    const shuffled = cell.sort(() => 0.5 - Math.random());
    sampled.push(...shuffled.slice(0, cellSampleCount));
  }

  return sampled.slice(0, maxPoints);
}

/**
 * Cache heatmap data in IndexedDB for offline viewing
 */
export async function cacheHeatmapData(jurisdictionId, incidents) {
  const db = await openHeatmapDB();
  const tx = db.transaction('heatmapCache', 'readwrite');
  const store = tx.objectStore('heatmapCache');

  await store.put({
    jurisdictionId,
    incidents,
    timestamp: Date.now(),
    expiresAt: Date.now() + (24 * 60 * 60 * 1000) // 24 hours
  });
}
```

## API Endpoints

### GET /sdcrs/incidents/v1/heatmap-data

Returns aggregated incident data optimized for heatmap rendering.

**Request:**
```
GET /sdcrs/incidents/v1/heatmap-data?jurisdictionId=JU001&fromDate=2024-01-01&toDate=2024-01-31&statuses=NEW,ASSIGNED
```

**Response:**
```json
{
  "ResponseInfo": { "status": "success" },
  "heatmapData": {
    "points": [
      {
        "latitude": 11.5879,
        "longitude": 43.1456,
        "weight": 3
      }
    ],
    "totalCount": 156,
    "timeRange": {
      "from": "2024-01-01T00:00:00Z",
      "to": "2024-01-31T23:59:59Z"
    }
  },
  "hotspots": [
    {
      "id": "HS001",
      "center": { "latitude": 11.5879, "longitude": 43.1456 },
      "incidentCount": 12,
      "severity": "HIGH",
      "areaName": "Market District"
    }
  ]
}
```

## Dependencies

| Dependency | Purpose | Required |
|------------|---------|----------|
| MC-VIEW-01 | Base map functionality | Yes |
| MapLibre GL | Heatmap layer support | Yes |
| egov-location | Reverse geocoding for area names | Yes |
| IndexedDB | Offline heatmap caching | No |

## Related Stories

- [MC-VIEW-01](./MC-VIEW-01.md) - Map View with Incident Markers
- [MC-VIEW-03](./MC-VIEW-03.md) - Filter Incident Queue
- [MC-VIEW-04](./MC-VIEW-04.md) - Incident Details View

---

*Last Updated: 2024-01-15*
