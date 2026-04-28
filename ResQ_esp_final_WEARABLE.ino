#include <WiFi.h>
#include <FirebaseESP32.h>
#include <Wire.h>
#include <MPU6050_light.h>
#include <SPI.h>
#include <LoRa.h>
#include "model.h" // Ensure your RandomForest model.h is in the same folder

// --- 1. WiFi and Firebase Setup ---
#define WIFI_SSID "SPVG"
#define WIFI_PASSWORD "tanishka@123"
#define DATABASE_URL "resq-8f0d3-default-rtdb.asia-southeast1.firebasedatabase.app"
#define DATABASE_SECRET "IQ3tV1sxFl95U17CpIR7vJnGFzZKPRHTjStvmLV6"

// --- 2. Pin Definitions (Standard SPI for ESP32) ---
#define SS      5
#define RST     14
#define DIO0    2

// Components
MPU6050 mpu(Wire);
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;
Eloquent::ML::Port::RandomForest forest;

// --- 3. Configuration & Hardcoded GPS ---
String deviceID = "device_01"; 
float manualLat = 12.996870; // Your specific location
float manualLng = 77.658054;

void setup() {
  Serial.begin(115200);
  
  // Initialize LoRa (Using 433MHz as per your sticker)
  LoRa.setPins(SS, RST, DIO0);
  if (!LoRa.begin(433E6)) {
    Serial.println("❌ LoRa Init Failed!");
  } else {
    Serial.println("✅ LoRa Initialized!");
  }

  // Connect WiFi
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) { 
    delay(500); 
    Serial.print("."); 
  }
  Serial.println("\n✅ WiFi Connected!");
  
  // Initialize MPU6050 (I2C)
  Wire.begin(21, 22);
  mpu.begin();
  delay(1000);
  mpu.calcOffsets(); 

  // Initialize Firebase
  config.database_url = DATABASE_URL;
  config.signer.tokens.legacy_token = DATABASE_SECRET;
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  Serial.println("✅ System Ready: Monitoring Motion...");
}

void loop() {
  mpu.update();

  // A. Get Scaled Features for AI
  float ax = mpu.getAccX() * 250.0;
  float ay = mpu.getAccY() * 250.0;
  float az = mpu.getAccZ() * 250.0;
  float features[] = {ax, ay, az};

  // B. Run AI Prediction
  int prediction = forest.predict(features); 

  // C. The Walking Shield (Total Magnitude check to prevent false alarms)
  float totalAcc = sqrt(sq(mpu.getAccX()) + sq(mpu.getAccY()) + sq(mpu.getAccZ()));
  if (totalAcc < 1.60) {
    prediction = 2; // Force to NORMAL if movement is minimal
  }

  // D. Stability Filter (Wait for consistency)
  static int confirmedPrediction = 2;
  static int consistencyCount = 0;
  static int lastDraftPrediction = -1;

  if (prediction == lastDraftPrediction) {
    consistencyCount++;
  } else {
    consistencyCount = 0;
  }
  lastDraftPrediction = prediction;

  if (consistencyCount >= 5) {
    confirmedPrediction = prediction;
  }

  // E. Firebase & LoRa Update (Every 3 Seconds)
  static unsigned long lastUpdate = 0;
  if (millis() - lastUpdate > 3000) { 
    String eventType = "";
    String statusLevel = "";

    if (confirmedPrediction == 0) { eventType = "DISTRESS"; statusLevel = "MEDIUM"; }
    else if (confirmedPrediction == 1) { eventType = "FALL"; statusLevel = "CRITICAL"; }
    else { eventType = "NORMAL"; statusLevel = "STABLE"; }

    // Update Firebase Path: /users/device_01/emergency
    FirebaseJson json;
    json.set("event", eventType);
    json.set("status", statusLevel);
    json.set("latitude", manualLat);
    json.set("longitude", manualLng);
    json.set("timestamp", millis());

    if (Firebase.setJSON(fbdo, "/users/" + deviceID + "/emergency", json)) {
      Serial.print("Firebase Synced: "); Serial.println(eventType);
    }

    // TRIGGER LoRa ALERT (Only on Fall or Distress)
    if (confirmedPrediction == 0 || confirmedPrediction == 1) {
      Serial.println("📡 EMERGENCY: Broadcasting via LoRa...");
      LoRa.beginPacket();
      LoRa.print(deviceID);
      LoRa.print(",");
      LoRa.print(eventType);
      LoRa.print(",");
      LoRa.print(manualLat, 6);
      LoRa.print(",");
      LoRa.print(manualLng, 6);
      LoRa.endPacket();
    }
    
    lastUpdate = millis();
  }
}
