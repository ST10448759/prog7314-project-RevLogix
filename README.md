ST10448759
ST10452608

<p align="center">
  <img src="docs/play-store/ic_launcher_512.png" alt="RevLogix icon" width="120">
</p>

# RevLogix

A native Android vehicle management app built in Kotlin with Jetpack Compose, with a hosted ASP.NET Core REST API behind it. Developed for PROG7314 (Part 1: research and design, Part 2: prototype, Final POE: complete app).

**Demo video:** [Watch RevLogix Demo on YouTube](https://youtube.com/shorts/RPlhC2HFFvo?feature=share)

## Purpose
RevLogix helps vehicle owners track fuel economy, expenses, maintenance schedules, and aftermarket parts in one place. The Part 1 research report showed that each existing app only covers part of this need, and none of them handle custom build parts.

## Features

| Feature | How it is done |
| :--- | :--- |
| **Register / log in with SSO** | Google account through Android Credential Manager, exchanged for a Firebase Auth session. First sign-in creates the account. |
| **Biometric authentication** | AndroidX `BiometricPrompt` (fingerprint or face). Switched on in Settings, then asked every time the app opens. |
| **Settings** | Language, biometric unlock, sync status / manual sync, send a test push notification, sign out. |
| **REST API + database** | ASP.NET Core API storing each user's data in Cloud Firestore. |
| **Offline mode with sync** | Room is the source the UI reads from. Every change is also written to a `sync_queue` table and pushed by a WorkManager job once the phone is online. |
| **Real-time notifications** | Firebase Cloud Messaging. The API sends a push when a saved service is overdue or due soon, and on request from Settings. |
| **Multi-language** | English, Afrikaans and isiZulu using Android string resources. Switching language in Settings applies immediately. |
| **Fuel and expense tracking** | Fill-ups, automatic l/100km, expenses with an optional receipt photo stored in blob storage. |
| **Maintenance and Health Score** | Dual-trigger (date OR odometer) reminders, a 0-100 Health Score and badge tier. |
| **Custom Build Ledger** | Aftermarket parts grouped by category with part and serial numbers. |

## Design considerations
- **Offline first.** Users are often in a garage or on the road with poor signal, so the app never waits for the network. All reads come from Room; the network is only used by the background sync.
- **The API does the heavy lifting.** The phone never talks to Firestore or Cloud Storage directly. It sends changes to the API, which validates the user, writes to the database, stores receipt images and decides when to send notifications.
- **Safe retries.** Records are stored under the id the phone gave them, so re-sending a change after a dropped connection overwrites the same document instead of creating a duplicate.
- **Security.** Every API call carries the user's Firebase ID token, which the API validates; a user can only reach `users/{their uid}/...`. Service account keys are only ever set as environment variables on the host, never committed. HTTP traffic is disabled in the app (HTTPS only).
- **Usable when things go wrong.** Sign-in, sync and notification failures are handled and shown as short messages instead of crashing the app. If biometrics are removed from the phone, the lock screen offers sign out.

## Architecture

```mermaid
flowchart LR
    subgraph Phone[Android app - Kotlin / Compose]
        UI[Compose screens] --> VM[ViewModels]
        VM --> Repo[Repositories]
        Repo --> Room[(Room database)]
        Repo --> Queue[(sync_queue)]
        Queue --> Worker[SyncWorker - WorkManager]
    end
    Worker -- HTTPS + Firebase ID token --> API[ASP.NET Core API]
    API --> Firestore[(Cloud Firestore)]
    API --> Blob[(Cloud Storage - receipts)]
    API -- push --> FCM[Firebase Cloud Messaging]
    FCM --> Phone
    Phone -- Google sign-in --> Auth[Firebase Authentication]















