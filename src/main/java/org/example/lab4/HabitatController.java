package org.example.lab4;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class HabitatController {
    @FXML private Canvas simulationCanvas;
    @FXML private CheckBox showInfoCheckBox;
    @FXML private RadioButton showTimeRadio;
    @FXML private RadioButton hideTimeRadio;
    @FXML private Button clearButton;
    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private Button currentObjectsButton;
    
    @FXML private Button personAIStartButton;
    @FXML private Button personAIPauseButton;
    @FXML private Button legalEntityAIStartButton;
    @FXML private Button legalEntityAIPauseButton;
    @FXML private ComboBox<String> personAIPriorityCombo;
    @FXML private ComboBox<String> legalEntityAIPriorityCombo;
    
    @FXML private TextField individualSpawnField;
    @FXML private TextField legalSpawnField;
    @FXML private ComboBox<Double> individualProbCombo;
    @FXML private ComboBox<Double> legalProbCombo;
    @FXML private MenuItem startMenuItem;
    @FXML private MenuItem stopMenuItem;
    @FXML private MenuItem clearMenuItem;
    @FXML private MenuItem currentObjectsMenuItem;
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem loadMenuItem;
    @FXML private TextField individualLifetimeField;
    @FXML private TextField legalLifetimeField;

    private GraphicsContext gc;
    private AnimationTimer timer;
    private long startTime;
    private long lastIndividualSpawn = 0;
    private long lastLegalSpawn = 0;
    private boolean showTime = false;
    private boolean isSimulationRunning = false;
    private boolean isFirstStart = true;
    private Random random = new Random();

    // Параметры симуляции
    private int N1 = 2000;
    private int N2 = 3000;
    private double P1 = 0.8;
    private double P2 = 0.8;
    private long individualLifetime = 30000;
    private long legalLifetime = 30000;
    private double moveSpeed = 2.0;

    private final Image individualImage = new Image("/individual.png");
    private final Image legalImage = new Image("/legal.png");
    private final Image backgroundImage = new Image("/background.png");
    private AudioClip musicClip;

    private final double width = 1000, height = 600, panelWidth = 200;
    
    private PersonAI personAI;
    private LegalEntityAI legalEntityAI;
    private final Object renderLock = new Object();
    
    // Конфигурация
    private SimulationConfig config = new SimulationConfig();
    private SimulationRecords simulationRecords = SimulationRecords.getInstance();

    @FXML
    public void initialize() {
        gc = simulationCanvas.getGraphicsContext2D();
        
        // Загружаем конфигурацию
        loadConfiguration();

        // Инициализация комбо-боксов вероятности
        for (double i = 0.0; i <= 1.0; i += 0.1) {
            individualProbCombo.getItems().add(Math.round(i * 10.0) / 10.0);
            legalProbCombo.getItems().add(Math.round(i * 10.0) / 10.0);
        }
        
        // Инициализация комбо-боксов приоритета
        personAIPriorityCombo.getItems().addAll("Минимальный", "Нормальный", "Максимальный");
        legalEntityAIPriorityCombo.getItems().addAll("Минимальный", "Нормальный", "Максимальный");
        
        // Применяем настройки из конфигурации
        applyConfiguration();
        
        // Инициализация AI
        Vector<Record> records = simulationRecords.getRecords();
        personAI = new PersonAI(records, width - panelWidth, height, moveSpeed);
        legalEntityAI = new LegalEntityAI(records, width - panelWidth, height, moveSpeed);

        // Загружаем музыку
        try {
            musicClip = new AudioClip(getClass().getResource("/music.mp3").toString());
            musicClip.setCycleCount(AudioClip.INDEFINITE);
            musicClip.setVolume(0.5);
        } catch (Exception e) {
            System.out.println("Музыкальный файл не найден");
        }

        // Настройка обработчиков
        setupEventHandlers();
    }

    private void loadConfiguration() {
        config.loadConfig();
    }

    private void applyConfiguration() {
        // Применяем параметры из конфигурации
        N1 = config.getIndividualSpawnPeriod();
        N2 = config.getLegalSpawnPeriod();
        P1 = config.getIndividualProbability();
        P2 = config.getLegalProbability();
        individualLifetime = config.getIndividualLifetime();
        legalLifetime = config.getLegalLifetime();
        showTime = config.isShowTime();

        // Обновляем элементы интерфейса
        individualSpawnField.setText(String.valueOf(N1));
        legalSpawnField.setText(String.valueOf(N2));
        individualProbCombo.setValue(P1);
        legalProbCombo.setValue(P2);
        individualLifetimeField.setText(String.valueOf(individualLifetime));
        legalLifetimeField.setText(String.valueOf(legalLifetime));
        showInfoCheckBox.setSelected(config.isShowInfo());
        
        if (showTime) {
            showTimeRadio.setSelected(true);
        } else {
            hideTimeRadio.setSelected(true);
        }
        
        personAIPriorityCombo.setValue(config.getPersonAIPriority());
        legalEntityAIPriorityCombo.setValue(config.getLegalEntityAIPriority());
    }

    private void saveConfiguration() {
        // Сохраняем текущие настройки в конфигурацию
        try {
            config.setIndividualSpawnPeriod(Integer.parseInt(individualSpawnField.getText()));
            config.setLegalSpawnPeriod(Integer.parseInt(legalSpawnField.getText()));
            config.setIndividualProbability(individualProbCombo.getValue());
            config.setLegalProbability(legalProbCombo.getValue());
            config.setIndividualLifetime(Long.parseLong(individualLifetimeField.getText()));
            config.setLegalLifetime(Long.parseLong(legalLifetimeField.getText()));
            config.setShowInfo(showInfoCheckBox.isSelected());
            config.setShowTime(showTimeRadio.isSelected());
            config.setPersonAIPriority(personAIPriorityCombo.getValue());
            config.setLegalEntityAIPriority(legalEntityAIPriorityCombo.getValue());
            
            config.saveConfig();
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Некорректные значения в полях");
        }
    }

    private void setupEventHandlers() {
        // Обработчики изменения параметров
        individualSpawnField.textProperty().addListener((obs, old, newVal) -> {
            try {
                N1 = Integer.parseInt(newVal);
            } catch (NumberFormatException ignored) {}
        });

        legalSpawnField.textProperty().addListener((obs, old, newVal) -> {
            try {
                N2 = Integer.parseInt(newVal);
            } catch (NumberFormatException ignored) {}
        });

        individualLifetimeField.textProperty().addListener((obs, old, newVal) -> {
            try {
                individualLifetime = Long.parseLong(newVal);
            } catch (NumberFormatException ignored) {}
        });

        legalLifetimeField.textProperty().addListener((obs, old, newVal) -> {
            try {
                legalLifetime = Long.parseLong(newVal);
            } catch (NumberFormatException ignored) {}
        });

        individualProbCombo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) P1 = newVal;
        });

        legalProbCombo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) P2 = newVal;
        });

        showTimeRadio.selectedProperty().addListener((obs, old, newVal) -> {
            showTime = newVal;
        });

        // Обработчики AI управления
        personAIPriorityCombo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && personAI != null) {
                personAI.setPriority(getPriorityValue(newVal));
            }
        });

        legalEntityAIPriorityCombo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && legalEntityAI != null) {
                legalEntityAI.setPriority(getPriorityValue(newVal));
            }
        });
    }

    private int getPriorityValue(String priority) {
        switch (priority) {
            case "Минимальный": return Thread.MIN_PRIORITY;
            case "Максимальный": return Thread.MAX_PRIORITY;
            default: return Thread.NORM_PRIORITY;
        }
    }

    @FXML
    public void startSimulation() {
        if (!isSimulationRunning) {
            isSimulationRunning = true;
            
            if (isFirstStart) {
                startTime = System.currentTimeMillis();
                isFirstStart = false;
            }
            
            // Запускаем AI
            if (personAI != null) personAI.start();
            if (legalEntityAI != null) legalEntityAI.start();
            
            timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    updateSimulation();
                    render();
                }
            };
            timer.start();
            
            if (musicClip != null) {
                musicClip.play();
            }
            
            updateButtonStates();
        }
    }

    @FXML
    public void stopSimulation() {
        if (isSimulationRunning) {
            isSimulationRunning = false;
            
            if (timer != null) {
                timer.stop();
            }
            
            // Останавливаем AI
            if (personAI != null) personAI.stop();
            if (legalEntityAI != null) legalEntityAI.stop();
            
            if (musicClip != null) {
                musicClip.stop();
            }
            
            updateButtonStates();
        }
    }

    @FXML
    public void clearSimulation() {
        stopSimulation();
        simulationRecords.clearRecords();
        isFirstStart = true;
        render();
    }

    @FXML
    public void toggleShowTime() {
        showTime = !showTime;
        showTimeRadio.setSelected(showTime);
        hideTimeRadio.setSelected(!showTime);
    }

    @FXML
    public void showCurrentObjects() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/currentObjectsDialog.fxml"));
            Scene scene = new Scene(loader.load());
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Текущие объекты");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(scene);
            
            CurrentObjectsDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setObjectsData(simulationRecords.getBirthTimes(), simulationRecords.getRecords());
            
            dialogStage.showAndWait();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть диалог текущих объектов");
        }
    }

    @FXML
    public void saveSimulation() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить симуляцию");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Simulation Files", "*.sim")
        );
        
        Stage stage = (Stage) simulationCanvas.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                saveSimulationToFile(file);
                showAlert("Успех", "Симуляция сохранена успешно");
            } catch (IOException e) {
                showAlert("Ошибка", "Не удалось сохранить симуляцию: " + e.getMessage());
            }
        }
    }

    @FXML
    public void loadSimulation() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Загрузить симуляцию");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Simulation Files", "*.sim")
        );
        
        Stage stage = (Stage) simulationCanvas.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            try {
                stopSimulation(); // Останавливаем текущую симуляцию
                loadSimulationFromFile(file);
                showAlert("Успех", "Симуляция загружена успешно");
            } catch (IOException | ClassNotFoundException e) {
                showAlert("Ошибка", "Не удалось загрузить симуляцию: " + e.getMessage());
            }
        }
    }

    private void saveSimulationToFile(File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            List<SerializableRecord> serializableRecords = new ArrayList<>();
            long currentTime = System.currentTimeMillis();
            
            for (Record record : simulationRecords.getRecords()) {
                serializableRecords.add(new SerializableRecord(record));
            }
            
            // Сохраняем записи и текущее время
            oos.writeObject(serializableRecords);
            oos.writeLong(currentTime);
            oos.writeBoolean(isFirstStart);
            if (!isFirstStart) {
                oos.writeLong(startTime);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadSimulationFromFile(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<SerializableRecord> serializableRecords = (List<SerializableRecord>) ois.readObject();
            long savedTime = ois.readLong();
            boolean wasFirstStart = ois.readBoolean();
            long savedStartTime = 0;
            if (!wasFirstStart) {
                savedStartTime = ois.readLong();
            }
            
            // Очищаем текущие записи
            simulationRecords.clearRecords();
            
            // Восстанавливаем записи с корректировкой времени
            long currentTime = System.currentTimeMillis();
            long timeDifference = currentTime - savedTime;
            
            for (SerializableRecord serRecord : serializableRecords) {
                // Корректируем время рождения
                long newBirthTime = serRecord.getBirthTime() + timeDifference;
                serRecord.setBirthTime(newBirthTime);
                
                // Создаем новую запись
                Record record;
                if ("PERSON".equals(serRecord.getRecordType())) {
                    record = new PersonRecord(serRecord.getX(), serRecord.getY(), 
                                            individualImage, newBirthTime, 
                                            serRecord.getLifetime(), serRecord.getId());
                } else {
                    record = new LegalEntityRecord(serRecord.getX(), serRecord.getY(), 
                                                 legalImage, newBirthTime, 
                                                 serRecord.getLifetime(), serRecord.getId());
                }
                
                // Восстанавливаем состояние движения
                if (serRecord.hasDestination()) {
                    record.setDestination(serRecord.getDestX(), serRecord.getDestY());
                    record.setReachedDestination(serRecord.hasReachedDestination());
                }
                
                simulationRecords.getRecords().add(record);
                simulationRecords.addUsedId(serRecord.getId());
                simulationRecords.getBirthTimes().put(serRecord.getId(), newBirthTime);
            }
            
            // Восстанавливаем состояние симуляции
            isFirstStart = wasFirstStart;
            if (!isFirstStart) {
                startTime = savedStartTime + timeDifference;
            }
            
            render(); // Перерисовываем
        }
    }

    // AI Control Methods
    @FXML
    public void startPersonAI() {
        if (personAI != null) {
            personAI.start();
            updateAIButtonStates();
        }
    }

    @FXML
    public void pausePersonAI() {
        if (personAI != null) {
            if (personAI.isPaused()) {
                personAI.resume();
            } else {
                personAI.pause();
            }
            updateAIButtonStates();
        }
    }

    @FXML
    public void startLegalEntityAI() {
        if (legalEntityAI != null) {
            legalEntityAI.start();
            updateAIButtonStates();
        }
    }

    @FXML
    public void pauseLegalEntityAI() {
        if (legalEntityAI != null) {
            if (legalEntityAI.isPaused()) {
                legalEntityAI.resume();
            } else {
                legalEntityAI.pause();
            }
            updateAIButtonStates();
        }
    }

    private void updateSimulation() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;

        // Создание новых объектов
        if (currentTime - lastIndividualSpawn >= N1 && Math.random() < P1) {
            double x = random.nextDouble() * (width - panelWidth - individualImage.getWidth());
            double y = random.nextDouble() * (height - individualImage.getHeight());
            simulationRecords.addPersonRecord(x, y, individualImage, currentTime, individualLifetime);
            lastIndividualSpawn = currentTime;
        }

        if (currentTime - lastLegalSpawn >= N2 && Math.random() < P2) {
            double x = random.nextDouble() * (width - panelWidth - legalImage.getWidth());
            double y = random.nextDouble() * (height - legalImage.getHeight());
            simulationRecords.addLegalEntityRecord(x, y, legalImage, currentTime, legalLifetime);
            lastLegalSpawn = currentTime;
        }

        // Удаление истекших объектов
        simulationRecords.removeExpired(currentTime);
    }

    private void render() {
        synchronized (renderLock) {
            // Очистка и фон
            gc.clearRect(0, 0, width, height);
            if (backgroundImage != null) {
                gc.drawImage(backgroundImage, 0, 0, width - panelWidth, height);
            }

            // Отрисовка объектов
            for (Record record : simulationRecords.getRecords()) {
                record.draw(gc);
            }

            // Панель управления
            gc.setFill(Color.LIGHTGRAY);
            gc.fillRect(width - panelWidth, 0, panelWidth, height);

            // Информация
            if (showInfoCheckBox.isSelected()) {
                drawInfo();
            }

            // Время
            if (showTime && !isFirstStart) {
                long elapsed = System.currentTimeMillis() - startTime;
                gc.setFill(Color.BLACK);
                gc.setFont(Font.font(14));
                gc.fillText("Время: " + elapsed + " мс", width - panelWidth + 10, height - 20);
            }
        }
    }

    private void drawInfo() {
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(12));
        int y = 30;
        
        int personCount = 0;
        int legalCount = 0;
        
        for (Record record : simulationRecords.getRecords()) {
            if (record instanceof PersonRecord) personCount++;
            else legalCount++;
        }
        
        gc.fillText("Физ. лица: " + personCount, width - panelWidth + 10, y);
        gc.fillText("Юр. лица: " + legalCount, width - panelWidth + 10, y + 20);
        gc.fillText("Всего: " + simulationRecords.getRecords().size(), width - panelWidth + 10, y + 40);
    }

    private void updateButtonStates() {
        startButton.setDisable(isSimulationRunning);
        stopButton.setDisable(!isSimulationRunning);
        startMenuItem.setDisable(isSimulationRunning);
        stopMenuItem.setDisable(!isSimulationRunning);
    }

    private void updateAIButtonStates() {
        if (personAI != null) {
            personAIStartButton.setDisable(personAI.isRunning());
            personAIPauseButton.setText(personAI.isPaused() ? "Возобновить" : "Пауза");
            personAIPauseButton.setDisable(!personAI.isRunning());
        }
        
        if (legalEntityAI != null) {
            legalEntityAIStartButton.setDisable(legalEntityAI.isRunning());
            legalEntityAIPauseButton.setText(legalEntityAI.isPaused() ? "Возобновить" : "Пауза");
            legalEntityAIPauseButton.setDisable(!legalEntityAI.isRunning());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Методы для обработки горячих клавиш (вызываются из Main)
    public void onApplicationExit() {
        saveConfiguration();
        stopSimulation();
    }
}