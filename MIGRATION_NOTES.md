# TruckDoc Mobile Migration Notes

## Build System Updates
- Updated to Android Gradle Plugin 8.4.0
- Updated to Gradle 8.6
- Updated to Kotlin 1.9.24
- Set Java compatibility to Java 17
- Added version catalog for dependency management
- Enabled view binding
- Enabled desugaring for Java 8+ features

## Dependency Migration Matrix

### Completed
- [x] ButterKnife → ViewBinding
  - Removed ButterKnife dependencies
  - Enabled view binding in build.gradle
  - Converted WizardActivity.java to use ViewBinding
  - Converted MessageAdapter.java to use ViewBinding
  - Converted BaseViewHolder.java to use ViewBinding
  - Converted ApnHelpWindow.java to use ViewBinding
  - Converted ImagesAdapter.java to use ViewBinding
  - Removed @BindView and @OnClick annotations
  - Added proper view binding initialization and cleanup
- [x] AndroidAnnotations → Plain Java/Kotlin + Jetpack
  - [x] Converted TextNotesFragment.java to use ViewBinding
  - [x] Converted ImagesFragment.java to use ViewBinding
  - [x] Converted CameraActivity.java to use ViewBinding
  - [x] Converted DashboardActivity.java to use ViewBinding
  - [x] Converted DialogActivity.java to use ViewBinding
  - [x] Converted UnauthorizedActivity.java to use ViewBinding
  - [x] Converted TruckdocPreferenceActivity.java to use ViewBinding
  - [x] Converted SplashActivity.java to use ViewBinding
  - [x] Converted ScannerActivity.java to use ViewBinding
  - [x] Converted RegisterActivity.java to use ViewBinding
  - [x] Converted NoConnectionFragment.java to use ViewBinding
  - [x] Converted NewMessageFragment.java to use ViewBinding
  - [x] Converted InboxFragment.java to use ViewBinding
  - [ ] Convert remaining fragments and activities
  - [ ] Remove AndroidAnnotations dependencies
- [ ] RxJava 1 → Kotlin Coroutines & Flow
- [ ] ORMLite → Room 2.7.1
- [ ] Picasso → Coil 3.x
- [ ] Retrofit 2.1 → Retrofit 2.11 + Moshi-Kotlin
- [ ] Dagger 2.35 → Dagger 2.55 + Hilt
- [ ] Fabric Crashlytics → Firebase Crashlytics
- [ ] Retrolambda → D8 desugaring
- [ ] Joda-Time → java.time (AndroidThreeTen)
- [ ] slf4j/logback → slf4j 2.0 + Timber 5

## Java to Kotlin Migration Guidelines
1. Keep existing Java code when migration cost is high
2. Write new code in Kotlin
3. Auto-convert Java to Kotlin where it simplifies (e.g., ButterKnife removal)
4. Use JetBrains @Nullable/@NonNull or Kotlin types for null safety

## Code Style Enforcement
- ktlint for Kotlin
- Google Java Format for Java
- Android Gradle Plugin Lint
- Detekt for Kotlin static analysis

## API Changes
- Package names remain unchanged (com.truckdoc.*)
- Public binary contracts stay compatible
- Unit & UI tests must compile
- Java tests converted to Kotlin only if needed

## Migration Steps
1. Update build system and dependencies
2. Migrate ButterKnife to ViewBinding
   - [x] Update build.gradle to enable view binding
   - [x] Remove ButterKnife dependencies
   - [x] Convert WizardActivity.java to use ViewBinding
   - [x] Convert MessageAdapter.java to use ViewBinding
   - [x] Convert BaseViewHolder.java to use ViewBinding
   - [x] Convert ApnHelpWindow.java to use ViewBinding
   - [x] Convert ImagesAdapter.java to use ViewBinding
3. Remove AndroidAnnotations
   - [x] Convert TextNotesFragment.java to use ViewBinding
   - [x] Convert ImagesFragment.java to use ViewBinding
   - [x] Convert CameraActivity.java to use ViewBinding
   - [x] Convert DashboardActivity.java to use ViewBinding
   - [x] Convert DialogActivity.java to use ViewBinding
   - [x] Convert UnauthorizedActivity.java to use ViewBinding
   - [x] Convert TruckdocPreferenceActivity.java to use ViewBinding
   - [x] Convert SplashActivity.java to use ViewBinding
   - [x] Convert ScannerActivity.java to use ViewBinding
   - [x] Convert RegisterActivity.java to use ViewBinding
   - [x] Convert NoConnectionFragment.java to use ViewBinding
   - [x] Convert NewMessageFragment.java to use ViewBinding
   - [x] Convert InboxFragment.java to use ViewBinding
   - [ ] Convert remaining fragments and activities
   - [ ] Remove AndroidAnnotations dependencies
4. Migrate RxJava to Coroutines/Flow
5. Migrate ORMLite to Room
6. Replace Picasso with Coil
7. Update Retrofit
8. Migrate Dagger to Hilt
9. Update Crashlytics
10. Remove Retrolambda
11. Migrate Joda-Time
12. Update logging system

## Notes
- Each migration step will be committed separately
- All migrations must preserve existing behavior
- Network payloads and DB schema must remain unchanged
- Analytics must continue to work as before

## ButterKnife to ViewBinding Migration Details
### Completed
- WizardActivity.java
  - Removed ButterKnife annotations
  - Added ActivityWizardBinding
  - Converted click listeners to lambda expressions
  - Added proper binding cleanup in onDestroy
  - Maintained all existing functionality

- MessageAdapter.java
  - Removed ButterKnife annotations
  - Added ListitemMessageInBinding and ListitemMessageOutBinding
  - Converted view holders to use ViewBinding
  - Maintained all existing functionality

- BaseViewHolder.java
  - Removed ButterKnife dependency
  - Simplified base class for ViewBinding support

- ApnHelpWindow.java
  - Removed ButterKnife annotations
  - Added WidgetFloatingWizardBinding
  - Converted click listeners to lambda expressions
  - Maintained all existing functionality

- ImagesAdapter.java
  - Removed ButterKnife annotations
  - Added ListitemImageBinding and ListitemImageButtonBinding
  - Converted view holders to use ViewBinding
  - Maintained all existing functionality

## AndroidAnnotations to ViewBinding Migration Details
### Completed
- TextNotesFragment.java
  - Removed @EFragment, @FragmentArg, @ViewById, and @AfterViews annotations
  - Added FragmentPageTextNotesBinding
  - Implemented proper fragment lifecycle methods
  - Added static factory method for fragment creation
  - Maintained all existing functionality

- ImagesFragment.java
  - Removed @EFragment, @FragmentArg, @ViewById, @AfterViews, @AfterInject, and @OnActivityResult annotations
  - Added FragmentPagePhotosBinding
  - Implemented proper fragment lifecycle methods
  - Added static factory method for fragment creation
  - Converted activity result handling to use standard Android APIs
  - Maintained all existing functionality

- CameraActivity.java
  - Removed @EActivity, @Extra, @ViewById, @AfterViews, and @Click annotations
  - Added ActivityCameraWizardBinding
  - Implemented proper activity lifecycle methods
  - Added static factory method for activity creation
  - Converted click listeners to use ViewBinding
  - Maintained all existing functionality

- DashboardActivity.java
  - Removed @EActivity, @ViewsById, and @Click annotations
  - Added ActivityHomeBinding
  - Implemented proper activity lifecycle methods
  - Converted click listeners to use ViewBinding
  - Maintained all existing functionality
  - Simplified button state management using ViewBinding

- DialogActivity.java
  - Removed @EActivity, @Extra, @ViewById, @AfterViews, and @Click annotations
  - Added ActivityDialogBinding
  - Implemented proper activity lifecycle methods
  - Added static factory method for activity creation
  - Converted click listeners to use ViewBinding
  - Maintained all existing functionality

- UnauthorizedActivity.java
  - Removed @EActivity, @Extra, @ViewById, @AfterViews, and @Click annotations
  - Added ActivityUnauthorizedBinding
  - Implemented proper activity lifecycle methods
  - Added static factory method for activity creation
  - Converted click listeners to use ViewBinding
  - Maintained all existing functionality

### Pending
- TruckdocPreferenceActivity.java
- SplashActivity.java
- ScannerActivity.java
- RegisterActivity.java
- NoConnectionFragment.java
- NewMessageFragment.java
- NotificationReceiver.java
- LocationReceiver.java

## Completed Tasks

### ViewBinding Migration
- [x] Converted `NewMessageFragment.java` to use ViewBinding
- [x] Converted `InboxFragment.java` to use ViewBinding
- [x] Converted `ImagesChoicePage.java` to use standard Android fragment creation
- [x] Converted `TextNotesPage.java` to use standard Android fragment creation
- [x] Converted `InboxActivity.java` to use standard Android fragment creation

### Service/Receiver Migration
- [x] Converted `NewMntService.java` to use standard Android IntentService
- [x] Converted `NewMessageService.java` to use standard Android IntentService
- [x] Converted `LocationReceiver.java` to use standard Android BroadcastReceiver
- [x] Converted `NotificationReceiver.java` to use standard Android BroadcastReceiver
- [x] Converted `FileActionIntentReceiver.java` to use standard Android intent creation
- [x] Converted `ConnectionRestoredReceiverForFileUpload.java` to use standard Android intent creation
- [x] Converted `CheckerConnectionReceiver.java` to use standard Android intent creation
- [x] Converted `IncomeMessagesAlarmManager.java` to use standard Android intent creation
- [x] Converted `BootCompletedJobIntentService.java` to use standard Android intent creation
- [x] Converted `MessageCheckService.java` to use standard Android intent creation
- [x] Converted `ResponseCheckHelper.java` to use standard Android intent creation
- [x] Converted `DashboardActivity.java` to use standard Android intent creation

### Dependency Removal
- [x] Removed AndroidAnnotations dependencies from all build.gradle files
- [x] Removed ButterKnife dependencies from all build.gradle files
- [x] Removed AndroidAnnotations generated code references
- [x] Removed ButterKnife bindings
- [x] Updated BaseViewHolder to use ViewBinding

## Pending Tasks

### Code Cleanup
- [ ] Remove any remaining generated files
- [ ] Clean up any unused imports
- [ ] Update any remaining hardcoded strings to use string resources
- [ ] Update any remaining hardcoded dimensions to use dimension resources
- [ ] Update any remaining hardcoded colors to use color resources

### Testing
- [ ] Test all converted fragments for proper lifecycle handling
- [ ] Test all converted services for proper intent handling
- [ ] Test all converted receivers for proper broadcast handling
- [ ] Test all converted activities for proper intent handling
- [ ] Test all converted views for proper binding
- [ ] Test all converted adapters for proper view holder binding

### Documentation
- [ ] Update README.md with new setup instructions
- [ ] Update build.gradle files with proper version catalogs
- [ ] Add migration guide for future reference
- [ ] Add troubleshooting guide for common issues 