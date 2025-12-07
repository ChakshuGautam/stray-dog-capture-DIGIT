# MC-VIEW-03: Filter Incident Queue

> **Back to:** [User Stories Master](../../design-outputs/02-user-stories.md)

## User Story

**As an** MC Officer,
**I want to** filter my incident queue by status, date, and priority,
**So that** I can focus on the most urgent cases first.

## Description

MC Officers need to efficiently manage their workload by filtering and sorting incidents. The filter system allows officers to view incidents based on status (new, assigned, in progress), time since report, and urgency indicators. This helps prioritize response efforts and ensures timely action on critical cases.

## Acceptance Criteria

| # | Criterion | Priority |
|---|-----------|----------|
| 1 | Filter by incident status (New, Assigned, In Progress, All) | Must |
| 2 | Filter by date range (Today, Last 7 days, Custom) | Must |
| 3 | Sort by oldest first (FIFO) or newest first | Must |
| 4 | Sort by distance from current location | Should |
| 5 | Filter count badge shows total matching incidents | Must |
| 6 | Active filters displayed as removable chips | Must |
| 7 | Save preferred filter as default | Should |
| 8 | Quick filter presets (Urgent, My Assigned, Nearby) | Should |
| 9 | Clear all filters with single action | Must |
| 10 | Filter state persists across sessions | Should |

## UI/UX Design

### List View with Filter Bar

```
┌─────────────────────────────────────────────────────────────┐
│ ☰ Menu        My Incidents (23)           [🗺️] [📋✓]      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ [🔍 Search location...]        [⚙️ Filter] [↕️ Sort] │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Active Filters:                                           │
│  ┌────────────┐ ┌──────────────┐ ┌──────────────────┐     │
│  │ New ✕      │ │ Assigned ✕   │ │ Last 7 days ✕    │     │
│  └────────────┘ └──────────────┘ └──────────────────┘     │
│  [Clear All]                                               │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 🔴 INC-2024-0156                         2.3 km ↗   │   │
│  │ Route de l'Aéroport, Sector 4                       │   │
│  │ Status: NEW • Reported: 2 hours ago                 │   │
│  │ ⚠️ Aggressive behavior noted                        │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 🟡 INC-2024-0155                         0.8 km ↗   │   │
│  │ Place du 27 Juin, Downtown                          │   │
│  │ Status: ASSIGNED • Reported: 5 hours ago            │   │
│  │ Assigned to you at 10:30 AM                         │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 🟢 INC-2024-0152                         3.1 km ↗   │   │
│  │ Boulevard de Gaulle, North District                 │   │
│  │ Status: IN_PROGRESS • Started: 1 hour ago           │   │
│  │ You are currently responding                        │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ... (20 more)                                             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Filter Panel (Bottom Sheet)

```
┌─────────────────────────────────────────────────────────────┐
│ ═══════════════════════════                                 │
│                                                             │
│               Filter Incidents                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  QUICK FILTERS                                             │
│  ─────────────────────────────────────────────             │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐             │
│  │ ⚡ Urgent  │ │ 👤 Mine    │ │ 📍 Nearby  │             │
│  │  (5)       │ │  (8)       │ │  (12)      │             │
│  └────────────┘ └────────────┘ └────────────┘             │
│                                                             │
│  STATUS                                                    │
│  ─────────────────────────────────────────────             │
│  ☑ New (unassigned)                          12           │
│  ☑ Assigned to me                             8           │
│  ☐ In Progress                                3           │
│  ☐ Captured (resolved)                       45           │
│  ☐ Unable to Locate                          15           │
│                                                             │
│  DATE RANGE                                                │
│  ─────────────────────────────────────────────             │
│  ○ Today                                                   │
│  ○ Last 7 days                                             │
│  ● Last 30 days                                            │
│  ○ Custom range...                                         │
│                                                             │
│  PRIORITY                                                  │
│  ─────────────────────────────────────────────             │
│  ☐ Aggressive behavior reported                            │
│  ☐ Near school/hospital                                    │
│  ☐ Multiple dogs                                           │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │    [Reset]              [Apply Filters (23)]        │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Sort Options Panel

```
┌─────────────────────────────────────────────────────────────┐
│ ═══════════════════════════                                 │
│                                                             │
│                  Sort By                                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ● Oldest First (FIFO)                        ✓     │   │
│  │    Prioritize incidents waiting longest             │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ○ Newest First                                     │   │
│  │    Show most recent reports first                   │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ○ Nearest First                                    │   │
│  │    Based on your current location                   │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ○ Priority Score                                   │   │
│  │    Aggressive, schools, multiple dogs               │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ☑ Save as my default sort                                │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Custom Date Range Picker

```
┌─────────────────────────────────────────────────────────────┐
│ ◀ Back          Custom Date Range                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  From Date                                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 📅  January 1, 2024                                 │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  To Date                                                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 📅  January 15, 2024                                │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Quick Select:                                             │
│  [Today] [Yesterday] [This Week] [This Month]              │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              [ Apply Date Range ]                   │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Technical Implementation

### Filter State Management

```javascript
// store/filterStore.js
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const DEFAULT_FILTERS = {
  statuses: ['NEW', 'ASSIGNED'],
  dateRange: 'LAST_7_DAYS',
  customDateFrom: null,
  customDateTo: null,
  priorityFlags: [],
  maxDistance: null // in km
};

const DEFAULT_SORT = {
  field: 'reportedDate',
  direction: 'asc' // FIFO - oldest first
};

export const useFilterStore = create(
  persist(
    (set, get) => ({
      filters: { ...DEFAULT_FILTERS },
      sort: { ...DEFAULT_SORT },
      savedDefaults: null,

      // Filter actions
      setStatusFilter: (statuses) => set((state) => ({
        filters: { ...state.filters, statuses }
      })),

      setDateRange: (dateRange) => set((state) => ({
        filters: {
          ...state.filters,
          dateRange,
          customDateFrom: null,
          customDateTo: null
        }
      })),

      setCustomDateRange: (from, to) => set((state) => ({
        filters: {
          ...state.filters,
          dateRange: 'CUSTOM',
          customDateFrom: from,
          customDateTo: to
        }
      })),

      setPriorityFlags: (flags) => set((state) => ({
        filters: { ...state.filters, priorityFlags: flags }
      })),

      setMaxDistance: (distance) => set((state) => ({
        filters: { ...state.filters, maxDistance: distance }
      })),

      // Sort actions
      setSort: (field, direction) => set({ sort: { field, direction } }),

      // Quick filter presets
      applyQuickFilter: (preset) => {
        const presets = {
          URGENT: {
            statuses: ['NEW', 'ASSIGNED'],
            priorityFlags: ['AGGRESSIVE', 'NEAR_SCHOOL', 'MULTIPLE_DOGS'],
            dateRange: 'TODAY'
          },
          MY_ASSIGNED: {
            statuses: ['ASSIGNED', 'IN_PROGRESS'],
            dateRange: 'LAST_30_DAYS',
            priorityFlags: []
          },
          NEARBY: {
            statuses: ['NEW', 'ASSIGNED', 'IN_PROGRESS'],
            maxDistance: 5, // 5 km radius
            dateRange: 'LAST_7_DAYS'
          }
        };
        set({ filters: { ...DEFAULT_FILTERS, ...presets[preset] } });
      },

      // Reset and save
      resetFilters: () => set({ filters: { ...DEFAULT_FILTERS } }),
      resetSort: () => set({ sort: { ...DEFAULT_SORT } }),
      resetAll: () => set({
        filters: { ...DEFAULT_FILTERS },
        sort: { ...DEFAULT_SORT }
      }),

      saveAsDefault: () => set((state) => ({
        savedDefaults: {
          filters: { ...state.filters },
          sort: { ...state.sort }
        }
      })),

      loadDefaults: () => {
        const { savedDefaults } = get();
        if (savedDefaults) {
          set({
            filters: { ...savedDefaults.filters },
            sort: { ...savedDefaults.sort }
          });
        }
      },

      // Computed values
      getActiveFilterCount: () => {
        const { filters } = get();
        let count = 0;
        if (filters.statuses.length > 0 && filters.statuses.length < 5) count++;
        if (filters.dateRange !== 'LAST_30_DAYS') count++;
        if (filters.priorityFlags.length > 0) count++;
        if (filters.maxDistance) count++;
        return count;
      }
    }),
    {
      name: 'mc-incident-filters',
      partialize: (state) => ({
        savedDefaults: state.savedDefaults,
        sort: state.sort
      })
    }
  )
);
```

### Incident Filter Service

```javascript
// services/IncidentFilterService.js
class IncidentFilterService {
  constructor(baseUrl) {
    this.baseUrl = baseUrl;
  }

  /**
   * Build query parameters from filter state
   */
  buildQueryParams(filters, sort, pagination, userLocation) {
    const params = new URLSearchParams();

    // Status filter
    if (filters.statuses && filters.statuses.length > 0) {
      params.append('statuses', filters.statuses.join(','));
    }

    // Date range
    const dateRange = this.resolveDateRange(filters);
    if (dateRange.from) {
      params.append('fromDate', dateRange.from.toISOString());
    }
    if (dateRange.to) {
      params.append('toDate', dateRange.to.toISOString());
    }

    // Priority flags
    if (filters.priorityFlags && filters.priorityFlags.length > 0) {
      params.append('priorityFlags', filters.priorityFlags.join(','));
    }

    // Distance filter (requires user location)
    if (filters.maxDistance && userLocation) {
      params.append('latitude', userLocation.latitude);
      params.append('longitude', userLocation.longitude);
      params.append('radiusKm', filters.maxDistance);
    }

    // Sorting
    params.append('sortBy', sort.field);
    params.append('sortOrder', sort.direction);

    // Pagination
    params.append('offset', pagination.offset || 0);
    params.append('limit', pagination.limit || 20);

    return params;
  }

  /**
   * Resolve date range to actual dates
   */
  resolveDateRange(filters) {
    const now = new Date();
    const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate());

    switch (filters.dateRange) {
      case 'TODAY':
        return { from: startOfToday, to: now };

      case 'LAST_7_DAYS':
        const last7 = new Date(startOfToday);
        last7.setDate(last7.getDate() - 7);
        return { from: last7, to: now };

      case 'LAST_30_DAYS':
        const last30 = new Date(startOfToday);
        last30.setDate(last30.getDate() - 30);
        return { from: last30, to: now };

      case 'CUSTOM':
        return {
          from: filters.customDateFrom ? new Date(filters.customDateFrom) : null,
          to: filters.customDateTo ? new Date(filters.customDateTo) : null
        };

      default:
        return { from: null, to: null };
    }
  }

  /**
   * Fetch filtered incidents
   */
  async fetchIncidents(filters, sort, pagination, jurisdictionId, userLocation) {
    const params = this.buildQueryParams(filters, sort, pagination, userLocation);
    params.append('jurisdictionId', jurisdictionId);

    const response = await fetch(
      `${this.baseUrl}/sdcrs/incidents/v1/_search?${params}`,
      {
        headers: {
          'Authorization': `Bearer ${this.getAuthToken()}`,
          'Content-Type': 'application/json'
        }
      }
    );

    if (!response.ok) {
      throw new Error(`Failed to fetch incidents: ${response.status}`);
    }

    const data = await response.json();

    return {
      incidents: data.incidents || [],
      total: data.totalCount || 0,
      facets: data.facets || {}
    };
  }

  /**
   * Get filter facets (counts per status, date, etc.)
   */
  async fetchFilterFacets(jurisdictionId, userLocation) {
    const params = new URLSearchParams({ jurisdictionId });

    if (userLocation) {
      params.append('latitude', userLocation.latitude);
      params.append('longitude', userLocation.longitude);
    }

    const response = await fetch(
      `${this.baseUrl}/sdcrs/incidents/v1/_facets?${params}`,
      {
        headers: {
          'Authorization': `Bearer ${this.getAuthToken()}`
        }
      }
    );

    const data = await response.json();
    return data.facets;
  }

  getAuthToken() {
    return localStorage.getItem('auth_token');
  }
}

export default IncidentFilterService;
```

### Filter Panel Component

```javascript
// components/FilterPanel.jsx
import React, { useState, useEffect } from 'react';
import { useFilterStore } from '../store/filterStore';

const STATUS_OPTIONS = [
  { value: 'NEW', label: 'New (unassigned)', color: '#f44336' },
  { value: 'ASSIGNED', label: 'Assigned to me', color: '#ff9800' },
  { value: 'IN_PROGRESS', label: 'In Progress', color: '#4caf50' },
  { value: 'CAPTURED', label: 'Captured (resolved)', color: '#9e9e9e' },
  { value: 'UTL', label: 'Unable to Locate', color: '#607d8b' }
];

const DATE_RANGE_OPTIONS = [
  { value: 'TODAY', label: 'Today' },
  { value: 'LAST_7_DAYS', label: 'Last 7 days' },
  { value: 'LAST_30_DAYS', label: 'Last 30 days' },
  { value: 'CUSTOM', label: 'Custom range...' }
];

const PRIORITY_OPTIONS = [
  { value: 'AGGRESSIVE', label: 'Aggressive behavior reported' },
  { value: 'NEAR_SCHOOL', label: 'Near school/hospital' },
  { value: 'MULTIPLE_DOGS', label: 'Multiple dogs' }
];

function FilterPanel({ isOpen, onClose, facets, onApply }) {
  const {
    filters,
    setStatusFilter,
    setDateRange,
    setPriorityFlags,
    applyQuickFilter,
    resetFilters
  } = useFilterStore();

  const [localFilters, setLocalFilters] = useState(filters);
  const [showDatePicker, setShowDatePicker] = useState(false);

  useEffect(() => {
    setLocalFilters(filters);
  }, [filters, isOpen]);

  const handleStatusChange = (status) => {
    const current = localFilters.statuses;
    const updated = current.includes(status)
      ? current.filter(s => s !== status)
      : [...current, status];
    setLocalFilters({ ...localFilters, statuses: updated });
  };

  const handleDateRangeChange = (range) => {
    if (range === 'CUSTOM') {
      setShowDatePicker(true);
    }
    setLocalFilters({ ...localFilters, dateRange: range });
  };

  const handlePriorityChange = (flag) => {
    const current = localFilters.priorityFlags;
    const updated = current.includes(flag)
      ? current.filter(f => f !== flag)
      : [...current, flag];
    setLocalFilters({ ...localFilters, priorityFlags: updated });
  };

  const handleApply = () => {
    setStatusFilter(localFilters.statuses);
    setDateRange(localFilters.dateRange);
    setPriorityFlags(localFilters.priorityFlags);
    onApply(localFilters);
    onClose();
  };

  const handleReset = () => {
    resetFilters();
    setLocalFilters({ ...filters });
  };

  const handleQuickFilter = (preset) => {
    applyQuickFilter(preset);
    onClose();
  };

  const getMatchCount = () => {
    // Calculate based on facets
    return localFilters.statuses.reduce((sum, status) => {
      return sum + (facets?.statusCounts?.[status] || 0);
    }, 0);
  };

  if (!isOpen) return null;

  return (
    <div className="filter-panel-overlay" onClick={onClose}>
      <div className="filter-panel" onClick={e => e.stopPropagation()}>
        <div className="filter-panel-header">
          <div className="drag-handle" />
          <h2>Filter Incidents</h2>
        </div>

        <div className="filter-panel-content">
          {/* Quick Filters */}
          <section className="filter-section">
            <h3>Quick Filters</h3>
            <div className="quick-filter-buttons">
              <button
                className="quick-filter-btn"
                onClick={() => handleQuickFilter('URGENT')}
              >
                <span className="icon">⚡</span>
                <span className="label">Urgent</span>
                <span className="count">({facets?.urgentCount || 0})</span>
              </button>
              <button
                className="quick-filter-btn"
                onClick={() => handleQuickFilter('MY_ASSIGNED')}
              >
                <span className="icon">👤</span>
                <span className="label">Mine</span>
                <span className="count">({facets?.assignedCount || 0})</span>
              </button>
              <button
                className="quick-filter-btn"
                onClick={() => handleQuickFilter('NEARBY')}
              >
                <span className="icon">📍</span>
                <span className="label">Nearby</span>
                <span className="count">({facets?.nearbyCount || 0})</span>
              </button>
            </div>
          </section>

          {/* Status Filter */}
          <section className="filter-section">
            <h3>Status</h3>
            {STATUS_OPTIONS.map(option => (
              <label key={option.value} className="checkbox-option">
                <input
                  type="checkbox"
                  checked={localFilters.statuses.includes(option.value)}
                  onChange={() => handleStatusChange(option.value)}
                />
                <span
                  className="status-dot"
                  style={{ backgroundColor: option.color }}
                />
                <span className="label">{option.label}</span>
                <span className="count">
                  {facets?.statusCounts?.[option.value] || 0}
                </span>
              </label>
            ))}
          </section>

          {/* Date Range */}
          <section className="filter-section">
            <h3>Date Range</h3>
            {DATE_RANGE_OPTIONS.map(option => (
              <label key={option.value} className="radio-option">
                <input
                  type="radio"
                  name="dateRange"
                  checked={localFilters.dateRange === option.value}
                  onChange={() => handleDateRangeChange(option.value)}
                />
                <span className="label">{option.label}</span>
              </label>
            ))}
          </section>

          {/* Priority Flags */}
          <section className="filter-section">
            <h3>Priority</h3>
            {PRIORITY_OPTIONS.map(option => (
              <label key={option.value} className="checkbox-option">
                <input
                  type="checkbox"
                  checked={localFilters.priorityFlags.includes(option.value)}
                  onChange={() => handlePriorityChange(option.value)}
                />
                <span className="label">{option.label}</span>
              </label>
            ))}
          </section>
        </div>

        <div className="filter-panel-footer">
          <button className="btn-secondary" onClick={handleReset}>
            Reset
          </button>
          <button className="btn-primary" onClick={handleApply}>
            Apply Filters ({getMatchCount()})
          </button>
        </div>
      </div>
    </div>
  );
}

export default FilterPanel;
```

### Active Filter Chips Component

```javascript
// components/ActiveFilterChips.jsx
import React from 'react';
import { useFilterStore } from '../store/filterStore';

const STATUS_LABELS = {
  NEW: 'New',
  ASSIGNED: 'Assigned',
  IN_PROGRESS: 'In Progress',
  CAPTURED: 'Captured',
  UTL: 'UTL'
};

const DATE_LABELS = {
  TODAY: 'Today',
  LAST_7_DAYS: 'Last 7 days',
  LAST_30_DAYS: 'Last 30 days',
  CUSTOM: 'Custom dates'
};

function ActiveFilterChips() {
  const {
    filters,
    setStatusFilter,
    setDateRange,
    setPriorityFlags,
    resetAll
  } = useFilterStore();

  const chips = [];

  // Status chips
  filters.statuses.forEach(status => {
    chips.push({
      id: `status-${status}`,
      label: STATUS_LABELS[status],
      type: 'status',
      value: status
    });
  });

  // Date range chip (only if not default)
  if (filters.dateRange !== 'LAST_30_DAYS') {
    chips.push({
      id: 'date-range',
      label: DATE_LABELS[filters.dateRange],
      type: 'date',
      value: filters.dateRange
    });
  }

  // Priority flag chips
  filters.priorityFlags.forEach(flag => {
    chips.push({
      id: `priority-${flag}`,
      label: flag.replace(/_/g, ' ').toLowerCase(),
      type: 'priority',
      value: flag
    });
  });

  const removeChip = (chip) => {
    switch (chip.type) {
      case 'status':
        setStatusFilter(filters.statuses.filter(s => s !== chip.value));
        break;
      case 'date':
        setDateRange('LAST_30_DAYS'); // Reset to default
        break;
      case 'priority':
        setPriorityFlags(filters.priorityFlags.filter(f => f !== chip.value));
        break;
    }
  };

  if (chips.length === 0) return null;

  return (
    <div className="active-filter-chips">
      <span className="chips-label">Active Filters:</span>
      <div className="chips-container">
        {chips.map(chip => (
          <span key={chip.id} className={`filter-chip chip-${chip.type}`}>
            {chip.label}
            <button
              className="chip-remove"
              onClick={() => removeChip(chip)}
              aria-label={`Remove ${chip.label} filter`}
            >
              ✕
            </button>
          </span>
        ))}
      </div>
      <button className="clear-all-btn" onClick={resetAll}>
        Clear All
      </button>
    </div>
  );
}

export default ActiveFilterChips;
```

### Sort Panel Component

```javascript
// components/SortPanel.jsx
import React from 'react';
import { useFilterStore } from '../store/filterStore';

const SORT_OPTIONS = [
  {
    field: 'reportedDate',
    direction: 'asc',
    label: 'Oldest First (FIFO)',
    description: 'Prioritize incidents waiting longest'
  },
  {
    field: 'reportedDate',
    direction: 'desc',
    label: 'Newest First',
    description: 'Show most recent reports first'
  },
  {
    field: 'distance',
    direction: 'asc',
    label: 'Nearest First',
    description: 'Based on your current location'
  },
  {
    field: 'priorityScore',
    direction: 'desc',
    label: 'Priority Score',
    description: 'Aggressive, schools, multiple dogs'
  }
];

function SortPanel({ isOpen, onClose }) {
  const { sort, setSort, saveAsDefault } = useFilterStore();
  const [saveDefault, setSaveDefault] = React.useState(false);

  const handleSortSelect = (option) => {
    setSort(option.field, option.direction);
    if (saveDefault) {
      saveAsDefault();
    }
    onClose();
  };

  const isSelected = (option) => {
    return sort.field === option.field && sort.direction === option.direction;
  };

  if (!isOpen) return null;

  return (
    <div className="sort-panel-overlay" onClick={onClose}>
      <div className="sort-panel" onClick={e => e.stopPropagation()}>
        <div className="sort-panel-header">
          <div className="drag-handle" />
          <h2>Sort By</h2>
        </div>

        <div className="sort-options">
          {SORT_OPTIONS.map((option, index) => (
            <button
              key={index}
              className={`sort-option ${isSelected(option) ? 'selected' : ''}`}
              onClick={() => handleSortSelect(option)}
            >
              <div className="option-content">
                <span className="option-label">{option.label}</span>
                <span className="option-description">{option.description}</span>
              </div>
              {isSelected(option) && <span className="check-icon">✓</span>}
            </button>
          ))}
        </div>

        <div className="sort-panel-footer">
          <label className="save-default-option">
            <input
              type="checkbox"
              checked={saveDefault}
              onChange={(e) => setSaveDefault(e.target.checked)}
            />
            Save as my default sort
          </label>
        </div>
      </div>
    </div>
  );
}

export default SortPanel;
```

## PWA-Specific Considerations

### Offline Filter Support

```javascript
// utils/offlineFilters.js
import { openDB } from 'idb';

const DB_NAME = 'sdcrs-mc';
const STORE_NAME = 'cached-incidents';

/**
 * Apply filters to cached incidents when offline
 */
export async function filterCachedIncidents(filters, sort) {
  const db = await openDB(DB_NAME, 1);
  const allIncidents = await db.getAll(STORE_NAME);

  let filtered = allIncidents;

  // Apply status filter
  if (filters.statuses && filters.statuses.length > 0) {
    filtered = filtered.filter(i => filters.statuses.includes(i.status));
  }

  // Apply date filter
  const dateRange = resolveDateRange(filters);
  if (dateRange.from) {
    filtered = filtered.filter(i =>
      new Date(i.reportedDate) >= dateRange.from
    );
  }
  if (dateRange.to) {
    filtered = filtered.filter(i =>
      new Date(i.reportedDate) <= dateRange.to
    );
  }

  // Apply priority filter
  if (filters.priorityFlags && filters.priorityFlags.length > 0) {
    filtered = filtered.filter(i =>
      filters.priorityFlags.some(flag => i.priorityFlags?.includes(flag))
    );
  }

  // Apply sorting
  filtered.sort((a, b) => {
    let comparison = 0;
    switch (sort.field) {
      case 'reportedDate':
        comparison = new Date(a.reportedDate) - new Date(b.reportedDate);
        break;
      case 'distance':
        comparison = (a.distance || 0) - (b.distance || 0);
        break;
      case 'priorityScore':
        comparison = (b.priorityScore || 0) - (a.priorityScore || 0);
        break;
    }
    return sort.direction === 'desc' ? -comparison : comparison;
  });

  return filtered;
}
```

## API Endpoints

### GET /sdcrs/incidents/v1/_search

Search incidents with filters.

**Query Parameters:**
- `jurisdictionId` (required): Jurisdiction ID
- `statuses`: Comma-separated status values
- `fromDate`: ISO date string
- `toDate`: ISO date string
- `priorityFlags`: Comma-separated flags
- `latitude`: User latitude for distance
- `longitude`: User longitude for distance
- `radiusKm`: Search radius in km
- `sortBy`: Field to sort by
- `sortOrder`: asc or desc
- `offset`: Pagination offset
- `limit`: Page size

### GET /sdcrs/incidents/v1/_facets

Get filter facet counts.

**Response:**
```json
{
  "facets": {
    "statusCounts": {
      "NEW": 12,
      "ASSIGNED": 8,
      "IN_PROGRESS": 3,
      "CAPTURED": 45,
      "UTL": 15
    },
    "urgentCount": 5,
    "assignedCount": 8,
    "nearbyCount": 12,
    "dateRangeCounts": {
      "TODAY": 3,
      "LAST_7_DAYS": 15,
      "LAST_30_DAYS": 45
    }
  }
}
```

## Dependencies

| Dependency | Purpose | Required |
|------------|---------|----------|
| zustand | State management for filters | Yes |
| idb | IndexedDB for offline filtering | No |
| date-fns | Date manipulation | Yes |

## Related Stories

- [MC-VIEW-01](./MC-VIEW-01.md) - Map View with Incident Markers
- [MC-VIEW-04](./MC-VIEW-04.md) - Incident Details View
- [MC-FLD-01](./MC-FLD-01.md) - Update Status to In Progress

---

*Last Updated: 2024-01-15*
