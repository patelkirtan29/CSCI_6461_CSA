package com.gwu.simulator;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CPU {
    private boolean halted;
    private Memory memory;
    // Optional UI/device hooks for I/O
    private Supplier<Integer> consoleInputSupplier; // supplies next console input (octal int)
    private Consumer<String> printerConsumer;       // consumes text lines for printer/console output

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

    // Wiring methods for I/O without coupling to UI classes
    public void setConsoleInputSupplier(Supplier<Integer> supplier) {
        this.consoleInputSupplier = supplier;
    }

    public void setPrinterConsumer(Consumer<String> consumer) {
        this.printerConsumer = consumer;
    }

    public void run(Runnable updateDisplay) {
        Thread runThread = new Thread(() -> {
            while (!isHalted()) {
                step();
                updateDisplay.run();
                try {
                    Thread.sleep(100); // Faster execution - 100ms per instruction
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        runThread.setDaemon(true);
        runThread.start();
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
        System.out.println("Executing opcode: " + opcode);
        
        switch (opcode) {
            case 0:  // HLT
                executeHaltInstruction();
                break;
            case 1:  // LDR
            case 2:  // STR
            case 3:  // LDA
            case 33: // LDX
            case 34: // STX
                executeLoadStoreInstruction();
                break;
            case 4:  // AMR
                executeArithmeticMemory('+');
                break;
            case 5:  // SMR
                executeArithmeticMemory('-');
                break;
            case 6:  // AIR
                executeArithmeticImmediate('+');
                break;
            case 7:  // SIR
                executeArithmeticImmediate('-');
                break;
            case 10: // JZ
            case 11: // JNE
            case 12: // JCC
            case 13: // JMA
            case 14: // JSR
            case 15: // RFS
            case 16: // SOB
            case 17: // JGE
                executeJumpInstruction(opcode);
                break;
            case 20: // MLT
            case 21: // DVD
            case 22: // TRR
            case 23: // AND
            case 24: // ORR
            case 25: // NOT
                executeArithmeticRegister(opcode);
                break;
            case 31: // SRC
            case 32: // RRC
                executeShiftRotate(opcode);
                break;
            case 61: // IN
            case 62: // OUT
                executeIO(opcode);
                break;
            default:
                setMFR(1); // Set illegal opcode fault
                System.out.println("Illegal opcode: " + opcode);
                halt();
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
        System.out.println("Load/Store Instruction" + opcode + " r" + r + " ix" + ix + " i" + i + " adr" + address);
        if (opcode == 1) { // load register from memory
            int ea = getEA(i, ix, address);
            System.out.println("Effective Address: " + ea);
            readMemory(ea);
            setGPR(r, MBR);
        } else if (opcode == 2) {
            int ea = getEA(i, ix, address);
            int valueToWrite = getGPR(r);
            writeMemory(ea, valueToWrite);
        } else if (opcode == 3) {
            int ea = getEA(i, ix, address);
            setGPR(r, ea);
        } else if (opcode == 33) {
            int ea = getEA(i, 0, address);
            readMemory(ea);
            setIXR(ix, MBR);
        } else if (opcode == 34) {
            int ea = getEA(i, 0, address);
            int valueToWrite = getIXR(ix);
            writeMemory(ea, valueToWrite);
        }
    }

    public void manual_load() {
        System.out.println("Manual load from address: " + MAR);
        readMemory(MAR);
    }

    public void manual_load_plus() {
        readMemory(MAR);
        setMAR(MAR + 1);
    }

    public void manual_store() {
        System.out.println("Manual store to address: " + MAR);
        System.out.println("Manual store to value: " + MBR);
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

    public boolean isHalted() {
        return halted;
    }

    public void halt() {
        halted = true;
    }

    public void unhalt() {
        halted = false;
    }

    public void setPC(int value) {
        PC = value & 0xFFF;
    }

    public int getPC() {
        return PC;
    }

    public void setIR(int value) {
        IR = value & 0xFFFF;
    }

    public int getIR() {
        return IR;
    }

    public void setMAR(int value) {
        MAR = value & 0xFFF;
    }

    public int getMAR() {
        return MAR;
    }

    public void setMBR(int value) {
        MBR = value & 0xFFFF;
    }

    public int getMBR() {
        return MBR;
    }

    public void setCC(int value) {
        CC = value & 0xF;
    }

    public int getCC() {
        return CC;
    }

    public void setMFR(int value) {
        MFR = value & 0xF;
    }

    public int getMFR() {
        return MFR;
    }

    public void setGPR(int i, int value) {
        GPR[i] = value & 0xFFFF;
    }

    public int getGPR(int i) {
        return GPR[i];
    }

    public void setIXR(int i, int value) {
        IXR[i - 1] = value & 0xFFFF;
    }

    public int getIXR(int i) {
        return IXR[i - 1];
    }

    private void readMemory(int address) {
        try {
            setMAR(address);
            int content = memory.getValueAt(MAR);
            setMBR(content);
        } catch (IllegalArgumentException ex) {
            // Memory bounds fault: set MFR bit (use 0x4) and halt
            setMFR(getMFR() | 0x4);
            System.err.println("Memory read fault: " + ex.getMessage());
            halt();
        }
    }

    private void writeMemory(int address, int value) {
        try {
            setMAR(address);
            setMBR(value);
            memory.setValueAt(MAR, (short) MBR);
        } catch (IllegalArgumentException ex) {
            // Memory bounds fault: set MFR bit (use 0x4) and halt
            setMFR(getMFR() | 0x4);
            System.err.println("Memory write fault: " + ex.getMessage());
            halt();
        }
    }

    private int getEA(int i, int ix, int address) {
        int ea = (ix == 0) ? address : address + (short) getIXR(ix);
        if (i == 1) {
            readMemory(ea);
            ea = MBR;
        }
        return ea;
    }

    private void executeArithmeticMemory(char op) {
        int r = (IR >> 8) & 0x3;
        int ix = (IR >> 6) & 0x3;
        int i = (IR >> 5) & 1;
        int address = IR & 0x1F;
        
        int ea = getEA(i, ix, address);
        readMemory(ea);
        int value = MBR;
        
        if (op == '+') {
            value = getGPR(r) + value;
        } else {
            value = getGPR(r) - value;
        }
        
        setGPR(r, value);
        updateArithmeticFlags(value);
    }

    private void executeArithmeticImmediate(char op) {
        int r = (IR >> 8) & 0x3;
        int immediate = IR & 0xFF;
        
        // Sign extend the 8-bit immediate value
        if ((immediate & 0x80) != 0) {
            immediate |= 0xFF00;
        }
        
        int value;
        if (op == '+') {
            value = getGPR(r) + immediate;
        } else {
            value = getGPR(r) - immediate;
        }
        
        setGPR(r, value);
        updateArithmeticFlags(value);
    }

    private void executeArithmeticRegister(int opcode) {
        int rx = (IR >> 8) & 0x3;
        int ry = (IR >> 6) & 0x3;
        
        switch (opcode) {
            case 20: // MLT
                int result = getGPR(rx) * getGPR(ry);
                setGPR(rx, result & 0xFFFF);
                setGPR(rx + 1, (result >> 16) & 0xFFFF);
                break;
                
            case 21: // DVD
                if (getGPR(ry) == 0) {
                    setMFR(2); // Divide by zero fault
                    halt();
                    return;
                }
                int quotient = getGPR(rx) / getGPR(ry);
                int remainder = getGPR(rx) % getGPR(ry);
                setGPR(rx, quotient);
                setGPR(rx + 1, remainder);
                break;
                
            case 22: // TRR
                setCC((getGPR(rx) == getGPR(ry)) ? 1 : 0);
                break;
                
            case 23: // AND
                setGPR(rx, getGPR(rx) & getGPR(ry));
                break;
                
            case 24: // ORR
                setGPR(rx, getGPR(rx) | getGPR(ry));
                break;
                
            case 25: // NOT
                setGPR(rx, ~getGPR(rx));
                break;
        }
    }

    private void executeJumpInstruction(int opcode) {
        int r = (IR >> 8) & 0x3;
        int ix = (IR >> 6) & 0x3;
        int i = (IR >> 5) & 1;
        int address = IR & 0x1F;
        
        int ea = getEA(i, ix, address);
        
        switch (opcode) {
            case 10: // JZ
                if (getGPR(r) == 0) setPC(ea);
                break;
                
            case 11: // JNE
                if (getGPR(r) != 0) setPC(ea);
                break;
                
            case 12: // JCC
                if ((getCC() & (1 << r)) != 0) setPC(ea);
                break;
                
            case 13: // JMA
                setPC(ea);
                break;
                
            case 14: // JSR
                setGPR(3, PC);  // Store return address in R3
                setPC(ea);
                break;
                
            case 15: // RFS
                int returnValue = IR & 0xFFFF;
                setGPR(0, returnValue);  // Store return value in R0
                setPC(getGPR(3));  // Return to address stored in R3
                break;
                
            case 16: // SOB
                setGPR(r, getGPR(r) - 1);
                if (getGPR(r) > 0) setPC(ea);
                break;
                
            case 17: // JGE
                if (getGPR(r) >= 0) setPC(ea);
                break;
        }
    }

    private void executeShiftRotate(int opcode) {
        int r = (IR >> 8) & 0x3;
        int al = (IR >> 7) & 0x1;  // 0 for left, 1 for right
        int lr = (IR >> 6) & 0x1;  // 0 for logical, 1 for arithmetic
        int count = IR & 0x1F;
        
        int value = getGPR(r);
        
        if (opcode == 31) { // SRC
            if (al == 0) { // Left shift
                if (lr == 0) { // Logical
                    value = value << count;
                } else { // Arithmetic
                    value = value << count;
                }
            } else { // Right shift
                if (lr == 0) { // Logical
                    value = value >>> count;
                } else { // Arithmetic
                    value = value >> count;
                }
            }
        } else { // RRC
            for (int i = 0; i < count; i++) {
                if (al == 0) { // Rotate left
                    int msb = (value >> 15) & 1;
                    value = ((value << 1) | msb) & 0xFFFF;
                } else { // Rotate right
                    int lsb = value & 1;
                    value = ((value >> 1) | (lsb << 15)) & 0xFFFF;
                }
            }
        }
        
        setGPR(r, value);
    }

    private void executeIO(int opcode) {
        int devid = IR & 0x1F;
        int r = (IR >> 6) & 0x3;
        
        switch (opcode) {
            case 61: // IN
                if (devid == 0 && consoleInputSupplier != null) { // Console keyboard
                    int input = consoleInputSupplier.get();
                    // If input is -1, it means no input available - wait by decrementing PC
                    if (input == -1) {
                        setPC(PC - 1); // Retry this instruction next cycle
                    } else {
                        setGPR(r, input);
                    }
                }
                break;
                
            case 62: // OUT
                if (devid == 1 && printerConsumer != null) { // Console printer
                    // Output as octal per UI convention
                    String text = String.format("%o", getGPR(r));
                    printerConsumer.accept(text);
                }
                break;
        }
    }

    private void updateArithmeticFlags(int result) {
        int cc = 0;
        
        if (result < 0) cc |= 0x8;      // Negative
        if (result == 0) cc |= 0x4;     // Zero
        if ((result & 0x10000) != 0) cc |= 0x2;  // Overflow
        // Divider fault would be handled separately
        
        setCC(cc);
    }
};