/**
 * SDCRS Workflow - XState Visualization
 *
 * Copy this entire file content to: https://stately.ai/viz
 *
 * This represents the complete dog report lifecycle:
 * SUBMIT → VALIDATION → VERIFICATION → ASSIGNMENT → FIELD VISIT → RESOLUTION
 */

import { createMachine } from 'xstate';

export const sdcrsMachine = createMachine({
  id: 'SDCRS_Workflow',
  initial: 'idle',
  description: 'Stray Dog Capture & Reporting System - Dog Report Lifecycle',

  states: {
    // ═══════════════════════════════════════════════════════════════════
    // SUBMISSION PHASE
    // ═══════════════════════════════════════════════════════════════════
    idle: {
      description: '📱 Waiting for Report\n\nActor: Teacher / Citizen\nAction: Submit dog sighting with photo & location',
      on: {
        SUBMIT: {
          target: 'pendingValidation',
          description: 'Teacher submits report with photo, GPS, description',
        },
      },
    },

    // ═══════════════════════════════════════════════════════════════════
    // VALIDATION PHASE (Automated)
    // ═══════════════════════════════════════════════════════════════════
    pendingValidation: {
      description: '🤖 Auto-Validation\n\nActor: SYSTEM\n\nChecks:\n• GPS consistency (EXIF vs submitted)\n• Image freshness (< 48 hours)\n• Duplicate detection (pHash)\n• Tenant boundary check',
      on: {
        AUTO_VALIDATE_PASS: {
          target: 'pendingVerification',
          description: 'All automated checks passed',
        },
        AUTO_VALIDATE_FAIL: {
          target: 'autoRejected',
          description: 'One or more checks failed',
        },
      },
    },

    autoRejected: {
      type: 'final',
      description: '❌ Auto-Rejected\n\nTerminal State\nNo Payout\n\nReasons:\n• GPS_MISMATCH\n• IMAGE_TOO_OLD\n• DUPLICATE_IMAGE\n• INVALID_LOCATION',
    },

    // ═══════════════════════════════════════════════════════════════════
    // VERIFICATION PHASE (Human Review)
    // ═══════════════════════════════════════════════════════════════════
    pendingVerification: {
      description: '👁️ Human Verification\n\nActor: VERIFIER\n\nReview:\n• Photo quality & content\n• Location validity\n• Duplicate check\n• Dog identification',
      on: {
        VERIFY: {
          target: 'verified',
          description: 'Verifier approves the report',
        },
        REJECT: {
          target: 'rejected',
          description: 'Verifier rejects (invalid evidence)',
        },
        MARK_DUPLICATE: {
          target: 'duplicate',
          description: 'Same dog already reported',
        },
      },
    },

    rejected: {
      type: 'final',
      description: '❌ Rejected\n\nTerminal State\nNo Payout\n\nReasons:\n• INVALID_EVIDENCE\n• NOT_A_STRAY_DOG\n• INAPPROPRIATE_CONTENT\n• INSUFFICIENT_DETAILS',
    },

    duplicate: {
      type: 'final',
      description: '📋 Duplicate\n\nTerminal State\nNo Payout\n\nLinked to original report',
    },

    // ═══════════════════════════════════════════════════════════════════
    // ASSIGNMENT PHASE
    // ═══════════════════════════════════════════════════════════════════
    verified: {
      description: '✅ Verified\n\nActor: MC_SUPERVISOR\n\nAction: Assign to Municipal Corporation officer for field visit',
      on: {
        ASSIGN_MC: {
          target: 'assigned',
          description: 'Supervisor assigns MC Officer',
        },
      },
    },

    assigned: {
      description: '👷 Officer Assigned\n\nActor: MC_OFFICER\nSLA: 24 hours\n\nOfficer notified via push notification',
      on: {
        START_FIELD_VISIT: {
          target: 'inProgress',
          description: 'Officer starts field visit',
        },
        REASSIGN: {
          target: 'assigned',
          description: 'Reassign to different officer',
        },
      },
    },

    // ═══════════════════════════════════════════════════════════════════
    // FIELD VISIT PHASE
    // ═══════════════════════════════════════════════════════════════════
    inProgress: {
      description: '🚗 Field Visit\n\nActor: MC_OFFICER\n\nActivities:\n• Navigate to location\n• Locate stray dog\n• Capture dog\n• Upload capture evidence',
      on: {
        MARK_CAPTURED: {
          target: 'captured',
          description: 'Dog successfully captured',
        },
        MARK_UNABLE_TO_LOCATE: {
          target: 'unableToLocate',
          description: 'Dog not found at location',
        },
        REASSIGN: {
          target: 'assigned',
          description: 'Reassign (officer unavailable)',
        },
      },
    },

    unableToLocate: {
      type: 'final',
      description: '🔍 Unable to Locate\n\nTerminal State\nNo Payout\n\nDog not found at reported location',
    },

    // ═══════════════════════════════════════════════════════════════════
    // RESOLUTION PHASE
    // ═══════════════════════════════════════════════════════════════════
    captured: {
      description: '🐕 Dog Captured!\n\nActor: SYSTEM\n\nProcessing payout...\n\nAmount: ₹500\nMonthly Cap: ₹5,000',
      on: {
        PROCESS_PAYOUT: {
          target: 'resolved',
          description: 'Payout initiated via Billing Service',
        },
        PAYOUT_CAPPED: {
          target: 'resolved',
          description: 'Monthly cap reached - no additional payout',
        },
      },
    },

    resolved: {
      type: 'final',
      description: '🎉 Resolved\n\nTerminal State\n\n✓ Dog captured\n✓ Payout processed (if eligible)\n✓ Teacher notified\n\nThank you for reporting!',
    },
  },
});

// ═══════════════════════════════════════════════════════════════════════════
// STATE METADATA (for documentation)
// ═══════════════════════════════════════════════════════════════════════════

export const stateMetadata = {
  // Payout eligibility by state
  payoutEligibility: {
    resolved: { eligible: true, amount: 500, currency: 'INR' },
    captured: { eligible: true, amount: 500, currency: 'INR' },
    autoRejected: { eligible: false },
    rejected: { eligible: false },
    duplicate: { eligible: false },
    unableToLocate: { eligible: false },
  },

  // Actors by state
  actors: {
    idle: ['TEACHER', 'CITIZEN'],
    pendingValidation: ['SYSTEM'],
    pendingVerification: ['VERIFIER'],
    verified: ['MC_SUPERVISOR'],
    assigned: ['MC_OFFICER'],
    inProgress: ['MC_OFFICER'],
    captured: ['SYSTEM'],
    resolved: ['SYSTEM'],
  },

  // SLAs
  sla: {
    pendingValidation: '5 minutes',
    pendingVerification: '4 hours',
    assigned: '24 hours',
    inProgress: '48 hours',
  },

  // Terminal states
  terminalStates: [
    'autoRejected',
    'rejected',
    'duplicate',
    'unableToLocate',
    'resolved',
  ],
};

// ═══════════════════════════════════════════════════════════════════════════
// WORKFLOW EVENTS (for documentation)
// ═══════════════════════════════════════════════════════════════════════════

export const workflowEvents = [
  {
    event: 'SUBMIT',
    from: 'idle',
    to: 'pendingValidation',
    actor: 'TEACHER',
    description: 'Submit new dog sighting report',
  },
  {
    event: 'AUTO_VALIDATE_PASS',
    from: 'pendingValidation',
    to: 'pendingVerification',
    actor: 'SYSTEM',
    description: 'Automated validation passed',
  },
  {
    event: 'AUTO_VALIDATE_FAIL',
    from: 'pendingValidation',
    to: 'autoRejected',
    actor: 'SYSTEM',
    description: 'Automated validation failed',
  },
  {
    event: 'VERIFY',
    from: 'pendingVerification',
    to: 'verified',
    actor: 'VERIFIER',
    description: 'Human verification approved',
  },
  {
    event: 'REJECT',
    from: 'pendingVerification',
    to: 'rejected',
    actor: 'VERIFIER',
    description: 'Human verification rejected',
  },
  {
    event: 'MARK_DUPLICATE',
    from: 'pendingVerification',
    to: 'duplicate',
    actor: 'VERIFIER',
    description: 'Marked as duplicate report',
  },
  {
    event: 'ASSIGN_MC',
    from: 'verified',
    to: 'assigned',
    actor: 'MC_SUPERVISOR',
    description: 'Assign MC officer for field visit',
  },
  {
    event: 'START_FIELD_VISIT',
    from: 'assigned',
    to: 'inProgress',
    actor: 'MC_OFFICER',
    description: 'Officer starts field visit',
  },
  {
    event: 'REASSIGN',
    from: ['assigned', 'inProgress'],
    to: 'assigned',
    actor: 'MC_SUPERVISOR',
    description: 'Reassign to different officer',
  },
  {
    event: 'MARK_CAPTURED',
    from: 'inProgress',
    to: 'captured',
    actor: 'MC_OFFICER',
    description: 'Dog successfully captured',
  },
  {
    event: 'MARK_UNABLE_TO_LOCATE',
    from: 'inProgress',
    to: 'unableToLocate',
    actor: 'MC_OFFICER',
    description: 'Dog not found at location',
  },
  {
    event: 'PROCESS_PAYOUT',
    from: 'captured',
    to: 'resolved',
    actor: 'SYSTEM',
    description: 'Payout processing initiated',
  },
  {
    event: 'PAYOUT_CAPPED',
    from: 'captured',
    to: 'resolved',
    actor: 'SYSTEM',
    description: 'Monthly cap exceeded, no payout',
  },
];

export default sdcrsMachine;
