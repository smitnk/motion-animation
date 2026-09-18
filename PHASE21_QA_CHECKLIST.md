# MotionCanvas Phase 21 QA Checklist

## Automated checks
- Debug APK build
- JVM unit tests
- Unsigned release APK build
- Debug APK artifact upload
- Unsigned release APK artifact upload

## Android device smoke test
Install the generated APK on a physical Android device and verify:
1. App launches without crashing.
2. Brush creates strokes on the canvas.
3. Eraser removes strokes.
4. Undo/redo works.
5. Layer visibility and opacity work.
6. Timeline frame selection works.
7. Frame playback works at the selected FPS.
8. Onion skin can be toggled.
9. Selection/transform works.
10. Grid and snapping controls work.
11. PNG export creates a readable image.
12. GIF export creates a playable animation.
13. Project save creates a readable project archive.
14. Saved project can be loaded and restores vector/raster content.
15. App survives creating, switching, and deleting multiple frames.

## Release gate
Do not label the release build production-ready until both automated CI and physical-device smoke testing pass.
