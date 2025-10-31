package com.gwu.simulator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

class CacheLine {
    boolean valid;
    int tag;
    int data;
    int timestamp;  // For tracking FIFO order

    CacheLine() {
        this.valid = false;
        this.tag = -1;
        this.data = 0;
        this.timestamp = 0;
    }
}

public class Cache {
    private static final int CACHE_SIZE = 16;
    private static final String TRACE_FILE = "cache_trace.txt";
    
    private final CacheLine[] lines;
    private final Queue<Integer> fifoQueue; // FIFO tracking
    private PrintWriter traceWriter;
    private int accessCount;
    private int hitCount;
    private int missCount;
    private int currentTimestamp;

    public Cache() {
        lines = new CacheLine[CACHE_SIZE];
        for (int i = 0; i < CACHE_SIZE; i++) {
            lines[i] = new CacheLine();
        }
        fifoQueue = new LinkedList<>();
        accessCount = 0;
        hitCount = 0;
        missCount = 0;
        currentTimestamp = 0;

        try {
            traceWriter = new PrintWriter(new FileWriter(TRACE_FILE, true));
        } catch (IOException e) {
            System.err.println("Could not create trace file: " + e.getMessage());
        }
    }

    // Read a word from memory (through cache)
    public int read(int address, int[] memory) {
        accessCount++;
        int tag = address;

        // Cache Hit
        for (CacheLine line : lines) {
            if (line.valid && line.tag == tag) {
                hitCount++;
                traceLog("READ HIT", address);
                return line.data;
            }
        }

        // Cache Miss
        missCount++;
        traceLog("READ MISS", address);
        int data = memory[address];
        insertLine(tag, data);
        return data;
    }

    // Write to cache + memory
    public void write(int address, int value, int[] memory) {
        accessCount++;
        int tag = address;

        // Update memory
        memory[address] = value;

        // Update cache (if present)
        for (CacheLine line : lines) {
            if (line.valid && line.tag == tag) {
                hitCount++;
                line.data = value;
                traceLog("WRITE HIT", address);
                return;
            }
        }

        // Cache miss on write → load it into cache (write-allocate)
        missCount++;
        traceLog("WRITE MISS", address);
        insertLine(tag, value);
    }

    // FIFO replacement logic
    private void insertLine(int tag, int data) {
        // If cache not full
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].valid) {
                lines[i].valid = true;
                lines[i].tag = tag;
                lines[i].data = data;
                lines[i].timestamp = currentTimestamp++;
                fifoQueue.add(i);
                return;
            }
        }

        // Cache full → FIFO replacement
        int replaceIndex = fifoQueue.poll();
        fifoQueue.add(replaceIndex);

        lines[replaceIndex].tag = tag;
        lines[replaceIndex].data = data;
        lines[replaceIndex].valid = true;
        lines[replaceIndex].timestamp = currentTimestamp++;
    }

    // Log cache operations to trace file
    private void traceLog(String operation, int address) {
        if (traceWriter != null) {
            traceWriter.printf("%s: Address=%04X, Hits=%d, Misses=%d, Hit Rate=%.2f%%\n",
                    operation, address, hitCount, missCount, getHitRate());
            traceWriter.flush();
        }
    }

    // Get cache hit rate
    public double getHitRate() {
        return accessCount == 0 ? 0 : (hitCount * 100.0) / accessCount;
    }

    // Get cache statistics
    public String getStats() {
        return String.format("Cache Statistics:\n" +
                           "Total Accesses: %d\n" +
                           "Cache Hits: %d\n" +
                           "Cache Misses: %d\n" +
                           "Hit Rate: %.2f%%\n",
                           accessCount, hitCount, missCount, getHitRate());
    }

    // Close trace file
    public void closeTrace() {
        if (traceWriter != null) {
            traceWriter.close();
        }
    }
}
