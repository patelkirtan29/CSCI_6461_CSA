@echo off
set PATH_TO_FX="D:\JavaFx\javafx-sdk-25\lib"

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