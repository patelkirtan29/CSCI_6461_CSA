# CSCI_6461_CSA

## Description
This repository contains a Java implementation of an assembler and a simulator for the Computer System Architecture course (CSCI_6461) at George Washington University. The project converts assembly source files into binary/machine code (assembler) and executes that code in a simulated CPU/memory environment (simulator).

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 8 or higher

### Commands to run the project

#### To Compile the project 
javac -d bin/classes src/main/java/com/gwu/assembler/*.java

#### To run the binary files
java -cp bin/classes com.gwu.assembler.Assembler

#### To run a specific file
java -cp bin/classes com.gwu.assembler.Assembler 1
pass 1 -> Shift Rotate
pass 2 -> I/O Operations
pass 3 -> Arithmetic Operations
pass 4 -> Load Store Opertaions (Memory to Register)
default -> Load Store Operations (Register to Register)