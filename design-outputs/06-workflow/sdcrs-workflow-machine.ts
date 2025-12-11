/**
 * SDCRS (Stray Dog Capture & Reporting System) - XState Workflow Machine
 *
 * This state machine represents the complete lifecycle of a dog report
 * from submission to resolution/rejection.
 *
 * Visualize at: https://stately.ai/viz
 *
 * Terminal States (with payout info):
 * - RESOLVED: Payout triggered (₹500)
 * - CAPTURED: Awaiting payout processing
 * - AUTO_REJECTED: No payout
 * - REJECTED: No payout
 * - DUPLICATE: No payout
 * - UNABLE_TO_LOCATE: No payout
 */

import { createMachine, assign } from 'xstate';

// Context type for the state machine
interface SDCRSContext {
  reportNumber: string;
  trackingId: string;
  reporterId: string;
  reporterType: 'TEACHER' | 'CITIZEN';
  tenantId: string;

  // Validation
  validationErrors: string[];
  gpsValidated: boolean;
  imageValidated: boolean;
  duplicateCheckPassed: boolean;

  // Assignment
  assignedOfficerId: string | null;
  assignedOfficerName: string | null;

  // Verification
  verifiedBy: string | null;
  verifiedAt: number | null;
  verificationRemarks: string | null;

  // Resolution
  resolvedBy: string | null;
  resolvedAt: number | null;
  resolutionType: string | null;
  resolutionRemarks: string | null;

  // Rejection
  rejectionReason: string | null;
  rejectionRemarks: string | null;

  // Payout
  payoutEligible: boolean;
  payoutAmount: number;
  payoutStatus: 'PENDING' | 'PROCESSED' | 'COMPLETED' | 'CAPPED' | null;
  demandId: string | null;
}

// Event types
type SDCRSEvent =
  | { type: 'SUBMIT'; reporterId: string; reporterType: 'TEACHER' | 'CITIZEN' }
  | { type: 'AUTO_VALIDATE_PASS' }
  | { type: 'AUTO_VALIDATE_FAIL'; errors: string[] }
  | { type: 'VERIFY'; verifierId: string; remarks?: string }
  | { type: 'REJECT'; verifierId: string; reason: string; remarks?: string }
  | { type: 'MARK_DUPLICATE'; verifierId: string; duplicateOf: string }
  | { type: 'ASSIGN_MC'; officerId: string; officerName: string; supervisorId: string }
  | { type: 'REASSIGN'; newOfficerId: string; newOfficerName: string; reason: string }
  | { type: 'START_FIELD_VISIT'; officerId: string }
  | { type: 'MARK_CAPTURED'; officerId: string; remarks?: string }
  | { type: 'MARK_UNABLE_TO_LOCATE'; officerId: string; remarks: string }
  | { type: 'PROCESS_PAYOUT' }
  | { type: 'PAYOUT_COMPLETED'; demandId: string }
  | { type: 'PAYOUT_CAPPED' };

// Initial context
const initialContext: SDCRSContext = {
  reportNumber: '',
  trackingId: '',
  reporterId: '',
  reporterType: 'TEACHER',
  tenantId: 'dj',

  validationErrors: [],
  gpsValidated: false,
  imageValidated: false,
  duplicateCheckPassed: false,

  assignedOfficerId: null,
  assignedOfficerName: null,

  verifiedBy: null,
  verifiedAt: null,
  verificationRemarks: null,

  resolvedBy: null,
  resolvedAt: null,
  resolutionType: null,
  resolutionRemarks: null,

  rejectionReason: null,
  rejectionRemarks: null,

  payoutEligible: false,
  payoutAmount: 500,
  payoutStatus: null,
  demandId: null,
};

/**
 * SDCRS Workflow State Machine
 */
export const sdcrsWorkflowMachine = createMachine({
  id: 'sdcrs-workflow',
  initial: 'idle',
  context: initialContext,

  states: {
    /**
     * Initial state - waiting for report submission
     */
    idle: {
      on: {
        SUBMIT: {
          target: 'pendingValidation',
          actions: assign({
            reporterId: (_, event) => event.reporterId,
            reporterType: (_, event) => event.reporterType,
          }),
        },
      },
      meta: {
        description: 'Waiting for report submission',
        actor: 'TEACHER / CITIZEN',
      },
    },

    /**
     * PENDING_VALIDATION - Automated validation in progress
     * System runs GPS check, image freshness, duplicate detection
     */
    pendingValidation: {
      entry: ['triggerAutoValidation', 'sendSubmissionNotification'],
      on: {
        AUTO_VALIDATE_PASS: {
          target: 'pendingVerification',
          actions: assign({
            gpsValidated: true,
            imageValidated: true,
            duplicateCheckPassed: true,
          }),
        },
        AUTO_VALIDATE_FAIL: {
          target: 'autoRejected',
          actions: assign({
            validationErrors: (_, event) => event.errors,
            rejectionReason: 'AUTO_VALIDATION_FAILED',
            rejectionRemarks: (_, event) => event.errors.join('; '),
          }),
        },
      },
      meta: {
        description: 'Automated validation in progress',
        actor: 'SYSTEM',
        checks: [
          'GPS consistency (EXIF vs submitted)',
          'Image freshness (< 48 hours)',
          'Duplicate detection (image hash)',
          'Tenant boundary validation',
        ],
      },
    },

    /**
     * AUTO_REJECTED - Terminal state (validation failed)
     * No payout eligible
     */
    autoRejected: {
      type: 'final',
      entry: ['sendRejectionNotification'],
      meta: {
        description: 'Automatically rejected by system validation',
        actor: 'SYSTEM',
        terminal: true,
        payoutEligible: false,
        reasons: [
          'GPS_MISMATCH',
          'IMAGE_TOO_OLD',
          'DUPLICATE_IMAGE',
          'INVALID_LOCATION',
        ],
      },
    },

    /**
     * PENDING_VERIFICATION - Awaiting human verification
     * Verifier reviews evidence and approves/rejects
     */
    pendingVerification: {
      on: {
        VERIFY: {
          target: 'verified',
          actions: assign({
            verifiedBy: (_, event) => event.verifierId,
            verifiedAt: () => Date.now(),
            verificationRemarks: (_, event) => event.remarks || null,
          }),
        },
        REJECT: {
          target: 'rejected',
          actions: assign({
            verifiedBy: (_, event) => event.verifierId,
            rejectionReason: (_, event) => event.reason,
            rejectionRemarks: (_, event) => event.remarks || null,
          }),
        },
        MARK_DUPLICATE: {
          target: 'duplicate',
          actions: assign({
            verifiedBy: (_, event) => event.verifierId,
            rejectionReason: 'DUPLICATE',
            rejectionRemarks: (_, event) => `Duplicate of ${event.duplicateOf}`,
          }),
        },
      },
      meta: {
        description: 'Awaiting verification by backend operator',
        actor: 'VERIFIER',
        actions: ['Review evidence', 'Verify location', 'Check for duplicates'],
      },
    },

    /**
     * REJECTED - Terminal state (manual rejection)
     * No payout eligible
     */
    rejected: {
      type: 'final',
      entry: ['sendRejectionNotification'],
      meta: {
        description: 'Rejected by verifier',
        actor: 'VERIFIER',
        terminal: true,
        payoutEligible: false,
        reasons: [
          'INVALID_EVIDENCE',
          'INAPPROPRIATE_CONTENT',
          'NOT_A_STRAY_DOG',
          'INSUFFICIENT_DETAILS',
        ],
      },
    },

    /**
     * DUPLICATE - Terminal state (duplicate report)
     * No payout eligible
     */
    duplicate: {
      type: 'final',
      entry: ['sendDuplicateNotification'],
      meta: {
        description: 'Marked as duplicate of existing report',
        actor: 'VERIFIER',
        terminal: true,
        payoutEligible: false,
      },
    },

    /**
     * VERIFIED - Report verified, ready for MC assignment
     */
    verified: {
      entry: ['sendVerificationNotification'],
      on: {
        ASSIGN_MC: {
          target: 'assigned',
          actions: assign({
            assignedOfficerId: (_, event) => event.officerId,
            assignedOfficerName: (_, event) => event.officerName,
          }),
        },
      },
      meta: {
        description: 'Verified and ready for MC officer assignment',
        actor: 'MC_SUPERVISOR',
        nextAction: 'Assign to Municipal Corporation officer',
      },
    },

    /**
     * ASSIGNED - MC Officer assigned, awaiting field visit
     */
    assigned: {
      entry: ['sendAssignmentNotification', 'notifyMCOfficer'],
      on: {
        START_FIELD_VISIT: {
          target: 'inProgress',
          guard: (context, event) =>
            context.assignedOfficerId === event.officerId,
        },
        REASSIGN: {
          target: 'assigned',
          actions: assign({
            assignedOfficerId: (_, event) => event.newOfficerId,
            assignedOfficerName: (_, event) => event.newOfficerName,
          }),
        },
      },
      meta: {
        description: 'MC Officer assigned, awaiting field visit',
        actor: 'MC_OFFICER',
        sla: '24 hours',
      },
    },

    /**
     * IN_PROGRESS - Field visit in progress
     */
    inProgress: {
      on: {
        MARK_CAPTURED: {
          target: 'captured',
          actions: assign({
            resolvedBy: (_, event) => event.officerId,
            resolvedAt: () => Date.now(),
            resolutionType: 'CAPTURED',
            resolutionRemarks: (_, event) => event.remarks || null,
            payoutEligible: true,
          }),
        },
        MARK_UNABLE_TO_LOCATE: {
          target: 'unableToLocate',
          actions: assign({
            resolvedBy: (_, event) => event.officerId,
            resolvedAt: () => Date.now(),
            resolutionType: 'UNABLE_TO_LOCATE',
            resolutionRemarks: (_, event) => event.remarks,
            payoutEligible: false,
          }),
        },
        REASSIGN: {
          target: 'assigned',
          actions: assign({
            assignedOfficerId: (_, event) => event.newOfficerId,
            assignedOfficerName: (_, event) => event.newOfficerName,
          }),
        },
      },
      meta: {
        description: 'Field visit in progress',
        actor: 'MC_OFFICER',
        activities: ['Locate dog', 'Capture dog', 'Upload evidence'],
      },
    },

    /**
     * UNABLE_TO_LOCATE - Terminal state (dog not found)
     * No payout eligible
     */
    unableToLocate: {
      type: 'final',
      entry: ['sendUnableToLocateNotification'],
      meta: {
        description: 'Dog could not be located at reported location',
        actor: 'MC_OFFICER',
        terminal: true,
        payoutEligible: false,
      },
    },

    /**
     * CAPTURED - Dog captured, payout processing
     */
    captured: {
      entry: ['sendCaptureNotification', 'triggerPayoutProcessing'],
      on: {
        PROCESS_PAYOUT: {
          target: 'resolved',
          actions: assign({
            payoutStatus: 'PROCESSED',
          }),
        },
        PAYOUT_CAPPED: {
          target: 'resolved',
          actions: assign({
            payoutStatus: 'CAPPED',
            payoutEligible: false,
          }),
        },
      },
      meta: {
        description: 'Dog captured, processing payout',
        actor: 'SYSTEM',
        payoutAmount: 500,
      },
    },

    /**
     * RESOLVED - Terminal state (successfully resolved)
     * Payout completed (if eligible)
     */
    resolved: {
      type: 'final',
      entry: ['sendResolutionNotification', 'sendPayoutNotification'],
      on: {
        PAYOUT_COMPLETED: {
          actions: assign({
            payoutStatus: 'COMPLETED',
            demandId: (_, event) => event.demandId,
          }),
        },
      },
      meta: {
        description: 'Report resolved, payout processed',
        actor: 'SYSTEM',
        terminal: true,
        payoutEligible: true,
      },
    },
  },
}, {
  actions: {
    // Validation actions
    triggerAutoValidation: () => {
      console.log('Triggering auto-validation via Kafka');
    },

    // Notification actions
    sendSubmissionNotification: (context) => {
      console.log(`SMS: Report ${context.reportNumber} submitted. Track at: ${context.trackingId}`);
    },
    sendVerificationNotification: (context) => {
      console.log(`SMS: Report ${context.reportNumber} has been verified`);
    },
    sendRejectionNotification: (context) => {
      console.log(`SMS: Report ${context.reportNumber} was rejected: ${context.rejectionReason}`);
    },
    sendDuplicateNotification: (context) => {
      console.log(`SMS: Report ${context.reportNumber} marked as duplicate`);
    },
    sendAssignmentNotification: (context) => {
      console.log(`SMS: MC Officer ${context.assignedOfficerName} assigned to report ${context.reportNumber}`);
    },
    notifyMCOfficer: (context) => {
      console.log(`Push notification to MC Officer ${context.assignedOfficerId}`);
    },
    sendCaptureNotification: (context) => {
      console.log(`SMS: Dog from report ${context.reportNumber} has been captured`);
    },
    sendUnableToLocateNotification: (context) => {
      console.log(`SMS: Dog from report ${context.reportNumber} could not be located`);
    },
    sendResolutionNotification: (context) => {
      console.log(`SMS: Report ${context.reportNumber} has been resolved`);
    },
    sendPayoutNotification: (context) => {
      if (context.payoutEligible) {
        console.log(`SMS: Payout of ₹${context.payoutAmount} initiated for report ${context.reportNumber}`);
      }
    },

    // Payout actions
    triggerPayoutProcessing: (context) => {
      console.log(`Triggering payout processing for teacher ${context.reporterId}`);
    },
  },
  guards: {
    isAssignedOfficer: (context, event) => {
      return context.assignedOfficerId === event.officerId;
    },
  },
});

/**
 * Simplified version for visualization (without TypeScript types)
 * Copy this to https://stately.ai/viz
 */
export const sdcrsWorkflowMachineSimple = createMachine({
  id: 'SDCRS Dog Report Workflow',
  initial: 'idle',

  states: {
    idle: {
      on: {
        SUBMIT: 'pendingValidation',
      },
      description: 'Waiting for report submission\nActor: TEACHER / CITIZEN',
    },

    pendingValidation: {
      on: {
        AUTO_VALIDATE_PASS: 'pendingVerification',
        AUTO_VALIDATE_FAIL: 'autoRejected',
      },
      description: 'Auto-validation in progress\nActor: SYSTEM\n\nChecks:\n- GPS consistency\n- Image freshness\n- Duplicate detection\n- Boundary validation',
    },

    autoRejected: {
      type: 'final',
      description: 'Auto-rejected by system\n\nNo payout\n\nReasons:\n- GPS mismatch\n- Old image\n- Duplicate\n- Invalid location',
    },

    pendingVerification: {
      on: {
        VERIFY: 'verified',
        REJECT: 'rejected',
        MARK_DUPLICATE: 'duplicate',
      },
      description: 'Awaiting human verification\nActor: VERIFIER\n\nActions:\n- Review evidence\n- Verify location\n- Check duplicates',
    },

    rejected: {
      type: 'final',
      description: 'Rejected by verifier\n\nNo payout',
    },

    duplicate: {
      type: 'final',
      description: 'Marked as duplicate\n\nNo payout',
    },

    verified: {
      on: {
        ASSIGN_MC: 'assigned',
      },
      description: 'Verified, ready for assignment\nActor: MC_SUPERVISOR',
    },

    assigned: {
      on: {
        START_FIELD_VISIT: 'inProgress',
        REASSIGN: 'assigned',
      },
      description: 'MC Officer assigned\nActor: MC_OFFICER\n\nSLA: 24 hours',
    },

    inProgress: {
      on: {
        MARK_CAPTURED: 'captured',
        MARK_UNABLE_TO_LOCATE: 'unableToLocate',
        REASSIGN: 'assigned',
      },
      description: 'Field visit in progress\nActor: MC_OFFICER',
    },

    unableToLocate: {
      type: 'final',
      description: 'Dog not found\n\nNo payout',
    },

    captured: {
      on: {
        PROCESS_PAYOUT: 'resolved',
        PAYOUT_CAPPED: 'resolved',
      },
      description: 'Dog captured!\nProcessing payout...\nActor: SYSTEM\n\nAmount: ₹500',
    },

    resolved: {
      type: 'final',
      description: 'Report resolved\n\nPayout: ₹500 (if eligible)\nMonthly cap: ₹5,000',
    },
  },
});

// Export for use
export default sdcrsWorkflowMachine;
