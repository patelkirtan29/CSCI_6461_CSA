#!/bin/bash

echo "---- Testing shift/rotate ----"
java -jar Assembler.jar 1 test
echo ""

echo "---- Testing IO operations ----"
java -jar Assembler.jar 2 test
echo ""

echo "---- Testing Arithmetic Operations ----"
java -jar Assembler.jar 3 test
echo ""

echo "---- Testing Load Store Opertaions (Memory to Register) ----"
java -jar Assembler.jar 4 test
echo ""

echo "---- Testing Multiply/Divide and Logical Operations (Register to Register) ----"
java -jar Assembler.jar 5 test
echo ""

echo "---- Testing Load Store Operations (Register to Register) ----"
java -jar Assembler.jar 6 test
echo ""
