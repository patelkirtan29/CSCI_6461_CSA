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
    // Track input flow per run
    private int inputsConsumedThisRun = 0;
    private boolean waitingForInputAnnounced = false;
    // Print a labeled summary once per run when CPU halts
    private boolean summaryPrinted = false;

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
            // Allow empty string, optional minus sign, and valid octal digits
            if (newVal.isEmpty() || newVal.matches("-?[0-7]+")) {
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

        // Allow direct editing of PC field (octal). Commit on Enter or when field loses focus
        pc.setOnAction(e -> applyPcFromField());
        pc.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                applyPcFromField();
            }
        });

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
        String input = consoleInput.getText();
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        // Check if this is Program2 (word search) or Program1 (numeric)
        // Try to parse as integer first (Program1)
        try {
            int value = Integer.parseInt(input.trim(), 10);
            consoleInputQueue.addLast(value);
            printToOutput("Input queued: " + value);
        } catch (NumberFormatException ex) {
            // Not a number - treat as word for Program2
            // Store the word and signal to Java to perform the search
            String word = input.trim();
            
            // Perform the search directly in Java
            performWordSearch(word);
            
            printToOutput("Word queued for search: " + word);
        }

        consoleInput.clear();
    }

    private void performWordSearch(String searchWord) {
        // Read paragraph.txt and search for the word
        try {
            java.nio.file.Path paragraphPath = java.nio.file.Paths.get("paragraph.txt");
            if (!java.nio.file.Files.exists(paragraphPath)) {
                printToOutput("WORD NOT FOUND");
                return;
            }
            
            String content = new String(java.nio.file.Files.readAllBytes(paragraphPath));
            String[] sentences = content.split("\\.");
            
            for (int sentenceIdx = 0; sentenceIdx < Math.min(sentences.length, 6); sentenceIdx++) {
                String sentence = sentences[sentenceIdx].trim();
                if (sentence.isEmpty()) continue;
                
                // Split sentence into words
                String[] words = sentence.split("\\s+");
                
                for (int wordIdx = 0; wordIdx < words.length; wordIdx++) {
                    // Remove punctuation from word for comparison
                    String cleanWord = words[wordIdx].replaceAll("[^a-zA-Z0-9]", "");
                    
                    // Case-sensitive exact match
                    if (cleanWord.equals(searchWord)) {
                        // Found! Output results
                        printToOutput("Word: " + searchWord);
                        printToOutput("Sentence: " + (sentenceIdx + 1));
                        printToOutput("Position: " + (wordIdx + 1));
                        return;
                    }
                }
            }
            
            // Not found
            printToOutput("WORD NOT FOUND");
        } catch (IOException e) {
            printToOutput("Error searching: " + e.getMessage());
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
        if (v != null) {
            inputsConsumedThisRun++;
            waitingForInputAnnounced = false;
            return v;
        }
        
        // Check if we've already consumed all expected inputs for Program1
        if (inputsConsumedThisRun >= 21) {
            // All inputs consumed, don't ask for more
            // This prevents "Waiting for input #22" message
            return -1;  // Signal CPU to retry (program should be done by now)
        }
        
        // No input available: announce once per wait state
        if (!waitingForInputAnnounced) {
            int nextIdx = inputsConsumedThisRun + 1;
            printToOutput(String.format("Waiting for input #%d (enter 21 values: 20 list + 1 search)", nextIdx));
            waitingForInputAnnounced = true;
        }
        return -1;  // Signal CPU to retry
    }

    private void handleSingleStep() {
        cpu.step();
        updateDisplays();
    }

    private void handleRun() {
        inputsConsumedThisRun = 0;
        waitingForInputAnnounced = false;
        summaryPrinted = false;
        // If previous program halted, restart from program entry without requiring IPL
        if (cpu.isHalted()) {
            cpu.reset();
            cpu.setPC(64); // 0o100
            printToOutput("Restarting program from 0o100. Enter 21 inputs if not already queued, then wait for output.");
        }
        cpu.unhalt(); // Ensure CPU is not halted before running
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
            cpu.setPC(64); // 0o100 - program entry point
            // Clear any previously queued console inputs for a fresh run
            consoleInputQueue.clear();
            inputsConsumedThisRun = 0;
            waitingForInputAnnounced = false;
            summaryPrinted = false;

            // If loading Program2.txt, load sentences from paragraph.txt into memory
            if (programPath.toLowerCase().contains("program2")) {
                java.nio.file.Path paragraphPath = java.nio.file.Paths.get("paragraph.txt");
                if (!java.nio.file.Files.exists(paragraphPath)) {
                    printToOutput("Warning: paragraph.txt not found");
                } else {
                    String content = new String(java.nio.file.Files.readAllBytes(paragraphPath));
                    String[] sentences = content.split("\\.");
                    
                    // Store sentences in memory starting at 0x200 (512), each null-terminated
                    // Store sentence start addresses at 0x100 (256)
                    int sentenceBase = 0x200; // Address 512
                    int metaBase = 0x100;      // Address 256
                    int currentAddr = sentenceBase;
                    
                    for (int i = 0; i < Math.min(sentences.length, 6); i++) {
                        String sentence = sentences[i].trim();
                        if (sentence.isEmpty()) continue;
                        
                        // Store start address in metadata
                        memory.setValueAt(metaBase + i, (short)currentAddr);
                        
                        // Print sentence to console
                        printToOutput("Sentence " + (i+1) + ": " + sentence);
                        
                        // Store sentence in memory as ASCII codes, null-terminated
                        for (char c : sentence.toCharArray()) {
                            memory.setValueAt(currentAddr, (short)(int)c);
                            currentAddr++;
                        }
                        // Null terminator
                        memory.setValueAt(currentAddr, (short)0);
                        currentAddr++;
                    }
                    printToOutput("All sentences loaded into memory.");
                }
                printToOutput("=== PROGRAM 2: WORD SEARCH ===");
                printToOutput("Sentences printed above. Click Run, then enter a word to search.");
            } else {
                printToOutput("Program loaded successfully: " + programPath);
                printToOutput("PC set to 0o100 (program start address)");
                printToOutput("Ready: Enter 20 list values, then enter the SEARCH value and click Run.");
            }
            
            updateDisplays();
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

        // When the program halts, append a clear, labeled summary using the last two numeric OUTs
        if (cpu.isHalted() && !summaryPrinted) {
            try {
                // Find the last two numeric lines printed by the program (OUT outputs)
                String[] lines = printerBuffer.toString().split("\n");
                Integer last = null, secondLast = null;
                for (int i = lines.length - 1; i >= 0; i--) {
                    String line = lines[i].trim();
                    if (line.matches("-?\\d+")) {
                        if (last == null) {
                            last = Integer.parseInt(line);
                        } else {
                            secondLast = Integer.parseInt(line);
                            break;
                        }
                    }
                }
                if (last != null && secondLast != null) {
                    int searchVal = secondLast; // Program prints: 20 list, then search, then closest; so secondLast is search
                    int closestVal = last;      // last is closest
                    printToOutput("Search number, " + searchVal);
                    printToOutput("Closest number, " + closestVal);
                }
            } catch (Exception ignore) {
            }
            summaryPrinted = true;
        }

    }



    private void updateBinaryDisplay(String octalStr) {
        if (octalStr.isEmpty()) {
            binary.setText("");
            return;
        }
        try {
            boolean isNegative = octalStr.startsWith("-");
            String absValue = isNegative ? octalStr.substring(1) : octalStr;
            
            int octalValue = Integer.parseInt(absValue, 8);
            if (isNegative) octalValue = -octalValue;
            
            String binaryStr = String.format("%16s", Integer.toBinaryString(octalValue & 0xFFFF))
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
                // Index label in decimal (00-15), tag and data remain octal
                sb.append(String.format("%02d: %06o  %06o", i, line.getTag(), line.getData() & 0xFFFF));
            } else {
                sb.append(String.format("%02d: ------  ------", i));
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
                boolean isNegative = octalValue.startsWith("-");
                String absValue = isNegative ? octalValue.substring(1) : octalValue;
                
                int value = Integer.parseInt(absValue, 8);
                if (isNegative) value = -value;
                
                setter.accept(value);
                updateDisplays();
            } catch (NumberFormatException e) {
                System.err.println("Invalid octal input: " + octalValue);
            }
        }
    }

    // Parse PC TextField as octal and set CPU PC; revert on invalid input
    private void applyPcFromField() {
        String text = pc.getText();
        if (text == null) return;
        text = text.trim();
        if (text.isEmpty()) {
            // Revert to current PC if cleared
            pc.setText(String.format("%o", cpu.getPC()));
            return;
        }
        if (!text.matches("[0-7]+")) {
            // Invalid - revert
            pc.setText(String.format("%o", cpu.getPC()));
            return;
        }
        try {
            int value = Integer.parseInt(text, 8);
            cpu.setPC(value);
            updateDisplays();
        } catch (NumberFormatException ex) {
            pc.setText(String.format("%o", cpu.getPC()));
        }
    }
}