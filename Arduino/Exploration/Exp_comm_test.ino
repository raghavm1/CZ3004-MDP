void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);

  //[NEW]
  Serial.setTimeout(0);
}

void loop() {
  // put your main code here, to run repeatedly:
  while (1) {
    if (Serial.available()) {
      String s = Serial.readString();
      if (s != "asd") {
       delay(500);
      Serial.println("84.0|84.0|84.0|3.0|3.0|84.0|1"); 
      s = "asd";
    }
  }
  }
}
