import serial
import time
import threading
import math

class ServoDriver(threading.Thread):
  def __init__(self,threadId, name, port, limits):
    threading.Thread.__init__(self)
    self.threadId = threadId
    self.name = name
    self.ser=serial.Serial(port=port,baudrate=45000)
    self.limits = limits
    self.loop = True
    self.command = [90,90] # angle and speed
    self.pos = [90,90] # angle for first and second servo

  def run(self):
    comm = [0,0]
    while self.loop:
      if comm[0] != self.command[0] or comm[1] != self.command[1]:
        comm = self.command
        n, angle, speed = self.__processComm__(comm)

        for i in range(n):
          self.__setPos__(i,angle[i],speed)

      time.sleep((100.0/6.0)/1000.0)

  def stop(self):
    self.loop = False

  def setCommand(self, comm):
    self.command = comm
    print(self.command)

  def __processComm__(self, comm):
    a = comm[0]
    s = comm[1]
    speed = s*1.27
    angles = [0,0]
    angles[0] = self.pos[0] - math.sin(math.radians(a))*(s/10)
    angles[1] = self.pos[1] - math.cos(math.radians(a))*(s/10)
    return 2, angles, speed

  def __setPos__(self, n, angle, speed = 127):
    if speed > 127:
      speed = 127
    elif speed < 0:
      speed = 0

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
    if angle > self.limits[n]["max"]:
      angle = self.limits[n]["max"]
      print ("WARNING: Angle range should be between {} and {}. ".format(self.limits[n]["min"],\
                                                                         self.limits[n]["max"]),
             "Setting angle to {} degrees.".format(self.limits[n]["max"]))
    elif angle < self.limits[n]["min"]:
      angle = self.limits[n]["min"]
      print ("WARNING: Angle range should be between {} and {}. ".format(self.limits[n]["min"],\
                                                                         self.limits[n]["max"]),
             "Setting angle to {} degrees.".format(self.limits[n]["min"]))

    self.pos[n] = angle
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

  servo = ServoDriver(1, "ServoDriver", port,limits)
  servo.start()
  angle = -180
  delta = 5
  speed = 90

  try:
    while True:
      if angle > 180:
        angle = -180

      angle = angle + delta
      servo.setCommand([angle, speed])
      time.sleep(0.4)
  except:
    servo.stop()
    servo.join()
