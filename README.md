# 🚨 ResQLink AI - Smart Emergency Response System

**A comprehensive IoT + Mobile emergency response platform that automatically alerts loved ones and emergency services with real-time GPS coordinates during critical situations.**
<img width="2671" height="1550" alt="image" src="https://github.com/user-attachments/assets/413f6690-20b8-48e2-91a3-14c4c0a1fc29" />

---

## 📋 Quick Overview

ResQLink AI bridges the gap between emergency detection and rapid response. It combines:
- **Manual SOS triggering** via Android app (long-press 3 seconds)
- **Automatic fall detection** via wearable band (ML-powered accelerometer analysis)
- **Multi-channel notifications** (SMS, real-time database, LoRa broadcasting)
- **Live location tracking** with Google Maps integration
- **IoT long-range communication** via LoRa (433MHz, several km range)

**Current Status**: Hackathon prototype with functional MVP

---

## ✨ Key Features

- ✅ **Manual Emergency Trigger** - Long-press button on Android app activates SOS with 5-second cancel window
- ✅ **Automatic Fall Detection** - ML RandomForest model analyzes 6-axis accelerometer data from wearable band
- ✅ **Wearable Band Integration** - ESP32-based device with real-time Firebase sync
- ✅ **Auto SMS Delivery** - Sends emergency alerts with Google Maps link to 3 emergency contacts
- ✅ **Emergency Hotlines** - One-tap calling to Police (100) and Ambulance (102)
- ✅ **Real-time Location Mapping** - Full Google Maps integration for tracking and navigation
- ✅ **Audio & Vibration Alerts** - High-decibel siren and haptic feedback during active emergency
- ✅ **LoRa Broadcasting** - Wearable broadcasts emergency packets via 433MHz LoRa for base station reception
- ✅ **Firebase Real-time Database** - Instant cloud synchronization of emergency state
- ✅ **Distress & Fall Classification** - Differentiates between normal movement, distress, and falls

---

## 🏗️ Architecture & Components

```
┌─────────────────────────────────────────────────────────────────┐
│                    ResQLink AI System Architecture               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐                    ┌──────────────────────┐   │
│  │ Android App  │◄────Firebase────►│ Firebase Realtime DB │   │
│  │ (MainActivity)│    Database       │  (Cloud Backend)     │   │
│  │ - Main Screen│  Sync             │                      │   │
│  │ - Pre-Alert  │                   │ /users/device_01/    │   │
│  │ - Alert View │                   │   emergency/         │   │
│  │ - Maps       │                   │   - event            │   │
│  │ - SMS Send   │                   │   - status           │   │
│  └──────────────┘                   │   - latitude         │   │
│         ▲                           │   - longitude        │   │
│         │ Manual SOS                │   - timestamp        │   │
│         │ Long-press 3s             └──────────────────────┘   │
│         │                                      ▲                │
│  ┌──────▼──────────────────┐                   │ Real-time      │
│  │   ESP32 Wearable Band   │                   │ Listener       │
│  ├─────────────────────────┤                   │                │
│  │ • MPU6050 Accelerometer │───── Fall ────►  │                │
│  │   (6-axis IMU)          │     Detection     │                │
│  │                         │     (ML Model)    │                │
│  │ • LoRa Module (433MHz)  │                   │                │
│  │   SS=5, RST=14, DIO0=2  │                   │                │
│  │                         │                   │                │
│  │ • WiFi Module           │─ Firebase Sync ──►                │
│  │   (SPVG network)        │                                   │
│  │                         │                                   │
│  │ • GPS Data              │                                   │
│  │   (hardcoded: 12.996870,│                                   │
│  │    77.658054)           │                                   │
│  └─────────────────────────┘                                   │
│         ▲                                                       │
│         │ Accelerometer Data                                   │
│         │ (3-axis: ax, ay, az)                                 │
│         │                                                       │
│         │ Event Type:                                          │
│         │ - NORMAL (class 2)                                   │
│         │ - DISTRESS (class 0)                                 │
│         │ - FALL (class 1)                                     │
│         │                                                       │
│  ┌──────▼──────────────────┐                                   │
│  │  LoRa Base Receiver     │                                   │
│  │  (lora_receiver.ino)    │                                   │
│  │                         │                                   │
│  │ • 433MHz Reception      │ Packet Format:                   │
│  │ • Pin Config (match TX) │ device_01,FALL,12.996870,77.658 │
│  │ • RSSI Signal Display   │                                   │
│  └─────────────────────────┘                                   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘

## Data Flow Summary

1. **Manual Trigger Flow**:
   MainActivity (long-press) → PreAlertActivity (5s countdown) → AlertActivity (active alert)
                           → Firebase write → SMS dispatch → Maps display

2. **Wearable Auto-Trigger Flow**:
   MPU6050 sensor → ML inference → Consistency filter → Firebase sync → Android listener
                                                      → LoRa broadcast → Base station

3. **SMS Notification**:
   AlertActivity → SMS API → Mom, Dad, Sister (phone numbers hardcoded)
                  Google Maps link embedded in message
```

---

## 💻 Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Android Framework** | Android SDK | API 24-35 |
| **Primary Language** | Java (Android) | 11 |
| **IoT Platform** | ESP32 (Arduino) | C++ (Arduino IDE) |
| **Backend Database** | Firebase Realtime DB | via BOM 33.7.0 |
| **Maps Integration** | Google Play Services Maps | v19.0.0 |
| **Build System** | Gradle (Kotlin DSL) | 8.7.0 |
| **UI Framework** | Material Design | 1.12.0 |
| **Wireless - Long-range** | LoRa (433MHz) | Arduino-LoRa library |
| **Wireless - Local** | WiFi | ESP32 built-in |
| **Accelerometer Lib** | MPU6050_light | Arduino library |
| **ML Framework** | Eloquent ML | RandomForest model.h |
| **Dependencies** |  |  |
| | androidx.appcompat:appcompat | 1.7.0 |
| | androidx.activity:activity | 1.9.3 |
| | androidx.constraintlayout:constraintlayout | 2.2.0 |
| | com.google.firebase:firebase-database | 33.7.0 (BOM) |
| | JUnit (testing) | 4.13.2 |

---

## 📁 Project Structure

```
ResQLink-AI/
│
├── README.md                              # This file
│
├── APP_RESQ/                              # Main Android Application
│   ├── app/
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── AndroidManifest.xml              # App permissions, activities, Google Maps API key
│   │   │   │   ├── java/com/example/resqlink_ai/
│   │   │   │   │   ├── MainActivity.java            # Main screen - SOS button (long-press trigger)
│   │   │   │   │   ├── PreAlertActivity.java        # 5-second countdown confirmation screen
│   │   │   │   │   └── AlertActivity.java           # Active emergency screen - maps, siren, hotline buttons
│   │   │   │   └── res/                             # UI resources (layouts, drawables, strings)
│   │   │   ├── test/                                # Unit tests
│   │   │   └── androidTest/                         # Instrumented tests
│   │   ├── build.gradle.kts                         # App-level build configuration
│   │   ├── google-services.json                     # Firebase configuration (auto-generated)
│   │   └── proguard-rules.pro                       # ProGuard obfuscation rules
│   │
│   ├── build.gradle.kts                   # Project-level build configuration
│   ├── settings.gradle.kts                # Gradle module settings
│   ├── gradle.properties                  # Gradle properties
│   └── gradle/
│       └── wrapper/
│           └── gradle-wrapper.properties  # Gradle wrapper version
│
├── ResQ_esp_final_WEARABLE.ino            # ESP32 Wearable Band Firmware
│   ├── WiFi & Firebase Setup
│   ├── MPU6050 Accelerometer Integration
│   ├── LoRa Broadcasting
│   ├── ML Model Inference (RandomForest)
│   ├── Fall Detection with Consistency Filter
│   └── Automatic Emergency Broadcasting
│
├── lora_receiver.ino                      # LoRa Base Station Receiver
│   ├── 433MHz Reception
│   ├── Emergency Packet Parsing
│   └── Serial Output (monitoring)
│
└── model.h                                # Pre-trained RandomForest ML Model
    └── Binary classification for fall/distress detection
```

---

## 🛠️ Setup & Installation

### Prerequisites

**For Android Development:**
- Android Studio (latest version)
- Android SDK: API 24-35
- Java 11 JDK or higher
- Firebase project with Realtime Database enabled
- Google Maps API key

**For IoT/Wearable Development:**
- Arduino IDE (v1.8.13 or higher)
- ESP32 board package (via Arduino Boards Manager)
- Required libraries:
  - WiFi (built-in)
  - FirebaseESP32
  - MPU6050_light
  - LoRa (Arduino-LoRa library)
  - Wire (I2C, built-in)
  - SPI (built-in)

### Step 1: Clone the Repository

```bash
git clone https://github.com/yourusername/ResQLink-AI.git
cd ResQLink-AI
```

### Step 2: Android App Setup

```bash
cd APP_RESQ
```

1. **Download `google-services.json`**:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Select your ResQLink project
   - Download `google-services.json` and place it in `APP_RESQ/app/`

2. **Add Google Maps API Key**:
   - Open [APP_RESQ/app/src/main/AndroidManifest.xml](APP_RESQ/app/src/main/AndroidManifest.xml)
   - Replace `AIzaSyAP34T1LO6aW7WqesFqLmjnYPQdcKJlREk` with your own Google Maps API key

3. **Install Dependencies**:
   - Open `APP_RESQ` in Android Studio
   - Let Gradle sync automatically, or manually run:
   ```bash
   ./gradlew build
   ```

### Step 3: IoT/Wearable Setup

1. **Install Arduino IDE**:
   - Download from [arduino.cc](https://www.arduino.cc/en/software)
   - Install ESP32 board support via Boards Manager

2. **Install Required Libraries**:
   - Open Arduino IDE → Sketch → Include Library → Manage Libraries
   - Search and install:
     - `FirebaseESP32` (by Mobizt)
     - `MPU6050_light` (by Romain Valentin)
     - `LoRa` (by Sandeep Mistry)

3. **Upload Wearable Firmware**:
   - Open [ResQ_esp_final_WEARABLE.ino](ResQ_esp_final_WEARABLE.ino)
   - Select Board: Tools → Board → ESP32 → ESP32 Dev Module
   - Select COM Port
   - Click Upload

4. **Upload Receiver Firmware** (Optional, for base station):
   - Open [lora_receiver.ino](lora_receiver.ino)
   - Same steps as above
   - Monitor Serial output at 115200 baud to see incoming emergency packets

---

## 🏗️ Building & Running

### Android App

**Via Android Studio:**
1. File → Open → Select `ResQLink-AI/APP_RESQ`
2. Click Run ▶️ or press Shift+F10

**Via Command Line:**
```bash
cd APP_RESQ
./gradlew assembleDebug          # Build APK (debug)
./gradlew installDebug           # Install on connected device
./gradlew assembleRelease        # Build APK (release)
```

**Generated APK Location:**
```
APP_RESQ/app/build/outputs/apk/debug/app-debug.apk
```

### ESP32 Wearable

**Via Arduino IDE:**
1. Tools → Board → Select "ESP32 Dev Module"
2. Tools → Port → Select COM port
3. Sketch → Upload (or press Ctrl+U)

**Monitor Serial Output:**
- Tools → Serial Monitor
- Set baud rate to **115200**
- Watch for:
  ```
  ✅ LoRa Initialized!
  ✅ WiFi Connected!
  ✅ System Ready: Monitoring Motion...
  ```

**For Fall Detection Debugging:**
```
Firebase Synced: NORMAL
Firebase Synced: DISTRESS
Firebase Synced: FALL
📡 EMERGENCY: Broadcasting via LoRa...
```

### LoRa Receiver (Base Station)

1. Upload [lora_receiver.ino](lora_receiver.ino) to a second ESP32
2. Open Serial Monitor (115200 baud)
3. When wearable detects fall/distress:
   ```
   🚨 EMERGENCY ALERT DETECTED 🚨
   FROM ID:  device_01
   EVENT:    FALL
   LOCATION: 12.996870, 77.658054
   RSSI:     -95 dBm
   ```

---

## 🔧 Hardware Setup

### ESP32 Wearable Band

#### **Pin Configuration**

```
ESP32 Dev Module Pinout:

SPI (LoRa Module)
├── SS (Chip Select)   → GPIO 5
├── RST (Reset)        → GPIO 14
├── DIO0 (IRQ)         → GPIO 2
├── MOSI               → GPIO 23 (auto-routed on ESP32)
├── MISO               → GPIO 19 (auto-routed on ESP32)
└── CLK                → GPIO 18 (auto-routed on ESP32)

I2C (MPU6050 Accelerometer)
├── SDA (Data)         → GPIO 21
├── SCL (Clock)        → GPIO 22
└── GND                → GND

Other
├── 5V Power           → LoRa module VCC, ESP32 VIN
└── GND                → Common ground for all modules
```

#### **Hardware Connections**

| Component | ESP32 Pin | Notes |
|-----------|-----------|-------|
| **LoRa Module (433MHz)** | | SPI interface |
| - VCC | 5V | Power supply |
| - GND | GND | Common ground |
| - SS | GPIO 5 | Chip select |
| - RST | GPIO 14 | Reset line |
| - DIO0 | GPIO 2 | Interrupt signal |
| **MPU6050** | | I2C interface |
| - VCC | 3.3V | Power (built-in on ESP32) |
| - GND | GND | Common ground |
| - SDA | GPIO 21 | I2C Data |
| - SCL | GPIO 22 | I2C Clock |
| **WiFi/BT** | Built-in | Antenna required |

### LoRa Base Station

Same pin configuration as wearable (SS=5, RST=14, DIO0=2) to ensure receiver matches transmitter settings.

---

## ⚙️ Configuration Guide

### Firebase Database Configuration

**Current Setup in Code:**

```cpp
#define DATABASE_URL "resq-8f0d3-default-rtdb.asia-southeast1.firebasedatabase.app"
#define DATABASE_SECRET "IQ3tV1sxFl95U17CpIR7vJnGFzZKPRHTjStvmLV6"
#define DEVICE_ID "device_01"
```

**To Update:**

1. Open [ResQ_esp_final_WEARABLE.ino](ResQ_esp_final_WEARABLE.ino), lines 10-13
2. Replace with your Firebase project:
   - `DATABASE_URL`: From Firebase Console → Project Settings → Database URL
   - `DATABASE_SECRET`: Firebase API key or custom secret (⚠️ see Security Notes)

**Firebase Database Structure:**

```
/users/
  └── device_01/
       └── emergency/
            ├── event: "NORMAL" | "DISTRESS" | "FALL"
            ├── status: "STABLE" | "MEDIUM" | "CRITICAL"
            ├── latitude: 12.996870
            ├── longitude: 77.658054
            └── timestamp: 1234567890
```

### Google Maps API Configuration

**Location in Manifest:**

[AndroidManifest.xml](APP_RESQ/app/src/main/AndroidManifest.xml), lines 28-31

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_GOOGLE_MAPS_API_KEY_HERE"/>
```

**Steps:**

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create new project or select existing
3. Enable "Maps SDK for Android"
4. Create API key (restricted to Android apps)
5. Add your app's package name and SHA-1 fingerprint
6. Copy API key to AndroidManifest.xml

### Emergency Contacts (SMS Recipients)

**Location in Code:**

[MainActivity.java](APP_RESQ/app/src/main/java/com/example/resqlink_ai/MainActivity.java)

**Current Hardcoded Contacts:**

```
Mom:      9108296410
Dad:      9606077664
Sister:   9035858622
Police:   100
Ambulance: 102
```

**To Update:**

Search for these phone numbers in MainActivity.java and replace with actual emergency contacts.

### WiFi Configuration (Wearable)

**Location in Code:**

[ResQ_esp_final_WEARABLE.ino](ResQ_esp_final_WEARABLE.ino), lines 10-11

```cpp
#define WIFI_SSID "SPVG"
#define WIFI_PASSWORD "tanishka@123"
```

**To Change WiFi Network:**

1. Open ResQ_esp_final_WEARABLE.ino
2. Replace SSID and password
3. Recompile and upload to ESP32

### LoRa Frequency

**Current Configuration:**

- Frequency: **433MHz**
- Transmitter: ResQ_esp_final_WEARABLE.ino, line 44 → `LoRa.begin(433E6)`
- Receiver: lora_receiver.ino, line 22 → `LoRa.begin(433E6)`

**Note:** Both transmitter and receiver **must use same frequency**.

### GPS/Location Data

**Current Configuration:**

[ResQ_esp_final_WEARABLE.ino](ResQ_esp_final_WEARABLE.ino), lines 36-37

```cpp
float manualLat = 12.996870;  // Hardcoded latitude (Bangalore area)
float manualLng = 77.658054;  // Hardcoded longitude
```

**To Update with Real GPS:**

Currently, coordinates are hardcoded. For production:
- Add GPS module (e.g., NEO-6M)
- Parse NMEA sentences via UART
- Replace hardcoded values with real-time coordinates

---

## ⚠️ Security Notes & Warnings

### 🔴 **CRITICAL ISSUES FOR PRODUCTION**

This is a **hackathon prototype**. The following hardcoded credentials and configurations **MUST NOT** be used in production:

#### 1. **Hardcoded WiFi Credentials**
```cpp
#define WIFI_SSID "SPVG"
#define WIFI_PASSWORD "tanishka@123"  // ⚠️ EXPOSED
```
**Fix**: Move to secure WiFi provisioning via Bluetooth or QR code scanning.

#### 2. **Firebase Database Secret Exposed**
```cpp
#define DATABASE_SECRET "IQ3tV1sxFl95U17CpIR7vJnGFzZKPRHTjStvmLV6"  // ⚠️ EXPOSED
```
**Fix**: Use Firebase Anonymous/Email authentication or OAuth instead of legacy tokens.

#### 3. **Google Maps API Key in Manifest**
```xml
android:value="AIzaSyAP34T1LO6aW7WqesFqLmjnYPQdcKJlREk"  // ⚠️ EXPOSED
```
**Fix**: 
- Restrict key to Android apps only (package name + SHA-1)
- Consider using a backend proxy for API calls
- Rotate keys regularly

#### 4. **Emergency Contact Numbers Hardcoded**
```java
// Mom, Dad, Sister phone numbers hardcoded in MainActivity.java
```
**Fix**: Store in secure encrypted database or allow user configuration via UI.

#### 5. **GPS Coordinates Hardcoded**
```cpp
float manualLat = 12.996870;  // ⚠️ EXPOSED LOCATION
float manualLng = 77.658054;
```
**Fix**: Integrate real GPS module for dynamic location tracking.

### 🟡 **RECOMMENDED SECURITY IMPROVEMENTS**

1. **Authentication**:
   - Implement Firebase Authentication (Email/SMS/OAuth)
   - Add user registration and device pairing flow
   - Require password for emergency trigger (or biometric)

2. **Data Encryption**:
   - Encrypt SMS messages and location data in transit (already HTTPS via Firebase)
   - Use TLS 1.3+ for all Firebase communications
   - Consider end-to-end encryption for sensitive data

3. **Access Control**:
   - Implement Firebase Security Rules to restrict database access
   - Only allow authenticated users to read/write their own emergency records
   - Block public access to location data

4. **API Key Management**:
   - Rotate API keys quarterly
   - Use different keys for development, staging, production
   - Monitor API usage for anomalies

5. **Credential Management**:
   - Use environment variables or secure config files (never hardcode)
   - Consider using AWS Secrets Manager or Google Cloud Secret Manager
   - Implement key rotation policies

6. **Audit Logging**:
   - Log all emergency events with timestamp and user ID
   - Track who accessed location data and when
   - Monitor for suspicious activity patterns

### 📋 **Before Production Launch**

- [ ] Move all hardcoded credentials to secure configuration
- [ ] Implement proper authentication system
- [ ] Add Firebase Security Rules
- [ ] Restrict Google Maps API key to Android apps only
- [ ] Test emergency notification delivery (SMS, Firebase)
- [ ] Verify GPS accuracy and backup location sources
- [ ] Conduct security audit and penetration testing
- [ ] Implement GDPR/privacy compliance (location tracking consent)
- [ ] Add rate limiting to prevent abuse
- [ ] Document incident response procedures

---

## 📊 Project Status

- **Type**: Hackathon MVP / Proof of Concept
- **Development Stage**: Alpha (functional but not production-ready)
- **Last Updated**: April 2026

**Current Capabilities**:
- ✅ Manual SOS trigger via Android app
- ✅ 5-second confirmation window
- ✅ SMS dispatch to hardcoded contacts
- ✅ Firebase real-time database sync
- ✅ Google Maps integration
- ✅ Fall detection via ML model
- ✅ LoRa broadcasting
- ✅ Base station receiver

**Known Limitations**:
- ❌ Hardcoded credentials (security risk)
- ❌ Hardcoded GPS coordinates (no real GPS module)
- ❌ Hardcoded emergency contacts (not user-configurable)
- ❌ Limited battery optimization
- ❌ No offline fallback mechanism
- ❌ No user authentication system
- ❌ ML model not fully tuned for fall detection accuracy

---

## 📚 Additional Resources

- [Firebase Android Setup](https://firebase.google.com/docs/database/android/start)
- [Google Maps Android API](https://developers.google.com/maps/documentation/android-sdk/overview)
- [Arduino ESP32 Board Package](https://docs.espressif.com/projects/arduino-esp32/en/latest/)
- [LoRa Module Specifications](https://lora-alliance.org/)
- [MPU6050 Datasheet](https://invensense.tdk.com/wp-content/uploads/2015/02/MPU-6000-Datasheet1.pdf)

---

## 🤝 Contributing

This is an active hackathon project. For contributions:
1. Test thoroughly on actual hardware before submitting
2. Update this README for any new features or configuration changes
3. Document security implications of any credential handling
4. Ensure backward compatibility with existing Firebase structure

---

## ⚖️ License & Acknowledgments

**Project**: ResQLink AI  
**Developed for**: IDEATHON-CIT Hackathon (2026)  
**Status**: Prototype/MVP

---

**Last Updated**: April 29, 2026  
**Maintainer**: Development Team
