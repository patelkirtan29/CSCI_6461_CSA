package com.gwu.assembler;

import com.gwu.simulator.Memory;

public class CPU {
    // Is the machine halted?
    private boolean halted;

    // Machine Memory
    private Memory memory;

    // Registers
    private int PC; // 12-bit Program Counter
    private int IR; // 16-bit Instruction Register
    private int MAR; // 12-bit Memory Address Register
    private int MBR; // 16-bit Memory Buffer Register
    private int CC; // 4-bit Condition Code
    private int MFR; // 4-bit Machine Fault Register
    private int[] GPR = new int[4]; // 16-bit General Purpose Registers
    private int[] IXR = new int[3]; // 16-bit Index Registers, numeration starts from 1

    public CPU(Memory memory) {
        this.memory = memory;
        reset();
    }

    public void run() {
        while (!isHalted()) {
            fetch();
            decodeAndExecute();
        }
    }

    public void step() {
        fetch();
        decodeAndExecute();
    }

    public void fetch() {
        readMemory(PC);
        setIR(MBR);
        setPC(PC + 1);
    }

    public void decodeAndExecute() {
        int opcode = (IR >> 10) & 0x3F;
        if (opcode == 0) {
            executeHaltInstruction();
        }
        else if (opcode == 1 || opcode == 2 || opcode == 3 || opcode == 33 || opcode == 34) {
            executeLoadStoreInstruction();
        }
    }

    public void executeHaltInstruction() {
        halt();
    }

    public void executeLoadStoreInstruction() {
        int opcode = (IR >> 10) & 0x3F;
        int r = (IR >> 8) & 0x3;
        int ix = (IR >> 6) & 0x3;
        int i = (IR >> 5) & 1;
        int address = IR & 0x1F;

        if (opcode == 1) { // load register from memory
            int ea = getEA(i, ix, address);
            readMemory(ea);
            setGPR(r, MBR);
        }
        else if (opcode == 2) { // store register from memory
            int ea = getEA(i, ix, address);
            int valueToWrite = getGPR(r);
            writeMemory(ea, valueToWrite);
        }
        else if (opcode == 3) { // load register with address
            int ea = getEA(i, ix, address);
            setGPR(r, ea);
        }
        else if (opcode == 33) { // Load Index Register from Memory
            int ea = getEA(i, 0, address); // compute ea without ix
            readMemory(ea);
            setIXR(ix, MBR);
        }
        else if (opcode == 34) { // Store Index Register to Memory
            int ea = getEA(i, 0, address); // compute ea without ix
            int valueToWrite = getIXR(ix);
            writeMemory(ea, valueToWrite);
        }
    }

    public void manual_load() {
        readMemory(MAR);
    }
    public void manual_load_plus() {
        readMemory(MAR);
        setMAR(MAR + 1);
    }

    public void manual_store() {
        writeMemory(MAR, MBR);
    }
    public void manual_store_plus() {
        writeMemory(MAR, MBR);
        setMAR(MAR + 1);
    }

    public void reset() {
        halted = false;
        setPC(0);
        setIR(0);
        setMAR(0);
        setMBR(0);
        setCC(0);
        setMFR(0);
        for (int i = 0; i < GPR.length; i++)
            setGPR(i, 0);
        for (int i = 0; i < IXR.length; i++)
            setIXR(i + 1, 0); // IXR indices are 1-based in setIXR
    }

    public boolean isHalted() { return halted; }
    public void halt() { halted = true; }
    public void unhalt() { halted = false; }

    public void setPC(int value) { PC = value & 0xFFF; }
    public int getPC() { return PC; }

    public void setIR(int value) { IR = value & 0xFFFF; }
    public int getIR() { return IR; } 

    public void setMAR(int value) { MAR = value & 0xFFF; }
    public int getMAR() { return MAR; }

    public void setMBR(int value) { MBR = value & 0xFFFF; }
    public int getMBR() { return MBR; }

    public void setCC(int value) { CC = value & 0xF; }
    public int getCC() { return CC; }

    public void setMFR(int value) { MFR = value & 0xF; }
    public int getMFR() { return MFR; }

    public void setGPR(int i, int value) { GPR[i] = value & 0xFFFF; }
    public int getGPR(int i) { return GPR[i]; }

    public void setIXR(int i, int value) { IXR[i - 1] = value & 0xFFFF; }
    public int getIXR(int i) { return IXR[i - 1]; }

    private void readMemory(int address) {
        // Use Memory API: set MAR, perform read(), then get MBR
        memory.setMAR(address);
        memory.read();
        int content = memory.getMBR();
        setMBR(content);
    }
    private void writeMemory(int address, int value) {
        // Use Memory API: set MAR and MBR, then perform write()
        memory.setMAR(address);
    memory.setMBR((short) value);
        memory.write();
    }

    private int getEA(int i, int ix, int address) {
        int ea = (ix == 0) ? address: address + (short)getIXR(ix);
        if (i == 1) {
            readMemory(ea);
            ea = MBR;
        }
        return ea;
    }
};