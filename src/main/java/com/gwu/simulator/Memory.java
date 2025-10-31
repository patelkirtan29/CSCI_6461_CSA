package com.gwu.simulator;

import java.io.*;
import java.util.Arrays;


public class Memory {

    private static final int MEMORY_SIZE = 2048;
    private static final String TRACE_FILE = "cache_trace.txt";
    private static final int CACHE_SIZE = 16;  // 16 cache lines as specified

    private final short[] memory = new short[MEMORY_SIZE];
    private final CacheLine[] cache;
    private final PrintWriter traceWriter;
    private int accessCount;
    private int hitCount;
    private int missCount;
    private int currentTimestamp;

    // Inner class for cache line structure
    public static class CacheLine {
        public boolean valid;      // Valid bit
        public int tag;           // Tag field (memory address)
        public short data;        // Data stored in cache line
        public int timestamp;     // For FIFO replacement

        CacheLine() {
            valid = false;
            tag = -1;
            data = 0;
            timestamp = 0;
        }
    }

    public Memory() {
        reset();
        cache = new CacheLine[CACHE_SIZE];
        for (int i = 0; i < CACHE_SIZE; i++) {
            cache[i] = new CacheLine();
        }
        accessCount = 0;
        hitCount = 0;
        missCount = 0;
        currentTimestamp = 0;

        // Initialize trace file
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(TRACE_FILE, true));
        } catch (IOException e) {
            System.err.println("Could not create cache trace file: " + e.getMessage());
        }
        traceWriter = pw;
    }

    /** Resets all memory contents and registers to zero (power-on reset). */
    public void reset() {
        Arrays.fill(memory, (short) 0);
    }

    public void loadProgramFromFile(String filePath) throws IOException {
        BufferedReader br = null;
        boolean loaded = false;

        // First try opening as a regular filesystem path
        try {
            br = new BufferedReader(new FileReader(filePath));
            loaded = true;
        } catch (FileNotFoundException e) {
            // If not found on filesystem, try to load as a classpath resource
            InputStream is = getClass().getClassLoader().getResourceAsStream(filePath.replace('\\', '/'));
            if (is == null) {
                // Try relative path without leading directories
                is = getClass().getClassLoader().getResourceAsStream(new java.io.File(filePath).getName());
            }
            if (is != null) {
                br = new BufferedReader(new InputStreamReader(is));
                loaded = true;
            }
        }

        if (!loaded || br == null) {
            throw new FileNotFoundException("Program file not found (filesystem or classpath): " + filePath);
        }

        try (BufferedReader reader = br) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");
                if (parts.length < 2) continue;

                int address = Integer.parseInt(parts[0], 8); // octal address
                short value = (short) Integer.parseInt(parts[1], 8); // octal value

                if (address >= 0 && address < MEMORY_SIZE) {
                    memory[address] = value;
                } else {
                    System.err.println("Invalid memory address in file: " + address);
                }
            }
        }

        System.out.println("Program loaded successfully into memory.");
    }

    /** Prints a memory range (for debugging). */
    public void dump(int start, int end) {
        if (start < 0 || end >= MEMORY_SIZE || start > end)
            throw new IllegalArgumentException("Invalid memory range.");

        System.out.println("------ Memory Dump (octal) ------");
        for (int i = start; i <= end; i++) {
            System.out.printf("%06o : %06o\n", i, memory[i]);
        }
        System.out.println("--------------------------------");
    }

    /** Returns the word stored at an address through cache. */
    public short getValueAt(int address) {
        if (address < 0 || address >= MEMORY_SIZE)
            throw new IllegalArgumentException("Address out of range: " + address);

        accessCount++;
        
        // Check cache first
        for (CacheLine line : cache) {
            if (line.valid && line.tag == address) {
                // Cache hit
                hitCount++;
                traceLog("READ HIT", address, line.data);
                return line.data;
            }
        }

        // Cache miss - fetch from memory and update cache
        missCount++;
        short value = memory[address];
        insertIntoCache(address, value);
        traceLog("READ MISS", address, value);
        
        return value;
    }

    /** Sets a value at an address through cache */
    public void setValueAt(int address, short value) {
        if (address < 0 || address >= MEMORY_SIZE)
            throw new IllegalArgumentException("Address out of range: " + address);

        accessCount++;
        
        // Update main memory (write-through policy)
        memory[address] = value;

        // Update cache if present
        boolean cacheHit = false;
        for (CacheLine line : cache) {
            if (line.valid && line.tag == address) {
                line.data = value;
                hitCount++;
                cacheHit = true;
                traceLog("WRITE HIT", address, value);
                break;
            }
        }

        // Cache miss - write-allocate policy
        if (!cacheHit) {
            missCount++;
            insertIntoCache(address, value);
            traceLog("WRITE MISS", address, value);
        }
    }

    /** Insert a new line into cache using FIFO replacement */
    private void insertIntoCache(int address, short value) {
        // First, look for an invalid (empty) line
        for (CacheLine line : cache) {
            if (!line.valid) {
                line.valid = true;
                line.tag = address;
                line.data = value;
                line.timestamp = currentTimestamp++;
                return;
            }
        }

        // No empty lines - find oldest line (FIFO)
        int oldestIndex = 0;
        int oldestTimestamp = cache[0].timestamp;
        
        for (int i = 1; i < CACHE_SIZE; i++) {
            if (cache[i].timestamp < oldestTimestamp) {
                oldestIndex = i;
                oldestTimestamp = cache[i].timestamp;
            }
        }

        // Replace the oldest line
        cache[oldestIndex].tag = address;
        cache[oldestIndex].data = value;
        cache[oldestIndex].timestamp = currentTimestamp++;
    }

    /** Log cache operations to trace file */
    private void traceLog(String operation, int address, short value) {
        if (traceWriter != null) {
            String message = String.format("%s: Address=%04o, Value=%06o, Hits=%d, Misses=%d, Hit Rate=%.2f%%\n",
                    operation, address, value, hitCount, missCount, getHitRate());
            traceWriter.printf(message);
            traceWriter.flush();
            if (consolePrinter != null) {
                consolePrinter.accept(message);
            }
        }
    }

    private java.util.function.Consumer<String> consolePrinter;

    public void setConsolePrinter(java.util.function.Consumer<String> printer) {
        this.consolePrinter = printer;
    }

    /** Get cache hit rate */
    public double getHitRate() {
        return accessCount == 0 ? 0 : (hitCount * 100.0) / accessCount;
    }

    public void enqueueInput(short value) {
        // For compatibility with the UI - not used in this implementation
    }

    /** Get cache statistics */
    public String getCacheStats() {
        return String.format("Cache Statistics:\n" +
                           "Total Accesses: %d\n" +
                           "Cache Hits: %d\n" +
                           "Cache Misses: %d\n" +
                           "Hit Rate: %.2f%%\n",
                           accessCount, hitCount, missCount, getHitRate());
    }

    /** Get the contents of the cache for visualization */
    public CacheLine[] getCacheContents() {
        CacheLine[] copy = new CacheLine[CACHE_SIZE];
        for (int i = 0; i < CACHE_SIZE; i++) {
            CacheLine line = cache[i];
            CacheLine newLine = new CacheLine();
            newLine.valid = line.valid;
            newLine.tag = line.tag;
            newLine.data = line.data;
            newLine.timestamp = line.timestamp;
            copy[i] = newLine;
        }
        return copy;
    }

    /** Close the trace file */
    public void closeCache() {
        if (traceWriter != null) {
            traceWriter.close();
        }
    }
}
