package com.gwu.simulator;

import java.io.IOException;

public class MemoryTest {
    public static void main(String[] args) throws Exception {
        Memory mem = new Memory();

        // Test write
        mem.setMAR(10);
        mem.setMBR((short) 123);
        mem.write();

        // Test read
        mem.setMAR(10);
        mem.read();
        System.out.println("Read from memory[10] = " + mem.getMBR());

        // Load program from file (assembler output)
        String programPath = "resources/output/sample_load.txt";
        if (args != null && args.length > 0 && args[0] != null && !args[0].isEmpty()) {
            programPath = args[0];
        }

        try {
            mem.loadProgramFromFile(programPath);
            mem.dump(6, 12); // show contents
        } catch (IOException e) {
            System.err.println("Could not load program from '" + programPath + "': " + e.getMessage());
            System.err.println("Continuing without loading program.\n");
            mem.dump(6, 12);
        }
    }
}
