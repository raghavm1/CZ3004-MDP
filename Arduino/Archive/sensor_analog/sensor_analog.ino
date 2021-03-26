#include <math.h>

// MANUAL PROXIMITY SENSOR CALIBRATION.

// Integer variables to hold sensor analog values.
// Voltage values range: 0V to 5V, Analog integer values range: 0 to 1023.
int sensorA0 = 0; // Proximity Sensor 1 on board - FRONT LEFT SENSOR.
int sensorA1 = 0; // Proximity Sensor 2 on board - FRONT MIDDLE SENSOR.
int sensorA2 = 0; // Proximity Sensor 3 on board - FRONT RIGHT SENSOR.
int sensorA3 = 0; // Proximity Sensor 4 on board - RIGHT SIDE FRONT SENSOR.
int sensorA4 = 0; // Proximity Sensor 5 on board - RIGHT SIDE BACK SENSOR.
int sensorA5 = 0; // Proximity Sensor 6 on board - FACING LEFT LONG RANGE SENSOR.
int count = 0;

// Float variables to hold the voltage value of the sensor outputs.
float sen1[50];
float sen2[50];
float sen3[50];
float sen4[50];
float sen5[50];
float sen6[50];

// Float variables to hold the converted distance based on sensor analog value.
double distance1_new = 0.0;
double distance2_new = 0.0;
double distance3_new = 0.0;
double distance4_new = 0.0;
double distance5_new = 0.0;
double distance6_new = 0.0;

double A = 0.0;
double B = 0.0;
double C = 0.0;
double distance2_f = 0.0;
// -----------------------------------------------------------------------------------
// Setup code runs once at startup.
void setup()
{
    Serial.begin(115200);
}

// Looping code runs continuously.
void loop()
{

    for (count = 0; count < 50; count++)
    {
        // Read the analog value of each sensor and accumulate them for averaging.

        sen1[count] = analogRead(A0);
        sen2[count] = analogRead(A1);
        sen3[count] = analogRead(A2);
        sen4[count] = analogRead(A3);
        sen5[count] = analogRead(A4);
        sen6[count] = analogRead(A5);
      delay(10);
    }

    mergeSort(sen1, 0, 49);
    mergeSort(sen2, 0, 49);
    mergeSort(sen3, 0, 49);
    mergeSort(sen4, 0, 49);
    mergeSort(sen5, 0, 49);
    mergeSort(sen6, 0, 49);


   

//Front Sensors Calibration for Battery A : [6.371V]
/*
    distance1_new = 40543*pow(sen1[25], -1.426); //38810x^-1.44
    distance2_new = 15936*pow(sen2[25], -1.219); //15592x^-1.233
    distance3_new = 31110*pow(sen3[25], -1.351); //846642x^-1.931
    
    if(distance1_new <= 10.5 && distance1_new >=9 || distance2_new <= 10.5 && distance2_new >=9 || distance3_new <= 10.5 && distance3_new >=9){
      distance1_new = distance1_new + 0.6;
      distance2_new = distance2_new + 0.6;
      distance3_new = distance3_new + 0.6;
    }
*/
/*
//Front sensor calibration : Baterry B [6.440V]
    distance1_new = 28788*pow(sen1[25],-1.383 );//28788x-1.373
    distance2_new = 15936*pow(sen2[25], -1.219); //15592x^-1.233
    distance3_new = 31110*pow(sen3[25], -1.351); //846642x^-1.931
    */
    //150.12e-0.005x
    //distance6_new = 150.12*exp(-0.005*sen6[25]);
    distance1_new = 40092*pow(sen1[25],-1.442);
    distance2_new = 14328*pow(sen2[25], -1.208);
    distance3_new = 19278*pow(sen3[25], -1.274);
    //distance4_new = 2166.5*pow(sen4[25],-0.771);
   // distance4_new = 27714*pow(sen4[25],-1.355); //16/03/21, 1035am: 27714x^-1.355
    distance4_new = 43878*pow(sen4[25],-1.435); //16/03/21, 1208pm Batt B: 43878x^-1.435
   // distance5_new = 17208*pow(sen5[25], -1.254); 
    distance5_new = 40258*pow(sen5[25], -1.416); //16/03/21, 1208pm Batt B: = 40258x^-1.416
    distance6_new = 137.08*exp(-0.005*sen6[25]);
//
/*  if(distance1_new < 8.2){
         distance1_new = distance1_new - 1.7;
        }else if(distance1_new > 6){
         distance1_new = distance1_new +0.5 ;
        }
        
  if(distance2_new < 7.6){
         distance2_new = distance2_new - 1.5;
        }else if(distance2_new > 6){
         distance2_new = distance2_new +0.5 ;
        }
  if(distance3_new < 7.5){
         distance3_new = distance3_new - 1.4;
        }else if(distance2_new > 6){
         distance3_new = distance3_new +0.5 ;
        }*/
   /* Serial.print("front left(1): ");
    Serial.println(sen1[25]);
    Serial.println("        ");
    Serial.println(distance1_new);

    Serial.print("front center(2): ");
    Serial.println(sen2[25]);
    Serial.println("        ");
    Serial.println(distance2_new);

    Serial.print("front right(3): ");
    Serial.println(sen3[25]);
    Serial.println("        ");
    Serial.println(distance3_new);*/

  /*  distance4_new = 2166.5*pow(sen4[25],-0.771);
    Serial.print("right back(4): ");
    Serial.println(sen4[25]);
    Serial.println("        ");
    Serial.println(distance4_new);*/

    //Serial.print("right front(5): ");
    //Serial.println(sen5[25]);
    //Serial.println("        ");
    //Serial.println(distance3_new)
    
/*
    distance4_new = 24560*pow(sen4[25],-1.33);
    distance5_new = 67.571*exp(-0.005*sen5[25]);

    Serial.print("Right_back (4): ");
    Serial.print(sen4[25]);
    Serial.print("        ");
    Serial.println(distance4_new);
    
    Serial.print("Right_front (5): ");
    Serial.print(sen5[25]);
    Serial.print("        ");
    Serial.println(distance5_new);
        Serial.println("");*/
    
    Serial.print("Front_Left (1): ");
    Serial.print(sen1[25]);
    Serial.print("        ");
    Serial.println(distance1_new);
    
    Serial.print("Front_Center (2): ");
    Serial.print(sen2[25]);
    Serial.print("        ");
    Serial.println(distance2_new);

    Serial.print("Front_Right (3): ");
    Serial.print(sen3[25]);
    Serial.print("        ");
    Serial.println(distance3_new+0.1);
    Serial.print("        ");
    Serial.println("");

    Serial.print("Right_back (4): ");
    Serial.print(sen4[25]);
    Serial.print("        ");
    Serial.println(distance4_new);
    Serial.print("        ");
    Serial.println("");

    Serial.print("Right_front (5): ");
    Serial.print(sen5[25]);
    Serial.print("        ");
    Serial.println(distance5_new);
    Serial.print("        ");
    Serial.println("");

    Serial.print("Left_front (6): ");
    Serial.print(sen6[25]);
    Serial.print("        ");
    Serial.println(distance6_new);
    Serial.print("        ");
    Serial.println("");
   //distance6_new = 137.08*exp(-0.005*(sen6[25]));
   //y = 17208x-1.254


    
   delay(750);
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
    while (i < n1 && j < n2)
    {
        if (L[i] <= R[j])
        {
            arr[k] = L[i];
            i++;
        }
        else
        {
            arr[k] = R[j];
            j++;
        }
        k++;
    }

    /* Copy the remaining elements of L[], if there
    are any */
    while (i < n1)
    {
        arr[k] = L[i];
        i++;
        k++;
    }

    /* Copy the remaining elements of R[], if there
    are any */
    while (j < n2)
    {
        arr[k] = R[j];
        j++;
        k++;
    }
}

void mergeSort(float arr[], int l, int r)
{
    if (l < r)
    {
        // Same as (l+r)/2, but avoids overflow for
        // large l and h
        int m = l + (r - l) / 2;

        // Sort first and second halves
        mergeSort(arr, l, m);
        mergeSort(arr, m + 1, r);

        merge(arr, l, m, r);
    }
}
