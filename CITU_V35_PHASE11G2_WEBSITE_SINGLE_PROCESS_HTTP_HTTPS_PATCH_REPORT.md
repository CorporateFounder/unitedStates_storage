# CITU V35 Phase 11G-2 Website Single Process HTTP+HTTPS Restore Patch Report

## 1. Executive summary

This phase patched the website project only so one Spring Boot website JVM can serve HTTPS on port 443 and add an HTTP connector on port 80 when explicitly launched in HTTPS mode.

No node/server project files were changed. No wallet project files were changed. No `Seting.java` file was changed. No VPS deployment, restart, process kill, push, tag, upload, burn transaction, or jackpot payout was performed.

## 2. Why HTTPS was missing

The current website configuration had SSL settings present, but active settings forced HTTP-only mode:

- `server.port=80`
- `server.ssl.enabled=false`

The old `HttpsRedirectFilter` source was fully commented out. The `Application.java` Tomcat additional connector code was also commented out and did not actively create a port 80 connector while HTTPS listened on port 443.

The VPS certificate file is `C:\certificate\citucorp.com.pfx`, while the old source default referenced `C:/certificate/cer.pfx`.

## 3. Files changed

Website-only files:

- `C:\citu\website.citu-web-master\src\main\java\com\example\website\citu\config\WebsiteConnectorConfig.java`
- `C:\citu\website.citu-web-master\src\main\java\com\example\website\citu\config\HttpsRedirectFilter.java`
- `C:\citu\website.citu-web-master\src\main\resources\application.properties`
- `C:\citu\website.citu-web-master\src\test\java\com\example\website\citu\config\WebsiteConnectorConfigTest.java`
- `C:\citu\website.citu-web-master\src\test\java\com\example\website\citu\config\HttpsRedirectFilterTest.java`
- `C:\citu\CITU_V35_PHASE11G2_WEBSITE_SINGLE_PROCESS_HTTP_HTTPS_PATCH_REPORT.md`

Local release artifact refreshed:

- `C:\citu\release-dryrun\v35-514291\citu-website-v35-activation-514291-1.0.0.jar`

## 4. Patch details

### Additional HTTP connector

Added `WebsiteConnectorConfig`, gated by properties:

- `server.ssl.enabled`
- `citu.http.additional.enabled`
- `server.port`
- `citu.http.additional.port`

The additional HTTP connector is created only when:

- SSL is enabled.
- `citu.http.additional.enabled=true`.
- the additional HTTP port is positive.
- the additional HTTP port differs from the primary server port.

This keeps HTTP-only mode safe. If launched with `server.port=80` and `server.ssl.enabled=false`, no extra connector is added.

### Redirect filter

Replaced the previously commented-out redirect filter with a configurable filter:

- `citu.https.redirect.enabled=false` by default.
- When disabled, normal HTTP pages pass through.
- When enabled, HTTP requests redirect to the same host and URI over HTTPS.

This avoids timeouts and avoids breaking HTTP access unless redirect is explicitly enabled.

### Certificate path default

Updated the default key-store path to:

```properties
server.ssl.key-store=file:C:/certificate/citucorp.com.pfx
```

No new certificate secret was added to code. The launch command below uses an environment variable for the password.

## 5. Exact VPS launch command for one-process 80+443

Future command only. Not run in this phase.

In `cmd.exe`:

```bat
set CITU_TLS_KEYSTORE_PASSWORD=<set-secret-on-vps>
java -Dserver.address=194.87.236.238 -Dserver.port=443 -Dserver.ssl.enabled=true -Dserver.ssl.key-store=file:C:/certificate/citucorp.com.pfx -Dserver.ssl.key-store-password=%CITU_TLS_KEYSTORE_PASSWORD% -Dserver.ssl.key-store-type=PKCS12 -Dcitu.http.additional.enabled=true -Dcitu.http.additional.port=80 -Dcitu.https.redirect.enabled=false -jar C:\citu-website-v35-activation-514291-1.0.0.jar
```

Expected result:

- HTTPS listens on `194.87.236.238:443`.
- HTTP listens on port `80` from the additional Tomcat connector.
- HTTP does not redirect unless `-Dcitu.https.redirect.enabled=true` is used.

Optional redirect mode:

```bat
set CITU_TLS_KEYSTORE_PASSWORD=<set-secret-on-vps>
java -Dserver.address=194.87.236.238 -Dserver.port=443 -Dserver.ssl.enabled=true -Dserver.ssl.key-store=file:C:/certificate/citucorp.com.pfx -Dserver.ssl.key-store-password=%CITU_TLS_KEYSTORE_PASSWORD% -Dserver.ssl.key-store-type=PKCS12 -Dcitu.http.additional.enabled=true -Dcitu.http.additional.port=80 -Dcitu.https.redirect.enabled=true -jar C:\citu-website-v35-activation-514291-1.0.0.jar
```

## 6. Exact rollback command

Future command only. Not run in this phase.

HTTP-only rollback:

```bat
java -Dserver.address=194.87.236.238 -Dserver.port=80 -Dserver.ssl.enabled=false -Dcitu.http.additional.enabled=false -Dcitu.https.redirect.enabled=false -jar C:\citu-website-v35-activation-514291-1.0.0.jar
```

Expected result:

- HTTP listens on port 80.
- No HTTPS connector is started.
- No redirect is applied.

## 7. Tests and build

Requested command:

```bat
mvn test
mvn clean package
```

Local machine result:

- `mvn` was not available on PATH.
- Used project wrapper instead:

```bat
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Results:

- Website tests: PASS, 18/18.
- Website clean package: PASS.
- Built jar: `C:\citu\website.citu-web-master\target\website.citu-0.0.24-SNAPSHOT.jar`
- Final copied artifact: `C:\citu\release-dryrun\v35-514291\citu-website-v35-activation-514291-1.0.0.jar`

## 8. Test coverage added

Added focused tests for:

- HTTP-only startup configuration does not create an additional connector.
- HTTPS mode with `citu.http.additional.enabled=true` creates an HTTP connector on port 80.
- Matching primary/additional ports are ignored.
- Redirect filter disabled mode allows HTTP requests through.
- Redirect filter enabled mode redirects HTTP requests to HTTPS.

## 9. Deployment note

This patch prepares a website jar suitable for replacing only the website artifact on the VPS after manual approval.

Before deployment/restart:

1. Confirm `C:\certificate\citucorp.com.pfx` exists on VPS.
2. Set the certificate password securely in the shell or service manager.
3. Confirm port 443 is still free.
4. Back up the current website jar.
5. Replace only the website jar.
6. Restart only the website process after explicit approval.
7. Verify:
   - `http://citucorp.com`
   - `https://citucorp.com`
   - `netstat -ano | findstr ":80"`
   - `netstat -ano | findstr ":443"`

## 10. Safety confirmation

- No `Seting.java` file was changed.
- No server/node project file was changed.
- No wallet project file was changed.
- No consensus logic was changed.
- `C:\server\resources` was not touched.
- `C:\resources` was not touched.
- No burn transaction was created.
- No jackpot payout was created.
- No VPS deployment was performed.
- No process was restarted.
- No process was killed.
- No source push was performed.
- No tag was created.
- No release asset upload was performed.
- No commit was created.
- `git add .` was not used.

## 11. Recommended next phase

Phase 11G-3: website-only VPS jar replacement and controlled website restart, after confirming the certificate file and backing up the old website jar.

