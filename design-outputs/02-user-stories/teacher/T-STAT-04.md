# T-STAT-04: Shareable Public Link

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** share a public link for my submission status,
**So that** others can track the progress without logging in.

---

## Description

Teachers can generate a shareable public permalink that shows the submission status without requiring authentication. This allows community members, school principals, or local authorities to follow the progress of a reported stray dog. The public view shows limited information for privacy - no teacher details or exact GPS coordinates.

---

## Acceptance Criteria

### Functional

- [ ] Public link accessible without login
- [ ] Link format: `https://sdcrs.gov.dj/status/{APPLICATION_ID}`
- [ ] Public view shows: status, general location (block only), condition tags
- [ ] Public view hides: teacher details, exact GPS, notes, selfie
- [ ] Share button generates link and opens native share sheet
- [ ] Link can be copied to clipboard
- [ ] Link works in SMS, WhatsApp, email
- [ ] Public page is mobile-responsive
- [ ] Public page has app download CTA
- [ ] Links remain active for 90 days after final status

### Public vs Private Information

| Information | Teacher View | Public View |
|-------------|--------------|-------------|
| Application ID | ✓ | ✓ |
| Current Status | ✓ | ✓ |
| Status Timeline | ✓ | ✓ |
| Dog Photo | ✓ | ✓ |
| Selfie Photo | ✓ | ✗ |
| General Location (Block) | ✓ | ✓ |
| Exact GPS | ✓ | ✗ |
| Condition Tags | ✓ | ✓ |
| Notes | ✓ | ✗ |
| Teacher Name | ✓ | ✗ |
| Payout Details | ✓ | ✗ |
| Rejection Reason | ✓ | ✓ (generic) |

---

## UI/UX Requirements (PWA)

### Share Button in App

```
┌─────────────────────────────────┐
│  ← Application Details          │
├─────────────────────────────────┤
│                                 │
│  SDCRS-20261207-A1B2C      [📋] │
│                                 │
│  ┌───────────────────────────┐  │
│  │      [Dog Photo]          │  │
│  │                           │  │
│  └───────────────────────────┘  │
│                                 │
│  Current Status                 │
│  ┌───────────────────────────┐  │
│  │  🟢 CAPTURED               │  │
│  │  Dec 8, 2026, 10:30 AM    │  │
│  └───────────────────────────┘  │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  [  🔗 Share Status Link  ]     │
│                                 │
└─────────────────────────────────┘
```

### Native Share Sheet

```
┌─────────────────────────────────┐
│  Share via                      │
├─────────────────────────────────┤
│                                 │
│  Track my stray dog report:     │
│  SDCRS-20261207-A1B2C           │
│  Status: Captured               │
│  https://sdcrs.gov.dj/s/A1B2C   │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  [WhatsApp] [SMS] [Copy] [More] │
│                                 │
└─────────────────────────────────┘
```

### Public Status Page

```
┌─────────────────────────────────┐
│  SDCRS - Stray Dog Tracking     │
├─────────────────────────────────┤
│                                 │
│  Application ID                 │
│  SDCRS-20261207-A1B2C           │
│                                 │
│  ┌───────────────────────────┐  │
│  │      [Dog Photo]          │  │
│  │                           │  │
│  └───────────────────────────┘  │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  Current Status                 │
│  ┌───────────────────────────┐  │
│  │  🟢 CAPTURED               │  │
│  │  Dec 8, 2026               │  │
│  └───────────────────────────┘  │
│                                 │
│  📍 Location: Central Block      │
│  🏷️ Condition: Injured           │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  Status Timeline                │
│                                 │
│  ● Captured - Dec 8             │
│  ● Verified - Dec 7             │
│  ● Reported - Dec 7             │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  📱 Report a stray dog?         │
│  [  Download SDCRS App  ]       │
│                                 │
│  ─────────────────────────────  │
│  © 2026 SDCRS Djibouti          │
│                                 │
└─────────────────────────────────┘
```

### Public Rejected View

```
┌─────────────────────────────────┐
│  SDCRS - Stray Dog Tracking     │
├─────────────────────────────────┤
│                                 │
│  Application ID                 │
│  SDCRS-20261205-P3Q4R           │
│                                 │
│  Current Status                 │
│  ┌───────────────────────────┐  │
│  │  🔴 NOT APPROVED           │  │
│  │  Dec 5, 2026               │  │
│  └───────────────────────────┘  │
│                                 │
│  This report did not meet our   │
│  verification criteria.         │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  📱 Report a stray dog?         │
│  [  Download SDCRS App  ]       │
│                                 │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Web Share API for native share sheet
- [ ] Fallback copy-to-clipboard for unsupported browsers
- [ ] Short URL generation for SMS-friendly sharing
- [ ] OpenGraph meta tags for rich link previews
- [ ] Public page works without JavaScript (SSR)
- [ ] Public page loads fast (< 2s)
- [ ] Deep link to app if installed

---

## Technical Notes

### Web Share API

```javascript
async function shareStatusLink(application) {
  const shareData = {
    title: 'SDCRS Status',
    text: `Track my stray dog report: ${application.applicationId}\nStatus: ${application.currentStatus.label}`,
    url: getPublicUrl(application.applicationId)
  };

  if (navigator.share && navigator.canShare(shareData)) {
    try {
      await navigator.share(shareData);
      trackShare(application.applicationId, 'native');
    } catch (error) {
      if (error.name !== 'AbortError') {
        console.error('Share failed:', error);
        fallbackShare(shareData);
      }
    }
  } else {
    fallbackShare(shareData);
  }
}

function fallbackShare(shareData) {
  // Show custom share modal with copy button
  showShareModal(shareData);
}
```

### Short URL Generation

```javascript
// Server-side short URL generation
function generateShortUrl(applicationId) {
  // Use last 5 characters of application ID
  const shortCode = applicationId.split('-').pop();
  return `https://sdcrs.gov.dj/s/${shortCode}`;
}

// Redirect service
app.get('/s/:code', async (req, res) => {
  const application = await findByShortCode(req.params.code);
  if (application) {
    res.redirect(`/status/${application.applicationId}`);
  } else {
    res.status(404).render('not-found');
  }
});
```

### Public Status API

```javascript
// Public endpoint - no auth required
app.get('/api/public/status/:applicationId', async (req, res) => {
  const application = await getApplication(req.params.applicationId);

  if (!application) {
    return res.status(404).json({ error: 'Not found' });
  }

  // Return only public-safe information
  res.json({
    applicationId: application.applicationId,
    status: {
      code: application.currentStatus.code,
      label: application.currentStatus.label,
      timestamp: application.currentStatus.timestamp
    },
    location: {
      block: application.location.block,
      district: application.location.district
      // No GPS coordinates
    },
    conditionTags: application.conditionTags,
    dogPhotoUrl: application.photos.dog, // Public photo
    // No selfie, notes, teacher info, payout details
    timeline: application.timeline.map(e => ({
      status: e.status,
      date: e.timestamp.split('T')[0] // Date only, no time
    })),
    isRejected: application.status === 'rejected',
    rejectionMessage: application.status === 'rejected'
      ? 'This report did not meet verification criteria.'
      : null
  });
});
```

### OpenGraph Meta Tags

```html
<!-- Public status page head -->
<head>
  <title>SDCRS Status - {{applicationId}}</title>

  <!-- OpenGraph -->
  <meta property="og:title" content="Stray Dog Report Status" />
  <meta property="og:description" content="Track status: {{applicationId}} - {{status}}" />
  <meta property="og:image" content="{{dogPhotoUrl}}" />
  <meta property="og:url" content="{{canonicalUrl}}" />
  <meta property="og:type" content="article" />

  <!-- Twitter Card -->
  <meta name="twitter:card" content="summary_large_image" />
  <meta name="twitter:title" content="SDCRS Status - {{applicationId}}" />
  <meta name="twitter:description" content="Status: {{status}}" />
  <meta name="twitter:image" content="{{dogPhotoUrl}}" />
</head>
```

### Copy to Clipboard Fallback

```javascript
async function copyToClipboard(text) {
  if (navigator.clipboard && navigator.clipboard.writeText) {
    await navigator.clipboard.writeText(text);
  } else {
    // Fallback for older browsers
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand('copy');
    document.body.removeChild(textarea);
  }

  showToast('Link copied to clipboard');
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Web Share API | Browser | Native sharing |
| Clipboard API | Browser | Copy functionality |
| URL Shortener | Service | Generate short URLs |
| SSR Framework | Backend | Public page rendering |

---

## Related Stories

- [T-STAT-01](./T-STAT-01.md) - View submission status
- [T-SUB-06](./T-SUB-06.md) - Receive Application ID
- [T-STAT-02](./T-STAT-02.md) - SMS with tracking link
