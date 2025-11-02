# CPU Simulator User Manual

## Table of Contents
1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [GUI Overview](#gui-overview)
4. [Basic Operations](#basic-operations)
5. [Running Program1 (Search Demo)](#running-program1-search-demo)
6. [Advanced Features](#advanced-features)
7. [Troubleshooting](#troubleshooting)

---

## Introduction

This is a JavaFX-based CPU simulator with a graphical interface for executing assembly programs. The simulator features:
- 16-bit architecture with 4 GPRs and 3 Index Registers
- 2048-word memory with 16-line FIFO cache
- Console I/O for interactive programs
- IPL (Initial Program Load) for automatic program loading
- Single-step and continuous execution modes

---

## Getting Started

### Prerequisites
- Java JDK 11 or higher
- JavaFX SDK 25.0.1 (or compatible version)

### Running the Simulator

#### Windows (PowerShell):
```powershell
cd "path\to\CSCI_6461_CSA"
$env:PATH_TO_FX="C:\path\to\javafx-sdk-25.0.1\lib"
javac --module-path $env:PATH_TO_FX --add-modules javafx.controls,javafx.fxml -d bin/classes src/main/java/com/gwu/simulator/*.java src/main/java/com/gwu/assembler/*.java
Copy-Item -Force src\main\resources\com\gwu\assembler\SGUI.fxml bin\classes\com\gwu\assembler\
Copy-Item -Force src\main\resources\com\gwu\assembler\style.css bin\classes\com\gwu\assembler\
java --module-path $env:PATH_TO_FX --add-modules javafx.controls,javafx.fxml --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED -cp bin/classes com.gwu.assembler.SGUI
```

#### Linux/Mac (Bash):
```bash
cd path/to/CSCI_6461_CSA
export PATH_TO_FX=/path/to/javafx-sdk-25.0.1/lib
javac --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml -d bin/classes src/main/java/com/gwu/simulator/*.java src/main/java/com/gwu/assembler/*.java
cp -f src/main/resources/com/gwu/assembler/SGUI.fxml bin/classes/com/gwu/assembler/
cp -f src/main/resources/com/gwu/assembler/style.css bin/classes/com/gwu/assembler/
java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED -cp bin/classes com.gwu.assembler.SGUI
```

---

## GUI Overview

### Left Panel - Registers
**General Purpose Registers (R0-R3)**
- Display current values in octal
- Click the button next to each register to update from "Octal Input"

**Index Registers (X1-X3)**
- Used for address indexing
- Update via "Octal Input" field + corresponding button

**Control Registers**
- **PC (Program Counter)**: Current instruction address
  - **Editable**: Click field, type octal address, press Enter
- **MAR (Memory Address Register)**: Current memory address
- **MBR (Memory Buffer Register)**: Memory data buffer
- **IR (Instruction Register)**: Currently executing instruction
- **CC (Condition Code)**: 4-bit status flags
- **MFR (Machine Fault Register)**: Error/fault indicators

### Center Panel - Controls
**Execution Controls**
- **Single Step**: Execute one instruction and stop
- **Run**: Execute continuously until HLT or error
- **Halt**: Stop execution immediately
- **IPL**: Initial Program Load - loads program from file and sets PC to 100 (octal)

**Memory Operations**
- **Load**: Read memory at MAR into MBR
- **Load+**: Read memory and increment MAR
- **Store**: Write MBR to memory at MAR
- **Store+**: Write and increment MAR

**Input Controls**
- **Octal Input**: Enter octal values (0-7 only) for registers
- **Program File**: Specify program filename (default: Program1.txt)

### Right Panel - Output
- **Cache Content**
- Shows 16 cache lines labeled 00–15 (decimal)
- Format: `Line: Tag Value`
- `------` indicates invalid/empty line

**Printer Output**
- Program output from OUT instructions
- Console messages (IPL status, input confirmations)
- Labeled results for Program1 (search/closest values)

**Console Input**
- Enter octal values for IN instructions
- Press Enter or click button to queue input
- Multiple values can be queued before running

---

## Basic Operations

### 1. Loading a Program
1. Enter program filename in "Program File" field (e.g., `Program1.txt`)
2. Click **IPL** button
3. Check "Printer Output" for success message
4. PC will be set to 100 (octal) automatically

### 2. Single-Step Execution
1. Load a program using IPL
2. Click **Single Step** to execute one instruction
3. Watch registers update after each step
4. Repeat to step through the program

### 3. Continuous Execution
1. Load a program using IPL
2. Click **Run** to execute until HLT
3. Use **Halt** to stop execution early if needed

### 4. Manual Register Updates
1. Type octal value in "Octal Input" field
2. Click button next to target register
3. Value updates immediately

### 5. Manual Memory Access
1. Enter address in octal via PC or MAR controls
2. Use **Load** to read from memory
3. Use **Store** to write MBR to memory

---

## Running Program1 (Search Demo)

**Program1** demonstrates a search algorithm that finds the closest value to a search target in a list of 20 numbers.

### Step-by-Step Instructions

#### 1. Load Program1
- Click **IPL** (Program1.txt is default)
- Wait for "Program loaded successfully" in Printer Output

#### 2. Queue Input Values
Enter **21 octal values** in Console Input (one at a time):
- **First 20 values**: Your list of numbers to search
- **21st value**: The search target

**Example:**
```
1 ← Press Enter (queues first list value)
2 ← Press Enter
3 ← Press Enter
...
20 ← Press Enter (20th list value)
5 ← Press Enter (search target)
```

Each input shows: `Input queued: X`

#### 3. Run the Program
- After all 21 inputs are queued, click **Run**
- Program executes automatically
- Wait for execution to complete (PC stops changing)

#### 4. View Results
Scroll to bottom of "Printer Output":
```
1        ← Your 20 list values
2        
3
...
20
5        ← Search value (raw)
5        ← Closest value (raw)
search value: 5     ← Labeled summary
closest value: 5    ← Labeled summary
```

### Example Test Cases

**Test 1: Exact Match**
```
Inputs: 1, 2, 3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 15, 16, 17, 20, 21, 22, 23, 24, 12
Expected: closest value = 12 (exact match)
```

**Test 2: Between Two Values**
```
Inputs: 10, 20, 30, 40, 50, 60, 70, 100, 110, 120, 130, 140, 150, 160, 170, 200, 210, 220, 230, 240, 55
Expected: closest value = 50 or 60 (both distance 5 from 55)
```

**Test 3: Boundary Case**
```
Inputs: 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 100
Expected: closest value = 1 (all values same, smallest diff = 77 octal)
```

---

## Advanced Features

### PC Direct Editing
You can edit the PC field directly:
1. Click on the PC text field
2. Delete existing value
3. Type new octal address
4. Press **Enter** to apply
5. PC updates immediately (no button needed)

### Cache Monitoring
- Watch cache populate as program accesses memory
- FIFO replacement: oldest entries evicted when full
- 16 lines (00-17 octal) hold most recently used addresses

### Octal/Binary Conversion
- Octal Input field shows binary equivalent automatically
- All register/memory values displayed in octal (base 8)
- Valid octal digits: 0-7 only

---

## Troubleshooting

### Problem: "Program file not found"
**Solution**: 
- Ensure `Program1.txt` is in the same directory as the simulator
- Check filename spelling (case-sensitive on Linux/Mac)
- Verify file encoding is ASCII (not UTF-16)

### Problem: Program doesn't execute after Run
**Possible causes**:
1. **Inputs not queued**: Enter all 21 values before clicking Run
2. **Program halted**: PC shows HLT instruction (000000)
3. **Waiting for input**: If IN instruction but no queued input

**Solution**: Click IPL to reload, re-queue all inputs, then Run

### Problem: Only "Input queued" messages appear
**Solution**: 
- This is normal - it confirms inputs are queued
- Click **Run** after all inputs are queued
- Actual program output appears after execution completes

### Problem: PC field won't accept input
**Solution**:
- Only octal digits (0-7) are allowed
- Clear field completely before typing
- Press Enter after typing new value

### Problem: Wrong closest value returned
**Check**:
1. Count inputs - must be exactly 21 (20 list + 1 search)
2. Verify all values are octal (no 8 or 9 digits)
3. Check for duplicate inputs (may affect expected result)
4. Try test case with exact match to verify logic

### Problem: Cache shows less than 20 values
**This is normal**:
- Cache holds only 16 lines (not all 20 list values)
- FIFO replacement evicts oldest entries
- Program still executes correctly

---

## Program Format Specification

### Assembly Program File Structure
Programs use octal notation with format:
```
ADDRESS VALUE    # COMMENT
001     000024    # N = 20
100     102102    # LDX X1,2
```

- **ADDRESS**: 3-digit octal (000-777)
- **VALUE**: 6-digit octal instruction/data
- **COMMENT**: Optional description (after `#`)

### Instruction Set Summary

**Load/Store**
- `LDR`: Load register from memory
- `STR`: Store register to memory
- `LDA`: Load address into register
- `LDX`: Load index register
- `STX`: Store index register

**Arithmetic**
- `AIR`: Add immediate to register
- `SIR`: Subtract immediate from register
- `AMR`: Add memory to register
- `SMR`: Subtract memory from register

**Control Flow**
- `JZ`: Jump if zero
- `JNE`: Jump if not equal
- `JCC`: Jump if condition code set
- `JMA`: Jump unconditional
- `SOB`: Subtract one and branch
- `HLT`: Halt execution

**I/O**
- `IN`: Input from device to register
- `OUT`: Output register to device

---

## Tips and Best Practices

1. **Always IPL before running**: Ensures clean state and correct PC
2. **Queue all inputs first**: Don't try to enter inputs during Run
3. **Use Single Step for debugging**: See instruction-by-instruction execution
4. **Check MFR after errors**: Non-zero indicates fault type
5. **Monitor cache for performance**: Frequent misses = poor locality
6. **Save interesting test cases**: Document input/output for regression testing

---

## Support and Documentation

- **Source Code**: `src/main/java/com/gwu/`
- **Sample Programs**: `resources/*.asm`
- **Project Guide**: `docs/CSCI_6461_ProjectII_Guide.md`
- **Repository**: https://github.com/patelkirtan29/CSCI_6461_CSA

---

## Quick Reference Card

### Common Octal Values
```
Decimal → Octal
0       → 0
8       → 10
16      → 20
32      → 40
64      → 100
100     → 144
255     → 377
2047    → 3777
```

### Keyboard Shortcuts
- **Enter** in PC field: Apply new program counter
- **Enter** in Console Input: Queue input value
- **IPL button**: Quick program reload

### Status Indicators
- **MFR = 0**: No faults
- **MFR ≠ 0**: Check fault type (see documentation)
- **PC changing**: Program executing
- **PC static**: Program halted or waiting for input

---

*Last Updated: November 1, 2025*
*Version: Project II - Search Algorithm Demo*
