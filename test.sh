#!/bin/bash

echo "---- Testing shift/rotate ----"
java -jar Assembler.jar 1
echo ""

echo "---- Testing IO operations ----"
java -jar Assembler.jar 2
echo ""

echo "---- Testing Arithmetic Operations ----"
java -jar Assembler.jar 3
echo ""

echo "---- Testing Load Store Opertaions (Memory to Register) ----"
java -jar Assembler.jar 4
echo ""

echo "---- Testing Multiply/Divide and Logical Operations (Register to Register) ----"
java -jar Assembler.jar 5
echo ""

echo "---- Testing Load Store Operations (Register to Register) ----"
java -jar Assembler.jar
echo ""
