/* Photocell simple testing sketch.
Connect one end of the photocell to 5V, the other end to Analog 0.
 Then connect one end of a 10K resistor from Analog 0 to ground
For more information see http://learn.adafruit.com/photocells */

int photocellPin = 0; // the cell and 10K pulldown are connected to a0
int photocellReading; // the analog reading from the analog resistor divider

void setup(void) {
  // We'll send debugging information via the Serial monitor
  Serial.begin(9600);
}

void loop(void) {
  photocellReading = analogRead(photocellPin);
  Serial.print("Analog reading = ");
  Serial.print(photocellReading); // the raw analog reading
  // We'll have a few threshholds, qualitatively determined
  
  if (photocellReading < 10) {
        //    Serial.println(" - Noir");
   Serial.println("-Nuit"); }
   
   else if (photocellReading < 200) {
       //    Serial.println(" - Sombre");
   Serial.println(" - Nuage_Sombre-epaise"); } 
    
    else if (photocellReading < 500) {
      //    Serial.println(" - Lumiere");
   Serial.println(" - Ensoleillé");  } 
  
      //    else (photocellReading < 800) {
     //    Serial.println(" - Lumineux"); }
  
  else {Serial.println(" - Nuagé"); }

  delay(5000);
}
