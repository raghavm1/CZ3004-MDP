//---PID Calculation for the left motor------/
double PID_computeL()
{
    //Proportional
    lerror = TargetRPM - rpmL; //Finding the error using our target RPM - current RPM
    lpTerm = lkp * lerror;     //caculate P term

    //Integral
    lsumError += lerror;
    if (lsumError > lmax)
    {
        lsumError = lmax;
    }
    else if (lsumError < lmin)
    {
        lsumError = lmin;
    }
    liTerm = lki * lsumError; // caculate i term
                              //
                              //  
    // Derivative
    ldTerm = lkd * (lprevPosition - rpmL);
    lprevPosition = rpmL;

    return (lpTerm + ldTerm + liTerm) * 1.041346578; //the computed speed after PID // 1.011929038 // 1.131346578
}

//---PID Calculation for the right motor------/
double PID_computeR()
{
    //Proportional
    rerror = TargetRPM - rpmR; //Finding the error using our target RPM - current RPM
    rpTerm = rkp * rerror;     //caculate P term

    //Integral
    rsumError += rerror;
    if (rsumError > rmax)
    {
        rsumError = rmax;
    }
    else if (rsumError < rmin)
    {
        rsumError = rmin;
    }
    riTerm = rki * rsumError; // caculate i term

    // Derivative
    rdTerm = rkd * (rprevPosition - rpmR);
    rprevPosition = rpmR;

    return rpTerm + rdTerm + riTerm; //the computed speed after PID
}

//---RPM Calculation for the left motor------/
void RPM_counterL()
{
    E1_counts2++;

    if (E1_counts < 10) //count the ticks per Revolution (562.25 ticks is 1 rev)
    {
        E1_counts++;
    }
    else
    {
        currentTimeL = micros();                                              //current time
        oneRev_TimeL = ((currentTimeL - startTimeL) / 10 * 562.25) / 1000000; //time in seconds for 1 rev
        rpmL = 60 / oneRev_TimeL;                                             //rev per min
        startTimeL = currentTimeL;                                            // reset the start time again

        //Display Left RPM
        //Serial.print("Left Motor (E1) Rpm: ");
        //Serial.println(rpmL);

        E1_counts = 0;
    }
}
//---RPM Calculation for the right motor------/
void RPM_counterR()
{
    E2_counts2++;

    if (E2_counts < 10) //count the ticks per Revolution (562.25 ticks is 1 rev)
    {
        E2_counts++;
    }
    else
    {
        currentTimeR = micros();                                              //current time
        oneRev_TimeR = ((currentTimeR - startTimeR) / 10 * 562.25) / 1000000; //time in seconds for 1 rev
        rpmR = 60 / oneRev_TimeR;                                             // rev per min
        startTimeR = currentTimeR;                                            // reset the start time again

        //Display Right Rpm
        //Serial.print("Right Motor (E2) Rpm: ");
        //Serial.println(rpmR);

        E2_counts = 0;
    }
}

void moveBackward()
{

    double adjustedLeftSpeed = PID_computeL();
    double adjustedRightSpeed = PID_computeR();
    md.setSpeeds(-adjustedLeftSpeed, adjustedRightSpeed);
    E2_counts2 = 0; // needed for rotation only
    //md.setM1Speed(-adjustedLeftSpeed);
    // md.setM2Speed(adjustedRightSpeed);
    //  Serial.print(adjustedLeftSpeed);
    //  Serial.print(" ");


    //  Serial.print(" ");
}

void moveForward()
{
    
    double adjustedLeftSpeed = PID_computeL();
    double adjustedRightSpeed = PID_computeR();
    md.setSpeeds(adjustedLeftSpeed, -adjustedRightSpeed);
    //E2_counts2 = 0;
    md.setM1Speed(adjustedLeftSpeed);
    md.setM2Speed(-adjustedRightSpeed);


    // }
    
    //  delay(10);
}
void calculate_Distance()
{
    leftDistance = (5.27 * PI) * (E1_counts2 / 526.25);  //5.27 left wheel distance travelled (E2_count2 is the current ticks) cause 1 rev = 6cm distance travelled
    rightDistance = (5.27 * PI) * (E2_counts2 / 526.25); //5.27 right wheel distance travelled
    lrAvgDistance = (leftDistance + rightDistance) / 2;  //get the avg distance travelled as it is using 2 wheels
}
void rotateRight(float degree)
{
    calculate_Distance();
    double adjustedLeftSpeed = PID_computeL();
    double adjustedRightSpeed = PID_computeR();
    md.setSpeeds(adjustedLeftSpeed, adjustedRightSpeed);

    // for 270, 17.6, after 2 hour usage
    // for 270, 17.4 with full charge
      // for 6.352V = 17.1 as of now
      
    // for battery A: 6.444V [as of 18th Feb] => 15.2
    
//Target_RPM = 140: Battery A, 6.444V, rkp = 16.8;
//Target_RPM = 140: Battery B, 6.404~6.462V, rkp = 15.9;
    rotateDistance = ((16.8 * PI) / 360) * degree; //18.89distance travel for 1cm based on degree 18.9 best

    //lrAvgDistance = 0;

    
    if (lrAvgDistance >= rotateDistance) //if rotation distance exceeded, stop the motor
    {

        E1_counts2 = 0;
        E2_counts2 = 0;
        lrAvgDistance = 0;
        rotateDistance = 0;
        md.setBrakes(300, 300);
        input = 'e'; // to stop entering turn left
                     //   rotate_Right = false;
    }


}
void rotateLeft(float degree)
{

    
    calculate_Distance();
    double adjustedLeftSpeed = PID_computeL();
    double adjustedRightSpeed = PID_computeR();
    md.setSpeeds(-adjustedLeftSpeed, -adjustedRightSpeed);
    // for 6.38V = 17.25 as of now
    
// for battery A: 6.444V [as of 18th Feb] => 17.25
//Target_RPM = 140: Battery A, 6.444V, = 15.8 6.344V = 15.8;
//Target_RPM = 140: Battery B, 6.462V, = 16.8;
    rotateDistance = ((15.8  * PI) / 360) * degree; //

    
   // lrAvgDistance = 0;
    if (lrAvgDistance >= rotateDistance) //if rotation distance exceeded, stop the motor
    {

        E1_counts2 = 0;
        E2_counts2 = 0;
        lrAvgDistance = 0;
        rotateDistance = 0;
        md.setBrakes(400, 400);
        input = 'e'; // to stop entering turn left
        rotate_Left = false;
    }

   
}
void move_10cm()
{

   // calculate_Distance();
    double adjustedLeftSpeed = PID_computeL();
    double adjustedRightSpeed = PID_computeR();
    md.setSpeeds(adjustedLeftSpeed, -adjustedRightSpeed);

    //if rotation distance exceeded, moving 100,110,120,130,140,150 = -7 of the value
    // lrAvgDistance to be 10
    // lrAvgDistance 97
    if (lrAvgDistance >= 10)
    {
        
        E1_counts2 = 0;
        E2_counts2 = 0;
        lrAvgDistance = 0;
        rotateDistance = 0;
        md.setBrakes(400, 400);
        input = 'e'; // to stop entering turn left
    }

  
}
