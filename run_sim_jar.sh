#!/bin/bash

# change this path to your JavaFX SDK lib directory
PATH_TO_FX="D:\JavaFx\javafx-sdk-25\lib"
java --module-path "$PATH_TO_FX" \
     --add-modules javafx.controls,javafx.fxml \
     -jar Simulator.jar