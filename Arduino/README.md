# CZ3004 MDP Arduino

![robot-front](https://github.com/raghavm1/CZ3004-MDP/blob/main/Arduino/robot-front.jpg)

## Components used

- Arduino Uno
- Arduino VNH5019 motor driver shield
- Sharp IR short range (GP2Y0A21YK) and long range (GP2Y0A02YK) proximity sensors
- Pololu low power 6V DC motors

## Fastest path

For running code for fastest path, go to the Fastest_Path folder, compile and upload Fastest_Path.ino on the Arduino

## Exploration

For running code for exploration, go to the, compile and upload Exploration.ino on the Arduino

## Sensor calibration

No external libraries have been used for obtaining sensor values. Analog values are read using the serial monitor and are plotted on a graph. After that, a function which fits the curve is used to convert analog readings to centimeter distances.

For sensor calibration, refer to sensor_calibration and upload the code on the Arduino. The serial monitor output are the analogRead() values.
