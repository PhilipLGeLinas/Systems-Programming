// Programmer: Philip L. GeLinas
// Created on: 11/16/2019

import java.util.Vector;

// Simulates the cache of an OS.
public class Cache {

    private int blockSize;
    private Block[] pageTable = null;
    private int victim;
    private Vector pages;

    // Allocates a cacheBlocks number of cache blocks,
    // each containing blockSize-byte data, on memory.
    public Cache(int blockSize, int cacheBlocks) {

        this.blockSize = blockSize;
        this.pages = new Vector();

        for(int i = 0; i < cacheBlocks; ++i) {
            byte[] arr = new byte[blockSize];
            this.pages.addElement(arr);
        }

        this.pageTable = new Block[cacheBlocks];
        this.victim = cacheBlocks - 1;

        for(int i = 0; i < cacheBlocks; ++i) {
            this.pageTable[i] = new Block();
        }

    }

    // Reads into the buffer[] array the cache block specified by blockId from the disk cache if it is in cache.
    // Otherwise, reads the corresponding disk block from the disk device. Upon an error, returns false.
    public synchronized boolean read(int blockId, byte[] buffer) {
        if (blockId < 0) {
            SysLib.cerr("Invalid blockId!\n");
            return false;
        } else {
            byte[] arr;
            for(int i = 0; i < this.pageTable.length; ++i) {
                if (this.pageTable[i].frame == blockId) {
                    arr = (byte[])((byte[])this.pages.elementAt(i));
                    System.arraycopy(arr, 0, buffer, 0, this.blockSize);
                    this.pageTable[i].reference = true;
                    return true;
                }
            }

            if ((i = this.findFreePage()) == -1) {
                i = this.nextVictim();
            }

            this.writeBack(i);
            SysLib.rawread(blockId, buffer);
            arr = new byte[this.blockSize];
            System.arraycopy(buffer, 0, arr, 0, this.blockSize);
            this.pages.set(i, arr);
            this.pageTable[i].frame = blockId;
            this.pageTable[i].reference = true;

            return true;
        }
    }

    // Writes the buffer[] array contents to the cache block specified by blockId from the disk cache if it is in cache.
    // Otherwise finds a free cache block and writes the buffer[] contents on it. No write through.
    // Upon an error, returns false.
    public synchronized boolean write(int blockId, byte[] buffer) {
        if (blockId < 0) {
            SysLib.cerr("Invalid blockId!\n");
            return false;
        } else {
            byte[] arr;
            for(int i = 0; i < this.pageTable.length; ++i) {
                if (this.pageTable[i].frame == blockId) {
                    arr = new byte[this.blockSize];
                    System.arraycopy(buffer, 0, arr, 0, this.blockSize);
                    this.pages.set(i, arr);
                    this.pageTable[i].reference = true;
                    this.pageTable[i].dirty = true;
                    return true;
                }
            }

            if ((i = this.findFreePage()) == -1) {
                i = this.nextVictim();
            }

            this.writeBack(i);
            arr = new byte[this.blockSize];
            System.arraycopy(buffer, 0, arr, 0, this.blockSize);
            this.pages.set(i, arr);
            this.pageTable[i].frame = blockId;
            this.pageTable[i].reference = true;
            this.pageTable[i].dirty = true;
            return true;
        }
    }

    // Writes back all dirty blocks to Disk.java and thereafter forces Disk.java to write back all contents
    // to the DISK file. Maintains clean block copies in Cache.java. Called when shutting down ThreadOS.
    public synchronized void sync() {
        for(int i = 0; i < this.pageTable.length; ++i) {
            this.writeBack(i);
        }

        SysLib.sync();
    }

    // Invalidates all cached blocks. Called when you keep running a different test
    // case without receiving any caching effects incurred by the previous test.
    public synchronized void flush() {
        for(int i = 0; i < this.pageTable.length; ++i) {
            this.writeBack(i);
            this.pageTable[i].reference = false;
            this.pageTable[i].frame = -1;
        }

        SysLib.sync();
    }

    // Searches for the first page that is set to invalid.
    private int findFreePage() {
        for(int i = 0; i < this.pageTable.length; ++i) {
            if (this.pageTable[i].frame == -1) {
                return i;
            }
        }

        return -1;
    }

    // Searches for the next page to be replaced.
    private int nextVictim() {
        while(true) {
            this.victim = (this.victim + 1) % this.pageTable.length;
            if (!this.pageTable[this.victim].reference) {
                return this.victim;
            }

            this.pageTable[this.victim].reference = false;
        }
    }

    // Writes data back to the disk before replacement.
    private void writeBack(int index) {
        if (this.pageTable[index].frame != -1 && this.pageTable[index].dirty) {
            byte[] current = (byte[])((byte[])this.pages.elementAt(index));
            SysLib.rawwrite(this.pageTable[index].frame, current);
            this.pageTable[index].dirty = false;
        }

    }
}

// A block containing a reference and dirty bit.
// Used to implement the enhanced second-chance algorithm.
public class Block {

    public boolean reference = false;
    public boolean dirty = false;
    public int frame = -1;

    public Block() {

    }
}
