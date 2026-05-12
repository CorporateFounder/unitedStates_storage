# CITU Project Map

Phase 0 audit only. No source code changes were made.

## Root
- `C:\citu\unitedStates_final-master`: wallet/client Spring Boot project with Thymeleaf UI plus duplicated blockchain, mining, validation, account, transaction and sync code.
- `C:\citu\unitedStates_storage-master`: server/node/storage Spring Boot project with blockchain persistence, mining, validation, API, node sync and tournament logic.
- `C:\citu\website.citu-web-master`: public Spring Boot/Thymeleaf website.

## Build Systems

### Wallet: `C:\citu\unitedStates_final-master`
- Build file: `pom.xml`.
- Maven wrapper: `mvnw`, `mvnw.cmd`.
- Spring Boot parent: `org.springframework.boot:spring-boot-starter-parent:2.7.5`.
- Artifact: `com.UNITED_STATES_CORPORATION_OF_HUMANITY:unitedStates:0.32.216-SNAPSHOT`.
- Java version: `17`.
- Main class: `src\main\java\International_Trade_Union\UnitedStatesApplication.java`.
- Frontend: Thymeleaf templates in `src\main\resources\templates`; static assets in `src\main\resources\static`.
- Node/npm: none found. No `package.json` found.
- Gradle: none found.
- Tests: no `src\test` directory found by `rg --files src\test`.

Recommended commands:
- Run: `cd C:\citu\unitedStates_final-master; .\mvnw.cmd spring-boot:run`
- Build: `cd C:\citu\unitedStates_final-master; .\mvnw.cmd clean package`
- Test: `cd C:\citu\unitedStates_final-master; .\mvnw.cmd test` (no tests discovered, but Maven lifecycle is available)

### Server/storage: `C:\citu\unitedStates_storage-master`
- Build file: `pom.xml`.
- Maven wrapper: `mvnw`, `mvnw.cmd`.
- Spring Boot parent: `org.springframework.boot:spring-boot-starter-parent:2.7.5`.
- Artifact: `com.UNITED_STATES_CORPORATION_OF_HUMANITY:unitedStates:0.0.423-SNAPSHOT`.
- Java version: `17`.
- Main class: `src\main\java\International_Trade_Union\UnitedStatesApplication.java`.
- Storage resources: `src\main\resources\blockchain`, `balance`, `transactions`, `poolAddress`, `minerAccount`, plus runtime `server\resources\h2DB`.
- Node/npm: none found. No `package.json` found.
- Gradle: none found.
- Tests: `src\test\java\Testing.java`, `src\test\java\UtilsBalanceTest.java`.

Recommended commands:
- Run: `cd C:\citu\unitedStates_storage-master; .\mvnw.cmd spring-boot:run`
- Build: `cd C:\citu\unitedStates_storage-master; .\mvnw.cmd clean package`
- Test: `cd C:\citu\unitedStates_storage-master; .\mvnw.cmd test`

### Website: `C:\citu\website.citu-web-master`
- Build file: `pom.xml`.
- Maven wrapper: `mvnw`, `mvnw.cmd`.
- Spring Boot parent: `org.springframework.boot:spring-boot-starter-parent:3.1.2`.
- Artifact: `com.example:website.citu:0.0.24-SNAPSHOT`.
- Java version: `19`.
- Main class: `src\main\java\com\example\website\citu\Application.java`.
- Frontend: Thymeleaf templates in `src\main\resources\templates`; static assets in `src\main\resources\static`; CSS at `src\main\resources\static\css\styles.css`.
- Node/npm: none found. No `package.json` found.
- Gradle: none found.
- Tests: `src\test\java\com\example\website\citu\ApplicationTests.java`.

Recommended commands:
- Run: `cd C:\citu\website.citu-web-master; .\mvnw.cmd spring-boot:run`
- Build: `cd C:\citu\website.citu-web-master; .\mvnw.cmd clean package`
- Test: `cd C:\citu\website.citu-web-master; .\mvnw.cmd test`

## Scripts Found
- Only Maven wrappers were found: `mvnw`, `mvnw.cmd` in all three projects.
- No npm scripts, Gradle scripts, shell scripts, PowerShell scripts, or package managers were found beyond Maven.

## Immediate Audit Summary
- Wallet and server duplicate consensus classes under the same package namespace `International_Trade_Union`; V35 must keep these synchronized or extract/share a single consensus module later.
- Website is informational only, but duplicates chain model/entity classes for explorer display.
- Server has explicit node/tournament behavior; wallet has local mining/UI behavior and some API/sync logic.
