# RevLogix

A native Android vehicle management app built in Kotlin with Jetpack Compose, developed for PROG7314 Part 2.

## Purpose
RevLogix helps vehicle owners track fuel economy, expenses, maintenance schedules, and aftermarket parts in one place, addressing the gap identified in the Part 1 research report (Drivvo, Simply Auto, and Fuelly each cover only part of this need).

## Architecture
- **UI:** Jetpack Compose, Material 3, dark automotive theme
- **Pattern:** MVVM (Compose UI → ViewModel → Repository → Room)
- **Local storage:** Room database (offline-first)
- **Navigation:** Jetpack Navigation Compose with bottom nav bar

## Features
- SSO sign-in flow
- Multi-vehicle profile management
- Fuel & Expense tracking with automatic fuel economy calculation
- Maintenance records with dual-trigger (time OR mileage) reminders and a Health Score
- Custom Build Ledger for aftermarket parts (audio, suspension, performance)
- Settings: language selection (English/isiZulu/Afrikaans), biometric toggle

## Version Control & CI
Commits are made incrementally per feature throughout development. A GitHub Actions workflow (`.github/workflows/android-ci.yml`) runs unit tests and builds a debug APK automatically on every push to `main`.

## Unit Testing
Unit tests cover the core business logic: `HealthScoreCalculatorTest` and `FuelEconomyCalculatorTest`, located under `app/src/test/`.

## Demo Video
[Link to be added]

## AI Usage
See the AI Sub-Report for details on where generative AI assisted development.