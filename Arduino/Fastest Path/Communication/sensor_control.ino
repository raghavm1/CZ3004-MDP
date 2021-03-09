/*int sensorA0 = 0; // Proximity Sensor 1 on board - FRONT LEFT SENSOR.
int sensorA1 = 0; // Proximity Sensor 2 on board - FRONT MIDDLE SENSOR.
int sensorA2 = 0; // Proximity Sensor 3 on board - FRONT RIGHT SENSOR.
int sensorA3 = 0; // Proximity Sensor 4 on board - RIGHT SIDE FRONT SENSOR.
int sensorA4 = 0; // Proximity Sensor 5 on board - RIGHT SIDE BACK SENSOR.
int sensorA5 = 0; // Proximity Sensor 6 on board - FACING LEFT LONG RANGE SENSOR.*/
int count = 0;

// Float variables to hold the voltage value of the sensor outputs.
float sen1[50];
float sen2[50];
float sen3[50];
float sen4[50];
float sen5[50];
float sen6[50];

// Float variables to hold the converted distance based on sensor analog value.
double distanceA0 = 0.0;
double distanceA1 = 0.0;
double distanceA2 = 0.0;
double distanceA3 = 0.0;
double distanceA4 = 0.0;
double distanceA5 = 0.0;
double distance1_new = 0.0;
double distance2_new = 0.0;
double distance3_new = 0.0;
double distance4_new = 0.0;
double A = 0.0;
double B = 0.0;
double C = 0.0;
double distance2_f = 0.0;
// -----------------------------------------------------------------------------------
// Setup code runs once at startup.

// Looping code runs continuously.
double checkSensorDistance(int param)
{

    double distance = 0.0;
  
    for (count = 0; count < 50; count++)
    {
        // Read the analog value of each sensor and accumulate them for averaging.

        sen1[count] = analogRead(A0);
        sen2[count] = analogRead(A1);
        sen3[count] = analogRead(A2);
        sen4[count] = analogRead(A3);
        sen5[count] = analogRead(A4);
        sen6[count] = analogRead(A5);
      //delay(10);
    }
    mergeSort(sen1, 0, 49);
    mergeSort(sen2, 0, 49);
    mergeSort(sen3, 0, 49);
    mergeSort(sen4, 0, 49);
    mergeSort(sen5, 0, 49);
    mergeSort(sen6, 0, 49);

    

    switch(param){

      case 1:{
        //distance = 40543*pow(sen1[25],-1.426);
        distance = 28788*pow(sen1[25],-1.383 );//28788x-1.373

      }break;
      
      case 2:{
    //distance2_new = 4709.1*pow(sen2[25], -0.974);
        distance = 15936*pow(sen2[25], -1.219); //15592x^-1.233

    //distance = 15936*pow(sen2[25],-1.219);
      }break;
      case 3:{
        //distance= 31110*pow(sen3[25], -1.351);
        distance = 31110*pow(sen3[25], -1.351); //846642x^-1.931

      }break;
      case 4:{
        //24560x-1.335
        distance = 24560*pow(sen4[25],-1.335);
      }break;
      case 5:{
        //67.571e-0.005x
        distance = 67.571*exp(-0.005*sen5[25]);
      }break;
      case 6:{
        //PS6
        distance = 119.23*exp(-0.01*sen6[25]);
      }break;

      
      
        
    }
    /*
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
    distance2_f = A + B*exp(C*sen2[25]);
   // distance2_new = Y0+ (A /(sen2[25]-X0));
    //distance1_new = 18237*pow(sen1[25], -1.247);

    distance1_new = 12732*pow(sen1[25], -1.186);

   
   
    //distance3_new = 155.83*exp(-0.005*sen3[25]);
    distance3_new = (-29.4879) + (23951.300626/(sen2[25] + 81.9795));
    Serial.print("Analog reading for Sensor 3: ");
    Serial.println(sen3[25]);
    Serial.println(distance3_new);
   */
   return distance;
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
