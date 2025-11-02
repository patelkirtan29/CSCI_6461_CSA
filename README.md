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

### Windows quick start (recommended)

This repo includes a Windows batch script configured for your JavaFX SDK path.

1) Ensure JavaFX SDK is available at:

	C:\Users\yabid\Downloads\openjfx-25.0.1_windows-x64_bin-sdk\javafx-sdk-25.0.1\lib

2) From a PowerShell terminal in the project folder, run:

```powershell
# Compile and run the JavaFX simulator (copies FXML/CSS automatically)
.\run_simulator.bat
```

You should see the GUI launch. The Program File field is prefilled with `Program1.txt` and is automatically loaded.

### Program 1 demo

Program1 demonstrates I/O and the cache-backed memory access:
- Reads 20 octal numbers from Console Input (press Enter after each); they will immediately print to the Printer pane.
- Prompts for a single search value (octal).
- Finds the closest value among the 20 inputs.
- Prints the search value and then the closest value (both in octal).

Notes:
- All inputs/outputs are octal. For example, decimal 9 should be entered as `11` (octal).
- Console Input is queued FIFO; you can type all 21 values (20 numbers + search) before pressing Run.
- Use Run for continuous execution or Step to execute one instruction at a time.

## Testing
There is a load file containing only Load/Store instructions, "test/AL_load.txt".
You can load it in Simulator by inputting the path to the file, and pressing "IPL" button.
