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

    //  // -----------------------------------------------------------------------------------
    //  // EQUATIONS TO CONVERT THE ANALOG VALUES INTO CENTIMETERS:
    //
    //  // SENSOR 1 CALCULATION AND OFFSETS:   READINGS DIFFERENT FROM EXCEL
    //  distanceA0 = -9.1042 + (8155.745 / (sensorA0_avg + 22.11502));
    //  if(distanceA0 < 15) {distanceA0 += 2;}
    //  else if(distanceA0 > 15 and distanceA0 < 30) {distanceA0 -= 1;}
    //  else if(distanceA0 > 30 and distanceA0 < 45) {distanceA0 -= 2;}
    //  else if(distanceA0 > 50 and distanceA0 < 55) {distanceA0 += 1;}
    //  else if(distanceA0 > 65 and distanceA0 < 70) {distanceA0 += 2;}
    //
    //  // SENSOR 2 CALCULATION AND OFFSETS:
    //  distanceA1 = -3.53939 + (5891.966 / (sensorA1_avg - 11.84241));
    //  if(distanceA1 < 10) {distanceA1 += 1;}
    //  else if(distanceA1 > 15 and distanceA1 < 20) {distanceA1 += 1;}
    //
    //  // SENSOR 3 CALCULATION AND OFFSETS:
    //  distanceA2 = 1.41294 + (4269.218 / (sensorA2_avg - 28.92149));
    //  if(distanceA2 > 10 and distanceA2 < 40) {distanceA2 += 1;}
    //  else if(distanceA2 > 55 and distanceA2 < 60) {distanceA2 += 1;}
    //  else if(distanceA2 > 65) {distanceA2 += 2;}
    //
    //  // SENSOR 4 CALCULATION AND OFFSETS:
    //  distanceA3 = 0.252644 + (4894.633 / (sensorA3_avg - 26.90775));
    //  if(distanceA3 > 70) {distanceA3 -= 3;}
    //  else if(distanceA3 > 80) {distanceA3 -= 2;}
    //
    //  // SENSOR 5 CALCULATION AND OFFSETS:
    //  distanceA4 = 0.404528 + (5267.347 / (sensorA4_avg - 7.79982));
    //  if(distanceA4 < 13) {distanceA4 -= 1;}
    //  else if(distanceA4 > 50 and distanceA4 < 70) {distanceA4 -= 2;}
    //
    //  // SENSOR 6 CALCULATION AND OFFSETS:
    //  distanceA5 = -3.3012 + (12806.428 / (sensorA5_avg - 9.81909));
    //  if(distanceA5 < 25) {distanceA5 -= 1;}
    //  else if(distanceA5 > 25 and distanceA5 < 45) {distanceA5 += 1;}
    //  else if(distanceA5 > 75 and distanceA5 < 80) {distanceA5 -= 1;}

    //  Serial.print(sen1[26]);
    //  Serial.print(", ");
    //  Serial.print(sen2[26]);
    //  Serial.print(", ");
    //  Serial.print(sen3[26]);
    //  Serial.print(", ");
    //  Serial.print(sen4[26]);
    //  Serial.print(", ");
    //  Serial.print(sen5[26]);
    //  Serial.print(", ");
    //  Serial.println(sen6[26]);

    //  y0 + (A/sen-x0)

    float Y0, A, X0;

    //PS2
    Y0 = -2.2391875396427654;
    A = 6888.216506585592;
    X0 = -29.964430577988644;

    
    //PS2
    B = 87.12835168852241;
    C = -0.007743254374094627;
    A = 9.43768900416158;

    //PS3
   
    
    //distance2_new = 4709.1*pow(sen2[25], -0.974);
  //  distance2_f = A + B*exp(C*sen2[25]);
    //distance2_new = Y0+ (A /(sen2[25]-X0));
    //distance1_new = 18237*pow(sen1[25], -1.247);

    distance1_new = 12732*pow(sen1[25], -1.186);
   // distance3_new = (0.0006*sen3[25]*sen3[25]) - 0.5197*sen3[25] + 134.03;

   
   
    //distance3_new = 155.83*exp(-0.005*sen3[25]);
    //distance3_new = (-29.4879) + (23951.300626/(sen2[25] + 81.9795));
    //distance3_new = 155.83*exp(-0.005*sen3[25]);
   // distance3_new = 155.5*exp(-0.006*sen3[25]);
/*
    distance4_new = 0;
    distance3_new = 28299*pow(sen3[25],-1.199);
    
    Serial.print("Analog reading for Sensor 2: ");
    distance2_new = 14871*pow(sen2[25],-1.234);
    Serial.println(sen2[25]);
    Serial.println(distance2_new);
    Serial.println("                   ");
    Serial.print("Analog reading for Sensor 4: ");
    distance4_new = 261245*pow(sen4[25],-1.764);
    261245x-1.764
    Serial.println(sen4[25]);
    Serial.println(distance4_new);*/

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
    distance6_new = 150.12*exp(-0.005*sen6[25]);
    
    Serial.print("long range (6): ");
    Serial.println(sen6[25]);
    Serial.println("        ");
    Serial.println(distance6_new);
    

    
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
    /*
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
    Serial.println(distance3_new);
    Serial.print("        ");
    Serial.println("");

   */ 
    
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
