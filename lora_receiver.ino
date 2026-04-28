#include <SPI.h>
#include <LoRa.h>

// --- PIN DEFINITIONS (Must match Transmitter) ---
#define SS      5
#define RST     14
#define DIO0    2

void setup() {
  Serial.begin(115200);
  while (!Serial);

  LoRa.setPins(SS, RST, DIO0);

  // Frequency must match Transmitter (433MHz)
  if (!LoRa.begin(433E6)) {
    Serial.println("LoRa Receiver Init Failed!");
    while (1);
  }
  
  Serial.println("📡 Receiver Active: Waiting for emergency packets from device_01...");
}

void loop() {
  int packetSize = LoRa.parsePacket();
  
  if (packetSize) {
    String incoming = "";
    while (LoRa.available()) {
      incoming += (char)LoRa.read();
    }

    Serial.println("\n🚨 EMERGENCY ALERT DETECTED 🚨");
    
    // Parse the Comma Separated Values: device_01,EVENT,LAT,LNG
    int firstComma = incoming.indexOf(',');
    int secondComma = incoming.indexOf(',', firstComma + 1);
    int thirdComma = incoming.indexOf(',', secondComma + 1);

    String id = incoming.substring(0, firstComma);
    String ev = incoming.substring(firstComma + 1, secondComma);
    String lat = incoming.substring(secondComma + 1, thirdComma);
    String lng = incoming.substring(thirdComma + 1);

    Serial.println("FROM ID:  " + id);
    Serial.println("EVENT:    " + ev);
    Serial.println("LOCATION: " + lat + ", " + lng);
    Serial.print("RSSI:     "); Serial.print(LoRa.packetRssi()); Serial.println(" dBm");
    Serial.println("---------------------------------------");
  }
}
