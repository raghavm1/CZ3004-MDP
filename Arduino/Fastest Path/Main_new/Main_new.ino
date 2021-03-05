#include <Arduino.h>
#include "DualVNH5019MotorShield.h"
#include "EnableInterrupt.h"
#include "Math.h"
#include "SharpIR.h"
DualVNH5019MotorShield md;

//-----Motor 1 E1/M1-----/
#define E1A_INPUT 3
#define E1B_INPUT 5

//-----Motor 2 E2/M2-----/
#define E2A_INPUT 11
#define E2B_INPUT 13

//-----Sensor initialisation-------/
SharpIR sensor1(1, A0);
SharpIR sensor2(1, A1);
SharpIR sensor3(1, A2);
SharpIR sensor4(1, A3);
SharpIR sensor5(1, A4);
SharpIR sensor6(3, A5);

// -- For reading input Code -----/
double sensor_diff = 0;
char piCommand_buffer[512], readChar, instruction, flushChar;
int arg;
String fromAlgo = "";
String singleCommand = "";
int i = 0;
int j = 0;
int rep = 0;
//-----Ticks Variables-----/
volatile long E1_counts = 0; //left
volatile long E2_counts = 0; //right

//-----rpmL count-----/
float startTimeL;
float currentTimeL;
float RevolutionTimeL;
float fourRev_TimeL;
float fourRev_CounterL = 0;
float oneRev_TimeL;
float current_TimeL;
float rpmL = 0;

//-----rpmR count-----/
float startTimeR;
float currentTimeR;
float RevolutionTimeR;
float fourRev_TimeR;
float fourRev_CounterR = 0;
float oneRev_TimeR;
float current_TimeR;
float rpmR = 0;

//-----Target RPM-----/
float TargetRPM = 140;

//---left PID------/
/* PID values
 * 1st run: 3.4,3.4,3 / 2nd run: 5,2.4,3 / 3rd run: 5,2.4,5 //(BEST)
 * 5,1.4,7 (2nd best)
 * lkp = 5, lki = 0.5, lkd = 5;
 * 
 * old -> float lkp = 5, lki = 2.4, lkd = 2;
 */

//---Maximum distance to be moved in straight line ---/
//for checklist
float setDistance = 120;

// for battery A full charge: 6.508V [As of 16th Feb] => lkp = 5.0, lki = 2.2, lkd = 1.8;
// for battery B full charge: 6.354V [As of 16th Feb] => lkp = 5.0, lki = 2.25, lkd = 1.75;
// for battery A: 6.444V [as of 18th Feb] => float lkp = 5.0, lki = 2.2, lkd = 1.75;

//Target_RPM = 140: Battery A, 6.462V, rkp = 5, rki = 2.25, rkd = 1.8;
//Target_RPM = 140: Battery B, 6.462V, rkp = 5, rki = 2.25, rkd = 1.8;

float lkp = 5.0, lki = 2.20, lkd = 1.8;
float lpTerm, ldTerm, liTerm;
float lprevPosition; // last position
float lerror = 0, lsumError = 0;
float l_speed = 0;
float lmax = 130, lmin = 0;

//---right PID------//
/* PID values
 * 1st run: 3.4,3.4,3 / 2nd run: 5,2.4,3 / 3rd run: 5,2.4,5 //(BEST)
 * 5,1.4,6 (2nd best)
 * rkp = 5, rki = 2.49, rkd = 6;
 * rkp = 5, rki = 0.5, rkd = 5; 
 * for fully charged battery A -> float rkp = 5, rki = 2.4, rkd = 1.9;
 * for fully charged battery B -> rkp = 5, rki = 2.4, rkd = 1.8;
 */
// for battery A: 6.444V [as of 18th Feb] => float rkp = 5.0, rki = 2.3, rkd = 1.8;
//float rkp = 5, rki = 2.35, rkd = 1.8;

//Target_RPM = 140: Battery A, 6.496V, rkp = 5, rki = 2.5, rkd = 1.8;
//Target_RPM = 140: Battery B, 6.462V, rkp = 5, rki = 2.5, rkd = 1.8;

float rkp = 5, rki = 2.5, rkd = 2.5;
float rpTerm, rdTerm, riTerm;
float rprevPosition; // last position
float rerror = 0, rsumError = 0;
float r_speed = 0;
float rmax = 130, rmin = 0;

//--------Motion configuration in switch case--------//
char input;
boolean Forward = false;
boolean Backward = false;
boolean rotate_Left = false;
boolean rotate_Right = false;
boolean move10cm = false;

//--------Serial read------//
int incomingByte = 0;

////-------Rotation---------//
volatile long E1_counts2 = 0; //left
volatile long E2_counts2 = 0; //right
float leftDistance = 0;
float rightDistance = 0;
float lrAvgDistance = 0;
#define PI 3.14159265359
float rotateDistance = 0;

//---Stop if fault default function------/
void stopIfFault()
{
  if (md.getM1Fault())
  {
    Serial.println("M1 fault");
    while (1)
      ;
  }
  if (md.getM2Fault())
  {
    Serial.println("M2 fault");
    while (1)
      ;
  }
}

//---------rotation counter for ticks--------------//
//void Rotate_counterL()
//{
//  E1_counts2++; // for rotation
//}
//void Rotate_counterR()
//{
//  E2_counts2++; // for rotation
//}

void setup()
{
  // put your setup code here, to run once:

  Serial.begin(115200);

  //[NEW]
  // Serial.setTimeout(0);

  md.init();

  pinMode(E1A_INPUT, INPUT); // Motor 1 (E1)
  pinMode(E2A_INPUT, INPUT); // Motor 2 (E2)

  startTimeL = micros();
  startTimeR = micros();
  //md.setM1Speed(-0);
  //md.setM2Speed(150);

  enableInterrupt(E1A_INPUT, RPM_counterL, RISING);
  enableInterrupt(E2A_INPUT, RPM_counterR, RISING);
  //  enableInterrupt(E1A_INPUT, Rotate_counterL, RISING);
  //  enableInterrupt(E2A_INPUT, Rotate_counterR, RISING);
  // enableInterrupt(E1A_INPUT, E1_count_increment, RISING);
  // enableInterrupt(E2A_INPUT, E2_count_increment, RISING);
  // md.setM2Speed(120);
}

void loop()
{
  int buffer_size = sizeof(piCommand_buffer) / sizeof(*piCommand_buffer);

  Serial.println(buffer_size);

  j = 0, arg = 0;

  // type character for command
  // for forward & backward, type F or B with distance / 10
  while (1)
  {
    if (Serial.available())
    {
      readChar = Serial.read();
      if (isAlphaNumeric(readChar) or readChar == '|')
      {
        piCommand_buffer[i] = readChar;
        i++;

        if (readChar == '#')
        {
          return;
        }

        if (readChar == '|' || i >= buffer_size)
        {
          i = 1;
          break;
        }
      }
    }
  }

  // multiply distance argument by 10
  while (piCommand_buffer[j] != '|' && j < buffer_size)
  {
    // subtract from ASCII value to get equivalent integer value
    arg = arg + (piCommand_buffer[i] - 48);
    arg *= 10;
    j++;
  }

  motionSwitch(instruction, arg);
}

void motionSwitch(char input, int rep)
{
  //int len_command = input.length();
  //Serial.print(input);

  //Serial.print("REP is ");
  //Serial.println(rep);
  switch (input)
  {

  case 'F': //moving Forward
    for (int i = 0; i < rep; i++)
    {
      E1_counts2 = 0;
      E2_counts2 = 0;
      calculate_Distance();
      while (lrAvgDistance < 9.7)
      {
        // Serial.println(lrAvgDistance);
        moveForward();
        calculate_Distance();
      }
    }
    md.setBrakes(400, 400);
    delay(250);
    break;

  case 'L': //rotating to the left

    E1_counts2 = 0;
    E2_counts2 = 0;
    calculate_Distance();
    rotateLeft(83);

    while (lrAvgDistance < rotateDistance)
    {
      rotateLeft(83);
      calculate_Distance();
    }
    md.setBrakes(400, 400);
    delay(250);
    break;

  case 'W': //rotating to the right
    E1_counts2 = 0;
    E2_counts2 = 0;
    calculate_Distance();
    rotateRight(83);
    while (lrAvgDistance < rotateDistance)
    {
      rotateRight(83);
      calculate_Distance();
    }
    md.setBrakes(400, 400);
    delay(250);
    break;

  case 'C':
    alignFront();
    break;
  }
}

void alignFront()
{
  double front_l = checkSensorDistance(1);
  double front_r = checkSensorDistance(3);

  sensor_diff = abs(front_l - front_r);

  while (sensor_diff > 0.2 && sensor_diff < 6)
  {
    Serial.print("Sensor values are: ");
    Serial.print(front_l);
    Serial.print("     ");
    Serial.println(front_r);
    Serial.print("     ");
    Serial.println(abs(front_r - front_l));
    if (front_r > front_l)
    {
      md.setSpeeds(75, -75);
    }
    else if (front_l > front_r)
    {
      md.setSpeeds(-75, 75);
    }
    //delay(20);
    //double front_l = checkSensorDistance(1);
    // double front_r = checkSensorDistance(3);
    sensor_diff = abs(front_l - front_r);
  }
  md.setBrakes(400, 400);
}

void alignRight()
{
  //for the case when there is nothing to the left; only calibrating with right sensors

  //check both right sensors
  //if distance is equal, return
  //else, rotate till distance is equal

  double front_r = checkSensorDistance(4);
  double back_r = checkSensorDistance(5);

  sensor_diff = abs(front_r - back_r);

  if ((front_r > 11) || (back_r > 11))
  {
    return;
  }

  while ((sensor_diff > 0.2) && (sensor_diff < 6))
  {
    if (back_r > front_r)
    {
      md.setSpeeds(50, -50);
    }
    else if (front_r > back_r)
    {
      md.setSpeeds(-50, 50);
    }

    front_r = checkSensorDistance(4);
    back_r = checkSensorDistance(5);
  }
  md.setBrakes(400, 400);
}

void moveNgrids(int n)
{
  for (int i = 0; i < n; i++)
  {
    move_10cm();
  }
}

void getSensorValues()
{
  float dist1 = checkSensorDistance(1);
  float dist2 = checkSensorDistance(2);
  float dist3 = checkSensorDistance(3);

  float dist4 = checkSensorDistance(4);

  float dist5 = checkSensorDistance(5);
  float dist6 = checkSensorDistance(6);

  Serial.print(dist1);
  Serial.print("|");
  Serial.print(dist2);
  Serial.print("|");
  Serial.print(dist3);
  Serial.print("|");
  Serial.print(dist4);
  Serial.print("|");
  Serial.print(dist5);
  Serial.print("|");
  Serial.print(dist6);
  Serial.print("|");
}