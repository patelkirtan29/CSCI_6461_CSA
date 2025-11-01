@echo off
REM change this path to your JavaFX SDK lib directory
set PATH_TO_FX="C:\Users\yabid\Downloads\openjfx-25.0.1_windows-x64_bin-sdk\javafx-sdk-25.0.1\lib"

REM Clean and recreate bin directory
rmdir /s /q bin\classes 2>nul
mkdir bin\classes

REM Create resource directories
mkdir bin\classes\com\gwu\assembler 2>nul

REM Copy resources
copy /Y "src\main\resources\com\gwu\assembler\SGUI.fxml" "bin\classes\com\gwu\assembler\"
copy /Y "src\main\resources\com\gwu\assembler\style.css" "bin\classes\com\gwu\assembler\"

REM Compile
javac --module-path %PATH_TO_FX% --add-modules javafx.controls,javafx.fxml -d bin/classes src/main/java/com/gwu/simulator/*.java src/main/java/com/gwu/assembler/*.java

REM Run
java --module-path %PATH_TO_FX% --add-modules javafx.controls,javafx.fxml -cp bin/classes com.gwu.assembler.SGUI

exit