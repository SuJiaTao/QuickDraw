# Contributing to QuickDraw

Thank you for your interest in contributing to **QuickDraw** (`ca.bael.QDraw`), a pure-Java 3D software rendering library! This document provides guidelines and instructions for building, testing, and contributing to the project.

---

## Prerequisites

- **Java Development Kit (JDK) 21**: The build is configured with a Java 21 toolchain in Gradle.
- **Gradle**: Use the included Gradle Wrapper (`./gradlew` on Linux/macOS or `gradlew.bat` on Windows, configured for Gradle 9.7.0). No separate Gradle installation is required.
- **Graphical Display Environment** *(for interactive demos)*: Running the visual demos (`sceneview`, `mascot`, `profile`) requires an active display server (X11/Wayland/macOS/Windows) because `QWindow` creates a Swing `JFrame`.

---

## Repository Structure

QuickDraw is organized as a multi-project Gradle build defined in [`settings.gradle`](settings.gradle):

```text
QuickDraw/
├── lib/                        # Core software rendering library (package ca.bael.QDraw)
│   ├── src/main/java/          # Library source files (QViewer, QShader, QMesh, etc.)
│   ├── src/main/resources/     # Library resources
│   └── src/test/java/          # JUnit Jupiter unit tests (Expects.java)
└── demos/                      # Standalone demo and benchmark applications
    ├── sceneview/              # Interactive 3D scene viewer demo
    ├── mascot/                 # Automated visual showcase of render modes & shaders
    ├── make-fuzz-texture/      # Offline texture generation utility using spatial color mapping
    └── profile/                # Performance benchmark (QTexture vs. QRenderBuffer sampling)
```

---

## Building and Testing

### Build the Entire Project

Compile the library, all demos, and run unit tests:

```bash
./gradlew build
```

### Run Unit Tests

Execute the JUnit Jupiter test suite in `:lib`:

```bash
./gradlew :lib:test
```

Test reports are generated at `lib/build/reports/tests/test/index.html`.

### Run Demo Applications

To run or learn more about the demo subprojects under `demos/`, see the [Demos section in README.md](README.md#demos).

---

## Coding Style & Conventions

When modifying existing classes or adding new ones to `ca.bael.QDraw`, please follow the established conventions in the codebase:

1. **Zero External Runtime Dependencies**:
   - The core library (`:lib`) relies strictly on standard Java SE / AWT / Swing / ImageIO APIs. Do not introduce third-party runtime dependencies without prior discussion.

2. **Naming Conventions**:
   - Public library classes in `ca.bael.QDraw` are prefixed with `Q` (e.g., `QViewer`, `QShader`, `QMesh`, `QTexture`, `QRenderBuffer`, `QVector3`, `QMatrix4x4`, `QColor`, `QMath`, `QWindow`, `QLight`, `QMaterial`, `QAttribBuffer`, `QAttribIndexer`, `QEncoding`, `QException`, `QSampleable`).
   - Internal helper methods on `QViewer` are prefixed with `internal` (e.g., `internalDraw`, `internalClipTri`).
   - Constants use `UPPER_SNAKE_CASE`.

3. **File Organization & Section Banners**:
   - Group class members using banner comments:
     ```java
     /////////////////////////////////////////////////////////////////
     // CONSTANTS

     /////////////////////////////////////////////////////////////////
     // PUBLIC ENUMS

     /////////////////////////////////////////////////////////////////
     // PRIVATE MEMBERS

     /////////////////////////////////////////////////////////////////
     // PUBLIC METHODS

     /////////////////////////////////////////////////////////////////
     // PRIVATE METHODS

     /////////////////////////////////////////////////////////////////
     // CONSTRUCTORS
     ```

4. **Data Layout & Abbreviations (`QEncoding`)**:
   - Classes that work directly with packed arrays (`QColor`, `QVector3`, `QMatrix4x4`, `QMath`, `QMesh`, `QViewer`) extend [`QEncoding`](lib/src/main/java/ca/bael/QDraw/QEncoding.java).
   - Follow the standard abbreviations and array layout documentation syntax defined in `QEncoding.java`:
     - `| x y z |` — array of fixed length
     - `< x y z >` — contiguous logical grouping of elements in an array
     - `| < x y z > ... |` — array of arbitrary number of groupings

5. **Error Handling**:
   - Throw [`QException`](lib/src/main/java/ca/bael/QDraw/QException.java) with the appropriate `QException.PointOfError` category (`NullParameter`, `InvalidParameter`, `MalformedData`, `InvalidData`, or `BadState`) and a descriptive message when validating parameters or state.

6. **Performance Considerations**:
   - The rasterizer, shader execution, and math routines (`QViewer`, `QMath`, `QTexture`) run on the CPU per vertex and per pixel. Avoid unnecessary object allocations in hot loops (such as `internalDrawFragment` or `QMath` array operations) and verify performance-sensitive changes using `./gradlew :demos:profile:run`.

---

## Testing Guidelines

- Add unit tests for new math operations, buffer manipulations, color encodings, and rendering utilities in [`lib/src/test/java/ca/bael/QDraw/Expects.java`](lib/src/test/java/ca/bael/QDraw/Expects.java) (or dedicated JUnit test classes under `lib/src/test/java/ca/bael/QDraw/`).
- Annotate all test methods with `@org.junit.jupiter.api.Test`.
- For rendering pipeline or shader changes, verify visual output with `:demos:sceneview:run` and `:demos:mascot:run`.

---

## Submitting Changes

1. Create a descriptive branch for your work (e.g., `fix-near-plane-clipping` or `add-obj-loader-tests`).
2. Keep commits focused and write clear commit messages explaining what changed and why.
3. Ensure `./gradlew build` passes with zero warnings or test failures.
4. Open a Pull Request describing the motivation, implementation details, and any visual or performance impact.

---

## License

By contributing to QuickDraw, you agree that your contributions will be licensed under the [MIT License](LICENSE).
