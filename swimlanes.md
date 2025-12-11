# Process Workflow & Swimlanes

This document provides the visual workflow representations for the Stray Dog Capture & Reporting System (SDCRS).

---

## Workflow Diagrams

### Swimlane Diagram (with UPI Payout)

**6-lane workflow:** Teacher → System → Verifier → MC Officer → UPI Adapter → Treasury

![Swimlane Diagram](./design-outputs/assets/SDCRS_Swimlane_UPI.png)

**Source:** [SDCRS_Swimlane_UPI.puml](./design-outputs/06-workflow/SDCRS_Swimlane_UPI.puml)

---

### State Machine Diagram

![State Machine Diagram](./design-outputs/assets/SDCRS_StateMachine_UPI.png)

**Source:** [SDCRS_StateMachine_UPI.puml](./design-outputs/06-workflow/SDCRS_StateMachine_UPI.puml)

---

### Interactive Visualization

For interactive state machine exploration, copy the contents of [sdcrs-workflow-viz-upi.ts](./design-outputs/06-workflow/sdcrs-workflow-viz-upi.ts) into [stately.ai/viz](https://stately.ai/viz).

---

## Workflow States

```
null (Start)
  └── SUBMIT → PENDING_VALIDATION
                  ├── AUTO_VALIDATE_FAIL → AUTO_REJECTED (terminal)
                  └── AUTO_VALIDATE_PASS → PENDING_VERIFICATION
                                              ├── REJECT → REJECTED (terminal)
                                              ├── MARK_DUPLICATE → DUPLICATE (terminal)
                                              └── VERIFY → VERIFIED
                                                            └── ASSIGN → ASSIGNED
                                                                          └── START_FIELD_VISIT → IN_PROGRESS
                                                                                                    ├── MARK_UNABLE_TO_LOCATE → UNABLE_TO_LOCATE (terminal)
                                                                                                    └── MARK_CAPTURED → CAPTURED
                                                                                                                          └── INITIATE_PAYOUT → PAYOUT_PENDING
                                                                                                                                                   ├── PAYOUT_SUCCESS → RESOLVED
                                                                                                                                                   └── PAYOUT_FAILED → PAYOUT_FAILED
                                                                                                                                                                         └── RETRY/MANUAL → RESOLVED
```

---

## Payout Rule

**Key Business Rule:** Teachers receive payout ONLY after Municipal Corporation successfully captures/resolves the reported dog.

| Terminal State | Payout? |
|----------------|---------|
| AUTO_REJECTED | No |
| REJECTED | No |
| DUPLICATE | No |
| UNABLE_TO_LOCATE | No |
| PAYOUT_FAILED | Pending retry |
| RESOLVED | Yes (₹500) |

---

## Related Documents

- [Process Workflow Documentation](./design-outputs/01-process-workflow-upi.md)
- [Workflow Configuration (JSON)](./design-outputs/03-configs/workflow/BusinessService-with-UPI.json)
- [Sequence Diagrams](./design-outputs/05-sequence-diagrams.md)
