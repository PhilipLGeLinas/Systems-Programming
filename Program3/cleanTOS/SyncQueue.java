// Programmer: Philip L. GeLinas
// Created on: 10/22/2019

import java.util.*;

// Used to maintain an array of QueueNode objects representing various conditions.
public class SyncQueue {

    // The default size for a SyncQueue.
    private final int DEFAULT_SIZE = 10;

    // Maintains an array of QueueNode objects, each representing a different
    // condition and enqueuing all threads that wait for this condition.
    private QueueNode[] queue;

    // Initializes the queue array and populates it with QueueNodes.
    private void initQueue(int size) {
        this.queue = new QueueNode[size];
        for (int i = 0; i < size; i++) {
            this.queue[i] = new QueueNode();
        }
    }

    // Generates a new array of QueueNodes of default size.
    public SyncQueue() {
        this.initQueue(DEFAULT_SIZE);
    }

    // Generates a new array of QueueNodes of the given size.
    public SyncQueue(int condMax) {
        this.initQueue(condMax);
    }

    // Enqueues the calling thread into the queue and sleeps it until a given condition is satisfied.
    // It returns the ID of a child thread that has woken the calling thread.
    public int enqueueAndSleep(int condition) {
        if (this.queue.length <= condition || condition < 0) {
            return -1;
        }

        // Returns the PID.
        return this.queue[condition].sleep();
    }

    // Dequeues and wakes up a thread waiting for a given condition. If there are two or more threads waiting for the
    // same condition, only one thread is dequeued and resumed. The FCFS (first-comefirst-service) order does not matter.
    // This function can receive the calling thread's ID, (tid) as the 2nd argument. This tid will be passed to the
    // thread that has been woken up from enqueueAndSleep. If no 2nd argument is given, you may regard tid as 0.
    public void dequeueAndWakeup(int condition) {
        this.dequeueAndWakeup(condition, 0);
    }

    // Dequeues and wakes up a thread waiting for a given condition. If there are two or more threads waiting for the
    // same condition, only one thread is dequeued and resumed. The FCFS (first-comefirst-service) order does not matter.
    // This function can receive the calling thread's ID, (tid) as the 2nd argument. This tid will be passed to the
    // thread that has been woken up from enqueueAndSleep. If no 2nd argument is given, you may regard tid as 0.
    public void dequeueAndWakeup(int condition, int tid) {
        if (condition < this.queue.length && condition > -1) {
            this.queue[condition].wake(tid);
        }
    }
}