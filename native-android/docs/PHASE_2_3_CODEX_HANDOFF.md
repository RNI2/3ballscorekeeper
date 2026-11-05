# Phase 2.3 Codex Handoff

Attach or extract this package into the root of `RNI2/3ballscorekeeper` so paths become `native-android/docs/...`.

Start from `development/native-android-ui-polish` at `1e5e1d67c7cc5bdbc5a9bb852e299ff9406f17bc`. Create `development/native-android-ui-richness`.

Read `native-android/docs/UI_RICHNESS_SPEC.md` **and inspect all mockups** before editing source. The approved mockups are visual direction, not source of truth for scoring or navigation.

Implement premium contrast and tactile polish, prioritize Live Scoring, Setup, History, Settings; keep scoring, persistence, flags, schemas, web and Capacitor unchanged. Run all existing tests, parity checks, JSON checks, Android build and lint. Add before/after emulator screenshots if available; label host renders separately. Produce `native-android/docs/UI_RICHNESS_REPORT.md` and update the spec index if needed. Commit locally on the new branch, but **do not push or merge without approval**. Report branch, SHA, tests, screenshots and deviations.
