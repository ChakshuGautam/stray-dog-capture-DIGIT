# S-VAL-03: Verify Photo Hash

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## System Behavior

**The system** generates and stores a hash of each submitted photo
**To ensure** photos are unique and detect duplicate submissions.

---

## Description

Each photo submitted is hashed using a perceptual hashing algorithm that can detect similar images even if slightly modified. This prevents teachers from submitting the same dog photo multiple times, or submitting slightly edited versions of the same image.

---

## Acceptance Criteria

### Validation Rules

- [ ] Generate perceptual hash (pHash) for each uploaded photo
- [ ] Compare hash against database of existing submissions
- [ ] Flag exact matches as duplicates
- [ ] Flag similar images (>90% match) for review
- [ ] Store hash with submission metadata
- [ ] Hash comparison scoped to tenant and time window (30 days)
- [ ] Separate checks for dog photo and selfie
- [ ] Log all hash comparisons for audit

### Hash Match Thresholds

| Match Similarity | Interpretation | Action |
|------------------|----------------|--------|
| 100% | Exact duplicate | Auto-reject |
| 95-99% | Minor edit (crop, filter) | Auto-reject |
| 90-94% | Likely same dog | Flag for review |
| <90% | Different image | Accept |

### Duplicate Detection Scope

| Check Against | Time Window | Scope |
|---------------|-------------|-------|
| Same teacher | 30 days | All submissions |
| Other teachers | 7 days | Same block/area |
| All submissions | 24 hours | Same GPS vicinity (500m) |

---

## Technical Implementation

### Perceptual Hash Generation

```javascript
const sharp = require('sharp');
const phash = require('phash-image');

async function generatePhotoHash(photoBuffer) {
  // Normalize image for consistent hashing
  const normalized = await sharp(photoBuffer)
    .resize(256, 256, { fit: 'cover' })
    .grayscale()
    .raw()
    .toBuffer();

  // Generate perceptual hash
  const hash = await phash.hash(normalized);

  return {
    pHash: hash,
    timestamp: Date.now()
  };
}
```

### Hash Comparison

```javascript
const phash = require('phash-image');

function compareHashes(hash1, hash2) {
  // Calculate Hamming distance between hashes
  const distance = phash.hammingDistance(hash1, hash2);

  // Convert to similarity percentage (64-bit hash)
  const similarity = 1 - (distance / 64);

  return {
    distance,
    similarity: Math.round(similarity * 100),
    isMatch: similarity >= 0.9,
    isExact: similarity >= 0.99
  };
}
```

### Duplicate Detection Service

```javascript
class DuplicateDetector {
  constructor(db) {
    this.db = db;
  }

  async checkForDuplicates(submission, photoHash) {
    const result = {
      isDuplicate: false,
      isNearDuplicate: false,
      matches: [],
      warnings: []
    };

    // Check 1: Same teacher, 30 days
    const sameTeacherMatches = await this.findMatches({
      teacherId: submission.teacherId,
      fromDate: daysAgo(30),
      hash: photoHash,
      threshold: 0.90
    });

    if (sameTeacherMatches.length > 0) {
      const exactMatch = sameTeacherMatches.find(m => m.similarity >= 0.95);
      if (exactMatch) {
        result.isDuplicate = true;
        result.matches.push({
          type: 'SAME_TEACHER_DUPLICATE',
          applicationId: exactMatch.applicationId,
          similarity: exactMatch.similarity
        });
        return result;
      }
      result.isNearDuplicate = true;
      result.warnings.push('SIMILAR_PREVIOUS_SUBMISSION');
    }

    // Check 2: Same area, 7 days
    const sameAreaMatches = await this.findMatches({
      block: submission.block,
      fromDate: daysAgo(7),
      excludeTeacher: submission.teacherId,
      hash: photoHash,
      threshold: 0.95
    });

    if (sameAreaMatches.length > 0) {
      result.isNearDuplicate = true;
      result.warnings.push('SIMILAR_SUBMISSION_IN_AREA');
      result.matches.push(...sameAreaMatches.map(m => ({
        type: 'SAME_AREA_SIMILAR',
        applicationId: m.applicationId,
        similarity: m.similarity
      })));
    }

    // Check 3: Same GPS vicinity, 24 hours
    const nearbyMatches = await this.findNearbyMatches({
      latitude: submission.latitude,
      longitude: submission.longitude,
      radiusMeters: 500,
      fromDate: daysAgo(1),
      hash: photoHash,
      threshold: 0.90
    });

    if (nearbyMatches.length > 0) {
      result.warnings.push('SIMILAR_NEARBY_RECENT');
      result.matches.push(...nearbyMatches.map(m => ({
        type: 'NEARBY_RECENT',
        applicationId: m.applicationId,
        similarity: m.similarity,
        distance: m.distance
      })));
    }

    return result;
  }

  async findMatches(criteria) {
    const candidates = await this.db.submissions.find({
      tenantId: criteria.tenantId,
      teacherId: criteria.teacherId,
      block: criteria.block,
      createdAt: { $gte: criteria.fromDate },
      ...(criteria.excludeTeacher && {
        teacherId: { $ne: criteria.excludeTeacher }
      })
    });

    return candidates
      .map(candidate => ({
        applicationId: candidate.applicationId,
        ...compareHashes(criteria.hash, candidate.photoHash)
      }))
      .filter(result => result.similarity >= criteria.threshold * 100)
      .sort((a, b) => b.similarity - a.similarity);
  }

  async findNearbyMatches(criteria) {
    // Use geospatial query to find nearby submissions
    const candidates = await this.db.submissions.find({
      location: {
        $nearSphere: {
          $geometry: {
            type: 'Point',
            coordinates: [criteria.longitude, criteria.latitude]
          },
          $maxDistance: criteria.radiusMeters
        }
      },
      createdAt: { $gte: criteria.fromDate }
    });

    return candidates
      .map(candidate => ({
        applicationId: candidate.applicationId,
        distance: calculateDistance(
          criteria.latitude, criteria.longitude,
          candidate.latitude, candidate.longitude
        ),
        ...compareHashes(criteria.hash, candidate.photoHash)
      }))
      .filter(result => result.similarity >= criteria.threshold * 100);
  }
}

function daysAgo(days) {
  return new Date(Date.now() - days * 24 * 60 * 60 * 1000);
}
```

### Integration in Submission Pipeline

```javascript
async function processSubmissionHash(submission) {
  // Generate hash for dog photo
  const photoBuffer = await fetchPhotoBuffer(submission.dogPhoto);
  const photoHash = await generatePhotoHash(photoBuffer);

  // Store hash with submission
  submission.photoHash = photoHash.pHash;

  // Check for duplicates
  const detector = new DuplicateDetector(db);
  const duplicateCheck = await detector.checkForDuplicates(submission, photoHash.pHash);

  // Log the check
  await logHashValidation(submission, duplicateCheck);

  if (duplicateCheck.isDuplicate) {
    await autoRejectSubmission(submission, 'DUPLICATE', {
      matchedApplication: duplicateCheck.matches[0].applicationId
    });
    return {
      success: false,
      reason: 'DUPLICATE',
      matches: duplicateCheck.matches
    };
  }

  if (duplicateCheck.isNearDuplicate) {
    submission.flags = submission.flags || [];
    submission.flags.push('POTENTIAL_DUPLICATE');
    submission.duplicateMatches = duplicateCheck.matches;
  }

  return { success: true, duplicateCheck };
}
```

### Validation Logging

```javascript
async function logHashValidation(submission, result) {
  await auditLog.create({
    type: 'HASH_VALIDATION',
    applicationId: submission.applicationId,
    teacherId: submission.teacherId,
    data: {
      photoHash: submission.photoHash.substring(0, 16) + '...', // Truncated for log
      isDuplicate: result.isDuplicate,
      isNearDuplicate: result.isNearDuplicate,
      matchCount: result.matches.length,
      matches: result.matches.map(m => ({
        applicationId: m.applicationId,
        type: m.type,
        similarity: m.similarity
      })),
      warnings: result.warnings
    },
    timestamp: new Date()
  });
}
```

---

## Error Messages

| Error Code | User Message |
|------------|--------------|
| DUPLICATE | This photo has already been submitted. Please take a new photo of the dog. |
| SIMILAR_SUBMISSION | This photo appears similar to a recent submission. If this is a different dog, please take another photo. |
| PHOTO_EDITED | This photo appears to have been edited. Please submit an unmodified photo taken with the app. |

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| sharp | Library | Image processing |
| phash-image | Library | Perceptual hashing |
| MongoDB | Database | Geospatial queries |
| Audit Log | Service | Record validations |

---

## Related Stories

- [S-VAL-01](./S-VAL-01.md) - Timestamp validation
- [S-VAL-02](./S-VAL-02.md) - Boundary validation
- [S-REJ-03](./S-REJ-03.md) - Auto-reject duplicates
