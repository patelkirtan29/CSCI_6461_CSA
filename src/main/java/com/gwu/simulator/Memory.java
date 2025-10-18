package com.gwu.simulator;

import java.io.*;
import java.util.Arrays;

/**
 * Memory.java
 * Implements a 2048-word main memory module for CS6461 simulator.
 * 
 * Each memory cell stores a 16-bit word (short).
 * 
 * MAR (Memory Address Register) holds the address.
 * MBR (Memory Buffer Register) holds the data to be read/written.
 * 
 * All memory is initialized to 0 at power-on.
 * 
 * Supports read(), write(), reset(), and loadProgramFromFile() (ROM Loader).
 */
public class Memory {

    // Maximum memory size (2K words = 2048)
    private static final int MEMORY_SIZE = 2048;

    // Memory array
    private final short[] memory = new short[MEMORY_SIZE];

    // Memory Address and Buffer Registers
    private int MAR;  // Memory Address Register (0 - 2047)
    private short MBR; // Memory Buffer Register (16-bit data word)

    // Constructor
    public Memory() {
        reset();
    }

    /** Resets all memory contents and registers to zero (power-on reset). */
    public void reset() {
        Arrays.fill(memory, (short) 0);
        MAR = 0;
        MBR = 0;
    }

    /** Sets the Memory Address Register (MAR). */
    public void setMAR(int address) {
        if (address < 0 || address >= MEMORY_SIZE) {
            throw new IllegalArgumentException("Address out of range: " + address);
        }
        this.MAR = address;
    }

    /** Gets the Memory Address Register (MAR). */
    public int getMAR() {
        return MAR;
    }

    /** Sets the Memory Buffer Register (MBR). */
    public void setMBR(short value) {
        this.MBR = value;
    }

    /** Gets the Memory Buffer Register (MBR). */
    public short getMBR() {
        return MBR;
    }

    /**
     * Reads a word from memory using MAR → loads it into MBR.
     */
    public void read() {
        MBR = memory[MAR];
    }

    /**
     * Writes the value from MBR into memory at address MAR.
     */
    public void write() {
        memory[MAR] = MBR;
    }

    /**
     * Loads a program file (load.txt) into memory.
     * File format: "address value" pairs in OCTAL, e.g.:
     * 000006 000012
     * 000007 000003
     */
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
                    System.err.println("⚠️ Invalid memory address in file: " + address);
                }
            }
        }

        System.out.println("✅ Program loaded successfully into memory.");
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

    /** Returns the word stored at an address. */
    public short getValueAt(int address) {
        if (address < 0 || address >= MEMORY_SIZE)
            throw new IllegalArgumentException("Address out of range: " + address);
        return memory[address];
    }

    /** Sets a value at an address (used by loader or CPU). */
    public void setValueAt(int address, short value) {
        if (address < 0 || address >= MEMORY_SIZE)
            throw new IllegalArgumentException("Address out of range: " + address);
        memory[address] = value;
    }
}
