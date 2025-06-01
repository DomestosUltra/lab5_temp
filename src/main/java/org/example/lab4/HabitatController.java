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

import java.io.File;
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

    @FXML
    public void initialize() {
        gc = simulationCanvas.getGraphicsContext2D();
        
        // Загружаем конфигурацию
        // loadConfiguration();

        for (double i = 0.0; i <= 1.0; i += 0.1) {
            individualProbCombo.getItems().add(Math.round(i * 10.0) / 10.0);
            legalProbCombo.getItems().add(Math.round(i * 10.0) / 10.0);
        }
        
        // Применяем настрой