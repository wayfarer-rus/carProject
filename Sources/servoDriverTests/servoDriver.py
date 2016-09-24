import serial
import sys
import time

class ServoMotor:
  def __init__(self,port,limits):
    self.ser=serial.Serial(port=port,baudrate=45000)
    self.limits = limits

  def setPos(self, n, angle, speed = 127):
    if speed > 127 or speed < 0:
      speed = 1
      print ("WARNING: Speed should be between 0 and 127. Speed was set to 1")

    print ("Setting servo "+str(n)+" speed to "+str(speed)+" out of 127.")
    speed = int(speed)
    # set speed command
    speedCmd = bytearray("     ", 'ascii')
    speedCmd[0]=0x80;
    speedCmd[1]=0x01;
    speedCmd[2]=0x01;
    speedCmd[3]=n;
    speedCmd[4]=speed;

    #Check that things are in range
    if angle > self.limits[n]["max"] or angle < self.limits[n]["min"]:
      angle=90
      print ("WARNING: Angle range should be between {} and {}. Setting angle to 90 degrees to be safe...".format(self.limits[n]["min"], self.limits[n]["max"]))

    print ("moving servo "+str(n)+" to "+str(angle)+" degrees.")
    #Valid range is 500-5500
    offyougo=int(5000*angle/180)+500
    #Get the lowest 7 bits
    byteone=offyougo&127
    #Get the highest 7 bits
    bytetwo=int((offyougo-(offyougo&127))/128)
    # set pos command
    posCmd = bytearray("      ", 'ascii')
    posCmd[0]=0x80;
    posCmd[1]=0x01;
    posCmd[2]=0x04;
    posCmd[3]=n;
    posCmd[4]=bytetwo;
    posCmd[5]=byteone;
    # send commands
    self.ser.write(speedCmd+posCmd)

if __name__ == "__main__":
  limits = {}
  limits[0] = {}
  limits[0]["min"] = 30
  limits[0]["max"] = 120
  limits[1] = {}
  limits[1]["min"] = 30
  limits[1]["max"] = 120
  port = '/dev/ttyAMA0'

  servo = ServoMotor(port,limits)
  angle = 90
  delta = 5
  while True:
    for i in range(2):
      if angle < limits[i]["min"]:
        delta = -delta;
      elif angle > limits[i]["max"]:
        delta = -delta

      angle = angle + delta
      servo.setPos(i,angle,100)
      time.sleep(0.2)
