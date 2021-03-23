#include <Arduino.h>
#include "DualVNH5019MotorShield.h"
#include "EnableInterrupt.h"
DualVNH5019MotorShield md;

//-----Motor 1 E1/M1-----/
#define E1A_INPUT 3
#define E1B_INPUT 5

//-----Motor 2 E2/M2-----/
#define E2A_INPUT 11
#define E2B_INPUT 13

//-----Sensor initialisation-------/
/*SharpIR sensor1(1, A0);
SharpIR sensor2(1, A1);
SharpIR sensor3(1, A2);
SharpIR sensor4(1, A3);
SharpIR sensor5(1, A4);
SharpIR sensor6(3, A5);*/

// -- For reading input Code -----/
double sensor_diff = 0;
char piCommand_buffer[256], readChar, instruction, flushChar; //buffer size halved to 256
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

float rkp = 5, rki = 2.5, rkd = 2.65;
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
//#define PI 3.14159265359
float rotateDistance = 0;

boolean isExploration = false, forward_error = false;

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
  Serial.setTimeout(0);

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

  //  Serial.println(buffer_size);

  j = 0, arg = 0;
  char ins;
  // type character for command
  // for forward & backward, type F or B with distance / 10
  while (1)
  {
    if (Serial.available())
    {
      readChar = Serial.read();
      if (isAlphaNumeric(readChar) || readChar == '|')
      {
        // [NEW] : to stop execution as soon as a B is received
        if(readChar == 'B'){
          md.setBrakes(400,400);
          
          break;
        }
        //[END NEW]
        //Serial.println(readChar);
        piCommand_buffer[i] = readChar;
        
        i++;
        //Serial.println(readChar);
        //Serial.print("next buffer value is ");
        //Serial.println(piCommand_buffer[i]);
        if (readChar == '#' || readChar == 'D')
        {
          return;
        }

        

        // TODO : configure for 2 digit numbers
        else if(readChar - '0' >= 1 && readChar - '0' <=9){
          arg = readChar - '0';
          
        }

        else if (readChar == '|' || i >= buffer_size)
        {
          i = i+2;  //original is i = 1
          break;
        }
        else if(isAlpha(readChar)) ins = readChar;
      }
      else {
        //Serial.println("Wrong instruction - braking");
        md.setBrakes(400,400);
          continue;
        }
    }
  }

  instruction = ins;  //[ORIGINAL] : piCommand_buffer[0]


 
  motionSwitch(instruction, arg);
}

void motionSwitch(char input, int rep)
{
  //int len_command = input.length();

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
      while (lrAvgDistance < 8.7)
      {
        // Serial.println(lrAvgDistance);
        moveForward();
        calculate_Distance();
      }
    }
    md.setBrakes(400, 400);

    //delay(250);
    break;

  case 'L': //rotating to the left
    //for(i = 0; i < rep; i++){
    E1_counts2 = 0;
    E2_counts2 = 0;
    calculate_Distance();
    rotateLeft(89);
      while (lrAvgDistance < rotateDistance)
      {
        rotateLeft(89);
        calculate_Distance();
      }
      md.setBrakes(400, 400);

      //delay(250);
    //}
    //md.setBrakes(400, 400);
    
    break;


  case 'W': //rotating to the right
    //for(i = 0; i < rep; i++){
      E1_counts2 = 0;
      E2_counts2 = 0;
      calculate_Distance();
      rotateRight(90);
      while (lrAvgDistance < rotateDistance)
      {
        rotateRight(90);
        calculate_Distance();
      }
      md.setBrakes(400, 400);

    break;

  case 'C':
    alignFront();
    //getSensorValues();
    break;

  case 'B':
    adjustDistance();
    break;

  case 'V':
    alignRight();
    break;
  case 'E':
    isExploration = true;

    break;

 case 'D':
 md.setBrakes(400,400);

 break;

 default:
 break;
  }



  // Confirm the order of the instructions below.
  if (isExploration == true)
  {
    if (input != 'E')
    { 
      //Serial.println("Going to adjust distance");
      adjustDistance();
      
      delay(100);

      //Serial.println("Going to align right");
      alignRight();
      delay(20);

      //Serial.println("Going to align front");
      alignFront();
      delay(20);
      getSensorValues();
    }
  }
}

void adjustDistance(){
  double front_m = checkSensorDistance(2);
  delay(500);
  if(checkSensorDistance(1) > 10){
    return;
  }

  /*while(checkSensorDistance(1) > 6 || checkSensorDistance(3) > 6){
    
    if(checkSensorDistance(1) <=5.5 || checkSensorDistance(3) <= 5.5 ){
      md.setSpeeds(75,-75);
    }
  }*/
  double front_l = checkSensorDistance(1);
  double front_r = checkSensorDistance(3);
  while (front_l <= 6 || front_r <= 6){
   // Serial.println(checkSensorDistance(2));
    if (front_l < 4.8 || front_r < 4.8){
      md.setSpeeds(-75, 75);
      //Serial.println("backward");
    }
    else break;
    
//    else if (checkSensorDistance(2) >= 4.9 && checkSensorDistance(2) <= 5.8){
//      md.setBrakes(400, 400);
//      break;
//    }
     front_l = checkSensorDistance(1);
     front_m = checkSensorDistance(2);
   front_r = checkSensorDistance(3);
    
   }
  
}

void alignFront()
{
  delay(100);
  double front_l = checkSensorDistance(1);
  double front_r = checkSensorDistance(3);

  sensor_diff = abs(front_l - front_r);

  if(front_l > 10 || front_r > 10){
    return;
  }
  
  while (sensor_diff > 0.2 && sensor_diff < 6)
  {
    //    Serial.print("Sensor values are: ");
    //    Serial.print(front_l);
    //    Serial.print("     ");
    //    Serial.println(front_r);
    //    Serial.print("     ");
    //    Serial.println(abs(front_r - front_l));
    if (checkSensorDistance(3) > checkSensorDistance(1)+0.35)
    {
      md.setSpeeds(-50, -50);
    }
    else if (checkSensorDistance(1) > checkSensorDistance(3))
    {
      md.setSpeeds(50, 50);
    }
    else{
      break;
    }
    front_l = checkSensorDistance(1);
    front_r = checkSensorDistance(3);

    sensor_diff = abs(front_l - front_r);
  }
  md.setBrakes(400, 400);
}

void wallCalib(){
  alignRight();
  if(checkSensorDistance(4) < 6){
    rotateRight(90);
    alignFront();
    adjustDistance();
    rotateLeft(89);
    alignRight();
  }
}

void alignRight()
{
  //for the case when there is nothing to the left; only calibrating with right sensors

  //check both right sensors
  //if distance is equal, return
  //else, rotate till distance is equal
  Serial.println("Doing AlignRight");
  double back_r = checkSensorDistance(4);
  double front_r = checkSensorDistance(5) ;
  sensor_diff = abs(back_r - front_r);
  if ((back_r > 12) || (front_r > 12)){
    return;
  }
  if(back_r < 4.5 || front_r < 4.5){
    
    E1_counts2 = 0;
    E2_counts2 = 0; 
    calculate_Distance();
    rotateRight(90);  
    while (lrAvgDistance < rotateDistance)
    {
        //Serial.print("inside ar");
        rotateRight(90);
        calculate_Distance();
    }
    Serial.println("finishing rotation right");
      md.setBrakes(400, 400);
     delay(200);

    adjustDistance();
    
    E1_counts2 = 0;
    E2_counts2 = 0;
    calculate_Distance();
    rotateLeft(89);
    while (lrAvgDistance < rotateDistance)
    {
      rotateLeft(89);
      calculate_Distance();
    }
      md.setBrakes(400, 400);
      delay(100);
  }
  

   back_r = checkSensorDistance(4);
   front_r = checkSensorDistance(5) ;
  sensor_diff = abs(back_r - front_r);
  while ((sensor_diff > 0.2) && (sensor_diff < 6))//0.2
  { //getSensorValues();
    //Serial.println(checkSensorDistance(5));
    //Serial.println(checkSensorDistance(4));
    //Serial.println("########");

    if(sensor_diff < 0.25){
      break;
    }
    if (back_r >= front_r)
    {
          E1_counts2 = 0;
    E2_counts2 = 0;
    calculate_Distance();
    rotateLeft(0.9);
    while (lrAvgDistance < rotateDistance)
    {
      rotateLeft(0.9);
      calculate_Distance();
    }
      md.setBrakes(400, 400);
    // md.setSpeeds(-50, -50); //md.setSpeeds(-75, -75);
    }
    else if (front_r >= back_r)
    {
      E1_counts2 = 0;
    E2_counts2 = 0;
    calculate_Distance();
    rotateRight(0.9);
    while (lrAvgDistance < rotateDistance)
    {
      rotateRight(0.9);
      calculate_Distance();
    }
     // md.setSpeeds(50, 50);
    }

    back_r = checkSensorDistance(4);
    front_r = checkSensorDistance(5);
    sensor_diff = abs(back_r - front_r);
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
  Serial.print(checkSensorDistance(3));
  Serial.print("|");
  Serial.print(checkSensorDistance(2));
  Serial.print("|");
  Serial.print(checkSensorDistance(1));
  Serial.print("|");
  Serial.print(checkSensorDistance(4));
  Serial.print("|");
  Serial.print(checkSensorDistance(5));
  Serial.print("|");
  Serial.print(checkSensorDistance(6));
  if (forward_error == true)
  {
    Serial.print("|");
    Serial.println("0");
  }
  else
  {
    Serial.print("|");
    Serial.println("1");
  }
  forward_error = false;
  //  Serial.println("");
}
