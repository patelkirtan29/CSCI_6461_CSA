package com.gwu.assembler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class SGUI extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL fxmlPath = getClass().getResource("/com/gwu/assembler/SGUI.fxml");
        if (fxmlPath == null) {
            throw new RuntimeException("FXML file not found!");
        }

        FXMLLoader loader = new FXMLLoader(fxmlPath);
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("CSCI 6461 Simulator");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
