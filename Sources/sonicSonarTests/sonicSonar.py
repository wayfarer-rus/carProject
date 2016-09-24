import RPi.GPIO as GPIO
import time
import threading

class SonicSonar(threading.Thread):
    soundSpeed = 17150.0

    def __init__(self, threadId, name, trigPin, echoPin, settleTime = 0.1):
        threading.Thread.__init__(self)
        self.threadId = threadId
        self.name = name
        self.trigPin = trigPin
        self.echoPin = echoPin
        self.settleTime = settleTime
        self.lastDistance = None

    def run(self):
        print("Distance Measurement In Progress")
        self.loop = True
        GPIO.setup(self.trigPin,GPIO.OUT) 
        GPIO.setup(self.echoPin,GPIO.IN)
        GPIO.output(self.trigPin, False)
        self.sonarLoop()
        print("Distance Measurement ended")

    def stop(self):
        self.loop = False

    def getDistance(self):
        return self.lastDistance

    def sonarLoop(self):
        while self.loop:
            time.sleep(self.settleTime)
            GPIO.output(self.trigPin, True)
            time.sleep(0.00001)
            GPIO.output(self.trigPin, False)

            pulse_start = time.time()

            while GPIO.input(self.echoPin)==0:
                pulse_start = time.time()

            while GPIO.input(self.echoPin)==1:
                pulse_end = time.time()

            pulse_duration = pulse_end - pulse_start
            distance = pulse_duration * self.soundSpeed
            self.lastDistance = round(distance, 2)


if __name__ == "__main__":
    GPIO.setmode(GPIO.BCM)
    TRIG = 28
    ECHO = 30
    sonarThread = SonicSonar(1, "Front Sonar", TRIG, ECHO)

    try :
        sonarThread.start()

        while True:
            distance = sonarThread.getDistance()
            print ("Distance: ",distance,"cm")
            time.sleep(0.5)
    except:
        sonarThread.stop()
        sonarThread.join()
    finally:
        GPIO.cleanup()



