# MC-VIEW-04: Incident Details View

> **Back to:** [User Stories Master](../../design-outputs/02-user-stories.md)

## User Story

**As an** MC Officer,
**I want to** view complete incident details including photos and location,
**So that** I can properly prepare before heading to the location.

## Description

The incident details view provides MC Officers with all relevant information about a stray dog sighting. This includes the photos submitted by the teacher, precise location data, behavioral notes, and the incident timeline. Officers use this information to assess the situation, plan their approach, and bring appropriate equipment.

**Important:** Teacher identity is hidden from MC Officers to maintain privacy. Only location and incident details are visible.

## Acceptance Criteria

| # | Criterion | Priority |
|---|-----------|----------|
| 1 | Display all photos submitted with the report | Must |
| 2 | Show swipeable photo gallery with zoom capability | Must |
| 3 | Display location on embedded mini-map | Must |
| 4 | Show address/landmark description | Must |
| 5 | Display behavioral notes (aggressive, injured, etc.) | Must |
| 6 | Show incident timeline with status changes | Should |
| 7 | Display time elapsed since report | Must |
| 8 | Hide teacher identity (no name, phone, or personal info) | Must |
| 9 | One-tap navigation to location | Must |
| 10 | Quick action buttons (Start, Navigate, UTL) | Must |
| 11 | Show dog count if multiple dogs reported | Should |
| 12 | Display previous incidents at same location | Should |

## UI/UX Design

### Incident Details - Main View

```
┌─────────────────────────────────────────────────────────────┐
│ ◀ Back           Incident Details            [⋮ More]      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │                   ┌─────────────┐                   │   │
│  │                   │             │                   │   │
│  │                   │   [Photo]   │                   │   │
│  │                   │    🐕        │                   │   │
│  │                   │             │                   │   │
│  │                   └─────────────┘                   │   │
│  │                                                     │   │
│  │                    ● ○ ○  (1/3)                     │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  INC-2026-0156                                      │   │
│  │  ─────────────────────────────────────────────      │   │
│  │  Status: 🔴 NEW           Reported: 2 hours ago    │   │
│  │                                                     │   │
│  │  📍 LOCATION                                        │   │
│  │  Route de l'Aéroport                               │   │
│  │  Near Total Gas Station, Sector 4                  │   │
│  │  Coordinates: 11.5879°N, 43.1456°E                 │   │
│  │                                                     │   │
│  │  ┌───────────────────────────────────────────┐     │   │
│  │  │         [Mini Map Preview]                │     │   │
│  │  │              📍                            │     │   │
│  │  │                                           │     │   │
│  │  └───────────────────────────────────────────┘     │   │
│  │  Distance: 2.3 km from you                         │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  🐕 DOG DETAILS                                     │   │
│  │  ─────────────────────────────────────────────      │   │
│  │  Number of dogs: 1                                 │   │
│  │                                                     │   │
│  │  ⚠️ BEHAVIOR ALERT                                  │   │
│  │  ┌─────────────────────────────────────────────┐   │   │
│  │  │ Aggressive behavior noted                   │   │   │
│  │  │ Dog was barking at passersby                │   │   │
│  │  └─────────────────────────────────────────────┘   │   │
│  │                                                     │   │
│  │  Additional Notes:                                 │   │
│  │  "Medium-sized brown dog, possibly injured        │   │
│  │   on right hind leg. Seen near garbage bins."     │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │       [🧭 Navigate]        [▶️ Start Response]      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Photo Gallery (Fullscreen)

```
┌─────────────────────────────────────────────────────────────┐
│ ✕ Close                                        1 / 3       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │                                                     │   │
│  │                                                     │   │
│  │                    ┌─────────┐                      │   │
│  │                    │         │                      │   │
│  │                    │  🐕     │                      │   │
│  │                    │  Photo  │                      │   │
│  │                    │         │                      │   │
│  │                    └─────────┘                      │   │
│  │                                                     │   │
│  │                                                     │   │
│  │                                                     │   │
│  │       ◀ Swipe                    Swipe ▶           │   │
│  │                                                     │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌───────────────────────────────────────────────────┐     │
│  │  [🔍 Zoom]    [📥 Save]    [↗️ Share]             │     │
│  └───────────────────────────────────────────────────┘     │
│                                                             │
│  Thumbnail Strip:                                          │
│  ┌────┐ ┌────┐ ┌────┐                                     │
│  │ 1  │ │ 2  │ │ 3  │                                     │
│  │[●] │ │    │ │    │                                     │
│  └────┘ └────┘ └────┘                                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Incident Timeline

```
┌─────────────────────────────────────────────────────────────┐
│ ◀ Back            Incident Timeline                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  INC-2026-0156                                             │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │  ●───────────────────────────────────────────────  │   │
│  │  │ REPORTED                                        │   │
│  │  │ Jan 15, 2026 at 10:30 AM                       │   │
│  │  │ Initial report submitted                        │   │
│  │  │                                                 │   │
│  │  ●───────────────────────────────────────────────  │   │
│  │  │ VERIFIED                                        │   │
│  │  │ Jan 15, 2026 at 10:45 AM                       │   │
│  │  │ Report verified and approved                    │   │
│  │  │                                                 │   │
│  │  ●───────────────────────────────────────────────  │   │
│  │  │ ROUTED                                          │   │
│  │  │ Jan 15, 2026 at 10:46 AM                       │   │
│  │  │ Assigned to your jurisdiction                   │   │
│  │  │                                                 │   │
│  │  ○───────────────────────────────────────────────  │   │
│  │    AWAITING RESPONSE                               │   │
│  │    Waiting for MC Officer action                   │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Time in current status: 1 hour 44 minutes                 │
│  Total time since report: 2 hours 0 minutes                │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Previous Incidents at Location

```
┌─────────────────────────────────────────────────────────────┐
│ ◀ Back          Location History                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  📍 Route de l'Aéroport, Sector 4                          │
│                                                             │
│  ⚠️ 3 previous incidents at this location                  │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  INC-2026-0089                      Dec 28, 2023    │   │
│  │  Status: CAPTURED ✓                                 │   │
│  │  Resolution: Dog captured and relocated             │   │
│  │  Response time: 4 hours                             │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  INC-2026-0045                      Dec 15, 2023    │   │
│  │  Status: UTL                                        │   │
│  │  Note: Dog not found after 2 hours search          │   │
│  │  Response time: 3 hours                             │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  INC-2026-0012                      Nov 30, 2023    │   │
│  │  Status: CAPTURED ✓                                 │   │
│  │  Resolution: 2 dogs captured                        │   │
│  │  Response time: 2 hours                             │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  📊 Location Statistics:                                   │
│  • Repeat incident location (hotspot)                      │
│  • Avg response time: 3 hours                              │
│  • Success rate: 67% (2/3 captured)                        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Technical Implementation

### Incident Details Service

```javascript
// services/IncidentDetailsService.js
class IncidentDetailsService {
  constructor(baseUrl) {
    this.baseUrl = baseUrl;
  }

  /**
   * Fetch complete incident details
   * Note: Teacher PII is excluded from response
   */
  async getIncidentDetails(incidentId) {
    const response = await fetch(
      `${this.baseUrl}/sdcrs/incidents/v1/${incidentId}`,
      {
        headers: {
          'Authorization': `Bearer ${this.getAuthToken()}`,
          'X-User-Role': 'MC_OFFICER' // Server filters PII based on role
        }
      }
    );

    if (!response.ok) {
      throw new Error(`Failed to fetch incident: ${response.status}`);
    }

    const data = await response.json();
    return this.transformIncidentForMC(data.incident);
  }

  /**
   * Transform incident data for MC Officer view
   * Ensures no teacher PII is exposed
   */
  transformIncidentForMC(incident) {
    return {
      id: incident.id,
      incidentNumber: incident.incidentNumber,
      status: incident.status,
      reportedDate: incident.reportedDate,
      verifiedDate: incident.verifiedDate,
      assignedDate: incident.assignedDate,

      // Location details
      location: {
        latitude: incident.latitude,
        longitude: incident.longitude,
        address: incident.address,
        landmark: incident.landmark,
        locality: incident.locality
      },

      // Dog details
      dogDetails: {
        count: incident.dogCount || 1,
        description: incident.dogDescription,
        behaviorFlags: incident.behaviorFlags || [],
        additionalNotes: incident.additionalNotes
      },

      // Photos (URLs only, no metadata with reporter info)
      photos: incident.photos?.map(p => ({
        id: p.id,
        url: p.url,
        thumbnailUrl: p.thumbnailUrl
      })) || [],

      // Timeline events
      timeline: incident.auditHistory?.map(event => ({
        action: event.action,
        timestamp: event.timestamp,
        description: this.getTimelineDescription(event)
      })) || [],

      // Jurisdiction info
      jurisdiction: {
        id: incident.jurisdictionId,
        name: incident.jurisdictionName
      },

      // Computed fields
      timeElapsed: this.calculateTimeElapsed(incident.reportedDate),
      priorityScore: this.calculatePriorityScore(incident)

      // Note: Reporter info is NOT included
    };
  }

  /**
   * Get human-readable timeline description
   */
  getTimelineDescription(event) {
    const descriptions = {
      CREATED: 'Initial report submitted',
      VERIFIED: 'Report verified and approved',
      ROUTED: 'Assigned to jurisdiction',
      ASSIGNED: 'Assigned to MC Officer',
      IN_PROGRESS: 'Response started',
      CAPTURED: 'Dog captured/resolved',
      UTL: 'Unable to locate'
    };
    return descriptions[event.action] || event.action;
  }

  /**
   * Calculate time elapsed since report
   */
  calculateTimeElapsed(reportedDate) {
    const reported = new Date(reportedDate);
    const now = new Date();
    const diffMs = now - reported;

    const hours = Math.floor(diffMs / (1000 * 60 * 60));
    const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));

    if (hours > 24) {
      const days = Math.floor(hours / 24);
      return `${days} day${days > 1 ? 's' : ''} ago`;
    }
    if (hours > 0) {
      return `${hours}h ${minutes}m ago`;
    }
    return `${minutes} minutes ago`;
  }

  /**
   * Calculate priority score based on factors
   */
  calculatePriorityScore(incident) {
    let score = 0;

    // Behavior flags increase priority
    if (incident.behaviorFlags?.includes('AGGRESSIVE')) score += 30;
    if (incident.behaviorFlags?.includes('INJURED')) score += 20;
    if (incident.behaviorFlags?.includes('NEAR_SCHOOL')) score += 25;
    if (incident.behaviorFlags?.includes('MULTIPLE_DOGS')) score += 15;

    // Time waiting increases priority
    const hoursWaiting = this.getHoursElapsed(incident.reportedDate);
    score += Math.min(hoursWaiting * 2, 20); // Max 20 points for time

    return score;
  }

  getHoursElapsed(dateString) {
    const date = new Date(dateString);
    return (new Date() - date) / (1000 * 60 * 60);
  }

  /**
   * Fetch previous incidents at same location
   */
  async getLocationHistory(latitude, longitude, radiusMeters = 100) {
    const params = new URLSearchParams({
      latitude,
      longitude,
      radiusMeters,
      limit: 10,
      excludeStatuses: 'NEW,ASSIGNED,IN_PROGRESS' // Only resolved
    });

    const response = await fetch(
      `${this.baseUrl}/sdcrs/incidents/v1/_nearby?${params}`,
      {
        headers: {
          'Authorization': `Bearer ${this.getAuthToken()}`
        }
      }
    );

    const data = await response.json();
    return {
      incidents: data.incidents || [],
      statistics: this.calculateLocationStats(data.incidents)
    };
  }

  /**
   * Calculate statistics for location
   */
  calculateLocationStats(incidents) {
    if (!incidents || incidents.length === 0) {
      return null;
    }

    const captured = incidents.filter(i => i.status === 'CAPTURED').length;
    const total = incidents.length;

    const responseTimes = incidents
      .filter(i => i.resolvedDate)
      .map(i => {
        const reported = new Date(i.reportedDate);
        const resolved = new Date(i.resolvedDate);
        return (resolved - reported) / (1000 * 60 * 60); // hours
      });

    const avgResponseTime = responseTimes.length > 0
      ? responseTimes.reduce((a, b) => a + b, 0) / responseTimes.length
      : null;

    return {
      totalIncidents: total,
      capturedCount: captured,
      successRate: Math.round((captured / total) * 100),
      avgResponseTimeHours: avgResponseTime ? avgResponseTime.toFixed(1) : null,
      isHotspot: total >= 3
    };
  }

  getAuthToken() {
    return localStorage.getItem('auth_token');
  }
}

export default IncidentDetailsService;
```

### Incident Details Component

```javascript
// components/IncidentDetailsView.jsx
import React, { useState, useEffect } from 'react';
import IncidentDetailsService from '../services/IncidentDetailsService';
import PhotoGallery from './PhotoGallery';
import MiniMap from './MiniMap';
import IncidentTimeline from './IncidentTimeline';

const BEHAVIOR_LABELS = {
  AGGRESSIVE: { label: 'Aggressive behavior', icon: '⚠️', severity: 'high' },
  INJURED: { label: 'Possibly injured', icon: '🩹', severity: 'medium' },
  NEAR_SCHOOL: { label: 'Near school/hospital', icon: '🏫', severity: 'medium' },
  MULTIPLE_DOGS: { label: 'Multiple dogs', icon: '🐕🐕', severity: 'medium' },
  FRIENDLY: { label: 'Appears friendly', icon: '🐕', severity: 'low' }
};

const STATUS_CONFIG = {
  NEW: { label: 'New', color: '#f44336', icon: '🔴' },
  ASSIGNED: { label: 'Assigned', color: '#ff9800', icon: '🟡' },
  IN_PROGRESS: { label: 'In Progress', color: '#4caf50', icon: '🟢' },
  CAPTURED: { label: 'Captured', color: '#9e9e9e', icon: '✅' },
  UTL: { label: 'Unable to Locate', color: '#607d8b', icon: '❌' }
};

function IncidentDetailsView({ incidentId, onNavigate, onStartResponse, onBack }) {
  const [incident, setIncident] = useState(null);
  const [locationHistory, setLocationHistory] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showGallery, setShowGallery] = useState(false);
  const [showTimeline, setShowTimeline] = useState(false);

  const service = new IncidentDetailsService('/api');

  useEffect(() => {
    loadIncidentDetails();
  }, [incidentId]);

  const loadIncidentDetails = async () => {
    try {
      setLoading(true);
      setError(null);

      const details = await service.getIncidentDetails(incidentId);
      setIncident(details);

      // Load location history in background
      const history = await service.getLocationHistory(
        details.location.latitude,
        details.location.longitude
      );
      setLocationHistory(history);

    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleNavigate = () => {
    const { latitude, longitude } = incident.location;
    onNavigate?.(latitude, longitude);
  };

  const handleStartResponse = () => {
    onStartResponse?.(incident.id);
  };

  if (loading) {
    return <LoadingSpinner message="Loading incident details..." />;
  }

  if (error) {
    return <ErrorView message={error} onRetry={loadIncidentDetails} />;
  }

  const statusConfig = STATUS_CONFIG[incident.status];

  return (
    <div className="incident-details-view">
      {/* Header */}
      <header className="details-header">
        <button className="back-btn" onClick={onBack}>◀ Back</button>
        <h1>Incident Details</h1>
        <button className="more-btn">⋮</button>
      </header>

      {/* Photo Gallery Preview */}
      {incident.photos.length > 0 && (
        <div
          className="photo-preview"
          onClick={() => setShowGallery(true)}
        >
          <img
            src={incident.photos[0].url}
            alt="Incident photo"
            className="main-photo"
          />
          <div className="photo-count">
            ● {'○'.repeat(incident.photos.length - 1)} ({incident.photos.length})
          </div>
        </div>
      )}

      {/* Incident Header */}
      <section className="incident-header-section">
        <h2 className="incident-number">{incident.incidentNumber}</h2>
        <div className="status-row">
          <span
            className="status-badge"
            style={{ backgroundColor: statusConfig.color }}
          >
            {statusConfig.icon} {statusConfig.label}
          </span>
          <span className="time-elapsed">
            Reported: {incident.timeElapsed}
          </span>
        </div>
      </section>

      {/* Location Section */}
      <section className="location-section">
        <h3>📍 Location</h3>
        <p className="address">{incident.location.address}</p>
        {incident.location.landmark && (
          <p className="landmark">Near {incident.location.landmark}</p>
        )}
        <p className="coordinates">
          {incident.location.latitude.toFixed(4)}°N,
          {incident.location.longitude.toFixed(4)}°E
        </p>

        <MiniMap
          latitude={incident.location.latitude}
          longitude={incident.location.longitude}
          onClick={handleNavigate}
        />

        <p className="distance">
          Distance: {calculateDistance(incident.location)} from you
        </p>
      </section>

      {/* Dog Details Section */}
      <section className="dog-details-section">
        <h3>🐕 Dog Details</h3>
        <p>Number of dogs: {incident.dogDetails.count}</p>

        {/* Behavior Alerts */}
        {incident.dogDetails.behaviorFlags.length > 0 && (
          <div className="behavior-alerts">
            <h4>⚠️ Behavior Alert</h4>
            {incident.dogDetails.behaviorFlags.map(flag => {
              const config = BEHAVIOR_LABELS[flag];
              return (
                <div
                  key={flag}
                  className={`behavior-tag severity-${config.severity}`}
                >
                  {config.icon} {config.label}
                </div>
              );
            })}
          </div>
        )}

        {/* Additional Notes */}
        {incident.dogDetails.additionalNotes && (
          <div className="additional-notes">
            <h4>Additional Notes:</h4>
            <p>"{incident.dogDetails.additionalNotes}"</p>
          </div>
        )}
      </section>

      {/* Location History (if hotspot) */}
      {locationHistory?.statistics?.isHotspot && (
        <section className="location-history-section">
          <div className="hotspot-warning">
            <span className="warning-icon">⚠️</span>
            <span>
              {locationHistory.statistics.totalIncidents} previous incidents at this location
            </span>
            <button onClick={() => setShowTimeline(true)}>
              View History →
            </button>
          </div>
        </section>
      )}

      {/* Action Buttons */}
      <div className="action-buttons">
        <button
          className="btn-secondary btn-navigate"
          onClick={handleNavigate}
        >
          🧭 Navigate
        </button>
        <button
          className="btn-primary btn-start"
          onClick={handleStartResponse}
          disabled={incident.status === 'IN_PROGRESS'}
        >
          ▶️ Start Response
        </button>
      </div>

      {/* Photo Gallery Modal */}
      {showGallery && (
        <PhotoGallery
          photos={incident.photos}
          onClose={() => setShowGallery(false)}
        />
      )}

      {/* Timeline Modal */}
      {showTimeline && (
        <IncidentTimeline
          timeline={incident.timeline}
          locationHistory={locationHistory}
          onClose={() => setShowTimeline(false)}
        />
      )}
    </div>
  );
}

export default IncidentDetailsView;
```

### Photo Gallery Component

```javascript
// components/PhotoGallery.jsx
import React, { useState, useRef } from 'react';
import { TransformWrapper, TransformComponent } from 'react-zoom-pan-pinch';

function PhotoGallery({ photos, onClose }) {
  const [currentIndex, setCurrentIndex] = useState(0);
  const touchStartX = useRef(0);

  const handleTouchStart = (e) => {
    touchStartX.current = e.touches[0].clientX;
  };

  const handleTouchEnd = (e) => {
    const touchEndX = e.changedTouches[0].clientX;
    const diff = touchStartX.current - touchEndX;

    if (Math.abs(diff) > 50) { // Minimum swipe distance
      if (diff > 0 && currentIndex < photos.length - 1) {
        setCurrentIndex(currentIndex + 1);
      } else if (diff < 0 && currentIndex > 0) {
        setCurrentIndex(currentIndex - 1);
      }
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'ArrowLeft' && currentIndex > 0) {
      setCurrentIndex(currentIndex - 1);
    } else if (e.key === 'ArrowRight' && currentIndex < photos.length - 1) {
      setCurrentIndex(currentIndex + 1);
    } else if (e.key === 'Escape') {
      onClose();
    }
  };

  const handleSave = async () => {
    try {
      const response = await fetch(photos[currentIndex].url);
      const blob = await response.blob();
      const url = URL.createObjectURL(blob);

      const a = document.createElement('a');
      a.href = url;
      a.download = `incident-photo-${currentIndex + 1}.jpg`;
      a.click();

      URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Failed to save photo:', error);
    }
  };

  return (
    <div
      className="photo-gallery-modal"
      onKeyDown={handleKeyDown}
      tabIndex={0}
    >
      <header className="gallery-header">
        <button className="close-btn" onClick={onClose}>✕ Close</button>
        <span className="photo-counter">{currentIndex + 1} / {photos.length}</span>
      </header>

      <div
        className="gallery-main"
        onTouchStart={handleTouchStart}
        onTouchEnd={handleTouchEnd}
      >
        <TransformWrapper>
          <TransformComponent>
            <img
              src={photos[currentIndex].url}
              alt={`Photo ${currentIndex + 1}`}
              className="gallery-image"
            />
          </TransformComponent>
        </TransformWrapper>
      </div>

      <div className="gallery-actions">
        <button className="action-btn" title="Zoom">
          🔍 Zoom
        </button>
        <button className="action-btn" onClick={handleSave} title="Save">
          📥 Save
        </button>
      </div>

      <div className="thumbnail-strip">
        {photos.map((photo, index) => (
          <button
            key={photo.id}
            className={`thumbnail ${index === currentIndex ? 'active' : ''}`}
            onClick={() => setCurrentIndex(index)}
          >
            <img src={photo.thumbnailUrl} alt={`Thumbnail ${index + 1}`} />
          </button>
        ))}
      </div>
    </div>
  );
}

export default PhotoGallery;
```

### Mini Map Component

```javascript
// components/MiniMap.jsx
import React, { useEffect, useRef } from 'react';
import maplibregl from 'maplibre-gl';

function MiniMap({ latitude, longitude, onClick }) {
  const mapContainer = useRef(null);
  const map = useRef(null);

  useEffect(() => {
    if (map.current) return;

    map.current = new maplibregl.Map({
      container: mapContainer.current,
      style: 'https://tiles.openfreemap.org/styles/liberty',
      center: [longitude, latitude],
      zoom: 15,
      interactive: false // Static map for preview
    });

    // Add marker
    new maplibregl.Marker({ color: '#f44336' })
      .setLngLat([longitude, latitude])
      .addTo(map.current);

    return () => {
      map.current?.remove();
      map.current = null;
    };
  }, [latitude, longitude]);

  return (
    <div
      className="mini-map-container"
      onClick={onClick}
      role="button"
      aria-label="Tap to navigate"
    >
      <div ref={mapContainer} className="mini-map" />
      <div className="map-overlay">
        <span>Tap to navigate</span>
      </div>
    </div>
  );
}

export default MiniMap;
```

## PWA-Specific Considerations

### Offline Incident Caching

```javascript
// utils/incidentCache.js
import { openDB } from 'idb';

const DB_NAME = 'sdcrs-mc';
const STORE_NAME = 'incident-details';

/**
 * Cache incident details for offline access
 */
export async function cacheIncidentDetails(incident) {
  const db = await openDB(DB_NAME, 1, {
    upgrade(db) {
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        db.createObjectStore(STORE_NAME, { keyPath: 'id' });
      }
    }
  });

  await db.put(STORE_NAME, {
    ...incident,
    cachedAt: Date.now()
  });
}

/**
 * Get cached incident details
 */
export async function getCachedIncident(incidentId) {
  const db = await openDB(DB_NAME, 1);
  return await db.get(STORE_NAME, incidentId);
}

/**
 * Cache photos for offline viewing
 */
export async function cachePhotos(photos) {
  const cache = await caches.open('incident-photos');

  await Promise.all(
    photos.map(async (photo) => {
      try {
        const response = await fetch(photo.url);
        await cache.put(photo.url, response);
      } catch (error) {
        console.warn(`Failed to cache photo: ${photo.id}`);
      }
    })
  );
}
```

## API Endpoints

### GET /sdcrs/incidents/v1/{incidentId}

Get incident details with role-based PII filtering.

**Response for MC_OFFICER role:**
```json
{
  "incident": {
    "id": "INC-2026-0156",
    "incidentNumber": "INC-2026-0156",
    "status": "NEW",
    "reportedDate": "2026-01-15T10:30:00Z",
    "latitude": 11.5879,
    "longitude": 43.1456,
    "address": "Route de l'Aéroport",
    "landmark": "Near Total Gas Station",
    "locality": "Sector 4",
    "dogCount": 1,
    "dogDescription": "Medium-sized brown dog",
    "behaviorFlags": ["AGGRESSIVE"],
    "additionalNotes": "Possibly injured on right hind leg",
    "photos": [
      {
        "id": "PHT001",
        "url": "/photos/PHT001.jpg",
        "thumbnailUrl": "/photos/PHT001_thumb.jpg"
      }
    ],
    "auditHistory": [
      { "action": "CREATED", "timestamp": "2026-01-15T10:30:00Z" },
      { "action": "VERIFIED", "timestamp": "2026-01-15T10:45:00Z" }
    ]
  }
}
```

**Note:** `reporterName`, `reporterPhone`, and other teacher PII fields are excluded.

## Dependencies

| Dependency | Purpose | Required |
|------------|---------|----------|
| MC-VIEW-01 | Map integration | Yes |
| react-zoom-pan-pinch | Photo zoom/pan | Yes |
| maplibre-gl | Mini map display | Yes |
| idb | Offline caching | No |

## Related Stories

- [MC-VIEW-01](./MC-VIEW-01.md) - Map View with Incident Markers
- [MC-VIEW-05](./MC-VIEW-05.md) - Teacher Privacy Protection
- [MC-FLD-01](./MC-FLD-01.md) - Update Status to In Progress
- [MC-FLD-02](./MC-FLD-02.md) - Navigate to Incident Location

---

*Last Updated: 2026-01-15*
