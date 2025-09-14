import java.io.*;
import java.util.*;

public class Assembler {
    private Map<String, Integer> symbolTable = new HashMap<>();     // labels → addresses
    private Map<Integer, Integer> memory = new LinkedHashMap<>();   // address → machine code
    private int locationCounter = 0;

    // -------- Opcode Table (from your ISA PDF) --------
    private static final Map<String, Integer> OPCODES = new HashMap<>();
    static {
        // Miscellaneous
        OPCODES.put("HLT", 0b000000);
        OPCODES.put("TRAP", 0b011000); // not needed until Part III

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

        // Floating point / vector (not needed until Part IV)
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
            } else {
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
            } else if ("DATA".equalsIgnoreCase(instr.opcode)) {
                int value;
                if (symbolTable.containsKey(instr.operands[0])) {
                    value = symbolTable.get(instr.operands[0]);
                } else {
                    value = Integer.parseInt(instr.operands[0]);
                }
                memory.put(locationCounter, value);
                locationCounter++;
            } else { // real instruction
                int code = assembleInstruction(instr);
                memory.put(locationCounter, code);
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
        instruction |= (opcodeBits & 0x3F) << 10; // 6 bits
        instruction |= (r & 0x03) << 8;           // 2 bits
        instruction |= (ix & 0x03) << 6;          // 2 bits
        instruction |= (i & 0x01) << 5;           // 1 bit
        instruction |= (address & 0x1F);          // 5 bits

        return instruction;
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
                operands = parts[1].split(",");
                for (int j = 0; j < operands.length; j++) {
                    operands[j] = operands[j].trim();
                }
            }
        }

        return new Instruction(label, opcode, operands, comment);
    }

    // -------- Write Output Files --------
    private void writeOutputFiles(String listingFile, String loadFile) throws IOException {
        try (PrintWriter listOut = new PrintWriter(new FileWriter(listingFile));
             PrintWriter loadOut = new PrintWriter(new FileWriter(loadFile))) {

            for (Map.Entry<Integer, Integer> entry : memory.entrySet()) {
                int addr = entry.getKey();
                int value = entry.getValue();

                // Listing file
                listOut.printf("%06o %06o\n", addr, value);

                // Load file
                loadOut.printf("%06o %06o\n", addr, value);
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

        assembler.writeOutputFiles("listing.txt", "load.txt");
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
