import java.io.*;
import java.util.*;

public class Assembler {
    private Map<String, Integer> symbolTable = new HashMap<>();     // labels → addresses

    // store both machine value and original source line
    class MemEntry {
        int value;
        String sourceLine;

        MemEntry(int value, String sourceLine) {
            this.value = value;
            this.sourceLine = sourceLine;
        }
    }

    private Map<Integer, MemEntry> memory = new LinkedHashMap<>(); // address → machine code
    private int locationCounter = 0;

    // -------- Opcode Table (from ISA PDF) --------
    private static final Map<String, Integer> OPCODES = new HashMap<>();
    static {
        // Miscellaneous
        OPCODES.put("HLT", 0b000000);

        // Load / Store
        OPCODES.put("LDR", 0b000001);
        OPCODES.put("STR", 0b000010);
        OPCODES.put("LDA", 0b000011);
        OPCODES.put("LDX", 0b100001);
        OPCODES.put("STX", 0b100010);

        // Transfer
        OPCODES.put("JZ",  0b001000);
        OPCODES.put("JNE", 0b001001);
        OPCODES.put("JCC", 0b001010);
        OPCODES.put("JMA", 0b001011);
        OPCODES.put("JSR", 0b001100);
        OPCODES.put("RFS", 0b001101);
        OPCODES.put("SOB", 0b001110);
        OPCODES.put("JGE", 0b001111);

        // Arithmetic / Logic
        OPCODES.put("AMR", 0b000100);
        OPCODES.put("SMR", 0b000101);
        OPCODES.put("AIR", 0b000110);
        OPCODES.put("SIR", 0b000111);

        // Register-to-Register ops
        OPCODES.put("MLT", 0b111000);
        OPCODES.put("DVD", 0b111001);
        OPCODES.put("TRR", 0b111010);
        OPCODES.put("AND", 0b111011);
        OPCODES.put("ORR", 0b111100);
        OPCODES.put("NOT", 0b111101);

        // Shift/Rotate
        OPCODES.put("SRC", 0b011001);
        OPCODES.put("RRC", 0b011010);

        // I/O
        OPCODES.put("IN",  0b110001);
        OPCODES.put("OUT", 0b110010);
        OPCODES.put("CHK", 0b110011);
    }

    // -------- Pass 1: Build Symbol Table --------
    private void pass1(List<String> lines) {
        locationCounter = 0;
        for (String line : lines) {
            Instruction instr = parseLine(line);

            if (instr == null) continue;

            if (instr.label != null && !instr.label.isEmpty()) {
                symbolTable.put(instr.label, locationCounter);
            }

            if ("LOC".equalsIgnoreCase(instr.opcode)) {
                locationCounter = Integer.parseInt(instr.operands[0]);
                // LOC doesn't take up memory space, just changes location counter
            } else if (instr.opcode != null && !instr.opcode.isEmpty()) {
                locationCounter++; // every DATA or instruction occupies memory
            }
        }
    }

    // -------- Pass 2: Generate Machine Code --------
    private void pass2(List<String> lines) {
        locationCounter = 0;
        for (String line : lines) {
            Instruction instr = parseLine(line);
            if (instr == null) continue;

            if ("LOC".equalsIgnoreCase(instr.opcode)) {
                locationCounter = Integer.parseInt(instr.operands[0]);
                // LOC doesn't generate machine code, just changes location counter
            } else if ("DATA".equalsIgnoreCase(instr.opcode)) {
                int value;
                String operand = instr.operands[0];
                if (symbolTable.containsKey(operand)) {
                    value = symbolTable.get(operand);
                } else {
                    value = Integer.parseInt(operand);
                }
                memory.put(locationCounter, new MemEntry(value, line));
                locationCounter++;
            } else if (instr.opcode != null && !instr.opcode.isEmpty()) { // real instruction
                int code = assembleInstruction(instr);
                memory.put(locationCounter, new MemEntry(code, line));
                locationCounter++;
            }
        }
    }

    // -------- Assemble One Instruction --------
    private int assembleInstruction(Instruction instr) {
        String opcode = instr.opcode.toUpperCase();
        int opcodeBits = OPCODES.getOrDefault(opcode, -1);
        if (opcodeBits == -1) {
            throw new IllegalArgumentException("Unknown opcode: " + opcode);
        }

        // Special handling for SRC / RRC
        if (opcode.equals("SRC") || opcode.equals("RRC")) {
            // Ensure we have exactly 4 operands
            if (instr.operands.length != 4) {
                throw new IllegalArgumentException(opcode + " requires exactly 4 operands: register, count, L/R, A/L");
            }
            
            int r = parseOperand(instr.operands[0]);       // register
            int count = parseOperand(instr.operands[1]);   // shift/rotate count
            int lr = parseOperand(instr.operands[2]);      // L/R bit
            int al = parseOperand(instr.operands[3]);      // A/L bit

            // Validate ranges
            if (r < 0 || r > 3) throw new IllegalArgumentException("Register must be 0-3");
            if (count < 0 || count > 15) throw new IllegalArgumentException("Count must be 0-15 for " + opcode);
            if (lr < 0 || lr > 1) throw new IllegalArgumentException("L/R bit must be 0 or 1");
            if (al < 0 || al > 1) throw new IllegalArgumentException("A/L bit must be 0 or 1");

            int instruction = 0;
            instruction |= (opcodeBits & 0x3F) << 10;  // opcode (6 bits)
            instruction |= (r & 0x03) << 8;            // register (2 bits)
            instruction |= (al & 0x01) << 7;           // A/L (1 bit) - bit 7
            instruction |= (lr & 0x01) << 6;           // L/R (1 bit) - bit 6  
            instruction |= (count & 0x0F);             // count (4 bits) - bits 3-0

            return instruction;
        }

        // Default path (for other instructions)
        int r = 0, ix = 0, address = 0, i = 0;

        if (instr.operands.length > 0 && !instr.operands[0].isEmpty())
            r = parseOperand(instr.operands[0]);

        if (instr.operands.length > 1 && !instr.operands[1].isEmpty())
            ix = parseOperand(instr.operands[1]);

        if (instr.operands.length > 2 && !instr.operands[2].isEmpty()) {
            String addr = instr.operands[2];
            if (symbolTable.containsKey(addr)) {
                address = symbolTable.get(addr);
            } else {
                address = parseOperand(addr);
            }
        }

        if (instr.operands.length > 3 && !instr.operands[3].isEmpty())
            i = parseOperand(instr.operands[3]);

        int instruction = 0;
        instruction |= (opcodeBits & 0x3F) << 10; // 6 bits
        instruction |= (r & 0x03) << 8;           // 2 bits
        instruction |= (ix & 0x03) << 6;          // 2 bits
        instruction |= (i & 0x01) << 5;           // 1 bit
        instruction |= (address & 0x1F);          // 5 bits

        return instruction;
    }

    // Helper method to parse operand as integer
    private int parseOperand(String operand) {
        if (operand == null || operand.trim().isEmpty()) {
            return 0;
        }
        operand = operand.trim();
        if (symbolTable.containsKey(operand)) {
            return symbolTable.get(operand);
        }
        return Integer.parseInt(operand);
    }

    // -------- Parse One Line --------
    private Instruction parseLine(String line) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith(";")) return null;

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
                // Split by comma and trim each operand
                String[] rawOperands = parts[1].split(",");
                operands = new String[rawOperands.length];
                for (int j = 0; j < rawOperands.length; j++) {
                    operands[j] = rawOperands[j].trim();
                }
            }
        }

        return new Instruction(label, opcode, operands, comment);
    }

    // -------- Write Output Files --------
    private void writeOutputFiles(String listingFile, String loadFile, List<String> source) throws IOException {
        new File(listingFile).getParentFile().mkdirs();
        new File(loadFile).getParentFile().mkdirs();

        try (PrintWriter listOut = new PrintWriter(new FileWriter(listingFile));
             PrintWriter loadOut = new PrintWriter(new FileWriter(loadFile))) {

            for (Map.Entry<Integer, MemEntry> entry : memory.entrySet()) {
                int addr = entry.getKey();
                MemEntry mem = entry.getValue();

                // Listing: octal + original source
                listOut.printf("%06o %06o %s\n", addr, mem.value, mem.sourceLine);

                // Load: only octal
                loadOut.printf("%06o %06o\n", addr, mem.value);
            }
        }
    }

    // -------- Main --------
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java Assembler <sourcefile.asm>");
            return;
        }

        String sourceFile = args[0];
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

        assembler.writeOutputFiles("generated/listing.txt", "generated/load.txt", lines);
        System.out.println("Assembly complete! Check listing.txt and load.txt");
    }
}

// -------- Instruction Class --------
class Instruction {
    String label;
    String opcode;
    String[] operands;
    String comment;

    Instruction(String label, String opcode, String[] operands, String comment) {
        this.label = label;
        this.opcode = opcode;
        this.operands = operands;
        this.comment = comment;
    }
}