package com.gwu.assembler;

import java.io.*;
import java.util.*;

public class Assembler {
    private final Map<String, Integer> symbolTable = new HashMap<>(); // labels → addresses
    private final Map<Integer, Integer> memory = new LinkedHashMap<>(); // address → machine code (keeps insertion// order)
    private final List<ListingEntry> listingEntries = new ArrayList<>(); // one entry per source line (keeps order)
    private int locationCounter = 0;

    // small helper for output listing
    private static class ListingEntry {
        Integer addr; // null if no addr (e.g., LOC or comment line)
        Integer value; // null if no value
        String sourceLine;

        ListingEntry(Integer addr, Integer value, String sourceLine) {
            this.addr = addr;
            this.value = value;
            this.sourceLine = sourceLine;
        }
    }

    // -------- Pass 1: Build Symbol Table --------
    private void pass1(List<String> lines) {
        locationCounter = 0;
        for (String raw : lines) {
            Instruction instr = parseLine(raw);

            if (instr == null)
                continue;

            // record label -> current location
            if (instr.label != null && !instr.label.isEmpty()) {
                // If LOC sets location later, labels before LOC get current counter
                symbolTable.put(instr.label, locationCounter);
            }

            // if LOC directive -> set locationCounter
            if ("LOC".equalsIgnoreCase(instr.opcode)) {
                if (instr.operands.length > 0 && !instr.operands[0].isEmpty())
                    locationCounter = Integer.parseInt(instr.operands[0]);
            } else {
                // every DATA or instruction occupies memory (HLT and normal instructions)
                locationCounter++;
            }
        }
    }

    // -------- Pass 2: Generate Machine Code and produce ordered listing --------
    private void pass2(List<String> lines) {
        locationCounter = 0;
        memory.clear();
        listingEntries.clear();

        for (String rawLine : lines) {
            Instruction instr = parseLine(rawLine);

            // preserve raw line in listing even if it's a comment or blank
            if (instr == null) {
                listingEntries.add(new ListingEntry(null, null, rawLine));
                continue;
            }

            // LOC directive: print raw line in listing with no addr/value, change
            // locationCounter
            if ("LOC".equalsIgnoreCase(instr.opcode)) {
                listingEntries.add(new ListingEntry(null, null, rawLine));
                if (instr.operands.length > 0 && !instr.operands[0].isEmpty()) {
                    locationCounter = Integer.parseInt(instr.operands[0]);
                }
                continue;
            }

            // DATA directive -> either numeric or symbolic value
            if ("DATA".equalsIgnoreCase(instr.opcode)) {
                int value;
                String operand = instr.operands.length > 0 ? instr.operands[0] : "0";
                if (symbolTable.containsKey(operand)) {
                    value = symbolTable.get(operand);
                } else {
                    value = Integer.parseInt(operand);
                }
                memory.put(locationCounter, value);
                listingEntries.add(new ListingEntry(locationCounter, value, rawLine));
                locationCounter++;
                continue;
            }

            // Normal instruction (including HLT)
            int code = assembleInstruction(instr);
            memory.put(locationCounter, code);
            listingEntries.add(new ListingEntry(locationCounter, code, rawLine));
            locationCounter++;
        }
    }

    // -------- Assemble One Instruction --------
    private int assembleInstruction(Instruction instr) {
        String opcode = instr.opcode.toUpperCase();
        int opcodeBits = Opcode.OPCODES.getOrDefault(opcode, -1);
        if (opcodeBits == -1) {
            throw new IllegalArgumentException("Unknown opcode: " + opcode);
        }

        int r = 0, ix = 0, address = 0, i = 0;

        if (instr.operands.length > 0 && !instr.operands[0].isEmpty())
            r = Integer.parseInt(instr.operands[0]);

        if (instr.operands.length > 1 && !instr.operands[1].isEmpty())
            ix = Integer.parseInt(instr.operands[1]);

        if (instr.operands.length > 2 && !instr.operands[2].isEmpty()) {
            String addr = instr.operands[2];
            if (symbolTable.containsKey(addr)) {
                address = symbolTable.get(addr);
            } else {
                address = Integer.parseInt(addr);
            }
        }

        if (instr.operands.length > 3 && !instr.operands[3].isEmpty())
            i = Integer.parseInt(instr.operands[3]);

        int instruction = 0;
        instruction |= (opcodeBits & 0x3F) << 10; // opcode: 6 bits 
        instruction |= (r & 0x03) << 8; // R: 2 bits 
        instruction |= (ix & 0x03) << 6; // IX: 2 bits 
        instruction |= (i & 0x01) << 5; // I: 1 bit 
        instruction |= (address & 0x1F); // address: 5 bits!
        return instruction;
    }

    // -------- Parse One Line --------
    private Instruction parseLine(String line) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith(";"))
            return null;

        String comment = "";
        if (line.contains(";")) {
            comment = line.substring(line.indexOf(";") + 1).trim();
            line = line.substring(0, line.indexOf(";")).trim();
        }

        String label = null, opcode = null;
        String[] operands = new String[0];

        if (line.contains(":")) {
            String[] parts = line.split(":", 2);
            label = parts[0].trim();
            line = parts[1].trim();
        }

        if (!line.isEmpty()) {
            String[] parts = line.split("\\s+", 2);
            opcode = parts[0].trim();
            if (parts.length > 1) {
                operands = parts[1].split(",");
                for (int j = 0; j < operands.length; j++) {
                    operands[j] = operands[j].trim();
                }
            }
        }

        return new Instruction(label, opcode, operands, comment);
    }

    // -------- Write Output Files --------
    private void writeOutputFiles(String listingFile, String loadFile, List<String> source) throws IOException {
        File listF = new File(listingFile);
        File loadF = new File(loadFile);
        if (listF.getParentFile() != null)
            listF.getParentFile().mkdirs();
        if (loadF.getParentFile() != null)
            loadF.getParentFile().mkdirs();

        try (PrintWriter listOut = new PrintWriter(new FileWriter(listF));
                PrintWriter loadOut = new PrintWriter(new FileWriter(loadF))) {

            for (ListingEntry e : listingEntries) {
                if (e.addr == null) {
                    // LOC, comment or blank — print the source line as-is
                    listOut.printf("%22s\n", e.sourceLine);
                } else {
                    // print octal address, octal value and source
                    listOut.printf("%06o %06o %s\n", e.addr, e.value, e.sourceLine);
                    // load file only contains address/value pairs (in octal)
                    loadOut.printf("%06o %06o\n", e.addr, e.value);
                }
            }
        }
    }

    // -------- Main --------
    public static void main(String[] args) throws IOException {
        String fileName = "resources/sample.asm";
        File sourceFile = new File(fileName);

        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(sourceFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }

        Assembler assembler = new Assembler();
        assembler.pass1(lines);
        assembler.pass2(lines);

        assembler.writeOutputFiles("resources/output/listing.txt", "resources/output/load.txt", lines);
        System.out.println("Assembly complete! Check resources/output/listing.txt and load.txt");
    }
}
