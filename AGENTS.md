# AGENTS.md

This file provides architectural context, build/test commands, and codebase conventions for AI coding agents working in the **QuickDraw** repository.

---

## Project Overview

**QuickDraw** (`ca.bael.QDraw`) is a pure-Java 3D software rendering library and rasterizer targeting **Java 21**. It implements a programmable vertex/fragment shader pipeline, perspective-correct barycentric rasterization, near-plane triangle clipping, backface culling, inverted-Z depth buffering, Wavefront `.obj` mesh loading, cache-friendly chunked texture storage, and a Swing double-buffered windowing wrapper—all with **zero external runtime dependencies**.

---

## Build, Test, and Run Commands

Always use the Gradle Wrapper (`./gradlew`) at the repository root:

| Task | Command | Notes |
| :--- | :--- | :--- |
| Build all subprojects & run tests | `./gradlew build` | Compiles `:lib` and all `:demos:*` subprojects |
| Compile core library only | `./gradlew :lib:compileJava` | Fast syntax/type check for library changes |
| Run unit tests | `./gradlew :lib:test` | Runs JUnit Jupiter tests in `lib/src/test/java` |
| Run Sceneview demo | `./gradlew :demos:sceneview:run` | Interactive 3D viewer (requires GUI display) |
| Run Mascot showcase demo | `./gradlew :demos:mascot:run` | Visual test suite (requires GUI display) |
| Run MakeFuzzTexture demo | `./gradlew :demos:make-fuzz-texture:run` | Headless-safe texture generator utility |
| Run Profile benchmark | `./gradlew :demos:profile:run` | Benchmarks `QTexture` vs. `QRenderBuffer` (requires GUI display) |

> **Headless Environment Note**: `QWindow` constructs a `javax.swing.JFrame` and initializes an AWT `BufferStrategy`. Running `:demos:sceneview:run`, `:demos:mascot:run`, or `:demos:profile:run` in a headless environment without an X11/Wayland display will throw `java.awt.HeadlessException`. `:lib:test` and `:demos:make-fuzz-texture:run` do not create windows and can run headlessly.

---

## Repository & Module Layout

Defined in [`settings.gradle`](settings.gradle):

- **`lib/`** (`java-library` plugin):
  - Source: `lib/src/main/java/ca/bael/QDraw/`
  - Resources: `lib/src/main/resources/`
  - Tests: `lib/src/test/java/ca/bael/QDraw/Expects.java` (JUnit Jupiter 6.1.3)
- **`demos/sceneview/`** (`application` plugin, mainClass = `Sceneview`):
  - Interactive first-person viewer (`WASD` movement, Arrow key look, `U`/`I`/`O`/`P` to toggle `CustomShader`, `Material`, `Normal`, and `Textured` modes).
- **`demos/mascot/`** (`application` plugin, mainClass = `Mascot`):
  - Cycles through 6 visual test scenes (`RegularMascot`, `MascotAndHisFriends`, `MascotAndHisBoyFriend`, `TechMascot`, `CustomWobblyMascot`, `UltraGFXMascot`) using `FuzzShader` and `WobbleShader`.
- **`demos/make-fuzz-texture/`** (`application` plugin, mainClass = `MakeFuzzTexture`):
  - Loads `/Mascot256_AltEyes.png`, applies `QSampleable.ColorMapSpacialFunction`, and saves to `demos/make-fuzz-texture/out/Mascot256_AltEyes.png`.
- **`demos/profile/`** (`application` plugin, mainClass = `Profile`):
  - Profiles rendering frame times (`TimerInstance`) alternating between chunked `QTexture` and linear `QRenderBuffer` across 4 texture resolutions.

---

## Architecture & Core Classes (`ca.bael.QDraw`)

### Data Encodings & Math
- **[`package-info.java`](lib/src/main/java/ca/bael/QDraw/package-info.java)**: Documents standard codebase abbreviations and comment notation. Encoding constants live in their owner classes: `COL_*` (private) in `QColor`, `MTR_*` (private) in `QMatrix4x4`, and `VCTR_*` / `VCTR_COMPARE_EPSILON = 0.0005f` (package-private) in `QVector3`, which other classes pull in via `import static ca.bael.QDraw.QVector3.VCTR_*`.
- **[`QVector3.java`](lib/src/main/java/ca/bael/QDraw/QVector3.java)**: 3-float vector wrapper supporting both in-place mutation (`vec.add(other)`, `vec.multiply3(fac)`) and static non-mutating operations (`QVector3.add(v1, v2)`). Note: static factory methods `QVector3.X()`, `Y()`, `Z()` currently return zero-initialized vectors (`COMPONENTS_X/Y/Z` are `{0,0,0}`); check callers before changing them.
  - Package-private static in-place `float[]` helpers (`copy`, `add`, `sub`, `scale`, `dot`, `cross`, `magnitude`, `sqrMagnitude`, `fastMagnitude`) are used by `QViewer` internals on raw position/normal arrays. `fastMagnitude` approximates Euclidean length without `Math.sqrt`.
- **[`QMatrix4x4.java`](lib/src/main/java/ca/bael/QDraw/QMatrix4x4.java)**: 16-float matrix indexed via `col + 4 * row`. Supports `Identity()`, `Zero()`, `scaleMatrix`, `translationMatrix`, `rotationMatrix` (angles in degrees), `TRS(translation, rotation, scale)`, and decomposition (`extractTranslation`, `extractScale`, `extractRotation`).
  - `transformPoint(v)` applies the 3x3 part plus translation; `transformDir(v)` applies only the 3x3 part (use for normals/directions). Both mutate `v` in place and accept `float[]` or `QVector3`. The projective row is ignored.
  - `normalMatrix()` returns the inverse-transpose of the 3x3 part (correct for non-uniform scale); normals transformed with it must be re-normalized.
  - `inverse()` returns the affine inverse (3x3 inverse plus inverted translation; the projective row is ignored).
  - Trigonometric helpers `cosDeg(degrees)` and `sinDeg(degrees)` take **degrees**, not radians.
- **[`QQuaternion.java`](lib/src/main/java/ca/bael/QDraw/QQuaternion.java)**: Orientation quaternion. `fromAxisAngle` (degrees, same sign convention as `QMatrix4x4.rotationMatrix`), `rotateLocal`/`rotateGlobal` (body vs world axis, re-normalized), static Hamilton `multiply(a, b)`, `rotate(v)` in place, `toMatrix()`.

### Geometry & Attributes
- **[`QAttribBuffer.java`](lib/src/main/java/ca/bael/QDraw/QAttribBuffer.java)**: Fixed-size, writable `float[]` buffer storing `attribCount` attributes of `compsPerAttrib` floats each. `new QAttribBuffer(comps, count)` is zero-filled, `wrap(data, comps)` does not copy, `set(i, ...)` writes one attribute.
- **[`QIndexBuffer.java`](lib/src/main/java/ca/bael/QDraw/QIndexBuffer.java)**: Fixed-size, writable `int[]` triangle vertex indices (length must be a multiple of 3). `new QIndexBuffer(triCount)` is zero-filled, `wrap(indices)` does not copy. One index buffer per draw indexes every bound attribute slot.
- **[`QMesh.java`](lib/src/main/java/ca/bael/QDraw/QMesh.java)**: One `QIndexBuffer` plus position (3 floats), UV (2 floats), and normal (3 floats) `QAttribBuffer`s. At `.obj`/array load, each unique (position, uv, normal) tuple becomes one vertex. `new QMesh(indexBuffer, pos, uv, normal)` takes buffers directly with no deduplication.
  - Can be constructed from raw arrays or a Wavefront `.obj` file path (`new QMesh(String objFilePath)`).
  - The `.obj` parser requires `v`, `vt`, `vn`, and face definitions formatted as `f p/uv/n ...` (1-indexed, must include normals). Faces with more than 3 vertices are triangulated via triangle-fan tessellation.

### Color, Textures, and Render Buffers
- **[`QColor.java`](lib/src/main/java/ca/bael/QDraw/QColor.java)**: 32-bit `ARGB` color wrapper (`0xAARRGGBB`). Channel values are `0..255`.
  - Color math (`blend`, `add`, `multiply`) exists as static methods on packed `int` ARGB (allocation-free, used by the pipeline) and as in-place chainable instance methods. Results are opaque (`A = 0xFF`) and channels saturate at `0..255`.
- **[`QSampleable.java`](lib/src/main/java/ca/bael/QDraw/QSampleable.java)**: Abstract 2D pixel source/target supporting UV sampling (`SampleType.Cutoff`, `Clamp`, `Repeat`) and functional pixel transforms (`mapColor`, `mapColorSpacial`, `copyTo`).
- **[`QTexture.java`](lib/src/main/java/ca/bael/QDraw/QTexture.java)**: Subclasses `QSampleable`. Stores pixel data in **16x16 (`CHUNK_WIDTH x CHUNK_HEIGHT`) spatial chunks** (`translateIndex(x, y)`) using bitwise shifts/masks to maximize CPU cache locality during 2D UV sampling. Y coordinates are flipped on image load so `(0, 0)` corresponds to the bottom-left corner. Supports saving to `.png`.
- **[`QRenderBuffer.java`](lib/src/main/java/ca/bael/QDraw/QRenderBuffer.java)**: Subclasses `QSampleable`. Wraps a `BufferedImage` (`TYPE_INT_ARGB` backed by `DataBufferInt`) and a `float[]` depth buffer. `coordToDataIndex(x, y)` inverts `y` so `(0, 0)` is bottom-left.

### Shaders, Lighting, and Rendering Pipeline
- **[`QShader.java`](lib/src/main/java/ca/bael/QDraw/QShader.java)**: Abstract base class for shaders. Subclasses must implement `requirements()` and may override the rest:
  - `ShaderRequirement[] requirements()` declaring required `Attribute`, `Uniform` (with expected `Class<?>`), `Texture` slots, and `Output` slots (with component count). Every slot written by `output`/`forward` must be declared as an `Output`, and every declared `Output` must be written on each `vertexShader` call (the pipeline throws `QException` otherwise); declared outputs are packed into one flat per-vertex array.
  - `QVector3 vertexShader(VertexShaderContext context)` (default: returns the position attribute from `QViewer.DEFAULT_SHADER_POSITION_SLOT` untransformed) reading inputs via `attribVec3`/`attrib`/`uniform`/`texture`, writing outputs via `output`/`forward`, and returning the transformed vertex position. `context.position()` is a reusable vector to build the returned position in (`attribVec3(slot, out)` fills an existing vector).
  - `QColor fragmentShader(FragmentShaderContext context)` (default: opaque magenta `(255, 0, 255, 255)` as a "not implemented" marker) reading interpolated outputs via `inputVec3`/`input`, plus `uniform`/`texture`/`screenX`/`screenY`/`invDepth`/`faceNormal`, and returning the fragment `QColor`. `context.color()` is a reusable color to return instead of `new QColor(...)`.
  - Reusable `position()`/`color()` objects hold the previous call's values: always `set` them before reading, and never retain them after returning.
  - Optional `beginDraw(VertexShaderContext context)` runs once per draw after uniforms/textures are bound and before any vertex is processed. It returns a `QDrawData` (or `null`) holding per-draw values (e.g. a normal matrix, `OutputSlot` handles); both contexts expose it via `context.drawData(Type.class)`. Only `uniform`/`texture` are meaningful inside `beginDraw`. Shaders must not write their own fields after construction: per-draw values go in the `QDrawData`, scratch values in locals or `context.position()`/`context.color()`.
  - `context.outputSlot(slot)` (call it in `beginDraw` and keep the handle in the returned `QDrawData`) validates an output slot once and returns an `OutputSlot` handle. `output`/`forward` (vertex) and `input`/`inputVec3` (fragment) accept the handle and skip the per-call slot lookup and declared-check. The `int slot` overloads still work and check on every call.
  - Also provides fast XORSHIFT deterministic PRNG (`seededRandom`, `seededRandomVector`).
- **[`QLight.java`](lib/src/main/java/ca/bael/QDraw/QLight.java)**: Point light (`position`, `strength`). `illuminate(point, outDir)` writes the normalized direction from `point` to the light into `outDir` and returns the attenuated strength (`strength / distance`). Positions are in **view space** (same space as transformed vertices; camera at the origin looking down -Z).
- **[`QMaterial.java`](lib/src/main/java/ca/bael/QDraw/QMaterial.java)**: Blinn-Phong material properties (`ambient`, `diffuse`, `specular`, `shininess`).
  - The `Material` render mode adds specular on top of the lit texture color and only for surfaces facing the light (N·L > 0).
- **[`QUniform.java`](lib/src/main/java/ca/bael/QDraw/QUniform.java)**: Interface for uniform slot values; `snapshot()` returns a copy unaffected by later changes to the original (immutable types return themselves). Implemented by `QMatrix4x4`, `QVector3`, `QColor`, `QMaterial`, `QLight`, `QLights`, and `QScalar`.
- **[`QLights.java`](lib/src/main/java/ca/bael/QDraw/QLights.java)** / **[`QScalar.java`](lib/src/main/java/ca/bael/QDraw/QScalar.java)**: `QUniform` wrappers for a light array (`count()`, `get(i)`) and an immutable number (`floatValue()`, `intValue()`, `doubleValue()`; constructed from any numeric primitive).
- **[`QViewer.java`](lib/src/main/java/ca/bael/QDraw/QViewer.java)**: The software rendering pipeline.
  - Built-in render modes (`RenderMode`): `SolidFill`, `Textured`, `Normal`, `Material` (Blinn-Phong), and `CustomShader`.
  - Per-draw raster state: `setCullMode(CullMode.Back|Front|None)`, `setBlendMode(BlendMode.Alpha|Additive|Replace)` (additive is alpha-weighted), `setColorWrite`, `setDepthTest`, `setDepthWrite`. `setFieldOfView(fovYDegrees, aspect)` sets symmetric view bounds.
  - Default slots:
    - Attributes (`0..7`): `0` = position, `1` = UV, `2` = normal.
    - Uniforms (`0..15`): `0` = transform `QMatrix4x4`, `1` = `QLights`, `2` = `QMaterial`. Only `QUniform` values can be bound.
    - Textures (`0..15`): `0` = primary `QSampleable`.
  - `flush()` runs stages 1�5 for every queued draw first (in parallel across draws, each geometry worker emitting screen-space vertices and triangle records into its own reusable `TriangleQueue`), then runs stage 6 over all emitted triangles in draw order.
  - **Pipeline stages in `GeometryWorker` (1-5) / `RasterWorker` (6)**:
    1. Validates every index is within every bound attribute buffer (`draw()` already checked `QShader.requirements()` and that an index buffer is set).
    2. Executes `vertexShader` for each of the 3 triangle vertices. Results are cached per vertex index for the duration of the draw, so vertex shaders must be deterministic in their attributes and uniforms.
    3. Computes face normal (`QVector3.cross`) and culls backfacing triangles (`QVector3.dot(normal, rayToCenter) > 0`).
    4. Clips triangles against `nearClip` (`Z > nearClip` is behind the camera; visible geometry has negative Z). Clips 1-vertex-behind into 2 triangles (quad) and 2-vertices-behind into 1 triangle, linearly interpolating shader outputs and preserving clockwise winding order.
    5. Projects vertices (`posn[Z] = 1.0f / posn[Z]`, `posn[X,Y] *= -posn[Z]`) and maps `viewBounds` to screen coordinates.
    6. Sorts vertices by Y, splits into flat-top and flat-bottom triangles, rasterizes scanlines, computes perspective-correct barycentric weights, tests inverted-Z depth (`invDepth < currentDepth`, skipped when `setDepthTest(false)`), invokes `fragmentShader`, discards fully transparent fragments (`A == 0`), alpha-blends with existing framebuffer color, and updates color, plus depth unless `setDepthWrite(false)`.
- **[`QWindow.java`](lib/src/main/java/ca/bael/QDraw/QWindow.java)**: Swing `JFrame` wrapper with a 2-buffer `BufferStrategy` and `InputListener` for polling keyboard (`isKeyDown`, `isCharDownIgnoreCase`, `getInputWASDVector`, `getInputArrowKeysVector`) and mouse state.
- **[`QException.java`](lib/src/main/java/ca/bael/QDraw/QException.java)**: Unchecked `RuntimeException` subclass parameterized by `PointOfError` (`NullParameter`, `InvalidParameter`, `MalformedData`, `InvalidData`, `BadState`).

---

## Important Gotchas & Conventions for Agents

1. **`QViewer.drawMesh()` Clears All Slots After Drawing**:
   - Calling `viewer.drawMesh(mesh)` automatically calls `clearTextureSlots()`, `clearUniformSlots()`, and `clearVertexAttribSlots()` (which also clears the index buffer) after `draw()`. Any uniforms (matrices, lights, materials) and textures **must be re-bound before each `drawMesh()` call**.
   - `draw()`/`drawMesh()` only **queue** the draw (shader requirements are still checked immediately). Nothing is rendered until `flush()`, which must be called before the render buffer is read or presented (e.g. before `QWindow.updateFrame()`). `clearFrame()` flushes first.
   - Queued draws capture viewer state (render mode, shader, fill color, sample type, near clip, view bounds, depth/color write, cull and blend mode, slots). Uniform values are copied via `QUniform.snapshot()`, so they may be modified right after `drawMesh()`; textures, meshes, and shader instances are captured by reference and must not be modified until `flush()`.
   - Errors raised while shading (e.g. unwritten outputs) surface from `flush()`, not `drawMesh()`.
   - `clearFrame()` is deferred too: the buffers are cleared at the start of the next `flush()` (each raster worker clears its own rows), not immediately.
   - `flush()` runs on a shared daemon thread pool, one worker per core: geometry workers each take whole draws, then (after that phase completes) raster workers each own horizontal strips of rows. `beginDraw`, `vertexShader` and `fragmentShader` are all called concurrently from several threads on the same shader instance, so they must not write to shader fields or other shared state; read per-draw values from `context.drawData(...)`.
2. **Coordinate System & Camera Space**:
   - The camera looks down the **negative Z-axis** (`nearClip` defaults to `-0.005f` and is clamped via `Math.min(MIN_NEAR_CLIP, val)`). Geometry in front of the camera has `Z < nearClip`.
   - Screen space and texture space have `(0, 0)` at the **bottom-left**.
3. **Uniform Type Checking**:
   - `QViewer.internalEnsureShaderRequirements` checks uniform types using exact class equality (`slotUniforms[slot].getClass() != require.uniformClass`). Pass the exact class expected by the shader (e.g., `QMatrix4x4.class`, `QLights.class`, `QMaterial.class`, `QScalar.class`).
4. **Unit Tests (`Expects.java`)**:
   - Unit tests in [`lib/src/test/java/ca/bael/QDraw/Expects.java`](lib/src/test/java/ca/bael/QDraw/Expects.java) (`ColorTest`, `RenderBufferTest`, `VectorTest`, `MatrixTest`, and `MathTest`) use JUnit Jupiter (`@Test`). When adding new tests, annotate them with `@Test` (non-static) and run `./gradlew :lib:test` to verify execution.
5. **Code Formatting Style**:
   - Maintain the existing file header comments, `/////////////////////////////////////////////////////////////////` section dividers, and the `package-info.java` abbreviation/notation conventions when editing files in `lib/src/main/java/ca/bael/QDraw/`.
