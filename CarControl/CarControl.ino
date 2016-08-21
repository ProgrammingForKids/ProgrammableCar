const unsigned long MAX_DISTANCE = 35;

#include <SoftwareSerial.h>// import the serial library

#include "TB6612FNG.h"


#include "Outlook.h"

/*
 * Ultrasonic sensor
  - Pin 10 ---> echo // yellow
  - Pin 11 ---> trig // green
// blue - vcc
*/
Outlook outlook(10, 11, MAX_DISTANCE);

int pinLed = 13;


#define USE_SERIAL_MONITOR

void Delay(int ms)
{
#ifdef USE_SERIAL_MONITOR
  Serial.print("Delay ");
  Serial.print(ms);
  Serial.println(" ms");
#endif
  delay(ms);
}


/*

  Bluetooth
  - Pin 12 ---> TX // purple
  - Pin 2  ---> RX // orange
  // ground - blk
  // vcc - white, gray - ground
*/
SoftwareSerial BT(12, 2);

/*
  Connections:
  Motor driver
  - Pin 3 ---> PWMA
  - Pin 4 ---> AIN2
  - Pin 5 ---> AIN1
  - Pin 6 ---> STBY
  - Pin 7 ---> BIN1
  - Pin 8 ---> BIN2
  - Pin 9 ---> PWMB
*/
TB6612FNG wheels(3, 4, 5, 6, 7, 8, 9);

unsigned long ready_to_read_time;

void setup()
{
  wheels.begin();

#ifdef USE_SERIAL_MONITOR
  Serial.begin(9600);
#endif

  //configure pin modes
  pinMode(pinLed, OUTPUT);
  digitalWrite(pinLed, LOW);


  outlook.begin();
  BT.begin(38400);

  Serial.println("Ready");
  ready_to_read_time = millis();
}



bool obstacle = false;
char  recent_state = 's'; // Stopped


void loop()
{
  char report = '\0';
  bool isClose = outlook.isInRange();

  if ( (!obstacle) && isClose )
  {
    digitalWrite(pinLed, HIGH);
    obstacle = true;
    report = 'O';
    wheels.Brake();
  }


  if ( (!isClose) && obstacle)
  {
    digitalWrite(pinLed, LOW);
    obstacle = false;
  }

  int BluetoothData = 's';

  if (BT.available())
  {
    BluetoothData = BT.read();
    Serial.print("Received ");
    Serial.println((char)BluetoothData);
  }


  if (recent_state != BluetoothData)
  {
    wheels.Stop();
    report = 'S';
    delay(100);
  }

  recent_state = BluetoothData;

  int aftermath = 30;

  switch (BluetoothData)
  {
    case 'f':
      if (obstacle)
      {
        wheels.Brake();
        recent_state = 's';
      }
      else
      {
        wheels.Forward();
        recent_state = 'f';
        report = 'F';
        aftermath = 100;
      }
      break;

    case 'b':
      wheels.Back();
      recent_state = 'b';
      report = 'B';
      aftermath = 100;
      break;

    case 'l':
      wheels.Left();
      recent_state = 'l';
      report = 'L';
      aftermath = 100;
      break;

    case 'r':
      wheels.Right();
      recent_state = 'r';
      report = 'R';
      aftermath = 100;
      break;

    case 's':
      report = 'S';
      break;

    case 'h':
      report = 'H';
      break;

    default:
      Serial.println("Unknown command");
      report = 'X';
      break;
  }

  if (report != '\0')
  {
    BT.print(report);
  }

  delay(aftermath);// prepare for next data ...
}

