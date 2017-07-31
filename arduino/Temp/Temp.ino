#include <OneWire.h>
#include <DallasTemperature.h>
#include <SoftwareSerial.h>

// Data wire is plugged into pin 3 on the Arduino
#define ONE_WIRE_BUS 3

// Setup a oneWire instance to communicate with any OneWire devices
// (not just Maxim/Dallas temperature ICs)
OneWire oneWire(ONE_WIRE_BUS);

// Pass our oneWire reference to Dallas Temperature.
DallasTemperature sensors(&oneWire);

// arduino>>bluetooth
// D5   >>>  Rx
// D4   >>>  Tx
SoftwareSerial bluetooth(5, 6); // RX, TX

int LEDPin = 13; //LED PIN on Arduino

void setup(void)
{
  // start serial port
  Serial.begin(9600);
  bluetooth.begin(9600);

  // Start up the library
  sensors.begin();
}


void loop(void)
{
  //write result to bluetooth
  if (bluetooth.available() > 0) {
    int bluetoothInputData = bluetooth.read();
    if (bluetoothInputData == 49) { // if number 1 is read we send temp data
      digitalWrite(LEDPin, 1);
      sensors.requestTemperatures(); // Send the command to get temperatures
      bluetooth.print(sensors.getTempCByIndex(0));
      bluetooth.print("END");
    }
  } else {
    digitalWrite(LEDPin, 0);
  }
}

