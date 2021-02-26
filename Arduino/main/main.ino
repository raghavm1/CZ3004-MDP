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
SharpIR sensor3(3, A2);
SharpIR sensor4(1, A3);
SharpIR sensor5(1, A4);


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
float TargetRPM = 100;

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
float lkp = 5.0, lki = 2.2, lkd = 1.75;
float lpTerm, ldTerm, liTerm;
float lprevPosition; // last position
float lerror = 0, lsumError = 0; 
float l_speed=0;
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

float rkp = 5, rki = 2.3, rkd = 1.8;
float rpTerm, rdTerm, riTerm;
float rprevPosition; // last position
float rerror = 0, rsumError = 0; 
float r_speed=0;
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
    while(1);
  }
  if (md.getM2Fault())
  {
    Serial.println("M2 fault");
    while(1);
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
  Serial.println("Dual VNH5019 Motor Shield");
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
  //obstacle_avoid();

  
  
/*

  float stopDistance = 10;//+10 for robot circumference
  float secondStop = 0;

 if(checkSensorDistance(1) > 15 && checkSensorDistance(5) > 15){ 
  Serial.println("\nInside loop");
  moveForward();
  
 }else{
    calculate_Distance();
    stopDistance = lrAvgDistance;
    Serial.println("\n###############  TURNING LEFT  ####################");
    md.setBrakes(400,400);
    delay(2000);
    E1_counts2 = 0;
    E2_counts2 = 0;
    do{
    rotateLeft(45);
    }while(lrAvgDistance < rotateDistance);
    
    Serial.println("\nNEXT STEP");
    delay(2000);
       // float start_time = millis();
        Serial.println("\n###############  MOVING FORWARD  ####################");
       // while(millis() - start_time <= 1200) moveForward();
       E1_counts2 = 0;
       E2_counts2 = 0;
       while(lrAvgDistance < 20){
         moveForward();
         calculate_Distance();
         Serial.println("lrAvgDistance :");
         Serial.print(lrAvgDistance);
        }

        md.setBrakes(400,400);
        delay(2000);
        Serial.println("\n###############  TURNING RIGHT  - 1 ####################");
        E1_counts2 = 0 ;
        E2_counts2 = 0;
        do{
          rotateRight(45);
        }while(lrAvgDistance < rotateDistance);
        
        delay(2000);
//        start_time = millis();
        Serial.println("###############  GOING FORWARD - 2  ####################");
        //while(millis() - start_time <= 1300)moveForward();
       E1_counts2 = 0;
       E2_counts2 = 0;
       while(lrAvgDistance < 30){
         moveForward();
         calculate_Distance();
         Serial.println("lrAvgDistance :");
         Serial.print(lrAvgDistance);
       }
        md.setBrakes(400,400);
        delay(2000);

        Serial.println("###############  TURNING RIGHT -2 ####################");
        E1_counts2 = 0 ;
        E2_counts2 = 0;
        do{
          rotateRight(45);
        }while(lrAvgDistance < rotateDistance);
        
        delay(2000);
//        start_time = millis();
        Serial.println("###############  GOING FORWARD - 3  ####################");
        //while(millis() - start_time <= 1300)moveForward();
       E1_counts2 = 0;
       E2_counts2 = 0;
       while(lrAvgDistance < 20){
         moveForward();
         calculate_Distance();
         Serial.println("lrAvgDistance :");
         Serial.print(lrAvgDistance);
       }
        md.setBrakes(400,400);

        

        delay(2000);
        Serial.println("###############  TURNING LEFT  ####################");
        E1_counts2 = 0;
        E2_counts2 = 0;
        do{
          rotateLeft(45);
          }while(lrAvgDistance <rotateDistance);
          
        delay(2000);
        Serial.println("###############  FINAL MOVING STRAIGHT: stopDistance till now is: ");
        Serial.print(stopDistance);
        E1_counts2 = 0;
        E2_counts2 = 0;
        
        

       while(lrAvgDistance + stopDistance + 60 < 150){ 
        moveForward();
        calculate_Distance();
        
        }
        while(true){
          Serial.println("Distance is now: ");
          Serial.print(lrAvgDistance+stopDistance+40);
          md.setBrakes(400,400);
          }
       
    
 
        
        
    
  }*/
 
  
 
  

 

  if(Serial.available() > 0)
  {
   // String data = Serial.readStringUntil('\n');
    //input = data.charAt(0);
    input = Serial.read(); 
  }

  switch(input)
  {
      case 'w': Forward = true; //backward
                Backward = false;
                rotate_Left = false;
                rotate_Right = false;
                move10cm = false;
                break;
        
      case 's': Forward = false; //forward
                Backward = true;
                rotate_Left = false;
                rotate_Right = false;
                move10cm = false;
                break;

      case 'a': Forward = false; //rotate right
                Backward = false;
                rotate_Left = true;
                rotate_Right = false;
                break;
      case 'd': Forward = false; // rotate left
                Backward = false;
                rotate_Left = false;
                rotate_Right = true;
                move10cm = false;
                break;
        
      case 'e': Forward = false; //stop
                Backward = false;
                rotate_Left = false;
                rotate_Right = false;
                move10cm = false;
                break;
      case 'q': Forward = false; //stop
                Backward = false;
                rotate_Left = false;
                rotate_Right = false;
                move10cm = true;
                break;
     case 'k':alignRight(); 
     break;
   }
   
  if(Forward == true && Backward == false && rotate_Left == false && rotate_Right == false && move10cm == false)
  {
    calculate_Distance();
    
    if(lrAvgDistance <120){
      
     Serial.println("\nDistance travelled is");
     Serial.print(lrAvgDistance);
      moveForward();
    }else{
      md.setBrakes(400,400);
      input = 'e';
      E1_counts2 =0;
      E2_counts2 = 0;
    }
  }
  else if(Forward == false && Backward == true && rotate_Left == false && rotate_Right == false && move10cm == false)
  {
    moveBackward();
  }
  else if(Forward  == false && Backward == false && rotate_Left == true && rotate_Right == false && move10cm == false)
  {
      rotateLeft(45);
  
  }
  else if(Forward  == false && Backward == false && rotate_Left == false && rotate_Right == true && move10cm == false)
  {
    rotateRight(45);
  }
  else if(Forward == false && Backward == false && rotate_Left == false && rotate_Right == false && move10cm == false)
  {
    md.setBrakes(300,300);
  }
  else if(Forward == false && Backward == false && rotate_Left == false && rotate_Right == false && move10cm == true)
  {
    move_10cm();
  }


  delay(10);

}


void alignRight(){
//for the case when there is nothing to the left; only calibrating with right sensors
  
  //check both right sensors
  //if distance is equal, return
  //else, rotate till distance is equal

  double front_r = checkSensorDistance();
  double back_r = checkSensorDistance();

  if(front_r == back_r){
    return;
  }else{
    while(front_r - back_r !=0){
    if(front_r > back_r){
      md.setSpeeds(-50,50);
    }else{
      md.setSpeeds(50,-50);
    }
    md.setBrakes(400,400);
    front_r = checkSensorDistance();
    back_r = checkSensorDistance();
  }
  }
}


/*void alignRight(){
  double r1 = checkSensorDistance(2);
  double l1 = checkSensorDistance(6);

  double diff = abs(r1-l1);

  if(r1 > 11 || l1> 11){
    return;
    
  }

  while ((diff > 0.2) && (diff < 6)) {
    if (l1 > r1) {
      md.setSpeeds(-50, 50);
    }
    else if (r1 > l1) {
      md.setSpeeds(50, -50);
    }
    //delay(30);
    r1 = checkSensorDistance(2);
    l1 = checkSensorDistance(6);
    diff = abs(r1 - l1);
  }
    md.setBrakes(400, 400);

    input = 'e';
}*/
