package com.gwu.assembler;

import java.io.*;
import java.nio.file.*;
import java.util.*;

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
        // Replace this block with real loader API:
        // YeabsiraLoader loader = new YeabsiraLoader();
        // loader.loadWords(machineCode.stream().mapToInt(Integer::intValue).toArray());
        for (int i = 0; i < machineCode.size(); i++) {
            System.out.printf("MEM[%04d] = %s%n", i, Integer.toBinaryString(machineCode.get(i)));
        }
        System.out.println("[INFO] Memory load simulation complete.");
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