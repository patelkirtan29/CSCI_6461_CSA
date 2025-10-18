package com.gwu.simulator;

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
        mem.loadProgramFromFile("resources/output/sample_load.txt");
        mem.dump(6, 12); // show contents
    }
}
