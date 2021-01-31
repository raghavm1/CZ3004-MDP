#include <Arduino.h>
#include <SharpIR.h>
#include "DualVNH5019MotorShield.h"
#include "PinChangeInterrupt.h"
#include "Math.h"
#include "PID_v2.h"

DualVNH5019MotorShield md;
#define IRPin A3
#define model 1

// Create a new instance of the SharpIR class:
SharpIR mySensor(IRPin, 1);

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

double TURN_R = 11.00; //new bat pos 6.2V Batt A Albert PB // Bat B 8.56 // Bat A 8.58
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
    md.setM2Speed(r_speed);
}

void rotateRight(double angle)
{
    startLeftEncoderValue = leftEncoderValue;
    startRightEncoderValue = rightEncoderValue;
    double target_Tick = 0;

    if (angle <= 90)
        target_Tick = angle * TURN_R; //8.96
    else if (angle <= 180)
        target_Tick = angle * 8.80; //tune 180
    else if (angle <= 360)
        target_Tick = angle * 8.65;
    else
        target_Tick = angle * 8.9;

    while (rightEncoderValue < startRightEncoderValue + target_Tick)
    {
        md.setSpeeds((200 + Output), -(200 - Output));
        myPID.Compute();
    }
    md.setBrakes(400, 400);
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
    // put your setup code here, to run once:
    pinMode(LEFT_ENCODER, INPUT);  //set digital pin 11 as input
    pinMode(RIGHT_ENCODER, INPUT); //set digital pin 3 as input
    Serial.begin(115200);
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

    PID_computeL(rpmLeft);
    PID_computeR(rpmRight);

    Serial.print("rpmLeft: ");
    Serial.print(rpmLeft);
    Serial.print(" ");
    Serial.print("rpmRight: ");
    Serial.print(rpmRight);
    Serial.print(" ");
    Serial.println("uT");

    //rotateRight(90);

    //delay(10000);

    Serial.println("Distance is ");
    Serial.print(mySensor.getDistance());
    if (mySensor.getDistance() <= 15)
    {
        md.setBrakes(200, 200);
        rotateRight(90);
    }

    //rotateRight(180);

    //delay(100);
}