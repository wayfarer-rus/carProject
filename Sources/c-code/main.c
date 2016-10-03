#include <stdbool.h>

#include "bluetoothServer.h"
#include "cameramodule.h"

static bool loop;
static bool alive;

void btAcceptCallback();
void btReaderCallback(char* buf, int size);
void btDisconnectCallback();
void cameraBufferReadyCallback(char* buf, int size);

void init() {
  initBTServer(btAcceptCallback, btReaderCallback, btDisconnectCallback);
  initCameraModule(cameraBufferReadyCallback);
}

void stop() {
  stopCameraModule();
  stopBTServer();
}

int main(int argc, char **argv) {
  alive = true;

  while (alive) {
    init();
    loop = false;
    // main loop
    while (loop) {
      // read client data
      printf("reading data from client");
      // read data from sensors
      printf("reading data from sensors");
      // process client data
      printf("processing client data");

    }

    stop();
  }
}

void btAcceptCallback() {
  startCamera();
  // something else?
}

void btReaderCallback(char* buf, int size) {
  // TODO: parse and set command
}

void btDisconnectCallback() {
  loop = false;
  // something else??
}

void cameraBufferReadyCallback(char* buf, int size) {
  // TODO: copy to the other buffer
}
