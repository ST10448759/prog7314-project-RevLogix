# RevLogix release notes

## Version 2.0 - Final POE
Everything added or changed since the Part 2 prototype (1.0).

### New
- **Real single sign-on.** The stubbed sign-in is replaced with Google sign-in through Firebase Authentication. Signing in with a new account registers it.
- **Biometric authentication.** Optional fingerprint / face unlock, turned on in Settings and asked for each time the app opens. A new lock screen handles the prompt.
- **Offline mode with sync.** Every change is saved on the phone first and queued. A background job uploads the queue automatically when the phone reconnects, retrying with back-off. Settings shows how many changes are still waiting and has a "Sync now" button.
- **Restore on a new phone.** After signing in on a phone with no data, the user's vehicles and records are downloaded from the cloud.
- **Push notifications.** The API sends Firebase Cloud Messaging notifications when a maintenance record is overdue or due soon, and a test notification can be sent from Settings. Android 13+ permission request included.
- **Multi-language.** All screens now use string resources in English, Afrikaans and isiZulu. The language can be changed in Settings and applies straight away.
- **Receipt photos.** An expense can have a receipt photo, uploaded by the API to blob storage when the expense syncs.
- **Hosted NoSQL backend.** The API now stores data in Cloud Firestore (replacing the local SQLite file), verifies the Firebase token on every request, and runs in a Docker container.
- **Production assets.** New launcher icon (adaptive and legacy sizes), notification icon, Play Store icon and store listing text.
- **Android logging** throughout the app.
- **Release signing** support through a git-ignored `keystore.properties`.

### Changed
- `MainActivity` now extends `FragmentActivity` (needed by `BiometricPrompt`).
- Repositories write to the sync queue as well as Room.
- Bottom navigation labels, dialogs and settings use string resources instead of hard-coded text.
- The API base URL is set in one place (`app/build.gradle.kts`) and the app no longer allows plain HTTP.
- Vehicles belong to the signed-in account instead of a fixed test user.
- GitHub Actions also builds the API and keeps the APK and test report from each run.
- `.gitignore` fixed (the last lines were saved in the wrong encoding so they were being ignored).
- Version bumped to 2.0 (versionCode 2).

### Removed
- Stub SSO endpoint and the SQLite / Entity Framework data layer in the API.
- The unused `VehicleDto` and the direct API call from `VehicleViewModel` (now handled by the sync queue).

### Tests
- Added `SyncPolicyTest` for the retry rules of the sync queue.

## Version 1.0 - Part 2 prototype
Room database, fuel and expense tracking, maintenance records with Health Score, Custom Build Ledger, stubbed sign-in, settings screen, basic API and GitHub Actions unit tests.
