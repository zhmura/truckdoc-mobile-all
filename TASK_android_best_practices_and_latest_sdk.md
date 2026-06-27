# Task: Android Best-Practices Verification + Latest SDK Support & Test

## Summary
Audit the TruckDoc Android client against current Android best practices, then upgrade
the build toolchain and `targetSdk`/`compileSdk` to the latest supported level and verify
the app builds, installs, and runs correctly on a current Android device/emulator.

This is required because the app is **not compliant with the current Google Play target-API
policy** and depends on several end-of-life libraries.

---

## Current state (baseline)
Captured from the repo at task-creation time.

| Setting | Current value | Source |
|---|---|---|
| Android Gradle Plugin | `4.2.2` | `build.gradle` |
| Gradle wrapper | `7.3.3` | `gradle/wrapper/gradle-wrapper.properties` |
| Kotlin | `1.7.10` | `build.gradle` |
| `minSdkVersion` | `26` | `build.gradle` (ext) |
| `compileSdkVersion` | `31` | `build.gradle` (ext) |
| `targetSdkVersion` | `31` | `build.gradle` (ext) |
| `buildToolsVersion` | `30.0.0` / `30.0.3` | `build.gradle` / `application/build.gradle` |
| Java source/target | `1.8` | `application/build.gradle` |
| `versionCode` / `versionName` | `39` / `3.9` | `application/build.gradle` |

### Known legacy / EOL dependencies and smells
- **Crashlytics Fabric SDK** `com.crashlytics.sdk.android:crashlytics:2.10.1` — Fabric is shut down; must migrate to Firebase Crashlytics.
- **ButterKnife** `10.2.1` — officially deprecated / EOL (view binding is the replacement).
- **AndroidAnnotations** `4.7.0` — effectively unmaintained.
- **RxJava 1.x** (`io.reactivex:rxjava:1.3.8`, `rxandroid:1.2.1`) — EOL; RxJava 1 is no longer maintained.
- **Picasso** `2.5.2`, **OrmLite** `4.48`, bundled `httpclient-1.1.1.jar` — very old.
- Legacy `com.android.support` / `support-v4` exclusions indicate incomplete AndroidX migration in transitive deps.
- Duplicate dependency / `dexOptions` blocks in `application/build.gradle`.
- `configurations.all { resolutionStrategy { failOnVersionConflict() } }` will make any version bump fail loudly (good for safety, painful during upgrade).

### Compliance gap (Google Play, as of 2026-06)
- **New apps & updates** must `targetSdk` **35 (Android 15)** or higher to be accepted by Google Play.
- **Existing apps** must target **34 (Android 14)** or higher to remain discoverable to new users on newer Android OS versions.
- Current `targetSdk 31` ⇒ **updates are blocked** and the app **loses discoverability** on Android 12+ devices.
- Latest stable platform is **Android 16 (API 36)**; preferred end state is `compileSdk 36`, `targetSdk 35+`.

---

## Goals / Definition of Done
1. A documented best-practices audit with pass/fail findings and prioritized remediation list.
2. Toolchain + SDK upgraded so the app is **Google Play target-API compliant** (`targetSdk >= 35`, `compileSdk >= 35`, ideally `36`).
3. App builds a signed release, installs, and passes a smoke test on a **physical/emulated Android 15 (API 35)** device.
4. The full API endpoint suite (register, sync, syncCheck, messages, upload-file, config update, maintenance report, route, log upload) still works after the upgrade — reuse the existing endpoint verification.

---

## Part A — Best-practices verification (audit)
Produce a checklist report. For each item: status (✅/⚠️/❌), evidence, and remediation note.

### A1. Build & toolchain
- [ ] AGP, Gradle, Kotlin, build-tools on currently supported versions.
- [ ] Java 8 → at least Java 11/17 source compatibility (required by newer AGP).
- [ ] Remove duplicate dependency declarations and duplicate `dexOptions` blocks.
- [ ] Reconcile `failOnVersionConflict()` strategy during/after the upgrade.
- [ ] Replace deprecated DSL (`compileSdkVersion`/`buildToolsVersion`/`lintOptions`) with current equivalents.

### A2. Manifest, permissions & components
- [ ] Every `Activity`/`Service`/`Receiver` with an intent filter declares `android:exported` explicitly (mandatory since API 31).
- [ ] Runtime permission requests for dangerous permissions (SMS, location, camera, storage, notifications).
- [ ] `POST_NOTIFICATIONS` runtime permission handled (required since API 33).
- [ ] Scoped storage compliance (no broad `WRITE_EXTERNAL_STORAGE` reliance; the log/photo code uses `Environment.getExternalStorageDirectory()` which must move to app-scoped or MediaStore APIs).
- [ ] Foreground services declare `foregroundServiceType` (required since API 34) and post a notification.
- [ ] `PendingIntent` uses `FLAG_IMMUTABLE`/`FLAG_MUTABLE` (required since API 31).
- [ ] Exact-alarm usage reviewed (`AlarmManager` in `checker` module) against API 31+/34 restrictions.

### A3. Background work & networking
- [ ] Background sync/message-check moved to/aligned with `WorkManager` / `JobScheduler` constraints (Doze/background-execution limits).
- [ ] Cleartext / mixed traffic disabled; network security config present; all endpoints HTTPS.
- [ ] Modern HTTP stack (drop bundled `httpclient-1.1.1.jar`; standardize on OkHttp/Retrofit).

### A4. Security & privacy
- [ ] No secrets committed (`keystore.properties`, signing material) — verify they are gitignored.
- [ ] Migrate off shut-down Crashlytics/Fabric SDK.
- [ ] Data Safety form items reviewed (location, SMS, device identifiers collected).
- [ ] R8/ProGuard rules valid for upgraded deps.

### A5. UI / lifecycle / API hygiene
- [ ] Replace removed/deprecated APIs flagged by `compileSdk` bump (e.g. `onBackPressed`, `getColor`, package visibility).
- [ ] Edge-to-edge / window inset handling for Android 15 (API 35 enforces edge-to-edge).
- [ ] Lint run with `abortOnError true` in CI (currently `false`).

---

## Part B — Latest SDK support + test
Incremental, one API level at a time to isolate breakages.

### B1. Toolchain upgrade
- [ ] Bump Gradle wrapper and AGP to versions that support `compileSdk 35/36`.
- [ ] Bump Kotlin and Java (source/target 17) as required by the new AGP.
- [ ] Bump `buildToolsVersion` accordingly.

### B2. SDK bump
- [ ] `compileSdkVersion` → `35` (then `36`).
- [ ] `targetSdkVersion` → `35` (Play-compliant), keep `minSdkVersion` at `26` (or re-evaluate).
- [ ] Resolve all new compile errors / deprecations introduced by the bump.
- [ ] Update behavior-change-affected code (foreground service types, exact alarms, edge-to-edge, notifications permission).

### B3. Dependency modernization (as needed to compile on new SDK)
- [ ] Firebase Crashlytics replaces Fabric Crashlytics.
- [ ] AndroidX artifacts only (remove `com.android.support` exclusions once transitives are clean).
- [ ] Evaluate replacing ButterKnife with view binding and RxJava 1 → RxJava 3 (can be staged as follow-ups if out of scope).

### B4. Build & runtime test matrix
- [ ] `./gradlew :application:assembleDefaultClientRelease` succeeds (signed).
- [ ] Install + launch on **API 35** emulator and a physical device.
- [ ] Smoke test core flows: registration, dashboard, send message (text + file), maintenance report, sync, log upload.
- [ ] Re-run the **API endpoint verification** (see existing `ApiIntegrationTest` and the verified endpoint matrix) against the backend and confirm all pass.
- [ ] Regression check on `minSdk 26` device to confirm no lower-bound breakage.

---

## Acceptance criteria
- Audit report (Part A) committed with findings and prioritized backlog.
- `targetSdk >= 35`, `compileSdk >= 35`; project builds a signed release without errors.
- App launches and passes the smoke + endpoint test matrix on API 35 and on `minSdk 26`.
- No EOL Crashlytics/Fabric SDK in the dependency graph.
- CI builds the release variant; lint runs and is triaged.

## Risks & notes
- `failOnVersionConflict()` will surface many transitive conflicts during the bump — expect iterative `resolutionStrategy.force` work.
- AndroidAnnotations + ButterKnife + Dagger annotation processors are sensitive to Java/Kotlin/AGP versions; upgrade them together.
- Android 15 edge-to-edge enforcement and foreground-service-type rules are the most likely runtime regressions.
- Keep `minSdk` decisions explicit — raising it drops old devices but removes compatibility shims.

## Suggested sub-tasks / sequencing
1. A — Run audit, produce findings report (no code changes).
2. B1 — Toolchain upgrade (AGP/Gradle/Kotlin/Java) on a branch; get a green build at `compileSdk 31`.
3. B2 — Bump to `compileSdk/targetSdk 35`, fix breakages.
4. B3 — Migrate EOL deps required to compile (Crashlytics first).
5. B4 — Full build + device + endpoint test matrix.
6. Bump to `compileSdk 36` once `35` is stable.

---

# Part A — Audit findings (executed)

## Environment reality (drives the approach)
- Installed SDK (`~/Android/Sdk`) only has **platforms 34, 35, 36** and **build-tools 34.0.0, 35.0.0, 36.1.0** — **no API 31 / build-tools 30**.
  ⇒ The current `compileSdk 31` / `buildTools 30.0.3` config **cannot build in this environment**; an upgrade is mandatory just to compile.
- JDK **17** is installed (good — required to run AGP 8).
- `cmdline-tools` is empty (no `sdkmanager`), so additional SDK packages can't be installed locally; we must target an installed platform (34/35/36).
- Consequence: must move to **AGP 8.x** (only AGP ≥ 8.1 supports `compileSdk` 34+; ≥ 8.5 for 35; ≥ 8.6 for 36). AGP 4.2.2 also won't run reliably on JDK 17.

## Findings by area

### Build / toolchain — ❌ must change
- AGP `4.2.2`, Gradle `7.3.3` are far too old for `compileSdk 34+`.
- Duplicate `dexOptions` blocks and duplicate `slf4j`/`logback` declarations in `application/build.gradle`.
- Deprecated DSL throughout (`compileSdkVersion`, `lintOptions`, `dexOptions`, nested `android {}` in `camera`, duplicate `buildToolsVersion` in `checker`/`standout`).
- `failOnVersionConflict()` will surface many conflicts once versions move (relaxed during migration).
- Old plugins incompatible with AGP 8: **`com.jakewharton.butterknife` Gradle plugin**, `dexcount 2.0.0`, `gradle-versions 0.33.0`.

### EOL / dead dependencies
- **Crashlytics Fabric** `com.crashlytics.sdk.android:crashlytics:2.10.1@aar` — ❌ Fabric maven repo is shut down and not even declared; **unresolvable**. Good news: in code it is used **only** for the `Crashlytics.TAG` log-tag constant (3 files) → safe to drop the dependency and replace the tag.
- **ButterKnife** (`@BindView`/`@OnClick` in ~6 files) — ⚠️ EOL. The annotation processor can still run on AGP 8; the **Gradle plugin** (R2 generation) cannot and must be removed.
- **AndroidAnnotations** `4.7.0` (`@EActivity`/`@EFragment`… in ~17 files, generates the `*_` classes referenced in the manifest) — ⚠️ unmaintained; highest-risk item for AGP 8 compatibility. Bump to `4.8.0`.
- **RxJava 1.x**, **Picasso 2.5.2**, **OrmLite 4.48**, bundled `httpclient-1.1.1.jar` — ⚠️ very old; not strictly blocking the SDK bump, staged as follow-ups.

### Manifest / permissions — ⚠️
- `android:usesCleartextTraffic="true"` + `requestLegacyExternalStorage="true"` — cleartext should be removed (all verified endpoints are HTTPS); legacy storage is ignored at `targetSdk 30+`.
- Broad storage model: `MANAGE_EXTERNAL_STORAGE` + `WRITE_EXTERNAL_STORAGE` + direct `Environment.getExternalStorageDirectory()` use (logs, photos, FileHelper) — ❌ not scoped-storage compliant; Play restricts `MANAGE_EXTERNAL_STORAGE`.
- `MessageCheckService` is a foreground service but declares no `android:foregroundServiceType` — ❌ required at API 34.
- `SCHEDULE_EXACT_ALARM` present — review against API 33/34 exact-alarm policy.
- No `POST_NOTIFICATIONS` runtime permission handling — ⚠️ required at API 33.
- Exported components are explicitly flagged (✅ `exported` set where intent filters exist).

### Code-level API hygiene
- ✅ Most `PendingIntent`s already use `FLAG_IMMUTABLE`/`FLAG_MUTABLE`.
- ❌ `mobile-modules/camera/.../NotificationHelper.java:88` uses `PendingIntent.getActivity(context, 0, new Intent(), 0)` — missing mutability flag → crashes on API 31+. (Fixed in Part B.)
- ⚠️ `getResources().getColor(...)` (deprecated) in many UI files; `onBackPressed()` overrides (deprecated at API 33). Non-blocking; flagged for follow-up.
- ⚠️ Android 15 (API 35) enforces edge-to-edge; window-inset handling needs a UI pass.

## Prioritized remediation backlog
1. **P0 (blocking build):** AGP/Gradle/JDK upgrade; namespaces + manifest `package` removal; remove dead Crashlytics dep; remove ButterKnife Gradle plugin; relax `failOnVersionConflict`; drop incompatible `dexcount`/old `gradle-versions`.
2. **P1 (behavior at targetSdk 35):** `foregroundServiceType`; `POST_NOTIFICATIONS`; scoped storage; remove cleartext; edge-to-edge.
3. **P2 (modernization):** Firebase Crashlytics; replace ButterKnife/AndroidAnnotations; RxJava 1→3; refresh Picasso/OrmLite/HTTP stack.

---

# Part B — Migration executed (build green at targetSdk 35)

## Result
- ✅ `:app:assembleDefaultClientDebug` — **SUCCESS** (`com.sanda.truckdoc.client-39.apk`, ~10 MB).
- ✅ `:app:assembleDefaultClientRelease` — **SUCCESS** (signed, R8-minified, ~4.2 MB).
- ✅ Verified APK: `compileSdkVersion 35`, `targetSdkVersion 35`, `minSdk 26` → **Google Play target-API compliant**.

## Changes made
**Build toolchain**
- AGP `4.2.2` → `8.5.2`; Gradle wrapper `7.3.3` → `8.7`; Kotlin `1.7.10` → `1.9.24`; runs on JDK 17.
- `compileSdk`/`targetSdk` `31` → `35`; `buildTools` `30.0.x` → `35.0.0` (via root `ext`).
- Removed AGP-8-incompatible plugins: `dexcount`, ButterKnife Gradle plugin; bumped `gradle-versions` to `0.51.0`.
- Relaxed `failOnVersionConflict()` during migration (use `preferProjectModules()`; re-enable with `force` rules later).
- `gradle.properties`: `-Xmx4g`, `nonTransitiveRClass=false`.
- Added `local.properties` (`sdk.dir`).

**Per-module (AGP 8 requirements)**
- Added `namespace` to all 5 Android modules (`app`, `camera`, `checker`, `standout`, `truckdocnetwork`) and removed `package=` from their manifests.
- Migrated DSL: `compileSdkVersion`→`compileSdk`, `lintOptions`→`lint`, removed `dexOptions`, removed duplicate `buildToolsVersion`, removed nested `android {}` (camera).
- Enabled `buildFeatures { buildConfig true }` for `app` and `checker` (off by default in AGP 8).
- Fixed AndroidAnnotations `annotationProcessorOptions` (the two `arguments =` assignments overwrote each other; `resourcePackageName` now correctly set to the namespace).
- Central `JavaCompile` fork with `--add-exports/--add-opens` for `jdk.compiler` (lets ButterKnife/AndroidAnnotations processors run on JDK 17).

**Dependencies / code**
- Removed dead **Crashlytics Fabric** dependency; replaced its only live use (`Crashlytics.TAG`) with a local constant.
- Removed duplicate `slf4j`/`logback` declarations.
- Bumped AndroidAnnotations `4.7.0`→`4.8.0` (app) and `4.6.0`→`4.8.0` (camera).
- PhotoView `1.3.1` (unresolvable) → `2.3.0`; updated import `uk.co.senab.photoview`→`com.github.chrisbanes.photoview`.
- Added `javax.annotation:javax.annotation-api:1.3.2` to `truckdoc-client-api` (generated POJOs import `javax.annotation.Generated`, removed from JDK 9+).
- Fixed `MediaMetadataRetriever.release()` now throwing `IOException` at SDK 35 (camera).
- Fixed `camera/NotificationHelper` `PendingIntent` missing mutability flag → `FLAG_IMMUTABLE`.
- Added `application/proguard-rules.pro` (was referenced but missing) with `-dontwarn com.squareup.okhttp.**` (Picasso 2.5.2 → OkHttp 2.x) and conservative keep rules for Jackson/OrmLite/Retrofit/RxJava.

## Still open (not blocking the build; tracked from Part A)
- **P1 runtime/behavior at targetSdk 35:** add `foregroundServiceType` to `MessageCheckService`; handle `POST_NOTIFICATIONS`; scoped-storage migration (off `MANAGE_EXTERNAL_STORAGE` + `getExternalStorageDirectory`); remove `usesCleartextTraffic`; edge-to-edge insets.
- **On-device verification:** install + smoke test on an API 35 device/emulator and re-run the endpoint matrix on-device (API endpoints already verified against the backend separately). No device/emulator was available in the build environment.
- **P2 modernization:** Firebase Crashlytics; replace ButterKnife/AndroidAnnotations; RxJava 1→3; refresh Picasso/OrmLite/HTTP stack; re-enable `failOnVersionConflict()`.
