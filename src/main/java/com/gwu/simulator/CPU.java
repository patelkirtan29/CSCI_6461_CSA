package com.gwu.simulator;

public class CPU {
    private boolean halted;
    private Memory memory;

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

    public void run(Runnable updateDisplay) {
        Thread runThread = new Thread(() -> {
            while (!isHalted()) {
                step();
                updateDisplay.run();
                try {
                    Thread.sleep(500); // Small delay
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
        if (opcode == 0) {
            executeHaltInstruction();
        } else if (opcode == 1 || opcode == 2 || opcode == 3 || opcode == 33 || opcode == 34) {
            executeLoadStoreInstruction();
        } else if (opcode == 8 || opcode == 9 || opcode == 10 || opcode == 11 || opcode == 12 || opcode == 13 || opcode == 14 || opcode == 15) {
            executeTransferInstruction();
        } else if (opcode == 4 || opcode == 5 || opcode == 6 || opcode == 7) {
            executeArithmeticAndLogicalInstruction();
        } else if (opcode == 56 || opcode == 57 || opcode == 58 || opcode == 59 || opcode == 60 || opcode == 61) {
            executeRegisterToRegisterInstruction();
        } else if (opcode == 25 || opcode == 26) {
            executeShiftRotateOperation();
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

    public void executeTransferInstruction() {
        int opcode = (IR >> 10) & 0x3F;
        int r = (IR >> 8) & 0x3;
        int ix = (IR >> 6) & 0x3;
        int i = (IR >> 5) & 1;
        int address = IR & 0x1F;

        int ea = getEA(i, ix, address);

        // PC is already incremented by 1 during fetch, so don't do PC + 1
        if (opcode == 8) {
            if (getGPR(r) == 0) {
                setPC(ea);
            }
        } else if (opcode == 9) {
            if (getGPR(r) != 0) {
                setPC(ea);
            }
        } else if (opcode == 10) {
            int CCbit = (getCC() >> r) & 1;
            if (CCbit == 1) {
                setPC(ea);
            }
        } else if (opcode == 11) {
            setPC(ea);
        } else if (opcode == 12) {
            // PC is already incremented by 1
            setGPR(3, getPC());
            setPC(ea);
        } else if (opcode == 13) {
            setGPR(0, address);
            setPC(getGPR(3));
        } else if (opcode == 14) {
            int rVal = getGPR(r);
            rVal -= 1;
            setGPR(r, rVal);
            if (rVal > 0) {
                setPC(ea);
            }
        } else if (opcode == 15) {
            int rVal = getGPR(r);
            if (rVal >= 0) {
                setPC(ea);
            }
        }
    }

    public void executeArithmeticAndLogicalInstruction() {
        int opcode = (IR >> 10) & 0x3F;
        int r = (IR >> 8) & 0x3;
        int ix = (IR >> 6) & 0x3;
        int i = (IR >> 5) & 1;
        int address = IR & 0x1F;

        int ea = getEA(i, ix, address);
        if (opcode == 4) {
            readMemory(ea);
            int mem = getMBR();
            int result = machineAdd(getGPR(i), mem);
            setGPR(i, result);
        } else if (opcode == 5) {
            readMemory(ea);
            int mem = getMBR();
            int result = machineSub(getGPR(i), mem);
            setGPR(i, result);
        } else if (opcode == 6) {
            int result = machineAdd(getGPR(i), address);
            setGPR(i, result);
        } else if (opcode == 7) {
            int result = machineSub(getGPR(i), address);
            setGPR(i, result);
        }
    }
    
    public void executeRegisterToRegisterInstruction() {
        int opcode = (IR >> 10) & 0x3F;
        int rx = (IR >> 8) & 0x3;
        int ry = (IR >> 6) & 0x3;
        if (opcode == 56) {
            int result = machineMult(getGPR(rx), getGPR(ry));
            int upperHalf = (result >> 16) & 0xFFFF;
            int lowerHalf = result & 0xFFFF;
            setGPR(rx, upperHalf);
            setGPR(rx + 1, lowerHalf);
        } else if (opcode == 57) {
            int result = machineDiv(getGPR(rx), getGPR(ry));
            int quotient = (result >> 16) & 0xFFFF;
            int remainder = result & 0xFFFF;
            setGPR(rx, quotient);
            setGPR(rx + 1, remainder);
        } else if (opcode == 58) {
            machineTRR(getGPR(rx), getGPR(ry));
        } else if (opcode == 59) {
            int result = machineAND(getGPR(rx), getGPR(ry));
            setGPR(rx, result);
        } else if (opcode == 60) {
            int result = machineOR(getGPR(rx), getGPR(ry));
            setGPR(rx, result);
        } else if (opcode == 61) {
            int result = machineNOT(getGPR(rx));
            setGPR(rx, result);
        }
    }

    public void executeShiftRotateOperation() {
        int opcode = (IR >> 10) & 0x3F;
        int r = (IR >> 8) & 0x3;
        int mode = (IR >> 7) & 0x1;
        int dir = (IR >> 6) & 1;
        int count = IR & 0xF;

        if (opcode == 25) {
            int result = machineShiftByCount(getGPR(r), count, dir, mode);
            setGPR(r, result);
        } else if (opcode == 26) {
            int result = machineRotateByCount(getGPR(r), count, dir, mode);
            setGPR(r, result);
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
        setMAR(address);
        int content = memory.getValueAt(MAR);
        setMBR(content);
    }

    private void writeMemory(int address, int value) {
        setMAR(address);
        setMBR(value);
        memory.setValueAt(MAR, (short) MBR);
    }

    private int getEA(int i, int ix, int address) {
        int ea = (ix == 0) ? address : address + (short) getIXR(ix);
        if (i == 1) {
            readMemory(ea);
            ea = MBR;
        }
        return ea;
    }

    private void setCCBit(int i, int bit) {
        int cc = getCC();
        if (bit == 1) {
            setCC(cc | (1 << bit));
        } else {
            setCC(cc & ~(1 << bit));
        }
    }

    private void setOverflow(int bit) {
        setCCBit(0, bit);
    }

    private void setUnderflow(int bit) {
        setCCBit(1, bit);
    }

    private void setDivByZero(int bit) {
        setCCBit(2, bit);
    }

    private void setEqualOrNot(int bit) {
        setCCBit(3, bit);
    }

    private int machineAdd(int a, int b) {
        int intResult = (int)a + (int)b;
        short result = (short)intResult;
        if (intResult > result) {
            setOverflow(1);
        } else {
            setOverflow(0);
        }
        if (intResult < result) {
            setUnderflow(1);
        } else {
            setUnderflow(0);
        }
        return (int)result;
    }

    private int machineSub(int a, int b) {
        int intResult = (int)a - (int)b;
        short result = (short)intResult;
        if (intResult > result) {
            setOverflow(1);
        } else {
            setOverflow(0);
        }
        if (intResult < result) {
            setUnderflow(1);
        } else {
            setUnderflow(0);
        }
        return (int)result;
    }

    private int machineMult(int a, int b) {
        int intResult = (int)a * (int)b;
        short result = (short)intResult;
        if (intResult > result) {
            setOverflow(1);
        } else {
            setOverflow(0);
        }
        if (intResult < result) {
            setUnderflow(1);
        } else {
            setUnderflow(0);
        }
        return intResult;
    }

    private int machineDiv(int a, int b) {
        if (b == 0) {
            setDivByZero(1);
            return 0;
        }
        setDivByZero(0);
        int quotient = a / b;
        int remainder = a % b;
        return (quotient << 16) | remainder;
    }

    private void machineTRR(int a, int b) {
        if (a == b) {
            setEqualOrNot(1);
        } else {
            setEqualOrNot(0);
        }
    }

    private int machineAND(int a, int b) {
        return a & b;
    }

    private int machineOR(int a, int b) {
        return a | b;
    }

    private int machineNOT(int a) {
        return (~a) & 0xFFFF;
    }

    private int machineShiftByCount(int value, int count, int dir, int mode) {
        if (count == 0)
            return value;
        int result = 0;
        if (mode == 0) { // arithmetic shift
            if (dir == 0) { // right
                result = value >> count;
            } else { // left
                result = value << count;
            }
        } else { // logical shift
            if (dir == 0) {
                result = value >>> count;
            } else {
                result = value << count;
            }
        }
        return (short)(result & 0xFFFF);
    }

    private int machineRotateByCount(int value, int count, int dir, int mode) {
        if (count == 0)
            return value;
        int val = value & 0xFFFF;
        if (dir == 0) {
            val = ((val >>> count) | (val << (16 - count))) & 0xFFFF;
        } else {
            val = ((val << count) | (val >>> (16 - count))) & 0xFFFF;
        }
        return val;
    }
};