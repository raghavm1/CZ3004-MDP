# CZ3004 MDP Arduino

## Components used

- Arduino Uno
- Arduino VNH5019 motor driver shield
- Sharp IR short range (GP2Y0A21YK) and long range (GP2Y0A02YK) proximity sensors
- Pololu low power 6V DC motors

## Fastest path

For running code for fastest path, compile and upload Fastest_Path.ino

## Exploration

For running code for exploration, compile and upload Exploration.ino

## Sensor calibration

No external libraries have been used for obtaining sensor values. Analog values are read using the serial monitor and are plotted on a graph. After that, a function which fits the curve is used to convert analog readings to centimeter distances.

For sensor calibration, refer to sensor_calibration and upload the code on the arduino uno board. The serial monitor output are the analogRead() values.
