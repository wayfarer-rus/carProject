#include <stdlib.h>

typedef struct QueueElem
{
  void *value;
  QueueElem *next;
} QueueElem;

typedef struct Queue
{
  QueueElem *head;
  QueueElem *tail;
} Queue;


static void _pushToQueue(Queue *queue, QueueElem *elem);
static QueueElem * _takeFromQueue(Queue *queue);
static QueueElem * _initQueueElem();

Queue* initQueue();
void destroyQueue(Queue *queue);
void pushToQueue(Queue *queue, void * value);
void * takeFromQueue(Queue *queue);
