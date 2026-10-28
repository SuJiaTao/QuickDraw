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
- **[`QEncoding.java`](lib/src/main/java/ca/bael/QDraw/QEncoding.java)**: Base class holding shared constants for 32-bit `ARGB` color bitmasks/shifts (`COL_*`), 3-component vector indices (`VCTR_*`, `VCTR_COMPARE_EPSILON = 0.0005f`), and 4x4 matrix dimensions (`MTR_*`). Also documents standard codebase abbreviations and comment notation.
- **[`QMath.java`](lib/src/main/java/ca/bael/QDraw/QMath.java)**: Static low-level float array operations (`copy2`, `copy3`, `copy4x4`, `cross3`, `dot3`, `add3`, `sub3`, `mult2`, `mult3`, `mul3_4x4`, `mul4x4`) with offset overloads to avoid array allocations.
  - Trigonometric helpers `cosf(degrees)` and `sinf(degrees)` take **degrees**, not radians.
  - Fast magnitude approximations (`fastmag2`, `fastmag3`) approximate Euclidean length without `Math.sqrt`.
- **[`QVector3.java`](lib/src/main/java/ca/bael/QDraw/QVector3.java)**: 3-float vector wrapper supporting both in-place mutation (`vec.add(other)`, `vec.multiply3(fac)`) and static non-mutating operations (`QVector3.add(v1, v2)`). Note: static factory methods `QVector3.X()`, `Y()`, `Z()` currently return zero-initialized vectors (`COMPONENTS_X/Y/Z` are `{0,0,0}`); check callers before changing them.
- **[`QMatrix4x4.java`](lib/src/main/java/ca/bael/QDraw/QMatrix4x4.java)**: 16-float matrix indexed via `col + 4 * row`. Supports `Identity()`, `Zero()`, `scaleMatrix`, `translationMatrix`, `rotationMatrix` (angles in degrees), `TRS(translation, rotation, scale)`, and decomposition (`extractTranslation`, `extractScale`, `extractRotation`).

### Geometry & Attributes
- **[`QAttribBuffer.java`](lib/src/main/java/ca/bael/QDraw/QAttribBuffer.java)**: Contiguous `float[]` buffer storing `attribCount` attributes of `compsPerAttrib` floats each.
- **[`QAttribIndexer.java`](lib/src/main/java/ca/bael/QDraw/QAttribIndexer.java)**: Holds `int[]` triangle vertex indices (length must be a multiple of 3) referencing a `QAttribBuffer`.
- **[`QMesh.java`](lib/src/main/java/ca/bael/QDraw/QMesh.java)**: Manages position (3 floats), UV (2 floats), and normal (3 floats) `QAttribBuffer`/`QAttribIndexer` pairs.
  - Can be constructed from raw arrays or a Wavefront `.obj` file path (`new QMesh(String objFilePath)`).
  - The `.obj` parser requires `v`, `vt`, `vn`, and face definitions formatted as `f p/uv/n ...` (1-indexed, must include normals). Faces with more than 3 vertices are triangulated via triangle-fan tessellation.

### Color, Textures, and Render Buffers
- **[`QColor.java`](lib/src/main/java/ca/bael/QDraw/QColor.java)**: 32-bit `ARGB` color wrapper (`0xAARRGGBB`). Channel values are `0..255`.
- **[`QSampleable.java`](lib/src/main/java/ca/bael/QDraw/QSampleable.java)**: Abstract 2D pixel source/target supporting UV sampling (`SampleType.Cutoff`, `Clamp`, `Repeat`) and functional pixel transforms (`mapColor`, `mapColorSpacial`, `copyTo`).
- **[`QTexture.java`](lib/src/main/java/ca/bael/QDraw/QTexture.java)**: Subclasses `QSampleable`. Stores pixel data in **16x16 (`CHUNK_WIDTH x CHUNK_HEIGHT`) spatial chunks** (`translateIndex(x, y)`) using bitwise shifts/masks to maximize CPU cache locality during 2D UV sampling. Y coordinates are flipped on image load so `(0, 0)` corresponds to the bottom-left corner. Supports saving to `.png`.
- **[`QRenderBuffer.java`](lib/src/main/java/ca/bael/QDraw/QRenderBuffer.java)**: Subclasses `QSampleable`. Wraps a `BufferedImage` (`TYPE_INT_ARGB` backed by `DataBufferInt`) and a `float[]` depth buffer. `coordToDataIndex(x, y)` inverts `y` so `(0, 0)` is bottom-left.

### Shaders, Lighting, and Rendering Pipeline
- **[`QShader.java`](lib/src/main/java/ca/bael/QDraw/QShader.java)**: Abstract base class for shaders. Subclasses implement:
  - `ShaderRequirement[] requirements()` declaring required `Attribute`, `Uniform` (with expected `Class<?>`), and `Texture` slots.
  - `QVector3 vertexShader(VertexShaderContext context)` returning transformed vertex position and writing varyings via `setOutputToFragShader` / `forwardAttributeToFragShader`.
  - `QColor fragmentShader(FragmentShaderContext context)` reading interpolated varyings via `getOutputFromVertShader` and returning fragment `QColor`.
  - Also provides fast XORSHIFT deterministic PRNG (`seededRandom`, `seededRandomVector`) and integer color math (`blendColor`, `addColor`, `multiplyColor`).
- **[`QLight.java`](lib/src/main/java/ca/bael/QDraw/QLight.java)** & **[`QMaterial.java`](lib/src/main/java/ca/bael/QDraw/QMaterial.java)**: Data structures for point/directional lights and Blinn-Phong material properties (`ambient`, `diffuse`, `specular`, `shininess`).
- **[`QViewer.java`](lib/src/main/java/ca/bael/QDraw/QViewer.java)**: The software rendering pipeline.
  - Built-in render modes (`RenderMode`): `SolidFill`, `Textured`, `Normal`, `Material` (Blinn-Phong), and `CustomShader`.
  - Default slots:
    - Attributes (`0..7`): `0` = position, `1` = UV, `2` = normal.
    - Uniforms (`0..15`): `0` = transform `QMatrix4x4`, `1` = `QLight[]`, `2` = `QMaterial`.
    - Textures (`0..15`): `0` = primary `QSampleable`.
  - **Pipeline stages in `internalDraw()`**:
    1. Validates vertex attribute indexers have matching triangle counts and checks `QShader.requirements()`.
    2. Executes `vertexShader` for each of the 3 triangle vertices.
    3. Computes face normal (`cross3`) and culls backfacing triangles (`dot3(normal, rayToCenter) > 0`).
    4. Clips triangles against `nearClip` (`Z > nearClip` is behind the camera; visible geometry has negative Z). Clips 1-vertex-behind into 2 triangles (quad) and 2-vertices-behind into 1 triangle, linearly interpolating shader outputs and preserving clockwise winding order.
    5. Projects vertices (`posn[Z] = 1.0f / posn[Z]`, `posn[X,Y] *= -posn[Z]`) and maps `viewBounds` to screen coordinates.
    6. Sorts vertices by Y, splits into flat-top and flat-bottom triangles, rasterizes scanlines, computes perspective-correct barycentric weights, tests inverted-Z depth (`invDepth <= currentDepth - DEPTH_TEST_EPSILON`), invokes `fragmentShader`, discards fully transparent fragments (`A == 0`), alpha-blends with existing framebuffer color, and updates depth and color buffers.
- **[`QWindow.java`](lib/src/main/java/ca/bael/QDraw/QWindow.java)**: Swing `JFrame` wrapper with a 2-buffer `BufferStrategy` and `InputListener` for polling keyboard (`isKeyDown`, `isCharDownIgnoreCase`, `getInputWASDVector`, `getInputArrowKeysVector`) and mouse state.
- **[`QException.java`](lib/src/main/java/ca/bael/QDraw/QException.java)**: Unchecked `RuntimeException` subclass parameterized by `PointOfError` (`NullParameter`, `InvalidParameter`, `MalformedData`, `InvalidData`, `BadState`).

---

## Important Gotchas & Conventions for Agents

1. **`QViewer.drawMesh()` Clears All Slots After Drawing**:
   - Calling `viewer.drawMesh(mesh)` automatically calls `clearTextureSlots()`, `clearUniformSlots()`, and `clearVertexAttribSlots()` after `draw()`. Any uniforms (matrices, lights, materials) and textures **must be re-bound before each `drawMesh()` call**.
2. **Coordinate System & Camera Space**:
   - The camera looks down the **negative Z-axis** (`nearClip` defaults to `-0.005f` and is clamped via `Math.min(MIN_NEAR_CLIP, val)`). Geometry in front of the camera has `Z < nearClip`.
   - Screen space and texture space have `(0, 0)` at the **bottom-left**.
3. **Uniform Type Checking**:
   - `QViewer.internalEnsureShaderRequirements` checks uniform types using exact class equality (`slotUniforms[slot].getClass() != require.uniformClass`). Pass the exact class expected by the shader (e.g., `QMatrix4x4.class`, `QLight[].class`, `QMaterial.class`, `Float.class`).
4. **Unit Tests (`Expects.java`)**:
   - Unit tests in [`lib/src/test/java/ca/bael/QDraw/Expects.java`](lib/src/test/java/ca/bael/QDraw/Expects.java) (`ColorTest`, `RenderBufferTest`, `VectorTest`, `MatrixTest`, and `MathTest`) use JUnit Jupiter (`@Test`). When adding new tests, annotate them with `@Test` (non-static) and run `./gradlew :lib:test` to verify execution.
5. **Code Formatting Style**:
   - Maintain the existing file header comments, `/////////////////////////////////////////////////////////////////` section dividers, and `QEncoding` conventions when editing files in `lib/src/main/java/ca/bael/QDraw/`.
