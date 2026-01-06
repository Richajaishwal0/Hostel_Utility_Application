#include "mess_feedback.h"
#include <string.h>

int main(int argc, char *argv[]) {
    if (argc != 2) {
        printf("Usage: %s <feedback_type>\n", argv[0]);
        printf("feedback_type: good, average, poor\n");
        exit(1);
    }

    int shmid = shmget(SHM_KEY, sizeof(feedback_data_t), 0666);
    if (shmid == -1) {
        perror("shmget");
        exit(1);
    }

    feedback_data_t *feedback = (feedback_data_t*)shmat(shmid, NULL, 0);
    if (feedback == (void*)-1) {
        perror("shmat");
        exit(1);
    }

    int semid = semget(SEM_KEY, 1, 0666);
    if (semid == -1) {
        perror("semget");
        exit(1);
    }

    // Submit feedback
    sem_wait(semid);
    
    if (strcmp(argv[1], "good") == 0) {
        feedback->good_count++;
        printf("Good feedback submitted!\n");
    } else if (strcmp(argv[1], "average") == 0) {
        feedback->average_count++;
        printf("Average feedback submitted!\n");
    } else if (strcmp(argv[1], "poor") == 0) {
        feedback->poor_count++;
        printf("Poor feedback submitted!\n");
    } else {
        printf("Invalid feedback type!\n");
        sem_signal(semid);
        shmdt(feedback);
        exit(1);
    }
    
    feedback->total_feedback++;
    
    printf("Updated counts - Good: %d, Average: %d, Poor: %d, Total: %d\n",
           feedback->good_count, feedback->average_count, 
           feedback->poor_count, feedback->total_feedback);
    
    sem_signal(semid);
    shmdt(feedback);
    return 0;
}