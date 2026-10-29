# Mascot Showcase Demo (`:demos:mascot`)

An automated visual test suite and showcase for **QuickDraw** (`ca.bael.QDraw`). It loops continuously through 6 distinct 15-second scenes exercising built-in render modes, lighting, texture manipulation, multi-mesh instancing, and custom vertex/fragment shaders.

---

## Running the Demo

From the repository root:

```bash
./gradlew :demos:mascot:run
```

> **Note**: Requires a graphical display environment (X11/Wayland/macOS/Windows).

---

## Showcase Scenes

The demo opens a `1200x960` window (`600x480` [`QRenderBuffer`](../../lib/src/main/java/ca/bael/QDraw/QRenderBuffer.java), near-clip `-0.4f`) and cycles through the following 15-second cuts in [`Mascot.java`](src/main/java/Mascot.java):

1. **`RegularMascot`**:
   - Renders a single rotating `Mascot_Smooth.obj` mesh with `MascotFuzzy256.png` using `RenderMode.Textured`.
2. **`MascotAndHisFriends`**:
   - Renders three rotating mascots side-by-side to compare built-in shading modes:
     - **Left**: `RenderMode.Textured`
     - **Center**: `RenderMode.Material` (Blinn-Phong shading with two `QLight` sources and shininess `1024`)
     - **Right**: `RenderMode.Normal` (RGB surface normal visualization)
3. **`MascotAndHisBoyFriend`**:
   - Renders two mascots rotating on perpendicular axes, demonstrating in-place texture channel manipulation (`flipBRInplace` swaps the red and blue channels of `MascotFuzzy256_AltEyes.png`).
4. **`TechMascot`**:
   - Stress-tests the pipeline by rendering a `12x12` oscillating grid (144 instances per frame) of rotating mascot meshes textured with `Tech.jpg`.
5. **`CustomWobblyMascot` ([`WobbleShader.java`](src/main/java/WobbleShader.java))**:
   - Demonstrates a custom vertex and fragment shader using a time uniform (`Float.class` in uniform slot `1`) to apply sinusoidal vertex displacement and partial alpha transparency (`alpha = 200`).
6. **`UltraGFXMascot` ([`FuzzShader.java`](src/main/java/FuzzShader.java))**:
   - Demonstrates procedural UV jitter, normal perturbation, and custom per-fragment lighting via `FuzzShader`.
