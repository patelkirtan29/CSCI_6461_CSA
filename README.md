# CSCI_6461_CSA

## Description
This repository contains a Java implementation of an assembler and a simulator for the Computer System Architecture course (CSCI_6461) at George Washington University. The project converts assembly source files into binary/machine code (assembler) and executes that code in a simulated CPU/memory environment (simulator).

## Prerequisites
- Java Development Kit (JDK) 8 or higher

## Build
```bash
# Use build_jar.sh script for compiling and packaging into a JAR file.
bash build_jar.sh
```

## Run
By running the following command, the assembler generates ***"generated"*** folder with ***"listing.txt"*** and ***"load.txt"*** files for given ***<source file>***.
```bash
# Example: java -jar Assembler.jar sample.asm
java -jar Assembler.jar <source file>
```

## Testing
There are 6 distinct test files:
1) Shift/Rotate (***resources/shift_rorate.asm***)
2) I/O Operations (***resources/IO.asm***)
3) Arithmetic Operations (***resources/MR_LS.asm***)
4) Load Store Opertaions (Memory to Register) (***resources/RM_RR_AL.asm***)
5) Multiply/Divide and Logical Operations (Register to Register) (***resources/reg_to_reg.asm***)
6) Load Store Opertaions (Memory to Register) (***resources/sample.asm***)

For testing each of them:
```bash
# Example: java -jar Assembler.jar 1 test
java -jar Assembler.jar <test number> test
```
For testing all of them:
```bash
bash test.sh
```
The listing and load files for tests would be generated in ***resources/output***. The correctness of generated files could be verified by comparing with files in ***resources/reference_output***.