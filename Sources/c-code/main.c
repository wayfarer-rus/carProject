#include <stdbool.h>
#include <pthread.h>
#include <stdlib.h>
#include <string.h>

#include "bluetoothServer.h"
#include "cameramodule.h"
#include "queue.h"

static bool loop;
static bool alive;
static Queue * cameraBufQueue;
static pthread_mutex_t camera_queue_mutex;

void btAcceptCallback();
void btReaderCallback(char* buf, int size);
void btDisconnectCallback();
void cameraBufferReadyCallback(char* buf, int size);

static void init() {
  cameraBufQueue = initQueue();
  initBTServer(btAcceptCallback, btReaderCallback, btDisconnectCallback);
  initCameraModule(cameraBufferReadyCallback);
}

static void stop() {
  stopCameraModule();
  stopBTServer();
  destroyQueue(cameraBufQueue);
}

int main(int argc, char **argv) {
  alive = true;

  while (alive) {
    init();
    loop = true;
    // main loop
    while (loop) {
      // read client data
      printf("reading data from client");
      // read data from sensors
      printf("reading data from sensors");
      // process client data
      printf("processing client data");
      // send camera data
      pthread_mutex_lock(&camera_queue_mutex);
      char* cameraBuf = (char* )takeFromQueue(cameraBufQueue);
      pthrred_mutex_unlock(&camera_queue_mutex);

      if (cameraBuf != NULL && loop) {
        printf("send video stream to BT client");
        writeToBtClient(cameraBuf);
      }
    }

    stop();
  }
}

void writeToBtClient(char* msg) {
  if (msg == null)
    return;

  int n = sizeof(msg)/sizeof(char);
  btWrite(msg, n);
}

void btAcceptCallback() {
  startCamera();
  // something else?
}

void btReaderCallback(char* buf, int size) {
  // TODO: parse and set command
  printf("BT reder says: %s", buf);
}

void btDisconnectCallback() {
  loop = false;
  // something else??
}

void cameraBufferReadyCallback(char* buf, int size) {
  if (size == 0 || buf == NULL)
    return;

  char * cameraBuf = (char*) malloc(sizeof(char)*size);
  memcpy(cameraBuf, buf, sizeof(char)*size);

  pthread_mutex_lock(&camera_queue_mutex);
  pushToQueue(cameraBufQueue, (void*)cameraBuf);
  pthread_mutex_unlock(&camera_queue_mutex);
}
