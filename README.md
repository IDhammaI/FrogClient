# Alien (Fabric 1.21.1)

A Fabric client mod for Minecraft 1.21.1.

## Core Requirements
*   **JDK 21** (Mandatory)
*   **Fabric Loom** (Build System)

## Quick Start

**Build:**
```bash
./gradlew build
```
Artifacts are located in `build/libs/`.

**Run Client:**
```bash
./gradlew runClient
```

## ⚠️ Dependencies
This project uses `flatDir` for local dependencies.
**DO NOT delete or rename JAR files in the `lib/` directory**, or the build will fail.

Required local libraries (included in repo):
- `baritone-api-fabric-1.21.1-1.11.2.jar`
- `nether-pathfinder-1.4.1.jar`
- `satin-2.0.0.jar`
- `sodium-fabric-0.6.13+mc1.21.1.jar`

## Configuration
Configuration files are generated in `vitality/cfg/` under the game run directory.

## License
MIT
