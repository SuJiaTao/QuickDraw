# Texture vs. RenderBuffer Profiler (`:demos:profile`)

A benchmarking application for **QuickDraw** (`ca.bael.QDraw`) that measures rendering performance when sampling from a spatially chunked [`QTexture`](../../lib/src/main/java/ca/bael/QDraw/QTexture.java) versus a linearly indexed [`QRenderBuffer`](../../lib/src/main/java/ca/bael/QDraw/QRenderBuffer.java) across multiple texture resolutions.

---

## Running the Benchmark

From the repository root:

```bash
./gradlew :demos:profile:run
```

> **Note**: Requires a graphical display environment (X11/Wayland/macOS/Windows). The window automatically closes once all benchmark passes complete.

---

## What It Benchmarks

Both `QTexture` and `QRenderBuffer` subclass [`QSampleable`](../../lib/src/main/java/ca/bael/QDraw/QSampleable.java), meaning either can be bound to a texture slot and sampled during fragment shading, but they use different memory layouts:

- **`QRenderBuffer`**: Stores pixels in a standard row-major scanline array (`x + width * (height - y - 1)`). Sampling vertically adjacent texels jumps an entire scanline in memory, which can cause CPU cache misses on high-resolution textures.
- **`QTexture`**: Subdivides the image into **16x16 (`4^2 x 4^2`) spatial chunks** (`translateIndex(x, y)`), using bitwise shifts and masks so that 2D-adjacent texels remain close together in 1D memory.

### Benchmark Methodology

Defined in [`Profile.java`](src/main/java/Profile.java) and [`TimerInstance.java`](src/main/java/TimerInstance.java):

1. Opens a `1600x1000` `QWindow` and `QRenderBuffer`.
2. Tests 4 texture resolutions (`Texture_Huge.jpg`, `Texture_Large.jpg`, `Texture_Medium.jpg`, `Texture_Small.jpg`) mapped onto `Mascot.obj`.
3. For each resolution, runs 2 iterations of `500` frames (`SAMPLE_FRAME_COUNT`).
4. On each frame, alternates the execution order between `QTexture` and `QRenderBuffer` (`(frame & 1) == 0`) to eliminate warm-up or frame-ordering bias, timing `viewer.setMatrix(...)` + `viewer.drawMesh(mesh)` in microseconds (`System.nanoTime() >> 10`).
5. Prints the average frame render time (`TEXAVG`, `RBAVG`) and the `TEXAVG / RBAVG` ratio (`< 1.0` indicates `QTexture` was faster than `QRenderBuffer`).
