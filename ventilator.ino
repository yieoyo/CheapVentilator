#include <Servo.h>
#include <SoftwareSerial.h>
#include<dht.h>
#include <EEPROM.h>

#define DHT11_PIN 10

dht DHT11;


int RESET_PIN = 12;
int ledState = 0; //0 = not resat, 1= resat

SoftwareSerial BT(2, 3); //tx,rx

Servo servo;
Servo air;

char servoPin = 5;
char btStatePin = 6; //bt state pin
char airPin = 9;

char a;

unsigned long previousMillisServo, previousMillisHumi;
unsigned long currentMillis;

enum { WAITING, MOVING} currentState = WAITING;

/* 5 second breathing cycle */
long currentIn = 1500;
long baseIn = 500;
long currentEx = 3000;
long baseEx = 500;
long changeRate = 100;

String outp =String("");

byte servoPos = 0;

bool showed = false;
long initialPos = 90;
long destPos = 30;
long forward = 1;

long totalLoad = 48000;
long currentLoad = 0;
long loadInc = 1000;
long minLoad = 2000;


long airStart = 500;
long airCurrent = 0;
long airEnd = 2300;
long perDegreeLoadChange = totalLoad/(airEnd-airStart);
long degreeForHundredLoad = (1000/perDegreeLoadChange) + 1;

bool hell = true;

float curHumi = 0.0;
float curTemp = 0.0;

float oxyPerHundredml =21.00; //oxygen in 
long oxyTotal = 0; //oxygen in 
float oxyRatio =21.00; //oxygen in 

long oxyChangeRate = 100;
long curOxyVol = 0;

void setup() {
   
   digitalWrite(LED_BUILTIN, ledState);
   digitalWrite(RESET_PIN, HIGH);
   pinMode(RESET_PIN,OUTPUT);
   pinMode(LED_BUILTIN, OUTPUT);
   checkLedState();
   
  BT.begin(9600);
  Serial.begin(19200);
  
  pinMode(btStatePin,INPUT);

  servo.attach(servoPin); 
   air.attach(airPin); 
   
  servo.write(initialPos);
  air.writeMicroseconds(airEnd);
  
  currentLoad=totalLoad;
  oxyTotal = (currentLoad * oxyPerHundredml)/100;
  oxyRatio = (oxyTotal * 100)/currentLoad;
  airCurrent = airEnd;
}

void loop() {
  if(hell){
    if(servo.read() <= initialPos || servo.read() >=initialPos){
  currentMillis = millis();
  if(digitalRead(btStatePin) == HIGH){
   if (currentMillis - previousMillisHumi >= 2000) //time to show humidity and temperature
      {
        int chk = DHT11.read11(DHT11_PIN);
        if(!isnan(DHT11.humidity) && curHumi != DHT11.humidity && !isnan(DHT11.temperature) && curTemp != DHT11.temperature && DHT11.temperature > 0 && DHT11.humidity >0){
          curHumi = DHT11.humidity;
           curTemp = DHT11.temperature;
           outp = String("H-");
       outp += String(curHumi);
       outp += String("-T-");
      outp += String(curTemp);
       outp += "#";
      BT.print(outp);
        } else if(!isnan(DHT11.humidity) && curHumi != DHT11.humidity && DHT11.humidity >0){
          curHumi = DHT11.humidity;
           outp = String("H-");
       outp += String(curHumi);
       outp += "#";
       BT.print(outp);
        } else if(!isnan(DHT11.temperature) && curTemp != DHT11.temperature && DHT11.temperature){
          curTemp = DHT11.temperature;
            outp = String("T-");
      outp += String(curTemp);
       outp += "#";
       BT.print(outp);
        }
        
        previousMillisHumi = currentMillis;
      }
  if(!showed){
 outp =  String("I-");
      outp += String(currentIn);
      outp += String("-E-");
       outp += String(currentEx);
       outp += String("-F-");
      outp += String((currentLoad*currentIn)/60000);
        outp += String("-O-");
       outp += String(oxyRatio);
       outp += String("-V-");
      outp += String(curOxyVol);
       outp += "#";
       Serial.println(outp);
      BT.print(outp);
  showed = true;
  }
} else {
  showed = false;
}
if (BT.available())
  // if text arrived in from BT serial...
  {

    
    a=(BT.read());
    if (a=='1')
    {
      digitalWrite(LED_BUILTIN, HIGH);
      currentIn += changeRate;
      outp = String("I-");
      outp += String(currentIn);
      outp += String("-F-");
      outp += String((currentLoad*currentIn)/60000);
       outp += "#";
      BT.print(outp);
    }
    if (a=='2')
    {
      digitalWrite(LED_BUILTIN, LOW);
      if(currentIn > baseIn) {
      currentIn -= changeRate;
      } 
      outp =  String("I-");
      outp += String(currentIn);
      outp += String("-F-");
      outp += String((currentLoad*currentIn)/60000);
       outp += "#";
      BT.print(outp);
    }
    if (a=='3')
    {
      digitalWrite(LED_BUILTIN, HIGH);
       currentEx += changeRate;
       outp = String("E-");
       outp += String(currentEx);
       outp += String("-F-");
      outp += String((currentLoad*currentIn)/60000);
       outp += "#";
      BT.print(outp);
    }
    if (a=='4')
    {
      digitalWrite(LED_BUILTIN, LOW);
       if(currentEx > baseEx) {
      currentEx -= changeRate;
      } 
      outp = String("E-");
      outp += String(currentEx);
      outp += String("-F-");
      outp += String((currentLoad*currentIn)/60000);
       outp += "#";
      BT.print(outp);
    }
    if (a=='5')
    {
      if(currentLoad +  loadInc <= totalLoad){
         airCurrent +=  degreeForHundredLoad;
         currentLoad += loadInc;
         air.writeMicroseconds(airCurrent);
      }
      outp = String("F-");
      outp += String((currentLoad*currentIn)/60000);
      outp += "#";
      BT.print(outp);
      
    }
    if (a=='6')
    {
      if(currentLoad -  loadInc >= minLoad){
         airCurrent -=  degreeForHundredLoad;
         currentLoad -= loadInc;
         air.writeMicroseconds(airCurrent);
         
      }
      outp = String("F-");
      outp += String((currentLoad*currentIn)/60000);
      outp += "#";
      BT.print(outp);
    }
    if (a == '7')
    {
      oxyTotal = (currentLoad * oxyPerHundredml)/100;
      currentLoad +=  oxyChangeRate;
      oxyTotal +=  oxyChangeRate;
     
      
      curOxyVol +=  oxyChangeRate;
      oxyRatio = float(oxyTotal * 100)/currentLoad;
      oxyPerHundredml = oxyRatio;
      outp = String("O-");
      outp += String(oxyRatio);
      outp += String("-F-");
      outp += String((currentLoad*currentIn)/60000);
      outp += String("-V-");
      outp += String(curOxyVol);
      outp += "#";
      Serial.println(outp);
      BT.print(outp);
    }
    if (a == '8')
    {
      oxyTotal = (currentLoad * oxyPerHundredml)/100;
      if(curOxyVol > 0){
        currentLoad -=  oxyChangeRate;
      
      oxyTotal -=  oxyChangeRate;
      curOxyVol -=  oxyChangeRate;
      oxyRatio = float(oxyTotal * 100)/currentLoad;
      oxyPerHundredml = oxyRatio;
      }
      
      outp = String("O-");
      outp += String(oxyRatio);
      outp += String("-F-");
      outp += String((currentLoad*currentIn)/60000);
      outp += String("-V-");
      outp += String(curOxyVol);
      outp += "#";
      Serial.println(outp);
      BT.print(outp);
    }
   
    // you can add more "if" statements with other characters to add more commands
  }
  switch (currentState)
  {
    case WAITING:
      
      
      if (servo.read() != initialPos || servo.read() !=destPos)
      {
        currentState = MOVING;
      }
      break;

    case MOVING:
      
      if (currentMillis - previousMillisServo >= currentEx && forward) //time to move the servo
      {
        moveservo();
        forward = !forward;
      } else if(currentMillis - previousMillisServo >=currentIn && !forward){
        moveservo();
        forward = ! forward;
      }
      if (servo.read() == initialPos || servo.read() ==destPos)
      {
        currentState = WAITING;
      }
      break;
  }//switch

} else {
  ledState=1;
  EEPROM.update(0, ledState);
  digitalWrite(LED_BUILTIN, ledState);
  digitalWrite(RESET_PIN, LOW);
  Serial.println("System Reset");
}

  }

}
void moveservo()
{
  //Serial.print("moving the servo to ");
  bool newMove=true; //otherwise it does all the moves all the time
  if (servoPos >= initialPos && newMove)
  {
    servoPos = destPos;
    newMove=false;
  }

  if (servoPos <= destPos && newMove)
  {
    servoPos = initialPos;
    newMove=false;
  }
  
   
  servo.write(servoPos);

  previousMillisServo = currentMillis;

}//moveservo
void checkLedState(){
  ledState = EEPROM.read(0);
  if(ledState == 1){
    digitalWrite(LED_BUILTIN, HIGH);
  }
  if(ledState == 0){
    digitalWrite(LED_BUILTIN, LOW);
  }
}
