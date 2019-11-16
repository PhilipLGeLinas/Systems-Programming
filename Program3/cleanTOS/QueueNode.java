// Programmer: Philip L. GeLinas
// Created on: 10/22/2019

import java.util.*;

// The building block of a SyncQueue.
public class QueueNode {

    // A list of process ids.
    private Vector PIDVector = new Vector();

    // Generates a new queue node and clears the pid vector.
    public QueueNode() {
        this.PIDVector.clear();
    }

    // Wakes a thread given a thread id.
    public synchronized void wake(int TID) {
        this.PIDVector.add(new Integer(TID));
        this.notify();
    }

    // Sleep and wait.
    public synchronized int sleep() {
        if (this.PIDVector.size() == 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {

            }
        }

        // Return the first pid in the PIDVector.
        Integer PID = (Integer)this.PIDVector.remove(0);
        return PID;
    }
}