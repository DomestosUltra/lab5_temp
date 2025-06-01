package org.example.lab4;

import java.io.*;
import java.util.Properties;

public class SimulationConfig {
    private static final String CONFIG_FILE = "simulation.properties";
    
    private int individualSpawnPeriod = 2000;
    private int legalSpawnPeriod = 3000;
    private double individualProbability = 0.8;
    private double legalProbability = 0.8;
    private long individualLifetime = 30000;
    private long legalLifetime = 30000;
    private boolean showInfo = false;
    private boolean showTime = false;
    private String personAIPriority = "Нормальный";
    private String legalEntityAIPriority = "Нормальный";
    
    public void loadConfig() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            
            individualSpawnPeriod = Integer.parseInt(props.getProperty("individual.spawn.period", "2000"));
            legalSpawnPeriod = Integer.parseInt(props.getProperty("legal.spawn.period", "3000"));
            individualProbability = Double.parseDouble(props.getProperty("individual.probability", "0.8"));
            legalProbability = Double.parseDouble(props.getProperty("legal.probability", "0.8"));
            individualLifetime = Long.parseLong(props.getProperty("individual.lifetime", "30000"));
            legalLifetime = Long.parseLong(props.getProperty("legal.lifetime", "30000"));
            showInfo = Boolean.parseBoolean(props.getProperty("show.info", "false"));
            showTime = Boolean.parseBoolean(props.getProperty("show.time", "false"));
            personAIPriority = props.getProperty("person.ai.priority", "Нормальный");
            legalEntityAIPriority = props.getProperty("legal.ai.priority", "Нормальный");
            
        } catch (IOException | NumberFormatException e) {
            System.out.println("Конфигурационный файл не найден или поврежден, используются значения по умолчанию");
        }
    }
    
    public void saveConfig() {
        Properties props = new Properties();
        props.setProperty("individual.spawn.period", String.valueOf(individualSpawnPeriod));
        props.setProperty("legal.spawn.period", String.valueOf(legalSpawnPeriod));
        props.setProperty("individual.probability", String.valueOf(individualProbability));
        props.setProperty("legal.probability", String.valueOf(legalProbability));
        props.setProperty("individual.lifetime", String.valueOf(individualLifetime));
        props.setProperty("legal.lifetime", String.valueOf(legalLifetime));
        props.setProperty("show.info", String.valueOf(showInfo));
        props.setProperty("show.time", String.valueOf(showTime));
        props.setProperty("person.ai.priority", personAIPriority);
        props.setProperty("legal.ai.priority", legalEntityAIPriority);
        
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Simulation Configuration");
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении конфигурации: " + e.getMessage());
        }
    }
    
    // Геттеры и сеттеры
    public int getIndividualSpawnPeriod() { return individualSpawnPeriod; }
    public void setIndividualSpawnPeriod(int individualSpawnPeriod) { this.individualSpawnPeriod = individualSpawnPeriod; }
    
    public int getLegalSpawnPeriod() { return legalSpawnPeriod; }
    public void setLegalSpawnPeriod(int legalSpawnPeriod) { this.legalSpawnPeriod = legalSpawnPeriod; }
    
    public double getIndividualProbability() { return individualProbability; }
    public void setIndividualProbability(double individualProbability) { this.individualProbability = individualProbability; }
    
    public double getLegalProbability() { return legalProbability; }
    public void setLegalProbability(double legalProbability) { this.legalProbability = legalProbability; }
    
    public long getIndividualLifetime() { return individualLifetime; }
    public void setIndividualLifetime(long individualLifetime) { this.individualLifetime = individualLifetime; }
    
    public long getLegalLifetime() { return legalLifetime; }
    public void setLegalLifetime(long legalLifetime) { this.legalLifetime = legalLifetime; }
    
    public boolean isShowInfo() { return showInfo; }
    public void setShowInfo(boolean showInfo) { this.showInfo = showInfo; }
    
    public boolean isShowTime() { return showTime; }
    public void setShowTime(boolean showTime) { this.showTime = showTime; }
    
    public String getPersonAIPriority() { return personAIPriority; }
    public void setPersonAIPriority(String personAIPriority) { this.personAIPriority = personAIPriority; }
    
    public String getLegalEntityAIPriority() { return legalEntityAIPriority; }
    public void setLegalEntityAIPriority(String legalEntityAIPriority) { this.legalEntityAIPriority = legalEntityAIPriority; }
}