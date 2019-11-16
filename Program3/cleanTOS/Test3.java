import java.util.Date;

class Test3 extends Thread {

    private int count; // The number of CPU and disk intensive threads to run.

    public Test3(String[] pairArr) {
        this.count = Integer.parseInt(pairArr[0]);
    }

    // Runs the simulation of CPU and disk intensive tasks.
    public void run() {
        String[] CPU_Intensive = SysLib.stringToArgs("TestThread3 comp");
        String[] Disk_Intensive = SysLib.stringToArgs("TestThread3 disk");
        long start = (new Date()).getTime();

        // Call the exec command for the CPU and disk intensive commands.
        for(int i = 0; i < this.count; i++) {
            SysLib.exec(CPU_Intensive);
            SysLib.exec(Disk_Intensive);
        }

        // Wait on each of the exec commands.
        for(int i = 0; i < this.count * 2; i++) {
            SysLib.join();
        }

        // Get the finishing time and print the result.
        long end = (new Date()).getTime();
        SysLib.cout("Total time: " + (end - start) + " milliseconds.\n");
        SysLib.exit();
    }
}