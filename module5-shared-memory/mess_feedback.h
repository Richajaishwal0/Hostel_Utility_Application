#ifndef MESS_FEEDBACK_H
#define MESS_FEEDBACK_H

#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/sem.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#define SHM_KEY 9999
#define SEM_KEY 8888

typedef struct {
    int good_count;
    int average_count;
    int poor_count;
    int total_feedback;
} feedback_data_t;

void sem_wait(int semid);
void sem_signal(int semid);
int create_semaphore();

#endif