# RevLogix setup guide

This is everything needed to run the final version: Firebase, the hosted API, the app and a signed release build.

## 1. Firebase project
1. Go to the [Firebase console](https://console.firebase.google.com) and create a project.
2. **Add an Android app** with the package name `com.revlogix.app`.
3. Add your **SHA-1 fingerprint** (Project settings > your Android app > Add fingerprint). Get the debug one with:
   ```
   ./gradlew signingReport
   ```
   Add the release keystore's SHA-1 as well once you have created it (step 5). Google sign-in will not work without the right SHA-1.
4. Download **google-services.json** and put it in the `app/` folder.
5. Turn on the services:
   - **Authentication** > Sign-in method > **Google**
   - **Firestore Database** > create database. Because only the API talks to Firestore, set the rules to deny direct access:
     ```
     rules_version = '2';
     service cloud.firestore {
       match /databases/{database}/documents {
         match /{document=**} { allow read, write: if false; }
       }
     }
     ```
   - **Storage** > get started. Note the bucket name shown at the top (for example `your-project.firebasestorage.app`). Firebase may ask for the pay-as-you-go (Blaze) plan to create a bucket; at student usage this stays within the free allowance, and you can set a budget alert.
   - **Cloud Messaging** is on by default.
6. Download a **service account key** for the API: Project settings > Service accounts > Generate new private key. Keep this JSON private. Never commit it.

## 2. Deploy the API (example: Render)
1. Push the repository to GitHub.
2. On [Render](https://render.com): New > Web Service > connect the repo.
3. Set **Root Directory** to `RevLogixApi` and **Runtime** to Docker (it uses `RevLogixApi/Dockerfile`).
4. Add environment variables:

   | Name | Value |
   |---|---|
   | `Firebase__ProjectId` | your Firebase project id |
   | `Firebase__CredentialsJson` | the whole service account JSON, pasted as one value |
   | `Firebase__StorageBucket` | the bucket name from the Storage page |

5. Deploy, then open the service URL in a browser. You should see `RevLogix API is running`.
6. On a free plan the service sleeps when idle and the first request can take about a minute. The app's sync retries automatically, but wake it up before recording the demo.

To run the API locally instead, set the same three variables in your shell and run `dotnet run --project RevLogixApi`. A real phone can only reach it over HTTPS, so use the hosted one for testing the app.

## 3. Point the app at the API
In `app/build.gradle.kts`, change `API_BASE_URL` to your service URL, keeping the trailing slash:
```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://your-service.onrender.com/\"")
```

## 4. Run on a physical phone
1. Enable Developer options and USB debugging on the phone, plug it in.
2. Run the `app` configuration from Android Studio.
3. Check each feature (this is also the order for the demo video):
   - Sign in with Google (register and log in)
   - Settings > turn on **Biometric unlock**, close the app and reopen it: the fingerprint / face prompt appears
   - Settings > change **Language** to Afrikaans, then isiZulu
   - Add a vehicle, fuel entry, an expense with a receipt photo, a maintenance record and a part
   - Switch on **airplane mode**, add an entry, see "changes waiting to sync" in Settings, switch airplane mode off and watch it clear
   - Show the same data appearing in the Firebase console (Authentication users, Firestore `users/...`, Storage `receipts/...`)
   - Settings > **Send test notification**, and add a maintenance record with a next-due odometer at or just above the current odometer to trigger an overdue / due soon push

## 5. Signed release build
1. Create a keystore (keep the file and passwords safe; losing them means you cannot update the app):
   ```
   keytool -genkey -v -keystore revlogix-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias revlogix
   ```
2. Create `keystore.properties` in the project root (it is git-ignored):
   ```
   storeFile=revlogix-release.jks
   storePassword=YOUR_STORE_PASSWORD
   keyAlias=revlogix
   keyPassword=YOUR_KEY_PASSWORD
   ```
3. Build:
   ```
   ./gradlew assembleRelease
   ```
   The signed APK is `app/build/outputs/apk/release/app-release.apk`. Install it on the phone and confirm Google sign-in still works (add the release SHA-1 to Firebase first).

## 6. Tag the final submission
On GitHub: Releases > Draft a new release > create the tag `Final-POE` on `main` > Publish.
