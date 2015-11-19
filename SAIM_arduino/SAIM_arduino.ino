int led = 13;
int CLKpin = 4;    // <-- Arduino pin delivering the clock pulses to pin 3 (CLK) of the TSL1412S
int SIpin = 5;     // <-- Arduino pin delivering the SI (serial-input) pulse to pin 2 of the TSL1412S
int AOpin1 = 1;    // <-- Arduino pin connected to pin 4 (analog output 1)of first TSL1412S (Detector 1)
int AOpin2 = 2;    // <-- Arduino pin connected to pin 8 (analog output 2)of first TSL1412S (Detector 1)
int AOpin3 = 3;    // <-- Arduino pin connected to pin 4 (analog output 1)of second TSL1412S (Detector 2)
int AOpin4 = 4;    // <-- Arduino pin connected to pin 8 (analog output 1)of second TSL1412S (Detector 2)
int IntArray[1536]; // <-- Array where readout of the photodiodes is stored, as integers for Detector 1
int IntArray2[1536]; // <-- Array where readout of the photodiodes is stored, as integers for Detector 2
int TakePic = 0;

void setup() {
  pinMode(led, OUTPUT);
  pinMode(CLKpin, OUTPUT); 
  pinMode(SIpin, OUTPUT);

  // use the full resolution of the ADC of the Due
  analogReadResolution(12);

  // shorten the analog input readout time to 4 microseconds (from 36.6), see http://www.djerickson.com/arduino/
  REG_ADC_MR = (REG_ADC_MR & 0xFFF0FFFF) | 0x00020000;  
  
  Serial.begin(115200);
}

// Main loop to take picture and blink led when 1 is communicated through serial port
void loop() {
  //Serial.print("To blink LED then take picture enter 1 \n");
  while(Serial.available() == 0) {
    TakePic = Serial.parseInt();
    if(TakePic == 1) {
      //Blink();
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
    IntArray[i] = analogRead(AOpin1);
    IntArray[i+768] = analogRead(AOpin2);
    IntArray2[i] = analogRead(AOpin3);
    IntArray2[i+768] = analogRead(AOpin4);
    ClockPulse();
  }
}

void SendReading() {
  Serial.write( (uint8_t*) IntArray, 3072);
  Serial.write( (uint8_t*) IntArray2, 3072);
  //for (int i = 0; i < 1536; i++) {
  //  Serial.print(IntArray[i]); Serial.print("\t"); Serial.print(IntArray2[i]); Serial.print("\n");
  //}
  //Serial.println("\n"); // <-- Send a linebreak to indicate the measurement is transmitted.
}

