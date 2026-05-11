# Repository Guidelines

## Project Structure & Module Organization

This is a Kotlin/Gradle multi-module project named `properties`.

- `server/` contains the Ktor JVM backend. Application modules live in `server/src/main/kotlin`, resources such as `application.yaml` and `logback.xml` live in `server/src/main/resources`, and tests live in `server/src/test/kotlin`.
- `client/` contains the Kotlin Multiplatform client module. Shared code is currently in `client/src/commonMain/kotlin`.
- `gradle/`, `gradlew`, and `gradlew.bat` provide the pinned Gradle wrapper and version catalog.
- Generated output is under `build/`, `server/build/`, `client/build/`, `.gradle/`, and Kotlin JS stores. Do not edit generated files directly.

## Build, Test, and Development Commands

Use the Gradle wrapper from the repository root:

- `.\gradlew.bat build` builds all modules and runs checks.
- `.\gradlew.bat :server:test` runs backend tests.
- `.\gradlew.bat :server:run` starts the Ktor server on the port configured in `server/src/main/resources/application.yaml` currently `8080`.
- `.\gradlew.bat :server:shadowJar` creates the runnable server fat jar.
- `.\gradlew.bat :client:build` builds the multiplatform client targets.

On Unix-like shells, use `./gradlew` instead of `.\gradlew.bat`.

## Coding Style & Naming Conventions

Use Kotlin idioms and the existing package namespace `com.pioneer`. Keep source files focused by feature or Ktor plugin, matching current names such as `Routing.kt`, `Mongo.kt`, and `Serialization.kt`.

Use four-space indentation. Prefer `camelCase` for functions and properties, `PascalCase` for classes and data classes, and descriptive endpoint/service names. Keep Ktor configuration split into extension-style `configure...` functions when adding server features.

## Testing Guidelines

Tests use `kotlin.test` and Ktor `testApplication`. Put backend tests in `server/src/test/kotlin` and name test classes after the behavior under test, for example `ServerTest` or `CarsRouteTest`.

Add or update tests for new routes, serialization behavior, database-facing service logic, and error handling. Run `.\gradlew.bat :server:test` before opening a pull request; run `.\gradlew.bat build` when changes touch shared Gradle, client, or cross-module code.

## Commit & Pull Request Guidelines

Recent commits use short, imperative summaries such as `Added Map View` and `Initial Setup`. Keep commits concise and focused; prefer messages like `Add car listing route` or `Fix Mongo update handling`.

Pull requests should include a brief description, the commands run for verification, and any configuration or migration notes. For API changes, document affected routes, request/response shapes, and expected status codes.

## Security & Configuration Tips

Keep secrets and environment-specific values out of source control. `application.yaml` should contain safe defaults only. When adding MongoDB or deployment configuration, prefer environment variables or external config and document required keys in the pull request.
