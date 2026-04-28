# ResQLink AI 🚨

**ResQLink AI** is a smart emergency response application designed to bridge the gap between distress detection and immediate action. Whether triggered manually or via a connected wearable band (Firebase-integrated), the app automates the process of notifying loved ones and emergency services with precise location data.

## Key Features
- **Dual Triggering**: Support for manual SOS (long-press) and IoT band triggers (Fall Detection/Distress).
- **Autonomous SOS**: Automatically sends multi-part SMS messages to emergency contacts (Mom, Dad, Sister) with live Google Maps links.
- **Smart SMS Delivery**: Optimized for modern Android versions (14+) and strictly filtered devices (Xiaomi/HyperOS) to ensure delivery even with emojis.
- **Visual Alert System**: High-decibel siren and real-time map tracking within the app.
- **Emergency Shortcuts**: One-tap dials for Police (100) and Ambulance (102).

## 🛠 Setup & Security (Read Before Pushing to GitHub)

To maintain security and ensure the app works after cloning, follow these steps to add your secrets. **Do not commit these files to your public repository.**

### 1. Firebase Integration
- Go to the [Firebase Console](https://console.firebase.google.com/).
- Create a project and add an Android app with the package name `com.example.resqlink_ai`.
- Download the `google-services.json` file.
- **Place it in**: `ResQLink-AI/app/` (The root of the `app` module).
- *Ensure `google-services.json` is added to your `.gitignore`.*

### 2. Google Maps API Key
- Go to the [Google Cloud Console](https://console.cloud.google.com/).
- Enable the **Maps SDK for Android**.
- Create an API Key and restrict it to your app's SHA-1 fingerprint.
- **Place it in**: `ResQLink-AI/app/src/main/AndroidManifest.xml`
  - Look for the `<meta-data>` tag with `android:name="com.google.android.geo.API_KEY"`.
  - Replace the `android:value` with your actual key.

### 3. Firebase Database URL
- In `MainActivity.java` and `AlertActivity.java`, update the `FIREBASE_URL` constant with your Realtime Database URL:
  ```java
  private final String FIREBASE_URL = "https://your-project-id-default-rtdb.firebaseio.com/";
  ```

## 🔐 Privacy & Safety
- **Permissions**: This app requires `SEND_SMS`, `ACCESS_FINE_LOCATION`, and `CALL_PHONE` to function correctly.
- **Data Usage**: Only emergency-related location data is sent to the configured Firebase instance when an alert is active.

## 🚀 Future Roadmap
- AI-based voice recognition for hands-free SOS triggers.
- Integration with local volunteer networks (ResQ-Squad).
- Health metric monitoring (Heart rate/O2) for proactive medical alerts.
