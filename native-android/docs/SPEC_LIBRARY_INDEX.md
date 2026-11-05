# 3 Ball Scorekeeper | Specification Library Index

**Owner:** No Detours Studio LLC  
**Proposed location:** `native-android/docs/SPEC_LIBRARY_INDEX.md`  
**Purpose:** Durable catalog of product, architectural, data and UI specifications for Android and iOS coordination.

| Document | Role | Status |
|---|---|---|
| `SCORING_RULES.md` | Source-of-truth gameplay rules and Master safeguards | Existing |
| `ARCHITECTURE.md` | Native modules, persistence, recovery | Existing |
| `IOS_DATA_CONTRACT.md` | Platform-neutral cross-platform record structure | Existing |
| `contract/game-record-v1.schema.json` | Machine-readable record schema | Existing |
| `UI_DESIGN_SPEC.md` | Phase 2.1 native screen layout and web visual identity | Existing |
| `UI_POLISH_SPEC.md` | Phase 2.2 tactile feedback and billiards-themed design | Existing |
| `UI_RICHNESS_SPEC.md` | Phase 2.3 richer contrast, semantic accents and premium material hierarchy | Approved v1.1 mockup references; add on Phase 2.3 branch |
| `UI_RICHNESS_REPORT.md` | Phase 2.3 implementation and visual testing evidence | Pending Codex implementation |

## Library rules

1. Keep approved specifications in `native-android/docs/` with version, status, owner, source branch and acceptance criteria.
2. Preserve previously approved specifications for traceability. If later design decisions supersede an older visual rule, document the supersession explicitly.
3. Scoring rules and data contracts override visual implementation preferences when behavior or records are at stake.
4. Codex may implement a spec and report test evidence, but only the owner can approve actual emulator appearance.
5. Keep web and Capacitor deployments intact, and develop on dedicated branches before merge.
6. Share platform-neutral design and contract artifacts with the independent iOS developer rather than treating Android source files as the iOS specification.

**Next checkpoint:** Implement Phase 2.3 in `development/native-android-ui-richness`, review the resulting Android emulator screens, and withhold merge until visual sign-off.

## Approved Phase 2.3 visual references

The five images under [`mockups/`](mockups/) accompany `UI_RICHNESS_SPEC.md` v1.1. Treat PNGs as approved visual direction, not authoritative scoring examples. Preserve this package together for both Android and future iOS design handoff.

| Asset | Status |
|---|---|
| `mockups/approved-three-screen-reference.png` | Approved visual direction |
| `mockups/game-setup.png` | Approved visual direction |
| `mockups/live-scoring.png` | Approved visual direction |
| `mockups/history-results.png` | Approved visual direction |
| `mockups/live-scoring-detailed-reference.png` | Approved visual direction |
