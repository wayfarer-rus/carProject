import io
import picamera
import threading
import time
import datetime
import bluetooth

class CameraStreamer(threading.Thread):
    def __init__(self, threadId, name, resolution = (320,240)):
        threading.Thread.__init__(self)
        self.threadId = threadId
        self.name = name
        self.resolution = resolution
        self.loop = True
        self.socketThread = SocketThread()
        self.socketThread.start()

    def run(self):
        camera = picamera.PiCamera()
        camera.rotation = 180
        camera.resolution = self.resolution
        stream = picamera.PiCameraCircularIO(camera, seconds=1, bitrate=100000)
        camera.start_recording(stream, format='h264')

        while self.loop:
            camera.wait_recording(1)
            buff = io.BytesIO()
            stream.copy_to(buff, seconds =1)
            self.socketThread.write(buff)
            buff.close()
            #stream.clear()
            buff = None

        camera.stop_recording()
        self.socketThread.stop()
        self.socketThread.join()

    def setSocket(self, clientSocket):
        self.socketThread.setSocket(clientSocket)

    def stop(self):
        self.loop = False

class SocketThread(threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)
        self.socket = None
        self.loop = True
        self.buff = None
        self.writeLock = threading.Lock()

    def run(self):
        while self.loop:
            if self.buff and self.socket:
                with self.writeLock:
                    size = self.buff.tell()
                    if size <= 0:
                        continue

                    headerMsg = "{\"size\":%d}" % (size)
                    ts = time.time()
                    st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S.%f')
                    print(st, " sending new video portion starting with ", headerMsg)
                    try:
                        #self.socket.send(headerMsg.encode('utf-8'))
                        self.socket.send(self.buff.getvalue())
                    except bluetooth.btcommon.BluetoothError:
                        self.socket = None
                        print("Error: connection pipe is broken")
                    self.buff.close()
                    self.buff = None

    def stop(self):
        self.loop = False

    def setSocket(self, socket):
        self.socket = socket

    def write(self, data):
        with self.writeLock:
            #if self.buff:
            #    self.buff.close()
            #    self.buff = None

            if not self.buff:
                self.buff = io.BytesIO()

            self.buff.write(data.getvalue())
