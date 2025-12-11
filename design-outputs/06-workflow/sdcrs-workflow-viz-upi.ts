/**
 * SDCRS Workflow - XState Visualization with UPI Payout States
 *
 * Copy this entire file content to: https://stately.ai/viz
 *
 * This represents the complete dog report lifecycle with UPI payout:
 * SUBMIT → VALIDATION → VERIFICATION → ASSIGNMENT → FIELD VISIT → UPI PAYOUT → RESOLUTION
 */

import { createMachine } from 'xstate';

export const sdcrsUpiMachine = createMachine({
  id: 'SDCRS_Workflow_UPI',
  initial: 'idle',
  description: 'Stray Dog Capture & Reporting System - Dog Report Lifecycle with UPI Payout',

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
      description: '🤖 Auto-Validation\n\nActor: SYSTEM\nSLA: 5 minutes\n\nChecks:\n• GPS consistency (EXIF vs submitted)\n• Image freshness (< 48 hours)\n• Duplicate detection (pHash)\n• Tenant boundary check',
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
      description: '👁️ Human Verification\n\nActor: VERIFIER\nSLA: 4 hours\n\nReview:\n• Photo quality & content\n• Location validity\n• Duplicate check\n• Dog identification',
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
        COMMENT: {
          target: 'pendingVerification',
          description: 'Teacher adds clarifying comment',
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
      description: '✅ Verified\n\nActor: MC_SUPERVISOR\nSLA: 1 hour\n\nAction: Assign to Municipal Corporation officer for field visit',
      on: {
        ASSIGN: {
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
          target: 'verified',
          description: 'Reassign to different officer',
        },
      },
    },

    // ═══════════════════════════════════════════════════════════════════
    // FIELD VISIT PHASE
    // ═══════════════════════════════════════════════════════════════════
    inProgress: {
      description: '🚗 Field Visit\n\nActor: MC_OFFICER\nSLA: 24 hours\n\nActivities:\n• Navigate to location\n• Locate stray dog\n• Capture dog\n• Upload capture evidence',
      on: {
        MARK_CAPTURED: {
          target: 'captured',
          description: 'Dog successfully captured',
        },
        MARK_UNABLE_TO_LOCATE: {
          target: 'unableToLocate',
          description: 'Dog not found at location',
        },
      },
    },

    unableToLocate: {
      type: 'final',
      description: '🔍 Unable to Locate\n\nTerminal State\nNo Payout\n\nDog not found at reported location',
    },

    // ═══════════════════════════════════════════════════════════════════
    // UPI PAYOUT PHASE (NEW)
    // ═══════════════════════════════════════════════════════════════════
    captured: {
      description: '🐕 Dog Captured!\n\nActor: SYSTEM\nSLA: 1 hour\n\nSystem auto-triggers UPI payout\n\nPush to Kafka: upi-payout-create',
      on: {
        INITIATE_PAYOUT: {
          target: 'payoutPending',
          description: 'System initiates UPI payout',
        },
      },
    },

    payoutPending: {
      description: '💳 UPI Payout Pending\n\nActor: UPI_ADAPTER\nSLA: 24 hours\n\nProcessing:\n• Create Razorpay Contact\n• Create Fund Account (VPA)\n• Create Payout\n• Await webhook callback',
      on: {
        PAYOUT_SUCCESS: {
          target: 'resolved',
          description: 'UPI payment successful (UTR received)',
        },
        PAYOUT_FAILED: {
          target: 'payoutFailed',
          description: 'UPI payment failed',
        },
      },
    },

    payoutFailed: {
      description: '⚠️ Payout Failed\n\nActor: STATE_ADMIN / SYSTEM\n\nReasons:\n• VPA invalid\n• Bank error\n• Insufficient balance\n• Network timeout\n\nRetries: Max 3',
      on: {
        RETRY_PAYOUT: {
          target: 'payoutPending',
          description: 'Retry payment (auto or manual)',
        },
        MANUAL_RESOLVE: {
          target: 'resolved',
          description: 'Admin marks resolved after offline payment',
        },
      },
    },

    // ═══════════════════════════════════════════════════════════════════
    // RESOLUTION PHASE
    // ═══════════════════════════════════════════════════════════════════
    resolved: {
      description: '🎉 Resolved\n\nTerminal State\n\n✓ Dog captured\n✓ UPI Payout complete\n✓ UTR recorded\n✓ Teacher notified\n\nAmount: ₹500',
      on: {
        RATE: {
          target: 'closed',
          description: 'Teacher provides feedback',
        },
      },
    },

    closed: {
      type: 'final',
      description: '📦 Closed\n\nFinal State\n\nFeedback received\nReport archived',
    },
  },
});

// ═══════════════════════════════════════════════════════════════════════════
// STATE METADATA (for documentation)
// ═══════════════════════════════════════════════════════════════════════════

export const stateMetadata = {
  // Payout eligibility by state
  payoutEligibility: {
    resolved: { eligible: true, amount: 500, currency: 'INR', method: 'UPI' },
    closed: { eligible: true, amount: 500, currency: 'INR', method: 'UPI' },
    captured: { eligible: true, amount: 500, currency: 'INR', method: 'UPI', note: 'Pending payout' },
    payoutPending: { eligible: true, amount: 500, currency: 'INR', method: 'UPI', note: 'Processing' },
    payoutFailed: { eligible: true, amount: 500, currency: 'INR', method: 'UPI', note: 'Retry pending' },
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
    payoutPending: ['UPI_ADAPTER', 'SYSTEM'],
    payoutFailed: ['STATE_ADMIN', 'SYSTEM'],
    resolved: ['SYSTEM'],
    closed: ['TEACHER'],
  },

  // SLAs (in milliseconds)
  sla: {
    pendingValidation: { duration: 300000, display: '5 minutes' },      // 5 min
    pendingVerification: { duration: 14400000, display: '4 hours' },    // 4 hours
    verified: { duration: 3600000, display: '1 hour' },                  // 1 hour
    assigned: { duration: 86400000, display: '24 hours' },               // 24 hours
    inProgress: { duration: 86400000, display: '24 hours' },             // 24 hours
    captured: { duration: 3600000, display: '1 hour' },                  // 1 hour
    payoutPending: { duration: 86400000, display: '24 hours' },          // 24 hours
  },

  // Terminal states
  terminalStates: [
    'autoRejected',
    'rejected',
    'duplicate',
    'unableToLocate',
    'resolved',
    'closed',
  ],

  // States with payout processing
  payoutStates: [
    'captured',
    'payoutPending',
    'payoutFailed',
    'resolved',
  ],
};

// ═══════════════════════════════════════════════════════════════════════════
// WORKFLOW EVENTS (for documentation)
// ═══════════════════════════════════════════════════════════════════════════

export const workflowEvents = [
  // Submission
  {
    event: 'SUBMIT',
    from: 'idle',
    to: 'pendingValidation',
    actor: 'TEACHER',
    description: 'Submit new dog sighting report',
  },
  // Validation
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
  // Verification
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
    event: 'COMMENT',
    from: 'pendingVerification',
    to: 'pendingVerification',
    actor: 'TEACHER',
    description: 'Teacher adds clarifying comment',
  },
  // Assignment
  {
    event: 'ASSIGN',
    from: 'verified',
    to: 'assigned',
    actor: 'MC_SUPERVISOR',
    description: 'Assign MC officer for field visit',
  },
  {
    event: 'REASSIGN',
    from: 'assigned',
    to: 'verified',
    actor: 'MC_SUPERVISOR',
    description: 'Reassign to different officer',
  },
  // Field Visit
  {
    event: 'START_FIELD_VISIT',
    from: 'assigned',
    to: 'inProgress',
    actor: 'MC_OFFICER',
    description: 'Officer starts field visit',
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
  // UPI Payout (NEW)
  {
    event: 'INITIATE_PAYOUT',
    from: 'captured',
    to: 'payoutPending',
    actor: 'SYSTEM',
    description: 'System initiates UPI payout via Kafka',
  },
  {
    event: 'PAYOUT_SUCCESS',
    from: 'payoutPending',
    to: 'resolved',
    actor: 'SYSTEM',
    description: 'UPI payment successful, UTR received',
  },
  {
    event: 'PAYOUT_FAILED',
    from: 'payoutPending',
    to: 'payoutFailed',
    actor: 'SYSTEM',
    description: 'UPI payment failed',
  },
  {
    event: 'RETRY_PAYOUT',
    from: 'payoutFailed',
    to: 'payoutPending',
    actor: ['SYSTEM', 'STATE_ADMIN'],
    description: 'Retry UPI payment',
  },
  {
    event: 'MANUAL_RESOLVE',
    from: 'payoutFailed',
    to: 'resolved',
    actor: 'STATE_ADMIN',
    description: 'Admin marks resolved after offline payment',
  },
  // Feedback
  {
    event: 'RATE',
    from: 'resolved',
    to: 'closed',
    actor: 'TEACHER',
    description: 'Teacher provides feedback',
  },
];

// ═══════════════════════════════════════════════════════════════════════════
// UPI PAYOUT CONFIGURATION
// ═══════════════════════════════════════════════════════════════════════════

export const upiPayoutConfig = {
  provider: 'RAZORPAY',
  mode: 'UPI',
  amount: 500,
  currency: 'INR',
  maxRetries: 3,
  kafkaTopics: {
    create: 'upi-payout-create',
    status: 'upi-payout-status',
    callback: 'sdcrs-payout-callback',
    persist: 'upi-payout-persist',
  },
  webhookEvents: [
    'payout.processed',
    'payout.failed',
    'payout.reversed',
  ],
};

export default sdcrsUpiMachine;
