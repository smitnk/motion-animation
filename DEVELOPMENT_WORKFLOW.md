# Development Workflow

## Roles

| Role | Responsibility |
|---|---|
| You | Product decisions, priorities, real-device testing |
| ChatGPT | Architecture, research, specifications, review, context |
| Codex | Implementation, automated tests, Android builds, bug fixing |
| Google AI Studio | Final UI/UX creation and emulator-based refinement |

## One feature from idea to release

### Step 1 — Define
You provide:
- feature name
- what it should do
- optional reference/screenshot
- priority

ChatGPT converts this into:
- behavior specification
- UI/interaction requirements
- technical approach
- acceptance criteria
- open-source reuse options, if applicable

### Step 2 — Prepare
Codex:
1. Updates from the correct base branch.
2. Creates a feature branch.
3. Reads `CODEX_CONTEXT.md` and relevant source files.
4. Checks existing implementations before adding duplicate systems.

### Step 3 — Implement
Codex:
1. Implements the smallest coherent feature.
2. Keeps existing behavior intact.
3. Adds or updates tests.
4. Updates documentation if needed.

### Step 4 — Verify
Codex:
1. Runs unit tests.
2. Runs the Android debug build.
3. Checks the changed feature for compile/runtime-risk issues.
4. Opens a PR with a concise summary and test evidence.

### Step 5 — Review
ChatGPT:
1. Reviews the changed files/diff.
2. Checks architecture and integration.
3. Checks license compliance for reused code.
4. Reviews CI results.
5. Classifies the result **NEW**, **STUCK**, or **PASSED**.
6. Requests fixes when evidence is insufficient.

### Step 6 — Integrate
After review:
- Merge the PR only when explicitly approved.
- Keep `main` as the known-good baseline.
- Start the next feature from the updated baseline.

### Step 7 — Visual creation/refinement
Google AI Studio:
1. Imports/syncs the latest GitHub project.
2. Runs the Android emulator.
3. Checks layout, canvas visibility, gestures, controls and animation workflow.
4. Refines UI/UX and implementation.
5. Syncs meaningful changes back to GitHub.

### Step 8 — Real-device acceptance
You install the latest APK and test:
- drawing
- touch gestures
- selection
- transforms
- timeline
- playback
- export
- save/load
- performance

Your observations become the next development ticket.

## Open-source reuse rule

Before copying code:
1. Identify the repository.
2. Verify its license.
3. If compatible, reuse and modify it while preserving required notices.
4. If licensing creates a major restriction, use the repository only as a reference and implement the feature independently.
5. Record reused/reference projects in `THIRD_PARTY_NOTICES.md`.

## Critical rule

Codex and Google AI Studio should not simultaneously modify the same branch. GitHub remains the shared source of truth, and each substantial change should have a traceable commit/PR.
