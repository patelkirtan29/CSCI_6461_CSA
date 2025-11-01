package com.gwu.assembler;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import com.gwu.simulator.CPU;
import com.gwu.simulator.Memory;
import com.gwu.simulator.Cache.CacheLine;

public class SGUIController {
    private CPU cpu;
    private Memory memory;
    private StringBuilder printerBuffer = new StringBuilder();
    // FIFO queue for console input values (octal words)
    private final Deque<Integer> consoleInputQueue = new ArrayDeque<>();

    @FXML private TextField gpr0, gpr1, gpr2, gpr3;
    @FXML private TextField ixr1, ixr2, ixr3;
    @FXML private TextField mar, mbr, pc, ir;
    @FXML private TextField cc, mfr;  // New status registers
    @FXML private TextField octalInput, binary;
    @FXML private TextField programFile;
    @FXML private TextField consoleInput;
    @FXML private TextArea printerOutput;
    @FXML private Button singleStepBtn, runBtn, iplBtn, haltBtn;
    @FXML private Button loadBtn, loadPlusBtn, storeBtn, storePlusBtn;
    @FXML private Button gpr0Btn, gpr1Btn, gpr2Btn, gpr3Btn;
    @FXML private Button ixr1Btn, ixr2Btn, ixr3Btn;
    @FXML private Button pcBtn, marBtn, mbrBtn, irBtn;
    @FXML private TextArea cacheContent;

    @FXML
    public void initialize() {
        memory = new Memory();
        cpu = new CPU(memory);
        // Wire UI I/O to CPU
        cpu.setConsoleInputSupplier(this::readFromConsole);
        cpu.setPrinterConsumer(this::printToOutput);
        // No table; cache content shown in a text area
        setupListeners();
        setupIPLProgram();
        updateDisplays();
    }

    private void setupListeners() {
        // Existing listeners
        octalInput.textProperty().addListener((obs, oldVal, newVal) -> {
            // Allow empty string or valid octal digits
            if (newVal.isEmpty() || newVal.matches("[0-7]+")) {
                updateBinaryDisplay(newVal);
            } else {
                // Revert to old value if invalid
                octalInput.setText(oldVal);
            }
        });

        // Console input handler
        consoleInput.setOnAction(e -> handleConsoleInput());

        // GPR button handlers
        gpr0Btn.setOnAction(e -> updateRegister(val -> cpu.setGPR(0, val)));
        gpr1Btn.setOnAction(e -> updateRegister(val -> cpu.setGPR(1, val)));
        gpr2Btn.setOnAction(e -> updateRegister(val -> cpu.setGPR(2, val)));
        gpr3Btn.setOnAction(e -> updateRegister(val -> cpu.setGPR(3, val)));

        // IXR button handlers
        ixr1Btn.setOnAction(e -> updateRegister(val -> cpu.setIXR(1, val)));
        ixr2Btn.setOnAction(e -> updateRegister(val -> cpu.setIXR(2, val)));
        ixr3Btn.setOnAction(e -> updateRegister(val -> cpu.setIXR(3, val)));

        // Special register button handlers
        pcBtn.setOnAction(e -> updateRegister(cpu::setPC));
        marBtn.setOnAction(e -> updateRegister(cpu::setMAR));
        mbrBtn.setOnAction(e -> updateRegister(cpu::setMBR));
        irBtn.setOnAction(e -> updateRegister(cpu::setIR));

        // Memory operation buttons
        loadBtn.setOnAction(e -> loadFromMemory());
        loadPlusBtn.setOnAction(e -> loadFromMemoryAndIncrement());
        storeBtn.setOnAction(e -> storeToMemory());
        storePlusBtn.setOnAction(e -> storeToMemoryAndIncrement());

        // Button event handlers
        singleStepBtn.setOnAction(e -> handleSingleStep());
        runBtn.setOnAction(e -> handleRun());
        haltBtn.setOnAction(e -> handleHalt());
        iplBtn.setOnAction(e -> handleIPL());
    }

    private void handleConsoleInput() {
        try {
            int value = Integer.parseInt(consoleInput.getText(), 8);  // Parse octal input
            consoleInputQueue.addLast(value);
            printToOutput("Input queued: " + String.format("%o", value));
            consoleInput.clear();
        } catch (NumberFormatException e) {
            printToOutput("Error: Invalid octal number");
        }
    }

    public void printToOutput(String text) {
        if (Platform.isFxApplicationThread()) {
            printerOutput.appendText(text + "\n");
            printerBuffer.append(text).append("\n");
        } else {
            Platform.runLater(() -> printToOutput(text));
        }
    }

    public int readFromConsole() {
        Integer v = consoleInputQueue.pollFirst();
        return v != null ? v : 0;  // Default value if no input available
    }

    private void handleSingleStep() {
        cpu.step();
        updateDisplays();
    }

    private void handleRun() {
        cpu.unhalt(); // Make sure CPU is not halted before running
        cpu.run(() -> {
            Platform.runLater(this::updateDisplays);
        });
    }

    private void handleHalt() {
        cpu.halt();
        updateDisplays();
    }

    private void handleIPL() {
        setupIPLProgram();
        updateDisplays();
    }

    private void setupIPLProgram() {
        String programPath = programFile.getText();
        if (programPath == null || programPath.trim().isEmpty()) {
            printToOutput("No program file specified");
            return;
        }

        try {
            memory.reset(); // Clear memory before loading new program
            memory.loadProgramFromFile(programPath);
            cpu.reset();  // Reset CPU state after loading program
            updateDisplays();
            printToOutput("Program loaded successfully: " + programPath);
        } catch (IOException e) {
            printToOutput("Error loading program: " + e.getMessage());
        }
    }
    
    private void updateDisplays() {
        if (Platform.isFxApplicationThread()) {
            updateDisplaysInternal();
        } else {
            Platform.runLater(this::updateDisplaysInternal);
        }
    }

    private void updateDisplaysInternal() {
        // Update register displays with octal values
        gpr0.setText(String.format("%o", cpu.getGPR(0)));
        gpr1.setText(String.format("%o", cpu.getGPR(1)));
        gpr2.setText(String.format("%o", cpu.getGPR(2)));
        gpr3.setText(String.format("%o", cpu.getGPR(3)));

        // Update IXR displays
        ixr1.setText(String.format("%o", cpu.getIXR(1)));
        ixr2.setText(String.format("%o", cpu.getIXR(2)));
        ixr3.setText(String.format("%o", cpu.getIXR(3)));

        // Update control registers
        pc.setText(String.format("%o", cpu.getPC()));
        ir.setText(String.format("%o", cpu.getIR()));
        mar.setText(String.format("%o", cpu.getMAR()));
        mbr.setText(String.format("%o", cpu.getMBR()));
        cc.setText(String.format("%o", cpu.getCC()));
        mfr.setText(String.format("%o", cpu.getMFR()));

        updateCacheDisplay();
    }



    private void updateBinaryDisplay(String octalStr) {
        if (octalStr.isEmpty()) {
            binary.setText("");
            return;
        }
        try {
            int octalValue = Integer.parseInt(octalStr, 8);
            String binaryStr = String.format("%16s", Integer.toBinaryString(octalValue))
                                   .replace(' ', '0');
            binary.setText(binaryStr);
        } catch (NumberFormatException e) {
            binary.setText("Invalid octal input");
        }
    }

    private void updateCacheDisplay() {
        if (cacheContent == null) return;
        StringBuilder sb = new StringBuilder();
        CacheLine[] lines = memory.getCache().getLines();
        for (int i = 0; i < lines.length; i++) {
            CacheLine line = lines[i];
            if (line.isValid()) {
                sb.append(String.format("%02o: %06o  %06o", i, line.getTag(), line.getData() & 0xFFFF));
            } else {
                sb.append(String.format("%02o: ------  ------", i));
            }
            sb.append('\n');
        }
        cacheContent.setText(sb.toString());
    }



    private void loadFromMemory() {
        cpu.manual_load();
        updateDisplays();
    }

    private void loadFromMemoryAndIncrement() {
        cpu.manual_load_plus();
        updateDisplays();
    }

    private void storeToMemory() {
        cpu.manual_store();
        updateDisplays();
    }

    private void storeToMemoryAndIncrement() {
        cpu.manual_store_plus();
        updateDisplays();
    }

    private void updateRegister(Consumer<Integer> setter) {
        String octalValue = octalInput.getText();
        if (!octalValue.isEmpty()) {
            try {
                int value = Integer.parseInt(octalValue, 8);
                setter.accept(value);
                updateDisplays();
            } catch (NumberFormatException e) {
                System.err.println("Invalid octal input: " + octalValue);
            }
        }
    }
}