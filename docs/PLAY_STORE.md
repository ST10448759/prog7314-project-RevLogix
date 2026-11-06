# Google Play Store preparation

## Store listing text
**App name:** RevLogix

**Short description (max 80 characters):**
Fuel, service and build tracker for your car - works offline.

**Full description:**
RevLogix keeps everything about your vehicle in one place.

- Log fuel fill-ups and see your average L/100km automatically
- Track expenses and attach a photo of the receipt
- Never miss a service: reminders trigger by date or by odometer, whichever comes first, and you get a vehicle Health Score
- Keep a ledger of aftermarket parts with brand, part number and serial number
- Works offline: add entries anywhere and they sync when you are back online
- Sign in with Google, lock the app with your fingerprint or face
- Available in English, Afrikaans and isiZulu

**Category:** Auto & Vehicles
**Content rating:** complete the questionnaire in Play Console (no violence, no user-generated sharing)

## Assets
| Asset | Status |
|---|---|
| App icon 512 x 512 PNG | `docs/play-store/ic_launcher_512.png` |
| Launcher icon in the app | adaptive icon + legacy sizes in `app/src/main/res` |
| Signed production APK | build with `./gradlew assembleRelease` (see `docs/SETUP.md`) |
| Phone screenshots (at least 2) | take on the physical phone and save in `docs/screenshots/` |

Suggested screenshots: sign-in, vehicle list, fuel screen with the average economy card, Health Score screen, Build Ledger, Settings with the language dialog.

## Before a real publish
- Privacy policy URL (required: the app uses Google account details and push notification tokens)
- Data safety form: account info (email, name) and device token are collected; data is encrypted in transit
- Feature graphic 1024 x 500 PNG
- Upload an Android App Bundle (`./gradlew bundleRelease`) - Play requires `.aab` for new apps
