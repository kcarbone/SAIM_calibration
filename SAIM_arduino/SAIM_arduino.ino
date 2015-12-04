int led = 13;
int CLKpin = 4;    // <-- Arduino pin delivering the clock pulses to pin 3 (CLK) of the TSL1412S
int SIpin = 3;     // <-- Arduino pin delivering the SI (serial-input) pulse to pin 2 of the TSL1412S
int AOpin1 = 1;    // <-- Arduino pin connected to pin 4 (analog output 1)of first TSL1412S (Detector 1)
int AOpin2 = 2;    // <-- Arduino pin connected to pin 8 (analog output 2)of first TSL1412S (Detector 1)
int AOpin3 = 3;    // <-- Arduino pin connected to pin 4 (analog output 1)of second TSL1412S (Detector 2)
int AOpin4 = 4;    // <-- Arduino pin connected to pin 8 (analog output 1)of second TSL1412S (Detector 2)
short dataD1[1536]; // <-- Array where readout of the photodiodes is stored, as short for Detector 1
short dataD2[1536]; // <-- Array where readout of the photodiodes is stored, as short for Detector 2
char serialCommand = '0';
int port = 0; // used to detect which port is connected, 0 = programming port; 1 = native port

// Note, the native USB port is much faster than the programming port, however, it does not work
// with Micro-Manager since some kind of handshaking needs to take place.  It seems possible to modify the
// Arduino code so that this is mo longer needed, see:
// https://mpuprojectblog.wordpress.com/2013/07/19/arduino-dues-serialusb-and-processing/

// The current code will work with either port (it checks through which port a command came and 
// will answer through that same port)

void setup() {
  pinMode(led, OUTPUT);
  pinMode(CLKpin, OUTPUT); 
  pinMode(SIpin, OUTPUT);

  // use the full resolution of the ADC of the Due
  analogReadResolution(12);

  // shorten the analog input readout time to 4 microseconds (from 36.6), see http://www.djerickson.com/arduino/
  // REG_ADC_MR = (REG_ADC_MR & 0xFFF0FFFF) | 0x00020000;  
  
  Serial.begin(115200);
  SerialUSB.begin(115200);
}

// Main loop to take picture and blink led when 1 is communicated through serial port
void loop() {
  if ( Serial.available() != 0 || SerialUSB.available() != 0) {
    if (Serial.available() != 0) {
      serialCommand = Serial.read();
      port = 0;
    } else {
      serialCommand = SerialUSB.read();
      port = 1;
    }
    if (serialCommand == '1') {
      Initialize();
      ReadAnalog();
      SendReading();
    }
  }
}

// Function to turn off led for debugging
void Off() {
  digitalWrite(led, LOW);
  delay(1000);
}

// Function to turn on led for debugging
void On() {
  digitalWrite(led, HIGH);
}

// Function to blink led for debugging
void Blink() {
  digitalWrite(led, HIGH);
  delay(1000);
  digitalWrite(led, LOW);
}

// Function to generate an outgoing clock pulse from the Arduino digital pin 'CLKpin'. This clock
// pulse is fed into pin 3 of the linear sensor:
void ClockPulse() {
  delayMicroseconds(1);
  digitalWrite(CLKpin, HIGH);
  digitalWrite(CLKpin, LOW);
}

//Clear the register so camera is ready to expose an image
void Initialize() {
  // Clock out any existing SI pulse through the ccd register:
  for (int i = 0; i < 786; i++) {
    ClockPulse();
  }
  // Create a new SI pulse and clock out that same SI pulse through the sensor register:
  digitalWrite(SIpin, HIGH);
  ClockPulse();
  digitalWrite(SIpin, LOW);
  for (int i = 0; i < 786; i++)
  {
    ClockPulse();
  }

}

void ClearAnalog() {
    // try to clear the Anolog input pins by reading them a number of time:
  for (int i = 0; i < 100; i++) {
    analogRead(AOpin1);
    analogRead(AOpin2);
    analogRead(AOpin3);
    analogRead(AOpin4);
  }
  
}

void ReadAnalog() {
  // Stop the ongoing integration of light quanta from each photodiode by clocking in a SI pulse 
  // into the sensors register:
  digitalWrite(SIpin, HIGH);
  ClockPulse();
  digitalWrite(SIpin, LOW);
  // For the first 18 clock pulses the sensor is not integrating, set integration time by 
  //increasing the value (768 was by trial and error)
  for(int i=0; i < 768; i++) {  
    ClockPulse();
  }
    
  ClearAnalog();
  
  // Stop the ongoing integration of light quanta from each photodiode by clocking in a SI pulse 
  // into the sensors register:
  digitalWrite(SIpin, HIGH);
  ClockPulse();
  digitalWrite(SIpin, LOW);
  
  for(int i=0; i < 768; i++) {
    delayMicroseconds(20);// <-- We add a delay to stabilize the AO output from the sensor
    dataD1[i] = analogRead(AOpin1);
    dataD1[i+768] = analogRead(AOpin2);
    dataD2[i] = analogRead(AOpin3);
    dataD2[i+768] = analogRead(AOpin4);
    ClockPulse();
  }
}

void SendReading() {
  // send data out in binary format, cast short array to byte
  if (port == 0) {
    Serial.write( (uint8_t*) dataD1, 3072);
    Serial.write( (uint8_t*) dataD2, 3072);
  } else if (port == 1) { // native port
    SerialUSB.write( (uint8_t*) dataD1, 3072);
    SerialUSB.write( (uint8_t*) dataD2, 3072);
    SerialUSB.println();
    SerialUSB.println("Done!");
  }
  //for (int i = 0; i < 1536; i++) {
  //  Serial.print(IntArray[i]); Serial.print("\t"); Serial.print(dataD2[i]); Serial.print("\n");
  //}
  //Serial.println("\n"); // <-- Send a linebreak to indicate the measurement is transmitted.
}

