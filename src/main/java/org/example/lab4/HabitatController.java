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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Random;

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

    private int N1 = 2000;
    private int N2 = 3000;
    private double P1 = 0.8;
    private double P2 = 0.8;
    private long individualLifetime = 30000;
    private long legalLifetime = 30000;
    private double moveSpeed = 2.0; // Default movement speed

    private final Image individualImage = new Image("/individual.png");
    private final Image legalImage = new Image("/legal.png");
    private final Image backgroundImage = new Image("/background.png");
    private AudioClip musicClip;

    private final double width = 1000, height = 600, panelWidth = 200;
    
    private PersonAI personAI;
    private LegalEntityAI legalEntityAI;
    private final Object renderLock = new Object();

    @FXML
    public void initialize() {
        gc = simulationCanvas.getGraphicsContext2D();

        for (double i = 0.0; i <= 1.0; i += 0.1) {
            individualProbCombo.getItems().add(Math.round(i * 10.0) / 10.0);
            legalProbCombo.getItems().add(Math.round(i * 10.0) / 10.0);
        }
        individualProbCombo.setValue(P1);
        legalProbCombo.setValue(P2);
        individualSpawnField.setText(String.valueOf(N1));
        legalSpawnField.setText(String.valueOf(N2));
        individualLifetimeField.setText(String.valueOf(individualLifetime));
        legalLifetimeField.setText(String.valueOf(legalLifetime));
        
        setupPriorityComboBoxes();

        updateButtonStates();
        loadMusic();
        setupHandlers();
        setupTooltips();
        
        initializeAI();
    }
    
    private void setupPriorityComboBoxes() {
        personAIPriorityCombo.getItems().addAll(
            "Минимальный", "Низкий", "Нормальный", "Высокий", "Максимальный"
        );
        legalEntityAIPriorityCombo.getItems().addAll(
            "Минимальный", "Низкий", "Нормальный", "Высокий", "Максимальный"
        );
        
        personAIPriorityCombo.setValue("Нормальный");
        legalEntityAIPriorityCombo.setValue("Нормальный");
    }
    
    private void initializeAI() {
        SimulationRecords simRecords = SimulationRecords.getInstance();
        personAI = new PersonAI(simRecords.getRecords(), width - panelWidth, height, moveSpeed);
        legalEntityAI = new LegalEntityAI(simRecords.getRecords(), width - panelWidth, height, moveSpeed);
    }

    private void loadMusic() {
        try {
            musicClip = new AudioClip(getClass().getResource("/music.wav").toExternalForm());
        } catch (Exception e) {
            System.out.println("Ошибка загрузки музыки: " + e.getMessage());
        }
    }

    private void playMusic() {
        if (musicClip != null) {
            musicClip.setCycleCount(AudioClip.INDEFINITE);
            musicClip.play();
        }
    }

    private void stopMusic() {
        if (musicClip != null && musicClip.isPlaying()) {
            musicClip.stop();
        }
    }

    @FXML
    void startSimulation() {
        if (!isSimulationRunning && validateInputs()) {
            if (!isFirstStart) {
                clearRecords();
            }
            isFirstStart = false;

            isSimulationRunning = true;
            startTime = System.currentTimeMillis();
            lastIndividualSpawn = startTime;
            lastLegalSpawn = startTime;
            playMusic();
            
            startAIThreads();

            timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    synchronized (renderLock) {
                        update();
                        render();
                    }
                }
            };
            timer.start();

            updateButtonStates();
        }
    }
    
    private void startAIThreads() {
        updateAIPriorities();
        
        personAI.start();
        legalEntityAI.start();
    }
    
    private void updateAIPriorities() {
        int personPriority = getPriorityFromString(personAIPriorityCombo.getValue());
        int legalEntityPriority = getPriorityFromString(legalEntityAIPriorityCombo.getValue());
        
        personAI.setPriority(personPriority);
        legalEntityAI.setPriority(legalEntityPriority);
    }
    
    private int getPriorityFromString(String priorityString) {
        switch (priorityString) {
            case "Минимальный":
                return Thread.MIN_PRIORITY;
            case "Низкий":
                return Thread.MIN_PRIORITY + 1;
            case "Высокий":
                return Thread.NORM_PRIORITY + 2;
            case "Максимальный":
                return Thread.MAX_PRIORITY;
            default:
                return Thread.NORM_PRIORITY;
        }
    }

    @FXML
    void stopSimulation() {
        if (isSimulationRunning) {
            isSimulationRunning = false;
            timer.stop();
            stopMusic();
            
            personAI.stop();
            legalEntityAI.stop();
            
            render();

            if (showInfoCheckBox.isSelected()) {
                showResultsDialog();
            } else {
                updateButtonStates();
            }
        }
    }
    
    @FXML
    void togglePersonAI() {
        if (personAI.isPaused()) {
            personAI.resume();
            personAIPauseButton.setText("Приостановить физ. лица");
        } else {
            personAI.pause();
            personAIPauseButton.setText("Возобновить физ. лица");
        }
    }
    
    @FXML
    void toggleLegalEntityAI() {
        if (legalEntityAI.isPaused()) {
            legalEntityAI.resume();
            legalEntityAIPauseButton.setText("Приостановить юр. лица");
        } else {
            legalEntityAI.pause();
            legalEntityAIPauseButton.setText("Возобновить юр. лица");
        }
    }
    
    @FXML
    void updatePersonAIPriority() {
        int priority = getPriorityFromString(personAIPriorityCombo.getValue());
        personAI.setPriority(priority);
    }
    
    @FXML
    void updateLegalEntityAIPriority() {
        int priority = getPriorityFromString(legalEntityAIPriorityCombo.getValue());
        legalEntityAI.setPriority(priority);
    }

    @FXML
    void showCurrentObjects() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/current-objects-dialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Текущие объекты");
            dialogStage.setScene(new Scene(loader.load()));

            CurrentObjectsDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            // Передаем данные из SimulationRecords
            SimulationRecords simRecords = SimulationRecords.getInstance();
            controller.setObjectsData(simRecords.getBirthTimes(), simRecords.getRecords());

            // Приостанавливаем симуляцию перед открытием диалога
            boolean wasRunning = isSimulationRunning;
            if (isSimulationRunning) {
                isSimulationRunning = false;
                timer.stop();
                stopMusic();
                updateButtonStates();
            }

            // Показываем диалог и ждем его закрытия
            dialogStage.showAndWait();

            // Возобновляем симуляцию, если она была активна
            if (wasRunning) {
                isSimulationRunning = true;
                timer.start();
                playMusic();
                updateButtonStates();
            }

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Не удалось загрузить диалог текущих объектов: " + e.getMessage()).showAndWait();
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder("Ошибки ввода:\n");

        try {
            int newN1 = Integer.parseInt(individualSpawnField.getText().trim());
            if (newN1 <= 0) throw new NumberFormatException("Период должен быть положительным");
            N1 = newN1;
        } catch (NumberFormatException e) {
            isValid = false;
            errorMessage.append("- Неверный период для физических лиц\n");
            individualSpawnField.setText(String.valueOf(N1));
        }

        try {
            int newN2 = Integer.parseInt(legalSpawnField.getText().trim());
            if (newN2 <= 0) throw new NumberFormatException("Период должен быть положительным");
            N2 = newN2;
        } catch (NumberFormatException e) {
            isValid = false;
            errorMessage.append("- Неверный период для юридических лиц\n");
            legalSpawnField.setText(String.valueOf(N2));
        }

        try {
            long newLifetime = Long.parseLong(individualLifetimeField.getText().trim());
            if (newLifetime <= 0) throw new NumberFormatException("Время жизни должно быть положительным");
            individualLifetime = newLifetime;
        } catch (NumberFormatException e) {
            isValid = false;
            errorMessage.append("- Неверное время жизни для физических лиц\n");
            individualLifetimeField.setText(String.valueOf(individualLifetime));
        }

        try {
            long newLifetime = Long.parseLong(legalLifetimeField.getText().trim());
            if (newLifetime <= 0) throw new NumberFormatException("Время жизни должно быть положительным");
            legalLifetime = newLifetime;
        } catch (NumberFormatException e) {
            isValid = false;
            errorMessage.append("- Неверное время жизни для юридических лиц\n");
            legalLifetimeField.setText(String.valueOf(legalLifetime));
        }

        P1 = individualProbCombo.getValue();
        P2 = legalProbCombo.getValue();

        if (!isValid) {
            new Alert(Alert.AlertType.ERROR, errorMessage.toString()).showAndWait();
        }

        return isValid;
    }

    private void update() {
        long currentTime = System.currentTimeMillis();

        synchronized (renderLock) {
            SimulationRecords.getInstance().removeExpired(currentTime - startTime);
            
            if (currentTime - lastIndividualSpawn >= N1 && random.nextDouble() < P1) {
                double x = random.nextDouble() * (width - panelWidth - individualImage.getWidth());
                double y = random.nextDouble() * (height - individualImage.getHeight());
                long birthTime = currentTime - startTime;
                SimulationRecords.getInstance().addPersonRecord(x, y, individualImage, birthTime, individualLifetime);
                lastIndividualSpawn = currentTime;
            }
            
            if (currentTime - lastLegalSpawn >= N2 && random.nextDouble() < P2) {
                double x = random.nextDouble() * (width - panelWidth - legalImage.getWidth());
                double y = random.nextDouble() * (height - legalImage.getHeight());
                long birthTime = currentTime - startTime;
                SimulationRecords.getInstance().addLegalEntityRecord(x, y, legalImage, birthTime, legalLifetime);
                lastLegalSpawn = currentTime;
            }
        }
    }

    private void render() {
        synchronized (renderLock) {
            gc.clearRect(0, 0, width - panelWidth, height);
            if (backgroundImage != null) {
                gc.drawImage(backgroundImage, 0, 0, width - panelWidth, height);
            } else {
                gc.setFill(Color.LIGHTGRAY);
                gc.fillRect(0, 0, width - panelWidth, height);
            }
            
            // Draw boundaries for quadrants
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeLine((width - panelWidth) / 2, 0, (width - panelWidth) / 2, height);
            gc.strokeLine(0, height / 2, width - panelWidth, height / 2);
            
            // Draw records
            SimulationRecords.getInstance().getRecords().forEach(record -> record.draw(gc));
            
            if (showTime && isSimulationRunning) {
                long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                gc.setFill(Color.BLACK);
                gc.setFont(new Font("Arial", 20));
                gc.fillText("Время: " + elapsed + "с", 10, 30);
            }
            
            if (!isSimulationRunning) {
                long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                gc.setFill(Color.BLACK);
                gc.setFont(new Font("Arial", 20));
                gc.fillText("Результаты симуляции:", 10, 60);
                gc.setFill(Color.BLUE);
                gc.fillText("Количество физ. лиц: " + countRecords(PersonRecord.class), 10, 90);
                gc.setFill(Color.GREEN);
                gc.fillText("Количество юр. лиц: " + countRecords(LegalEntityRecord.class), 10, 120);
                gc.setFill(Color.RED);
                gc.fillText("Время симуляции: " + elapsed + "с", 10, 150);
            }
        }
    }

    @FXML
    private void clearRecords() {
        synchronized (renderLock) {
            SimulationRecords.getInstance().clearRecords();
            render();
        }
    }

    private void showResultsDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/results-dialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Результаты симуляции");
            dialogStage.setScene(new Scene(loader.load()));

            ResultsDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            String results = String.format(
                    "Результаты симуляции:\n" +
                            "Количество физ. лиц: %d\n" +
                            "Количество юр. лиц: %d\n" +
                            "Время симуляции: %d секунд",
                    countRecords(PersonRecord.class),
                    countRecords(LegalEntityRecord.class),
                    elapsed
            );
            controller.setResults(results);

            dialogStage.showAndWait();

            if (controller.isOKPressed()) {
                updateButtonStates();
            } else {
                isSimulationRunning = true;
                playMusic();
                timer.start();
                startAIThreads();
                updateButtonStates();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Не удалось загрузить диалог результатов: " + e.getMessage()).showAndWait();
        }
    }

    private int countRecords(Class<? extends Record> clazz) {
        return (int) SimulationRecords.getInstance().getRecords().stream().filter(r -> r.getClass().equals(clazz)).count();
    }

    private void setupHandlers() {
        clearButton.setOnAction(e -> clearRecords());
        showTimeRadio.setOnAction(e -> { showTime = true; render(); });
        hideTimeRadio.setOnAction(e -> { showTime = false; render(); });
        currentObjectsButton.setOnAction(e -> showCurrentObjects());
        
        // New AI control handlers
        personAIPauseButton.setOnAction(e -> togglePersonAI());
        legalEntityAIPauseButton.setOnAction(e -> toggleLegalEntityAI());
        personAIPriorityCombo.setOnAction(e -> updatePersonAIPriority());
        legalEntityAIPriorityCombo.setOnAction(e -> updateLegalEntityAIPriority());
    }

    private void setupTooltips() {
        showInfoCheckBox.setTooltip(new Tooltip("Показывать диалог с результатами после остановки симуляции"));
        showTimeRadio.setTooltip(new Tooltip("Показывать время симуляции на холсте во время работы"));
        hideTimeRadio.setTooltip(new Tooltip("Скрыть время симуляции на холсте"));
        clearButton.setTooltip(new Tooltip("Очистить все объекты с холста"));
        startButton.setTooltip(new Tooltip("Запустить симуляцию (клавиша B)"));
        stopButton.setTooltip(new Tooltip("Остановить симуляцию (клавиша E)"));
        currentObjectsButton.setTooltip(new Tooltip("Показать список текущих объектов"));
        individualSpawnField.setTooltip(new Tooltip("Период появления физических лиц (в миллисекундах)"));
        legalSpawnField.setTooltip(new Tooltip("Период появления юридических лиц (в миллисекундах)"));
        individualProbCombo.setTooltip(new Tooltip("Вероятность появления физических лиц (0.0–1.0)"));
        legalProbCombo.setTooltip(new Tooltip("Вероятность появления юридических лиц (0.0–1.0)"));
        individualLifetimeField.setTooltip(new Tooltip("Время жизни физических лиц (в миллисекундах)"));
        legalLifetimeField.setTooltip(new Tooltip("Время жизни юридических лиц (в миллисекундах)"));
        
        // New tooltips for AI controls
        personAIPauseButton.setTooltip(new Tooltip("Приостановить/возобновить движение физических лиц"));
        legalEntityAIPauseButton.setTooltip(new Tooltip("Приостановить/возобновить движение юридических лиц"));
        personAIPriorityCombo.setTooltip(new Tooltip("Установить приоритет потока для физических лиц"));
        legalEntityAIPriorityCombo.setTooltip(new Tooltip("Установить приоритет потока для юридических лиц"));
    }

    public void toggleShowTime() {
        showTime = !showTime;
        render();
    }

    private void updateButtonStates() {
        startButton.setDisable(isSimulationRunning);
        stopButton.setDisable(!isSimulationRunning);
        startMenuItem.setDisable(isSimulationRunning);
        stopMenuItem.setDisable(!isSimulationRunning);
        
        // Update AI control buttons
        boolean aiEnabled = isSimulationRunning;
        personAIPauseButton.setDisable(!aiEnabled);
        legalEntityAIPauseButton.setDisable(!aiEnabled);
        personAIPriorityCombo.setDisable(!aiEnabled);
        legalEntityAIPriorityCombo.setDisable(!aiEnabled);
        
        if (!aiEnabled) {
            personAIPauseButton.setText("Приостановить физ. лица");
            legalEntityAIPauseButton.setText("Приостановить юр. лица");
        }
    }
}