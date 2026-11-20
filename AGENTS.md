# AGENTS.md

Context for AI coding agents working in **QuickDraw**. This file describes how the pieces fit together and the contracts that method signatures don't show. It deliberately does not mirror the API: read the source for exact classes and methods.

## Project

QuickDraw (`ca.bael.QDraw`) is a pure-Java 21 software rasterizer: programmable vertex/fragment shaders, perspective-correct rasterization, near-plane clipping, G-buffer/deferred support, and multi-threaded geometry and raster stages, with no external runtime dependencies. It is intended as the graphics backend for a toy flight simulator.

## Build, test, run

Always use the Gradle wrapper at the repository root.

| Task | Command |
| :--- | :--- |
| Build everything and run tests | `./gradlew build` |
| Compile the library only | `./gradlew :lib:compileJava` |
| Run unit tests | `./gradlew :lib:test` |
| Demos | `./gradlew :demos:<sceneview\|mascot\|deferred\|profile\|make-fuzz-texture>:run` |

All demos except `make-fuzz-texture` open a Swing window and throw `HeadlessException` without a display. `:lib:test` is headless-safe.

## Layout

- `lib/`: core library in `ca.bael.QDraw`, the AWT/Swing bridge in `ca.bael.QDraw.awt`, unit tests in `lib/src/test/java/ca/bael/QDraw/Expects.java` (JUnit Jupiter).
- `demos/`: one `application` subproject per demo. `sceneview` (interactive viewer), `mascot` (visual test scenes), `deferred` (G-buffer and lighting pass), `profile` (texture storage benchmark), `make-fuzz-texture` (headless texture tool).

## How it fits together

A frame: bind state and slots on a `QViewer` (attribute and index buffers, uniforms, textures, projection, raster toggles), then `draw()`/`drawMesh()` queues a snapshot of that state. `flush()` runs geometry (vertex shading, culling, near clipping, projection) in parallel across draws, then rasterization (depth test, fragment shading, blending, G-buffer writes) in parallel across row strips of the `QRenderBuffer` target. The target's color `QImage` is then handed to a `QPresenter`.

Roles:
- `QViewer`: pipeline front end and per-draw state. Package-private classes (`GeometryWorker`, `RasterWorker`, `TriangleClipper`, ...) implement the stages.
- `QShader`: base class for shaders; declares requirements and receives vertex/fragment contexts.
- `QProjection`: camera frustum; owns projection and unprojection.
- `QAttribBuffer`, `QIndexBuffer`, `QMesh`: vertex data. A mesh is one index buffer plus position/UV/normal buffers.
- `QSampleable` and its subclasses: `QTexture` (chunked, for sampling), `QMipTexture` (mip chain), `QImage` (linear pixels, for presenting and I/O), `QRenderBuffer` (a color `QImage` plus depth and optional G-buffers).
- `QUniform`: values bindable to uniform slots. Math types, `QLights`, `QMaterial`, `QScalar`, `QFloats`, `QProjection`, or user-defined records.
- `QPresenter` and `ca.bael.QDraw.awt`: getting frames on screen (Swing canvas/panel/window, zero-copy `BufferedImage` views).
- Math: `QVector3`, `QMatrix4x4`, `QQuaternion`, `QColor`.

## Contracts

### Deferred execution and capture
- `draw()` checks shader requirements immediately but renders nothing. Work happens in `flush()`, and shading errors surface there. Flush before reading or presenting a target.
- A queued draw snapshots viewer state. Uniforms are copied via `snapshot()` (`@QImmutable` types return themselves) and may be changed right after queuing. Types marked `@QReadDuringFlush` (textures, attribute and index buffers, meshes, shaders) are captured by reference: do not modify them until `flush()` returns.
- `drawMesh()` clears texture, uniform and attribute/index slots after queuing, so re-bind them before every call. Other state (render mode, shader, projection, raster toggles) persists.
- `clearFrame()` is deferred to the next `flush()`. `setRenderTarget()` flushes first, so earlier draws and a pending clear apply to the old target.
- `QPresenter.present()` is synchronous: the image may be reused once it returns.

### Shaders and threading
- `beginDraw`, `vertexShader` and `fragmentShader` run concurrently on one shader instance. Shaders must not write their own fields; per-draw values go in the `QDrawData` returned by `beginDraw`.
- Context-provided objects (`position()`, `color()`, `scratchVec3(i)`) are reused between calls: `set` them before reading and never keep them after returning.
- Every output and G-buffer slot a shader writes must be declared in `requirements()`, and every declared output must be written for every vertex. Uniform requirements must name a class, and the bound value must be an instance of it.
- Vertex results are cached per vertex index within a draw, so vertex shaders must be deterministic in their attributes and uniforms.
- Fragments with alpha 0 are discarded: no color, depth or G-buffer write.

### Conventions
- The camera looks down −Z; visible geometry has `z < -near`. Lights and shading positions are in view space.
- Screen and texture coordinates have `(0, 0)` at the bottom-left. Image memory is still stored top row first.
- Depth stores `1/z` (negative; `0` means cleared/background); smaller is closer.
- Colors are packed, non-premultiplied `0xAARRGGBB`. Static color math saturates and returns opaque colors, except `QColor.blend` (`BlendMode.Alpha`), which composites straight-alpha "over" and keeps a translucent destination translucent.
- Angles are in degrees. For matrices, `a.multiply(b)` means "apply `a`, then `b`". `QQuaternion` angles use the same sign as `QMatrix4x4.rotationMatrix` (pinned by `QuaternionTest`).

### Multi-pass
- A pass that reconstructs positions from another pass's depth must use the same `QProjection` as the pass that wrote it. Build its full-screen quad with `QMesh.screenQuad(projection)`.
- Current-pixel G-buffer/depth reads require the source to match the target size. The explicit-coordinate reads accept any size and clamp to the edges.
- Never sample a buffer that the same flush renders into; copy it first (`copyTo`, `copyDepthTo`, `copyGBuffersTo`, which require equal sizes). Never copy into a buffer whose viewer has a pending clear.

### Data
- `QAttribBuffer` and `QIndexBuffer` are fixed-size, and `wrap` shares the caller's array. Indices are validated against every bound attribute buffer during `flush()`.
- `QMipTexture` is read-only and built once; rebuild it when the source changes.
- For large worlds, keep vertex data local and pass camera-relative offsets (computed in `double`) to avoid float precision loss.

## Code conventions
- Public classes in `ca.bael.QDraw` are prefixed with `Q`; internal pipeline classes are package-private and unprefixed.
- File header: author, year range `first-last` (update the last year when modifying a file), file name.
- `////...` section dividers (CONSTANTS, PUBLIC/PRIVATE MEMBERS, PUBLIC/PRIVATE METHODS, CONSTRUCTORS); abbreviation conventions are in `package-info.java`.
- Private helpers are prefixed `internal`. Errors are `QException` with a `PointOfError`.
- Tests are `@Test` methods in `Expects.java`. Prefer a small test over prose for subtle conventions.
