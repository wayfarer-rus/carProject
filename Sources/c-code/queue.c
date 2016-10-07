#include "queue.h"

Queue* initQueue() {
  Queue* queue = (Queue*) malloc(sizeof(Queue));
  queue->head = NULL;
  queue->tail = NULL;
  return queue;
}

void destroyQueue(Queue *queue) {
  if (queue == NULL)
    return;

  QueueElem * cur = queue-head;

  while (cur != NULL) {
    QueueElem *tmp = cur;
    cur = cur->next;

    if (tmp->value != NULL)
      free(tmp->value);

    free(tmp);
  }

  free(queue);
  queue = NULL;
}

void pushToQueue(Queue *queue, void * value) {
  if (queue == NULL)
    return;

  QueueElem * newElem = _initQueueElem();
  newElem->value = value;
  _pushToQueue(queue, newElem);
}

void * takeFromQueue(Queue *queue) {
  if (queue == NULL)
    return NULL;

  QueueElem * elem = _takeFromQueue(queue);

  if (elem == NULL)
    return NULL;

  void * res = elem->value;
  free(elem);
  return res;
}

QueueElem * _initQueueElem() {
  QueueElem * elem = (QueueElem*)malloc(sizeof(QueueElem));
  elem->value = NULL;
  elem->next = NULL;
  return elem;
}

void _pushToQueue(Queue *queue, QueueElem *elem) {
  if (queue == NULL || elem == NULL)
    return;

  QueueElem *head = queue->head; // will return NULL, if queue is empty.
  elem->next = head;
  queue->head = elem;

  if (queue->tail == NULL) // no elements in the queue. set tail to the same value as head.
    queue->tail = elem;
}

QueueElem * _takeFromQueue(Queue *queue) {
  if (queue == NULL || queue->tail == NULL)
    return NULL;

  QueueElem * tail = queue->tail;
  QueueElem * cur = queue->head;

  if (cur == tail) { // if head equal to tail (one element in the queue)
    queue->head = NULL;
    queue->tail = NULL;
    return tail;
  }

  while (cur->next != tail) {
    cur = cur->next;
  }

  cur->next = NULL;
  queue->tail = cur;
  return tail;
}
