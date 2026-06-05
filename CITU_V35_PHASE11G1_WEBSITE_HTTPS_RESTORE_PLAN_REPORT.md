# CITU V35 Phase 11G-1 Website HTTPS Restore Plan

## 1. Executive summary

This phase inspected the website source/configuration only. No code was changed, no process was restarted or killed, no deployment was performed, and no node/server resources were touched.

Current finding:

- The current website source contains SSL certificate settings for `C:/certificate/cer.pfx`.
- The current active configuration forces `server.port=80` and `server.ssl.enabled=false`.
- Current source does not actively configure a second embedded Tomcat connector for both HTTP 80 and HTTPS 443 in one process.
- A commented-out `TomcatServletWebServerFactory` customizer exists, but it only shows an additional HTTP connector on port 80 and is not active.
- A commented-out HTTPS redirect filter exists, but it is not active.
- Therefore, the new website starts only on HTTP/80 because SSL is explicitly disabled.

## 2. Files inspected

Website project:

- `C:\citu\website.citu-web-master\src\main\resources\application.properties`
- `C:\citu\website.citu-web-master\src\main\java\com\example\website\citu\Application.java`
- `C:\citu\website.citu-web-master\src\main\java\com\example\website\citu\config\HttpsRedirectFilter.java`
- `C:\citu\website.citu-web-master\src\main\java`
- `C:\citu\website.citu-web-master\src\main\resources`

Additional local check:

- `C:\certificate`

Local result:

- `C:\certificate` does not exist in this local development environment.
- This does not prove the VPS lacks it; the VPS must be checked before any HTTPS launch.

## 3. Current HTTPS-related configuration

`application.properties` currently contains:

```properties
#server.port=443
server.ssl.key-store=file:C:/certificate/cer.pfx
server.ssl.key-store-password=bioshoknewersion
server.ssl.key-store-type=PKCS12
server.port=80
server.ssl.enabled=false
```

Meaning:

- The certificate path is still present.
- The certificate password and type are still present.
- Port 443 is commented out.
- Port 80 is active.
- SSL is explicitly disabled.

This explains why the new website works on HTTP port 80 but does not listen on HTTPS port 443.

## 4. Application connector code

`Application.java` imports:

- `Connector`
- `TomcatServletWebServerFactory`
- `WebServerFactoryCustomizer`
- `Bean`

But the customizer is commented out:

```java
//  @Bean
//  public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainerCustomizer() {
//      return factory -> {
//          Connector httpConnector = createHttpConnector();
//          factory.addAdditionalTomcatConnectors(httpConnector);
//      };
//  }
```

The commented helper creates only an HTTP connector:

```java
//  private Connector createHttpConnector() {
//      Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
//      connector.setPort(80);
//      return connector;
//  }
```

Current conclusion:

- Dual 80/443 support is not active in current source.
- The visible commented connector does not itself configure HTTPS/443.
- The old one-PID 80+443 behavior is not reproduced by active current code.

## 5. HTTPS redirect code

`HttpsRedirectFilter.java` is fully commented out.

It previously contained a possible redirect from HTTP to HTTPS:

```java
// String httpsUrl = "https://" + request.getServerName()
//         + request.getRequestURI();
// response.sendRedirect(httpsUrl);
```

Current conclusion:

- There is no active HTTP-to-HTTPS redirect filter in current source.
- HTTP currently stays HTTP.

## 6. Did old website support HTTPS from code/config?

Based on the current source:

- HTTPS support was intended or previously configured via `server.ssl.*` properties.
- The certificate path was expected to be `C:/certificate/cer.pfx`.
- Current active source/config does not enable HTTPS.

Based on the manual VPS inventory:

- Old PID `4056` reportedly listened on both `0.0.0.0:80` / `[::]:80` and `194.87.236.238:443`.
- Its command included `java -Dserver.port=80 -Dserver.address=194.87.236.238 -jar C:\website.citu-0.0.30-SNAPSHOT.jar`.

This suggests one of these was true for the old running jar:

1. The old jar had different active connector code/config than the current source.
2. The process command line included additional SSL properties not visible in the summarized inventory.
3. Another configuration source or packaged property in the old jar enabled 443.

The current source alone does not show active one-process 80+443 support.

## 7. Is HTTPS support still present in current source?

Partially:

- Certificate settings are present in `application.properties`.
- SSL can likely be enabled by launch command if `C:\certificate\cer.pfx` exists on the VPS and the password is valid.

Not fully:

- `server.ssl.enabled=false` disables SSL by default.
- `server.port=80` starts HTTP only by default.
- No active second connector is present for simultaneous HTTP 80 and HTTPS 443 in one JVM.
- No active redirect filter is present.

## 8. Certificate path

Expected certificate path from current config:

```text
C:\certificate\cer.pfx
```

Config value:

```properties
server.ssl.key-store=file:C:/certificate/cer.pfx
```

Certificate type:

```properties
server.ssl.key-store-type=PKCS12
```

Before any HTTPS launch, verify on the VPS:

```powershell
Test-Path C:\certificate\cer.pfx
Get-ChildItem C:\certificate
```

Do not alter the certificate in this phase.

## 9. Can HTTPS be restored by launch command only?

### HTTPS-only process on 443

Yes, likely, if `C:\certificate\cer.pfx` exists on the VPS and the password is correct.

Future command, not run in this phase:

```bat
java -Dserver.address=194.87.236.238 -Dserver.port=443 -Dserver.ssl.enabled=true -Dserver.ssl.key-store=file:C:/certificate/cer.pfx -Dserver.ssl.key-store-password=bioshoknewersion -Dserver.ssl.key-store-type=PKCS12 -jar C:\citu-website-v35-activation-514291-1.0.0.jar
```

This would start the website on HTTPS/443. It does not also provide HTTP/80 in the same process.

### Keep current HTTP/80 and add HTTPS/443 as a second process

If port 80 is already served by the new website and port 443 is empty, HTTPS can likely be restored later by starting a second website process only on 443:

```bat
java -Dserver.address=194.87.236.238 -Dserver.port=443 -Dserver.ssl.enabled=true -Dserver.ssl.key-store=file:C:/certificate/cer.pfx -Dserver.ssl.key-store-password=bioshoknewersion -Dserver.ssl.key-store-type=PKCS12 -jar C:\citu-website-v35-activation-514291-1.0.0.jar
```

This is command-only, but it creates two website JVMs: one on 80 and one on 443.

### One process serving both 80 and 443

No, not with the current active source/config alone.

To run one JVM on both 80 and 443, the website needs either:

- a code patch adding an additional HTTP connector while the main server listens on HTTPS/443, or
- a reverse proxy/TLS terminator outside the Java app.

## 10. Minimal safe patch plan if one-process 80+443 is required

Do not apply this in Phase 11G-1. This is only a plan.

Patch option:

1. Set the main Spring Boot server to HTTPS/443:
   - `server.port=443`
   - `server.ssl.enabled=true`
   - keep `server.ssl.key-store=file:C:/certificate/cer.pfx`
   - keep PKCS12 settings
2. Add an active `WebServerFactoryCustomizer<TomcatServletWebServerFactory>` in `Application.java`.
3. Add one additional HTTP connector on port 80.
4. Optionally enable a redirect filter from HTTP to HTTPS if desired.
5. Build/test website.
6. Smoke locally with a non-privileged test pair of ports, for example 8085/8443, before VPS deployment.
7. Deploy only after explicit approval.

Alternative no-code infrastructure option:

1. Keep Java website on HTTP/80 or a local high port.
2. Use IIS/nginx/Apache or Windows HTTP.sys/TLS binding to terminate HTTPS/443.
3. Proxy to the Java app.

## 11. Why new website only starts on port 80

Because current active `application.properties` says:

```properties
server.port=80
server.ssl.enabled=false
```

The SSL key-store settings are present but inactive while SSL is disabled.

## 12. Recommended next phase

Recommended next phase:

**Phase 11G-2 website HTTPS restore dry-run/approval**

Before any action:

1. Confirm `C:\certificate\cer.pfx` exists on VPS.
2. Confirm no current listener on 443.
3. Decide desired topology:
   - two JVMs: existing HTTP/80 plus new HTTPS/443 process, or
   - one JVM with a connector patch, or
   - reverse proxy/TLS terminator.
4. If choosing command-only two-process restore, start only the HTTPS/443 website process after approval and verify:
   - `curl -I https://citucorp.com`
   - browser loads `https://citucorp.com`
   - `http://citucorp.com` still works
5. Do not touch node/server resources.

## 13. Safety confirmation

- No code was modified.
- No templates were modified.
- No `Seting.java` was modified.
- No consensus logic was changed.
- No website process was restarted.
- No process was killed.
- No deployment was performed.
- No node/server resources were touched.
- No burn transaction was created.
- No jackpot payout was created.
- No push was performed.
- No tag was created.
- No release upload was performed.
- No commit was created.

