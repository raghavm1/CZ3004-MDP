#include <Arduino.h>
#include <SharpIR.h>
#include "DualVNH5019MotorShield.h"
#include "PinChangeInterrupt.h"
#include "Math.h"
#include "PID_v2.h"

DualVNH5019MotorShield md;
#define model 1

//-----Sensor init-------/
SharpIR sensor1(1, A0); 
SharpIR sensor2(1, A1);
SharpIR sensor3(3, A2);
SharpIR sensor4(1, A3);
SharpIR sensor5(1, A4);
SharpIR sensor6(1, A5);

//-----Motor 1 E1/M1-----/
#define E1A_INPUT 3
#define E1B_INPUT 5

//-----Motor 2 E2/M2-----/
#define E2A_INPUT 11
#define E2B_INPUT 13

//-----WH testing-----/
float left_tick;
float right_tick;
float freqL;
float rpmLeft;
float freqR;
float rpmRight;

//-----Target RPM-----/
float TargetRPM = 100;

//-----Ticks Variable-----/
volatile long E1_counts = 0;
volatile long E2_counts = 0;

//-----rpmL count-----/
float startTimeL;
float currentTimeL;
float RevolutionTimeL;
float fourRev_TimeL;
float fourRev_CounterL = 0;
float oneRev_TimeL;
float current_TimeL;

//-----rpmR count-----/
float startTimeR;
float currentTimeR;
float RevolutionTimeR;
float fourRev_TimeR;
float fourRev_CounterR = 0;
float oneRev_TimeR;
float current_TimeR;

//---left PID------/
float lkp = 1, lki = 0, lkd = 0.0024;
float lerror = 0, lprev_error = 0, lsumError = 0;
float l_speed = 0;

//---right PID------/
float rkp = 1, rki = 0, rkd = 0.0029;
float rerror = 0, rprev_error = 0, rsumError = 0;
float r_speed = 0;

double TURN_R = 16.00; //new bat pos 6.2V Batt A Albert PB // Bat B 8.56 // Bat A 8.58
double leftEncoderValue = 0;
double rightEncoderValue = 0;
double Setpoint, Input, Output;

double startRightEncoderValue;
double startLeftEncoderValue;

PID myPID(&leftEncoderValue, &Output, &rightEncoderValue, 0.5, 0, 0, DIRECT);

#define LEFT_ENCODER 11 //left motor encoder A to pin 11
#define RIGHT_ENCODER 3 //right motor encoder A to pin 3

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

//---PID Calculation for Left motor------/
void PID_computeL(float rpmL)
{
    lerror = TargetRPM - rpmL; //Finding the error using our target RPM - current RPM
                               //  l_speed = rpmL + lkp*lerror + lkd * (lerror - lprev_error);
    l_speed -= (lerror * lkp) + (lprev_error * lki) + (lsumError * lkd);
    lprev_error = lerror;
    lsumError = lsumError + lerror;
    md.setM1Speed(-l_speed);
}

//---PID Calculation for Right motor------/
void PID_computeR(float rpmR)
{
    rerror = TargetRPM - rpmR; //Finding the error using our target RPM - current RPM
    //r_speed = rpmR + rkp*rerror + rkd * (rerror - rprev_error);
    r_speed += (rerror * rkp) + (rprev_error * rki) + (rsumError * rkd);
    rprev_error = rerror;
    rsumError = rsumError + rerror;
    md.setM2Speed(-r_speed);
}

void rotateRight(double angle)
{
    startLeftEncoderValue = leftEncoderValue;
    startRightEncoderValue = rightEncoderValue;
    double target_Tick = 0;

    if (angle <= 90)
        target_Tick = angle *TURN_R ; //8.96
    else if (angle <= 180)
        target_Tick = angle * 8.80; //tune 180
    else if (angle <= 360)
        target_Tick = angle * 8.65;
    else
        target_Tick = angle * 8.9;

    while (rightEncoderValue < startRightEncoderValue + target_Tick)
    {
        md.setSpeeds((350 + Output), -(350 - Output));
        myPID.Compute();
    }
    md.setBrakes(350, 350);
    delay(5);
}
void leftEncoderInc(void)
{
    leftEncoderValue++;
}

void rightEncoderInc(void)
{
    rightEncoderValue++;
}
void setup()
{
  Serial.begin(115200);
    pinMode(LEFT_ENCODER, INPUT);  //set digital pin 11 as input
    pinMode(RIGHT_ENCODER, INPUT); //set digital pin 3 as input
    
    Serial.println("Dual VNH5019 Motor Shield");
    md.init();
    attachPCINT(digitalPinToPCINT(RIGHT_ENCODER), rightEncoderInc, HIGH);
    myPID.SetOutputLimits(-50, 50);
    myPID.SetMode(AUTOMATIC);

    pinMode(E1A_INPUT, INPUT); // Motor 1 (E1)
    pinMode(E2A_INPUT, INPUT); // Motor 2 (E2)

    startTimeL = millis();
    startTimeR = millis();

    md.setM1Speed(200);
    md.setM2Speed(200);

    //  enableInterrupt(E1A_INPUT, RPM_counterL, RISING);
    //  enableInterrupt(E2A_INPUT, RPM_counterR, RISING);

    // enableInterrupt(E1A_INPUT, E1_count_increment, RISING);
    // enableInterrupt(E2A_INPUT, E2_count_increment, RISING);
}

void loop()
{
    // put your main code here, to run repeatedly:

    left_tick = pulseIn(E1A_INPUT, HIGH);
    rpmLeft = ((1 / (2 * left_tick)) * pow(10, 6) * 60) / 562.25;

    right_tick = pulseIn(E2A_INPUT, HIGH);
    rpmRight = ((1 / (2 * right_tick)) * pow(10, 6) * 60) / 562.25;

   // PID_computeL(rpmLeft);
    //PID_computeR(rpmRight);

    Serial.flush();
    char a = Serial.read();

    switch(a){
      case 'w':{
            PID_computeL(rpmLeft);
          PID_computeR(rpmRight);
        }
          break;
         case 'a':
            //rotateLeft(90);
            break;
         case 'd':
         rotateRight(90);
            break;
        
    }
    Serial.print("rpmLeft: ");
    Serial.print(rpmLeft);
    Serial.print(" ");
    Serial.print("rpmRight: ");
    Serial.print(rpmRight);
    Serial.print(" ");
    Serial.println("uT");


    

    //rotateRight(90);

    //delay(10000);
    //checkSensorDistance(1);
    //checkSensorDistance(5);
}

void checkSensorDistance_wo(int a){
//  float arr[300];
  md.setBrakes(200,200);
  switch(a){
    case 1:
      Serial.println("Distance of PS1: ");
        Serial.print(sensor1.getDistance());
      
      Serial.println();
      break;
    case 2:
      Serial.println("Distance of PS2: ");
        Serial.print(sensor2.getDistance());
      Serial.println();
      
      break;
    case 3:
      Serial.println("Distance of PS3: ");
        Serial.print(sensor3.getDistance());
     Serial.println();
      break;
    case 4:
      Serial.println("Distance of PS4: ");
        Serial.print(sensor4.getDistance());
      Serial.println();
      
      break;
    case 5:
      Serial.println("Distance of PS5: ");
        Serial.print(sensor5.getDistance());
      Serial.println();
      
      break;
    case 6:
      Serial.println("Distance of PS6: ");
        Serial.print(sensor6.getDistance());
      Serial.println();
      
      break;
  }
  delay(500);
  
}

void checkSensorDistance(int a){
  float arr[100];
  md.setBrakes(200,200);
  switch(a){
    case 1:
      for(int i = 0; i<100; i++){
        arr[i] = sensor1.getDistance();
      }
      mergeSort(arr, 0, 99);
      Serial.println("Distance of PS1: ");
      Serial.print(arr[45]);
            Serial.println();

      break;
    case 2:
      for(int i = 0; i<100; i++){
        arr[i] = sensor2.getDistance();
      }
      mergeSort(arr, 0, 99);
      Serial.println("Distance of PS2: ");
      Serial.print(arr[45]);
            Serial.println();

      break;
    case 3:
      for(int i = 0; i<100; i++){
      arr[i] = sensor3.getDistance();
      }
      mergeSort(arr, 0, 99);
      Serial.println("Distance of PS3: ");
      Serial.print(arr[45]);
            Serial.println();

      break;
    case 4:
      for(int i = 0; i<100; i++){
        arr[i] = sensor4.getDistance();
      }
      mergeSort(arr, 0, 99);
      Serial.println("Distance of PS4: ");
      Serial.print(arr[45]);
            Serial.println();

      break;
    case 5:
      for(int i = 0; i<100; i++){
      arr[i] = sensor5.getDistance();
      }
      mergeSort(arr, 0, 99);
      Serial.println("Distance of PS5: ");
      Serial.print(arr[45]);
            Serial.println();

      break;
    case 6:
      for(int i = 0; i<100; i++){
        arr[i] = sensor6.getDistance();
      }
      mergeSort(arr, 0, 99);
      Serial.println("Distance of PS6: ");
      Serial.print(arr[45]);
            Serial.println();

      break;
  }
  delay(500);
  
}

void sensorTest(int angle)
{
    rotateRight(angle);
    Serial.println("#################");
    Serial.println("SensorTest distance: ");
    Serial.print(sensor1.getDistance());

    if(sensor1.getDistance() <= 15){
      md.setBrakes(200,200);
      delay(500);
      rotateRight(90);
      delay(100);
    }
}


void merge(float arr[], int l, int m, int r)
{
    int i, j, k;
    int n1 = m - l + 1;
    int n2 = r - m;
 
    /* create temp arrays */
    float L[n1], R[n2];
 
    /* Copy data to temp arrays L[] and R[] */
    for (i = 0; i < n1; i++)
        L[i] = arr[l + i];
    for (j = 0; j < n2; j++)
        R[j] = arr[m + 1 + j];
 
    /* Merge the temp arrays back into arr[l..r]*/
    i = 0; // Initial index of first subarray
    j = 0; // Initial index of second subarray
    k = l; // Initial index of merged subarray
    while (i < n1 && j < n2) {
        if (L[i] <= R[j]) {
            arr[k] = L[i];
            i++;
        }
        else {
            arr[k] = R[j];
            j++;
        }
        k++;
    }
 
    /* Copy the remaining elements of L[], if there
    are any */
    while (i < n1) {
        arr[k] = L[i];
        i++;
        k++;
    }
 
    /* Copy the remaining elements of R[], if there
    are any */
    while (j < n2) {
        arr[k] = R[j];
        j++;
        k++;
    }
}
 
/* l is for left index and r is right index of the
sub-array of arr to be sorted */
void mergeSort(float arr[], int l, int r)
{
    if (l < r) {
        // Same as (l+r)/2, but avoids overflow for
        // large l and h
        int m = l + (r - l) / 2;
 
        // Sort first and second halves
        mergeSort(arr, l, m);
        mergeSort(arr, m + 1, r);
 
        merge(arr, l, m, r);
    }
}
