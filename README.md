# Quickdraw

Java Software Rendering library.


## Quickstart

package the the `:lib`
```bash
./gradlew lib:jar
```

a jar containing the library will be produced at `lib/build/libs/lib-{version}.jar`

> replace "{version}" with the current version of QuickDraw

then in your `build.gradle`, add

```gradle
dependencies{
    implementation files("{path to the jar file}")
}
```

then you can use QuickDraw to start drawing. 

## Documentation

```bash
./gradlew lib:javadoc
```

will generate javadoc for under `lib/build/docs/javadoc`

> more documentation coming soon. See Demos below for more usage guides.

## Demos

Each subproject under `demos/` uses the Gradle `application` plugin and can be launched via its `:run` task:

- **[Scene Viewer (`demos/sceneview`)](demos/sceneview/README.md)** — Interactive 3D scene viewer (`WASD` to move, Arrow keys to look, `U`/`I`/`O`/`P` to switch between `CustomShader`, `Material`, `Normal`, and `Textured` render modes):
  ```bash
  ./gradlew :demos:sceneview:run
  ```
- **[Mascot Showcase (`demos/mascot`)](demos/mascot/README.md)** — Automated visual showcase cycling through 15-second scenes demonstrating textures, Blinn-Phong materials, normal rendering, instancing, `WobbleShader`, and `FuzzShader`:
  ```bash
  ./gradlew :demos:mascot:run
  ```
- **[Make Fuzz Texture (`demos/make-fuzz-texture`)](demos/make-fuzz-texture/README.md)** — Offline texture generation utility that reads `/Mascot256_AltEyes.png`, applies spatial noise via `QSampleable.ColorMapSpacialFunction`, and writes to `demos/make-fuzz-texture/out/Mascot256_AltEyes.png`:
  ```bash
  ./gradlew :demos:make-fuzz-texture:run
  ```
- **[Performance Profiler (`demos/profile`)](demos/profile/README.md)** — Benchmarks chunked `QTexture` vs. linear `QRenderBuffer` sampling across small, medium, large, and huge textures:
  ```bash
  ./gradlew :demos:profile:run
  ```

## Contributing

see [CONTRIBUTING.md](CONTRIBUTING.md) and [AGENTS.md](AGENTS.md)