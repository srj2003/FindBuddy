// #include "BluetoothSerial.h"

// BluetoothSerial SerialBT;

// void setup() {
//   Serial.begin(115200);
//   SerialBT.begin("ESP32test");  // Set a unique Bluetooth name
//   Serial.println("Bluetooth is ready. Waiting for connection...");
// }

// void loop() {
//   if (SerialBT.available()) {
//     String message = SerialBT.readStringUntil('\n');
//     Serial.print("Received: ");
//     Serial.println(message);
//     SerialBT.println("Echo: " + message);
//   }
// }
#include "BluetoothSerial.h"
#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClient.h> // Needed for WiFi.isConnected()

// Define Bluetooth Serial object
BluetoothSerial SerialBT;

const char* ssid = "Srinjoy";
const char* password = "srinjoy2122003";
const char* apiEndpoint = "https://my-flask-api-1s94.onrender.com/call";
String uid = "";

// Define the GPIO pin for the LED
const int ledPin = 2; // Example: Use GPIO pin 2. Change if needed.

String extractUID(String json) {
  int start = json.indexOf("\"uid\":\"") + 7;
  if (start < 7) return "";
  int end = json.indexOf("\"", start);
  return json.substring(start, end);
}

void setup() {
  Serial.begin(115200);
  SerialBT.begin("ESP32_Bluetooth"); // Start Bluetooth with a device name

  pinMode(ledPin, OUTPUT); // Set the LED pin as an output
  digitalWrite(ledPin, LOW); // Turn off the LED initially

  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi...");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi connected");

  HTTPClient http;
  http.begin(apiEndpoint);
  Serial.print("Making HTTP GET request...");
  int httpCode = http.GET();
  if (httpCode == HTTP_CODE_OK) {
    uid = extractUID(http.getString());
    Serial.println(" UID received: " + uid);
  } else {
    Serial.printf(" HTTP GET failed, error: %s\n", http.errorToString(httpCode).c_str());
  }
  http.end();
}

void loop() {
  Serial.println("UID: " + uid);
  delay(1000);

  // Check Bluetooth connection status
  if (!SerialBT.connected()) {
    Serial.println("Bluetooth Disconnected!");
    // Blink the LED rapidly when Bluetooth is disconnected
    for (int i = 0; i < 10; i++) {
      digitalWrite(ledPin, HIGH);
      delay(100);
      digitalWrite(ledPin, LOW);
      delay(100);
    }
  } else {
    // Ensure LED is off when Bluetooth is connected (or not blinking)
    digitalWrite(ledPin, LOW);
  }
}
