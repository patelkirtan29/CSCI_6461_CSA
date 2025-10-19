# CSCI_6461_CSA

## Description
This repository contains a Java implementation of an assembler and a simulator for the Computer System Architecture course (CSCI_6461) at George Washington University. The project converts assembly source files into binary/machine code (assembler) and executes that code in a simulated CPU/memory environment (simulator).

## Prerequisites
- Java Development Kit (JDK) 24 or higher
- JavaFX SDK 25

## Build
```bash
# Use build_jar.sh script for compiling and packaging simulator into a JAR file.
# Update "PATH_TO_FX" variable to your JavaFX library path in build_jar.sh
bash build_jar.sh
```

## Run
By running the following command, you run the simulator.
```bash
# update PATH_TO_FX to your JavaFX library path in run_sim_jar.sh
bash run_sim_jar.sh
# or you can run
java --module-path "$PATH_TO_FX" --add-modules javafx.controls,javafx.fxml -jar Simulator.jar
```

## Testing
There is a load file containing only Load/Store instructions, "test/AL_load.txt".
You can load it in Simulator by inputting the path to the file, and pressing "IPL" button.
