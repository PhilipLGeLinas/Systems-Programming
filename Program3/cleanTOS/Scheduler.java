//// Programmer: Philip L. GeLinas
//// Created on: 10.05.2019
//
//import java.util.*;
//
//// Multi-level Feedback Queue Scheduler
//public class Scheduler extends Thread {
//
//    // Part 2: Step 1
//    private Vector queue_0;
//    private Vector queue_1;
//    private Vector queue_2;
//
//    private int queue_1_counter;
//    private int queue_2_counter;
//    private boolean restart;
//
//    // private Vector queue;
//    private int timeSlice;
//
//    private static final int Q0_TIME_SLICE = 500;
//
//    // New data added to p161
//    private boolean[] tids; // Indicate which ids have been used
//    private static final int DEFAULT_MAX_THREADS = 10000;
//
//    // A new feature added to p161
//    // Allocate an ID array, each element indicating if that id has been used
//    private int nextId = 0;
//
//    private void initTid(int maxThreads) {
//        tids = new boolean[maxThreads];
//        for (int i = 0; i < maxThreads; i++)
//            tids[i] = false;
//    }
//
//    // A new feature added to p161
//    // Search an available thread ID and provide a new thread with this ID
//    private int getNewTid() {
//        for (int i = 0; i < tids.length; i++) {
//            int tentative = (nextId + i) % tids.length;
//            if (tids[tentative] == false) {
//                tids[tentative] = true;
//                nextId = (tentative + 1) % tids.length;
//                return tentative;
//            }
//        }
//        return -1;
//    }
//
//    // A new feature added to p161
//    // Return the thread ID and set the corresponding tids element to be unused
//    private boolean returnTid(int tid) {
//        if (tid >= 0 && tid < tids.length && tids[tid] == true) {
//            tids[tid] = false;
//            return true;
//        }
//        return false;
//    }
//
//    // A new feature added to p161
//    // Retrieve the current thread's TCB from the queue
//    public TCB getMyTcb() {
//        Thread myThread = Thread.currentThread(); // Get my thread object
//        synchronized (queue_0) {
//            for (int i = 0; i < queue_0.size(); i++) {
//                TCB tcb = (TCB) queue_0.elementAt(i);
//                Thread thread = tcb.getThread();
//                if (thread == myThread) // if this is my TCB, return it
//                    return tcb;
//            }
//        }
//        synchronized (queue_1) {
//            for (int i = 0; i < queue_1.size(); i++) {
//                TCB tcb = (TCB) queue_1.elementAt(i);
//                Thread thread = tcb.getThread();
//                if (thread == myThread) // if this is my TCB, return it
//                    return tcb;
//            }
//        }
//        synchronized (queue_2) {
//            for (int i = 0; i < queue_2.size(); i++) {
//                TCB tcb = (TCB) queue_2.elementAt(i);
//                Thread thread = tcb.getThread();
//                if (thread == myThread) // if this is my TCB, return it
//                    return tcb;
//            }
//        }
//        return null;
//    }
//
//    // A new feature added to p161
//    // Return the maximal number of threads to be spawned in the system
//    public int getMaxThreads() {
//        return tids.length;
//    }
//
//    public Scheduler() {
//        timeSlice = Q0_TIME_SLICE;
//        queue_0 = new Vector();
//        queue_1 = new Vector();
//        queue_2 = new Vector();
//        queue_1_counter = 0;
//        queue_2_counter = 0;
//        restart = false;
//        initTid(DEFAULT_MAX_THREADS);
//    }
//
//    public Scheduler(int quantum) {
//        timeSlice = quantum;
//        queue_0 = new Vector();
//        queue_1 = new Vector();
//        queue_2 = new Vector();
//        queue_1_counter = 0;
//        queue_2_counter = 0;
//        restart = false;
//        initTid(DEFAULT_MAX_THREADS);
//    }
//
//    // A new feature added to p161
//    // A constructor to receive the max number of threads to be spawned
//    public Scheduler(int quantum, int maxThreads) {
//        timeSlice = quantum;
//        queue_0 = new Vector();
//        queue_1 = new Vector();
//        queue_2 = new Vector();
//        queue_1_counter = 0;
//        queue_2_counter = 0;
//        restart = false;
//        initTid(maxThreads);
//    }
//
//    private void schedulerSleep() {
//        try {
//            Thread.sleep(timeSlice);
//        } catch (InterruptedException e) {
//        }
//    }
//
//    // A modified addThread of p161 example
//    public TCB addThread(Thread t) {
//        // t.setPriority(2);
//        TCB parentTcb = getMyTcb(); // get my TCB and find my TID
//        int pid = (parentTcb != null) ? parentTcb.getTid() : -1;
//        int tid = getNewTid(); // get a new TID
//        if (tid == -1)
//            return null;
//        TCB tcb = new TCB(t, tid, pid); // create a new TCB
//        queue_0.add(tcb); // Part 2: Step 2
//        return tcb;
//    }
//
//    // A new feature added to p161
//    // Removing the TCB of a terminating thread
//    public boolean deleteThread() {
//        TCB tcb = getMyTcb();
//        if (tcb != null)
//            return tcb.setTerminated();
//        else
//            return false;
//    }
//
//    public void sleepThread(int milliseconds) {
//        try {
//            sleep(milliseconds);
//        } catch (InterruptedException e) {
//        }
//    }
//
//    // A modified run of p161
//    public void run() {
//        Thread current = null;
//        // this.setPriority(6);
//
//        while (true) {
//            try {
//                // get the next TCB and its thread
//                if (queue_0.size() > 0) {
//                    TCB currentTCB = (TCB) queue_0.firstElement();
//                    if (currentTCB.getTerminated() == true) {
//                        queue_0.remove(currentTCB);
//                        returnTid(currentTCB.getTid());
//                        continue;
//                    }
//                    current = currentTCB.getThread();
//                    if (current != null) {
//                        if (current.isAlive())
//                            // current.setPriority(4);
//                            current.resume();
//                        else {
//                            // Spawn must be controlled by Scheduler
//                            // Scheduler must start a new thread
//                            // current.setPriority(4);
//                            current.start();
//                        }
//                    }
//
//                    schedulerSleep();
//
//                    synchronized (queue_0) {
//                        if (current != null && current.isAlive()) {
//                            // current.setPriority(2);
//                            current.suspend();
//                        }
//                        queue_0.remove(currentTCB); // rotate this TCB to the end
//                        queue_1.add(currentTCB);
//                    }
//                } else if (queue_1.size() > 0) {
//                    TCB currentTCB = (TCB) queue_1.firstElement();
//                    if (currentTCB.getTerminated() == true) {
//                        queue_1.remove(currentTCB);
//                        returnTid(currentTCB.getTid());
//                        continue;
//                    }
//                    current = currentTCB.getThread();
//                    if (current != null) {
//                        if (current.isAlive())
//                            // current.setPriority(4);
//                            current.resume();
//                        else {
//                            // Spawn must be controlled by Scheduler
//                            // Scheduler must start a new thread
//                            // current.setPriority(4);
//                            current.start();
//                        }
//                    }
//
//                    while (queue_1_counter < 2) {
//                        schedulerSleep();
//                        queue_1_counter++;
//                        if (queue_0.size() > 0) {
//                            restart = true;
//                            break;
//                        }
//                    }
//                    if (restart) {
//                        restart = false;
//                        continue;
//                    }
//                    queue_1_counter = 0;
//
//                    synchronized (queue_1) {
//                        if (current != null && current.isAlive()) {
//                            // current.setPriority(2);
//                            current.suspend();
//                        }
//                        queue_1.remove(currentTCB); // rotate this TCB to the end
//                        queue_2.add(currentTCB);
//                    }
//                } else if (queue_2.size() > 0) {
//                    TCB currentTCB = (TCB) queue_2.firstElement();
//                    if (currentTCB.getTerminated() == true) {
//                        queue_2.remove(currentTCB);
//                        returnTid(currentTCB.getTid());
//                        continue;
//                    }
//                    current = currentTCB.getThread();
//                    if (current != null) {
//                        if (current.isAlive())
//                            // current.setPriority(4);
//                            current.resume();
//                        else {
//                            // Spawn must be controlled by Scheduler
//                            // Scheduler must start a new thread
//                            // current.setPriority(4);
//                            current.start();
//                        }
//                    }
//
//                    while (queue_2_counter < 4) {
//                        schedulerSleep();
//                        queue_2_counter++;
//                        if (queue_0.size() > 0 || queue_1.size() > 0) {
//                            restart = true;
//                            break;
//                        }
//                    }
//                    if (restart) {
//                        restart = false;
//                        continue;
//                    }
//                    queue_2_counter = 0;
//
//                    synchronized (queue_2) {
//                        if (current != null && current.isAlive()) {
//                            // current.setPriority(2);
//                            current.suspend();
//                        }
//                        queue_2.remove(currentTCB); // rotate this TCB to the end
//                        queue_2.add(currentTCB);
//                    }
//                }
//            } catch (NullPointerException e3) {
//            }
//            ;
//        }
//    }
//}

import java.util.*;

// Round Robin Scheduler
public class Scheduler extends Thread
{
    private Vector queue;
    private int timeSlice;
    private static final int DEFAULT_TIME_SLICE = 1000;

    // New data added to p161
    private boolean[] tids; // Indicate which ids have been used
    private static final int DEFAULT_MAX_THREADS = 10000;

    // A new feature added to p161
    // Allocate an ID array, each element indicating if that id has been used
    private int nextId = 0;
    private void initTid( int maxThreads ) {
        tids = new boolean[maxThreads];
        for ( int i = 0; i < maxThreads; i++ )
            tids[i] = false;
    }

    // A new feature added to p161
    // Search an available thread ID and provide a new thread with this ID
    private int getNewTid( ) {
        for ( int i = 0; i < tids.length; i++ ) {
            int tentative = ( nextId + i ) % tids.length;
            if ( tids[tentative] == false ) {
                tids[tentative] = true;
                nextId = ( tentative + 1 ) % tids.length;
                return tentative;
            }
        }
        return -1;
    }

    // A new feature added to p161
    // Return the thread ID and set the corresponding tids element to be unused
    private boolean returnTid( int tid ) {
        if ( tid >= 0 && tid < tids.length && tids[tid] == true ) {
            tids[tid] = false;
            return true;
        }
        return false;
    }

    // A new feature added to p161
    // Retrieve the current thread's TCB from the queue
    public TCB getMyTcb( ) {
        Thread myThread = Thread.currentThread( ); // Get my thread object
        synchronized( queue ) {
            for ( int i = 0; i < queue.size( ); i++ ) {
                TCB tcb = ( TCB )queue.elementAt( i );
                Thread thread = tcb.getThread( );
                if ( thread == myThread ) // if this is my TCB, return it
                    return tcb;
            }
        }
        return null;
    }

    // A new feature added to p161
    // Return the maximal number of threads to be spawned in the system
    public int getMaxThreads( ) {
        return tids.length;
    }

    public Scheduler( ) {
        timeSlice = DEFAULT_TIME_SLICE;
        queue = new Vector( );
        initTid( DEFAULT_MAX_THREADS );
    }

    public Scheduler( int quantum ) {
        timeSlice = quantum;
        queue = new Vector( );
        initTid( DEFAULT_MAX_THREADS );
    }

    // A new feature added to p161
    // A constructor to receive the max number of threads to be spawned
    public Scheduler( int quantum, int maxThreads ) {
        timeSlice = quantum;
        queue = new Vector( );
        initTid( maxThreads );
    }

    private void schedulerSleep( ) {
        try {
            Thread.sleep( timeSlice );
        } catch ( InterruptedException e ) {
        }
    }

    // A modified addThread of p161 example
    public TCB addThread( Thread t ) {
        // t.setPriority( 2 );
        TCB parentTcb = getMyTcb( ); // get my TCB and find my TID
        int pid = ( parentTcb != null ) ? parentTcb.getTid( ) : -1;
        int tid = getNewTid( ); // get a new TID
        if ( tid == -1)
            return null;
        TCB tcb = new TCB( t, tid, pid ); // create a new TCB
        queue.add( tcb );
        return tcb;
    }

    // A new feature added to p161
    // Removing the TCB of a terminating thread
    public boolean deleteThread( ) {
        TCB tcb = getMyTcb( );
        if ( tcb!= null )
            return tcb.setTerminated( );
        else
            return false;
    }

    public void sleepThread( int milliseconds ) {
        try {
            sleep( milliseconds );
        } catch ( InterruptedException e ) { }
    }

    // A modified run of p161
    public void run( ) {
        Thread current = null;

        // this.setPriority( 6 );

        while ( true ) {
            try {
                // get the next TCB and its thrad
                if ( queue.size( ) == 0 )
                    continue;
                TCB currentTCB = (TCB)queue.firstElement( );
                if ( currentTCB.getTerminated( ) == true ) {
                    queue.remove( currentTCB );
                    returnTid( currentTCB.getTid( ) );
                    continue;
                }
                current = currentTCB.getThread( );
                if ( current != null ) {
                    if ( current.isAlive( ) )
                        // current.setPriority( 4 );
                        current.resume();
                    else {
                        // Spawn must be controlled by Scheduler
                        // Scheduler must start a new thread
                        current.start( );
                        // current.setPriority( 4 );
                    }
                }

                schedulerSleep( );
                // System.out.println("* * * Context Switch * * * ");

                synchronized ( queue ) {
                    if ( current != null && current.isAlive( ) )
                        // current.setPriority( 2 );
                        current.suspend();
                    queue.remove( currentTCB ); // rotate this TCB to the end
                    queue.add( currentTCB );
                }
            } catch ( NullPointerException e3 ) { };
        }
    }
}