# Make Fuzz Texture Utility (`:demos:make-fuzz-texture`)

A headless command-line utility demonstrating **QuickDraw's** 2D image processing and spatial color-mapping APIs (`QTexture`, `QSampleable.ColorMapSpacialFunction`, and `QShader` deterministic PRNG helpers).

---

## Running the Utility

From the repository root:

```bash
./gradlew :demos:make-fuzz-texture:run
```

Unlike the interactive demos, this utility does not create a `QWindow` and can run in headless environments.

---

## How It Works

Defined in [`MakeFuzzTexture.java`](src/main/java/MakeFuzzTexture.java):

1. **Loads Source Texture**:
   - Reads `/Mascot256_AltEyes.png` from `src/main/resources/` into a [`QTexture`](../../lib/src/main/java/ca/bael/QDraw/QTexture.java).
2. **Applies Spatial Color Mapping**:
   - Invokes `tex.mapColorSpacial(...)` with a custom `QSampleable.ColorMapSpacialFunction`.
   - For each pixel `(x, y)`, checks whether the pixel matches one of the three base blue palette colors (ignoring alpha via `QColor.equalsIgnoreAlpha`):
     - Primary blue `(25, 76, 178)`
     - Secondary blue `(127, 153, 204)`
     - Tertiary blue `(204, 204, 229)`
   - Uses bitwise-shifted coordinates `(x >> n, y >> m)` as seeds to `QShader.seededRandom(...)` and scales the pixel's brightness via `QShader.multiplyColor(...)` to produce a multi-frequency "fuzzy" procedural texture effect.
3. **Saves Output**:
   - Writes the resulting image to `demos/make-fuzz-texture/out/Mascot256_AltEyes.png` using `QTexture.save(...)`.
