# SDCRS Dashboard Designs

Interactive HTML mockups for the Stray Dog Capture & Reporting System (SDCRS) dashboards, designed for each user persona in the workflow.

---

## Design Rationale

**1. Role-Based Information Architecture**
Each dashboard surfaces only the metrics relevant to that user's decision-making context. A teacher needs earnings visibility and submission status; a verifier needs queue depth and SLA pressure; an MC officer needs geographic assignment context. This reduces cognitive load and accelerates task completion.

**2. Visual Hierarchy: Overview → Detail**
Dashboards follow the information visualization mantra: *"Overview first, zoom and filter, then details on demand."* Key Performance Indicators (KPIs) appear as prominent cards at the top, with supporting charts and tables below. Users can quickly assess system health before diving into specifics.

**3. Actionable Metrics Over Vanity Metrics**
Every visualized metric ties to a decision or action the user can take. Teachers see "potential earnings" to motivate submissions; verifiers see "SLA breach risk" to prioritize work; supervisors see "team workload distribution" to rebalance assignments. Metrics without clear action paths were excluded.

**4. Chart Type Selection by Data Relationship**
- **Line charts**: Temporal trends (submissions over time, participation growth)
- **Bar charts**: Categorical comparisons (district performance, team workload)
- **Doughnut/Pie charts**: Part-to-whole relationships (status distribution, fraud types)
- **Funnel charts**: Sequential conversion (report → verified → captured → resolved)
- **Maps**: Geographic context for field operations (assignment locations)

**5. Progressive Complexity Across Hierarchy**
Dashboard complexity scales with organizational level. Teachers see simple personal stats; district admins see aggregated ward-level funnels; state admins see cross-district comparisons with budget tracking. This mirrors how each role's analytical needs differ in scope and depth.

---

## Dashboard Index

| # | File | Persona | Description |
|---|------|---------|-------------|
| 01 | [01-teacher-dashboard.html](./01-teacher-dashboard.html) | Teacher (Reporter) | Personal submission tracking, earnings status, monthly trends |
| 02 | [02-verifier-dashboard.html](./02-verifier-dashboard.html) | Verifier (Backend Operator) | Review queue, SLA monitoring, fraud flags, verification metrics |
| 03 | [03-mc-officer-dashboard.html](./03-mc-officer-dashboard.html) | MC Field Officer | Map view with assignments, capture tasks, field operation stats |
| 04 | [04-mc-supervisor-dashboard.html](./04-mc-supervisor-dashboard.html) | MC Supervisor | Team status, workload distribution, escalations, performance |
| 05 | [05-district-admin-dashboard.html](./05-district-admin-dashboard.html) | District Administrator | District KPIs, ward-level funnel, trend analysis, leaderboards |
| 06 | [06-state-admin-dashboard.html](./06-state-admin-dashboard.html) | State Administrator | Statewide metrics, district comparison, budget tracking, fraud analysis |

---

## Common Features

All dashboards include:

- **Date Range Filters**: Today, Week, Month, Custom range picker
- **Responsive Layout**: Cards and grids adapt to viewport
- **Theme Consistency**: Role-specific accent colors for quick context identification
- **Interactive Charts**: Chart.js visualizations with hover tooltips
- **Real-time Indicators**: Simulated live data badges where applicable

---

## Theme Colors by Role

| Role | Primary Color | Rationale |
|------|---------------|-----------|
| Teacher | Blue (#2563eb) | Trust, stability - core reporters |
| Verifier | Purple (#7c3aed) | Authority, decision-making power |
| MC Officer | Orange (#ea580c) | Action, urgency - field operations |
| MC Supervisor | Cyan (#0891b2) | Oversight, coordination |
| District Admin | Indigo (#4f46e5) | Analytics, strategic view |
| State Admin | Purple + Dark (#7c3aed) | Executive, comprehensive control |

---

## Technology Stack

- **HTML5**: Single-file, self-contained dashboards
- **CSS3**: Embedded styles with CSS Grid/Flexbox layouts
- **Chart.js (CDN)**: Data visualization library
- **Leaflet.js (CDN)**: Map visualization (MC Officer dashboard)

---

## Usage

Open any `.html` file directly in a browser. No build step or server required.

```bash
# Example: Open teacher dashboard
open 01-teacher-dashboard.html
```

---

## Mapping to Process Performance Indicators (PPIs)

These dashboards visualize the 70+ PPIs defined in [03-service-design.md](../03-service-design.md). Key mappings:

| Dashboard | Primary PPIs |
|-----------|-------------|
| Teacher | T-SUB-01 (submissions), T-PAY-01 (earnings), T-STAT-01 (success rate) |
| Verifier | V-SLA-01 (verification time), V-REJ-01 (rejection rate), V-FRD-01 (fraud detection) |
| MC Officer | MC-CAP-01 (capture rate), MC-FLD-01 (field time), MC-UTL-01 (unable to locate) |
| MC Supervisor | Team aggregates of MC Officer PPIs, workload distribution |
| District Admin | Aggregated district PPIs, funnel conversion rates |
| State Admin | Statewide aggregates, cross-district comparisons, budget utilization |

---

*Document Version: 1.0*
*Last Updated: December 2025*
