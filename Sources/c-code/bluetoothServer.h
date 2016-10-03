#include <stdio.h>
#include <unistd.h>
#include <sys/socket.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/rfcomm.h>
#include <stdlib.h>
#include <pthread.h>
#include <stdbool.h>

static struct sockaddr_rc loc_addr = { 0 };
static struct sockaddr_rc rem_addr = { 0 };
static int socketHandler;
static int clientHandler;
static socklen_t opt = sizeof(rem_addr);
static pthread_t acceptThread;
static pthread_t readerThread;
static pthread_t writerThread;
static bool reader_loop;
static bool writer_loop;

static void *acceptFunction(void *ptr);
static void *readerLoop(void *ptr);
static void *writerLoop(void *ptr);

static void (*_acceptCallback)();
static void (*_readerCallback)(char*,int);
static void (*_disconnectCallback)();

void initBTServer(void (*acceptCallback)(), void (*readerCallback)(char*,int), void (*disconnectCallback)());
void stopBTServer();
void btWrite(char *buf, int size);
