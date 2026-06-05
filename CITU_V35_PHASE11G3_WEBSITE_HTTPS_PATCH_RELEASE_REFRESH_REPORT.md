# CITU V35 Phase 11G-3 Website HTTPS Patch Release Refresh Report

## 1. Executive summary

This phase committed the website-only single-process HTTP+HTTPS patch, rebuilt the website artifact, refreshed the existing website GitHub Release assets, and verified the public download checksum.

No VPS deployment was performed. No VPS process was restarted or killed. No node/server or wallet project files were changed. No `Seting.java` file was changed.

## 2. Commit

Patch commit:

```text
f1fc9ff phase 11G3 restore website HTTPS connector
```

Files committed:

- `CITU_V35_PHASE11G1_WEBSITE_HTTPS_RESTORE_PLAN_REPORT.md`
- `CITU_V35_PHASE11G2_WEBSITE_SINGLE_PROCESS_HTTP_HTTPS_PATCH_REPORT.md`
- `website.citu-web-master/src/main/java/com/example/website/citu/config/HttpsRedirectFilter.java`
- `website.citu-web-master/src/main/java/com/example/website/citu/config/WebsiteConnectorConfig.java`
- `website.citu-web-master/src/main/resources/application.properties`
- `website.citu-web-master/src/test/java/com/example/website/citu/config/HttpsRedirectFilterTest.java`
- `website.citu-web-master/src/test/java/com/example/website/citu/config/WebsiteConnectorConfigTest.java`

Commit staging used explicit paths only. `git add .` was not used.

## 3. Tests and package

Website project:

```bat
cd /d C:\citu\website.citu-web-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Result:

- Website tests: PASS, 18/18.
- Website clean package: PASS.

Note:

- `mvn` was not available on PATH, so the project Maven wrapper was used.
- Maven warning remains about duplicate `org.bouncycastle:bcprov-jdk15on` dependency declaration; this was pre-existing and not changed in this phase.

## 4. Final website artifact

Final local artifact:

```text
C:\citu\release-dryrun\v35-514291\citu-website-v35-activation-514291-1.0.0.jar
```

SHA256:

```text
3db621d1f3a6c2f9126880375e6cb11ced5ac5e2e98eb1b9cb62fe7e3de48fc4  citu-website-v35-activation-514291-1.0.0.jar
```

SHA file:

```text
C:\citu\release-dryrun\v35-514291\CITU_V35_514291_WEBSITE_SHA256SUMS.txt
```

## 5. GitHub Release refresh

Repository:

```text
CorporateFounder/website.citu-web
```

Existing release tag:

```text
v35-website-activation-514291
```

Release URL:

```text
https://github.com/CorporateFounder/website.citu-web/releases/tag/v35-website-activation-514291
```

Assets refreshed with `--clobber`:

- `citu-website-v35-activation-514291-1.0.0.jar`
- `CITU_V35_514291_WEBSITE_SHA256SUMS.txt`

No node or wallet release assets were uploaded.

## 6. Public download verification

Downloaded from GitHub Release into a local temp verification folder:

- `citu-website-v35-activation-514291-1.0.0.jar`
- `CITU_V35_514291_WEBSITE_SHA256SUMS.txt`

Verification result:

- Expected SHA256 from downloaded SHA file:
  `3db621d1f3a6c2f9126880375e6cb11ced5ac5e2e98eb1b9cb62fe7e3de48fc4`
- Actual SHA256 of downloaded jar:
  `3db621d1f3a6c2f9126880375e6cb11ced5ac5e2e98eb1b9cb62fe7e3de48fc4`
- Result: PASS.

Patched jar confirmation:

- Downloaded jar contains:
  - `BOOT-INF/classes/com/example/website/citu/config/WebsiteConnectorConfig.class`
  - `BOOT-INF/classes/com/example/website/citu/config/HttpsRedirectFilter.class`

## 7. Exact VPS command after release verification

Future command only. Not run in this phase.

```bat
set CITU_TLS_KEYSTORE_PASSWORD=<set-secret-on-vps>
java -Dserver.address=194.87.236.238 -Dserver.port=443 -Dserver.ssl.enabled=true -Dserver.ssl.key-store=file:C:/certificate/citucorp.com.pfx -Dserver.ssl.key-store-password=%CITU_TLS_KEYSTORE_PASSWORD% -Dserver.ssl.key-store-type=PKCS12 -Dcitu.http.additional.enabled=true -Dcitu.http.additional.port=80 -Dcitu.https.redirect.enabled=false -jar C:\citu-website-v35-activation-514291-1.0.0.jar
```

Rollback command:

```bat
java -Dserver.address=194.87.236.238 -Dserver.port=80 -Dserver.ssl.enabled=false -Dcitu.http.additional.enabled=false -Dcitu.https.redirect.enabled=false -jar C:\citu-website-v35-activation-514291-1.0.0.jar
```

## 8. Safety confirmation

- No server/node project file was changed.
- No wallet project file was changed.
- No `Seting.java` file was changed.
- No consensus logic was changed.
- `C:\server\resources` was not touched.
- `C:\resources` was not touched.
- No VPS deployment was performed.
- No VPS process was restarted.
- No process was killed.
- No burn transaction was created.
- No jackpot payout was created.
- No node release asset was uploaded.
- No wallet release asset was uploaded.
- No new release tag was created.
- No new GitHub Release was created.
- Existing website GitHub Release assets were refreshed only for the website jar and website SHA256 file.

## 9. Recommended next phase

Phase 11G-4: website-only VPS deployment/restart approval, using the verified GitHub website release asset, after backing up the currently running website jar and confirming `C:\certificate\citucorp.com.pfx` is present on the VPS.

