# Auth lab: what was added to Make it So v2

## 1. Set up Firebase (once)
1. Firebase Console -> create a project -> add an Android app with package name
   `com.google.firebase.example.makeitso`.
2. Authentication -> Sign-in method: enable **Email/Password** and **Google**
   (Anonymous is no longer used and can stay off).
3. Project settings -> your Android app -> add the **debug SHA-1**
   (run `./gradlew signingReport` and copy the SHA1 of the `debug` variant).
4. **Now** download `google-services.json` and put it into `v2/app/`.
   Download it AFTER enabling Google and adding the SHA-1; otherwise it has no web client and the
   build fails with "Unresolved reference: default_web_client_id".
5. Firestore Database -> create a database -> Rules tab -> paste the contents of
   `firestore.rules` -> Publish.

## 2. Run
Open the `v2` folder in Android Studio and run on a device or emulator that has Google Play
services and a Google account (needed for the password dialog and Google sign-in).

## 3. What changed
| Requirement | Where |
|---|---|
| Email/password sign-up and sign-in | `AuthRemoteDataSource`, `SignUpViewModel`, `SignInViewModel` |
| Offer to save the password after sign-up | `SignUpScreen` -> `CredentialAuthClient.savePassword` |
| Saved account offered on the sign-in screen, in one dialog with Google | `SignInScreen` -> `CredentialAuthClient.requestSavedCredential` |
| The app does not store the password | it only lives in memory until the system dialog is shown |
| Google sign-in via Credential Manager -> ID token -> Firebase | `CredentialAuthClient`, `AuthRemoteDataSource.signInWithGoogle` |
| Profile screen (sign-in method, email, name, UID, ...) | `ui/profile/*`, `AuthRepository.getUserProfile` |
| Protected zone, also after restart | `MainActivity` (start destination + auth-state guard) |
| Per-user data enforced by the server | `firestore.rules` (items are keyed by `ownerId`) |
| Sign-out, Back doesn't return | `ProfileScreen` + `MainActivity` (`popUpTo(graph.id)`) |

Removed: anonymous guest accounts, the old Settings screen.

## 4. Quick test plan
1. Fresh install -> sign-in screen (no to-do list without an account).
2. Sign up with email/password -> system "save password" dialog -> to-do list.
3. Add a task, kill the app, reopen -> still signed in, task still there.
4. Profile -> shows "Email and password", email, UID. Sign out -> sign-in screen; press Back -> the
   app closes (it does not show the to-do list).
5. Sign-in screen opens a system dialog listing the saved password and your Google account.
   Pick Google -> profile shows "Google".
6. Create a second account: it must not see the first account's tasks. Also try the Rules
   Playground in the Firebase console (`/todoitems/<id>` as a different uid -> denied).

## 4b. Devices
Credential Manager below Android 14 relies on Google Play services; microG alone is not expected
to provide the password/Google dialogs. Email/password, the profile and the rules can still be
tested on such a phone.
