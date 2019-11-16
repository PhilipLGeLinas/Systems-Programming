// Programmer: Philip L. GeLinas
// Created on: 11/16/2019

import java.util.Date;
import java.util.Random;

// Tests the functionality of Cache.java.
class Test4 extends Thread {

    private boolean enabled;
    private int testcase;
    private long startTime;
    private long endTime;
    private byte[] wbytes;
    private byte[] rbytes;
    private Random rand;

    private void getPerformance(String var1) {
        if (this.enabled) {
            SysLib.cout("Test " + var1 + "(cache enabled): " + (this.endTime - this.startTime) + "\n");
        } else {
            SysLib.cout("Test " + var1 + "(cache disabled): " + (this.endTime - this.startTime) + "\n");
        }

    }

    private void read(int var1, byte[] var2) {
        if (this.enabled) {
            SysLib.cread(var1, var2);
        } else {
            SysLib.rawread(var1, var2);
        }

    }

    private void write(int var1, byte[] var2) {
        if (this.enabled) {
            SysLib.cwrite(var1, var2);
        } else {
            SysLib.rawwrite(var1, var2);
        }

    }

    private void randomAccess() {
        int[] var1 = new int[200];

        int var2;
        for(var2 = 0; var2 < 200; ++var2) {
            var1[var2] = Math.abs(this.rand.nextInt() % 512);
        }

        int var3;
        for(var2 = 0; var2 < 200; ++var2) {
            for(var3 = 0; var3 < 512; ++var3) {
                this.wbytes[var3] = (byte)var3;
            }

            this.write(var1[var2], this.wbytes);
        }

        for(var2 = 0; var2 < 200; ++var2) {
            this.read(var1[var2], this.rbytes);

            for(var3 = 0; var3 < 512; ++var3) {
                if (this.rbytes[var3] != this.wbytes[var3]) {
                    SysLib.cerr("ERROR\n");
                    SysLib.exit();
                }
            }
        }

    }

    private void localizedAccess() {
        for(int var1 = 0; var1 < 20; ++var1) {
            int var2;
            for(var2 = 0; var2 < 512; ++var2) {
                this.wbytes[var2] = (byte)(var1 + var2);
            }

            for(var2 = 0; var2 < 1000; var2 += 100) {
                this.write(var2, this.wbytes);
            }

            for(var2 = 0; var2 < 1000; var2 += 100) {
                this.read(var2, this.rbytes);

                for(int var3 = 0; var3 < 512; ++var3) {
                    if (this.rbytes[var3] != this.wbytes[var3]) {
                        SysLib.cerr("ERROR\n");
                        SysLib.exit();
                    }
                }
            }
        }

    }

    private void mixedAccess() {
        int[] var1 = new int[200];

        int var2;
        for(var2 = 0; var2 < 200; ++var2) {
            if (Math.abs(this.rand.nextInt() % 10) > 8) {
                var1[var2] = Math.abs(this.rand.nextInt() % 512);
            } else {
                var1[var2] = Math.abs(this.rand.nextInt() % 10);
            }
        }

        int var3;
        for(var2 = 0; var2 < 200; ++var2) {
            for(var3 = 0; var3 < 512; ++var3) {
                this.wbytes[var3] = (byte)var3;
            }

            this.write(var1[var2], this.wbytes);
        }

        for(var2 = 0; var2 < 200; ++var2) {
            this.read(var1[var2], this.rbytes);

            for(var3 = 0; var3 < 512; ++var3) {
                if (this.rbytes[var3] != this.wbytes[var3]) {
                    SysLib.cerr("ERROR\n");
                    SysLib.exit();
                }
            }
        }

    }

    private void adversaryAccess() {
        int var1;
        int var2;
        for(var1 = 0; var1 < 20; ++var1) {
            for(var2 = 0; var2 < 512; ++var2) {
                this.wbytes[var2] = (byte)var2;
            }

            for(var2 = 0; var2 < 10; ++var2) {
                this.write(var1 * 10 + var2, this.wbytes);
            }
        }

        for(var1 = 0; var1 < 20; ++var1) {
            for(var2 = 0; var2 < 10; ++var2) {
                this.read(var1 * 10 + var2, this.rbytes);

                for(int var3 = 0; var3 < 512; ++var3) {
                    if (this.rbytes[var3] != this.wbytes[var3]) {
                        SysLib.cerr("ERROR\n");
                        SysLib.exit();
                    }
                }
            }
        }

    }

    public Test4(String[] var1) {
        this.enabled = var1[0].equals("enabled");
        this.testcase = Integer.parseInt(var1[1]);
        this.wbytes = new byte[512];
        this.rbytes = new byte[512];
        this.rand = new Random();
    }

    public void run() {
        SysLib.flush();
        this.startTime = (new Date()).getTime();
        switch(this.testcase) {
            case 1:
                this.randomAccess();
                this.endTime = (new Date()).getTime();
                this.getPerformance("random accesses");
                break;
            case 2:
                this.localizedAccess();
                this.endTime = (new Date()).getTime();
                this.getPerformance("localized accesses");
                break;
            case 3:
                this.mixedAccess();
                this.endTime = (new Date()).getTime();
                this.getPerformance("mixed accesses");
                break;
            case 4:
                this.adversaryAccess();
                this.endTime = (new Date()).getTime();
                this.getPerformance("adversary accesses");
                break;
            case 5:
                this.randomAccess();
                this.endTime = (new Date()).getTime();
                this.getPerformance("random accesses");
                this.startTime = (new Date()).getTime();
                this.localizedAccess();
                this.endTime = (new Date()).getTime();
                this.getPerformance("localized accesses");
                this.startTime = (new Date()).getTime();
                this.mixedAccess();
                this.endTime = (new Date()).getTime();
                this.getPerformance("mixed accesses");
                this.startTime = (new Date()).getTime();
                this.adversaryAccess();
                this.endTime = (new Date()).getTime();
                this.getPerformance("adversary accesses");
        }

        SysLib.exit();
    }
}
