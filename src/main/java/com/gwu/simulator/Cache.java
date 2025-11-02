package com.gwu.simulator;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Implements a fully associative cache with FIFO replacement policy.
 * Cache specifications:
 * - 16 cache lines
 * - Fully associative
 * - FIFO replacement policy
 * - Write-through policy
 * - Unified cache (stores both instructions and data)
 */
public class Cache {
    public static final int CACHE_SIZE = 16;
    private CacheLine[] lines;
    private Queue<Integer> fifoQueue;  // Queue for FIFO replacement policy

    /**
     * Represents a single cache line with tag, valid bit, and data.
     */
    public static class CacheLine {
        private int tag;           // Memory address tag
        private boolean valid;     // Valid bit
        private short data;        // 16-bit word data
        
        public CacheLine() {
            this.valid = false;
            this.tag = 0;
            this.data = 0;
        }

        public String toString() {
            return String.format("Tag: %04X, Valid: %b, Data: %04X", tag, valid, data & 0xFFFF);
        }

        // Getters and setters
        public int getTag() { return tag; }
        public boolean isValid() { return valid; }
        public short getData() { return data; }
        public void setTag(int tag) { this.tag = tag; }
        public void setValid(boolean valid) { this.valid = valid; }
        public void setData(short data) { this.data = data; }
    }

    public Cache() {
        lines = new CacheLine[CACHE_SIZE];
        for (int i = 0; i < CACHE_SIZE; i++) {
            lines[i] = new CacheLine();
        }
        fifoQueue = new LinkedList<>();
    }

    /**
     * Reads data from cache. Returns null if cache miss.
     * @param address Memory address to read from
     * @return Data if cache hit, null if cache miss
     */
    public Short read(int address) {
        for (CacheLine line : lines) {
            if (line.isValid() && line.getTag() == address) {
                return line.getData(); // Cache hit
            }
        }
        return null; // Cache miss
    }

    /**
     * Writes data to cache using write-through policy.
     * @param address Memory address
     * @param data Data to write
     * @return Index where data was written
     */
    public int write(int address, short data) {
        // First, check if address already exists in cache
        for (int i = 0; i < CACHE_SIZE; i++) {
            if (lines[i].isValid() && lines[i].getTag() == address) {
                lines[i].setData(data);
                return i;
            }
        }

        // If not found, need to find a spot or replace
        int index = findEmptyLine();
        if (index == -1) {
            // No empty line, use FIFO replacement
            index = fifoQueue.remove();
        }

        lines[index].setTag(address);
        lines[index].setValid(true);
        lines[index].setData(data);
        fifoQueue.offer(index);

        return index;
    }

    /**
     * Finds an empty cache line.
     * @return Index of empty line, or -1 if none available
     */
    private int findEmptyLine() {
        for (int i = 0; i < CACHE_SIZE; i++) {
            if (!lines[i].isValid()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets all cache lines for display purposes.
     * @return Array of cache lines
     */
    public CacheLine[] getLines() {
        return lines;
    }

    /**
     * Clears the cache (invalidates all lines).
     */
    public void clear() {
        for (CacheLine line : lines) {
            line.setValid(false);
        }
        fifoQueue.clear();
    }
}