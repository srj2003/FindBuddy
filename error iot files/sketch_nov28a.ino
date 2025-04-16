#include "BluetoothSerial.h"
#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClient.h> // Needed for WiFi.isConnected()

// Define Bluetooth Serial object
BluetoothSerial SerialBT;

const char* ssid = "Your_SSID";
const char* password = "Your_PASSWORD";
const char* apiEndpoint = "http://your-api-endpoint/call";
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


// #include <BluetoothSerial.h>
// #include <WiFi.h>
// #include <HTTPClient.h>
// #include <Preferences.h>
// #include <WiFiClientSecure.h>

// BluetoothSerial SerialBT;
// Preferences preferences;

// const int greenLED = 2;   // Authentication indicator
// const int blueLED = 4;    // Bluetooth connection indicator

// bool isAuthenticated = false;
// unsigned long previousMillis = 0;
// bool blueLedState = false;

// void setup() {
//   Serial.begin(115200);
//   delay(1000);
//   Serial.println(F("🔧 Starting setup..."));

//   pinMode(greenLED, OUTPUT);
//   pinMode(blueLED, OUTPUT);
//   digitalWrite(greenLED, LOW);
//   digitalWrite(blueLED, LOW);

//   // Initialize Bluetooth
//   Serial.println(F("📶 Initializing Bluetooth..."));
//   SerialBT.begin("ESP32test");
//   Serial.println(F("✅ Bluetooth initialized."));

//   // Initialize preferences
//   preferences.begin("storage", false);

//   // Connect to Wi-Fi
//   Serial.println(F("🌐 Connecting to Wi-Fi..."));
//   connectToWiFi();

//   // Fetch UID and store in preferences
//   Serial.println(F("🔐 Fetching UID from API..."));
//   String uid = fetchUID();
//   if (!uid.isEmpty()) {
//     preferences.putString("uid", uid);
//     Serial.print(F("💾 UID stored: "));
//     Serial.println(uid);
//   } else {
//     Serial.println(F("❌ Failed to fetch UID."));
//   }

//   Serial.println(F("✅ Setup completed."));
//   delay(2000);
// }

// void loop() {
//   // Handle Bluetooth LED status
//   updateBluetoothLED();

//   // Exit early if not connected
//   if (!SerialBT.hasClient()) {
//     delay(100);
//     return;
//   }

//   // Handle Serial passthrough
//   while (Serial.available()) {
//     char c = Serial.read();
//     SerialBT.write(c);
//     Serial.print(F("🔁 Serial to BT: "));
//     Serial.println(c);
//   }

//   // Handle Bluetooth input
//   String incoming = "";
//   while (SerialBT.available()) {
//     char c = SerialBT.read();
//     if (c == '\n') break;
//     incoming += c;
//   }
//   incoming.trim();

//   if (incoming.length() > 0) {
//     Serial.print(F("📩 BT to Serial: "));
//     Serial.println(incoming);

//     if (incoming == "GET_UID") {
//       String storedUID = preferences.getString("uid", "");
//       SerialBT.println(storedUID);
//       Serial.print(F("📤 Sent UID via BT: "));
//       Serial.println(storedUID);
//       return;
//     }

//     if (!isAuthenticated) {
//       if (incoming.startsWith("AUTH:")) {
//         String receivedUID = incoming.substring(5);
//         String storedUID = preferences.getString("uid", "");

//         if (receivedUID == storedUID) {
//           isAuthenticated = true;
//           digitalWrite(greenLED, HIGH); // Authentication success
//           SerialBT.println("AUTH_OK");
//           Serial.println(F("✅ Authenticated."));
//         } else {
//           digitalWrite(greenLED, LOW);
//           SerialBT.println("AUTH_FAIL");
//           Serial.println(F("❌ Authentication failed."));
//         }
//       } else {
//         Serial.println(F("⏳ Awaiting AUTH..."));
//       }
//     } else {
//       // Handle authenticated messages
//       Serial.print(F("✔️ Authenticated message: "));
//       Serial.println(incoming);
//     }
//   }

//   delay(50);
// }

// void updateBluetoothLED() {
//   if (SerialBT.hasClient()) {
//     digitalWrite(blueLED, HIGH); // solid ON
//   } else {
//     // blink every 500ms
//     unsigned long currentMillis = millis();
//     if (currentMillis - previousMillis >= 500) {
//       previousMillis = currentMillis;
//       blueLedState = !blueLedState;
//       digitalWrite(blueLED, blueLedState ? HIGH : LOW);
//     }
//   }
// }

// void connectToWiFi() {
//   const char* ssid = "Srinjoy";
//   const char* password = "srinjoy2122003";

//   Serial.print(F("📡 Connecting to SSID: "));
//   Serial.println(ssid);
//   WiFi.begin(ssid, password);

//   int retry = 0;
//   while (WiFi.status() != WL_CONNECTED) {
//     delay(500);
//     Serial.print(".");
//     retry++;
//     if (retry > 20) {
//       Serial.println(F("\n❌ Wi-Fi connection failed."));
//       return;
//     }
//   }

//   Serial.println(F("\n✅ Connected to Wi-Fi."));
//   Serial.print(F("🌐 IP Address: "));
//   Serial.println(WiFi.localIP());
// }

// String fetchUID() {
//   const char* apiEndpoint = "https://my-flask-api-1s94.onrender.com/call";
//   HTTPClient http;
//   WiFiClientSecure client;

//   client.setInsecure();  // Skip SSL cert validation (for now)

//   String uid;

//   Serial.println(F("🌍 Sending HTTPS GET to API..."));
//   if (!http.begin(client, apiEndpoint)) {
//     Serial.println(F("❌ Failed to begin HTTPS connection"));
//     return "";
//   }

//   int httpResponseCode = http.GET();
//   Serial.print(F("🔁 HTTP Response Code: "));
//   Serial.println(httpResponseCode);

//   if (httpResponseCode == 200) {
//     uid = http.getString();
//     uid.trim();
//     if (uid.startsWith("\"") && uid.endsWith("\"")) {
//       uid = uid.substring(1, uid.length() - 1);  // Remove quotes
//     }
//     Serial.print(F("📦 Fetched UID: "));
//     Serial.println(uid);
//   } else {
//     Serial.print(F("❌ HTTP GET failed, code: "));
//     Serial.println(httpResponseCode);
//   }

//   http.end();
//   return uid;
// }
// #include "BluetoothSerial.h"
// #include <WiFi.h>
// #include <HTTPClient.h>
// #include <WiFiClient.h> // Needed for WiFi.isConnected()

// // Define Bluetooth Serial object
// BluetoothSerial SerialBT;

// const char* ssid = "Srinjoy";
// const char* password = "srinjoy2122003";
// const char* apiEndpoint = "https://my-flask-api-1s94.onrender.com/call";
// String uid = "";

// // Define the GPIO pin for the LED
// const int ledPin = 2; // Example: Use GPIO pin 2. Change if needed.

// String extractUID(String json) {
//   int start = json.indexOf("\"uid\":\"") + 7;
//   if (start < 7) return "";
//   int end = json.indexOf("\"", start);
//   return json.substring(start, end);
// }

// void setup() {
//   Serial.begin(115200);
//   SerialBT.begin("ESP32_Bluetooth"); // Start Bluetooth with a device name

//   pinMode(ledPin, OUTPUT); // Set the LED pin as an output
//   digitalWrite(ledPin, LOW); // Turn off the LED initially

//   WiFi.begin(ssid, password);
//   Serial.print("Connecting to WiFi...");
//   while (WiFi.status() != WL_CONNECTED) {
//     delay(500);
//     Serial.print(".");
//   }
//   Serial.println("\nWiFi connected");

//   HTTPClient http;
//   http.begin(apiEndpoint);
//   Serial.print("Making HTTP GET request...");
//   int httpCode = http.GET();
//   if (httpCode == HTTP_CODE_OK) {
//     uid = extractUID(http.getString());
//     Serial.println(" UID received: " + uid);
//   } else {
//     Serial.printf(" HTTP GET failed, error: %s\n", http.errorToString(httpCode).c_str());
//   }
//   http.end();
// }

// void loop() {
//   Serial.println("UID: " + uid);
//   delay(1000);

//   // Check Bluetooth connection status
//   if (!SerialBT.connected()) {
//     Serial.println("Bluetooth Disconnected!");
//     // Blink the LED rapidly when Bluetooth is disconnected
//     for (int i = 0; i < 10; i++) {
//       digitalWrite(ledPin, HIGH);
//       delay(100);
//       digitalWrite(ledPin, LOW);
//       delay(100);
//     }
//   } else {
//     // Ensure LED is off when Bluetooth is connected (or not blinking)
//     digitalWrite(ledPin, LOW);
//   }
// }