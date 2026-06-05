# CITU V35 Phase 11G-4 Website 500 Number Format Hotfix Report

## 1. Executive summary

This phase fixed the website-only `NumberFormattingAspect` so number formatting is conservative and cannot throw `NumberFormatException` during page rendering for normal text, URLs, IP addresses, date-like values, slash-separated values, labels, already formatted values, or mixed text.

No VPS deployment was performed. No VPS process was restarted or killed. No server/node project file, wallet project file, `Seting.java`, consensus code, or resource folder was touched.

## 2. Root cause

The VPS stack trace pointed to:

```text
java.lang.NumberFormatException
at com.example.website.citu.config.NumberFormattingAspect.formatNumbers(...)
```

The failure mode is unsafe number formatting during controller/page rendering. Public website pages include values that look partially numeric but are not clean numbers, such as:

- URLs: `http://194.87.236.238:82/size`
- IP addresses: `194.87.236.238`
- dates: `2026-06-05`
- slash-separated values
- labels with embedded numbers
- already formatted values such as `1,261,440 GOLD/year`

The hotfix makes the aspect format only clean standalone numeric strings and leave everything else unchanged.

## 3. Files changed

Website-only:

- `C:\citu\website.citu-web-master\src\main\java\com\example\website\citu\config\NumberFormattingAspect.java`
- `C:\citu\website.citu-web-master\src\test\java\com\example\website\citu\config\NumberFormattingAspectTest.java`
- `C:\citu\CITU_V35_PHASE11G4_WEBSITE_500_NUMBER_FORMAT_HOTFIX_REPORT.md`

Local release artifact refreshed:

- `C:\citu\release-dryrun\v35-514291\citu-website-v35-activation-514291-1.0.0.jar`
- `C:\citu\release-dryrun\v35-514291\CITU_V35_514291_WEBSITE_SHA256SUMS.txt`

## 4. Hotfix behavior

The aspect now:

- leaves `null` unchanged
- leaves empty strings unchanged
- leaves normal text unchanged
- leaves URLs unchanged
- leaves IP addresses unchanged
- leaves date-like strings unchanged
- leaves slash-separated strings unchanged
- leaves strings with labels and numbers unchanged
- leaves already formatted strings unchanged
- formats only clean numeric strings
- supports integers
- supports decimal numbers
- supports negative numeric strings
- catches `NumberFormatException` defensively and returns the original value

Clean numeric examples:

- `12345` -> `12345.0000000000`
- `12345.6789` -> `12345.6789000000`
- `-123.45` -> `-123.4500000000`

Preserved examples:

- `http://194.87.236.238:82/size`
- `https://citucorp.com`
- `194.87.236.238`
- `2026-06-05`
- `v35/protocol`
- `Fork height: 514291`
- `1,234.00`
- `1,261,440 GOLD/year`

## 5. HTTP/HTTPS connector patch status

The Phase 11G-3 HTTP+HTTPS connector patch remains intact:

- HTTP-only startup remains supported.
- HTTPS on 443 with additional HTTP on 80 remains supported.
- Redirect remains configurable with `citu.https.redirect.enabled`.

## 6. Tests and package

Command run:

```bat
cd /d C:\citu\website.citu-web-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Result:

- Website tests: PASS, 28/28.
- Website clean package: PASS.

New tests added:

- clean integer string
- clean decimal string
- negative numeric string
- null value
- empty string
- normal text
- URL
- IP address
- date-like string
- slash-separated string
- string with labels and numbers
- already formatted string
- page-style mixed values do not throw
- map values only format clean numeric strings

Note:

- Maven warning remains about duplicate `org.bouncycastle:bcprov-jdk15on`; this was pre-existing and not changed in this phase.

## 7. Refreshed website jar

Final local artifact:

```text
C:\citu\release-dryrun\v35-514291\citu-website-v35-activation-514291-1.0.0.jar
```

New SHA256:

```text
52b732d886945421616f70909dfe86387f12ee0c70b7650ea06629e462001054  citu-website-v35-activation-514291-1.0.0.jar
```

## 8. Safe emergency HTTP-only command

Future command only. Not run in this phase.

```bat
java -Dserver.address=194.87.236.238 -Dserver.port=80 -Dserver.ssl.enabled=false -Dcitu.http.additional.enabled=false -Dcitu.https.redirect.enabled=false -jar C:\citu-website-v35-activation-514291-1.0.0.jar
```

## 9. Safe final HTTPS+HTTP command

Future command only. Not run in this phase.

```bat
set CITU_TLS_KEYSTORE_PASSWORD=<set-secret-on-vps>
java -Dserver.address=194.87.236.238 -Dserver.port=443 -Dserver.ssl.enabled=true -Dserver.ssl.key-store=file:C:/certificate/citucorp.com.pfx -Dserver.ssl.key-store-password=%CITU_TLS_KEYSTORE_PASSWORD% -Dserver.ssl.key-store-type=PKCS12 -Dcitu.http.additional.enabled=true -Dcitu.http.additional.port=80 -Dcitu.https.redirect.enabled=false -jar C:\citu-website-v35-activation-514291-1.0.0.jar
```

## 10. Safety confirmation

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
- No push was performed.
- No tag was created.
- No release asset upload was performed.
- No commit was created.
- `git add .` was not used.

## 11. Recommended next phase

Phase 11G-5: commit the website-only NumberFormattingAspect hotfix, refresh the website GitHub Release asset, verify public download SHA256, then proceed to a separately approved website-only VPS restart.

