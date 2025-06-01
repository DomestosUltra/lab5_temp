package org.example.lab4;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ResultsDialogController {
    @FXML private TextArea resultsArea;
    @FXML private javafx.scene.control.Button okButton;
    @FXML private javafx.scene.control.Button cancelButton;

    private Stage dialogStage;
    private boolean isOKPressed = true;

    // Установка сцены диалога
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Установка текста результатов
    public void setResults(String results) {
        resultsArea.setText(results);
    }

    // Проверка, была ли нажата кнопка "ОК"
    public boolean isOKPressed() {
        return isOKPressed;
    }

    @FXML
    private void handleOK() {
        isOKPressed = true;
        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        isOKPressed = false;
        dialogStage.close();
    }
}