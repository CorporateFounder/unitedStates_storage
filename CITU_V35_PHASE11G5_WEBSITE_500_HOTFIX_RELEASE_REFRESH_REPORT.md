# CITU V35 Phase 11G-5 Website 500 Hotfix Release Refresh Report

## 1. Executive summary

This phase committed the website-only `NumberFormattingAspect` 500 hotfix, rebuilt the website jar, refreshed the existing website GitHub Release assets, and verified the public download SHA256.

No VPS deployment was performed. No VPS process was restarted or killed. No server/node or wallet project files were changed. No `Seting.java` file was changed.

## 2. Commit

Hotfix commit:

```text
68c31bc phase 11G5 fix website number formatting hotfix
```

Files committed:

- `CITU_V35_PHASE11G3_WEBSITE_HTTPS_PATCH_RELEASE_REFRESH_REPORT.md`
- `CITU_V35_PHASE11G4_WEBSITE_500_NUMBER_FORMAT_HOTFIX_REPORT.md`
- `website.citu-web-master/src/main/java/com/example/website/citu/config/NumberFormattingAspect.java`
- `website.citu-web-master/src/test/java/com/example/website/citu/config/NumberFormattingAspectTest.java`

Commit staging used explicit paths only. `git add .` was not used.

## 3. Tests and package

Website project:

```bat
cd /d C:\citu\website.citu-web-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Result:

- Website tests: PASS, 28/28.
- Website clean package: PASS.

Note:

- Maven warning remains about duplicate `org.bouncycastle:bcprov-jdk15on` dependency declaration; this was pre-existing and not changed in this phase.

## 4. Final website jar SHA256

Final local artifact:

```text
C:\citu\release-dryrun\v35-514291\citu-website-v35-activation-514291-1.0.0.jar
```

SHA256:

```text
a9a31d0de0c4df5d781fca3f7b5abd616cfe4ee1522b6a37dbf4facd2fc612ce  citu-website-v35-activation-514291-1.0.0.jar
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
  `a9a31d0de0c4df5d781fca3f7b5abd616cfe4ee1522b6a37dbf4facd2fc612ce`
- Actual SHA256 of downloaded jar:
  `a9a31d0de0c4df5d781fca3f7b5abd616cfe4ee1522b6a37dbf4facd2fc612ce`
- Result: PASS.

Patched jar confirmation:

- Downloaded jar contains:
  - `BOOT-INF/classes/com/example/website/citu/config/WebsiteConnectorConfig.class`
  - `BOOT-INF/classes/com/example/website/citu/config/HttpsRedirectFilter.class`
  - `BOOT-INF/classes/com/example/website/citu/config/NumberFormattingAspect.class`

## 7. Exact VPS HTTP-only emergency command

Future command only. Not run in this phase.

```bat
java -Dserver.address=194.87.236.238 -Dserver.port=80 -Dserver.ssl.enabled=false -Dcitu.http.additional.enabled=false -Dcitu.https.redirect.enabled=false -jar C:\citu-website-v35-activation-514291-1.0.0.jar
```

## 8. Exact VPS HTTPS+HTTP final command

Future command only. Not run in this phase.

```bat
set CITU_TLS_KEYSTORE_PASSWORD=<set-secret-on-vps>
java -Dserver.address=194.87.236.238 -Dserver.port=443 -Dserver.ssl.enabled=true -Dserver.ssl.key-store=file:C:/certificate/citucorp.com.pfx -Dserver.ssl.key-store-password=%CITU_TLS_KEYSTORE_PASSWORD% -Dserver.ssl.key-store-type=PKCS12 -Dcitu.http.additional.enabled=true -Dcitu.http.additional.port=80 -Dcitu.https.redirect.enabled=false -jar C:\citu-website-v35-activation-514291-1.0.0.jar
```

## 9. Safety confirmation

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

## 10. Recommended next phase

Phase 11G-6: website-only VPS deployment/restart approval using the verified GitHub website release asset, after backing up the currently running website jar and confirming the intended HTTP-only or HTTPS+HTTP launch mode.

