# Huntmate

Huntmate is a Kotlin + Jetpack Compose Android MVP for travel photo sharing, traveler discovery, connection requests, and one-to-one chat. Firebase powers authentication, Firestore data, and Storage media uploads.

## What's included

- Email/password authentication flow
- Onboarding and editable traveler profiles
- Photo-post feed with likes and comments
- Discovery screen with compatibility scoring
- Connection request lifecycle
- Chat list and chat thread screens
- Firestore and Storage security rule starters

## Setup

1. Open the project in Android Studio.
2. Add your Firebase Android app config as `app/google-services.json`.
3. Create Firebase Authentication, Firestore, and Storage in your Firebase project.
4. Publish [firestore.rules](./firestore.rules) and [storage.rules](./storage.rules) to Firebase.
5. Sync Gradle and run the `app` module on an Android device or emulator.

## Notes

- This workspace did not include Gradle Wrapper files, so Android Studio/your local Gradle installation will need to generate or supply them before command-line builds.
- Google Sign-In, notifications, admin moderation tooling, and map-based discovery are intentionally left out of this MVP.
