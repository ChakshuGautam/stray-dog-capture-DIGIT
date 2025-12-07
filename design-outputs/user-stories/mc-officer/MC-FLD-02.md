# MC-FLD-02: Navigate to Incident Location

> **Back to:** [User Stories Master](../../design-outputs/02-user-stories.md)

## User Story

**As an** MC Officer,
**I want to** navigate to the incident location using my preferred maps app,
**So that** I can reach the location quickly and efficiently.

## Description

MC Officers need seamless navigation to incident locations. The system integrates with native device navigation apps (Google Maps, Apple Maps, Waze) to provide turn-by-turn directions. Officers can choose their preferred app, and the system remembers this preference.

## Acceptance Criteria

| # | Criterion | Priority |
|---|-----------|----------|
| 1 | One-tap navigation from incident details | Must |
| 2 | Support Google Maps, Apple Maps, and Waze | Must |
| 3 | Remember user's preferred navigation app | Should |
| 4 | Pass destination coordinates accurately | Must |
| 5 | Include destination name/landmark in navigation | Should |
| 6 | Work offline with cached coordinates | Should |
| 7 | Show estimated time/distance before launching | Should |
| 8 | Handle case when no maps app installed | Must |
| 9 | Deep link opens directly in navigation mode | Must |
| 10 | Option to view in-app map preview first | Should |

## UI/UX Design

### Navigation App Selection

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│               Choose Navigation App                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Navigate to:                                              │
│  Route de l'Aéroport, Sector 4                            │
│  📍 2.3 km away • ~8 min drive                            │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  🗺️  Google Maps                              ▶    │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  🍎  Apple Maps                               ▶    │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  🚗  Waze                                     ▶    │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ☐ Remember my choice                                      │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    [Cancel]                         │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Quick Navigate Button

```
┌─────────────────────────────────────────────────────────────┐
│                    Incident Details                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  📍 LOCATION                                               │
│  Route de l'Aéroport                                       │
│  Near Total Gas Station, Sector 4                          │
│                                                             │
│  ┌───────────────────────────────────────────────────┐     │
│  │                                                   │     │
│  │              [Mini Map Preview]                   │     │
│  │                    📍                             │     │
│  │                                                   │     │
│  └───────────────────────────────────────────────────┘     │
│                                                             │
│  Distance: 2.3 km • ETA: ~8 minutes                        │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │    🧭  Navigate with Google Maps                    │   │
│  │       [Change App]                                  │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### No Maps App Fallback

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│               ⚠️ No Navigation App Found                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  No supported navigation app was detected on your          │
│  device.                                                   │
│                                                             │
│  Destination coordinates:                                  │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  11.5879°N, 43.1456°E                      [Copy]  │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  You can:                                                  │
│  • Copy coordinates and paste in any maps app              │
│  • View the location on our in-app map                     │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │     [📋 Copy]          [🗺️ View In-App Map]         │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Install a navigation app:                                 │
│  [Get Google Maps] [Get Apple Maps] [Get Waze]             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Technical Implementation

### Navigation Service

```javascript
// services/NavigationService.js
class NavigationService {
  constructor() {
    this.preferredApp = this.loadPreferredApp();
  }

  /**
   * Supported navigation apps configuration
   */
  static NAVIGATION_APPS = {
    GOOGLE_MAPS: {
      id: 'google_maps',
      name: 'Google Maps',
      icon: '🗺️',
      // Universal link that works on both iOS and Android
      urlTemplate: 'https://www.google.com/maps/dir/?api=1&destination={lat},{lng}&travelmode=driving',
      // Native app schemes
      androidScheme: 'google.navigation:q={lat},{lng}',
      iosScheme: 'comgooglemaps://?daddr={lat},{lng}&directionsmode=driving',
      // Store links
      androidStore: 'https://play.google.com/store/apps/details?id=com.google.android.apps.maps',
      iosStore: 'https://apps.apple.com/app/google-maps/id585027354'
    },
    APPLE_MAPS: {
      id: 'apple_maps',
      name: 'Apple Maps',
      icon: '🍎',
      urlTemplate: 'https://maps.apple.com/?daddr={lat},{lng}&dirflg=d',
      iosScheme: 'maps://?daddr={lat},{lng}&dirflg=d',
      iosOnly: true
    },
    WAZE: {
      id: 'waze',
      name: 'Waze',
      icon: '🚗',
      urlTemplate: 'https://waze.com/ul?ll={lat},{lng}&navigate=yes',
      androidScheme: 'waze://?ll={lat},{lng}&navigate=yes',
      iosScheme: 'waze://?ll={lat},{lng}&navigate=yes',
      androidStore: 'https://play.google.com/store/apps/details?id=com.waze',
      iosStore: 'https://apps.apple.com/app/waze-navigation-live-traffic/id323229106'
    }
  };

  /**
   * Get available navigation apps for current platform
   */
  getAvailableApps() {
    const isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent);
    const apps = Object.values(NavigationService.NAVIGATION_APPS);

    return apps.filter(app => {
      if (app.iosOnly && !isIOS) return false;
      return true;
    });
  }

  /**
   * Navigate to coordinates with preferred or specified app
   * @param {number} lat - Latitude
   * @param {number} lng - Longitude
   * @param {Object} options - Navigation options
   */
  async navigate(lat, lng, options = {}) {
    const {
      appId = this.preferredApp,
      label = 'Incident Location'
    } = options;

    const app = NavigationService.NAVIGATION_APPS[appId] ||
                NavigationService.NAVIGATION_APPS.GOOGLE_MAPS;

    const url = this.buildNavigationUrl(app, lat, lng, label);

    // Try to open native app first, fall back to web URL
    const opened = await this.tryOpenNativeApp(app, lat, lng);

    if (!opened) {
      // Fall back to universal link
      window.location.href = url;
    }

    // Log navigation for analytics
    this.logNavigationEvent(app.id, lat, lng);
  }

  /**
   * Build navigation URL for specific app
   */
  buildNavigationUrl(app, lat, lng, label) {
    let url = app.urlTemplate
      .replace('{lat}', lat)
      .replace('{lng}', lng);

    // Add label if supported
    if (app.id === 'GOOGLE_MAPS' && label) {
      url += `&destination_place_id=${encodeURIComponent(label)}`;
    }

    return url;
  }

  /**
   * Try to open native app using scheme
   */
  async tryOpenNativeApp(app, lat, lng) {
    const isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent);
    const scheme = isIOS ? app.iosScheme : app.androidScheme;

    if (!scheme) return false;

    const url = scheme
      .replace('{lat}', lat)
      .replace('{lng}', lng);

    return new Promise((resolve) => {
      const timeout = setTimeout(() => {
        resolve(false);
      }, 1500);

      // Try to open native app
      window.location.href = url;

      // If we're still here after a short delay, app likely opened
      document.addEventListener('visibilitychange', function handler() {
        if (document.hidden) {
          clearTimeout(timeout);
          document.removeEventListener('visibilitychange', handler);
          resolve(true);
        }
      });
    });
  }

  /**
   * Set preferred navigation app
   */
  setPreferredApp(appId) {
    this.preferredApp = appId;
    localStorage.setItem('preferred_nav_app', appId);
  }

  /**
   * Load saved preferred app
   */
  loadPreferredApp() {
    return localStorage.getItem('preferred_nav_app') || 'GOOGLE_MAPS';
  }

  /**
   * Get preferred app info
   */
  getPreferredAppInfo() {
    return NavigationService.NAVIGATION_APPS[this.preferredApp];
  }

  /**
   * Calculate estimated time and distance
   * Uses Haversine formula for distance
   */
  calculateETA(fromLat, fromLng, toLat, toLng) {
    const R = 6371; // Earth's radius in km
    const dLat = this.toRad(toLat - fromLat);
    const dLng = this.toRad(toLng - fromLng);

    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(this.toRad(fromLat)) * Math.cos(this.toRad(toLat)) *
              Math.sin(dLng / 2) * Math.sin(dLng / 2);

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = R * c;

    // Rough ETA assuming 30 km/h average urban speed
    const avgSpeedKmh = 30;
    const timeMinutes = Math.round((distance / avgSpeedKmh) * 60);

    return {
      distanceKm: Math.round(distance * 10) / 10,
      distanceText: distance < 1
        ? `${Math.round(distance * 1000)} m`
        : `${Math.round(distance * 10) / 10} km`,
      timeMinutes,
      timeText: timeMinutes < 60
        ? `~${timeMinutes} min`
        : `~${Math.round(timeMinutes / 60)} hr ${timeMinutes % 60} min`
    };
  }

  toRad(deg) {
    return deg * (Math.PI / 180);
  }

  /**
   * Copy coordinates to clipboard
   */
  async copyCoordinates(lat, lng) {
    const text = `${lat}, ${lng}`;
    try {
      await navigator.clipboard.writeText(text);
      return true;
    } catch (error) {
      // Fallback for older browsers
      const textArea = document.createElement('textarea');
      textArea.value = text;
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
      return true;
    }
  }

  /**
   * Log navigation event for analytics
   */
  logNavigationEvent(appId, lat, lng) {
    // Analytics tracking
    console.log('Navigation started:', { appId, lat, lng });
  }
}

export default NavigationService;
```

### Navigate Button Component

```javascript
// components/NavigateButton.jsx
import React, { useState, useEffect } from 'react';
import NavigationService from '../services/NavigationService';
import { useGeolocation } from '../hooks/useGeolocation';

function NavigateButton({ destination, label, variant = 'primary' }) {
  const [showAppPicker, setShowAppPicker] = useState(false);
  const [eta, setEta] = useState(null);
  const { position } = useGeolocation();

  const navService = new NavigationService();
  const preferredApp = navService.getPreferredAppInfo();

  useEffect(() => {
    if (position && destination) {
      const calculated = navService.calculateETA(
        position.coords.latitude,
        position.coords.longitude,
        destination.latitude,
        destination.longitude
      );
      setEta(calculated);
    }
  }, [position, destination]);

  const handleNavigate = () => {
    if (preferredApp) {
      navService.navigate(
        destination.latitude,
        destination.longitude,
        { label }
      );
    } else {
      setShowAppPicker(true);
    }
  };

  const handleSelectApp = (appId, remember) => {
    if (remember) {
      navService.setPreferredApp(appId);
    }
    navService.navigate(
      destination.latitude,
      destination.longitude,
      { appId, label }
    );
    setShowAppPicker(false);
  };

  return (
    <>
      <button
        className={`btn-navigate btn-${variant}`}
        onClick={handleNavigate}
      >
        <span className="btn-icon">🧭</span>
        <span className="btn-text">
          Navigate{preferredApp ? ` with ${preferredApp.name}` : ''}
        </span>
        {eta && (
          <span className="btn-eta">{eta.distanceText} • {eta.timeText}</span>
        )}
      </button>

      {preferredApp && (
        <button
          className="btn-link change-app-link"
          onClick={() => setShowAppPicker(true)}
        >
          Change App
        </button>
      )}

      {showAppPicker && (
        <AppPickerDialog
          destination={destination}
          eta={eta}
          onSelect={handleSelectApp}
          onClose={() => setShowAppPicker(false)}
        />
      )}
    </>
  );
}

function AppPickerDialog({ destination, eta, onSelect, onClose }) {
  const [remember, setRemember] = useState(false);
  const navService = new NavigationService();
  const availableApps = navService.getAvailableApps();

  return (
    <div className="dialog-overlay" onClick={onClose}>
      <div className="dialog-content" onClick={e => e.stopPropagation()}>
        <h2>Choose Navigation App</h2>

        <div className="destination-info">
          <p>Navigate to:</p>
          <p className="destination-address">{destination.address}</p>
          {eta && (
            <p className="destination-eta">
              📍 {eta.distanceText} away • {eta.timeText} drive
            </p>
          )}
        </div>

        <div className="app-list">
          {availableApps.map(app => (
            <button
              key={app.id}
              className="app-option"
              onClick={() => onSelect(app.id, remember)}
            >
              <span className="app-icon">{app.icon}</span>
              <span className="app-name">{app.name}</span>
              <span className="app-arrow">▶</span>
            </button>
          ))}
        </div>

        <label className="remember-choice">
          <input
            type="checkbox"
            checked={remember}
            onChange={(e) => setRemember(e.target.checked)}
          />
          Remember my choice
        </label>

        <button className="btn-secondary" onClick={onClose}>
          Cancel
        </button>
      </div>
    </div>
  );
}

export default NavigateButton;
```

### In-App Map Preview Component

```javascript
// components/MapPreview.jsx
import React, { useEffect, useRef } from 'react';
import maplibregl from 'maplibre-gl';

function MapPreview({ destination, userLocation, onNavigate }) {
  const mapContainer = useRef(null);
  const map = useRef(null);

  useEffect(() => {
    if (map.current) return;

    // Calculate bounds to show both points
    const bounds = new maplibregl.LngLatBounds();
    bounds.extend([destination.longitude, destination.latitude]);

    if (userLocation) {
      bounds.extend([userLocation.longitude, userLocation.latitude]);
    }

    map.current = new maplibregl.Map({
      container: mapContainer.current,
      style: 'https://tiles.openfreemap.org/styles/liberty',
      bounds,
      fitBoundsOptions: { padding: 50 }
    });

    // Add destination marker
    new maplibregl.Marker({ color: '#f44336' })
      .setLngLat([destination.longitude, destination.latitude])
      .setPopup(new maplibregl.Popup().setText(destination.address || 'Destination'))
      .addTo(map.current);

    // Add user location marker if available
    if (userLocation) {
      new maplibregl.Marker({ color: '#2196f3' })
        .setLngLat([userLocation.longitude, userLocation.latitude])
        .setPopup(new maplibregl.Popup().setText('Your Location'))
        .addTo(map.current);

      // Draw line between points
      map.current.on('load', () => {
        map.current.addSource('route', {
          type: 'geojson',
          data: {
            type: 'Feature',
            geometry: {
              type: 'LineString',
              coordinates: [
                [userLocation.longitude, userLocation.latitude],
                [destination.longitude, destination.latitude]
              ]
            }
          }
        });

        map.current.addLayer({
          id: 'route',
          type: 'line',
          source: 'route',
          paint: {
            'line-color': '#2196f3',
            'line-width': 3,
            'line-dasharray': [2, 2]
          }
        });
      });
    }

    return () => {
      map.current?.remove();
      map.current = null;
    };
  }, [destination, userLocation]);

  return (
    <div className="map-preview-container">
      <div ref={mapContainer} className="map-preview" />
      <button
        className="btn-navigate-overlay"
        onClick={onNavigate}
      >
        🧭 Start Navigation
      </button>
    </div>
  );
}

export default MapPreview;
```

## PWA-Specific Considerations

### Offline Coordinates Cache

```javascript
// utils/coordinatesCache.js
import { openDB } from 'idb';

/**
 * Cache incident coordinates for offline navigation
 */
export async function cacheCoordinates(incidents) {
  const db = await openDB('sdcrs-mc', 1);

  const tx = db.transaction('coordinates', 'readwrite');
  const store = tx.objectStore('coordinates');

  for (const incident of incidents) {
    await store.put({
      id: incident.id,
      latitude: incident.latitude,
      longitude: incident.longitude,
      address: incident.address,
      landmark: incident.landmark,
      cachedAt: Date.now()
    });
  }
}

/**
 * Get cached coordinates
 */
export async function getCachedCoordinates(incidentId) {
  const db = await openDB('sdcrs-mc', 1);
  return await db.get('coordinates', incidentId);
}
```

## Dependencies

| Dependency | Purpose | Required |
|------------|---------|----------|
| MC-VIEW-04 | Incident details | Yes |
| MC-FLD-01 | Start response context | Yes |
| MapLibre GL | In-app map preview | No |
| Geolocation API | User location for ETA | No |

## Related Stories

- [MC-FLD-01](./MC-FLD-01.md) - Update Status to In Progress
- [MC-VIEW-01](./MC-VIEW-01.md) - Map View with Incident Markers
- [MC-CAP-01](./MC-CAP-01.md) - Mark Captured/Resolved

---

*Last Updated: 2024-01-15*
