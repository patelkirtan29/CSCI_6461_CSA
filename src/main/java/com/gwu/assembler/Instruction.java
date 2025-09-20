package com.gwu.assembler;

public class Instruction {
    String label;
    String opcode;
    String[] operands;
    String comment;

    public Instruction(String label, String opcode, String[] operands, String comment) {
        this.label = label;
        this.opcode = opcode;
        this.operands = operands;
        this.comment = comment;
    }
}
