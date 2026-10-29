# Sceneview Demo (`:demos:sceneview`)

An interactive first-person 3D scene viewer built on **QuickDraw** (`ca.bael.QDraw`) that demonstrates real-time camera movement, multiple render modes, dynamic lighting, Blinn-Phong materials, and custom shaders.

---

## Running the Demo

From the repository root:

```bash
./gradlew :demos:sceneview:run
```

> **Note**: Requires a graphical display environment (X11/Wayland/macOS/Windows).

---

## Controls

| Input | Action |
| :--- | :--- |
| `W` / `S` | Move camera forward / backward |
| `A` / `D` | Strafe camera left / right |
| `Up` / `Down` Arrow | Pitch camera up / down |
| `Left` / `Right` Arrow | Yaw camera left / right |
| `U` | Switch mascot model to `RenderMode.CustomShader` ([`FuzzShader`](src/main/java/FuzzShader.java)) |
| `I` | Switch mascot model to `RenderMode.Material` (Blinn-Phong shading) |
| `O` | Switch mascot model to `RenderMode.Normal` (surface normal visualization) |
| `P` | Switch mascot model to `RenderMode.Textured` (unlit texture sampling) |

---

## Scene Overview & Implementation Details

- **Display & Resolution**:
  - Opens a `1300x1000` [`QWindow`](../../lib/src/main/java/ca/bael/QDraw/QWindow.java) backed by a half-resolution (`650x500`) [`QRenderBuffer`](../../lib/src/main/java/ca/bael/QDraw/QRenderBuffer.java) with near-clip set to `-0.05f`.
- **Models & Assets** (`src/main/resources/`):
  - **Model 0 (`Mascot_Smooth.obj` + `MascotFuzzy256.png`)**: Positioned at `(0.0, 0.0, -3.0)` with uniform scale `0.35`. Renders using the currently selected mode (`U`/`I`/`O`/`P`, defaulting to `CustomShader` with `FuzzShader`).
  - **Model 1 (`Cube.obj` + `Texture_Medium.jpg`)**: Positioned at `(-3.0, -1.3, -3.0)` with uniform scale `0.35`. Renders using `RenderMode.Material` with a high-shininess (`8192`) [`QMaterial`](../../lib/src/main/java/ca/bael/QDraw/QMaterial.java).
- **Lighting**:
  - Two point lights (`QLight`) at world positions `(5.0, 1.0, 3.0)` (strength `5.0`) and `(-2.0, 0.0, -20.0)` (strength `0.3`), transformed into view space each frame before binding to uniform slot `QViewer.DEFAULT_SHADER_LIGHTS_SLOT`.
- **Custom Shader ([`FuzzShader.java`](src/main/java/FuzzShader.java))**:
  - Perturbs fragment UV coordinates using deterministic screen-space pseudo-random noise (`QShader.seededRandom(fctx)`).
  - Offsets surface normals per texel color using `QShader.seededRandomVector` and computes distance-attenuated diffuse + Phong lighting.
