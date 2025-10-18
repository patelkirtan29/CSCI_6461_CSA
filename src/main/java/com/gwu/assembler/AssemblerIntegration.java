package com.gwu.assembler;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.gwu.simulator.Memory;
import com.gwu.assembler.CPU;

public class AssemblerIntegration {

    // Step 1: Read assembler text file (.txt/.obj)
    public static List<Instruction> readAssemblyFile(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        List<Instruction> instructions = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith(";") || line.startsWith("//")) continue;

            String label = "";
            String opcode = "";
            String comment = "";
            String[] operands = new String[0];

            // Split comments
            if (line.contains(";")) {
                String[] parts = line.split(";", 2);
                line = parts[0].trim();
                comment = parts[1].trim();
            }

            // Tokenize label, opcode, operands
            String[] tokens = line.split("\\s+|,\\s*");
            if (tokens.length > 0) {
                // Check for label
                if (tokens[0].endsWith(":")) {
                    label = tokens[0].substring(0, tokens[0].length() - 1);
                    opcode = tokens.length > 1 ? tokens[1].toUpperCase() : "";
                    operands = Arrays.copyOfRange(tokens, 2, tokens.length);
                } else {
                    opcode = tokens[0].toUpperCase();
                    operands = Arrays.copyOfRange(tokens, 1, tokens.length);
                }
            }

            instructions.add(new Instruction(label, opcode, operands, comment));
        }
        return instructions;
    }

    // Step 2: Encode instructions to binary
    public static List<Integer> encodeInstructions(List<Instruction> instructions) {
        List<Integer> machineCode = new ArrayList<>();

        for (Instruction inst : instructions) {
            Integer opcodeBinary = Opcode.OPCODES.get(inst.opcode);
            if (opcodeBinary == null) {
                System.err.println("[WARN] Unknown opcode: " + inst.opcode);
                continue;
            }

            int encoded = opcodeBinary << 10;
            machineCode.add(encoded);
        }

        return machineCode;
    }

    // Step 3: Loader integration (replace with Yeabsira’s loader)
    public static void loadIntoMemory(List<Integer> machineCode) {
        System.out.println("[INFO] Loading " + machineCode.size() + " words into memory...");

        Memory memory = new Memory();
        for (int addr = 0; addr < machineCode.size(); addr++) {
            int word = machineCode.get(addr);
            memory.setMAR(addr);
            memory.setMBR((short) word);
            memory.write();
        }

        System.out.println("[INFO] Program loaded into memory successfully.");

        // Create CPU and run it for a limited number of steps to avoid infinite loops
        CPU cpu = new CPU(memory);
        int maxSteps = 1000;
        int steps = 0;
        while (!cpu.isHalted() && steps < maxSteps) {
            cpu.step();
            steps++;
        }
        System.out.println("[INFO] CPU executed " + steps + " steps; halted=" + cpu.isHalted());
    }

    // Step 4: End-to-end integration
    public static void main(String[] args) throws Exception {
        Path asmPath = Paths.get("Program1.txt");
        System.out.println("[STEP] Reading: " + asmPath);

        List<Instruction> instructions = readAssemblyFile(asmPath);
        System.out.println("[STEP] Encoding instructions...");
        List<Integer> machineCode = encodeInstructions(instructions);

        System.out.println("[STEP] Sending to memory...");
        loadIntoMemory(machineCode);

        System.out.println("[STEP] Integration test complete!");
    }
}