#include <stdbool.h>

#include "bluetoothServer.h"
#include "cameramodule.h"

void init() {
  initBTServer();
  initCameraModule();
}

void stop() {
  stopCameraModule();
  stopBTServer();
}

int main(int argc, char **argv) {
  init();
  bool loop = false;
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
