#!/bin/bash

# change this path to your JavaFX SDK lib directory
PATH_TO_FX="D:\JavaFx\javafx-sdk-25\lib"

mkdir -p temp/classes/com/gwu/assembler
mkdir -p temp/classes/com/gwu/simulator

# Copy resources
cp src/main/resources/com/gwu/assembler/SGUI.fxml temp/classes/com/gwu/assembler/
cp src/main/resources/com/gwu/assembler/style.css temp/classes/com/gwu/assembler/

# Compile Java sources
javac --module-path "$PATH_TO_FX" --add-modules javafx.controls,javafx.fxml \
    -d temp/classes \
    src/main/java/com/gwu/assembler/*.java \
    src/main/java/com/gwu/simulator/*.java

# Create JAR with manifest
jar cfm Simulator.jar manifest.txt -C temp/classes .

# Clean up
rm -rf temp

echo "Created JAR file: Simulator.jar"