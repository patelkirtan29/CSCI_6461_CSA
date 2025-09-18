#!/bin/bash

# create bin and dist folders
mkdir -p bin dist

# compile Assembler
javac -d bin src/Assembler.java
# create JAR
jar cfm dist/Assembler.jar manifest.txt -C bin .
