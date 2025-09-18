package com.gwu.assembler;

import java.util.HashMap;
import java.util.Map;

public class Opcode {
    public static final Map<String, Integer> OPCODES = new HashMap<>();

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
        OPCODES.put("JZ", 0b001000);
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

        // Multiply/Divide and Logical Operations
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
        OPCODES.put("IN", 0b110001);
        OPCODES.put("OUT", 0b110010);
        OPCODES.put("CHK", 0b110011);

        // Floating point / vector (not needed until Part IV)
    }

}
