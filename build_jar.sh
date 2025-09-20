#!/bin/bash

# create bin and dist folders
mkdir -p bin

# compile Assembler
javac -d bin src/main/java/com/gwu/assembler/*.java
# create JAR
jar cfm Assembler.jar manifest.txt -C bin .

# remove bin folder
rm -r bin