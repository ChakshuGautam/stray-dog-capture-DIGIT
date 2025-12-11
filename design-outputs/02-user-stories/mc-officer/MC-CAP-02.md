# MC-CAP-02: Add Resolution Notes and Photo

> **Back to:** [User Stories Master](../../design-outputs/02-user-stories.md)

## User Story

**As an** MC Officer,
**I want to** add notes and photos when marking an incident as resolved,
**So that** there is documentation of the capture for records and quality assurance.

## Description

Resolution documentation provides valuable records for municipal administration, helps with quality assurance, and creates an audit trail. Officers can add text notes describing the resolution and optionally attach a photo of the captured dog or resolved situation.

## Acceptance Criteria

| # | Criterion | Priority |
|---|-----------|----------|
| 1 | Text input field for resolution notes | Must |
| 2 | Notes field supports multi-line text | Must |
| 3 | Camera integration for photo capture | Must |
| 4 | Photo preview before submission | Must |
| 5 | Option to retake/remove photo | Should |
| 6 | Notes limited to 500 characters | Should |
| 7 | Character count displayed | Should |
| 8 | Photo compressed before upload | Must |
| 9 | Works offline with queued sync | Must |
| 10 | Quick templates for common notes | Should |

## UI/UX Design

### Resolution Notes Input

```
┌─────────────────────────────────────────────────────────────┐
│                Resolution Documentation                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Resolution Photo:                                         │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │              ┌─────────────────┐                    │   │
│  │              │                 │                    │   │
│  │              │   📷            │                    │   │
│  │              │   Preview       │                    │   │
│  │              │                 │                    │   │
│  │              └─────────────────┘                    │   │
│  │                                                     │   │
│  │  [🔄 Retake]              [🗑️ Remove]              │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Resolution Notes:                                         │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Dog was found approximately 50 meters from the     │   │
│  │ reported location. Captured using catch pole.      │   │
│  │ Dog appeared healthy, medium size, brown color.    │   │
│  │ Transported to municipal shelter facility.         │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│  Characters: 218 / 500                                     │
│                                                             │
│  Quick Templates:                                          │
│  [Standard capture] [Owner found] [Dog relocated]          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Photo Capture Flow

```
┌─────────────────────────────────────────────────────────────┐
│                     Camera View                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │                                                     │   │
│  │                                                     │   │
│  │                   [Camera Feed]                     │   │
│  │                                                     │   │
│  │                                                     │   │
│  │                                                     │   │
│  │                                                     │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │      [✕ Cancel]        [📸]         [🔄 Flip]       │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Tips:                                                     │
│  • Include the captured dog in frame                       │
│  • Good lighting improves photo quality                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Photo Preview and Confirm

```
┌─────────────────────────────────────────────────────────────┐
│                   Review Photo                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │                                                     │   │
│  │                                                     │   │
│  │              [Captured Photo Preview]               │   │
│  │                                                     │   │
│  │                                                     │   │
│  │                                                     │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Photo will be attached to resolution record.              │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │       [🔄 Retake]           [✓ Use Photo]           │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Technical Implementation

### Resolution Notes Component

```javascript
// components/ResolutionNotesInput.jsx
import React, { useState } from 'react';

const MAX_CHARACTERS = 500;

const QUICK_TEMPLATES = [
  {
    id: 'standard',
    label: 'Standard capture',
    text: 'Dog located and captured at reported location. Transported to municipal shelter facility.'
  },
  {
    id: 'owner',
    label: 'Owner found',
    text: 'Dog was claimed by owner at the location. Owner verified and provided contact information.'
  },
  {
    id: 'relocated',
    label: 'Dog relocated',
    text: 'Dog had already left the reported area. Searched surrounding area but unable to locate.'
  },
  {
    id: 'multiple',
    label: 'Multiple dogs',
    text: 'Multiple dogs found at location. All dogs captured and transported to shelter.'
  }
];

function ResolutionNotesInput({ value, onChange, error }) {
  const [showTemplates, setShowTemplates] = useState(false);

  const handleChange = (e) => {
    const text = e.target.value;
    if (text.length <= MAX_CHARACTERS) {
      onChange(text);
    }
  };

  const applyTemplate = (template) => {
    onChange(template.text);
    setShowTemplates(false);
  };

  const remainingChars = MAX_CHARACTERS - (value?.length || 0);
  const isNearLimit = remainingChars < 50;

  return (
    <div className="resolution-notes-input">
      <label className="input-label">
        Resolution Notes:
        <span className="optional-badge">Optional</span>
      </label>

      <textarea
        value={value || ''}
        onChange={handleChange}
        placeholder="Add details about the resolution (how the dog was captured, condition, etc.)..."
        rows={4}
        className={`notes-textarea ${error ? 'has-error' : ''}`}
      />

      <div className="input-footer">
        <span className={`char-count ${isNearLimit ? 'near-limit' : ''}`}>
          {value?.length || 0} / {MAX_CHARACTERS}
        </span>

        <button
          type="button"
          className="btn-templates"
          onClick={() => setShowTemplates(!showTemplates)}
        >
          📝 Quick Templates
        </button>
      </div>

      {showTemplates && (
        <div className="templates-dropdown">
          {QUICK_TEMPLATES.map(template => (
            <button
              key={template.id}
              type="button"
              className="template-option"
              onClick={() => applyTemplate(template)}
            >
              <span className="template-label">{template.label}</span>
              <span className="template-preview">
                {template.text.substring(0, 50)}...
              </span>
            </button>
          ))}
        </div>
      )}

      {error && (
        <span className="error-text">{error}</span>
      )}
    </div>
  );
}

export default ResolutionNotesInput;
```

### Photo Capture Component

```javascript
// components/ResolutionPhotoCapture.jsx
import React, { useState, useRef, useCallback } from 'react';

function ResolutionPhotoCapture({ onPhotoCapture, onRemove, currentPhoto }) {
  const [mode, setMode] = useState('idle'); // idle, camera, preview
  const [stream, setStream] = useState(null);
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const fileInputRef = useRef(null);

  const startCamera = async () => {
    try {
      const mediaStream = await navigator.mediaDevices.getUserMedia({
        video: {
          facingMode: 'environment', // Rear camera
          width: { ideal: 1280 },
          height: { ideal: 720 }
        }
      });

      setStream(mediaStream);
      if (videoRef.current) {
        videoRef.current.srcObject = mediaStream;
      }
      setMode('camera');
    } catch (error) {
      console.error('Camera access denied:', error);
      // Fall back to file input
      fileInputRef.current?.click();
    }
  };

  const stopCamera = useCallback(() => {
    if (stream) {
      stream.getTracks().forEach(track => track.stop());
      setStream(null);
    }
    setMode('idle');
  }, [stream]);

  const capturePhoto = () => {
    const video = videoRef.current;
    const canvas = canvasRef.current;

    if (!video || !canvas) return;

    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;

    const ctx = canvas.getContext('2d');
    ctx.drawImage(video, 0, 0);

    canvas.toBlob(async (blob) => {
      // Compress image
      const compressedBlob = await compressImage(blob);
      const file = new File([compressedBlob], 'resolution-photo.jpg', {
        type: 'image/jpeg'
      });

      onPhotoCapture(file, URL.createObjectURL(compressedBlob));
      stopCamera();
      setMode('preview');
    }, 'image/jpeg', 0.8);
  };

  const handleFileSelect = async (event) => {
    const file = event.target.files[0];
    if (file) {
      const compressedBlob = await compressImage(file);
      const compressedFile = new File([compressedBlob], 'resolution-photo.jpg', {
        type: 'image/jpeg'
      });

      onPhotoCapture(compressedFile, URL.createObjectURL(compressedBlob));
      setMode('preview');
    }
  };

  const handleRetake = () => {
    onRemove();
    startCamera();
  };

  const handleRemove = () => {
    onRemove();
    setMode('idle');
  };

  // Render based on mode
  if (mode === 'camera') {
    return (
      <div className="photo-capture camera-mode">
        <video
          ref={videoRef}
          autoPlay
          playsInline
          muted
          className="camera-feed"
        />
        <canvas ref={canvasRef} style={{ display: 'none' }} />

        <div className="camera-controls">
          <button className="btn-cancel" onClick={stopCamera}>
            ✕ Cancel
          </button>
          <button className="btn-capture" onClick={capturePhoto}>
            📸
          </button>
          <button className="btn-flip" onClick={toggleCamera}>
            🔄 Flip
          </button>
        </div>

        <div className="camera-tips">
          <p>• Include the captured dog in frame</p>
          <p>• Good lighting improves quality</p>
        </div>
      </div>
    );
  }

  if (mode === 'preview' || currentPhoto) {
    return (
      <div className="photo-capture preview-mode">
        <div className="photo-preview">
          <img
            src={currentPhoto}
            alt="Resolution photo preview"
          />
        </div>

        <div className="preview-controls">
          <button className="btn-retake" onClick={handleRetake}>
            🔄 Retake
          </button>
          <button className="btn-remove" onClick={handleRemove}>
            🗑️ Remove
          </button>
        </div>
      </div>
    );
  }

  // Idle mode - show capture button
  return (
    <div className="photo-capture idle-mode">
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        capture="environment"
        onChange={handleFileSelect}
        style={{ display: 'none' }}
      />

      <button className="btn-add-photo" onClick={startCamera}>
        <span className="icon">📷</span>
        <span className="text">Add Resolution Photo</span>
        <span className="hint">Optional</span>
      </button>
    </div>
  );
}

/**
 * Compress image to reduce upload size
 */
async function compressImage(blob, maxWidth = 1280, quality = 0.7) {
  return new Promise((resolve) => {
    const img = new Image();
    img.onload = () => {
      const canvas = document.createElement('canvas');

      let width = img.width;
      let height = img.height;

      // Scale down if needed
      if (width > maxWidth) {
        height = (height * maxWidth) / width;
        width = maxWidth;
      }

      canvas.width = width;
      canvas.height = height;

      const ctx = canvas.getContext('2d');
      ctx.drawImage(img, 0, 0, width, height);

      canvas.toBlob(resolve, 'image/jpeg', quality);
    };
    img.src = URL.createObjectURL(blob);
  });
}

export default ResolutionPhotoCapture;
```

### Offline Photo Queue

```javascript
// services/OfflinePhotoQueue.js
import { openDB } from 'idb';

const DB_NAME = 'sdcrs-photos';
const STORE_NAME = 'pending-photos';

class OfflinePhotoQueue {
  async initDB() {
    return openDB(DB_NAME, 1, {
      upgrade(db) {
        if (!db.objectStoreNames.contains(STORE_NAME)) {
          db.createObjectStore(STORE_NAME, { keyPath: 'id' });
        }
      }
    });
  }

  /**
   * Queue photo for later upload
   */
  async queuePhoto(incidentId, photoData, type = 'RESOLUTION') {
    const db = await this.initDB();

    const record = {
      id: `${incidentId}_${type}_${Date.now()}`,
      incidentId,
      type,
      photoData, // Base64 encoded
      queuedAt: new Date().toISOString(),
      status: 'PENDING',
      retryCount: 0
    };

    await db.put(STORE_NAME, record);
    return record.id;
  }

  /**
   * Get pending photos for sync
   */
  async getPendingPhotos() {
    const db = await this.initDB();
    const all = await db.getAll(STORE_NAME);
    return all.filter(p => p.status === 'PENDING');
  }

  /**
   * Sync pending photos when online
   */
  async syncPendingPhotos() {
    const pending = await this.getPendingPhotos();

    for (const photo of pending) {
      try {
        await this.uploadPhoto(photo);
        await this.markSynced(photo.id);
      } catch (error) {
        console.error('Failed to sync photo:', photo.id, error);
        await this.incrementRetry(photo.id);
      }
    }
  }

  async uploadPhoto(photoRecord) {
    // Convert base64 back to blob
    const response = await fetch(photoRecord.photoData);
    const blob = await response.blob();

    const formData = new FormData();
    formData.append('file', blob, 'resolution-photo.jpg');
    formData.append('type', photoRecord.type);
    formData.append('incidentId', photoRecord.incidentId);

    const uploadResponse = await fetch('/api/sdcrs/photos/v1/upload', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      },
      body: formData
    });

    if (!uploadResponse.ok) {
      throw new Error(`Upload failed: ${uploadResponse.status}`);
    }

    return uploadResponse.json();
  }

  async markSynced(photoId) {
    const db = await this.initDB();
    const photo = await db.get(STORE_NAME, photoId);
    if (photo) {
      photo.status = 'SYNCED';
      photo.syncedAt = new Date().toISOString();
      await db.put(STORE_NAME, photo);
    }
  }

  async incrementRetry(photoId) {
    const db = await this.initDB();
    const photo = await db.get(STORE_NAME, photoId);
    if (photo) {
      photo.retryCount++;
      if (photo.retryCount >= 3) {
        photo.status = 'FAILED';
      }
      await db.put(STORE_NAME, photo);
    }
  }
}

export default OfflinePhotoQueue;
```

## API Endpoints

### POST /sdcrs/photos/v1/upload

Upload resolution photo.

**Request (multipart/form-data):**
- `file`: Image file (JPEG/PNG, max 5MB)
- `type`: "RESOLUTION"
- `incidentId`: Incident ID

**Response:**
```json
{
  "photoId": "PHT-RES-001",
  "url": "/photos/PHT-RES-001.jpg",
  "thumbnailUrl": "/photos/PHT-RES-001_thumb.jpg",
  "uploadedAt": "2026-01-15T11:30:00Z"
}
```

## Dependencies

| Dependency | Purpose | Required |
|------------|---------|----------|
| MC-CAP-01 | Mark captured flow | Yes |
| MediaDevices API | Camera access | Yes |
| Canvas API | Photo capture | Yes |
| idb | Offline photo queue | Yes |

## Related Stories

- [MC-CAP-01](./MC-CAP-01.md) - Mark Captured/Resolved
- [MC-UTL-02](./MC-UTL-02.md) - Add UTL Notes
- [T-SUB-02](../teacher/T-SUB-02.md) - Capture Photo

---

*Last Updated: 2026-01-15*
