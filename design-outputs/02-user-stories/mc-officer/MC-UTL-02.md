# MC-UTL-02: Add UTL Notes

[← Back to User Stories Master](../user-stories-master.md) | [← Back to MC Officer Stories](./index.md)

## User Story

**As an** MC Officer marking an incident as Unable to Locate,
**I want to** add detailed notes explaining why I couldn't find the dog,
**So that** there is a clear record of my search efforts and the teacher/admin can understand the situation.

## Description

When an MC Officer marks an incident as UTL (Unable to Locate), they must provide notes explaining their search efforts and why the dog couldn't be found. This documentation is critical for:
- Justifying the UTL status to supervisors
- Providing feedback to the teacher about their report
- Supporting potential re-assignment or escalation
- Building historical data for location accuracy analysis

## Acceptance Criteria

### Functional Requirements
- [ ] MC Officer can enter free-text notes (up to 500 characters)
- [ ] Quick templates available for common UTL scenarios
- [ ] Notes are required for certain UTL reasons (e.g., "Other")
- [ ] Notes support voice-to-text input for hands-free entry
- [ ] Search effort details can be documented (time spent, area covered)
- [ ] Notes are saved with timestamp and officer ID

### Non-Functional Requirements
- [ ] Voice-to-text works offline (device native)
- [ ] Notes auto-save every 10 seconds to prevent data loss
- [ ] Character count shown in real-time
- [ ] Notes sync when connection restored

## UI/UX Design

### UTL Notes Screen
```
┌─────────────────────────────────────────┐
│ ← Add UTL Details                       │
├─────────────────────────────────────────┤
│                                         │
│  Reason: Dog not at reported location   │
│                                         │
│  ┌─────────────────────────────────────┐│
│  │ Quick Templates                     ││
│  ├─────────────────────────────────────┤│
│  │ ┌─────────────┐ ┌─────────────────┐ ││
│  │ │ Searched    │ │ No dog visible  │ ││
│  │ │ 15 minutes  │ │ in area         │ ││
│  │ └─────────────┘ └─────────────────┘ ││
│  │ ┌─────────────┐ ┌─────────────────┐ ││
│  │ │ Asked       │ │ Location too    │ ││
│  │ │ locals      │ │ vague           │ ││
│  │ └─────────────┘ └─────────────────┘ ││
│  └─────────────────────────────────────┘│
│                                         │
│  ┌─────────────────────────────────────┐│
│  │ Notes *                             ││
│  │                                     ││
│  │ Arrived at location at 10:45 AM.   ││
│  │ Searched the area for 20 minutes.  ││
│  │ Asked three local residents - none ││
│  │ had seen the dog. The reported     ││
│  │ location appears to be near a busy ││
│  │ road; dog may have moved on.       ││
│  │                                     ││
│  │                                     ││
│  │                         🎤          ││
│  └─────────────────────────────────────┘│
│                              285/500    │
│                                         │
│  ┌─────────────────────────────────────┐│
│  │ Search Effort (Optional)           ││
│  ├─────────────────────────────────────┤│
│  │ Time spent searching:              ││
│  │ [ 20 ] minutes                     ││
│  │                                     ││
│  │ Area covered (estimate):           ││
│  │ [ 100 ] meters radius              ││
│  │                                     ││
│  │ [ ✓ ] Asked locals/witnesses       ││
│  │ [ ✓ ] Checked nearby hiding spots  ││
│  │ [   ] Waited for dog to appear     ││
│  └─────────────────────────────────────┘│
│                                         │
│  ┌─────────────────────────────────────┐│
│  │          Submit UTL Report          │
│  └─────────────────────────────────────┘│
│                                         │
└─────────────────────────────────────────┘
```

### Voice Input Active
```
┌─────────────────────────────────────────┐
│                                         │
│  ┌─────────────────────────────────────┐│
│  │ Notes *                             ││
│  │                                     ││
│  │ Arrived at location at 10:45 AM... ││
│  │                                     ││
│  │   ┌───────────────────────────┐     ││
│  │   │    🎤 Listening...       │     ││
│  │   │    ════════════════      │     ││
│  │   │    "...asked three..."   │     ││
│  │   │                          │     ││
│  │   │    [Done]  [Cancel]      │     ││
│  │   └───────────────────────────┘     ││
│  │                                     ││
│  └─────────────────────────────────────┘│
│                                         │
└─────────────────────────────────────────┘
```

## Technical Implementation

### UTL Notes Service
```javascript
// src/services/UTLNotesService.js
import { openDB } from 'idb';

class UTLNotesService {
  static MAX_CHARACTERS = 500;
  static AUTO_SAVE_INTERVAL = 10000; // 10 seconds

  static QUICK_TEMPLATES = [
    {
      id: 'searched_time',
      label: 'Searched 15 minutes',
      text: 'Searched the reported location and surrounding area for approximately 15 minutes. No dog matching the description was found.'
    },
    {
      id: 'no_dog_visible',
      label: 'No dog visible in area',
      text: 'Arrived at the reported location. No stray dog was visible in the immediate area or surroundings.'
    },
    {
      id: 'asked_locals',
      label: 'Asked locals',
      text: 'Spoke with local residents/shopkeepers in the area. No one had seen a stray dog matching the description recently.'
    },
    {
      id: 'location_vague',
      label: 'Location too vague',
      text: 'The reported location was too vague to effectively search. Landmarks mentioned could not be identified.'
    },
    {
      id: 'dog_left',
      label: 'Dog appears to have left',
      text: 'Evidence suggests a dog was present earlier (droppings, disturbed area) but has since moved on from the location.'
    },
    {
      id: 'inaccessible',
      label: 'Area inaccessible',
      text: 'The reported location is inaccessible due to private property/construction/locked gate. Unable to enter and search.'
    }
  ];

  static SEARCH_EFFORT_OPTIONS = [
    { id: 'asked_locals', label: 'Asked locals/witnesses' },
    { id: 'checked_hiding', label: 'Checked nearby hiding spots' },
    { id: 'waited', label: 'Waited for dog to appear' },
    { id: 'expanded_search', label: 'Expanded search beyond location' },
    { id: 'used_food', label: 'Used food/bait to attract' }
  ];

  // Get DB instance
  static async getDB() {
    return openDB('sdcrs-utl-notes', 1, {
      upgrade(db) {
        if (!db.objectStoreNames.contains('drafts')) {
          const store = db.createObjectStore('drafts', { keyPath: 'incidentId' });
          store.createIndex('timestamp', 'timestamp');
        }
      }
    });
  }

  // Save draft notes (auto-save)
  static async saveDraft(incidentId, notesData) {
    const db = await this.getDB();
    const draft = {
      incidentId,
      ...notesData,
      timestamp: Date.now()
    };
    await db.put('drafts', draft);
    return draft;
  }

  // Get draft notes
  static async getDraft(incidentId) {
    const db = await this.getDB();
    return db.get('drafts', incidentId);
  }

  // Delete draft (after successful submission)
  static async deleteDraft(incidentId) {
    const db = await this.getDB();
    await db.delete('drafts', incidentId);
  }

  // Validate notes
  static validateNotes(notes, utlReason) {
    const errors = [];

    // Check if notes required for certain reasons
    const reasonsRequiringNotes = ['OTHER', 'INACCESSIBLE'];
    if (reasonsRequiringNotes.includes(utlReason) && (!notes || notes.trim().length === 0)) {
      errors.push('Notes are required for this UTL reason');
    }

    // Check character limit
    if (notes && notes.length > this.MAX_CHARACTERS) {
      errors.push(`Notes cannot exceed ${this.MAX_CHARACTERS} characters`);
    }

    // Check minimum length for meaningful notes
    if (notes && notes.trim().length > 0 && notes.trim().length < 10) {
      errors.push('Please provide more detailed notes (at least 10 characters)');
    }

    return {
      isValid: errors.length === 0,
      errors
    };
  }

  // Apply template to existing notes
  static applyTemplate(currentNotes, templateId) {
    const template = this.QUICK_TEMPLATES.find(t => t.id === templateId);
    if (!template) return currentNotes;

    // Append template to existing notes with separator
    if (currentNotes && currentNotes.trim().length > 0) {
      return `${currentNotes}\n\n${template.text}`;
    }
    return template.text;
  }

  // Format search effort data
  static formatSearchEffort(searchEffort) {
    const parts = [];

    if (searchEffort.timeSpent) {
      parts.push(`Search time: ${searchEffort.timeSpent} minutes`);
    }

    if (searchEffort.areaCovered) {
      parts.push(`Area covered: ~${searchEffort.areaCovered}m radius`);
    }

    if (searchEffort.actions && searchEffort.actions.length > 0) {
      const actionLabels = searchEffort.actions
        .map(actionId => this.SEARCH_EFFORT_OPTIONS.find(o => o.id === actionId)?.label)
        .filter(Boolean);
      if (actionLabels.length > 0) {
        parts.push(`Actions taken: ${actionLabels.join(', ')}`);
      }
    }

    return parts.join('\n');
  }
}

export default UTLNotesService;
```

### Voice Input Hook
```javascript
// src/hooks/useVoiceInput.js
import { useState, useCallback, useRef, useEffect } from 'react';

export function useVoiceInput(onTranscript) {
  const [isListening, setIsListening] = useState(false);
  const [interimTranscript, setInterimTranscript] = useState('');
  const [error, setError] = useState(null);
  const recognitionRef = useRef(null);

  useEffect(() => {
    // Check for browser support
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

    if (!SpeechRecognition) {
      setError('Voice input not supported on this device');
      return;
    }

    const recognition = new SpeechRecognition();
    recognition.continuous = true;
    recognition.interimResults = true;
    recognition.lang = 'en-US';

    recognition.onstart = () => {
      setIsListening(true);
      setError(null);
    };

    recognition.onend = () => {
      setIsListening(false);
    };

    recognition.onerror = (event) => {
      setIsListening(false);
      switch (event.error) {
        case 'no-speech':
          setError('No speech detected. Please try again.');
          break;
        case 'audio-capture':
          setError('No microphone found.');
          break;
        case 'not-allowed':
          setError('Microphone permission denied.');
          break;
        default:
          setError('Voice input error. Please try again.');
      }
    };

    recognition.onresult = (event) => {
      let interim = '';
      let final = '';

      for (let i = event.resultIndex; i < event.results.length; i++) {
        const transcript = event.results[i][0].transcript;
        if (event.results[i].isFinal) {
          final += transcript;
        } else {
          interim += transcript;
        }
      }

      setInterimTranscript(interim);
      if (final) {
        onTranscript(final);
      }
    };

    recognitionRef.current = recognition;

    return () => {
      if (recognitionRef.current) {
        recognitionRef.current.stop();
      }
    };
  }, [onTranscript]);

  const startListening = useCallback(() => {
    if (recognitionRef.current && !isListening) {
      try {
        recognitionRef.current.start();
      } catch (e) {
        // Already started
      }
    }
  }, [isListening]);

  const stopListening = useCallback(() => {
    if (recognitionRef.current && isListening) {
      recognitionRef.current.stop();
    }
    setInterimTranscript('');
  }, [isListening]);

  return {
    isListening,
    interimTranscript,
    error,
    startListening,
    stopListening,
    isSupported: !!recognitionRef.current
  };
}
```

### UTL Notes Form Component
```jsx
// src/components/mc-officer/UTLNotesForm.jsx
import React, { useState, useEffect, useCallback, useRef } from 'react';
import UTLNotesService from '../../services/UTLNotesService';
import { useVoiceInput } from '../../hooks/useVoiceInput';

function UTLNotesForm({ incidentId, utlReason, onSubmit, onCancel }) {
  const [notes, setNotes] = useState('');
  const [searchEffort, setSearchEffort] = useState({
    timeSpent: '',
    areaCovered: '',
    actions: []
  });
  const [validationErrors, setValidationErrors] = useState([]);
  const [isSaving, setIsSaving] = useState(false);
  const [lastSaved, setLastSaved] = useState(null);

  const autoSaveTimerRef = useRef(null);

  // Voice input
  const handleVoiceTranscript = useCallback((transcript) => {
    setNotes(prev => prev + ' ' + transcript);
  }, []);

  const {
    isListening,
    interimTranscript,
    error: voiceError,
    startListening,
    stopListening,
    isSupported: voiceSupported
  } = useVoiceInput(handleVoiceTranscript);

  // Load draft on mount
  useEffect(() => {
    async function loadDraft() {
      const draft = await UTLNotesService.getDraft(incidentId);
      if (draft) {
        setNotes(draft.notes || '');
        setSearchEffort(draft.searchEffort || {
          timeSpent: '',
          areaCovered: '',
          actions: []
        });
        setLastSaved(new Date(draft.timestamp));
      }
    }
    loadDraft();
  }, [incidentId]);

  // Auto-save effect
  useEffect(() => {
    if (autoSaveTimerRef.current) {
      clearTimeout(autoSaveTimerRef.current);
    }

    autoSaveTimerRef.current = setTimeout(async () => {
      if (notes || searchEffort.timeSpent || searchEffort.areaCovered || searchEffort.actions.length > 0) {
        await UTLNotesService.saveDraft(incidentId, { notes, searchEffort });
        setLastSaved(new Date());
      }
    }, UTLNotesService.AUTO_SAVE_INTERVAL);

    return () => {
      if (autoSaveTimerRef.current) {
        clearTimeout(autoSaveTimerRef.current);
      }
    };
  }, [notes, searchEffort, incidentId]);

  const handleNotesChange = (e) => {
    const value = e.target.value;
    if (value.length <= UTLNotesService.MAX_CHARACTERS) {
      setNotes(value);
      setValidationErrors([]);
    }
  };

  const handleTemplateClick = (templateId) => {
    const updatedNotes = UTLNotesService.applyTemplate(notes, templateId);
    if (updatedNotes.length <= UTLNotesService.MAX_CHARACTERS) {
      setNotes(updatedNotes);
    }
  };

  const handleSearchEffortChange = (field, value) => {
    setSearchEffort(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleActionToggle = (actionId) => {
    setSearchEffort(prev => ({
      ...prev,
      actions: prev.actions.includes(actionId)
        ? prev.actions.filter(a => a !== actionId)
        : [...prev.actions, actionId]
    }));
  };

  const handleSubmit = async () => {
    // Validate
    const validation = UTLNotesService.validateNotes(notes, utlReason);
    if (!validation.isValid) {
      setValidationErrors(validation.errors);
      return;
    }

    setIsSaving(true);
    try {
      // Format search effort for submission
      const formattedSearchEffort = UTLNotesService.formatSearchEffort(searchEffort);
      const fullNotes = formattedSearchEffort
        ? `${notes}\n\n--- Search Effort ---\n${formattedSearchEffort}`
        : notes;

      await onSubmit({
        notes: fullNotes,
        searchEffort
      });

      // Clear draft after successful submission
      await UTLNotesService.deleteDraft(incidentId);
    } catch (error) {
      setValidationErrors([error.message || 'Failed to submit. Please try again.']);
    } finally {
      setIsSaving(false);
    }
  };

  const isNotesRequired = ['OTHER', 'INACCESSIBLE'].includes(utlReason);
  const characterCount = notes.length;
  const remainingChars = UTLNotesService.MAX_CHARACTERS - characterCount;

  return (
    <div className="utl-notes-form">
      {/* UTL Reason Display */}
      <div className="utl-reason-display">
        <span className="label">Reason:</span>
        <span className="value">{utlReason}</span>
      </div>

      {/* Quick Templates */}
      <div className="quick-templates">
        <h4>Quick Templates</h4>
        <div className="template-chips">
          {UTLNotesService.QUICK_TEMPLATES.slice(0, 4).map(template => (
            <button
              key={template.id}
              className="template-chip"
              onClick={() => handleTemplateClick(template.id)}
              disabled={remainingChars < template.text.length}
            >
              {template.label}
            </button>
          ))}
        </div>
      </div>

      {/* Notes Input */}
      <div className="notes-input-container">
        <label htmlFor="utl-notes">
          Notes {isNotesRequired && <span className="required">*</span>}
        </label>
        <div className="textarea-wrapper">
          <textarea
            id="utl-notes"
            value={notes}
            onChange={handleNotesChange}
            placeholder="Describe your search efforts and why the dog couldn't be located..."
            rows={6}
            className={validationErrors.length > 0 ? 'error' : ''}
          />
          {voiceSupported && (
            <button
              className={`voice-button ${isListening ? 'listening' : ''}`}
              onClick={isListening ? stopListening : startListening}
              type="button"
              aria-label={isListening ? 'Stop voice input' : 'Start voice input'}
            >
              🎤
            </button>
          )}
        </div>

        {/* Voice Input Overlay */}
        {isListening && (
          <div className="voice-overlay">
            <div className="voice-indicator">
              <span className="mic-icon">🎤</span>
              <span className="status">Listening...</span>
              <div className="audio-visualizer">════════════════</div>
              {interimTranscript && (
                <p className="interim">"{interimTranscript}"</p>
              )}
              <div className="voice-actions">
                <button onClick={stopListening}>Done</button>
                <button onClick={() => { stopListening(); }}>Cancel</button>
              </div>
            </div>
          </div>
        )}

        {voiceError && (
          <p className="voice-error">{voiceError}</p>
        )}

        <div className="notes-footer">
          <span className={`char-count ${remainingChars < 50 ? 'warning' : ''}`}>
            {characterCount}/{UTLNotesService.MAX_CHARACTERS}
          </span>
          {lastSaved && (
            <span className="auto-save-indicator">
              Auto-saved {lastSaved.toLocaleTimeString()}
            </span>
          )}
        </div>
      </div>

      {/* Search Effort Section */}
      <div className="search-effort-section">
        <h4>Search Effort (Optional)</h4>

        <div className="effort-inputs">
          <div className="input-group">
            <label htmlFor="time-spent">Time spent searching:</label>
            <div className="input-with-unit">
              <input
                id="time-spent"
                type="number"
                min="1"
                max="180"
                value={searchEffort.timeSpent}
                onChange={(e) => handleSearchEffortChange('timeSpent', e.target.value)}
                placeholder="0"
              />
              <span className="unit">minutes</span>
            </div>
          </div>

          <div className="input-group">
            <label htmlFor="area-covered">Area covered (estimate):</label>
            <div className="input-with-unit">
              <input
                id="area-covered"
                type="number"
                min="10"
                max="1000"
                value={searchEffort.areaCovered}
                onChange={(e) => handleSearchEffortChange('areaCovered', e.target.value)}
                placeholder="0"
              />
              <span className="unit">meters radius</span>
            </div>
          </div>
        </div>

        <div className="effort-actions">
          {UTLNotesService.SEARCH_EFFORT_OPTIONS.map(option => (
            <label key={option.id} className="checkbox-label">
              <input
                type="checkbox"
                checked={searchEffort.actions.includes(option.id)}
                onChange={() => handleActionToggle(option.id)}
              />
              <span>{option.label}</span>
            </label>
          ))}
        </div>
      </div>

      {/* Validation Errors */}
      {validationErrors.length > 0 && (
        <div className="validation-errors">
          {validationErrors.map((error, index) => (
            <p key={index} className="error-message">{error}</p>
          ))}
        </div>
      )}

      {/* Submit Button */}
      <div className="form-actions">
        <button
          className="btn-secondary"
          onClick={onCancel}
          disabled={isSaving}
        >
          Back
        </button>
        <button
          className="btn-primary"
          onClick={handleSubmit}
          disabled={isSaving || (isNotesRequired && !notes.trim())}
        >
          {isSaving ? 'Submitting...' : 'Submit UTL Report'}
        </button>
      </div>
    </div>
  );
}

export default UTLNotesForm;
```

### Styles
```css
/* src/styles/utl-notes.css */

.utl-notes-form {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.utl-reason-display {
  padding: 12px;
  background: #f5f5f5;
  border-radius: 8px;
}

.utl-reason-display .label {
  color: #666;
  margin-right: 8px;
}

.utl-reason-display .value {
  font-weight: 600;
  color: #333;
}

.quick-templates h4 {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.template-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.template-chip {
  padding: 8px 12px;
  background: #e3f2fd;
  border: 1px solid #2196f3;
  border-radius: 16px;
  color: #1976d2;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.template-chip:hover {
  background: #bbdefb;
}

.template-chip:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.notes-input-container label {
  display: block;
  font-weight: 500;
  margin-bottom: 8px;
}

.notes-input-container .required {
  color: #d32f2f;
}

.textarea-wrapper {
  position: relative;
}

.textarea-wrapper textarea {
  width: 100%;
  padding: 12px;
  padding-right: 48px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  resize: vertical;
  min-height: 120px;
}

.textarea-wrapper textarea:focus {
  outline: none;
  border-color: #2196f3;
  box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.2);
}

.textarea-wrapper textarea.error {
  border-color: #d32f2f;
}

.voice-button {
  position: absolute;
  bottom: 12px;
  right: 12px;
  width: 36px;
  height: 36px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.2s;
}

.voice-button:hover {
  background: #e0e0e0;
}

.voice-button.listening {
  background: #ffebee;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.voice-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}

.voice-indicator {
  text-align: center;
  padding: 20px;
}

.voice-indicator .mic-icon {
  font-size: 32px;
  display: block;
  margin-bottom: 8px;
}

.voice-indicator .status {
  font-weight: 500;
  color: #333;
}

.audio-visualizer {
  color: #2196f3;
  letter-spacing: 2px;
  margin: 8px 0;
  animation: visualize 0.5s infinite alternate;
}

@keyframes visualize {
  from { opacity: 0.5; }
  to { opacity: 1; }
}

.voice-indicator .interim {
  font-style: italic;
  color: #666;
  margin: 12px 0;
}

.voice-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 16px;
}

.voice-actions button {
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.voice-error {
  color: #d32f2f;
  font-size: 12px;
  margin-top: 4px;
}

.notes-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
}

.char-count {
  font-size: 12px;
  color: #666;
}

.char-count.warning {
  color: #ff9800;
}

.auto-save-indicator {
  font-size: 11px;
  color: #4caf50;
}

.search-effort-section h4 {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}

.effort-inputs {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.input-group {
  flex: 1;
}

.input-group label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
}

.input-with-unit {
  display: flex;
  align-items: center;
  gap: 8px;
}

.input-with-unit input {
  width: 80px;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.input-with-unit .unit {
  font-size: 13px;
  color: #666;
}

.effort-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
}

.checkbox-label span {
  font-size: 14px;
}

.validation-errors {
  padding: 12px;
  background: #ffebee;
  border-radius: 8px;
}

.error-message {
  color: #d32f2f;
  font-size: 13px;
  margin: 0;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.form-actions .btn-secondary {
  flex: 1;
  padding: 14px;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
}

.form-actions .btn-primary {
  flex: 2;
  padding: 14px;
  background: #ff9800;
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}

.form-actions .btn-primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}
```

## PWA Considerations

### Offline Capabilities
- Voice-to-text uses device native API (works offline)
- Notes auto-save to IndexedDB every 10 seconds
- Drafts persist across app restarts
- Form data survives connection loss

### Performance
- Character count updates in real-time without lag
- Template application is instant
- Auto-save happens in background without blocking UI

### Accessibility
- Voice input provides hands-free note entry
- Clear character limits and validation feedback
- Proper ARIA labels on voice controls
- Sufficient touch targets for field operations

## Dependencies

- **idb**: IndexedDB wrapper for draft storage
- **Web Speech API**: Native browser voice recognition
- **MC-UTL-01**: UTL reason selection (provides reason code)

## Related Stories

- [MC-UTL-01](./MC-UTL-01.md) - Mark Unable to Locate (parent flow)
- [MC-CAP-02](./MC-CAP-02.md) - Add Resolution Notes (similar pattern)
- [MC-NOT-02](./MC-NOT-02.md) - Notify Teacher on UTL

## Notes

- Notes are critical for accountability and may be reviewed by supervisors
- Voice input significantly improves field usability
- Search effort tracking helps identify location accuracy issues
- Auto-save prevents data loss during field operations
