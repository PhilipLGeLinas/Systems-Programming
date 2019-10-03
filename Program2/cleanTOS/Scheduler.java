import java.util.*;

public class Scheduler extends Thread {

    // Part 2: Step 1
    private Vector queue_0;
    private Vector queue_1;
    private Vector queue_2;

    // private Vector queue;
    private int timeSlice;

    private static final int Q0_TIME_SLICE = 500;
    private static final int Q1_TIME_SLICE = 1000;

    // New data added to p161 
    private boolean[] tids; // Indicate which ids have been used
    private static final int DEFAULT_MAX_THREADS = 10000;

    // A new feature added to p161 
    // Allocate an ID array, each element indicating if that id has been used
    private int nextId = 0;

    private void initTid(int maxThreads) {
        tids = new boolean[maxThreads];
        for (int i = 0; i < maxThreads; i++)
            tids[i] = false;
    }

    // A new feature added to p161 
    // Search an available thread ID and provide a new thread with this ID
    private int getNewTid() {
        for (int i = 0; i < tids.length; i++) {
            int tentative = (nextId + i) % tids.length;
            if (tids[tentative] == false) {
                tids[tentative] = true;
                nextId = (tentative + 1) % tids.length;
                return tentative;
            }
        }
        return -1;
    }

    // A new feature added to p161 
    // Return the thread ID and set the corresponding tids element to be unused
    private boolean returnTid(int tid) {
        if (tid >= 0 && tid < tids.length && tids[tid] == true) {
            tids[tid] = false;
            return true;
        }
        return false;
    }

    // A new feature added to p161 
    // Retrieve the current thread's TCB from the queue
    public TCB getMyTcb() {
        Thread myThread = Thread.currentThread(); // Get my thread object
        synchronized (queue_0) {
            for (int i = 0; i < queue_0.size(); i++) {
                TCB tcb = (TCB) queue_0.elementAt(i);
                Thread thread = tcb.getThread();
                if (thread == myThread) // if this is my TCB, return it
                    return tcb;
            }
        }
        return null;
    }

    // A new feature added to p161 
    // Return the maximal number of threads to be spawned in the system
    public int getMaxThreads() {
        return tids.length;
    }

    public Scheduler() {
        timeSlice = Q0_TIME_SLICE;
        Vector queue_0 = new Vector();
        Vector queue_1 = new Vector();
        Vector queue_2 = new Vector();
        initTid(DEFAULT_MAX_THREADS);
    }

    public Scheduler(int quantum) {
        timeSlice = quantum;
        Vector queue_0 = new Vector();
        Vector queue_1 = new Vector();
        Vector queue_2 = new Vector();
        initTid(DEFAULT_MAX_THREADS);
    }

    // A new feature added to p161 
    // A constructor to receive the max number of threads to be spawned
    public Scheduler(int quantum, int maxThreads) {
        timeSlice = quantum;
        Vector queue_0 = new Vector();
        Vector queue_1 = new Vector();
        Vector queue_2 = new Vector();
        initTid(maxThreads);
    }

    private void schedulerSleep() {
        try {
            Thread.sleep(timeSlice);
        } catch (InterruptedException e) {
        }
    }

    // A modified addThread of p161 example
    public TCB addThread(Thread t) {
        // t.setPriority(2);
        TCB parentTcb = getMyTcb(); // get my TCB and find my TID
        int pid = (parentTcb != null) ? parentTcb.getTid() : -1;
        int tid = getNewTid(); // get a new TID
        if (tid == -1)
            return null;
        TCB tcb = new TCB(t, tid, pid); // create a new TCB
        queue_0.add(tcb); // Part 2: Step 2
        return tcb;
    }

    // A new feature added to p161
    // Removing the TCB of a terminating thread
    public boolean deleteThread() {
        TCB tcb = getMyTcb();
        if (tcb != null)
            return tcb.setTerminated();
        else
            return false;
    }

    public void sleepThread(int milliseconds) {
        try {
            sleep(milliseconds);
        } catch (InterruptedException e) {
        }
    }

    // A modified run of p161
    public void run() {
        Thread current = null;
        // this.setPriority(6);

        while (true) {
            try {
                // get the next TCB and its thread
                if (queue_0.size() == 0)
                    continue;
                TCB currentTCB = (TCB) queue_0.firstElement();
                if (currentTCB.getTerminated() == true) {
                    queue_0.remove(currentTCB);
                    returnTid(currentTCB.getTid());
                    continue;
                }
                current = currentTCB.getThread();
                if (current != null) {
                    if (current.isAlive())
                        // current.setPriority(4);
                        current.resume();
                    else {
                        // Spawn must be controlled by Scheduler
                        // Scheduler must start a new thread
                        // current.setPriority(4);
                        current.start();
                    }
                }

                schedulerSleep();
                // System.out.println("* * * Context Switch * * * ");

                synchronized (queue_0) {
                    if (current != null && current.isAlive()) {
                        // current.setPriority(2);
                        current.suspend();
                    }
                    queue_0.remove(currentTCB); // rotate this TCB to the end
                    queue_0.add(currentTCB);
                }
            } catch (NullPointerException e3) {
            }
            ;
        }
    }
}
