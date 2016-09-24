#!/usr/bin/python -p
import RPi.GPIO as GPIO
import time
import threading
import os
import _thread
from sonicSonar import SonicSonar
from bluetoothCarServer import BluetoothCarServer
from servoDriver import ServoDriver
from cameraDriver import CameraStreamer

try:
    from msvcrt import getch  # try to import Windows version
except ImportError:
    def getch():   # define non-Windows version
        """Waits for a single keypress on stdin.

        This is a silly function to call if you need to do it a lot because it has
        to store stdin's current setup, setup stdin for reading single keystrokes
        then read the single keystroke then revert stdin back after reading the
        keystroke.

        Returns the character of the key that was pressed (zero on
        KeyboardInterrupt which can happen when a signal gets handled)

        """
        import termios, fcntl, sys, os
        fd = sys.stdin.fileno()
        # save old state
        flags_save = fcntl.fcntl(fd, fcntl.F_GETFL)
        attrs_save = termios.tcgetattr(fd)
        # make raw - the way to do this comes from the termios(3) man page.
        attrs = list(attrs_save) # copy the stored version to update
        # iflag
        attrs[0] &= ~(termios.IGNBRK | termios.BRKINT | termios.PARMRK
                      | termios.ISTRIP | termios.INLCR | termios. IGNCR
                      | termios.ICRNL | termios.IXON )
        # oflag
        attrs[1] &= ~termios.OPOST
        # cflag
        attrs[2] &= ~(termios.CSIZE | termios. PARENB)
        attrs[2] |= termios.CS8
        # lflag
        attrs[3] &= ~(termios.ECHONL | termios.ECHO | termios.ICANON
                      | termios.ISIG | termios.IEXTEN)
        termios.tcsetattr(fd, termios.TCSANOW, attrs)
        # turn off non-blocking
        fcntl.fcntl(fd, fcntl.F_SETFL, flags_save & ~os.O_NONBLOCK)
        # read a single keystroke
        try:
            ret = sys.stdin.read(1) # returns a single character
        except KeyboardInterrupt:
            ret = 0
        finally:
            # restore old state
            termios.tcsetattr(fd, termios.TCSAFLUSH, attrs_save)
            fcntl.fcntl(fd, fcntl.F_SETFL, flags_save)
        return ret

char = None

def keypress():
    global char
    char = getch()


class PWM:
    def __init__( self, pin ):
        self.pin = pin
        self.value = 0.

    def set( self, value ):
        if (value < 0):
            self.value = 0.
        elif (value > 1.0):
            self.value = 1.
        else:
            self.value = value

        cmd = 'echo "%d=%.2f" > /dev/pi-blaster' % ( self.pin, self.value )
        os.system(cmd)

    def get(self):
        return self.value

class CarDriver(threading.Thread):

    def __init__(self, threadId, name, controlPinsMap, sleepTime = 1.0/60.0):
        threading.Thread.__init__(self)
        self.threadId = threadId
        self.name = name
        self.controlPinsMap = controlPinsMap
        self.sleepTime = sleepTime
        self.loop = True
        self.rfw = PWM(self.controlPinsMap["rightForward"])
        self.lfw = PWM(self.controlPinsMap["leftForward"])
        self.rbw = PWM(self.controlPinsMap["rightBackward"])
        self.lbw = PWM(self.controlPinsMap["leftBackward"])
        self.speed = 1.
        self.command = [0, 0]

    def run(self):
        print("Init Sonars...")
        # init front sonar
        trig = self.controlPinsMap["frontSonar"]["trig"]
        echo = self.controlPinsMap["frontSonar"]["echo"]
        sonarThread = SonicSonar(self.threadId*100,"Front Sonar", trig, echo, 0.1)
        sonarThread.start()
        print("Sonars ready!")
        print("Init car wheels driver...")
        cmd = 'ps -C pi-blaster -o pid | xargs kill'
        print(cmd);
        os.system(cmd)
        # init car wheels driver
        a = [self.controlPinsMap["rightForward"],\
             self.controlPinsMap["leftForward"],\
             self.controlPinsMap["rightBackward"],\
             self.controlPinsMap["leftBackward"]]
        cmd = '/home/alarm/pi-blaster --gpio %s' % (','.join(map(str,a)))
        print(cmd);
        os.system(cmd)
        self.stopCar()
        print("Car wheels driver ready!")
        print("We are ready for input. Bring it on!")
        #_thread.start_new_thread(keypress, ())

        while self.loop:
            distance = sonarThread.getDistance()
            #print ("Front Sonar Distance: ",distance,"cm")
            self.readControllerKeys()

            if (distance > 10):
                self.drive(self.command)
            else:
                self.stopCar()

            time.sleep(self.sleepTime)

        print("Stop car")
        self.stopCar()
        cmd = 'ps -C pi-blaster -o pid | xargs kill'
        os.system(cmd)
        print("Stop sonars")
        sonarThread.stop()
        sonarThread.join()

    def stop(self):
        self.loop = False

    def stopCar(self):
        self.rfw.set(0.)
        self.rbw.set(0.)
        self.lfw.set(0.)
        self.lbw.set(0.)

    def readControllerKeys(self):
        global char
        if char is not None:
            print ("Key pressed is " , char)
            if char == "q":
                self.command[0] = 1
            elif char == "w":
                self.command[1] = 1
            elif char == "a":
                self.command[0] = -1
            elif char == "s":
                self.command[1] = -1
            elif char == "x":
                self.command[0] = self.command[1] = 0
            elif char == "-":
                self.speed = self.speed - 0.1
            elif char == "=":
                self.speed = self.speed + 0.1
            elif char == "t":
                self.loop = False
                char = None
                return

            _thread.start_new_thread(keypress, ())
            char = None

    def drive(self, command):
        if (command[0] > 0):
            self.lbw.set(0)
            self.lfw.set(command[0]*self.speed)
        elif (command[0] < 0):
            self.lfw.set(0)
            self.lbw.set((-command[0])*self.speed)
        else:
            self.lbw.set(0)
            self.lfw.set(0)

        if (command[1] > 0):
            self.rbw.set(0)
            self.rfw.set(command[1]*self.speed)
        elif (command[1] < 0):
            self.rfw.set(0)
            self.rbw.set((-command[1])*self.speed)
        else:
            self.rbw.set(0)
            self.rfw.set(0)

    def setCommand(self, command):
        self.command = command

    def getCommand(self):
        return self.command

if __name__ == "__main__":
    GPIO.setmode(GPIO.BCM)
    controlMap = {}
    controlMap["frontSonar"]={}
    controlMap["frontSonar"]["trig"] = 28
    controlMap["frontSonar"]["echo"] = 30
    controlMap["rightForward"]       = 17
    controlMap["rightBackward"]      = 18
    controlMap["leftForward"]        = 22
    controlMap["leftBackward"]       = 27

    limits = {}
    limits[0] = {}
    limits[0]["min"] = 30
    limits[0]["max"] = 120
    limits[1] = {}
    limits[1]["min"] = 30
    limits[1]["max"] = 120
    port = '/dev/ttyAMA0'

    carThread = CarDriver(1, "Car Driver", controlMap)
    servoTurretThread = ServoDriver(2, "ServoTurret Driver", port, limits)
    # reset servo driver
    resetPin = 4
    GPIO.setup(resetPin,GPIO.OUT)
    GPIO.output(resetPin, False)
    time.sleep(1/1000)
    GPIO.output(resetPin, True)
    time.sleep(5/1000)
    # init camera streamer
    streamer = CameraStreamer(4, "Pi Camera Streamer")
    # finally init bluetooth server
    btServer = BluetoothCarServer(3, "BT Car Server", carThread, servoTurretThread, streamer)

    try:
        # start all threads
        streamer.start()
        servoTurretThread.start()
        carThread.start()
        btServer.start()
        # join main to all threads
        streamer.join()
        carThread.join()
        servoTurretThread.join()
        btServer.join()
    except:
        btServer.stop()
        carThread.stop()
        servoTurretThread.stop()
        streamer.stop()
    finally:
        GPIO.cleanup()
#        os.system('shutdown -h now')
