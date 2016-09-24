import bluetooth
import threading
import json

class BluetoothCarServer(threading.Thread):

    def __init__(self, threadId, name, carDriver = None, servoDriver = None, cameraDriver = None):
        threading.Thread.__init__(self)
        self.threadId = threadId
        self.name = name
        self.port = 1
        self.loop = True
        self.carDriver = carDriver
        self.servoDriver = servoDriver
        self.cameraDriver = cameraDriver

    def run(self):
        print("Init server socket")
        server_sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
        server_sock.bind(("",self.port))
        print("waiting for connection")
        server_sock.listen(1)

        while self.loop:
            client_sock,address = server_sock.accept()
            print ("Accepted connection from ",address)
            # get video data and send to client
            if self.cameraDriver:
                self.cameraDriver.setSocket(client_sock)

            try:
                while self.loop:
                    # init servo-turret command
                    servoTurretCommand = [0,0]
                    # init wheels command array [left, right]
                    if self.carDriver:
                        carWheelsCommand = self.carDriver.getCommand()
                    else:
                        carWheelsCommand = [0,0]

                    # lock and read data from client
                    data = client_sock.recv(1024)
                    print("received [%s]" % data)
                    dataStr = data.decode("utf-8")
                    commands = [x for x in dataStr.split("#") if x != None and len(x)>0]

                    for s in commands:
                        try:
                            d = json.loads(s)
                        except json.decoder.JSONDecodeError:
                            print("Error: can't decode string ",s)
                            d = None

                        if d != None:
                            if 'rightWheels' in d:
                                # set command for right pair of wheels
                                carWheelsCommand[1] = d['rightWheels']/100.0
                            elif 'leftWheels' in d:
                                # set command for left pair of wheels
                                carWheelsCommand[0] = d['leftWheels']/100.0
                            elif 'joystick' in d:
                                #set command for servo-turret
                                servoTurretCommand = [d['joystick']['angle'], d['joystick']['power']];
                            elif 'terminate' in d:
                                self.loop = False
                                if self.cameraDriver:
                                    self.cameraDriver.stop()
                                if self.servoDriver:
                                    self.servoDriver.stop()
                                if self.carDriver:
                                    self.carDriver.stop()
                                continue;

                    if self.carDriver:
                        print("set command to the carDriver")
                        self.carDriver.setCommand(carWheelsCommand)

                    if self.servoDriver:
                        print("set servo-turret command to the servoDriver")
                        self.servoDriver.setCommand(servoTurretCommand)
            except IOError:
                if self.carDriver:
                    self.carDriver.stopCar();

                print ("Connection was dropped. Reaccept.")

        print("Server stopped. Closing connections")
        client_sock.close()
        server_sock.close()

    def stop(self):
        self.loop = False

if __name__ == "__main__":
    cs = BluetoothCarServer(1, "BT Car Server")

    try:
        cs.start()
        cs.join()
    except:
        cs.stop()
