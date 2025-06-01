package org.example.lab4;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.TreeSet;
import java.util.Vector;

public class CurrentObjectsDialogController {
    @FXML private ListView<String> objectsList;
    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Установка данных объектов
    public void setObjectsData(HashMap<Integer, Long> birthTimes, Vector<Record> records) {
        TreeSet<String> objectEntries = new TreeSet<>(); // Множество для хранения записей
        for (Record record : records) {
            long birthTime = birthTimes.get(record.getId());
            String type = (record instanceof PersonRecord) ? "Физ. лицо" : "Юр. лицо";
            String entry = String.format("ID: %d, Тип: %s, Время рождения: %d мс",
                    record.getId(), type, birthTime);
            objectEntries.add(entry);
        }

        objectsList.setItems(FXCollections.observableArrayList(objectEntries));
    }

    @FXML
    private void handleClose() {
        dialogStage.close();
    }
}