// package com.gwu.assembler;

// import java.io.IOException;
// import java.util.List;
// import java.util.function.Consumer;
// import javafx.application.Platform;
// import javafx.fxml.FXML;
// import javafx.scene.control.TextField;
// import javafx.scene.control.Button;

// import com.gwu.simulator.CPU;
// import com.gwu.simulator.Memory;

// public class SGUIController {
//     private CPU cpu;
//     private Memory memory;

//     @FXML private TextField gpr0, gpr1, gpr2, gpr3;
//     @FXML private TextField ixr1, ixr2, ixr3;
//     @FXML private TextField mar, mbr, pc, ir;
//     @FXML private TextField octalInput, binary;
//     @FXML private TextField programFile;
//     @FXML private Button singleStepBtn, runBtn, iplBtn, haltBtn;
//     @FXML private Button loadBtn, loadPlusBtn, storeBtn, storePlusBtn;
//     @FXML private Button gpr0Btn, gpr1Btn, gpr2Btn, gpr3Btn;
//     @FXML private Button ixr1Btn, ixr2Btn, ixr3Btn;
//     @FXML private Button pcBtn, marBtn, mbrBtn, irBtn;

//     // I/O UI
//     @FXML private javafx.scene.control.TextArea consolePrinterArea;
//     @FXML private javafx.scene.control.TextField keyboardInputField;
//     @FXML private javafx.scene.control.Button keyboardSubmitBtn;

//     // Cache UI
//     @FXML private javafx.scene.control.TableView<com.gwu.simulator.Cache.Line> cacheTable;
//     @FXML private javafx.scene.control.TableColumn<com.gwu.simulator.Cache.Line, String> colIndex;
//     @FXML private javafx.scene.control.TableColumn<com.gwu.simulator.Cache.Line, String> colValid;
//     @FXML private javafx.scene.control.TableColumn<com.gwu.simulator.Cache.Line, String> colTag;
//     @FXML private javafx.scene.control.TableColumn<com.gwu.simulator.Cache.Line, String> colData;
//     @FXML private javafx.scene.control.Button invalidateCacheBtn;
//     @FXML private javafx.scene.control.Button showCacheStatsBtn;

//     @FXML
//     public void initialize() {
//         memory = new Memory();
//         memory.setConsolePrinter(s -> {
//             // append on FX thread
//             Platform.runLater(() -> consolePrinterArea.appendText(s));
//         });

//         cpu = new CPU(memory);
//         setupListeners();
//         setupCacheTable();
//         setupIPLProgram();
//     }

//     private void setupListeners() {
//         // Octal input listener
//         octalInput.textProperty().addListener((obs, oldVal, newVal) -> {
//             if (newVal.matches("[0-7]*")) {
//                 updateBinaryDisplay(newVal);
//             } else {
//                 octalInput.setText(oldVal);
//             }
//         });

//         // GPR button handlers
//         gpr0Btn.setOnAction(e -> updateRegister(val -> cpu.setGPR(0, val)));
//         gpr1Btn.setOnAction(e -> updateRegister(val -> cpu.setGPR(1, val)));
//         gpr2Btn.setOnAction(e -> updateRegister(val -> cpu.setGPR(2, val)));
//         gpr3Btn.setOnAction(e -> updateRegister(val -> cpu.setGPR(3, val)));

//         // IXR button handlers
//         ixr1Btn.setOnAction(e -> updateRegister(val -> cpu.setIXR(1, val)));
//         ixr2Btn.setOnAction(e -> updateRegister(val -> cpu.setIXR(2, val)));
//         ixr3Btn.setOnAction(e -> updateRegister(val -> cpu.setIXR(3, val)));

//         // Special register button handlers
//         pcBtn.setOnAction(e -> updateRegister(cpu::setPC));
//         marBtn.setOnAction(e -> updateRegister(cpu::setMAR));
//         mbrBtn.setOnAction(e -> updateRegister(cpu::setMBR));
//         irBtn.setOnAction(e -> updateRegister(cpu::setIR));

//         // Memory operation buttons
//         loadBtn.setOnAction(e -> loadFromMemory());
//         loadPlusBtn.setOnAction(e -> loadFromMemoryAndIncrement());
//         storeBtn.setOnAction(e -> storeToMemory());
//         storePlusBtn.setOnAction(e -> storeToMemoryAndIncrement());

//         // Button event handlers
//         singleStepBtn.setOnAction(e -> handleSingleStep());
//         runBtn.setOnAction(e -> handleRun());
//         haltBtn.setOnAction(e -> handleHalt());
//         iplBtn.setOnAction(e -> handleIPL());

//         keyboardSubmitBtn.setOnAction(e -> {
//             String txt = keyboardInputField.getText();
//             if (txt == null || txt.trim().isEmpty()) return;
//             try {
//                 int val = Integer.parseInt(txt); // decimal input assumed; change if you want octal
//                 memory.enqueueInput((short) val);
//                 keyboardInputField.clear();
//             } catch (NumberFormatException ex) {
//                 System.err.println("Invalid input: " + txt);
//             }
//         });
//     }

//     private void handleSingleStep() {
//         cpu.step();
//         updateDisplays();
//     }

//     private void handleRun() {
//         cpu.unhalt(); // Make sure CPU is not halted before running
//         cpu.run(() -> {
//             javafx.application.Platform.runLater(this::updateDisplays);
//         });
//     }

//     private void handleHalt() {
//         cpu.halt();
//         updateDisplays();
//     }

//     private void handleIPL() {
//         setupIPLProgram();
//         updateDisplays();
//     }

//     private void setupIPLProgram() {
//         String programPath = programFile.getText();
//         if (programPath == null || programPath.trim().isEmpty()) {
//             System.err.println("No program file specified");
//             return;
//         }

//         try {
//             memory.reset(); // Clear memory before loading new program
//             memory.loadProgramFromFile(programPath);
//             cpu.reset();  // Reset CPU state after loading program
//             updateDisplays();
//         } catch (IOException e) {
//             System.err.println("Error loading program from file '" + programPath + "': " + e.getMessage());
//         }
//     }
    
//     private void updateDisplays() {
//         if (Platform.isFxApplicationThread()) {
//             updateDisplaysInternal();
//         } else {
//             Platform.runLater(this::updateDisplaysInternal);
//         }
//     }

//     private void updateDisplaysInternal() {
//         // Update register displays with octal values
//         gpr0.setText(String.format("%o", cpu.getGPR(0)));
//         gpr1.setText(String.format("%o", cpu.getGPR(1)));
//         gpr2.setText(String.format("%o", cpu.getGPR(2)));
//         gpr3.setText(String.format("%o", cpu.getGPR(3)));

//         // Update IXR displays
//         ixr1.setText(String.format("%o", cpu.getIXR(1)));
//         ixr2.setText(String.format("%o", cpu.getIXR(2)));
//         ixr3.setText(String.format("%o", cpu.getIXR(3)));

//         // Update control registers
//         pc.setText(String.format("%o", cpu.getPC()));
//         ir.setText(String.format("%o", cpu.getIR()));
//         mar.setText(String.format("%o", cpu.getMAR()));
//         mbr.setText(String.format("%o", cpu.getMBR()));

//         // refresh cache table
//         refreshCacheTable();
//     }



//     private void updateBinaryDisplay(String octalStr) {
//         if (octalStr.isEmpty()) {
//             binary.setText("");
//             return;
//         }
//         try {
//             int octalValue = Integer.parseInt(octalStr, 8);
//             String binaryStr = String.format("%16s", Integer.toBinaryString(octalValue))
//                                    .replace(' ', '0');
//             binary.setText(binaryStr);
//         } catch (NumberFormatException e) {
//             binary.setText("Invalid octal input");
//         }
//     }


//     private void loadFromMemory() {
//         cpu.manual_load();
//         updateDisplays();
//     }

//     private void loadFromMemoryAndIncrement() {
//         cpu.manual_load_plus();
//         updateDisplays();
//     }

//     private void storeToMemory() {
//         cpu.manual_store();
//         updateDisplays();
//     }

//     private void storeToMemoryAndIncrement() {
//         cpu.manual_store_plus();
//         updateDisplays();
//     }

//     private void updateRegister(Consumer<Integer> setter) {
//         String octalValue = octalInput.getText();
//         if (!octalValue.isEmpty()) {
//             try {
//                 int value = Integer.parseInt(octalValue, 8);
//                 setter.accept(value);
//                 updateDisplays();
//             } catch (NumberFormatException e) {
//                 System.err.println("Invalid octal input: " + octalValue);
//             }
//         }
//     }

//     private void setupCacheTable() {
//         // colIndex: shows line index
//         colIndex.setCellValueFactory(cell -> {
//             int idx = memory.getCache().getLines().indexOf(cell.getValue());
//             return new javafx.beans.property.SimpleStringProperty(String.valueOf(idx));
//         });
//         colValid.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().valid ? "1" : "0"));
//         colTag.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().tag >= 0 ? String.valueOf(cell.getValue().tag) : "-"));
//         colData.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(String.format("%06o", cell.getValue().data)));

//         // populate
//         refreshCacheTable();

//         invalidateCacheBtn.setOnAction(e -> {
//             memory.getCache().reset();
//             refreshCacheTable();
//         });

//         showCacheStatsBtn.setOnAction(e -> {
//             long hits = memory.getCache().getHits();
//             long misses = memory.getCache().getMisses();
//             System.out.println("Cache hits: " + hits + " misses: " + misses);
//         });
//     }

//     private void refreshCacheTable() {
//         List<com.gwu.simulator.Cache.Line> lines = memory.getCache().getLines();
//         cacheTable.getItems().setAll(lines);
//     }
// }

package com.gwu.assembler;

import java.io.IOException;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

import com.gwu.simulator.CPU;
import com.gwu.simulator.Memory;

public class SGUIController {
    private CPU cpu;
    private Memory memory;

    @FXML private TextField gpr0, gpr1, gpr2, gpr3;
    @FXML private TextField ixr1, ixr2, ixr3;
    @FXML private TextField mar, mbr, pc, ir;
    @FXML private TextField octalInput, binary;
    @FXML private TextField programFile;
    @FXML private Button singleStepBtn, runBtn, iplBtn, haltBtn;
    @FXML private Button loadBtn, loadPlusBtn, storeBtn, storePlusBtn;
    @FXML private Button gpr0Btn, gpr1Btn, gpr2Btn, gpr3Btn;
    @FXML private Button ixr1Btn, ixr2Btn, ixr3Btn;
    @FXML private Button pcBtn, marBtn, mbrBtn, irBtn;

    @FXML
    public void initialize() {
        memory = new Memory();
        cpu = new CPU(memory);
        setupListeners();
        setupIPLProgram();
    }

    private void setupListeners() {
        // Octal input listener
        octalInput.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.matches("[0-7]*")) {
                updateBinaryDisplay(newVal);
            } else {
                octalInput.setText(oldVal);
            }
        });

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

    private void handleSingleStep() {
        cpu.step();
        updateDisplays();
    }

    private void handleRun() {
        cpu.unhalt(); // Make sure CPU is not halted before running
        cpu.run(() -> {
            javafx.application.Platform.runLater(this::updateDisplays);
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
            System.err.println("No program file specified");
            return;
        }

        try {
            memory.reset(); // Clear memory before loading new program
            memory.loadProgramFromFile(programPath);
            cpu.reset();  // Reset CPU state after loading program
            updateDisplays();
        } catch (IOException e) {
            System.err.println("Error loading program from file '" + programPath + "': " + e.getMessage());
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