package org.example.lab4;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Scene scene = new Scene(loader.load());

        HabitatController controller = loader.getController();

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case B:
                    controller.startSimulation();
                    break;
                case E:
                    controller.stopSimulation();
                    break;
                case T:
                    controller.toggleShowTime();
                    break;
            }
            event.consume();
        });

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}