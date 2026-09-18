# Motion Animation — Build Roadmap

## Product goal

Build a production-quality Android 2D drawing and animation application, inspired by the workflow of professional mobile animation/drawing tools while keeping the implementation maintainable and license-compliant.

## Team workflow

### 1. Product owner — You
- Decide the feature and priority.
- Test every meaningful milestone on a real Android device.
- Report visible bugs, usability problems, and missing behavior.
- Do not need to write code.

### 2. Architecture / second brain — ChatGPT
- Convert product ideas into technical specifications.
- Research reusable open-source implementations and verify licenses.
- Break large features into coherent implementation tasks.
- Review code changes and CI results.
- Identify regressions and integration risks.
- Maintain project context and documentation.
- Prepare implementation/test instructions for Codex and refinement instructions for Google AI Studio.

### 3. Developer / tester — Codex
- Implement the approved feature on a dedicated branch.
- Reuse compatible open-source code when the license permits it and preserve required notices.
- Otherwise implement original code using the reference only as guidance.
- Run unit tests and Android builds.
- Fix compiler errors and regressions.
- Open a PR instead of directly changing `main` for substantial work.

### 4. Final creator / visual refinement — Google AI Studio
- Import the GitHub project.
- Use the Android emulator to inspect the real UI and interaction flow.
- Refine UI, UX, touch interactions, and visual polish.
- Continue implementation where appropriate.
- Sync changes through GitHub rather than creating an unrelated copy of the project.

## Golden development loop

```
IDEA
  ↓
You define what you want
  ↓
ChatGPT writes the specification + acceptance criteria
  ↓
Codex implements on a feature branch
  ↓
Codex runs tests + Android build
  ↓
GitHub PR
  ↓
ChatGPT reviews diff + CI
  ↓
Fix/revise if required
  ↓
Merge to main after verification
  ↓
Google AI Studio imports the updated main branch
  ↓
Emulator/device visual testing + UX refinement
  ↓
You test on your Android phone
  ↓
Feedback becomes the next feature
```

## Branch policy

- `main` = verified project baseline.
- `feature/*` = substantial feature development.
- `fix/*` = focused bug fixes.
- Avoid parallel edits to the same feature from Codex and Google AI Studio.
- Do not merge automatically after a PR is created.
- Before starting a new feature, use the latest verified `main` unless a feature explicitly depends on another branch.
- Keep commits coherent and easy to review.

## Definition of done

A feature is not considered complete until:

- [ ] It compiles.
- [ ] Relevant unit tests pass.
- [ ] Android debug build passes in CI.
- [ ] No new compiler warnings/errors hide a functional problem.
- [ ] Existing drawing/animation behavior still works.
- [ ] Touch interaction has been tested on Android.
- [ ] UI does not hide the canvas or timeline.
- [ ] Required third-party notices/licenses are preserved.
- [ ] The feature is documented when it changes user behavior or architecture.
- [ ] The PR has been reviewed before merge.

## Build phases

### Phase 0 — Project foundation
- [ ] Gradle wrapper
- [ ] `.gitignore`
- [ ] Unit-test foundation
- [ ] Project documentation
- [ ] Stable CI build

### Phase 1 — Core drawing studio
- [ ] Canvas/artboard
- [ ] Pencil, pen, marker, airbrush
- [ ] Eraser
- [ ] Brush controls
- [ ] Color picker
- [ ] Layers
- [ ] Undo/redo
- [ ] Save/load project

### Phase 2 — Professional drawing tools
- [ ] Line, rectangle, ellipse and quick shapes
- [ ] Fill
- [ ] Selection and lasso
- [ ] Transform
- [ ] Flip
- [ ] Node/Bézier editing
- [ ] Symmetry
- [ ] Grid and guides
- [ ] Snapping
- [ ] Ruler

### Phase 3 — Animation
- [ ] Frame creation
- [ ] Hold frames
- [ ] Timeline
- [ ] FPS control
- [ ] Playback
- [ ] Onion skin
- [ ] Loop modes
- [ ] Animation editing workflow
- [ ] PNG/GIF export

### Phase 4 — Advanced animation
- [ ] Multi-joint puppet rigging
- [ ] Weight painting
- [ ] Sculpt tools
- [ ] Advanced transform/pivot behavior
- [ ] Better Bézier/node editing
- [ ] Real clipping/masking workflow
- [ ] Advanced perspective interaction

### Phase 5 — Production polish
- [ ] Performance profiling
- [ ] Large-canvas stability
- [ ] Memory management
- [ ] Gesture conflict handling
- [ ] Accessibility basics
- [ ] Crash/error handling
- [ ] Project compatibility/versioning
- [ ] Export reliability
- [ ] App icon, branding and release configuration

## Verification labels

Every development checkpoint should be reported as one of:

- **NEW** — not implemented or not yet verified.
- **STUCK** — implementation exists but is blocked by a known issue.
- **PASSED** — implementation/build/test evidence confirms the checkpoint.

Never treat a successful compile as proof that the runtime UI is correct.

## Current baseline

The repository already contains the Compose Android foundation, drawing engines, animation/timeline foundation, layers, selection/transform systems, guides/snapping/ruler foundations, export/save-load paths, and CI configuration inherited from the earlier MotionCanvas development line.

The next objective is to make this repository a clean, reproducible Android project before expanding the feature set.
