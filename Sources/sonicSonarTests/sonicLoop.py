import RPi.GPIO as GPIO
import time
GPIO.setmode(GPIO.BCM)

TRIG = 28
ECHO = 30

print ("Distance Measurement In Progress")

GPIO.setup(TRIG,GPIO.OUT)
GPIO.setup(ECHO,GPIO.IN)

GPIO.output(TRIG, False)
print ("Waiting For Sensor To Settle")

while (True):
	time.sleep(0.1)

	GPIO.output(TRIG, True)
	time.sleep(0.00001)
	GPIO.output(TRIG, False)

GPIO.cleanup()
