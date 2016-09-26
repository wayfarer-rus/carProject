#include "bluetoothServer.h"

void initBTServer() {
  // allocate socket
  socketHandler = socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM);

  // bind socket to port 1 of the first available
  // local bluetooth adapter
  loc_addr.rc_family = AF_BLUETOOTH;
  loc_addr.rc_bdaddr = *BDADDR_ANY;
  loc_addr.rc_channel = (uint8_t) 1;
  bind(socketHandler, (struct sockaddr *)&loc_addr, sizeof(loc_addr));

  // put socket into listening mode
  listen(socketHandler, 1);
  int retval;
  retval = pthread_create(&acceptThread, NULL, acceptFunction, NULL);
  if(retval) {
    fprintf(stderr,"Error - pthread_create() return code: %d\n",iret1);
    exit(EXIT_FAILURE);
  }
}

void *acceptFunction(void *ptr) {
  char buf[1024] = { 0 };
  clientHandler = accept(socketHandler, (struct sockaddr *)&rem_addr, &opt);
  ba2str( &rem_addr.rc_bdaddr, buf );
  fprintf(stderr, "accepted connection from %s\n", buf);
  int retval;
  retval = pthread_create(&readerThread, NULL, readerLoop, NULL);
  if(retval) {
    fprintf(stderr,"Error - pthread_create() return code: %d\n",iret1);
    exit(EXIT_FAILURE);
  }

  retval = pthread_create(&writerThread, NULL, writerLoop, NULL);
  if(retval) {
    fprintf(stderr,"Error - pthread_create() return code: %d\n",iret1);
    exit(EXIT_FAILURE);
  }
}

void *readerLoop(void *ptr) {
  char buf[1024] = { 0 };
  memset(buf, 0, sizeof(buf));
  reader_loop = true;

  while (reader_loop) {
    // read data from the client
    bytes_read = read(clientHandler, buf, sizeof(buf));
    if( bytes_read > 0 ) {
        printf("received [%s]\n", buf);
    }
    // TODO: send data to main loop
  }

}

void *writerLoop(void *ptr) {
  char buf[1024] = { 0 };
  memset(buf, 0, sizeof(buf));
  writer_loop = true;
  bool ready = false;
  int status;

  while (writer_loop) {
    // TODO: prepare buffer
    if (ready) {
      status = write(clientHandler, buf, sizeof(buf));

      if (status < 0) {
        perror("Error while writint to bluetoth client");
        writer_loop = false;
        // TODO:: signal to the main-loop that client is not available
        break;
      }
    }
  }
}

void stopBTServer() {
  reader_loop = false;
  writer_loop = false;
  pthread_join( readerThread, NULL);
  pthread_join( writerThread, NULL);
  // close connection
  close(clientHandler);
  close(socketHandler);
}
