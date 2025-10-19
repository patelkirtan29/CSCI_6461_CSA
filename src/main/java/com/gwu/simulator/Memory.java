package com.gwu.simulator;

import java.io.*;
import java.util.Arrays;


public class Memory {

    private static final int MEMORY_SIZE = 2048;

    private final short[] memory = new short[MEMORY_SIZE];

    public Memory() {
        reset();
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

    /** Returns the word stored at an address. */
    public short getValueAt(int address) {
        if (address < 0 || address >= MEMORY_SIZE)
            throw new IllegalArgumentException("Address out of range: " + address);
        return memory[address];
    }

    /** Sets a value at an address  */
    public void setValueAt(int address, short value) {
        if (address < 0 || address >= MEMORY_SIZE)
            throw new IllegalArgumentException("Address out of range: " + address);
        memory[address] = value;
    }
}
