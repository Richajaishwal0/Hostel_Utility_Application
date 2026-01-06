#include "mess_feedback.h"
#include <string.h>

int main() {
    // Create shared memory
    int shmid = shmget(SHM_KEY, sizeof(feedback_data_t), IPC_CREAT | 0666);
    if (shmid == -1) {
        perror("shmget");
        exit(1);
    }

    feedback_data_t *feedback = (feedback_data_t*)shmat(shmid, NULL, 0);
    if (feedback == (void*)-1) {
        perror("shmat");
        exit(1);
    }

    int semid = create_semaphore();

    // Initialize feedback data
    sem_wait(semid);
    feedback->good_count = 0;
    feedback->average_count = 0;
    feedback->poor_count = 0;
    feedback->total_feedback = 0;
    sem_signal(semid);

    printf("Mess Feedback Server initialized.\n");
    printf("Shared memory and semaphore created.\n");
    printf("Feedback counters reset to zero.\n");

    // Keep server running
    while (1) {
        sleep(5);
        sem_wait(semid);
        printf("Current Feedback - Good: %d, Average: %d, Poor: %d, Total: %d\n",
               feedback->good_count, feedback->average_count, 
               feedback->poor_count, feedback->total_feedback);
        sem_signal(semid);
    }

    shmdt(feedback);
    return 0;
}