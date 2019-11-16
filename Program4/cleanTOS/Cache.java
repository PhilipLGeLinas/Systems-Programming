// Programmer: Philip L. GeLinas
// Created on: 11/16/2019

import java.util.Vector;

// Simulates the cache of an OS.
public class Cache {

    private int blockSize;
    private Vector pages;
    private int victim;
    private Cache.Entry[] pageTable = null;

    // Allocates a cacheBlocks number of cache blocks,
    // each containing blockSize-byte data, on memory.
    public Cache(int blockSize, int cacheBlocks) {
        this.blockSize = blockSize;
        this.pages = new Vector();

        for(int i = 0; i < cacheBlocks; ++i) {
            byte[] arr = new byte[blockSize];
            this.pages.addElement(arr);
        }

        this.victim = cacheBlocks - 1;
        this.pageTable = new Cache.Entry[cacheBlocks];

        for(int i = 0; i < cacheBlocks; ++i) {
            this.pageTable[i] = new Cache.Entry();
        }

    }

    // Reads into the buffer[] array the cache block specified by blockId from the disk cache if it is in cache.
    // Otherwise, reads the corresponding disk block from the disk device. Upon an error, returns false.
    public synchronized boolean read(int var1, byte[] var2) {
        if (var1 < 0) {
            SysLib.cerr("threadOS: a wrong blockId for cread\n");
            return false;
        } else {
            int var3;
            byte[] var4;
            for(var3 = 0; var3 < this.pageTable.length; ++var3) {
                if (this.pageTable[var3].frame == var1) {
                    var4 = (byte[])((byte[])this.pages.elementAt(var3));
                    System.arraycopy(var4, 0, var2, 0, this.blockSize);
                    this.pageTable[var3].reference = true;
                    return true;
                }
            }

            if ((var3 = this.findFreePage()) == -1) {
                var3 = this.nextVictim();
            }

            this.writeBack(var3);
            SysLib.rawread(var1, var2);
            var4 = new byte[this.blockSize];
            System.arraycopy(var2, 0, var4, 0, this.blockSize);
            this.pages.set(var3, var4);
            this.pageTable[var3].frame = var1;
            this.pageTable[var3].reference = true;
            return true;
        }
    }

    // Writes the buffer[] array contents to the cache block specified by blockId from the disk cache if it is in cache.
    // Otherwise finds a free cache block and writes the buffer[] contents on it. No write through.
    // Upon an error, returns false.
    public synchronized boolean write(int var1, byte[] var2) {
        if (var1 < 0) {
            SysLib.cerr("threadOS: a wrong blockId for cwrite\n");
            return false;
        } else {
            int var3;
            byte[] var4;
            for(var3 = 0; var3 < this.pageTable.length; ++var3) {
                if (this.pageTable[var3].frame == var1) {
                    var4 = new byte[this.blockSize];
                    System.arraycopy(var2, 0, var4, 0, this.blockSize);
                    this.pages.set(var3, var4);
                    this.pageTable[var3].reference = true;
                    this.pageTable[var3].dirty = true;
                    return true;
                }
            }

            if ((var3 = this.findFreePage()) == -1) {
                var3 = this.nextVictim();
            }

            this.writeBack(var3);
            var4 = new byte[this.blockSize];
            System.arraycopy(var2, 0, var4, 0, this.blockSize);
            this.pages.set(var3, var4);
            this.pageTable[var3].frame = var1;
            this.pageTable[var3].reference = true;
            this.pageTable[var3].dirty = true;
            return true;
        }
    }

    // Writes back all dirty blocks to Disk.java and thereafter forces Disk.java to write back all contents
    // to the DISK file. Maintains clean block copies in Cache.java. Called when shutting down ThreadOS.
    public synchronized void sync() {
        for(int var1 = 0; var1 < this.pageTable.length; ++var1) {
            this.writeBack(var1);
        }

        SysLib.sync();
    }

    // Invalidates all cached blocks. Called when you keep running a different test
    // case without receiving any caching effects incurred by the previous test.
    public synchronized void flush() {
        for(int var1 = 0; var1 < this.pageTable.length; ++var1) {
            this.writeBack(var1);
            this.pageTable[var1].reference = false;
            this.pageTable[var1].frame = -1;
        }

        SysLib.sync();
    }

    private int findFreePage() {
        for(int var1 = 0; var1 < this.pageTable.length; ++var1) {
            if (this.pageTable[var1].frame == -1) {
                return var1;
            }
        }

        return -1;
    }

    private int nextVictim() {
        while(true) {
            this.victim = (this.victim + 1) % this.pageTable.length;
            if (!this.pageTable[this.victim].reference) {
                return this.victim;
            }

            this.pageTable[this.victim].reference = false;
        }
    }

    private void writeBack(int var1) {
        if (this.pageTable[var1].frame != -1 && this.pageTable[var1].dirty) {
            byte[] var2 = (byte[])((byte[])this.pages.elementAt(var1));
            SysLib.rawwrite(this.pageTable[var1].frame, var2);
            this.pageTable[var1].dirty = false;
        }

    }

    private class Entry {
        public static final int INVALID = -1;
        public boolean reference = false;
        public boolean dirty = false;
        public int frame = -1;

        public Entry() {
        }
    }
}
