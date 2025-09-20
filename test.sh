#!/bin/bash

echo "Testing shift/rotate"
java -jar Assembler.jar 1
echo "Testing IO operations"
java -jar Assembler.jar 2
echo "Testing Arithmetic Operations"
java -jar Assembler.jar 3
echo "Testing Load Store Opertaions (Memory to Register)"
java -jar Assembler.jar 4
echo "Testing Load Store Operations (Register to Register)"
java -jar Assembler.jar