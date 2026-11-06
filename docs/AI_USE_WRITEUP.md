# AI use write-up - Final POE

**Tool:** Claude (Anthropic), used through the Claude chat interface.

## What I used it for
I gave Claude the Final POE brief, the marking rubric and my Part 1 and Part 2 project, and asked it to help complete the remaining requirements. It produced first drafts of:

- **Code generation:** Google sign-in with Firebase Authentication, the biometric lock, the offline sync queue (WorkManager worker, retry rules, restore after sign-in), Firebase Cloud Messaging handling, the language switcher, and the receipt photo picker.
- **API changes:** moving the ASP.NET Core API from SQLite to Cloud Firestore, adding Firebase token validation, receipt blob storage, device token registration and push notification endpoints, and a Dockerfile.
- **Translations:** the Afrikaans and isiZulu strings.
- **DevOps and documents:** the updated GitHub Actions workflow, README, release notes, setup guide and Play Store listing text, plus the launcher icon artwork.
- **Planning:** a list of the files that changed and a suggested order for committing them.

The Part 1 research and design were not changed by this, and my Part 2 code was kept as it was unless something had to change for the new features (the changes are listed in the release notes).

## What I did myself
I created the Firebase project and enabled Authentication, Firestore, Storage and Cloud Messaging. I downloaded and added `google-services.json` to the `app/` folder, configured my SHA-1 signing keys, deployed the backend API, and configured all necessary environment variables. Following that, I opened the project in Android Studio, built and ran the signed APK on my physical Android phone, tested Google sign-in, biometrics, airplane-mode offline sync queues, and push notifications. I resolved any minor configuration or build warnings, recorded the demo video with a live voice-over, and captured the required store screenshots.

## Checking and limits
Claude could not compile or run the code in its environment, so the code was only reviewed by reading it. When building the project locally, I verified dependency compatibility, checked Gradle synchronization, and ran the unit test suite to ensure all business calculators and sync policies passed successfully. The isiZulu translations were spot-checked and reviewed with a first-language speaker, and the Afrikaans translations were verified for local correctness.

## Where AI was not used
AI was not used for recording the physical device demo video, executing the Firebase project creation and hosting configurations, taking the on-device Play Store screenshots, or compiling and generating the final signed release build on my physical hardware.