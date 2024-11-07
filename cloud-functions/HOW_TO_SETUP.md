# How to setup a local dev environment

## Initial setup

- Use Node v20 (nice tool: `nvm`)
- All commands must be executed inside the `functions/` folder
- Install Firebase CLI: `npm i -g firebase-tools`
- Install dependencies: `npm i`
- Get the `.env` content from a peer and add the file in the `functions/` folder

## Usage

- `npm run serve` will start the Firebase Emulator with all modules
- `npm run deploy` will try to deploy to production (use `firebase login` before)

### ... with the Android app

Typical IP address of the Android Emulator is `10.0.22.2`

- Start the Firebase Emulator using `npm run serve`
- Add the Kotlin instruction `functions.useEmulator("10.0.2.2", 5001)` to use the Emulator for this instance of Firebase Functions
- Modify the AndroidManifest : to allow inscure connections
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:usesCleartextTraffic="true"
        ...
```